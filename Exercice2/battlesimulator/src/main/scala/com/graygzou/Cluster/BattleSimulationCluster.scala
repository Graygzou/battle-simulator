/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  * @version 0.0.1
  */

// scalastyle:off println
package com.graygzou.Cluster

import com.graygzou.Team
import org.apache.spark.graphx.{GraphLoader, _}
import org.apache.spark.{SparkConf, SparkContext}
// To make some of the examples work we will also need RDD
// $example off$

class BattleSimulationCluster(appName: String, mode: String) {

  // Dummy values
  var NbTurnMax = 0

  // Init Scala Context
  // Create the SparkConf and the SparkContext with the correct value
  val conf = new SparkConf().setAppName(appName).setMaster(mode)
  val sc = new SparkContext(conf)

  /**
    * Method used to check the end of the fight
    * @param currentGraph : actual graph that represent alive entities
    * @return Array[Long] : represent the number of alive members of each team. Available in the following form :
    *         List(nbAliveTeam1, nbAliveTeam2, ..., nbAliveTeamN)
    */
  def countTeamMember(currentGraph: Graph[_,_]): List[Int] = {
    var teamMember: List[Int] = List() // TODO make more generic
    for ( team_ind <- 0 to 1 ) {
      val verticesCurrentTeam = currentGraph.vertices.filter {
        case (id, infos) => infos.asInstanceOf[com.graygzou.Creatures.Entity].ownTeam.equals(Team(team_ind))
        case _ => false
      }.count()
      teamMember = verticesCurrentTeam.toInt :: teamMember
    }
    return teamMember
  }

  /**
    * Clean the application by closing the SparkContext
    */
  def cleanScalaContext = {
    sc.stop();
  }


  private def setupGame(entitiesFile: String, relationFile: String) : Graph[_,_] = {
    NbTurnMax = 100  // Should be in the game.txt

    // Load the first team data and parse into tuples of entity id and attribute list
    val entitiesPath = getClass.getResource(entitiesFile)
    val gameEntities = sc.textFile(entitiesPath.getPath)
      .map(line => line.split(","))
      .map(parts => (parts.head.toLong, new com.graygzou.Creatures.Entity(parts.tail)))

    // Parse the edge data which is already in userId -> userId format
    // This graph represents possible interactions (hostiles or not) between entities.
    val relationsPath = getClass.getResource(relationFile)
    val relationGraph = GraphLoader.edgeListFile(sc, relationsPath.getPath())

    // Attach the users attributes
    var mainGraph = relationGraph.outerJoinVertices(gameEntities) {
      case (uid, deg, Some(attrList)) => attrList
      // Some users may not have attributes so we set them as empty
      case (uid, deg, None) => Array.empty[String]
    }

    // Print the initial graph
    mainGraph.triplets.map(
      triplet => triplet.srcAttr + " = " + triplet.attr + " = " + triplet.dstAttr
    ).collect.foreach(println(_))

    return mainGraph
  }


  def launchGame(entitiesFile: String, relationFile: String) = {

    // Init the first graph with
    var mainGraph = setupGame(entitiesFile, relationFile)

    // Extract all the team size and store them in a structure
    var teamMember = countTeamMember(mainGraph)
    var currentTurn = 0

    // --------------
    // Gameloop
    // --------------
    // While their is still two teams in competition
    // (at least one node from the last two teams)
    while (teamMember.count((numVertices) => numVertices.!=(0)) >= 2 && currentTurn <= NbTurnMax) {
      println("Turn n°" + currentTurn)

      // ---------------------------------
      // Execute a turn of the game
      // ---------------------------------
      val playOneTurn: VertexRDD[(java.io.Serializable, Int)] = mainGraph.aggregateMessages[(java.io.Serializable, Int)](

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


      // Update variables
      // the team size based on the graph
      teamMember = countTeamMember(mainGraph)

      currentTurn += 1

    }

    println("The fight is done.")
    println("The winning team is : ...") // TODO

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
    */
  }
}
// scalastyle:on println
