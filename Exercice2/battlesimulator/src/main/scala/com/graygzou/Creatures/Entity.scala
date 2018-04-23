/**
  * @author : Grégoire Boiron <gregoire.boiron@gmail.com>
  *           Vidal Florian
  *
  * Functions comments are from the following website :
  * http://www.d20pfsrd.com/alignment-description/movement/
  */

package com.graygzou.Creatures

import com.graygzou.Cluster.GameUtils
import com.graygzou.Creatures.SteeringBehavior.SteeringBehavior
import com.graygzou.{EntitiesRelationType, Team}
import com.jme3.math.Vector3f
import org.apache.spark.graphx.VertexId
import org.apache.spark.{SparkConf, SparkContext}
import src.main.scala.com.graygzou.Cluster.Crawler

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer

/**
  * Hint : We do not used case classes for representing entities because those are not immutable data
  * (they will take damages, move around and maybe die).
  */
class Entity(args: Array[String]) extends Serializable {

  //var crawler = new Crawler

  // Basic members fields.
  private var ownType = ""
  private var currentHealth = 0.0
  private var ownArmor = 0.0 // Should be 10 + armor bonus + shield bonus + Dexterity modifier + other modifiers
  private var ownMeleeAttack = 0.0
  private var ownMeleeAttackRange = 0.0
  private var ownRangedAttack = 0.0
  private var ownRangedAttackRange = 0.0
  private var ownRegeneration = 0.0
  var turnDone = false

  // Set his team to a default value.
  private var ownTeam = Team(0)
  private var currentPosition : Vector3f = new Vector3f(0, 0, 0)
  private var ownMaxSpeed = 0.0
  private var currentSpeed = 0.0
  private var ownMaxFly = 0.0
  private var currentFly = 0.0
  private var flying = false

  private var ownHeal = 0.0
  private var ownHealRange = 0.0



  private var ownSpells : ArrayBuffer[String] = new ArrayBuffer[String]()

  private var ownRelatedEntities : HashMap[VertexId, (Entity,EntitiesRelationType.Value)] = HashMap.empty[VertexId,(Entity,EntitiesRelationType.Value)]
  private var ownMaxHealth = 0.0
  private var ownGoal : VertexId = _

  // Used to make the agent move in the game
  //var steeringBehaviorModule = new SteeringBehavior()

  // no arguments constructor.
  def this() {
    this(Array("1","dummy","0","0","0","0","0","0","0","(0/0/0)","0","0","0","0",""))
  }

  // Function that initialize class members
  def initClassFields(): Unit = {
    ownTeam = Team(args(0).toInt - 1) // In the .txt file, we start counting team at '1' and not '0'
    ownType = args(1)
    ownMaxHealth = args(2).toDouble
    currentHealth = args(2).toDouble
    ownArmor = args(3).toDouble
    ownMeleeAttack = args(4).toDouble
    ownMeleeAttackRange = args(5).toDouble
    ownRangedAttack = args(6).toDouble
    ownRangedAttackRange = args(7).toDouble
    ownRegeneration = args(8).toDouble
    // Special case for position
    currentPosition = retrievePosition(args(9))
    ownMaxSpeed = args(10).toDouble
    currentSpeed = args(10).toDouble
    ownMaxFly = args(11).toDouble
    currentFly = args(11).toDouble
    ownHeal = args(12).toDouble
    ownHealRange = args(13).toDouble


    //ownSpells = crawler.getSpellsByCreature(ownType)

  }

  // Call the "constructor" like.
  initClassFields()

  // World coordinate
  //var ownPosition = position;

  // Accessors
  def getTeam: Team.Value = ownTeam
  def getCurrentPosition: Vector3f = currentPosition
  def getType: String = ownType
  def getHealth: Double = currentHealth
  def getArmor: Double = ownArmor
  def getMeleeAttack: Double = ownMeleeAttack
  def getMeleeAttackRange: Double = ownMeleeAttackRange
  def getRangedAttack: Double = ownRangedAttack
  def getRangedAttackRange: Double = ownRangedAttackRange
  def getRegeneration: Double = ownRegeneration
  def getSpells: ArrayBuffer[String] = ownSpells
  def getRelatedEntities: HashMap[VertexId, (Entity,EntitiesRelationType.Value)] = ownRelatedEntities
  def getMaxHealth: Double = ownMaxHealth
  def getGoal: VertexId = ownGoal
  def getMaxSpeed: Double = ownMaxSpeed
  def getCurrentSpeed: Double = currentSpeed
  def getMaxFly: Double = ownMaxFly
  def getCurrentFly: Double = currentFly
  def isFlying: Boolean = flying
  def getHeal: Double = ownHeal
  def getHealRange: Double = ownHealRange


  def hasHeal: Boolean = ownHeal > 0

  def addRelativeEntity(vertexId: VertexId, entity: Entity, relation: EntitiesRelationType.Value): Unit = {
    ownRelatedEntities += (vertexId -> (entity, relation))
  }
  // Functions that will be used for the simulation

  // Attack
  def Attack(target:Entity, damages: Double): Unit =
    println(s"I'm attacking ${target.toString} with the current damages $damages")


  // Regular movements on Land.
  /**
    * A walk represents unhurried but purposeful movement (3 miles per hour for an unencumbered adult human).
    * A character moving his speed one time in a single round, is walking when he or she moves.
    * @param x position on x axis
    * @param y position on y axis
    * @param z position on z axis
    */
  def Walk(x: Double, y: Double, z: Double): Unit =
    println(s"I'm walking to ($x $y $z) position")

  /**
    * A hustle is a jog (about 6 miles per hour for an unencumbered human). A character moving his speed twice in a
    * single round, or moving that speed in the same round that he or she performs a standard action or another move
    * action, is hustling when he or she moves.
    * @param x position on x axis
    * @param y position on y axis
    * @param z position on z axis
    */
  def Hustle(x: Double, y: Double, z: Double): Unit =
    println(s"I'm running to ($x $y $z) position")

  /**
    * Moving three times speed is a running pace for a character in heavy armor
    * (about 7 miles per hour for a human in full plate).
    * @param x position on x axis
    * @param y position on y axis
    * @param z position on z axis
    */
  def RunTimes3(x: Double, y: Double, z: Double): Unit =
    println(s"I'm runningx3 to ($x $y $z) position")

  /**
    * Moving four times speed is a running pace for a character in light, medium, or no armor (about 12 miles per hour
    * for an unencumbered human, or 9 miles per hour for a human in chainmail.)
    * @param x position on x axis
    * @param y position on y axis
    * @param z position on z axis
    */
  def RunTimes4(x: Double, y: Double, z: Double): Unit =
    println(s"I'm runningx4 to ($x $y $z) position")


  // ----- Others ------

  override def toString: String =
    s"Type: $getType, Position: $getCurrentPosition, Team: $getTeam Health: $getHealth, " +
      s"Armor: $getArmor, MeleeAttack: $getMeleeAttack (rg: $getMeleeAttackRange), RangeAttack: $getRangedAttack (rg: $getRangedAttackRange), Regeneration: $getRegeneration, Relations: ${getRelatedEntities.keySet}"


  /**
    * Find all the entities within 150ft and the closest enemy (can be farther than 150ft)
    * @return tuple of the entities nearby and the closest enemy
    */
  def searchEntitiesNearby() : (ArrayBuffer[(VertexId, (Entity, EntitiesRelationType.Value))], (VertexId, (Entity, EntitiesRelationType.Value))) = {
    var nearbyEntities = new ArrayBuffer[(VertexId, (Entity, EntitiesRelationType.Value))]()
    var firstIteration = true
    var closestEnemy: (VertexId, (Entity, EntitiesRelationType.Value)) = null
    for(entity <- ownRelatedEntities){
      if (entity._2._1.getCurrentPosition.distance(currentPosition) <= 150) {
        nearbyEntities += entity
      }
      if (entity._2._2 == EntitiesRelationType.Enemy){
        if (firstIteration){
          closestEnemy = entity
          firstIteration = false
        } else {
          if (entity._2._1.getCurrentPosition.distance(currentPosition) < closestEnemy._2._1.getCurrentPosition.distance(currentPosition)){
            closestEnemy = entity
          }
        }
      }
    }
    (nearbyEntities, closestEnemy)
  }


  /**
    * @param nearbyEntities list of the entities close
    * @param minHealth minimum maxHP to consider the entity worth to heal
    * @param minPercentage under what % of the maxHP should an entity be considered worth to be healed
    * @return tuple with a boolean (true if someone should be healed) and the entity
    */
  def findMostWoundedAlly(nearbyEntities: ArrayBuffer[(VertexId, (Entity, EntitiesRelationType.Value))], minHealth: Double, minPercentage: Double ):(Boolean, VertexId) = {
    var foundOne = false
    var ally: (VertexId, (Entity, EntitiesRelationType.Value)) = (0L,(this, EntitiesRelationType.Ally))
    var allyId: VertexId = 0L
    for (entity <- nearbyEntities){
      if (entity._2._2 == EntitiesRelationType.Ally){
        if (entity._2._1.getHealth <= minPercentage*entity._2._1.getMaxHealth && entity._2._1.getMaxHealth >= minHealth) {
          if(!foundOne){
            ally = entity
            allyId = entity._1
            foundOne = true
          } else {
            if(entity._2._1.getHealth < ally._2._1.getHealth){
              ally = entity
              allyId = entity._1
            }
          }
        }
      }
    }
    (foundOne, allyId)
  }

  def searchGoal(myVertexId: VertexId): Unit = {
    /* Search a goal :
     *    - Priority 1 : heal yourself (HP under 40%)
     *    - Priority 2 : find the most wounded ally to heal (HP under 40% and ally can't be killed in 1-2 hits)
     *    - Priority 3 : attack closest enemy
    */

    // Should the entity heal itself ?
    if (currentHealth <= 0.4*ownMaxHealth && hasHeal){
        ownGoal = myVertexId
    } else {
      // Find all the entities nearby (150ft or less)
      val entities = searchEntitiesNearby()
      val nearbyEntities = entities._1
      val closestEnemy = entities._2
      var goalFound = false

      if (nearbyEntities.nonEmpty) {
        // Should the entity heal an ally ?
        if (hasHeal){
          val ally = findMostWoundedAlly(nearbyEntities,60,0.4)
          if (ally._1){
            goalFound = true
            ownGoal = ally._2
          }
        }
        if(!goalFound){
          ownGoal = closestEnemy._1
        }
      } else {
        // Goal is the closest enemy
        ownGoal = closestEnemy._1
      }
    }
  }


  /**
    *
    * @param relationType type of the relation with the destination entity
    * @return The amount of damage/heal the entity want to affect the enemies/allies
    */

  /*
      Regarde toutes les entités associées à proximité
      Regarde si allié à besoin d'un soin (prioritaire)
      Sinon attaque si possible
      L'objectif sera mis dans une variable goal : VertexId
      On verifie dans le map que ce soit le bon objectif
   */
  def computeIA(relationType: EntitiesRelationType.Value, myVertexId: VertexId, itsVertexId: VertexId, distance: Float): (VertexId, Double) = {

    var action = (-3L,0D)

    if (!turnDone) {
      // Search a goal
      searchGoal(myVertexId)

      // The goal is the entity itself i.e. it will heal itself
      if(ownGoal == myVertexId){
        action = (myVertexId, ownHeal)
      } else {


        if (itsVertexId == ownGoal){
          // That's our goal, what should the entity do?
          if(relationType == EntitiesRelationType.Ally){
            // It's an ally, the entity should heal if its in range or move
            if(distance <= ownHealRange){

            }
          }




        } else {
          action = (ownGoal,0)
        }

      }



      //searchGoal(myVertexId)
      val d20Dice = GameUtils.rollDice(20)
      // Test, depending on the throw if the attack is successful.
      d20Dice match {
        case 1 => {
          println("Miss ...")
        }
        case 20 => println("HIT ! Maybe critical ?")
        case value => {
          println(" Let's test.. ")
          if (value + 10 > 20) {
            println(" HIT ! ")
          } else {
            println("Miss ...")
          }
        }
      }
      var value = 10

      turnDone = true
      // Return a positive value if the entity is an ally (=heal) or negative if it's an enemy (=damage)
      if (relationType == EntitiesRelationType.Ally)
        action = (ownGoal,value)
      else
        action = (ownGoal,-value)
    }
    action
  }

  /**
    * Add the given amount of health to the entity
    * Note : This value can be negative if we want to damage the life of this entity.
    * @param amount total of life we want to give back or retrieve.
    */
  def takeDamages(amount: Float): Unit = {
    currentHealth += amount
  }

  def retrievePosition(str: String) : Vector3f = {
    var position = new Vector3f(0,0,0)
    val coordinates : Array[String] = str.replace("(", "").replace(")","").split("/")
    if(coordinates.length == 3) {
      val x : Float = coordinates(0).toFloat
      val y : Float = coordinates(1).toFloat
      val z : Float = {
        coordinates(2).toFloat
      }
      position = new Vector3f(x, y, z)
    }
    position
  }

}
