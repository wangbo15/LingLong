# Hanabi

## I. Introduction

Hanabi is an automated-program repair tool for fixing Java conditional statements.


## II. Environment

* OS: Ubuntu 16.04.2 LTS
* JDK: Oracle jdk1.7
* Eclipse Oxygen.3a Release

## III. Usage

|  Bug   | Model  |
|  ----  | ----  |
| Math 3,4,5  | Math 12 |
| Math 15,22,25,26,32,33,35  | Math 37 |
| Math 48,50,53 | Math 59 |
| Math 61,63 | Math 67 |
| Math 73 | Math 75 |
| Math 85,93,94 | Math 94 |
| Lang 2 | Lang 5 |
| Lang 7 | Lang 14 |
| Lang 24 | Lang 24 |
| Lang 35 | Lang 35 |
| Time 15 | Time 17 |
| Time 19 | Time 24 |
| Chart 1 | Chart 4 |
| Chart 14 | Chart 16 |
| Chart 19 | Chart 26 |

To fix a bug from the left column, we should use the corresponding model in the right column.
For example, to fix Time-15, we need to invoke the sever for L2S-Cond first, like:

```
cd LingLong/Cond/python
python run_predict_server.py -s time -i 19 -nt -nr -nrb
```

Then we invoke Hanabi to perform repair.
We can directly run the `Main` class in eclipse, or run the encapsulated junit test cases.

