# ScalaProject

A set of tools for analyzing data from predictive analytics contest VK cup. Akka streams are actively used.

## Implemented functions:
-   `Analyzer.getSize` - returns `Future[Int]` - 
number of entries in a stream
-   `Analyzer.getUserFrequency` - returns `Source[(Int, Int), Any]` - 
source of pairs: user id and amount of advertisement seen by user with this id
-   `Analyzer.getStringHistFuture` - returns `Future[String]` - 
makes a historgram out of source of pairs (id, value) based on values