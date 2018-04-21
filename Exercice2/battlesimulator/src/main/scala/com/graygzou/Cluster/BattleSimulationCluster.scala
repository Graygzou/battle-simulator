/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  * @version 0.0.1
  */

package com.graygzou.Cluster

import java.{io, util}

import com.graygzou.{EntitiesRelationType, Relation, Team}
import com.graygzou.Creatures.Entity
import org.apache.spark.graphx.{GraphLoader, _}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.immutable.HashMap


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
        case (_, infos) => infos.asInstanceOf[Entity].getTeam.equals(Team(team_ind))
        case _ => false
      }.count()
      teamMember = verticesCurrentTeam.toInt :: teamMember
    }
    teamMember
  }

  /**
    * Clean the application by closing the SparkContext
    */
  def cleanScalaContext(sc: SparkContext): Unit = {
    sc.stop()
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
    var gameEntities : RDD[(VertexId, Entity)] = sc.textFile(entitiesPath.getPath)
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


    // Add related entities (not optimized AT ALL but it does the job)
    val gameEntitiesList = gameEntities.collectAsMap()
    val relationGraphList = relationGraph.collect()

    for (relation <- relationGraphList){
      val relationId = relation.dstId
      gameEntitiesList(relation.srcId).addRelativeEntity(relationId, gameEntitiesList(relationId))
    }

    val updatedGameEntities = new Array[(VertexId, Entity)](gameEntitiesList.keySet.size)
    var i = 0
    for (key <- gameEntitiesList.keySet) {
      updatedGameEntities(i) = (key, gameEntitiesList(key))
      i += 1
    }
    gameEntities = sc.parallelize(updatedGameEntities)



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
    mainGraph
  }

  /**
    * MergeMsg function
    * Reduce Function : Received message
    * 1) Sum of the damage taken
    * 2) Check if some spells can be cast to avoid damages
    * 3) Take damages left.
    * TODO
    */
  def launchGame(entitiesFile: String, relationFile: String): Unit = {

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
      val playOneTurn: VertexRDD[(Float, Entity)] = mainGraph.aggregateMessages[(Float, Entity)](
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
          //TODO: Replace 50 with the range of the entity (needs to be added to entity)
          if (triplet.srcAttr.getCurrentPosition.distance(triplet.dstAttr.getCurrentPosition) <= 50 ) {
            // Execute the turn of the source node (entity)
            val relationType = triplet.attr.getType
            val resSrc = triplet.srcAttr.computeIA(relationType)
            println("IA source: " + resSrc)
            triplet.sendToDst((resSrc, triplet.dstAttr))
          } else {
            triplet.sendToDst((0, triplet.dstAttr))
          }
        },

        // Reduce Function : Received message
        (a, b) => {
          (a._1 + b._1, a._2)
        }
      )

      // Update the entities health points with corresponding messages (heals or attacks)
      val updateEntity = (id: VertexId, value : (Float, Entity)) => {
        value match { case (amountAction, entity) =>
          //if(amountAction != 0) {
            print("Entity has been updated : "+id+ " - "+ entity.getType + " from team " + entity.getTeam + " received a total of" + amountAction + "HP (from " + entity.getHealth + "hp to ")
            entity.takeDamages(amountAction)
            println(entity.getHealth + "hp)")
         // }
          entity
        }
      }

      val updatedEntities = playOneTurn.mapValues(updateEntity)
      updatedEntities.collect.foreach(println(_))

      // Join the updated values to the graph
      mainGraph = mainGraph.joinVertices(updatedEntities)((_, _, newEntity) => newEntity)

      println("After damages and heals :")
      mainGraph.vertices.collect.foreach(println(_))

      // Filter all the dead entities from the graph
      mainGraph = mainGraph.subgraph(vpred = (_, infos) => infos.getHealth > 0)
      println("After death entity removel :")
      mainGraph.vertices.collect.foreach(println(_))
      Thread.sleep(5000)

      // the team size based on the graph
      teamMember = countTeamMember(mainGraph)

      currentTurn += 1
    }

    println("The fight is done.")
    if(currentTurn >= NbTurnMax) {
      println("It's a tie !")
    } else {
      val entitiesLeft = mainGraph.vertices.collect()
      if(entitiesLeft.length > 0) {
        println("The winning team is : " + entitiesLeft(0)._2.getTeam) // Get the first team
      } else {
        println("You are both dead !! HAHAHAHA")
      }
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
