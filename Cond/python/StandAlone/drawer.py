import ConfigParser
import os
import sys
import pandas as pd
import ast
import numpy as np
import matplotlib.pyplot as plt

pred_config = ConfigParser.ConfigParser()
pred_config.read("env.conf")
home_path = os.environ['HOME']

BEAM_K = 200

def get_search_signature(fn):
    res = fn.split('-')[0]
    if 'dijkstra' in res:
        res = res[0: res.rfind('.')]
    else:
        tmp = res.split('.')
        res = '.'.join([tmp[0], tmp[1], tmp[3]])

    res = res.upper().replace('RECUR', 'RC').replace('DIJKSTRA', 'DJ').replace('BEAM', 'BM')
    return res


def get_file_list(proj, bug_id, rule_filter=[], search_filter=[]):
    """
    rule_filter = ['RC', 'TD', 'BU']
    search_filter = ['DJ', 'BM.100']
    """
    res = {}
    record_path = pred_config.get('PATH', 'predall_L2S_path')
    record_path = home_path + record_path + proj + '_' + bug_id + '/'
    file_names = os.listdir(record_path)
    for fn in file_names:
        if fn.endswith("-time.txt"):
            search_sig = get_search_signature(fn)
            rule = search_sig[: search_sig.find('.')]
            search = search_sig[search_sig.find('.') + 1:]
            if rule in rule_filter or search in search_filter:
                continue

            sig = proj.upper() + '.' + search_sig
            # todo : check assert
            assert not res.has_key(sig)
            res[sig] = record_path + fn

    return res


def process_file(fn):
    time_data = pd.read_csv(fn, sep='\t', header=None, encoding='utf-8')

    all_time_table = []
    for idx, row in time_data.iterrows():
        if_id = row[0]
        time = row[1]
        print if_id
        print time
        line_list = ast.literal_eval(time)
        if len(line_list) == BEAM_K:
            all_time_table.append(line_list)

    all_time_matrix = np.array(all_time_table, dtype=float)
    # take average time
    all_time_matrix = all_time_matrix / 1000
    time_sum = all_time_matrix.sum(axis=0)
    return time_sum, len(all_time_table)


def draw_fig_by_search_set(sig2fn):
    all_dj_sum = np.zeros((BEAM_K,), dtype=float)
    all_dj_count = 0
    all_bm_25_sum = np.zeros((BEAM_K,), dtype=float)
    all_bm_25_count = 0
    all_bm_100_sum = np.zeros((BEAM_K,), dtype=float)
    all_bm_100_count = 0
    all_bm_400_sum = np.zeros((BEAM_K,), dtype=float)
    all_bm_400_count = 0
    for key, value in sig2fn.items():
        curr_sum, curr_count = process_file(value)
        if '.DJ' in key:
            all_dj_sum += curr_sum
            all_dj_count += curr_count
        elif '.BM.100' in key:
            all_bm_100_sum += curr_sum
            all_bm_100_count += curr_count
        elif '.BM.25' in key:
            all_bm_25_sum += curr_sum
            all_bm_25_count += curr_count
        elif '.BM.400' in key:
            all_bm_400_sum += curr_sum
            all_bm_400_count += curr_count
        else:
            raise Exception(key)

    all_dj_sum = all_dj_sum / all_dj_count
    all_bm_100_sum = all_bm_100_sum / all_bm_100_count
    all_bm_25_sum = all_bm_25_sum / all_bm_25_count
    all_bm_400_sum = all_bm_400_sum / all_bm_400_count
    y = range(BEAM_K)
    # plt.figure(figsize=(10, 5))
    plt.plot(all_bm_25_sum, y, label='BM.25')
    plt.plot(all_bm_100_sum, y, label='BM.100')
    plt.plot(all_bm_400_sum, y, label='BM.400')
    plt.plot(all_dj_sum, y, label='DJ')

    plt.ylabel("Number of Generated Expressions")
    plt.xlabel("Time (s)")
    plt.legend(['BM.25', 'BM.100', 'BM.400', 'DJ'])
    plt.grid(True)
    plt.savefig('/home/nightwish/search.eps')
    plt.show()


def draw_fig_by_rule_set(sig2fn):
    all_rc_sum = np.zeros((BEAM_K,), dtype=float)  # type: ndarray
    all_rc_count = 0
    all_bu_sum = np.zeros((BEAM_K,), dtype=float)
    all_bu_count = 0
    all_td_sum = np.zeros((BEAM_K,), dtype=float)
    all_td_count = 0

    for key, value in sig2fn.items():
        curr_sum, curr_count = process_file(value)
        if 'RC.' in key:
            all_rc_sum += curr_sum
            all_rc_count += curr_count
        elif 'BU.' in key:
            all_bu_sum += curr_sum
            all_bu_count += curr_count
        elif 'TD.' in key:
            all_td_sum += curr_sum
            all_td_count += curr_count
        else:
            raise Exception()

    all_rc_sum = all_rc_sum / all_rc_count
    all_bu_sum = all_bu_sum / all_bu_count
    all_td_sum = all_td_sum / all_td_count

    y = range(BEAM_K)
    # plt.figure(figsize=(10, 5))
    plt.plot(all_bu_sum, y, label='BU')
    plt.plot(all_td_sum, y, label='TD')
    plt.plot(all_rc_sum, y, label='RC')

    plt.ylabel("Number of Generated Expressions")
    plt.xlabel("Time (s)")
    plt.legend(['BU', 'TD', 'RECUR'])
    plt.grid(True)
    plt.savefig('/home/nightwish/rule.eps')
    plt.show()


if __name__ == '__main__':
    projects = [
        ('lang', '65'),
        ('chart', '26'),
        ('time', '27'),
        ('math', '106')
    ]
    files = []
    for project in projects:
        files.extend(get_file_list(project[0], project[1], rule_filter=[]).items())

    sig2fn = dict(files)

    BY_RULE = False
    if BY_RULE:
        # draw via different rule sets
        draw_fig_by_rule_set(sig2fn)
    else:
        # draw via different search configuration
        draw_fig_by_search_set(sig2fn)
