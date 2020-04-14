#!/bin/bash

export MAVEN_OPTS="-ea -Xms1024m -Xmx30000m"

function die {
	echo "$1" 2>&1
	exit 1
}

[[ $# -gt 0 ]] || die "Usage: $0 {basename of a webgraph} [0: binary or 1: human-readable]"
type=0
[[ $# -gt 1 ]] && type=$2

graphname=$1
[[ -r ${graphname}.graph ]] || die "Not readable:  ${graphname}.graph"
[[ -r ${graphname}.properties ]] || die "Not readable:  ${graphname}.properties"

mvn install || die "Could not compile"
[[ -f ${graphname}.offsets ]]  || \
mvn exec:java -Dexec.mainClass="it.unimi.dsi.webgraph.BVGraph" -Dexec.args="-o -O -L ${graphname}" || \
die "Could not compute offsets"
mvn exec:java -Dexec.mainClass="Streaming" -Dexec.args="${graphname} -t${type} -o ${graphname}.adj" 

