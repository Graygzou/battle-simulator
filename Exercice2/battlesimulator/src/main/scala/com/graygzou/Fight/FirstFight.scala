/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  */

// scalastyle:off println
package com.graygzou.Fight

import com.graygzou.Creatures.Entity
import com.graygzou.Team
import org.apache.spark.graphx.GraphLoader
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.xerces.impl.XMLEntityManager.Entity
// To make some of the examples work we will also need RDD
import org.apache.spark.rdd.RDD
// $example off$

object FirstFight {




  /**
    * Method used to check the end of the fight
    * @param currentGraph : actual graph that represent alive entities
    * @return Array[Long] : represent the number of alive members of each team.
    */
  def countTeamMember(currentGraph: Graph[_,_]): Array[Long] = {
    var teamMember = Array[Long](2) // TODO make more generic
    println(teamMember.length)
    for ( team_ind <- 0 to 1 ) {
      println(Team.values.size-1)
      teamMember(team_ind) = currentGraph.vertices.filter {
        case (id, dragon) => dragon.asInstanceOf[com.graygzou.Creatures.Entity].ownTeam == Team.values(Team(team_ind))
      }.count()
    }
    return teamMember
  }


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

    // Extract all the team size and store them in a structure
    var teamMember = countTeamMember(mainGraph)

    // --------------
    // Gameloop
    // --------------
    // While their is still two teams in competition
    // (at least one node from the last two teams)
    while( teamMember.count( (numVertices: Long) => numVertices.!=(0) ) >= 2 ) {

      // ---------------------------------
      // Execute a turn of the game
      // ---------------------------------
      val playOneTurn: VertexRDD[(java.io.Serializable, Int)] = mainGraph.aggregateMessages[(java.io.Serializable, Int)] (

        // Map Function : Send message (Source => Destinateur)
        // 1) Retrieve creatures in range
        // 2) Move around
        // 3) Attacks msg (random + attack > armor => attack) or Do nothin
        // 4) Heal msg
        // 5) Move around
        triplet => {
          // TODO
          /*
          if (triplet.srcAttr > triplet.dstAttr) {
            // Send message to destination vertex containing counter and age
            triplet.sendToDst((1, triplet.srcAttr))
          }*/
        },

        // Reduce Function : Received message
        // 1) Sum of the damage taken
        // 2) Check if some spells can be cast to avoid damages
        // 3) Take damages left.
        (id, Entity) => {
          // TODO
          (id._1, id._2)
        }
      )

      // Divide total age by number of older followers to get average age of older followers
      // TODO
      /*
      val avgAgeOfOlderFollowers: VertexRDD[Double] =
        playOneTurn.mapValues( (id, value) =>
          value match { case (count, totalAge) => totalAge / count } )*/


      // Display the current turn in console
      // TODO
      //avgAgeOfOlderFollowers.collect.foreach(println(_))


      // Update the team size based on the graph
      teamMember = countTeamMember(mainGraph)

    }

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
