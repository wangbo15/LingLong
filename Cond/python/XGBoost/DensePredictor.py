from Utils.config import *
from pca.PCAColumn import PCAColumn
from Utils.string_utils import preprocess_numbers
from Utils.string_utils import get_bool_value


import pickle as pk
import pandas as pd
import xgboost as xgb
import heapq
from StringIO import StringIO
from run_predict_server import MSG_END
import ConfigParser


ENV_CONFIG = ConfigParser.ConfigParser()
ENV_CONFIG.read("env.conf")
LEARNING_ALG = ENV_CONFIG.get('LEARNING', 'algorithm')
ACTIVATE_FS = get_bool_value(ENV_CONFIG.get('LEARNING', 'feature_selector'))
REMOVE_PCA_FEATURES = get_bool_value(ENV_CONFIG.get('LEARNING', 'remove_pca_processed_features'))


class DensePredictor:

    def __init__(self, config):
        assert isinstance(config, Configure)
        self.configure = config

        self.xgb_models = {}
        self.pca_model = {}
        self.pca_del_list = {}
        self.label_encoder = {}
        self.feature_selector = {}

        if config.__direction__ == BU_DIRE:
            steps = [V0_MISSION, EXPR_MISSION, VAR_MISSION]
        elif config.__direction__ == TD_DIRE:
            steps = [EXPR_MISSION, VAR_MISSION]
        elif config.__direction__ == RC_DIRE:
            steps = [EXPR_MISSION, VAR_MISSION, RECURNODE_MISSION]
        elif config.__direction__ == RCBU_DIRE:
            steps = [RCBU_V0_MISSION, RCBU_V1_MISSION, RCBU_E0_MISSION, RCBU_E1_MISSION, RCBU_R0_MISSION, RCBU_R1_MISSION]
        else:
            assert False

        print 'All loading steps: ', steps

        if config.__direction__ != RCBU_DIRE:
            pca_steps = [V0_MISSION, VAR_MISSION, EXPR_MISSION]
        else:
            pca_steps = [RCBU_V0_MISSION, RCBU_V1_MISSION, RCBU_E0_MISSION, RCBU_E1_MISSION]

        for step in steps:
            # xgb_model
            xgb_model_path = config.get_model_file(config.__direction__, step)

            assert os.path.exists(xgb_model_path)

            print 'Loading: ', xgb_model_path
            # print xgb_model_path
            self.xgb_models[step] = pk.load(open(xgb_model_path, 'r'))

            if step in pca_steps:
                # pca_model
                pca_model_path = config.get_pca_model(step)

                assert os.path.exists(pca_model_path)

                # print pca_model_path
                print 'Loading: ', pca_model_path
                self.pca_model[step] = pk.load(open(pca_model_path, 'r'))

                # pca_del_list
                del_list_file = config.get_deleted_zero_columm(step)

                assert os.path.exists(del_list_file)

                # print del_list_file
                print 'Loading: ', del_list_file
                self.pca_del_list[step] = pk.load(open(del_list_file, 'r'))

            # label_encoder
            label_encoder_file = config.get_all_label_encoders(step)

            assert os.path.exists(label_encoder_file)

            # print label_encoder_file
            print 'Loading: ', label_encoder_file
            self.label_encoder[step] = pk.load(open(label_encoder_file, 'r'))

        v0_cols = self.configure.get_label_for_v0()
        var_cols = self.configure.get_label_for_var()
        expr_cols = self.configure.get_label_for_expr()

        if config.__direction__ != RCBU_DIRE:
            all_expr_encoders = self.label_encoder['expr']
            Y_encoder = all_expr_encoders['pred']
            self.expr_pred_inverse_encoder = dict(zip(Y_encoder.values(), Y_encoder.keys()))
            self.all_label_encoded_col = {'v0': v0_cols, 'var': var_cols, 'expr': expr_cols, "recurnode": expr_cols}
        else:
            all_e0_encoders = self.label_encoder['e0']
            Y0 = all_e0_encoders['pred']
            self.e0_pred_inverse_encoder = dict(zip(Y0.values(), Y0.keys()))

            all_e1_encoders = self.label_encoder['e1']
            Y1 = all_e1_encoders['pred']
            self.e1_pred_inverse_encoder = dict(zip(Y1.values(), Y1.keys()))

            self.all_label_encoded_col = {'v0': v0_cols, 'v1': var_cols, 'e0': expr_cols, "e1": expr_cols, 'r0': expr_cols, "r1": expr_cols}

        if ACTIVATE_FS:
            self.feature_selector = {}
            if config.__direction__ != RCBU_DIRE:
                expr_selector_path = self.configure.get_feature_selection_model('expr')
                assert os.path.exists(expr_selector_path)
                expr_selector = pk.load(open(expr_selector_path, 'r'))
                print 'Loading: ', expr_selector_path
                self.feature_selector[EXPR_MISSION] = expr_selector
            else:
                for ex in [RCBU_E0_MISSION, RCBU_E1_MISSION]:
                    selector_path = self.configure.get_feature_selection_model(ex)
                    assert os.path.exists(selector_path)
                    selector = pk.load(open(selector_path, 'r'))
                    print 'Loading: ', selector_path
                    self.feature_selector[ex] = selector

                assert len(self.feature_selector) == 2

        print '>>>>>>>> Ready'

    def encoder_column(self, series, encoder_dict):
        """
        @:param series: pandas.Series
        @:param encoder: dict, str->int
        """
        series = series.copy()
        for index, val in series.iteritems():
            if val not in encoder_dict:
                encoder_dict[val] = len(encoder_dict)
            series[index] = encoder_dict[val]
            # print series[index], type(series[index])
        return series.astype('int')

    def pre_process(self, misson_tp, ori_file_data, all_pca_added_df, all_encoders):
        """
        :param ori_file_data: DataFrame
        :param all_pca_added_df: DataFrame
        :return X: matrix of features, Y: labels
        """
        assert all_pca_added_df is None or ori_file_data.shape[0] == all_pca_added_df.shape[0]

        ori_file_data.drop(['id', 'line', 'column'], axis=1, inplace=True)

        all_var_missions = [V0_MISSION, VAR_MISSION, RCBU_V0_MISSION, RCBU_V1_MISSION]
        all_expr_missions = [EXPR_MISSION, RCBU_E0_MISSION, RCBU_E1_MISSION]

        if misson_tp in all_var_missions:
            ori_file_data.drop(['putin'], axis=1,inplace=True)
        elif misson_tp in all_expr_missions:
            ori_file_data.drop(['pred'], axis=1, inplace=True)
            '''
            dropped = self.configure.get_label_for_expr()
            for tag in dropped:
                if tag in ori_file_data.columns:
                    ori_file_data.drop([tag], axis=1, inplace=True)
            '''

        X = pd.concat([ori_file_data, all_pca_added_df], axis=1)

        columns = [i for i in X.columns.tolist() ]

        label_encoded_cols = self.all_label_encoded_col[misson_tp]

        if LEARNING_ALG == 'xgb':
            if misson_tp == VAR_MISSION or misson_tp == RCBU_V1_MISSION:
                X['num0'] = X['num0'].apply(preprocess_numbers)
                X['num1'] = X['num1'].apply(preprocess_numbers)

        for col in columns:
            if str(col) in label_encoded_cols and col in all_encoders:
                X[col] = self.encoder_column(X[col], all_encoders[col])

        if misson_tp in all_expr_missions:
            if REMOVE_PCA_FEATURES:
                transformed_tags = Configure.get_expr_tags_for_pca()
                for tag in transformed_tags:
                    if tag in X.columns:
                        X.drop([tag], axis=1, inplace=True)

            # print X.shape
            if ACTIVATE_FS:
                selector = self.feature_selector[misson_tp]
                X = selector.transform(X)

        return X

    def pre_process_without_pca_for_recur(self, ori_file_data, all_encoders, mission_type):
        ori_file_data.drop(['id', 'line', 'column'], axis=1, inplace=True)
        Y_col = 'parenttp' if mission_type == RCBU_R0_MISSION else 'nodetp'
        Y = ori_file_data[Y_col]
        ori_file_data.drop([Y_col], axis=1, inplace=True)
        if mission_type == RCBU_R1_MISSION:
            # all false
            ori_file_data.drop(['isroot'], axis=1, inplace=True)
        X = ori_file_data
        assert X.shape[0] == Y.shape[0]

        label_encoded_cols = self.configure.get_label_for_recurnode()

        columns = [i for i in X.columns.tolist()]

        # for predicting
        for col in columns:
            if str(col) in label_encoded_cols:
                X[col] = self.encoder_column(X[col], all_encoders[col])

        return X

    def get_models(self, mission_type):
        return self.pca_model[mission_type], \
               self.pca_del_list[mission_type], \
               self.xgb_models[mission_type], \
               self.label_encoder[mission_type]

    def predict_var(self, raw_data, upward):
        if upward:
            mission_type = V0_MISSION
        else:
            if self.configure.__direction__ != RCBU_DIRE:
                mission_type = VAR_MISSION
            else:
                mission_type = RCBU_V1_MISSION

        tmp_raw_data = StringIO(raw_data) # let str to be a tmp file
        data = pd.read_csv(tmp_raw_data, sep='\t', header=0, encoding='utf-8')
        all_var_name = data['varname'].copy()
        all_isfld = data['isfld'].copy()

        for tag in self.configure.get_removed_label_for_var():
            if tag in data.columns:
                data.drop(tag, axis=1, inplace=True)

        pca_model, pca_del_list, model, label_encoder = self.get_models(mission_type)
        pca_tag_list = Configure.get_tags_for_pca(mission_type)
        pca_df = PCAColumn.get_pca_dataframe(data,
                                             self.configure,
                                             mission_type,
                                             pca_tag_list,
                                             pca_model,
                                             is_training=False,
                                             all_deleted_list_dict=pca_del_list)

        X = self.pre_process(mission_type, data, pca_df, label_encoder)

        '''
        for tag in pca_tag_list:
            if tag in X.columns:
                X.drop([tag], axis=1, inplace=True)
        '''

        res = []
        if LEARNING_ALG == 'xgb':
            M_pred = xgb.DMatrix(X)
            y_prob = model.predict(M_pred)
            for i in range(y_prob.shape[0]):
                # print all_var_name[i], '\t', y_prob[i]
                if all_isfld[i]:
                    res_line = '%s#F\t%.17f' % (all_var_name[i], y_prob[i])  # is field
                else:
                    res_line = '%s\t%.17f' % (all_var_name[i], y_prob[i])  # is local
                res.append(res_line)

        else:
            # nb, svm, dt, rf
            y_prob = model.predict_proba(X)
            for i in range(len(y_prob)):
                if all_isfld[i]:
                    res_line = '%s#F\t%.17f' % (all_var_name[i], y_prob[i][1])  # is field
                else:
                    res_line = '%s\t%.17f' % (all_var_name[i], y_prob[i][1])  # is local
                res.append(res_line)

        res.append(MSG_END + '\n')
        return res

    def predict_expr(self, raw_data, mission_type=EXPR_MISSION):
        pca_model, pca_del_list, model, label_encoder = self.get_models(mission_type)

        tmp_raw_data = StringIO(raw_data) # let str to be a tmp file
        data = pd.read_csv(tmp_raw_data, sep='\t', header=0, encoding='utf-8')

        for tag in self.configure.get_removed_label_for_expr():
            if tag in data.columns:
                data.drop(tag, axis=1, inplace=True)

        transformed_tags = Configure.get_tags_for_pca(mission_type)
        pca_df = PCAColumn.get_pca_dataframe(data,
                                             self.configure,
                                             mission_type,
                                             transformed_tags,
                                             pca_model,
                                             is_training=False,
                                             all_deleted_list_dict=pca_del_list)

        X = self.pre_process(mission_type, data, pca_df, label_encoder)

        res = []
        if LEARNING_ALG == 'xgb':
            M_pred = xgb.DMatrix(X)
            y_prob = model.predict(M_pred)
            # if self.configure.__direction__ == 'bottom_up':
            line = y_prob[0]  # only has one line
        else:
            # nb, svm, dt, rf
            line = model.predict_proba(X)
            line = pd.Series(line[0])
            # path = '/home/linglong/tmp/pred.expr.csv'
            # X.to_csv(path, sep='\t', header=True, index=False)

        top = self.configure.get_gen_expr_top()
        if line.shape[0] < top:
            top = line.shape[0]
        alts = heapq.nlargest(top, range(len(line)), line.__getitem__)

        # print '>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>'
        for j in range(top):
            label = alts[j]
            if self.configure.__direction__ != RCBU_DIRE:
                original = self.expr_pred_inverse_encoder[label]
            elif mission_type == RCBU_E0_MISSION:
                original = self.e0_pred_inverse_encoder[label]
            elif mission_type == RCBU_E1_MISSION:
                original = self.e1_pred_inverse_encoder[label]
            else:
                assert False

            res_line = '%s\t%.17f' % (original, line[alts[j]])
            res.append(res_line)
            # print label, res_line


        res.append(MSG_END + '\n')
        return res

    def predict_recurnode(self, raw_data, mission_type):
        model = self.xgb_models[mission_type]
        label_encoder = self.label_encoder[mission_type]

        tmp_raw_data = StringIO(raw_data) # let str to be a tmp file
        data = pd.read_csv(tmp_raw_data, sep='\t', header=0, encoding='utf-8')

        for tag in self.configure.get_removed_label_for_recur():
            if tag in data.columns:
                data.drop(tag, axis=1, inplace=True)

        X = self.pre_process_without_pca_for_recur(data, label_encoder, mission_type)

        res = []
        if LEARNING_ALG == 'xgb':
            M_pred = xgb.DMatrix(X)
            y_prob = model.predict(M_pred)

            # if self.configure.__direction__ == 'bottom_up':
            line = y_prob[0]  # only has one line

        else:
            # nb, svm, dt, rf
            y_prob = model.predict_proba(X)
            line = pd.Series(y_prob[0])

        alts = heapq.nlargest(line.shape[0], range(len(line)), line.__getitem__)

        for j in range(line.shape[0]):
            label = alts[j]
            res_line = '%s\t%.17f' % (label, line[alts[j]])
            res.append(res_line)

        res.append(MSG_END + '\n')
        return res

    def predict_recurexpr(self, raw_data):
        mission_type = 'expr'
        pca_model, pca_del_list, model, label_encoder = self.get_models(mission_type)

        tmp_raw_data = StringIO(raw_data) # let str to be a tmp file
        data = pd.read_csv(tmp_raw_data, sep='\t', header=0, encoding='utf-8')

        for tag in self.configure.get_removed_label_for_expr():
            if tag in data.columns:
                data.drop(tag, axis=1, inplace=True)

        transformed_tags = Configure.get_tags_for_pca(mission_type)
        pca_df = PCAColumn.get_pca_dataframe(data,
                                             self.configure,
                                             mission_type,
                                             transformed_tags,
                                             pca_model,
                                             is_training=False,
                                             all_deleted_list_dict=pca_del_list)

        X = self.pre_process(mission_type, data, pca_df, label_encoder)

        if LEARNING_ALG == 'xgb':
            M_pred = xgb.DMatrix(X)
            y_prob = model.predict(M_pred)
            line = y_prob[0]  # only has one line
        else:
            line = model.predict_proba(X)
            line = pd.Series(line[0])

        res = []
        # if self.configure.__direction__ == 'bottom_up':
        top = self.configure.get_gen_expr_top()
        if line.shape[0] < top:
            top = line.shape[0]

        alts = heapq.nlargest(top, range(len(line)), line.__getitem__)

        for j in range(top):
            label = alts[j]
            original = self.expr_pred_inverse_encoder[label]
            res_line = '%s\t%.17f' % (original, line[alts[j]])
            res.append(res_line)

        res.append(MSG_END + '\n')
        return res

    def predict_recurvar(self, raw_data):
        mission_type = 'var'
        tmp_raw_data = StringIO(raw_data) # let str to be a tmp file
        data = pd.read_csv(tmp_raw_data,    sep='\t', header=0, encoding='utf-8')
        all_var_name = data['varname'].copy()
        all_isfld = data['isfld'].copy()

        for tag in self.configure.get_removed_label_for_var():
            if tag in data.columns:
                data.drop(tag, axis=1, inplace=True)

        pca_model, pca_del_list, model, label_encoder = self.get_models(mission_type)
        pca_tag_list = Configure.get_tags_for_pca(mission_type)
        pca_df = PCAColumn.get_pca_dataframe(data,
                                             self.configure,
                                             mission_type,
                                             pca_tag_list,
                                             pca_model,
                                             is_training=False,
                                             all_deleted_list_dict=pca_del_list)

        X = self.pre_process(mission_type, data, pca_df, label_encoder)

        res = []
        if LEARNING_ALG == 'xgb':
            M_pred = xgb.DMatrix(X)
            y_prob = model.predict(M_pred)
            for i in range(y_prob.shape[0]):
                # print all_var_name[i], '\t', y_prob[i]
                if all_isfld[i]:
                    res_line = '%s#F\t%.17f' % (all_var_name[i], y_prob[i])  # is field
                else:
                    res_line = '%s\t%.17f' % (all_var_name[i], y_prob[i])  # is local
                res.append(res_line)

        else:
            y_prob = model.predict_proba(X)
            for i in range(len(y_prob)):
                if all_isfld[i]:
                    res_line = '%s#F\t%.17f' % (all_var_name[i], y_prob[i][1])  # is field
                else:
                    res_line = '%s\t%.17f' % (all_var_name[i], y_prob[i][1])  # is local
                res.append(res_line)

        res.append(MSG_END + '\n')
        return res
