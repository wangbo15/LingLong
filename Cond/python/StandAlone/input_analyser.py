import ConfigParser
import os
import pandas as pd
import numpy as np


pred_config = ConfigParser.ConfigParser()
pred_config.read("env.conf")
home_path = os.environ['HOME']


def get_expr(fn):
    data = pd.read_csv(fn, sep='\t', header=0, encoding='utf-8')
    ids = np.array(data['id']).tolist()
    preds = np.array(data['pred']).tolist()
    res = {}
    for i in range(0, len(preds)):
        cond_id = ids[i]
        res[cond_id] = preds[i]

    return res


def compare_td_bu_expr_data():
    prefix = '/home/linglong/workspace/eclipse/Condition/python/input/time/time_27/expr/'
    bu = get_expr(prefix + 'time_27.expr.csv')
    td = get_expr(prefix + 'time_27.topdown.expr.csv')

    bu_set = set(bu.keys())
    td_set = set(td.keys())

    l1 = list(td_set - bu_set)
    l1.sort()

    print 'TD - BU: ', len(l1)
    for key in l1:
        print td[key]
    print l1

    l2 = list((bu_set - td_set))
    l2.sort()
    print 'BU - TD: ', len(l2)
    print l2


def get_training_data_with(y):
    # 101 $ <= 0	1.00000000000000000
    path = '/home/linglong/tmp/top_down.expr.csv'
    data = pd.read_csv(path, sep='\t', header=0, encoding='utf-8')
    targets = data.loc[data['pred'] == y]

    path = '/home/linglong/tmp/top_down.filter.expr.csv'
    targets.to_csv(path, sep='\t', header=True, index=False)


if __name__ == '__main__':
    # compare_td_bu_expr_data()
    get_training_data_with(101)

