import sys
import os
import commands
import ConfigParser

from Utils.config import *

pred_config = ConfigParser.ConfigParser()
pred_config.read("env.conf")


home_path = os.environ['HOME']

predictor_path = home_path + pred_config.get('PATH', 'predictor_path')
all_csv_path = home_path + pred_config.get('PATH', 'all_csv_path')
project_root = home_path + pred_config.get('PATH', 'project_root')

DIRECTION = BU_DIRE
MERGE_JDK = False


def execute_cmd(cmd, exitonerr=True):
    print cmd
    status, msg = commands.getstatusoutput(cmd)
    if status != 0:
        sys.stderr.write('WRONG CMD: ' + cmd + '\n')
        sys.stderr.write(msg + '\n\n')
        sys.stderr.write(os.getcwd() + '\n\n')
        if exitonerr:
            exit(-1)
    sys.stdout.write(msg)
    return


def copy_csv(pro_name, id):
    # mkdir output
    output_path = predictor_path + 'input/' + pro_name + '/' + pro_name + '_' + str(id)
    if not os.path.exists(output_path):
        os.makedirs(output_path)

    # mkdir input/[var, expr, pred]
    for mission in ['/var/', '/expr/', '/pred/', '/recur/', '/plain/']:
        pro_dir = predictor_path + 'input/' + pro_name + '/' + pro_name + '_' + str(id) + mission
        if not os.path.exists(pro_dir):
            os.makedirs(pro_dir)

        # copy from tmp/res/ to var/ and expr/
        cp_cmd = 'cp -f '
        if mission == '/var/':
            if DIRECTION == BU_DIRE:
                cp_cmd_0 = cp_cmd + all_csv_path + '/' + pro_name + '_' + str(id) + '.var.csv ' + pro_dir
                execute_cmd(cp_cmd_0)
                cp_cmd_1 = cp_cmd + all_csv_path + '/' + pro_name + '_' + str(id) + '.v0.csv ' + pro_dir
                execute_cmd(cp_cmd_1)
            if DIRECTION == TD_DIRE:
                cp_cmd_2 = cp_cmd + all_csv_path + '/' + pro_name + '_' + str(id) + '.topdown.var.csv ' + pro_dir
                execute_cmd(cp_cmd_2)
            if DIRECTION == RC_DIRE:
                cp_cmd_2 = cp_cmd + all_csv_path + '/' + pro_name + '_' + str(id) + '.recur.var.csv ' + pro_dir
                execute_cmd(cp_cmd_2)
            # TODO: for plain

        elif mission == '/expr/':
            allpred_name =  pro_name + '_' + str(id) + '.allpred.csv'
            allpred_path = all_csv_path + '/' + allpred_name
            cp_cmd_1 = cp_cmd + allpred_path + ' ' + pro_dir
            execute_cmd(cp_cmd_1)

            if DIRECTION == BU_DIRE:
                expr_name = pro_name + '_' + str(id) + '.expr.csv'
                expr_path = all_csv_path + '/' + expr_name
                cp_cmd_0 = cp_cmd + expr_path + ' ' + pro_dir
                execute_cmd(cp_cmd_0)

            if DIRECTION == TD_DIRE:
                expr_name = pro_name + '_' + str(id) + '.topdown.expr.csv'
                cp_cmd_2 = cp_cmd + all_csv_path + '/' + expr_name + ' ' + pro_dir
                execute_cmd(cp_cmd_2)

            if MERGE_JDK:
                if DIRECTION == BU_DIRE:
                    jdk_expr_path = all_csv_path + '/github/jdk7_math.expr.csv'
                elif DIRECTION == TD_DIRE:
                    jdk_expr_path = all_csv_path + '/github/jdk7_math.topdown.expr.csv'

                append_without_head(jdk_expr_path, pro_dir + expr_name)
                jdk_all_pred_path = all_csv_path + '/github/jdk7_math.allpred.csv'
                append_without_head(jdk_all_pred_path, pro_dir + allpred_name)

            if DIRECTION == RC_DIRE:
                cp_cmd_2 = cp_cmd + all_csv_path + '/' + pro_name + '_' + str(id) + '.recur.expr.csv ' + pro_dir
                execute_cmd(cp_cmd_2)
            # TODO: for plain

        if DIRECTION == RC_DIRE and mission == '/recur/':
            cp_cmd_0 = cp_cmd + all_csv_path + '/' + pro_name + '_' + str(id) + '.recur.csv ' + pro_dir
            execute_cmd(cp_cmd_0)

    return


def append_without_head(jdk_path, ori_path):
    print "APPEND %s TO %s" % (jdk_path, ori_path)
    ori_file = open(ori_path, 'a')
    line = 0
    for txt in open(jdk_path, 'r'):
        line += 1
        if line == 1:
            continue

        ori_file.write(txt)

    ori_file.close()


def train_model(pro_name, id, overwrite=False):
    var_mod = predictor_path + 'model/' + pro_name + '_' + str(id) + '.var_model.pkl'
    expr_mod = predictor_path + 'model/' + pro_name + '_' + str(id) + '.expr_model.pkl'

    if not overwrite and os.path.exists(var_mod) and os.path.exists(expr_mod):
        print '#### BOTH MODEL EXIST'
        return
    os.chdir(predictor_path)
    train_cmd = 'python train_model.py ' + pro_name + ' ' + str(id)
    print '\n#### TRAINING MODEL CMD: ' + train_cmd
    # execute_cmd(train_cmd)
    print '\n#### TRAINING FINISH'
    return


def clear_out(pro_name, id):
    os.chdir(predictor_path)
    res_root = predictor_path + 'output/' + pro_name + '/' + pro_name + '_' + str(id) + '/'
    rm_cmd = "rm -f " + res_root + "*.csv"
    print "#### CLEAR OUTPUT DIR: " + rm_cmd
    execute_cmd(rm_cmd)


def predict_expr(pro_name, id, tested_bug, bug_no):
    var_mod = predictor_path + 'model/' + pro_name + '_' + str(id) + '.var_model.pkl'
    expr_mod = predictor_path + 'model/' + pro_name + '_' + str(id) + '.expr_model.pkl'
    if not os.path.exists(var_mod) or not os.path.exists(expr_mod):
        sys.stderr.write('NO MODEL OF ' + pro_name + '_' + str(id) + '\n')
    os.chdir(predictor_path)

    res_root = predictor_path + 'output/' + pro_name + '/' + pro_name + '_' + str(id) + '/'

    pred_cmd = 'python run_predict.py ' + pro_name + ' ' + str(id)
    execute_cmd(pred_cmd)

    for post_fix in ['.var_pred.csv', '.expr_pred.csv', '.joint.csv']:
        cp_cmd = 'cp -f ' + res_root + pro_name + '_' + str(id) + post_fix + ' ' + res_root + pro_name + '_' + str(tested_bug) + '_' + str(bug_no) + post_fix
        execute_cmd(cp_cmd)
        # rm_cmd = 'rm ' + res_root + pro_name + '_' + str(id) + post_fix
        # execute_cmd(rm_cmd, exitonerr=False)
    return


if __name__ == '__main__':

    if len(sys.argv) != 4 and len(sys.argv) != 5:
        print("\nWrong argument number\n!")
        sys.exit(1)

    pro_name, id, DIRECTION = sys.argv[1], sys.argv[2], sys.argv[3]
    # print mode, pro_name, id, tested_bug, bug_no

	# Merge JDK source
    if len(sys.argv) == 5:
        last = sys.argv[4]
        if last == 'T' or last == 'True':
            MERGE_JDK = True
        elif last == 'F' or last == 'False':
            MERGE_JDK = False
        else:
            print("\nWrong last argument\n!")
            sys.exit(1)

    assert DIRECTION in DIRECTIONS

    os.chdir(predictor_path)
    print "######## COPYING FOR %s_%s, MERGE_JDK: %s ########" % (pro_name, id, str(MERGE_JDK))
    copy_csv(pro_name, id)
    # train_model(pro_name, id)
    # clear_out(pro_name, id)
