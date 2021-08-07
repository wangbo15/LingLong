## LingLong Framework

## I. Introduction

LingLong is an abstract framework that synthesizes codes under given specification, and also estimates the probabilities of codes.
This repo contains two projects, i.e., `Cond` and `Hanabi`.

* [Cond](https://github.com/wangbo15/LingLong/tree/main/Cond) is an instance of LingLong, aiming to synthesize Java conditional expressions.
* [Hanabi](https://github.com/wangbo15/LingLong/tree/main/Hanabi) is an automated program repair tool equipped with Cond, aimming to fix bugs in Java conditional expressions.


## II. Cond
### Training Stage

1. *Training data extraction*. Given a Java project, LingLong collects all the conditional expressions and parses them into options by a specified order, after which LingLong generates training dataset.

2. *Traning Models*. LingLong trains machine learning models against the training data.

### Synthesizing Stage

1. *Code Location Selection*. Select a certain code location, from which LingLong extracts *Context Features* and fires the generation step.

2. *Expression Generation*. Under the given *context*, LingLong synthesizes *K* conditional expressions ranked by their estimated probabilities.


### Environment

* OS: Linux (Tested on Ubuntu 16.04.2 LTS)
* JDK: Oracle JDK 1.7
* Required python packages: gensim (3.1.0)
, matplotlib (2.0.2), nltk (3.2.4), numpy (1.13.3), pandas (0.20.1), scikit-learn (0.19.1), scipy (1.0.0), xgboost (0.6a2)


### How to run

*Cond* is an *Eclipse* project, in which it can be directly imported and executed.
The main entrence is the class `edu.pku.sei.conditon.dedu.predall.PredAllExperiment`, and the configuration file is *config.ini* which controls the rule set selection.

## III. Hanabi

### Environment
* OS: Linux (Tested on Ubuntu 16.04.2 LTS)
* JDK: Oracle JDK 1.7 and 1.8
* Defects4J and Bugs.jar are required

### How to run
*Hanabi* is an *Eclipse* project, in which it can be directly imported and executed.
The main class is `cn.edu.pku.sei.plde.hanabi.main.Main`.
We can also perform repair by JUnit tests.

### Generated patches

The generated patches can be found in the folder [Patch](https://github.com/wangbo15/LingLong/tree/main/Patches).

