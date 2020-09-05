#!/bin/bash -l

## Job submission script on QUT HPC

#PBS -N spmmodelrunner
#PBS -l ncpus=3
#PBS -l mem=55gb
#PBS -l walltime=05:10:00

export JMEM=50g

module load java/1.8.0_231

if [[ ! -z ${PBS_O_WORKDIR+epsilon} ]]; then
    cd $PBS_O_WORKDIR
fi

modelrunner.sh

