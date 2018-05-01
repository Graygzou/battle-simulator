/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  * @version 0.0.1
  */

package com.graygzou.Cluster

import java.util

import com.graygzou.Creatures.{GraphEntity, VisualizationEntity3D}
import com.graygzou.Utils.GameUtils
import com.jme3.math.ColorRGBA
import com.jme3.scene.Spatial
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.graphx.{GraphLoader, _}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

class BattleSimulationCluster(appName: String, MasterURL: String) extends Serializable {

  private val debug = false

  // Init Scala Context
  val conf = new SparkConf().setAppName(appName).setMaster(MasterURL)
  val sc = new SparkContext(conf)

  var mainGraph: Graph[GraphEntity, Relation] = _

  var NbTurnMax = 100 // Default value
  var currentTurn = 0

  var screenTeamsColor: Array[ColorRGBA] = _

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
        case (_, info) => info.asInstanceOf[GraphEntity].getTeam.equals(Team(team_ind))
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
  def setupGame(entitiesFile: String, relationFile: String, visualization: Boolean) {
    currentTurn = 0
    NbTurnMax = 100  // Should be in the game.txt
    val nbTeam = 2 // Should be in the entitiesFile or another.

    setupTeams(nbTeam, Array(100, 100))

    // Load the first team data and parse into tuples of entity id and attribute list
    val entitiesPath = getClass.getResource(entitiesFile)
    var gameEntities : RDD[(VertexId, GraphEntity)] = null
    if(visualization) {

      // Load the first team data and parse into tuples of entity id and attribute list
      gameEntities = sc.textFile(entitiesPath.getPath)
        .map(line => line.split(","))
        .map(parts => (parts.head.toLong, new VisualizationEntity3D(parts.tail)))
    } else {
      // Load the first team data and parse into tuples of entity id and attribute list
      gameEntities = sc.textFile(entitiesPath.getPath)
        .map(line => line.split(","))
        .map(parts => (parts.head.toLong, new com.graygzou.Creatures.GraphEntity(parts.tail)))
      // TODO instantiate special type like Humanoid, Dragon, ... instead of simple Entity
    }


    if(debug) {
      val screenEntities : Array[GraphEntity] = gameEntities.map(x => x._2).collect
      println("Nb Entity: " + screenEntities.length)
      screenEntities.foreach(e => println("print e: " + e.toString))
    }

    // Add entities to teams
    gameEntities.map(x => x._2).collect.foreach( entity => {
      entity.setTeamColor(screenTeamsColor(entity.getTeam.id))
    })

    // count in teams
    if(debug) {
      screenTeamsColor.foreach(color => {
        println("Team: " + color)
        gameEntities.map(_._2).filter(entity => entity.getTeamColor.equals(color)).collect.foreach(t =>  println("print Team" + color + " , t:" + t.toString))
      })
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

    val updatedGameEntities = new Array[(VertexId, GraphEntity)](gameEntitiesList.keySet.size)
    var i = 0
    for (key <- gameEntitiesList.keySet) {
      updatedGameEntities(i) = (key, gameEntitiesList(key))
      i += 1
    }
    gameEntities = sc.parallelize(updatedGameEntities)

    val defaultEntity = new GraphEntity()
    mainGraph = Graph(gameEntities, relationGraph, defaultEntity)

    GameUtils.printGraph(mainGraph)
  }

  /**
    * Create all the teams and store it in screenTeams
    * @param nbTeam
    * @param nbEntityPerTeams
    */
  def setupTeams(nbTeam: Int, nbEntityPerTeams: Array[Int]): Unit = {
    // Make teams a broadcast variables
    screenTeamsColor = new Array(nbTeam)
    for (i <- 0 to (nbTeam - 1)) {
      screenTeamsColor(i) = ColorRGBA.randomColor()
    }
  }

  def getEntities(): Array[GraphEntity] = {
    // Collect entities and return them
    mainGraph.vertices.map(_._2).collect()
  }

  /**
    * Give an int corresponding of the final result
    * -1 : everybody died during the fight.
    * 0 : make number of turn reached, it's a tie.
    * otherwise : the id of the winning team
    * @return an int that represent the final result of the last fight.
    */
  def getFightResult(): Int = {
    var result: Int = -1
    if(currentTurn < NbTurnMax) {
      val entitiesLeft = mainGraph.vertices.collect()
      if(entitiesLeft.length > 0) {
        // Get the first team
        result = entitiesLeft(0)._2.getTeam.id
      } else {
        result = -2
      }
    }
    result
  }

  /**
    * Test if the fight is still going or not.
    * @return true if the fight is still going, false otherwise.
    */
  def isFightNotFinished(): Boolean = {
    return countTeamMember(mainGraph).count((numVertices) => numVertices.!=(0)) >= 2 && currentTurn <= NbTurnMax
  }

  /**
    * Current turn number
    * @return current turn number
    */
  def getCurrentTurnNumber(): Int = {
    return currentTurn
  }

  /**
    * Print the fight graph.
    * TODO Make it more sexy. GameUtil printGraph ?
    */
  def printCurrentGraph(): Unit = {
    println("PRINT THE GRAPHE !")
    mainGraph.vertices.collect.foreach(println(_))
  }

  /**
    * Play one turn of the simulation.
    * @return entities updated after this turn.
    */
  def playOneTurn(): Unit = {
    playOneTurn(0.0f)
  }

  /**
    *
    * @param tpf
    */
  def playOneTurn(tpf: Float): Unit = {

    // Make tpf a broadcast variable. NOP.
    //val currentTpf = sc.broadcast(tpf)

    // ---------------------------------
    // Execute a turn of the game
    // ---------------------------------
    val playOneTurn: VertexRDD[(Float, GraphEntity)] = mainGraph.aggregateMessages[(Float, GraphEntity)](
      /**
        * SendMsg function
        * -- Should check if the opponents are aware of him (surprise round)
        * 2) Move around
        * 3) Attacks msg (random + attack > armor => attack) or Do nothing
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

        val action = entitySrc.computeIA(relationType, triplet.srcId, triplet.dstId, distance, tpf)

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
    val updateEntity = (id: VertexId, value : (Float, GraphEntity)) => {
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

    // Join the updated values to the graph
    mainGraph = mainGraph.joinVertices(updatedEntities)((_, _, newEntity) => newEntity)

    // Filter all the dead entities from the graph
    mainGraph = mainGraph.subgraph(vpred = (_, info) => info.getHealth > 0)

    // Clean the context
    //currentTpf.unpersist()
    //currentTpf.destroy()

    currentTurn += 1
  }

  def setup3DEntities(spatials : util.HashMap[String, Spatial]): Unit = {
    mainGraph.mapVertices((id: VertexId, entity: GraphEntity) => {
      entity.asInstanceOf[VisualizationEntity3D].setSpatial(spatials.get(entity.getType))
    })
  }

  /**
    * Useful for setting Spatial to the 3D entities.
    * @param f function used on each vertex.
    */
  def mapFunction(f: ((VertexId, GraphEntity) => GraphEntity)): Unit =  {

  }

}
