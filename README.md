## LingLong Framework

## I. Introduction

LingLong is an abstract framework that synthesizes codes under given specification, and also estimates the probabilities of codes.
Cond is an instance of LingLong, aiming to synthesize Java conditional expressions.


### Training Stage

1. *Training data extraction*. Given a Java project, LingLong collects all the conditional expressions and parses them into options by a specified order, after which LingLong generates training dataset.

2. *Traning Models*. LingLong trains machine learning models against the training data.

### Synthesizing Stage

1. *Code Location Selection*. Select a certain code location, from which LingLong extracts *Context Features* and fires the generation step.

2. *Expression Generation*. Under the given *context*, LingLong synthesizes *K* conditional expressions ranked by their estimated probabilities.


## II. Environment

* OS: Linux (Tested on Ubuntu 16.04.2 LTS)
* JDK: Oracle jdk1.7
* Required python packages: gensim (3.1.0)
, matplotlib (2.0.2), nltk (3.2.4), numpy (1.13.3), pandas (0.20.1), scikit-learn (0.19.1), scipy (1.0.0), xgboost (0.6a2)


## III. How to run

LingLong is an *Eclipse* project, in which it can be directly imported and executed.
The main entrence is the class *edu.pku.sei.conditon.dedu.predall.PredAllExperiment*, and the configuration file is *config.ini*.


## IV. Generated patches

The generated patches can be found in the folder [Patch](https://github.com/wangbo15/LingLong/tree/main/Patches).

