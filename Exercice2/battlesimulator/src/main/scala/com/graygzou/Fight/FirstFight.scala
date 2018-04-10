/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  */

// scalastyle:off println
package com.graygzou.Fight

import com.graygzou.Creatures.Entity
import org.apache.spark.graphx.GraphLoader
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.xerces.impl.XMLEntityManager.Entity
// To make some of the examples work we will also need RDD
import org.apache.spark.rdd.RDD
// $example off$

object FirstFight {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Fight 1").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Load the first team data and parse into tuples of entity id and attribute list
    val gameEntities = sc.textFile("resources/Fight1/entities.txt")
      .map(line => line.split(","))
      .map(parts => (parts.head.toLong, new com.graygzou.Creatures.Entity(parts.tail)))

    // Parse the edge data which is already in userId -> userId format
    // This graph represents possible interactions (hostiles or not) between entities.
    val relationGraph = GraphLoader.edgeListFile(sc, "resources/Fight1/relations.txt")

    // Attach the users attributes
    val mainGraph = relationGraph.outerJoinVertices(gameEntities) {
      case (uid, deg, Some(attrList)) => attrList
      // Some users may not have attributes so we set them as empty
      case (uid, deg, None) => Array.empty[String]
    }

    // Print the initial graph
    mainGraph.triplets.map(
      triplet => triplet.srcAttr + " = " + triplet.attr + " = " + triplet.dstAttr
    ).collect.foreach(println(_))

    /*
    var team1 = mainGraph.subgraph(entity => entity.)

    var team2 = mainGraph.subgraph*/

    // Gameloop
    // While their is still a link between Team1 and Team2
    // {
      // For each nodes of the graph, in parallel
      // {
        // The current node check if an ennemy is in range
        // {
          // if so => Attack him
          // if random + attack > armor
          // {
            // Send message to hurt him
          // }
        // }
        // else
        // {
          // if not => Move in the direction of the closest ennemy
        // }
      // }
    // }









    // }

    /*

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
    sc.stop();
  }
}
// scalastyle:on println
