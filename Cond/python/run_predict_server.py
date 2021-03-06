import socket
import traceback
import time
import ConfigParser
import argparse
import sys

from XGBoost.DensePredictor import *


ENV_CONFIG = ConfigParser.ConfigParser()
ENV_CONFIG.read("env.conf")
LEARNING_ALG = ENV_CONFIG.get('LEARNING', 'algorithm')

MSG_END = '#@!<<<<'

TD_EXPR_REQUEST_HEAD = '#@!>>TD_EXPR\n'
TD_VAR_REQUEST_HEAD = '#@!>>TD_VAR\n'

RECUR_NODE_REQUEST_HEAD = '#@!>>RC_NODE\n'
RECUR_EXPR_REQUEST_HEAD = '#@!>>RC_EXPR\n'
RECUR_VAR_REQUEST_HEAD = '#@!>>RC_VAR\n'

BU_V0_REQUEST_HEAD = '#@!>>BU_V0\n'
BU_EXPR_REQUEST_HEAD = '#@!>>BU_EXPR\n'
BU_VAR_REQUEST_HEAD = '#@!>>BU_VAR\n'

CLOSE_REQUEST_HEAD = '#@!CLOSE\n'

ERR_RESPOND = bytes('#@!ERROR\n')

DEFAULT_PORT = 6666

def recv_end(the_socket):
    total_data = [];
    while True:
        data = the_socket.recv(16384)
        if CLOSE_REQUEST_HEAD in data:
            return None
        if MSG_END in data:
            total_data.append(data[:data.find(MSG_END)])
            break
        if data == b"":
            # the connection expires
            return None

        total_data.append(data)
        if len(total_data) > 1:
            # check if end_of_data was split
            last_pair = total_data[-2] + total_data[-1]
            if MSG_END in last_pair:
                total_data[-2] = last_pair[:last_pair.find(MSG_END)]
                total_data.pop()
                break
    return ''.join(total_data)


def process(client_data, td_predictor, bu_predictor, recur_predictor):
    try:
        res_list = []

        if bu_predictor is not None:
            if client_data.startswith(BU_V0_REQUEST_HEAD):  # for bottom up v0
                data = client_data[len(BU_V0_REQUEST_HEAD):]
                res_list = bu_predictor.predict_var(data, True)

            elif client_data.startswith(BU_EXPR_REQUEST_HEAD):  # for bottom up expr
                data = client_data[len(BU_EXPR_REQUEST_HEAD):]
                res_list = bu_predictor.predict_expr(data)

            elif client_data.startswith(BU_VAR_REQUEST_HEAD):  # for bottom up var
                data = client_data[len(BU_VAR_REQUEST_HEAD):]
                res_list = bu_predictor.predict_var(data, False)

        if td_predictor is not None:
            if client_data.startswith(TD_EXPR_REQUEST_HEAD):  # for top down expr
                data = client_data[len(TD_EXPR_REQUEST_HEAD):]
                res_list = td_predictor.predict_expr(data)

            elif client_data.startswith(TD_VAR_REQUEST_HEAD):  # for top down var
                data = client_data[len(TD_VAR_REQUEST_HEAD):]
                res_list = td_predictor.predict_var(data, False)

        if recur_predictor is not None:
            if client_data.startswith(RECUR_NODE_REQUEST_HEAD): # for top down recur node
                data = client_data[len(RECUR_NODE_REQUEST_HEAD):]
                res_list = recur_predictor.predict_recurnode(data)

            elif client_data.startswith(RECUR_EXPR_REQUEST_HEAD):  # for top down recur node
                data = client_data[len(RECUR_EXPR_REQUEST_HEAD):]
                res_list = recur_predictor.predict_recurexpr(data)

            elif client_data.startswith(RECUR_VAR_REQUEST_HEAD):  # for top down recur node
                data = client_data[len(RECUR_VAR_REQUEST_HEAD):]
                res_list = recur_predictor.predict_recurvar(data)

        return '\n'.join(res_list)

    except:
        traceback.print_exc()
        return MSG_END + '\n'


def get_predictor(args):
    proj = args.subject
    bug = args.id

    td_predictor = None
    bu_predictor = None
    recur_predictor = None

    if args.use_td:
        td_config = Configure(
            proj,
            bug,
            'top_down',
            1,
            'model/',
            'input/',
            'output/',
            sys.maxint  # TOP: 10, 100, sys.maxint
        )
        td_predictor = DensePredictor(td_config)

    if args.use_bu:
        bu_config = Configure(
            proj,
            bug,
            'bottom_up',
            1,
            'model/',
            'input/',
            'output/',
            sys.maxint  # TOP: 10, 100, sys.maxint
        )
        bu_predictor = DensePredictor(bu_config)

    if args.use_rc:
        recur_config = Configure(
            proj,
            bug,
            'recur',
            1,
            'model/',
            'input/',
            'output/',
            sys.maxint  # TOP: 10, 100, sys.maxint
        )
        recur_predictor = DensePredictor(recur_config)

    return td_predictor, bu_predictor, recur_predictor


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Run predictors by sever')
    parser.add_argument('-s', '--subject', dest='subject', type=str, help='the subject name', required=True)

    parser.add_argument('-i', '--id', dest='id', type=str, help='the bug id', required=True)

    parser.add_argument('-nt', '--no-td', action='store_false', dest='use_td',
                        help='TD OFF', required=False)

    parser.add_argument('-nb', '--no-bu', action='store_false', dest='use_bu',
                        help='BU OFF', required=False)

    parser.add_argument('-nr', '--no-recur', action='store_false', dest='use_rc',
                        help='RECUR OFF', required=False)

    parser.add_argument('-p', '--port', dest='port', type=int, default=DEFAULT_PORT,
                        help='the port to sever', required=False)

    args = parser.parse_args()

    # use localhost rather than 127.0.0.1 !
    ip_port = ('localhost', args.port)
    sever = socket.socket()
    sever.bind(ip_port)
    sever.listen(8)

    print 'Connecting with port %d' % (args.port)

    print 'Loading %s models for %s_%s. TD: %s, BU: %s, RC: %s' % \
          (LEARNING_ALG.upper(), args.subject, args.id, args.use_td, args.use_bu, args.use_rc)

    td_predictor, bu_predictor, recur_predictor = get_predictor(args)

    while True:
        conn, addr = sever.accept()
        print '>>>> CONNECTED >>>>'

        start = time.time()

        # conn.settimeout(300)

        while True:
            client_data = recv_end(conn)
            # print client_data

            if not client_data or client_data.startswith(CLOSE_REQUEST_HEAD):
                conn.close()
                end = time.time()
                print '>>>> CLOSED >>>> ', (end - start), ' sec'
                break
            try:
                response = process(client_data, td_predictor, bu_predictor, recur_predictor)
                # print response
                conn.sendall(response)
            except:
                traceback.print_exc()
                conn.sendall(ERR_RESPOND)
        conn.close()

    sever.close()
