#!/usr/bin/python

import argparse
import os
import zipfile

from set_evn import copy_csv

home_path = os.environ['HOME']

DATA_BASE = home_path + '/tmp/res/'
MODEL_BASE = home_path + '/workspace/eclipse/Condition/python/model/'
PACK_DEST = home_path + '/tmp/pack/'
TARGET_FILE = None
OUT_DIR = '/'

USE_MODEL = False
USE_TRAINING_DATA = False

RULE_SET = ['BU', 'TD', 'RECUR', 'ALL']

CSV_POSTFIX = {
            'BU': ['allpred.csv', 'expr.csv', 'v0.csv', 'var.csv'],
            'TD': ['allpred.csv', 'topdown.expr.csv', 'topdown.var.csv'],
            'RECUR': ['allpred.csv', 'full.csv', 'recur.csv', 'recur.expr.csv', 'recur.var.csv']
        }

MODEL_POSTFIX = {
            'BU': ['expr.fs.pkl', 'expr.lb.pkl', 'expr_model.pkl', 'v0.lb.pkl', 'v0_model.pkl',
                   'var.lb.pkl', 'var_model.pkl'],
            #'TD': ['allpred.csv', 'topdown.expr.csv', 'topdown.var.csv'],
            #'RECUR': ['allpred.csv', 'full.csv', 'recur.csv', 'recur.expr.csv', 'recur.var.csv']
        }

PCA_POSTFIX = {
    'BU': ['expr.del.pkl', 'expr.pca.pkl', 'v0.del.pkl', 'v0.pca.pkl', 'var.del.pkl', 'var.pca.pkl']
    # 'TD': [],
    # 'RECUR' : []
}


def get_data_list(proj, rule):
    res = []
    for post in CSV_POSTFIX[rule]:
        fname = DATA_BASE + proj + '.' + post
        res.append(fname)
    return res


def get_model_files(proj, rule):
    res = []
    dir = MODEL_BASE
    if rule == 'TD':
        dir = dir + 'top_down/'
    elif rule == 'RECUR':
        dir = dir + 'recur/'

    for post in MODEL_POSTFIX[rule]:
        fname = dir + proj + '.' + post
        res.append(fname)

    pca_dir = dir + 'pca/'
    for post in PCA_POSTFIX[rule]:
        fname = pca_dir + proj + '_' + post
        res.append(fname)
    return res


def unpack(proj_id, backup=False, repair=False):
    zf = zipfile.ZipFile(TARGET_FILE, 'r')
    try:
        if backup:
            bk = zipfile.ZipFile(TARGET_FILE[:-4] + ".unpack.bk.zip", 'w', zipfile.ZIP_DEFLATED)

        for f in zf.namelist():
            path = OUT_DIR + f
            if os.path.exists(path):
                if backup:
                    bk.write(path)
                print 'Warning!!! Unpacking will overwrite %s' % path

            if USE_MODEL:
                if f.endswith('.pkl'):
                    zf.extract(f, path=OUT_DIR)
            if USE_TRAINING_DATA:
                if f.endswith('.csv'):
                    zf.extract(f, path=OUT_DIR)

        if backup:
            bk.close()

    except RuntimeError as e:
        print e
    zf.close()

    if repair:
        print 'Perform setenv for repair'
        tmp = proj_id.split('_')
        copy_csv(pro_name=tmp[0], id=tmp[1], merge_jdk=True);


def pack(proj, rule):
    zf = zipfile.ZipFile(TARGET_FILE, 'w', zipfile.ZIP_DEFLATED)
    files = []
    if USE_TRAINING_DATA:
        files += get_data_list(proj, rule)
    if USE_MODEL:
        files += get_model_files(proj, rule)

    unexist = []
    for line in files:
        print line
        if not os.path.exists(line):
            unexist.append(line)

    if len(unexist) > 0:
        for line in unexist:
            print '-------- Does not exist %s' % line
        # exit(-1)

    for line in files:
        if line in unexist:
            continue
        zf.write(line)

    zf.close()


def check_args(args):
    # decl global
    global TARGET_FILE, OUT_DIR, USE_TRAINING_DATA, USE_MODEL

    if rule not in RULE_SET:
        print 'Illegal expansion rule'
        exit(-1)

    if args.repair and rule != 'BU':
        print 'Error rule set for repair'
        exit(-1)

    if args.data == 'M':
        USE_MODEL = True
    elif args.data == 'D':
        USE_TRAINING_DATA = True
    elif args.data == 'MD' or args.data == 'DM':
        USE_MODEL = USE_TRAINING_DATA = True
    else:
        print 'Illegal data type, -d can only be [M|D]'
        exit(-1)

    if args.filename is None or args.filename is '':
        TARGET_FILE = PACK_DEST + proj + '.zip'
    else:
        TARGET_FILE = PACK_DEST + args.filename

    if args.unpack:
        if not os.path.exists(TARGET_FILE):
            print 'No target zip file to unpack: %s' % TARGET_FILE
            exit(-1)

    if args.outdir is not None:
        if not args.unpack:
            print 'Illegal base output dir for packing'
            exit(-1)

        OUT_DIR = args.outdir

    if not os.path.exists(PACK_DEST):
        os.makedirs(PACK_DEST)


###############################################################################
if __name__ == '__main__':
    desc = 'This script is used to pack or unpack data and models'
    parser = argparse.ArgumentParser(description=desc)

    parser.add_argument('-u', '--unpack', dest='unpack', action='store_true',
                        help='unpack mode', default=False, required=False)

    parser.add_argument('-p', '--project', dest='project', type=str, help='the project name', required=True)

    parser.add_argument('-f', '--filename', dest='filename', type=str, help='the input/output file name', required=False)

    parser.add_argument('-o', '--outdir', dest='outdir', type=str, help='the base output dir for unzip', required=False)

    parser.add_argument('-r', '--repair', action='store_true', dest='repair', default=False,
                        help='used for repair', required=False)

    parser.add_argument('-d', '--data', dest='data', type=str,
                        help='the data to be packed/unpacked, -d [M|D|MD]]', required=True)

    parser.add_argument('-b', '--backup', action='store_true', dest='backup', default=False,
                        help='backup before unpack', required=False)

    parser.add_argument('-e', '--expansion', dest='expansion', type=str,
                        help='the expansion rule', required=False, default='BU')

    args = parser.parse_args()
    rule = args.expansion
    proj = args.project
    check_args(args)

    bk = args.backup
    rp = args.repair

    if args.unpack:
        unpack(proj, backup=bk, repair=rp)
    else:
        print 'Packing the data and model of %s into %s%s.zip' % (proj, PACK_DEST, proj)
        pack(proj, rule)
