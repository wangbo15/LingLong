import os
import logging
import sys
import ConfigParser


ENV_CONFIG = ConfigParser.ConfigParser()
ENV_CONFIG.read("env.conf")
LEARNING_ALG = ENV_CONFIG.get('LEARNING', 'algorithm')

BU_DIRE = 'bottom_up'
TD_DIRE = 'top_down'
RC_DIRE = 'recur'
PL_DIRE = 'plain'
DIRECTIONS = [BU_DIRE, TD_DIRE, RC_DIRE, PL_DIRE]

EXPR_MISSION = 'expr'
VAR_MISSION = 'var'
V0_MISSION = 'v0'
RECURNODE_MISSION = 'recurnode'
MISSION_TYPE = [EXPR_MISSION, VAR_MISSION, V0_MISSION, RECURNODE_MISSION]


class Configure(object):

    def __init__(self, project_name, bug_id, direction, expr_freq, model_path, input_path, output_path, gen_expr_top):
        self.__project_name__ = project_name
        self.__bug_id__ = bug_id
        self.prognm_and_id = project_name + '_' + bug_id

        assert direction in DIRECTIONS
        self.__direction__ = direction
        self.__expr_freq__ = expr_freq

        self.__model_path__ = model_path
        # self.__model_path__ = model_path + self.prognm_and_id.lower() + "/"
        if direction != BU_DIRE:
            if model_path.endswith('/'):
                self.__model_path__ += direction + "/"
            else:
                self.__model_path__ += "/" + direction + "/"

        self.__input_path__ = input_path
        self.__output_path__ = output_path
        self.__gen_expr_top__ = gen_expr_top
        self.__input_base_path__ = input_path + project_name + '/' + self.prognm_and_id
        self.__output_base_path__ = output_path + project_name + '/' + self.prognm_and_id

        # init folders
        self.get_expr_pred_out_file()

        logging.basicConfig(level=logging.DEBUG,
                            format='%(asctime)s %(levelname)-8s:\n%(message)s\n',
                            filename=self.__output_base_path__ + '/log.txt',
                            filemode='a')

        ch = logging.StreamHandler(sys.stdout)
        ch.setLevel(logging.INFO)
        logging.getLogger('').addHandler(ch)

    def get_project_name(self):
        return self.__project_name__

    def get_bug_id(self):
        return self.__bug_id__

    def get_expr_frequency(self):
        return self.__expr_freq__

    def get_gen_expr_top(self):
        return self.__gen_expr_top__

    def get_raw_v0_train_in_file(self):
        assert self.__direction__ == BU_DIRE
        return self.__input_base_path__ + '/var/' + self.__project_name__ + '_' + self.__bug_id__ + '.v0.csv'

    def get_raw_var_train_in_file(self):
        if self.__direction__ == TD_DIRE:
            return self.__input_base_path__ + '/var/' + self.__project_name__ + '_' + self.__bug_id__ + '.topdown.var.csv'
        elif self.__direction__ == BU_DIRE:
            return self.__input_base_path__ + '/var/' + self.__project_name__ + '_' + self.__bug_id__ + '.var.csv'
        elif self.__direction__ == RC_DIRE:
            return self.__input_base_path__ + '/var/' + self.__project_name__ + '_' + self.__bug_id__ + '.recur.var.csv'
        else:
            assert False

    def get_raw_expr_train_in_file(self):
        if self.__direction__ == TD_DIRE:
            return self.__input_base_path__ + '/expr/' + self.__project_name__ + '_' + self.__bug_id__ + '.topdown.expr.csv'
        elif self.__direction__ == BU_DIRE:
            return self.__input_base_path__ + '/expr/' + self.__project_name__ + '_' + self.__bug_id__ + '.expr.csv'
        elif self.__direction__ == RC_DIRE:
            return self.__input_base_path__ + '/expr/' + self.__project_name__ + '_' + self.__bug_id__ + '.recur.expr.csv'
        else:
            assert False

    def get_raw_recur_node_train_in_file(self):
        if self.__direction__ == RC_DIRE:
            return self.__input_base_path__ + '/recur/' + self.__project_name__ + '_' + self.__bug_id__ + '.recur.csv'
        else:
            assert False

    def get_model_path_prefix(self):
        if not os.path.exists(self.__model_path__):
            os.makedirs(self.__model_path__)
        return self.__model_path__ + self.__project_name__ + '_' + self.__bug_id__

    def get_xgb_model_file(self, direction, mission_type):

        if direction == BU_DIRE:
            if mission_type == V0_MISSION:
                return self.get_v0_model_file()

            elif mission_type == EXPR_MISSION:
                return self.get_expr_model_file()

            elif mission_type == VAR_MISSION:
                return self.get_var_model_file()

        elif direction == TD_DIRE:
            if mission_type == EXPR_MISSION:
                return self.get_expr_model_file()

            elif mission_type == VAR_MISSION:
                return self.get_var_model_file()

        elif direction == RC_DIRE:
            if mission_type == EXPR_MISSION:
                return self.get_expr_model_file()

            elif mission_type == VAR_MISSION:
                return self.get_var_model_file()

            elif mission_type == RECURNODE_MISSION:
                return self.get_recurnode_model_file()

        else:
            assert False

    def get_v0_model_file(self):
        # python/model/math_1.var_model.pkl
        if not os.path.exists(self.__model_path__):
            os.makedirs(self.__model_path__)

        if LEARNING_ALG != 'xgb':
            return self.__model_path__ + self.__project_name__ + '_' + self.__bug_id__ + '.v0_' + LEARNING_ALG + '.model.pkl'
        else:
            return self.__model_path__ + self.__project_name__ + '_' + self.__bug_id__ + '.v0_model.pkl'

    def get_var_model_file(self):
        # python/model/math_1.var_model.pkl
        if not os.path.exists(self.__model_path__):
            os.makedirs(self.__model_path__)

        if LEARNING_ALG != 'xgb':
            return self.__model_path__ + self.__project_name__ + '_' + self.__bug_id__ + '.var_' + LEARNING_ALG + '.model.pkl'
        else:
            return self.__model_path__ + self.__project_name__ + '_' + self.__bug_id__ + '.var_model.pkl'

    def get_expr_model_file(self):
        # python/model/math_1.expr_model.pkl
        if not os.path.exists(self.__model_path__):
            os.makedirs(self.__model_path__)
        if LEARNING_ALG != 'xgb':
            return self.__model_path__ + self.__project_name__ + '_' + self.__bug_id__ + '.expr_' + LEARNING_ALG + '.model.pkl'
        else:
            return self.__model_path__ + self.__project_name__ + '_' + self.__bug_id__ + '.expr_model.pkl'

    def get_recurnode_model_file(self):
        # python/model/math_1.expr_model.pkl
        if not os.path.exists(self.__model_path__):
            os.makedirs(self.__model_path__)
        return self.__model_path__ + self.__project_name__ + '_' + self.__bug_id__ + '.recurnode_model.pkl'

    def get_raw_v0_pred_in_file(self):
        # python/input/math/math_1/pred/math_1.v0.csv
        var_path = self.__input_base_path__ + '/pred/' + self.__project_name__ + '_' + self.__bug_id__ + '.v0.csv'
        return var_path

    def get_raw_var_pred_in_file(self):
        # python/input/math/math_1/pred/math_1.var.csv
        var_path = self.__input_base_path__ + '/pred/' + self.__project_name__ + '_' + self.__bug_id__ + '.var.csv'
        return var_path

    def get_raw_expr_pred_in_file(self):
        # python/input/math/math_1/pred/math_1.expr.csv
        expr_path = self.__input_base_path__ + '/pred/' + self.__project_name__ + '_' + self.__bug_id__ + '.expr.csv'
        return expr_path

    def get_raw_recurnode_pred_in_file(self):
        # python/input/math/math_1/pred/math_1.expr.csv
        expr_path = self.__input_base_path__ + '/pred/' + self.__project_name__ + '_' + self.__bug_id__ + '.recur.csv'
        return expr_path

    def get_v0_pred_out_file(self):
        # python/output/math/math_1/math_1.var.csv
        if not os.path.exists(self.__output_base_path__):
            os.makedirs(self.__output_base_path__)
        path = self.__output_base_path__ + '/' + self.__project_name__ + '_' + self.__bug_id__ + '.v0_pred.csv'
        return path

    def get_var_pred_out_file(self):
        # python/output/math/math_1/math_1.var.csv
        if not os.path.exists(self.__output_base_path__):
            os.makedirs(self.__output_base_path__)
        path = self.__output_base_path__ + '/' + self.__project_name__ + '_' + self.__bug_id__ + '.var_pred.csv'
        return path

    def get_expr_pred_out_file(self):
        # python/output/math/math_1/math_1.expr.csv
        if not os.path.exists(self.__output_base_path__):
            os.makedirs(self.__output_base_path__)
        path = self.__output_base_path__ + '/' + self.__project_name__ + '_' + self.__bug_id__ + '.expr_pred.csv'
        return path

    def get_recurnode_pred_out_file(self):
        # python/output/math/math_1/math_1.expr.csv
        if not os.path.exists(self.__output_base_path__):
            os.makedirs(self.__output_base_path__)
        path = self.__output_base_path__ + '/' + self.__project_name__ + '_' + self.__bug_id__ + '.recur_pred.csv'
        return path

    def get_joint_predict_file(self):
        # python/output/math/math_1/math_1.joint.csv
        if not os.path.exists(self.__output_base_path__):
            os.makedirs(self.__output_base_path__)
        return self.__output_base_path__ + '/' + self.__project_name__ + '_' + self.__bug_id__ + '.joint.csv'

    # added for pca
    def get_pca_model(self, mission_type):
        path = self.__model_path__ + 'pca/' # + mission_type + '/'
        if not os.path.exists(path):
            os.makedirs(path)
        if LEARNING_ALG != 'xgb':
            return path + self.__project_name__ + '_' + self.__bug_id__ + '_' + mission_type + '_' + LEARNING_ALG + ".pca.pkl"
        else:
            return path + self.__project_name__ + '_' + self.__bug_id__ + '_' + mission_type + ".pca.pkl"

    def get_deleted_zero_columm(self, mission_type):
        path = self.__model_path__ + "pca/"
        if not os.path.exists(path):
            os.makedirs(path)
        if LEARNING_ALG != 'xgb':
            return path + self.__project_name__ + '_' + self.__bug_id__ + '_' + mission_type + '_' + LEARNING_ALG + ".del.pkl"
        else:
            return path + self.__project_name__ + '_' + self.__bug_id__ + '_' + mission_type + ".del.pkl"

    def get_all_label_encoders(self, mission_type):
        assert mission_type in MISSION_TYPE
        path = self.__model_path__
        if not os.path.exists(path):
            os.makedirs(path)
        if LEARNING_ALG != 'xgb':
            return path + self.__project_name__ + '_' + self.__bug_id__ + '.' + mission_type + '_' + LEARNING_ALG + '.lb.pkl'
        else:
            return path + self.__project_name__ + '_' + self.__bug_id__ + '.' + mission_type + '.lb.pkl'

    def get_feature_selection_model(self, mission_type):
        assert mission_type in MISSION_TYPE
        path = self.__model_path__
        if not os.path.exists(path):
            os.makedirs(path)
        if LEARNING_ALG != 'xgb':
            return path + self.__project_name__ + '_' + self.__bug_id__ + '.' + mission_type + '_' + LEARNING_ALG + '.fs.pkl'
        else:
            return path + self.__project_name__ + '_' + self.__bug_id__ + '.' + mission_type + '.fs.pkl'

    @staticmethod
    def get_tags_for_pca(mission_type):
        if mission_type == V0_MISSION:
            return Configure.get_v0_tags_for_pca()
        elif mission_type == EXPR_MISSION:
            return Configure.get_expr_tags_for_pca()
        elif mission_type == VAR_MISSION:
            return Configure.get_var_tags_for_pca()
        else:
            assert False

    @staticmethod
    def get_expr_tags_for_pca():
        # expr_tags = ['tdname','methodname','allloc','allfld',
        #              'befsyn','bdsyn','afsyn',
        #              'befcd','befpre']
        expr_tags = Configure.get_v0_tags_for_pca()
        return expr_tags

    @staticmethod
    def get_v0_tags_for_pca():
        # var_tags = Configure.get_expr_tags_for_pca()
        # var_tags.extend(['pred', 'varname', 'lastpre'])

        var_tags = ['varname', 'vartype', 'tdname', 'methodname',
         'allloc', 'allloctp', 'allfld', 'allfldtp',
         'befsyn', 'bdsyn', 'afsyn',
         'varpre1st', 'varpre2nd', 'tppre1st', 'tppre2nd', 'tppre3rd',
         'pstmt0', 'pstmt1', 'nstmt0', 'nstmt1',
         'befcd', 'befpre', 'lastpre']

        # var_tags = []
        return var_tags

    @staticmethod
    def get_var_tags_for_pca():
        # var_tags = Configure.get_expr_tags_for_pca()
        # var_tags.extend(['pred', 'varname', 'lastpre'])
        var_tags = ['varname', 'vartype', 'tdname', 'methodname',
         'allloc', 'allloctp', 'allfld', 'allfldtp',
         'befsyn', 'bdsyn', 'afsyn',
         'varpre1st', 'varpre2nd', 'tppre1st', 'tppre2nd', 'tppre3rd',
         'pstmt0', 'pstmt1', 'nstmt0', 'nstmt1',
         'befcd', 'befpre', 'lastpre', 'pred']

        # var_tags = []
        return var_tags

    @staticmethod
    def get_label_for_expr():
        # 'mtdmod'
        columns = ['filename','tdname','methodname',
                'allloc', 'allloctp','allfld', 'allfldtp','bodyctl','befsyn','bdsyn','afsyn',
                'bes0','bes1','bes2','bes3','bes4','bes5','bds0','bds1','bds2','bds3','afs0','afs1',
                'afs2','afs3','lv0','lv1','lv2','lv3','befcd','befpre','pred',
                'pstmt0', 'pstmt1', 'nstmt0', 'nstmt1',
                'varname', 'vartype', 'ltt0', 'ltt1', 'ltt2', 'wd0', 'wd1', 'wd2', 'twdl0',
                'docexcp', 'docop',
                'varpre1st', 'varpre2nd', 'tppre1st', 'tppre2nd', 'tppre3rd',
                'lastassign', 'ass_op', 'ass_mtd', 'ass_name', 'ass_num',
                'bodyuse', 'castedtp', 'outuse', 'lastpre',
                  'isroot', 'parenttp', 'siblingtp', 'location', 'nodetp']
        return columns

    @staticmethod
    def get_label_for_v0():
        columns = ['filename','tdname','methodname',
                'allloc','allloctp','allfld','allfldtp','bodyctl','befsyn','bdsyn','afsyn',
                'bes0','bes1','bes2','bes3','bes4','bes5','bds0','bds1','bds2','bds3','afs0','afs1',
                'afs2','afs3','lv0','lv1','lv2','lv3','befcd','befpre','pred',
                'roottp','varname','vartype','ltt0', 'ltt1', 'ltt2','wd0', 'wd1', 'wd2', 'twdl0',
                'lastassign', 'ass_op', 'ass_mtd', 'ass_name', 'ass_num',
                'bodyuse', 'castedtp', 'outuse','lastpre',
                'docexcp', 'docop',
                'varpre1st', 'varpre2nd', 'tppre1st', 'tppre2nd', 'tppre3rd',
                'pstmt0','pstmt1','nstmt0','nstmt1']
        return columns

    @staticmethod
    def get_label_for_var():
        columns = ['filename', 'tdname', 'methodname',
                'allloc', 'allloctp', 'allfld', 'allfldtp', 'bodyctl', 'befsyn', 'bdsyn', 'afsyn',
                'bes0', 'bes1', 'bes2', 'bes3', 'bes4', 'bes5', 'bds0', 'bds1', 'bds2', 'bds3',
                'afs0', 'afs1',
                'afs2', 'afs3', 'lv0', 'lv1', 'lv2', 'lv3', 'befcd', 'befpre', 'pred',
                'roottp', 'varname', 'vartype', 'ltt0', 'ltt1', 'ltt2', 'wd0', 'wd1', 'wd2', 'twdl0',
                'lastassign', 'ass_op', 'ass_mtd', 'ass_name', 'ass_num',
                'bodyuse', 'castedtp', 'outuse', 'lastpre',
                'docexcp', 'docop',
                'varpre1st', 'varpre2nd', 'tppre1st', 'tppre2nd', 'tppre3rd',
                'pstmt0','pstmt1','nstmt0','nstmt1', 'roottp', 'ariop', 'mtd0',
                'isroot', 'parenttp', 'siblingtp', 'location', 'nodetp']

        if LEARNING_ALG != 'xgb':
            columns.extend(['num0', 'num1'])

        return columns

    @staticmethod
    def get_label_for_recurnode():
        columns = ['filename', 'tdname', 'methodname',
             'allloc', 'allloctp', 'allfld', 'allfldtp', 'bodyctl', 'befsyn', 'bdsyn', 'afsyn',
             'bes0', 'bes1', 'bes2', 'bes3', 'bes4', 'bes5', 'bds0', 'bds1', 'bds2', 'bds3',
             'afs0', 'afs1', 'afs2', 'afs3', 'lv0', 'lv1', 'lv2', 'lv3',
             'befcd', 'befpre', 'pstmt0', 'pstmt1', 'nstmt0', 'nstmt1',
             'isroot', 'parenttp', 'siblingtp', 'location']
        return columns

    @staticmethod
    def get_removed_label_for_expr():
        '''
        ['filename', 'tdname', 'methodname', 'mtdmod', 'mtdln', 'locnum', 'paranum', 'fldnum',
        'allloc', 'allloctp', 'locintnm', 'locfltnm', 'locarrnm', 'allfld', 'allfldtp', 'inloop', \
        'bodyctl', 'befsyn', 'bdsyn', 'afsyn', 'bes0', 'bes1', 'bes2', 'bes3', 'bes4', 'bes5', 'bds0', \
        'bds1', 'bds2', 'bds3', 'afs0', 'afs1', 'afs2', 'afs3', 'lv0', 'lv1', 'lv2', 'lv3', 'pstmt0', \
        'pstmt1', 'nstmt0', 'nstmt1', 'befcd', 'befpre']
        '''
        return ['allloc', 'allloctp', 'allfld', 'allfldtp', 'befsyn', 'bdsyn', 'afsyn', 'pstmt0', 'pstmt1', 'nstmt0', 'nstmt1', 'befcd', 'befpre',
                'bes0', 'bes1', 'bes2', 'bes3', 'bes4', 'bes5', 'bds0', 'bds1', 'bds2', 'bds3', 'afs0', 'afs1', 'afs2', 'afs3', 'lv0', 'lv1', 'lv2', 'lv3']
        '''
        return ['allloc', 'allloctp', 'allfld', 'allfldtp', 'befsyn', 'bdsyn', 'afsyn', 'pstmt0', 'pstmt1', 'nstmt0', 'nstmt1', 'befcd', 'befpre',
                'bes0', 'bes1', 'bes2', 'bes3', 'bes4', 'bes5', 'bds0', 'bds1', 'bds2', 'bds3', 'afs0', 'afs1', 'afs2', 'afs3', 'lv0', 'lv1', 'lv2', 'lv3',
                'occpostime',
                'varname', 'vartype', 'vnmlen', 'shortvn', 'vnmwds', 'ltt0', 'ltt1', 'ltt2', 'wd0', 'wd1', 'wd2', 'isint', 'isflt', 'isarr', 'iscoll', 'ispmtarr', 'prmtandspl',
                'twdl0', 'lastassign', 'ass_op', 'ass_mtd', 'ass_name', 'ass_num', 'dis0', 'dis0_l10', 'dis0_l20', 'dis0_g20', 'preassnum', 'isparam',
                'isfld', 'isfnl', 'isidxer', 'bodyuse', 'outuse', 'incondnum', 'filecondnum', 'totcondnum', 'lastpre', 'docexcp', 'docop', 'doczero',
                'docone', 'docnon', 'docrange', 'docincode']
        '''

    @staticmethod
    def get_removed_label_for_var():
        return ['allloc', 'allloctp', 'allfld', 'allfldtp', 'befsyn', 'bdsyn', 'afsyn', 'pstmt0', 'pstmt1', 'nstmt0', 'nstmt1', 'befcd', 'befpre']

    @staticmethod
    def get_removed_label_for_recur():
        return ['allloc', 'allloctp', 'allfld', 'allfldtp', 'befsyn', 'bdsyn', 'afsyn', 'pstmt0', 'pstmt1', 'nstmt0', 'nstmt1', 'befcd', 'befpre',
                'bes0', 'bes1', 'bes2', 'bes3', 'bes4', 'bes5', 'bds0', 'bds1', 'bds2', 'bds3', 'afs0', 'afs1', 'afs2', 'afs3', 'lv0', 'lv1', 'lv2', 'lv3']
