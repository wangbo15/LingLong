# LingLong Synthesis Framework


## I. Introduction

[LingLong Synthesis Framework](https://wangbo15.github.io/LingLong/)  is an abstract framework that synthesizes codes under given specification, and also estimates the probabilities of codes.
Cond is an instance of LingLong, aiming to synthesize Java conditional expressions.

Hanabi is an application of LingLong to repair Java `if` statements by synthesizing conditional expressions.


## II. Structure of the project
```powershell
  |--- README.md   :  user guidance
  |--- Cond        :  The L2S-Cond for synthesizing Java conditional expressions.
  |--- Hanabi      :  The automated-program repair tool based on L2S-Cond
  |--- Patches     :  The patches and their descriptions of Hanabi
```

