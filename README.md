# spd\_we

This project shows a technique for discovering Stochastic Petri Nets from event logs using process mining. It combines a number of established process mining discovery algorithms with weight estimators which capture a stochastic perspective. It also includes scaffolding code for experimental evaluation, and result files from our evaluation.

The estimation framework and evaluation is described in "Burke, A, Leemans, S.J.J and Wynn, M. T. (2021) - Stochastic Process Discovery By Weight Estimation" (presented at the PQMI2020 workshop) DOI 10.1007/978-3-030-72693-5_20.

# ProM Users

## Installation

Download `spndiscover-1.0.jar` and place in a folder on the ProM classpath.

This is a similar installation process to [Fodina](http://www.processmining.be/fodina/). 

## Running Plugins

Once installed, the plugins are 
+ Mine Stochastic Petri net with estimators
    + User can selects an estimator and classifier through the GUI.
+ Mine Stochastic Petri net from log with estimator
    + Uses a default miner and estimator to produce a GSPN directly from an event log.

# Developer and Command Line Use

## Running From Command Line

The test scaffold entry point is `ModelRunner.java`.

The reporting entry point is `SPNDiscoverReporter.java`.

Some scripts for running on Windows and Unix are in `scripts/`.

## Building

Requirements: 
 + Java 8 (version 8 due to ProM required JDK version)
 + ant, ivy
 + lpsolve 5.5 (install in `ldlib`)
 + Some miners require extra jars, see note in `ldlib` folder. This is needed when doing integrated miner+estimator runs from the command line.
 + R (for the R scripts used in reporting only)

To build:
`ant`

To build a zip for distributing, eg to run from command line or on Unix:
`ant makezip`

# Results

Result files from experiments performed on this framework are in `results`.
