import sys

from XGBoost.ExprWithPCA import *
from XGBoost.VarWithPCA import *
from XGBoost.RecurNode import *
from Utils.config import *

if __name__ == '__main__':

    pred_direction = BU_DIRE
    if len(sys.argv) == 5:
        pred_direction = sys.argv[4]

    assert pred_direction in DIRECTIONS

    config = Configure(
        sys.argv[1],
        sys.argv[2],
        pred_direction,
        1,
        'model/',
        'input/',
        'output/',
        sys.maxint     #TOP: 10, 100, sys.maxint
    )

    mission_type = sys.argv[3]
    assert mission_type in MISSION_TYPE

    start = time.time()
    if mission_type == EXPR_MISSION:
        if pred_direction == BU_DIRE and len(sys.argv) == 5:
            config.__gen_expr_top__ = int(sys.argv[4])

        expr_predictor = ExprWithPCA(config)
        expr_predictor.predict()
        end = time.time()
        print 'EXPR PRED TIME: ', (end - start)

    elif mission_type == V0_MISSION:
        var_predictor = VarWithPCA(config)
        var_predictor.predict(True)
        end = time.time()
        print 'V0 PRED TIME: ', (end - start)
    elif mission_type == VAR_MISSION:
        var_predictor = VarWithPCA(config)
        var_predictor.predict(False)
        end = time.time()
        print 'ALL VAR PRED TIME: ', (end - start)
    elif mission_type == RECURNODE_MISSION:
        node_predictor = RecurNode(config)
        node_predictor.predict()
        end = time.time()
        print 'ALL VAR PRED TIME: ', (end - start)