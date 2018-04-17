/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  * @version 0.0.1
  */

package com.graygzou.Cluster

import java.io

import com.graygzou.{EntitiesRelationType, Relation, Team}
import com.graygzou.Creatures.Entity
import javax.management.relation.RelationType
import org.apache.spark.graphx.{GraphLoader, _}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random
// To make some of the examples work we will also need RDD

class BattleSimulationCluster(conf: SparkConf, sc: SparkContext) extends Serializable {

  // Dummy values
  var NbTurnMax = 0

  /**
    * Method used to check the end of the fight
    * @param currentGraph : actual graph that represent alive entities
    * @return Array[Long] : represent the number of alive members of each team. Available in the following form :
    *         List(nbAliveTeam1, nbAliveTeam2, ..., nbAliveTeamN)
    */
  def countTeamMember(currentGraph: Graph[_,_]): List[Int] = {
    // ----
    // TODO : Maybe use groupBy function
    // ----
    var teamMember: List[Int] = List() // TODO make more generic
    for ( team_ind <- 0 to 1 ) {
      val verticesCurrentTeam = currentGraph.vertices.filter {
        case (id, infos) => infos.asInstanceOf[Entity].getTeam.equals(Team(team_ind))
        case _ => false
      }.count()
      teamMember = verticesCurrentTeam.toInt :: teamMember
    }
    return teamMember
  }

  /**
    * Clean the application by closing the SparkContext
    */
  def cleanScalaContext(sc: SparkContext) = {
    sc.stop();
  }

  /**
    * Method called to init the game graph.
    * @param entitiesFile File that contains all the game entities.
    * @param relationFile File that contains all the relation between entities.
    * @return The game graph will all the entities graph.
    */
  private def setupGame(entitiesFile: String, relationFile: String) : Graph[Entity, Relation] = {
    NbTurnMax = 100  // Should be in the game.txt

    // Load the first team data and parse into tuples of entity id and attribute list
    val entitiesPath = getClass.getResource(entitiesFile)
    val gameEntities : RDD[(VertexId, Entity)] = sc.textFile(entitiesPath.getPath)
      .map(line => line.split(","))
      .map(parts => (parts.head.toLong, new com.graygzou.Creatures.Entity(parts.tail)))

    // Parse the edge data which is already in userId -> userId format
    // This graph represents possible interactions (hostiles or not) between entities.
    val relationsPath = getClass.getResource(relationFile)

    // Note :
    // We do not used edgeListFile because it can only parse source and dest id.
    //val relationGraph = GraphLoader.edgeListFile(sc, relationsPath.getPath())

    // Instead we use :
    val relationGraph : RDD[Edge[Relation]] = sc.textFile(relationsPath.getPath)
      .map(line => line.split(","))
      .map(parts => Edge(parts.head.toLong, parts.tail.head.toLong, new Relation(parts.tail.tail)))

    // Attach the users attributes
    /*
    val mainGraph: Graph[Entity, PartitionID] = relationGraph.outerJoinVertices(gameEntities) {
      case (uid, deg, Some(attrList)) => {
        println("A : " + uid)
        println("B : " + deg )
        println("C : " + attrList )
        attrList
      }
      // Some users may not have attributes so we set them as empty
      case (uid, deg, None) => null
    }*/

    val defaultEntity = new Entity()

    val mainGraph = Graph(gameEntities, relationGraph, defaultEntity)

    return mainGraph
  }

  /**
    * MergeMsg function
    * Reduce Function : Received message
    * 1) Sum of the damage taken
    * 2) Check if some spells can be cast to avoid damages
    * 3) Take damages left.
    * TODO
    */
  def launchGame(entitiesFile: String, relationFile: String): Any = {

    // Init the first graph with
    var mainGraph: Graph[Entity, Relation] = setupGame(entitiesFile, relationFile)

    GameUtils.printGraph(mainGraph)

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

      GameUtils.printGraph(mainGraph)

      // ---------------------------------
      // Execute a turn of the game
      // ---------------------------------
      val playOneTurn: VertexRDD[(EntitiesRelationType.Value, Float)] = mainGraph.aggregateMessages[(EntitiesRelationType.Value, Float)](
        /**
          * SendMsg function
          * -- Should check if the opponents are aware of him (surprise round)
          * 2) Move around
          * 3) Attacks msg (random + attack > armor => attack) or Do nothin
          * 4) Heal msg
          * 5) Move around
          * // TODO Should check if the two node are aware of each others.
          */
        // Map Function : Send message (Src -> Dest) and (Dest -> Src)
        // Attention : If called, this method need to be executed in the Serialize class
        triplet => {
          // Execute the turn of the source node (entity)
          val resSrc = triplet.srcAttr.computeIA()
          println("IA source: " + resSrc)
          triplet.sendToDst((triplet.attr.getType, resSrc))

          // Execute the turn of the source node (entity)
          val resDest = triplet.dstAttr.computeIA()
          println("IA destinataire: " + resDest)
          triplet.sendToSrc((triplet.attr.getType, resDest))
        },

        // Reduce Function : Received message
        (a, b) => {
          // Same kind of messages (both heal or attacks
          if(a._1 == b._1) {
            (a._1, a._2 + b._2)
          } else {
            // TODO
            (a._1, a._2 + b._2)
          }
        }
      )

      // Update the game entities life's with corresponding messages (heals or attacks)
      val updatedEntities: VertexRDD[Entity] =
      playOneTurn.mapValues( (vertexId, value : (EntitiesRelationType.Value, Float)) =>
        value match { case (entity, amountAction) =>
          if(amountAction > 0) {
            entity.asInstanceOf[Entity].takeDamages(amountAction)
          }
          return entity
        }
      )

      // Display the results
      updatedEntities.collect.foreach(println(_))

      Thread.sleep(5000)


      // Display the current turn in console
      // TODO
      //avgAgeOfOlderFollowers.collect.foreach(println(_))

      // ---------------------------------
      // Update variables
      // ---------------------------------
      // Filter all the dead entities from the graph
      mainGraph.subgraph(vpred = (id, infos) => infos.asInstanceOf[Entity].getHealth <= 0)
      // the team size based on the graph
      teamMember = countTeamMember(mainGraph)

      currentTurn += 1
    }

    println("The fight is done.")
    if(currentTurn >= NbTurnMax) {
      println("It's a tight !")
    } else {
      println("The winning team is : " + teamMember.filter((numVertices) => numVertices.!=(0)).apply(0)) // Get the first team
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
    */
  }
}
