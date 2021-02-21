import os
import pandas as pd
import numpy as np


def analysis_seq():
    base = '/home/nightwish/Documents/predall/time_27/'
    td_path = base + 'td.beam.400.800-2020-08-05-14-10-07-seq.csv'
    bu_path = base + 'bu.beam.400.800-2020-08-05-21-37-26-seq.csv'

    td_data = pd.read_csv(td_path, sep='\t', header=0, encoding='utf-8')
    bu_data = pd.read_csv(bu_path, sep='\t', header=0, encoding='utf-8')

    td_ids = set(td_data['id'].tolist())
    bu_ids = set(bu_data['id'].tolist())

    intersection = td_ids.intersection(bu_ids)

    intersection = sorted(intersection)

    # td_data = td_data[td_data['id'].isin(intersection)]
    # bu_data = bu_data[bu_data['id'].isin(intersection)]

    # print td_data
    # print bu_data

    a = 0
    b = 0
    c = 0
    d = 0
    e = 0
    final_score_count = 0
    rank_diff_sum = 0
    count = 0
    for i in intersection:
        # print '#### %d' % i

        if td_data.loc[td_data['id'] == i, 'ranking'].iat[0] == -1 and \
                bu_data.loc[bu_data['id'] == i, 'ranking'].iat[0] == -1:
            continue

        if td_data.loc[td_data['id'] == i, 'ranking'].iat[0] != -1 and \
                bu_data.loc[bu_data['id'] == i, 'ranking'].iat[0] == -1:
            print 'TD HIT ONLY', i

        if td_data.loc[td_data['id'] == i, 'ranking'].iat[0] == -1 and \
                bu_data.loc[bu_data['id'] == i, 'ranking'].iat[0] != -1:
            print 'BU HIT ONLY', i

        if td_data.loc[td_data['id'] == i, 'p1'].iat[0] == '-':
            continue

        if td_data.loc[td_data['id'] == i, 'p2'].iat[0] != '-':
            continue

        if bu_data.loc[bu_data['id'] == i, 'p2'].iat[0] != '-':
            continue

        count += 1

        td_expr_rank = td_data.loc[td_data['id'] == i, 'ranking'].astype(float).iat[0]
        bu_expr_rank = bu_data.loc[bu_data['id'] == i, 'ranking'].astype(float).iat[0]

        td_expr_prob = td_data.loc[td_data['id'] == i, 'p0'].astype(float).iat[0]
        bu_expr_prob = bu_data.loc[bu_data['id'] == i, 'p1'].astype(float).iat[0]

        td_var_prob = td_data.loc[td_data['id'] == i, 'p1'].astype(float).iat[0]
        bu_var_prob = bu_data.loc[bu_data['id'] == i, 'p0'].astype(float).iat[0]

        td_final_prob = td_data.loc[td_data['id'] == i, 'score'].astype(float).iat[0]
        bu_final_prob = bu_data.loc[bu_data['id'] == i, 'score'].astype(float).iat[0]

        if td_expr_rank < bu_expr_rank:
            # print "TD", i
            a += 1
            rank_diff_sum += bu_expr_rank - td_expr_rank

            if td_expr_prob > bu_expr_prob:
                # print 'TD EXPR LARGER'
                c += 1
            if td_var_prob > bu_var_prob:
                # print 'TD VAR LARGER'
                d += 1
            if td_final_prob > bu_final_prob:
                # print 'TD FINAL LARGER'
                e += 1

            # print 'E:', td_expr_prob - bu_expr_prob
            # print 'V:', td_var_prob - bu_var_prob

        elif bu_expr_rank < td_expr_rank:
            # print "BU", i
            b += 1
            # print td_expr_prob - bu_expr_prob
            # print td_var_prob - bu_var_prob

        if td_final_prob > bu_final_prob:
            final_score_count += 1

    print '>>>>>>>>>>>'
    print 'TOTAL SIZE: ', count
    print a, b, c, d, e
    print float(rank_diff_sum) / float(a)


if __name__ == '__main__':
    analysis_seq()
