/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  * @version 0.0.1
  */

package com.graygzou.Cluster

import com.graygzou.Creatures.Entity
import com.graygzou.Utils.GameUtils
import com.jme3.math.ColorRGBA
import org.apache.spark.graphx.{GraphLoader, _}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

class BattleSimulationCluster(appName: String, MasterURL: String) extends Serializable {

  private val debug = false

  // Init Scala Context
  val conf = new SparkConf().setAppName(appName).setMaster(MasterURL)
  val sc = new SparkContext(conf)

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
    // TODO : Maybe use groupBy function ?
    var teamMember: List[Int] = List()
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
      // TODO instantiate special type like Humanoid, Dragon, ... instead of Entity
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

    val defaultEntity = new Entity()
    val mainGraph = Graph(gameEntities, relationGraph, defaultEntity)
    mainGraph
  }

  /**
    * Method called to create the initial graph of entities.
    * @param entitiesFile file that contrains entities.
    * @param relationFile file that contains the relationship between entities.
    * @param visualization true if we want to render in 3D the fight, false otherwise.
    * @return
    */
  def initGame(entitiesFile: String, relationFile: String, visualization: Boolean): Graph[Entity, Relation] = {

    val nbTeam = 2 // Should be in the entitiesFile or another.
    val TeamsNbMembers = Array(220, 220) // also

    // Create all the teams
    screenTeams = new Array(nbTeam)
    for (i <- 0 to (nbTeam - 1)) {
      screenTeams(i) = new TeamEntities(ColorRGBA.randomColor(), TeamsNbMembers(i))
    }

    // Setup the first graph with given files
    val mainGraph: Graph[Entity, Relation] = setupGame(entitiesFile, relationFile, visualization)

    GameUtils.printGraph(mainGraph)

    mainGraph
  }

  /**
    * Gameloop used for the cluster version only.
    * @param mainGraph
    * @return The final turn number the fight ended.
    */
  def gameloop(mainGraph: Graph[Entity, Relation]): Int = {

    // Extract all the team size and store them in a structure
    var teamMember = countTeamMember(mainGraph)
    var currentTurn = 0

    var currentGraph = mainGraph

    // While their is still two teams in competition
    // (at least one node from the last two teams)
    while (isFightNotFinished(teamMember, currentTurn)) {
      println("Turn n°" + currentTurn)

      var updatedEntities: VertexRDD[Entity] = playOneTurn(currentGraph)

      // Join the updated values to the graph
      currentGraph = currentGraph.joinVertices(updatedEntities)((_, _, newEntity) => newEntity)

      // Filter all the dead entities from the graph
      currentGraph = currentGraph.subgraph(vpred = (_, info) => info.getHealth > 0)
      if(debug) {
        currentGraph.vertices.collect.foreach(println(_))
      }

      // Wait one second
      Thread.sleep(1000)

      // the team size based on the graph
      teamMember = countTeamMember(currentGraph)

      currentTurn += 1
    }

    println("The fight is done.")
    if(currentTurn >= NbTurnMax) {
      println("It's a tie !")
    } else {
      val entitiesLeft = currentGraph.vertices.collect()
      if(entitiesLeft.length > 0) {
        println("The winning team is : " + entitiesLeft(0)._2.getTeam ) // Get the first team
      } else {
        println("You are all dead !! HAHAHAHA")
      }
    }

    currentTurn
  }

  /*
  def getWinnerTeam(): Team.Value = {
    return entitiesLeft(0)._2.getTeam
  }*/

  /**
    * Test if the fight is still going or not.
    * @param teamMember List that contains the number of alive entity for each team.
    * @param currentTurn The number of the current turn.
    * @return true if the fight is still going, false otherwise.
    */
  def isFightNotFinished(teamMember: List[Int], currentTurn: Float): Boolean = {
    return teamMember.count((numVertices) => numVertices.!=(0)) >= 2 && currentTurn <= NbTurnMax
  }


  /**
    * Play one turn of the simulation.
    * @param mainGraph current game graph.
    * @return entities updated after this turn.
    */
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
        * TODO Should check if the two node are aware of each others.
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
        entity.updateRelatedEntities()

        entity
      }
    }

    val updatedEntities = playOneTurn.mapValues(updateEntity)
    //updatedEntities.collect.foreach(println(_))
    updatedEntities
  }

}
