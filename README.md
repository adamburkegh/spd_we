# spd\_we

This project shows a technique for discovering Stochastic Petri Nets from event logs using process mining. It combines a number of established process mining discovery algorithms with weight estimators which capture a stochastic perspective. It also includes scaffolding code for experimental evaluation, and result files from our evaluation.

The estimation framework and evaluation is described in "Burke, A, Leemans, S.J.J and Wynn, M. T. (2021) - Stochastic Process Discovery By Weight Estimation", DOI 10.1007/978-3-030-72693-5_20. You can also see this  [short blog post](https://adamburkeware.net/2020/10/06/spd-by-weight-estimation.html).

This page details the Java / ProM implementation. Alternative implementations are listed at the end.

 * [Developer and Command Line Use](dev)
 * [ProM Users](prom)
 * [Results](results)
 * [Alternative Implementations](alt)

<a id="dev"></a>
# Developer and Command Line Use

## Running From Command Line

The test scaffold entry point is `ModelRunner.java`.

`PPTEstimateRunner.java` is a simple tool for applying estimated weights to process tree models, so long as the original discovery algorithm outputs a process tree.

Note that both `ModelRunner` and `PPTEstimateRunner` depend on a config file called `instance.properties` found in the `config` directory. Most of the behaviour is controlled from this file, including which estimators to use, directories for input event logs and models, and so on. The meaning of the properties is defined in `ModelRunner`. 

The default `instance.properties` reads an example log file `data/exercise1.xes`, runs the Inductive miner on it, performs weight estimation, and calculates the Earth Movers' Distance using the tEMSC 0.8 measure. 

XML model and result files are output to 'var/'; `mrun_*` files are calculations, `osmodel_*` files are PNML files with stochastic weights.

The reporting entry point is `SPNDiscoverReporter.java`.

Some scripts for running on Windows and Unix are in `scripts/`.



### Example Output

```
2024-02-22 16:48:26,965 INFO  [main] spn_discover.ModelRunner - SPM model runner initializing
2024-02-22 16:48:26,969 INFO  [main] spn_discover.ModelRunner - Using data location data
2024-02-22 16:48:26,969 INFO  [main] spn_discover.ModelRunner - Using data files [exercise1.xes]
2024-02-22 16:48:26,974 INFO  [main] spn_discover.ModelRunner - Using classifier NAME
2024-02-22 16:48:26,988 INFO  [main] spn_discover.ModelRunner - Beginning run -- Inductive Miner -- exercise1.xes
...
2024-02-22 16:48:27,340 INFO  [main] spn_discover.ModelRunner - SPM model runner finished
```

## Building

Requirements: 
 + Java 8 (version 8 due to ProM required JDK version)
 + ant, ivy
 + lpsolve 5.5 (for alignments, install in `ldlib`)
 + ivy will download third party jars for fodina and [prob-process-tree](https://github.com/adamburkegh/prob-process-tree)
 + Some miners require extra jars, see note in `ldlib` folder. This is needed when doing integrated miner+estimator runs from the command line.
 + R (for the R scripts used in reporting only)

To build:

`ant resolve`

`ant test`

To build a zip for distributing, eg to run from the Windows command line or on Unix:
`ant makezip`


<a id="prom"></a>
# ProM Users

Plugins are available in the ProM nightly build. Unfortunately a build pipeline issue meant these plugins were broken in ProM 6.14, but at time of writing they run in the [nightly build](https://promtools.org/prom-6-nightly-builds/).

## Running Plugins

The plugins can be run from the ProM GUI, as:
+ Mine Stochastic Petri net with estimators
    + User can selects an estimator and classifier through the GUI.
+ Mine Stochastic Petri net from log with estimator
    + Uses a default miner and estimator to produce a GSPN directly from an event log.


## Installation Of Alternative Discovery Algorithms in ProM

The estimator plugins require an event log and a Petri net as input. In earlier versions of ProM, it was possible to integrate plugins such as Fodina not available in ProM releases. According to our testing, this has not been possible since ProM 6.10. As before, it is still possible to load a Petri net as a PNML file after creating it outside ProM.


## Plugin Source

The ProM plugin source is a subset of this repository, synched through a copying process in the ant build file. This reduces the surface of dependencies to manage in ProM. The source project used by ProM to build is the [StochasticWeightEstimation plugin](https://github.com/promworkbench/StochasticWeightEstimation) under the promworkbench project.

<a id="results"></a>
# Results

Result files from experiments performed on this framework are in `results`.

<a id="alt"></a>
# Alternative Implementations

Two of these estimation techniques - alignment and frequency estimation - are implemented in [Ebi](https://bpm.rwth-aachen.de/ebi/) as of version 0.1.0 (25 October 2024). Ebi has binary releases and is an open-source project implemented in Rust.
