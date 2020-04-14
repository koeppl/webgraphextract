# Webgraph Dumper

Extract the adjacency lists of a webgraph

Usage:
- download the files `instance.graph` and `instance.properties` of a webgraph `instance` from [WebGraph](http://law.di.unimi.it/datasets.php)
- run `convert.sh instance [01]`
where the last option is a flag with '0' for a binary (default) and '1' for a human-readable dump

## Binary Dump

The results are two binary files `instance.adj.stat` and `instance.adj.adj`.
`instance.adj.stat` stores two integers:
- A 32-bit integer with the number of nodes
- A 64-bit long with the number of arcs


`instance.adj.adj` stores the adjacency lists of each node in sorted order.
The end of the adjacency list of the `i`-th node is marked with the number `n+i`, where `n` is the number of nodes in the graph.


## Human Readable Dump
The dump is one single file `insntance.adj` 
starting with the number of nodes and arcs.
This is following the adjacency lists, starting with the node ID of the adjancency list, a colon, and a whitespace separated list of integers.
