/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  */

// scalastyle:off println
package com.graygzou.Fight

import org.apache.spark.graphx.GraphLoader
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
// To make some of the examples work we will also need RDD
import org.apache.spark.rdd.RDD
// $example off$

object FirstFight {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Fight 1").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Create an RDD for the vertices
    val users: RDD[(VertexId, (String, String))] =
      sc.parallelize(Array((3L, ("rxin", "student")), (7L, ("jgonzal", "postdoc")),
        (5L, ("franklin", "prof")), (2L, ("istoica", "prof"))))

    // Create an RDD for edges
    val relationships: RDD[Edge[String]] =
      sc.parallelize(Array(Edge(3L, 7L, "collab"),    Edge(5L, 3L, "advisor"),
        Edge(2L, 5L, "colleague"), Edge(5L, 7L, "pi")))
    // Define a default user in case there are relationship with missing user
    val defaultUser = ("John Doe", "Missing")
    // Build the initial Graph
    val graph = Graph(users, relationships, defaultUser)

    // Count all users which are postdocs
    println(graph.vertices.filter { case (id, (name, pos)) => pos == "prof" }.count)
    // Count all the edges where src > dst
    println(graph.edges.filter(e => e.srcId > e.dstId).count)


/*
// Load the first team data and parse into tuples of entity id and attribute list
val gameEntities = (sc.textFile("resources/entities.txt")
  .map(line => line.split(","))
  .map( parts => (parts.head.toLong, parts.tail) ))

//var team1 = graph.subgraph

//var team2 = graph.subgraph

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

//spark.stop()*/*/
sc.stop();
}
}
// scalastyle:on println
