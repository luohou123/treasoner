# Introduction #

TReasoner was developed as module of logical analysis of business process knowledge base in enterprise architecture verification expert system.

# Details #

TReasoner implements tableau algorithm for **SHOIQ** (introduced by _Horrocks_ and _Sattler_) with some optimizations such as backJumping, caching, global caching, [SS-branching](https://code.google.com/p/treasoner/wiki/SSbranching) and Bron-Kerbosch algorithm. TReasoner support only 3 operation on knowledge bases: classification, consistency checking and satisfiability checking.