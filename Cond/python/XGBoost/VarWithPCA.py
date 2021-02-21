import time
import pandas as pd
import pickle
import xgboost as xgb
import os
import gc
import logging
from sklearn import metrics

from sklearn.model_selection import train_test_split
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC

import numpy as np

from pca.PCAForXgb import PCAForXgb
from pca.PCAColumn import PCAColumn
from Utils.config import *
from Utils.string_utils import preprocess_numbers
import ConfigParser


ENV_CONFIG = ConfigParser.ConfigParser()
ENV_CONFIG.read("env.conf")
LEARNING_ALG = ENV_CONFIG.get('LEARNING', 'algorithm')


class VarWithPCA(PCAForXgb):

    def preprocess(self, ori_file_data, all_pca_added_df, misson_tp, all_encoders, is_training=False):
        """
        :param ori_file_data: DataFrame
        :param all_pca_added_df: DataFrame
        :return X: matrix of features, Y: labels
        """
        # assert ori_file_data.shape[0] == all_pca_added_df.shape[0]

        ori_file_data.drop(['id', 'line', 'column'], axis=1, inplace=True)
        Y = ori_file_data['putin']
        ori_file_data.drop(['putin'], axis=1,inplace=True)


        # TODO: for debug
        # ori_file_data.info()
        # all_pca_added_df.info()

        X = pd.concat([ori_file_data, all_pca_added_df], axis=1)

        columns = [i for i in X.columns.tolist() ]

        if misson_tp == V0_MISSION:
            label_encoded_cols = self.__configure__.get_label_for_v0()

        elif misson_tp == VAR_MISSION:
            label_encoded_cols = self.__configure__.get_label_for_var()

        if LEARNING_ALG == 'xgb' and misson_tp == VAR_MISSION:
             X['num0'] = X['num0'].apply(preprocess_numbers)
             X['num1'] = X['num1'].apply(preprocess_numbers)

        all_columns = np.asarray(X.columns.values)
        logging.info('>> ALL COLUMNS IN {} TRAINING DATA'.format(misson_tp.upper()))
        logging.info(all_columns)

        # for training
        if is_training:
            for col in columns:
                if str(col) in label_encoded_cols:
                    all_encoders[col] = {}
                    X[col] = self.encoder_column(X[col], all_encoders[col])

        #for predicting
        else:
            Y = None
            for col in columns:
                if str(col) in label_encoded_cols:
                    print col
                    X[col] = self.encoder_column(X[col], all_encoders[col])

        return X, Y

    def get_predicated_label(self, y_pred):
        result = []
        for item in y_pred:
            if item >= 0.5:
                result.append(True)
            else:
                result.append(False)

        # print result
        return result

    def train_xgb(self, X, Y, is_v0):
        X_train, X_valid, y_train, y_valid = train_test_split(X, Y, test_size=0.1, random_state=7)
        if is_v0:
            logging.info('V0 TRAINING SET SIZE: {}'.format(X_train.shape))
            logging.info('V0 VALIDATION SET SIZE: {}'.format(X_valid.shape))
        else:
            logging.info('ALLVAR TRAINING SET SIZE: {}'.format(X_train.shape))
            logging.info('ALLVAR VALIDATION SET SIZE: {}'.format(X_valid.shape))

        # BINARY-CLASSIFICATION PROBLEM FOR VAR
        M_train = xgb.DMatrix(X_train, label=y_train)
        M_valid = xgb.DMatrix(X_valid, label=y_valid)

        params = {
            'booster': 'gbtree',
            'objective': 'binary:logistic',
            'max_depth': 6,
            'eta': 0.1,
            'gamma': 0.2,
            'subsample': 0.7,
            'col_sample_bytree': 0.2,
            'min_child_weight': 1,
            'save_period': 0,
            'eval_metric': 'error',
            'silent': 1,
            'lambda': 2
        }

        num_round = 1000
        early_stop = 50
        learning_rates = [(num_round - i) / (num_round * 5.0) for i in range(num_round)]

        watchlist = [(M_train, 'train'), (M_valid, 'eval')]
        model = xgb.train(params, M_train, num_boost_round=num_round, evals=watchlist,
                          early_stopping_rounds=early_stop, learning_rates=learning_rates)

        logging.info("Var best score: {}".format(model.best_score))

        y_pred = model.predict(M_valid)
        y_pred_label = self.get_predicated_label(y_pred)
        self.get_metrics(y_valid, y_pred_label)

        return model

    def train_naive_bayes(self, X, Y):
        model = GaussianNB()
        model.fit(X, Y)
        return model

    def train_svm(self, X, Y):
        model = SVC(probability=True)
        model.fit(X, Y)
        return model

    def train(self, is_v0):
        pca_start_time =  time.time()

        if is_v0:
            var_file = self.__configure__.get_raw_v0_train_in_file()
            misson_tp = V0_MISSION
            model_file = self.__configure__.get_v0_model_file()
            transformed_tags = Configure.get_v0_tags_for_pca()
        else:
            misson_tp = VAR_MISSION
            var_file = self.__configure__.get_raw_var_train_in_file()
            model_file = self.__configure__.get_var_model_file()
            transformed_tags = Configure.get_var_tags_for_pca()

        logging.info('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START {} {} >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>'.format(misson_tp.upper(), LEARNING_ALG.upper()))

        ori_file_data = pd.read_csv(var_file, sep='\t', header=0, encoding='utf-8')
        I = ori_file_data['id']

        for tag in self.__configure__.get_removed_label_for_var():
            if tag in ori_file_data.columns:
                ori_file_data.drop(tag, axis=1, inplace=True)

        pd.set_option('display.max_columns', None)

        # logging.info(ori_file_data.describe(include='all'))

        all_model_set = {}

        all_pca_added_df = PCAColumn.get_pca_dataframe(ori_file_data,
                                                       self.__configure__,
                                                       misson_tp,
                                                       transformed_tags,
                                                       all_model_set,
                                                       is_training=True)

        pca_model_path = self.__configure__.get_pca_model(misson_tp)
        pickle.dump(all_model_set, open(pca_model_path, "wb"), protocol=2)

        pca_end_time = time.time()
        logging.info('VAR TRAINING PCA TIME: {} s'.format(pca_end_time - pca_start_time))

        # label encoder
        all_encoders = {}  # TODO: reusing the encoders of Expr

        X, Y = self.preprocess(ori_file_data, all_pca_added_df, misson_tp, all_encoders=all_encoders, is_training=True)

        # dump all label encoders
        with open(self.__configure__.get_all_label_encoders(misson_tp), 'wb') as f:
            pickle.dump(all_encoders, f, protocol=2)

        encoder_end_time = time.time()
        logging.info('VAR TRAINING ENCODER TIME: {} s'.format(encoder_end_time - pca_end_time))

        gc.collect()

        '''
        for tag in transformed_tags:
            if tag in X.columns:
                X.drop([tag], axis=1, inplace=True)
        '''

        if LEARNING_ALG == 'xgb':
            model = self.train_xgb(X, Y, is_v0)
        elif LEARNING_ALG == 'nb':
            # data = pd.concat([I, X, Y], axis=1)
            # if is_v0:
            #     path = '/home/nightwish/tmp/' + self.__configure__.__direction__ + '.v0.csv'
            # else:
            #     path = '/home/nightwish/tmp/' + self.__configure__.__direction__ + '.var.csv'
            # data.to_csv(path, sep='\t', header=True, index=False)
            model = self.train_naive_bayes(X, Y)
        elif LEARNING_ALG == 'svm':
            model = self.train_svm(X, Y)

        with open(model_file, 'wb') as f:
            pickle.dump(model, f, protocol=2)
            print('VAR MODEL SAVED AS {}'.format(model_file))

        train_end_time = time.time()
        logging.info("VAR Training Time {} s".format(train_end_time - pca_end_time))

    def get_metrics(self, y_true, y_pred):
        classify_report = metrics.classification_report(y_true, y_pred)
        logging.info(classify_report)

    def predict(self, is_v0):
        if is_v0:
            misson_tp = V0_MISSION
            data_file_path = self.__configure__.get_raw_v0_pred_in_file()
            transformed_tags = Configure.get_v0_tags_for_pca()
            model_file = self.__configure__.get_v0_model_file()
            var_predicted = self.__configure__.get_v0_pred_out_file()
        else:
            misson_tp = VAR_MISSION
            data_file_path = self.__configure__.get_raw_var_pred_in_file()
            transformed_tags = Configure.get_var_tags_for_pca()
            model_file = self.__configure__.get_var_model_file()
            var_predicted = self.__configure__.get_var_pred_out_file()

        data = pd.read_csv(data_file_path, sep='\t', header=0, encoding='utf-8')

        all_var_name = data['varname'].copy()
        all_isfld = data['isfld'].copy()

        start = time.time()
        pca_model_path = self.__configure__.get_pca_model(misson_tp)
        with open(pca_model_path, 'r') as f:
            all_model_set = pickle.load(f)

        del_list_file = self.__configure__.get_deleted_zero_columm(misson_tp)
        with open(del_list_file, 'r') as f:
            all_deleted_list_dict = pickle.load(f)

        end = time.time()
        print 'LOAD PCA TIME: ', (end - start)

        pca_df = PCAColumn.get_pca_dataframe(data, self.__configure__,
                                             misson_tp,
                                             transformed_tags,
                                             all_model_set,
                                             is_training=False,
                                             all_deleted_list_dict=all_deleted_list_dict)

        with open(self.__configure__.get_all_label_encoders(misson_tp), 'r') as f:
            all_encoders = pickle.load(f)

        X, nop_Y = self.preprocess(data, pca_df, misson_tp, all_encoders=all_encoders, is_training=False)

        start = time.time()
        xgb_var_model = pickle.load(open(model_file, 'r'))

        end = time.time()
        print 'LOAD TIME: ', (end - start)

        M_pred = xgb.DMatrix(X)
        y_prob = xgb_var_model.predict(M_pred)

        if os.path.exists(var_predicted):
            os.remove(var_predicted)

        with open(var_predicted, 'w') as output:
            for i in range(y_prob.shape[0]):
                # print all_var_name[i], '\t', y_prob[i]
                if all_isfld[i]:
                    output.write('{}'.format(all_var_name[i]) + "#F")  #is field
                else:
                    output.write('{}'.format(all_var_name[i]))
                output.write('\t%.17f' % y_prob[i])
                output.write('\n')
