// scalastyle:off println
package com.graygzou.Fight

// $example on$
import org.apache.spark.graphx.GraphLoader
import org.apache.spark.{SparkConf, SparkContext}
// $example off$

object FirstFight {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Fight 1").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // $example on$
    // Load the first team data and parse into tuples of entity id and attribute list
    val gameEntities = (sc.textFile("resources/entities.txt")
      .map(line => line.split(","))
      .map( parts => (parts.head.toLong, parts.tail) ))

    //var team1 = ...

    //var team2 = ...

    /*
    // Parse the edge data which is already in userId -> userId format
    // This graph represents possible interactions (hostiles or not) between entities.
    val relationGraph = GraphLoader.edgeListFile(sc, "resources/relations.txt")

    // Attach the users attributes
    val graph = followerGraph.outerJoinVertices(users) {
      case (uid, deg, Some(attrList)) => attrList
      // Some users may not have attributes so we set them as empty
      case (uid, deg, None) => Array.empty[String]
    }

    // Restrict the graph to users with usernames and names
    val subgraph = graph.subgraph(vpred = (vid, attr) => attr.size == 2)

    // Compute the PageRank
    val pagerankGraph = subgraph.pageRank(0.001)

    // Get the attributes of the top pagerank users
    val userInfoWithPageRank = subgraph.outerJoinVertices(pagerankGraph.vertices) {
      case (uid, attrList, Some(pr)) => (pr, attrList.toList)
      case (uid, attrList, None) => (0.0, attrList.toList)
    }

    println(userInfoWithPageRank.vertices.top(5)(Ordering.by(_._2._1)).mkString("\n"))
    // $example off$

    //spark.stop()*/
  }
}
// scalastyle:on println
