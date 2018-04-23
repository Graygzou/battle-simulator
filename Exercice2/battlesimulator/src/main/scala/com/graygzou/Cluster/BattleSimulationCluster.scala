/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  * @version 0.0.1
  */

package com.graygzou.Cluster

import com.graygzou.{EntitiesRelationType, Relation, Team, TeamEntities}
import com.graygzou.Creatures.Entity
import com.jme3.math.ColorRGBA
import org.apache.spark.graphx.{GraphLoader, _}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}


// To make some of the examples work we will also need RDD

class BattleSimulationCluster(appName: String, MasterURL: String) extends Serializable {

  private val debug = false

  // Init Scala Context
  // Create the SparkConf and the SparkContext with the correct value
  // Usefull for the 3D visualization
  val conf = new SparkConf().setAppName(appName).setMaster(MasterURL)
  val sc = new SparkContext(conf)

  // Dummy values
  var NbTurnMax = 0

  // 3D Variables
  var screenEntities: Array[Entity] = Array.empty
  var screenTeams: Array[TeamEntities] = Array.empty

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
        case (_, info) => info.asInstanceOf[Entity].getTeam.equals(Team(team_ind))
        case _ => false
      }.count()
      teamMember = verticesCurrentTeam.toInt :: teamMember
    }
    teamMember
  }

  /**
    * Clean the application by closing the SparkContext
    */
  def cleanScalaContext(): Unit = {
    sc.stop()
  }

  /**
    * Method called to init the game graph.
    * @param entitiesFile File that contains all the game entities.
    * @param relationFile File that contains all the relation between entities.
    * @return The game graph will all the entities graph.
    */
  private def setupGame(entitiesFile: String, relationFile: String, visualization: Boolean) : Graph[Entity, Relation] = {
    NbTurnMax = 100  // Should be in the game.txt

    // Load the first team data and parse into tuples of entity id and attribute list
    val entitiesPath = getClass.getResource(entitiesFile)
    var gameEntities : RDD[(VertexId, Entity)] = null
    if(visualization) {
      // Load the first team data and parse into tuples of entity id and attribute list

      gameEntities = sc.textFile(entitiesPath.getPath)
        .map(line => line.split(","))
        .map(parts => (parts.head.toLong, new com.graygzou.Creatures.Entity3D(parts.tail)))
    } else {
      // Load the first team data and parse into tuples of entity id and attribute list
      gameEntities = sc.textFile(entitiesPath.getPath)
        .map(line => line.split(","))
        .map(parts => (parts.head.toLong, new com.graygzou.Creatures.Entity(parts.tail)))
    }

    // Retrieve screen entities
    screenEntities = gameEntities.map(x => x._2).collect

    if(debug) {
      println("Nb Entity: " + screenEntities.length)
      screenEntities.foreach(e => println(e.toString))
    }

    // Create teams
    screenEntities.foreach( e => {
      println(e.getTeam.id)
      screenTeams(e.getTeam.id).addEntity(e)
    })

    // count in teams
    if(debug) {
      println("Nb Team " + screenTeams.length)
      screenTeams.foreach(t =>  println(t.toString))
    }

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
      gameEntitiesList(relation.srcId).addRelativeEntity(relationId, gameEntitiesList(relationId), relation.attr.getType)
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
  def initGame(entitiesFile: String, relationFile: String, visualization: Boolean): Graph[Entity, Relation] = {

    val nbTeam = 2 // Should be in the entitiesFile or another.
    val TeamsNbMembers = Array(100, 100) // also

    // Create all the teams
    screenTeams = new Array(nbTeam)
    for (i <- 0 to (nbTeam - 1)) {
      screenTeams(i) = new TeamEntities(ColorRGBA.randomColor(), TeamsNbMembers(i))
    }

    // Setup the first graph with given files
    var mainGraph: Graph[Entity, Relation] = setupGame(entitiesFile, relationFile, visualization)

    GameUtils.printGraph(mainGraph)

    return mainGraph
  }

  def gameloop(mainGraph: Graph[Entity, Relation]): Int = {

    // Extract all the team size and store them in a structure
    var teamMember = countTeamMember(mainGraph)
    var currentTurn = 0

    var currentGraph = mainGraph

    // --------------
    // Gameloop
    // --------------
    // While their is still two teams in competition
    // (at least one node from the last two teams)
    while (teamMember.count((numVertices) => numVertices.!=(0)) >= 2 && currentTurn <= NbTurnMax) {
      println("Turn n°" + currentTurn)

      GameUtils.printGraph(currentGraph)

      var updatedEntities: VertexRDD[Entity] = playOneTurn(currentGraph)

      // Join the updated values to the graph
      currentGraph = currentGraph.joinVertices(updatedEntities)((_, _, newEntity) => newEntity)

      // Filter all the dead entities from the graph
      currentGraph = currentGraph.subgraph(vpred = (_, info) => info.getHealth > 0)
      currentGraph.vertices.collect.foreach(println(_))
      Thread.sleep(5000)

      // the team size based on the graph
      teamMember = countTeamMember(currentGraph)

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
        println("You are all dead !! HAHAHAHA")
      }
    }

    return currentTurn

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


  def playOneTurn(mainGraph: Graph[Entity, Relation]): VertexRDD[Entity] = {
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
        * 5) Move arounddef initGame(entitiesFile: String, relationFile: String): Unit = {
        *
        * val nbTeam = 2 // Should be in the entitiesFile or another.
        * val TeamsNbMembers = Array(100, 100) // also
        *
        * // Create all the teams
        * screenTeams = new Array(nbTeam)
        * for(i <- 0 to (nbTeam-1)) {
        * screenTeams(i) = new TeamEntities(ColorRGBA.randomColor(), TeamsNbMembers(i))
        * }
        *
        * // Init the first graph with
        * var mainGraph: Graph[Entity, Relation] = setupGame(entitiesFile, relationFile)
        *
        *     GameUtils.printGraph(mainGraph)
        *
        * // play the game
        * /*
        * val currentTurn = gameloop(mainGraph)
        *
        * println("The fight is done.")
        * if(currentTurn >= NbTurnMax) {
        * println("It's a tie !")
        * } else {
        * val entitiesLeft = mainGraph.vertices.collect()
        * if(entitiesLeft.length > 0) {
        * println("The winning team is : " + entitiesLeft(0)._2.getTeam) // Get the first team
        * } else {
        * println("You are both dead !! HAHAHAHA")
        * }
        * }*/
        * }
        * // TODO Should check if the two node are aware of each others.
        */
      // Map Function : Send message (Src -> Dest) and (Dest -> Src)
      // Attention : If called, this method need to be executed in the Serialize class
      triplet => {
        val entitySrc = triplet.srcAttr
        val distance = entitySrc.getCurrentPosition.distance(triplet.dstAttr.getCurrentPosition)
        val relationType = triplet.attr.getType

        val action = entitySrc.computeIA(relationType, triplet.srcId, triplet.dstId, distance)

        if (action._1 == triplet.srcId){
          triplet.sendToSrc((action._2.toFloat, entitySrc))
        } else if (action._1 == triplet.dstId) {
          triplet.sendToDst((action._2.toFloat, triplet.dstAttr))
        }
        triplet.sendToSrc(0, entitySrc)
      },

      // Reduce Function : Received message
      (a, b) => {
        (a._1 + b._1, a._2)
      }
    )

    // Update the entities health points with corresponding messages (heals or attacks)
    val updateEntity = (id: VertexId, value : (Float, Entity)) => {
      value match { case (amountAction, entity) =>
        // Update damages
        if(amountAction != 0) {
        print("Entity has been updated : "+id+ " - "+ entity.getType + " from team " + entity.getTeam + " received a total of" + amountAction + "HP (from " + entity.getHealth + "hp to ")
        entity.takeDamages(amountAction)
        println(entity.getHealth + "hp)")
        }

        // Update other fields
        entity.resetTurn()
        entity.resetSpeed()
        entity.regenerate()
        entity.fixHealth()

        entity
      }
    }

    val updatedEntities = playOneTurn.mapValues(updateEntity)
    updatedEntities.collect.foreach(println(_))
    updatedEntities
  }

}
