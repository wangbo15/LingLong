import pandas as pd
from sklearn.model_selection import train_test_split
import xgboost as xgb
import pickle
import time
import gc
import heapq
import numpy as np

from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC

from sklearn import metrics

from Utils.config import *
from pca.PCAForXgb import PCAForXgb


class RecurNode(PCAForXgb):

    def preprocess(self, ori_file_data, all_encoders, is_training=False):
        ori_file_data.drop(['id', 'line', 'column'], axis=1, inplace=True)
        Y_col = 'nodetp'
        Y = ori_file_data[Y_col]
        ori_file_data.drop([Y_col], axis=1, inplace=True)
        X = ori_file_data
        assert X.shape[0] == Y.shape[0]

        label_encoded_cols = self.__configure__.get_label_for_recurnode()

        columns = [i for i in X.columns.tolist()]
        if is_training:
            for col in columns:
                if str(col) in label_encoded_cols:
                    all_encoders[col] = {}
                    X[col] = self.encoder_column(X[col], all_encoders[col])

            all_encoders['pred'] = {}
            Y = self.encoder_column(Y, all_encoders['pred'])

            all_columns = np.asarray(X.columns.values)
            logging.info('>> ALL COLUMNS IN {} EXPR TRAINING DATA'.format(self.__configure__.__direction__.upper()))
            logging.info(all_columns)

        #for predicting
        else:
            Y = None
            for col in columns:
                if str(col) in label_encoded_cols:
                    X[col] = self.encoder_column(X[col], all_encoders[col])

                #TODO: need to dump changed encoder for backup?

        return X, Y

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

    def train_xgb(self, X, Y):
        X_train, X_valid, y_train, y_valid = train_test_split(X, Y, test_size=0.1, random_state=7)

        gc.collect()

        logging.info('RECUR NODE TRAINING SET SIZE: {}'.format(X_train.shape))
        logging.info('VALIDATION SET SIZE: {}'.format(X_valid.shape))

        M_train = xgb.DMatrix(X_train, label=y_train)
        M_valid = xgb.DMatrix(X_valid, label=y_valid)

        clazzes = ['NONE', 'AND', 'OR', 'NOT']
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
            'num_class': len(clazzes)
        }

        num_round = 1000
        early_stop = 50
        learning_rates = [(num_round - i) / (num_round * 5.0) for i in range(num_round)]

        watchlist = [(M_train, 'train'), (M_valid, 'eval')]
        model = xgb.train(params, M_train, num_boost_round=num_round, evals=watchlist,
                          early_stopping_rounds=early_stop, learning_rates=learning_rates)

        y_pred = model.predict(M_valid)
        y_pred_label = self.get_predicated_label(y_pred)
        self.get_metrics(y_valid, y_pred_label)
        logging.info("RECUR_NODE best score: {}".format(model.best_score))
        return model

    def train_naive_bayes(self, X, Y):
        model = GaussianNB()
        model.fit(X, Y)
        return model

    def train_svm(self, X, Y):
        model = SVC(decision_function_shape='ovr', probability=True)
        model.fit(X, Y)
        return model

    def train(self):
        logging.info('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START RECUR_NODE {} >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>'.format(LEARNING_ALG.upper()))

        node_file = self.__configure__.get_raw_recur_node_train_in_file()
        ori_file_data = pd.read_csv(node_file, sep='\t', header=0, encoding='utf-8')

        for tag in self.__configure__.get_removed_label_for_recur():
            if tag in ori_file_data.columns:
                ori_file_data.drop(tag, axis=1, inplace=True)

        # label encoder
        start_time = time.time()
        all_encoders = {}
        X, Y = self.preprocess(ori_file_data, all_encoders=all_encoders, is_training=True)
        with open(self.__configure__.get_all_label_encoders(RECURNODE_MISSION), 'wb') as f:
            pickle.dump(all_encoders, f, protocol=2)

        encoder_end_time = time.time()
        logging.info('RECUR_NODE TRAINING ENCODER TIME: {} s'.format(encoder_end_time - start_time))

        start_time = time.time()

        if LEARNING_ALG == 'xgb':
            model = self.train_xgb(X, Y)
        elif LEARNING_ALG == 'nb':
            model = self.train_naive_bayes(X, Y)
        elif LEARNING_ALG == 'svm':
            model = self.train_svm(X, Y)

        model_file = self.__configure__.get_recurnode_model_file()
        with open(model_file, 'wb') as f:
            pickle.dump(model, f, protocol=2)
            print('RECUR_NODE MODEL SAVED AS {}'.format(model_file))

        train_end_time = time.time()
        logging.info("RECUR_NODE {} Training Time {} s".format(LEARNING_ALG, train_end_time - start_time))

    def get_metrics(self, y_true, y_pred):
        classify_report = metrics.classification_report(y_true, y_pred)
        logging.info(classify_report)

    def predict(self):
        data_file_path = self.__configure__.get_raw_recurnode_pred_in_file()
        data = pd.read_csv(data_file_path, sep='\t', header=0, encoding='utf-8')

        with open(self.__configure__.get_all_label_encoders(RECURNODE_MISSION), 'r') as f:
            all_encoders = pickle.load(f)

        start = time.time()
        X, nop_Y = self.preprocess(data, all_encoders=all_encoders, is_training=False)
        end = time.time()
        print 'PREPRO TIME: ', (end - start)

        model_file = self.__configure__.get_recurnode_model_file()
        xgb_recur_model = pickle.load(open(model_file, 'r'))

        M_pred = xgb.DMatrix(X)
        y_prob = xgb_recur_model.predict(M_pred)
        end = time.time()

        line = y_prob[0] # only has one line
        recur_node_type_num = 4
        alts = heapq.nlargest(recur_node_type_num, range(len(line)), line.__getitem__)
        recur_predicted = self.__configure__.get_recurnode_pred_out_file()
        if os.path.exists(recur_predicted):
            os.remove(recur_predicted)
        with open(recur_predicted, 'w') as output:
            for j in range(recur_node_type_num):
                label = alts[j]
                output.write('{}'.format(label))  # predicate
                output.write('\t%.17f' % line[alts[j]])
                output.write('\n')
                # print j, '\t', original, '\t', line[alts[j]]