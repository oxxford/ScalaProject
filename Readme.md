#ScalaProject

A set of tools for analyzing data from predictive analytics contest VK cup. Akka streams are actively used.

##Implemented functions:
-   `Analyzer.getAmountOfUsers` - returns `Future[Int]` - 
number of users in UserEntry stream
-   `Analyzer.getStringHistFuture` - returns `Future[String]` - 
textual histogram representation about number of time a user have seen an advertisement