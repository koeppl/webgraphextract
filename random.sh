#!/bin/bash

function die {
	echo "$1" 2>&1
	exit 1
}

[[ $# -eq 3 ]] || die "Usage: $0 {basename of the new webgraph} num_nodes num_edges"
graphname=$1
num_nodes=$2
num_edges=$3
[[ -e ${graphname}.graph ]] && die "$1 already exists"


set -x
set -e
mvn install || die "Could not compile"
mvn exec:java -Dexec.mainClass="RandomGraphGenerator" -Dexec.args="${graphname} -n ${num_nodes} -m ${num_edges}"
mvn exec:java -Dexec.mainClass="Streaming" -Dexec.args="${graphname} -t2 -o ${graphname}.matrix" 
mvn exec:java -Dexec.mainClass="Streaming" -Dexec.args="${graphname} -t1 -o ${graphname}.human" 
mvn exec:java -Dexec.mainClass="Streaming" -Dexec.args="${graphname} -t0 -o ${graphname}.adj" 
~/code/matrixrepair/repair/bal/irepair ${graphname}.adj.adj
