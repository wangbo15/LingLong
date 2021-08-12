import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC
from sklearn import tree
from sklearn.ensemble import RandomForestClassifier
from sklearn.feature_selection import f_classif
from sklearn.feature_selection import SelectPercentile
import xgboost as xgb
import numpy as np
import pickle
import heapq
import os
import time
import gc
from sklearn import metrics
import logging

from sklearn import cross_validation, metrics
from sklearn.model_selection import GridSearchCV
from sklearn.metrics import classification_report

from pca.PCAColumn import PCAColumn
from Utils.config import *
from Utils.string_utils import get_bool_value
from pca.PCAForXgb import PCAForXgb
import ConfigParser


ENV_CONFIG = ConfigParser.ConfigParser()
ENV_CONFIG.read("env.conf")
LEARNING_ALG = ENV_CONFIG.get('LEARNING', 'algorithm')
ACTIVATE_FS = get_bool_value(ENV_CONFIG.get('LEARNING', 'feature_selector'))
FS_PCT = int(ENV_CONFIG.get('LEARNING', 'feature_selector_pct'))

REMOVE_PCA_FEATURES = get_bool_value(ENV_CONFIG.get('LEARNING', 'remove_pca_processed_features'))


class ExprWithPCA(PCAForXgb):

    def preprocess(self, ori_file_data, all_pca_added_df, all_encoders, is_training=False):
        """
        :param ori_file_data: DataFrame
        :param all_pca_added_df: DataFrame
        :return X: matrix of features, Y: labels
        """
        # assert ori_file_data.shape[0] == all_pca_added_df.shape[0]

        ori_file_data.drop(['id', 'line', 'column'], axis=1, inplace=True)
        Y = ori_file_data['pred']
        ori_file_data.drop(['pred'], axis=1, inplace=True)
        X = pd.concat([ori_file_data, all_pca_added_df], axis=1)

        assert X.shape[0] == Y.shape[0]

        columns = [i for i in X.columns.tolist()]

        label_encoded_cols = self.__configure__.get_label_for_expr()

        # for training
        if is_training:
            for col in columns:
                if str(col) in label_encoded_cols:
                    all_encoders[col] = {}
                    X[col] = self.encoder_column(X[col], all_encoders[col])

            all_encoders['pred'] = {}
            Y = self.encoder_column(Y, all_encoders['pred'])

            if REMOVE_PCA_FEATURES:
                transformed_tags = Configure.get_expr_tags_for_pca()
                for tag in transformed_tags:
                    if tag in X.columns:
                        X.drop([tag], axis=1, inplace=True)

            all_columns = np.asarray(X.columns.values)
            logging.info('>> ALL COLUMNS IN {} EXPR TRAINING DATA'.format(self.__configure__.__direction__.upper()))
            logging.info(all_columns)

        # for predicting
        else:
            Y = None
            for col in columns:
                if str(col) in label_encoded_cols:
                    X[col] = self.encoder_column(X[col], all_encoders[col])
                # TODO: need to dump changed encoder for backup?

        return X, Y

    def select_feature(self, X, Y, mission):
        selector = SelectPercentile(f_classif, percentile=FS_PCT).fit(X, Y)
        with open(self.__configure__.get_feature_selection_model(mission), 'wb') as f:
            pickle.dump(selector, f, protocol=2)
        ori_shape = X.shape
        all_columns = np.asarray(X.columns.values)
        selected_columns = all_columns[np.asarray(selector.get_support())]
        X = selector.transform(X)
        logging.info('>> FEATURE SELECTION: {} TO {}'.format(ori_shape, X.shape))
        logging.info(selected_columns)
        return X

    def get_predicated_label(self, y_pred):
        result = []
        for row in y_pred:
            # i'th pred item
            curr_max = 0
            max_idx = -1
            s = ''
            for idx, p in enumerate(row):
                if p > curr_max:
                    curr_max = p
                    max_idx = idx

            result.append(max_idx)

        # print result
        return result

    def add_to_frequency(self, ori_file_data, frequency=5):
        ori_line = ori_file_data.shape[0]
        uniques = ori_file_data['pred'].value_counts(sort=True)

        new_ori_row_list = []
        ori_cols = ori_file_data.columns

        for x in xrange(len(ori_file_data.index)):
            new_ori_row_list.extend(np.array(ori_file_data[x:x + 1]).tolist())
            pred = ori_file_data['pred'].iloc[x]
            curr_freq = uniques[pred]
            for i in xrange(curr_freq, frequency + 1):
                new_ori_row_list.extend(np.array(ori_file_data[x:x + 1]).tolist())

        ori_file_data = pd.DataFrame(new_ori_row_list, columns=ori_cols)

        curr_line = ori_file_data.shape[0]
        logging.info('CHANGE %d LINES TO %d LINES BY FREQ %d' % (ori_line, curr_line, frequency))

        pd.set_option('display.max_columns', None)

        # logging.info(ori_file_data.describe(include='all'))
        gc.collect()
        return ori_file_data

    def filter_by_frequency(self, ori_file_data, frequency=1):
        ori_line = ori_file_data.shape[0]

        uniques = ori_file_data['pred'].value_counts(sort=True)
        more_than_freq_time = uniques[uniques > frequency].index.values.tolist()

        new_ori_row_list = []
        ori_cols = ori_file_data.columns
        for x in xrange(len(ori_file_data.index)):
            if ori_file_data['pred'].iloc[x] in more_than_freq_time:
                new_ori_row_list.extend(np.array(ori_file_data[x:x + 1]).tolist())

        ori_file_data = pd.DataFrame(new_ori_row_list, columns=ori_cols)

        curr_line = ori_file_data.shape[0]

        logging.info('ALL THE %d LINES HAS %d LINES FREQ > %d' %(ori_line, curr_line, frequency))

        # logging.info(ori_file_data.describe(include='all'))
        gc.collect()
        return ori_file_data

    def train_xgb(self, X, Y, class_num):
        X_train, X_valid, y_train, y_valid = train_test_split(X, Y, test_size=0.1, random_state=7)

        logging.info('EXPR TRAINING SET SIZE: {}'.format(X_train.shape))
        logging.info('VALIDATION SET SIZE: {}'.format(X_valid.shape))

        M_train = xgb.DMatrix(X_train, label=y_train)
        M_valid = xgb.DMatrix(X_valid, label=y_valid)

        # cv_model = self.xgb_cla ssifier_cv(X_train, y_train, X_valid, y_valid)
        model = self.xgboost_inner(M_train, M_valid, class_num)

        logging.info("EXPR best score: {}".format(model.best_score))

        y_pred = model.predict(M_valid)
        y_pred_label = self.get_predicated_label(y_pred)
        # self.get_metrics(y_valid, y_pred_label)
        return model

    def train_naive_bayes(self, X, Y):
        model = GaussianNB()
        model.fit(X, Y)
        return model

    def train_svm(self, X, Y):
        model = SVC(decision_function_shape='ovr', probability=True)
        model.fit(X, Y)
        return model

    def train_dt(self, X, Y):
        model = tree.DecisionTreeClassifier(criterion="entropy")
        model.fit(X, Y)
        return model

    def train_rf(self, X, Y):
        model = RandomForestClassifier(criterion='entropy', n_estimators=10, random_state=1, n_jobs=-1)
        model.fit(X, Y)
        return model

    def train(self, upward):
        '''
        entrance of expr training
        :return: 
        '''
        logging.info('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START EXPR %s, UPWARD: %s >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>' % (LEARNING_ALG.upper(), upward))
        pca_start_time = time.time()

        expr_file = self.__configure__.get_raw_expr_train_in_file(upward)
        ori_file_data = pd.read_csv(expr_file, sep='\t', header=0, encoding='utf-8')

        # ori_file_data = self.filter_by_frequency(ori_file_data)
        ori_file_data = self.add_to_frequency(ori_file_data)

        I = ori_file_data['id']

        all_model_set = {}

        transformed_tags = Configure.get_expr_tags_for_pca()

        for tag in self.__configure__.get_removed_label_for_expr():
            if tag in ori_file_data.columns:
                ori_file_data.drop(tag, axis=1, inplace=True)

        if self.__configure__.__direction__ == RCBU_DIRE:
            if upward:
                mission = RCBU_E0_MISSION
            else:
                mission = RCBU_E1_MISSION
        else:
            mission = EXPR_MISSION

        all_pca_added_df = PCAColumn.get_pca_dataframe(ori_file_data,
                                                   self.__configure__,
                                                   mission,
                                                   transformed_tags,
                                                   all_model_set,
                                                   is_training=True)

        pca_model_path = self.__configure__.get_pca_model(mission)
        pickle.dump(all_model_set, open(pca_model_path, "wb"), protocol=2)

        pca_end_time = time.time()
        logging.info('EXPR TRAINING PCA TIME: {} s'.format(pca_end_time - pca_start_time))

        class_num = len(ori_file_data['pred'].value_counts())
        logging.info('UNIQUE EXPR NUM: {}'.format(class_num))

        # label encoder
        all_encoders = {}

        X, Y = self.preprocess(ori_file_data, all_pca_added_df, all_encoders=all_encoders, is_training=True)

        if ACTIVATE_FS:
            X = self.select_feature(X, Y, mission)

        with open(self.__configure__.get_all_label_encoders(mission), 'wb') as f:
            pickle.dump(all_encoders, f, protocol=2)

        encoder_end_time = time.time()
        logging.info('EXPR TRAINING ENCODER TIME: {} s'.format(encoder_end_time - pca_end_time))

        gc.collect()

        if LEARNING_ALG == 'xgb':
            model = self.train_xgb(X, Y, class_num)
        elif LEARNING_ALG == 'nb':
            # data = pd.concat([I, X, Y], axis=1)
            # path = '/home/linglong/tmp/' + self.__configure__.__direction__ + '.expr.csv'
            # data.to_csv(path, sep='\t', header=True, index=False)
            model = self.train_naive_bayes(X, Y)
        elif LEARNING_ALG == 'svm':
            model = self.train_svm(X, Y)
        elif LEARNING_ALG == 'dt':
            model = self.train_dt(X, Y)

        model_file = self.__configure__.get_model_file(self.__configure__.__direction__, mission)
        with open(model_file, 'wb') as f:
            pickle.dump(model, f, protocol=2)
            print('EXPR MODEL SAVED AS {}'.format(model_file))

        train_end_time = time.time()
        logging.info("EXPR {} MODEL Training Time: {} s".format(LEARNING_ALG, train_end_time - pca_end_time))

    def xgboost_inner(self, M_train, M_valid, class_num, early_stop=50):

        params = {
            'booster': 'gbtree',
            'objective': 'multi:softprob',
            'max_depth': 7,
            'eta': 0.1,
            'gamma': 0.2,
            'subsample': 0.7,
            'col_sample_bytree': 0.2,
            'min_child_weight': 1,
            'save_period': 0,
            'eval_metric': 'merror',  # merror or mlogloss
            'silent': 1,
            'lambda': 1,
            'num_class': class_num
        }
        num_round = 1000
        learning_rates = [(num_round - i) / (num_round * 5.0) for i in range(num_round)]
        watchlist = [(M_train, 'train'), (M_valid, 'eval')]
        model = xgb.train(params, M_train, num_boost_round=num_round, evals=watchlist,
                      early_stopping_rounds=early_stop, learning_rates=learning_rates)
        return model

    def xgb_classifier_cv(self, X_train, y_train, X_valid, y_valid, early_stop=30):
        params = {
            'max_depth': 7,
            'learning_rate': 0.1,
            'objective': 'multi:softprob',
            'booster': 'gbtree',
            'gamma': 0.2,
            'subsample': 0.7,
            'colsample_bytree': 0.2,
            'min_child_weight': 1,
            'silent': False,
            # 'seed': 260817,
        }

        model = xgb.XGBClassifier(**params)

        param_test1 = {
            'max_depth': range(6, 10, 2),
            # 'min_child_weight': range(1, 6, 2)
        }

        gsearch1 = GridSearchCV(estimator=model,
                                param_grid=param_test1, scoring='roc_auc', n_jobs=8, iid=False, cv=3)

        gsearch1.fit(X_train, y_train)
        print gsearch1.grid_scores_, gsearch1.best_params_, gsearch1.best_score_

        model.fit(X_train, y_train, early_stopping_rounds=early_stop, eval_metric='merror',
                    eval_set=[(X_valid, y_valid)])
        return model

    def get_metrics(self, y_true, y_pred):
        file = open(self.__configure__.get_all_label_encoders(EXPR_MISSION), 'r')
        all_encoders = pickle.load(file)
        Y_encoder = all_encoders['pred']
        inverse_label_encoder = dict(zip(Y_encoder.values(), Y_encoder.keys()))

        y_true_ori = []
        y_pred_ori = []
        for x,y in zip(y_true, y_pred):
            y_true_ori.append(inverse_label_encoder[x])
            y_pred_ori.append(inverse_label_encoder[y])

        classify_report = metrics.classification_report(y_true_ori, y_pred_ori)
        logging.info(classify_report)
