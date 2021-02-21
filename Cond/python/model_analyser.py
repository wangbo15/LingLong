import pickle
from Utils.config import Configure
from xgboost import XGBClassifier, plot_importance
from matplotlib import pyplot
import sys

sys.path.append("/usr/bin/dot")

c1 = Configure(
    'math',
    '12',
    'bottom_up',
    1,
    'model/',
    'input/',
    'output/',
    0
)

c2 = Configure(
    'lang',
    '5',
    'bottom_up',
    1,
    'model/',
    'input/',
    'output/',
    0
)

c3 = Configure(
    'chart',
    '4',
    'bottom_up',
    1,
    'model/',
    'input/',
    'output/',
    0
)

c4 = Configure(
    'time',
    '11',
    'bottom_up',
    1,
    'model/',
    'input/',
    'output/',
    0
)

def process_model(path):
    model = pickle.load(open(path, 'r'))
    importance = model.get_score(importance_type='weight')
    res = sorted(importance.items(), lambda x, y: cmp(x[1], y[1]), reverse=True)
    return res


if __name__ == '__main__':

    projs = [c1, c2, c3, c4]

    ori_expr = 0
    spe_expr = 0

    ori_v0 = 0
    spe_v0 = 0

    ori_var = 0
    spe_var = 0

    current_ori, current_spe = 0, 0
    for config in projs:
        print config.prognm_and_id
        expr_mode = config.get_expr_model_file()
        res = process_model(expr_mode)

        for i in res:
            fea_name, weight = i[0], i[1]
            if fea_name in Configure.get_expr_tags_for_pca():
                # print i
                ori_expr += weight
            elif 'p_' in fea_name:
                # print i
                spe_expr += weight

        v0_model = config.get_v0_model_file()
        res = process_model(v0_model)
        for i in res:
            fea_name, weight = i[0], i[1]
            if fea_name in Configure.get_v0_tags_for_pca():
                # print i
                ori_v0 += weight
            elif 'p_' in fea_name:
                # print i
                spe_v0 += weight

        var_model = config.get_var_model_file()
        res = process_model(var_model)
        for i in res:
            fea_name, weight = i[0], i[1]
            if fea_name in Configure.get_var_tags_for_pca():
                # print i
                ori_var += weight
            elif 'p_' in fea_name:
                # print i
                spe_var += weight

        print ori_expr + ori_v0 + ori_var - current_ori
        print spe_expr + spe_v0 + spe_var - current_spe
        current_ori = ori_expr + ori_v0 + ori_var
        current_spe = spe_expr + spe_v0 + spe_var
    #plot_tree(model)
    #pyplot.show()

    print 'V0: ', ori_v0, spe_v0, spe_v0 / float(ori_v0)
    print 'EXPR: ', ori_expr, spe_expr, spe_expr / float(ori_expr)
    print 'VAR: ', ori_var, spe_var, spe_var / float(ori_var)

    #print



