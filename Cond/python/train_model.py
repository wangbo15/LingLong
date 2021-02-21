import sys
import time

from Utils.config import *

from XGBoost.ExprWithPCA import *
from XGBoost.VarWithPCA import *
from XGBoost.RecurNode import *

if __name__ == '__main__':
    if len(sys.argv) != 3 and len(sys.argv) != 4:
        print("Wrong argument number!")
        sys.exit(1)

    # proj_name, bug_id, direction

    if len(sys.argv) == 4:
        direction = sys.argv[3]
    else:
        direction = BU_DIRE

    assert direction in DIRECTIONS

    config = Configure(
        sys.argv[1],
        sys.argv[2],
        direction,
        0,
        'model/',
        'input/',
        'output/',
        10
    )

    logging.info('########## BEGIN TRAINING FOR ' + config.prognm_and_id + ' ' + direction.upper() + ' '
                 + LEARNING_ALG.upper() + ' ##########')
    all_time_start = time.time()

    # train position 0 var
    var_trainer = VarWithPCA(config)

    if direction == BU_DIRE:
        # only 'bottom_up' need train v0
        var_trainer.train(is_v0=True)

    # train all position var
    var_trainer.train(is_v0=False)

    # train expr
    expr_trainer = ExprWithPCA(config)
    expr_trainer.train()

    if direction == RC_DIRE:
        recurnode_trainer = RecurNode(config)
        recurnode_trainer.train()

    time_end = time.time()
    logging.info('########## TOTAL TRAINING TIME ' + str(float(time_end - all_time_start)/60) + ' M ' + direction.upper() + ' ##########\n')
