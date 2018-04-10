/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  */

package com.graygzou.Examples

import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx.{Edge, Graph, VertexId, VertexRDD}
import org.apache.spark.rdd.RDD

object Collaborators {

  def main(args: Array[String]): Unit = {

    var conf = new SparkConf().setAppName("Collaborators 1").setMaster("local[*]")
    var sc = new SparkContext(conf)

    // Create an RDD for the vertices
    var users: RDD[(VertexId, (String, String))] =
      sc.parallelize(Array((3L, ("rxin", "student")), (7L, ("jgonzal", "postdoc")),
        (5L, ("franklin", "prof")), (2L, ("istoica", "prof"))))

    // Create an RDD for edges
    var relationships: RDD[Edge[String]] =
      sc.parallelize(Array(Edge(3L, 7L, "collab"), Edge(5L, 3L, "advisor"),
        Edge(2L, 5L, "colleague"), Edge(5L, 7L, "pi")))
    // Define a default user in case there are relationship with missing user
    var defaultUser = ("John Doe", "Missing")
    // Build the initial Graph
    var graph = Graph(users, relationships, defaultUser)

    // Count all users which are postdocs
    println(graph.vertices.filter { case (id, (name, pos)) => pos == "prof" }.count)
    // Count all the edges where src > dst
    println(graph.edges.filter(e => e.srcId > e.dstId).count)

    // Use the triplets view to create an RDD of facts.
    val facts: RDD[String] =
      graph.triplets.map(triplet => triplet.srcAttr._1 + " is the " + triplet.attr + " of " + triplet.dstAttr._1)
    facts.collect.foreach(println(_))

    // Use the implicit GraphOps.inDegrees operator
    val inDegrees: VertexRDD[Int] = graph.inDegrees
    inDegrees.collect.foreach(println(_))

    sc.stop();

    // Remove broken links
    conf = new SparkConf().setAppName("Collaborators 2").setMaster("local[*]")
    sc = new SparkContext(conf)

    // Create an RDD for the vertices
    val users2: RDD[(VertexId, (String, String))] =
    sc.parallelize(Array((3L, ("rxin", "student")), (7L, ("jgonzal", "postdoc")),
      (5L, ("franklin", "prof")), (2L, ("istoica", "prof")),
      (4L, ("peter", "student"))))
    // Create an RDD for edges
    val relationships2: RDD[Edge[String]] =
      sc.parallelize(Array(Edge(3L, 7L, "collab"),    Edge(5L, 3L, "advisor"),
        Edge(2L, 5L, "colleague"), Edge(5L, 7L, "pi"),
        Edge(4L, 0L, "student"),   Edge(5L, 0L, "colleague")))
    // Define a default user in case there are relationship with missing user
    defaultUser = ("John Doe", "Missing")
    // Build the initial Graph
    graph = Graph(users2, relationships2, defaultUser)
    // Notice that there is a user 0 (for which we have no information) connected to users
    // 4 (peter) and 5 (franklin).
    graph.triplets.map(
      triplet => triplet.srcAttr._1 + " is the " + triplet.attr + " of " + triplet.dstAttr._1
    ).collect.foreach(println(_))
    // Remove missing vertices as well as the edges to connected to them
    val validGraph = graph.subgraph(vpred = (id, attr) => attr._2 != "Missing")
    // The valid subgraph will disconnect users 4 and 5 by removing user 0
    validGraph.vertices.collect.foreach(println(_))
    validGraph.triplets.map(
      triplet => triplet.srcAttr._1 + " is the " + triplet.attr + " of " + triplet.dstAttr._1
    ).collect.foreach(println(_))


    sc.stop()

    // Test aggregate Messages

    conf = new SparkConf().setAppName("AggregateMsg").setMaster("local[*]")
    sc = new SparkContext(conf)

    // Create a graph with "age" as the vertex property.
    // Here we use a random graph for simplicity.
    val grapht: Graph[Double, Int] =
    GraphGenerators.logNormalGraph(sc, numVertices = 100).mapVertices( (id, _) => id.toDouble )

    // Print the initial graph
    grapht.triplets.map(
      triplet => triplet.srcAttr + " = " + triplet.attr + " = " + triplet.dstAttr
    ).collect.foreach(println(_))

    // Compute the number of older followers and their total age
    val olderFollowers: VertexRDD[(Int, Double)] = grapht.aggregateMessages[(Int, Double)](
      triplet => { // Map Function
        if (triplet.srcAttr > triplet.dstAttr) {
          // Send message to destination vertex containing counter and age
          triplet.sendToDst((1, triplet.srcAttr))
        }
      },
      // Add counter and age
      (a, b) => (a._1 + b._1, a._2 + b._2) // Reduce Function
    )
    // Divide total age by number of older followers to get average age of older followers
    val avgAgeOfOlderFollowers: VertexRDD[Double] =
      olderFollowers.mapValues( (id, value) =>
        value match { case (count, totalAge) => totalAge / count } )
    // Display the results
    avgAgeOfOlderFollowers.collect.foreach(println(_))


  }
}
