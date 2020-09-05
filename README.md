# spd\_we

This project shows a technique for discovering Stochastic Petri Nets from event logs using process mining techniques. It combines a number of established process mining discovery algorithms with weight estimators which capture a stochastic perspective.

The estimation framework and evaluation test scaffold is described in "Burke, A, Leemans, S.J.J and Wynn, M. T. - Stochastic Process Discovery By Weight Estimation" (forthcoming).

Requirements: 
 + Java 8 (version 8 due to ProM required JDK version)
 + ant, ivy
 + lpsolve 5.5 (install in ldlib)
 + Some miners require extra jars, see note in ldlib
 + R (for the R scripts used in reporting only)

To build:
ant 

To build a zip for distributing, eg to run from command line or on Unix:
ant makezip

