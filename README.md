# webgraph

- Download the files `instance.graph` and `instance.properties` of a webgraph `instance` from [WebGraph](http://law.di.unimi.it/datasets.php)
- run `convert.sh instance`

The results are two binary files `instance.stat` and `instance.adj`.
`instance.stat` stores two integers:
- A 32-bit integer with the number of nodes
- A 64-bit long with the number of arcs


`instance.adj` stores the adjacency lists of each node in sorted order.
The end of the adjacency list of the `i`-th node is marked with the number `n+i`, where `n` is the number of nodes in the graph.
