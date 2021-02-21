Predictor
====

Preparing Depencencies
----

In Ubuntu 16.04, make sure you have these packages:

* gensim (3.1.0)
* matplotlib (2.0.2)
* nltk (3.2.4)
* numpy (1.13.3)
* pandas (0.20.1)
* scikit-learn (0.19.1)
* scipy (1.0.0)
* xgboost (0.6a2)

You can get them by the cmds as bellow:

`sudo apt-get install python-pip`

`wget https://bootstrap.pypa.io/get-pip.py`

`sudo python get-pip.py`

`sudo pip install xgboost`

`sudo pip install pandas`

`sudo pip install matplotlib`

`sudo pip install sklearn`

`sudo pip install gensim`


Usage
----
* Model training:
`python train_model.py math 3`

* Prediction for one statement:
`python run_all.py math 3`
