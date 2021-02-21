import ConfigParser
import os
import pandas as pd
import numpy as np


pred_config = ConfigParser.ConfigParser()
pred_config.read("env.conf")
home_path = os.environ['HOME']


def compare_seq_prob(td, bu):
    td_data = pd.read_csv(td, sep='\t', header=0, encoding='utf-8')
    bu_data = pd.read_csv(bu, sep='\t', header=0, encoding='utf-8')

    td_expr_prob = np.array(td_data['p0']).tolist()
    bu_expr_prob = np.array(bu_data['p1']).tolist()

    print '>>>>>>>>>>>>>>>>>> TD_EXPR VS BU_EXPR >>>>>>>>>>>>>>>>'
    for td, bu in zip(td_expr_prob, bu_expr_prob):
        if td < bu:
            print td, bu

    td_var_prob = np.array(td_data['p1']).tolist()
    bu_v0_prob = np.array(bu_data['p0']).tolist()
    print '>>>>>>>>>>>>>>>>>> TD_VAR VS BU_V0 >>>>>>>>>>>>>>>>'
    for td, bu in zip(td_var_prob, bu_v0_prob):
        if td > bu:
            print td, bu, (td - bu)


def analysis_seq(prefix):
    bu_seq = prefix + 'bu.dijkstra.400-2020-05-04-11-42-50-seq.csv'
    td_seq = prefix + 'td.dijkstra.400-2020-05-04-11-42-11-seq.csv'
    compare_seq_prob(td_seq, bu_seq)


def get_ranking(fn):
    data = pd.read_csv(fn, sep='\t', header=0, encoding='utf-8')
    rank = np.array(data['rank']).tolist()
    ids = np.array(data['condid']).tolist()
    res = {}
    for i in range(0, len(rank)):
        if rank[i] >= 0:
            cond_id = ids[i]
            res[cond_id] = rank[i]

    return res


def analysis_ranking(prefix):
    td_rank = get_ranking(prefix + 'td.dijkstra.400-2020-05-02-19-02-20-ranking.csv')
    bu_rank = get_ranking(prefix + 'bu.dijkstra.400-2020-05-02-16-39-16-ranking.csv')

    td_set = set(td_rank.keys())
    bu_set = set(bu_rank.keys())

    l1 = list(td_set - bu_set)
    l1.sort()
    l2 = list((bu_set - td_set))
    l2.sort()
    print 'TD - BU: ', len(l1)
    print l1
    print 'BU - TD: ', len(l2)
    print l2


if __name__ == '__main__':
    prefix = '/home/nightwish/Documents/predall/time_27/'

    # analysis_ranking(prefix)

    analysis_seq(prefix)