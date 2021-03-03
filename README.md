## LingLong Framework

## I. Introduction

LingLong is an instance of the *Learning to Synthesize (L2S)* abstract, aiming to synthesize Java conditional expressions.
The original paper of L2S framework can be found in our workshop paper:
```tex
@inproceedings{L2S-GI18,
	title={Learning to Synthesize},
	author={Xiong, Yingfei and Wang, Bo and Guirong Fu and Linfei Zang},
	booktitle={International Genetic Improvement Workshop},
	year={2018},
	doi={10.1145/3194810.3194816},
}
```


### Training Stage
Given a Java project, LingLong collects all the conditional expressions and parses them.

### Synthesizing Stage

## II. Environment

* OS: Linux (Tested on Ubuntu 16.04.2 LTS)
* JDK: Oracle jdk1.7
* Required python packages: gensim (3.1.0)
, matplotlib (2.0.2), nltk (3.2.4), numpy (1.13.3), pandas (0.20.1), scikit-learn (0.19.1), scipy (1.0.0), xgboost (0.6a2)


## III. How to run
