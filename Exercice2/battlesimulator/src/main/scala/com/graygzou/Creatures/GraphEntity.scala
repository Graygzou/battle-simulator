/**
  * @author : Grégoire Boiron <gregoire.boiron@gmail.com>
  *           Vidal Florian
  *
  * Functions comments are from the following website :
  * http://www.d20pfsrd.com/alignment-description/movement/
  */

package com.graygzou.Creatures

import com.graygzou.Cluster.{EntitiesRelationType, Team, TeamEntities}
import com.graygzou.Creatures.SteeringBehavior.{Fly, FlyQuality, SteeringBehavior}
import com.graygzou.Utils.GameUtils
import com.jme3.math.{ColorRGBA, Vector3f}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.graphx.VertexId
import org.apache.spark.{SparkConf, SparkContext}
import src.main.scala.com.graygzou.Cluster.Crawler

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer

/**
  * Hint : We do not used case classes for representing entities because those are not immutable data
  * (they will take damages, move around and maybe die).
  *
  * TODO should be abstract
  */
class GraphEntity(args: Array[String]) extends Serializable {

  //var crawler = new Crawler

  // Basic members fields.
  private var ownType = ""
  private var currentHealth = 0.0f
  private var ownArmor = 0.0f // Should be 10 + armor bonus + shield bonus + Dexterity modifier + other modifiers
  private var ownMeleeAttackDamage = 0.0f
  private var ownMeleeAttackRange = 0.0f
  private var ownMeleeAttackPrecision: Array[Float] = _
  private var ownNumberMelee = 0.0f
  private var ownRangedAttackDamage = 0.0f
  private var ownRangedAttackRange = 0.0f
  private var ownRangedAttackPrecision: Array[Float] = _
  private var ownNumberRange = 0.0f
  private var ownRegeneration = 0.0f
  var turnDone = false

  protected var ownTeam = Team(0)
  protected var teamColor = ColorRGBA.randomColor()
  protected var currentPosition : Vector3f = new Vector3f(0, 0, 0)
  protected var ownMaxSpeed = 0.0f
  protected var currentSpeed = 0.0f
  protected var ownFlyParams = new Fly(FlyQuality.None)
  protected var ownMaxFly = 0.0f
  protected var currentFly = 0.0f
  protected var flying = false

  protected var ownHeal = 0.0f
  protected var ownHealRange = 0.0f

  protected var ownSpells : ArrayBuffer[String] = new ArrayBuffer[String]()

  protected var ownRelatedEntities : HashMap[VertexId, (GraphEntity,EntitiesRelationType.Value)] = HashMap.empty[VertexId,(GraphEntity,EntitiesRelationType.Value)]
  protected var ownMaxHealth = 0.0f
  protected var ownGoal : (VertexId, GraphEntity) = _

  var meleeMode = 0
  var rangedMode = 0

  // no arguments constructor.
  def this() {
    this(Array("1","dummy","0","0","0","0","0","0","0","0","0","(0/0/0)","0","0","0","0","0",""))
  }

  // Function that initialize class members
  def initClassFields(): Unit = {
    ownTeam = Team(args(0).toInt - 1) // In the .txt file, we start counting team at '1' and not '0'
    ownType = args(1)
    ownMaxHealth = args(2).toFloat
    currentHealth = args(2).toFloat
    ownArmor = args(3).toFloat
    ownMeleeAttackDamage = args(4).toFloat
    ownMeleeAttackRange = args(5).toFloat
    ownMeleeAttackPrecision = makeArray(args(6))
    ownNumberMelee = ownMeleeAttackPrecision.length

    ownRangedAttackDamage = args(7).toFloat
    ownRangedAttackRange = args(8).toFloat
    ownRangedAttackPrecision = makeArray(args(9))
    ownNumberRange = ownRangedAttackPrecision.length

    ownRegeneration = args(10).toFloat
    // Special case for position
    currentPosition = retrievePosition(args(11))
    ownMaxSpeed = args(12).toFloat
    currentSpeed = args(12).toFloat
    ownFlyParams = new Fly(FlyQuality(args(13).toInt))
    ownMaxFly = args(14).toFloat
    currentFly = args(14).toFloat
    ownHeal = args(15).toFloat
    ownHealRange = args(16).toFloat

    // if no range attack, replace stats with melee (or we should rewrite AI)
    if (ownRangedAttackRange == 0){
      ownRangedAttackDamage = ownMeleeAttackDamage
      ownRangedAttackRange = ownMeleeAttackRange
      ownRangedAttackPrecision = ownMeleeAttackPrecision
    }

    //ownSpells = crawler.getSpellsByCreature(ownType)

  }

  // Call the "constructor" like.
  initClassFields()

  // Accessors
  def getTeam: Team.Value = ownTeam
  def getTeamColor: ColorRGBA = teamColor
  def getCurrentPosition: Vector3f = currentPosition
  def getType: String = ownType
  def getHealth: Float = currentHealth
  def getArmor: Float = ownArmor
  def getMeleeAttackDamage: Float = ownMeleeAttackDamage
  def getMeleeAttackRange: Float = ownMeleeAttackRange
  def getMeleeAttackPrecision: Array[Float] = ownMeleeAttackPrecision
  def getRangedAttackDamage: Double = ownRangedAttackDamage
  def getRangedAttackRange: Double = ownRangedAttackRange
  def getRangedAttackPrecision: Array[Float] = ownRangedAttackPrecision
  def getRegeneration: Float = ownRegeneration
  def getSpells: ArrayBuffer[String] = ownSpells
  def getRelatedEntities: HashMap[VertexId, (GraphEntity,EntitiesRelationType.Value)] = ownRelatedEntities
  def getMaxHealth: Float = ownMaxHealth
  def getGoal: (VertexId,GraphEntity) = ownGoal
  def getMaxSpeed: Float = ownMaxSpeed
  def getCurrentSpeed: Float = currentSpeed
  def getMaxFly: Float = ownMaxFly
  def getCurrentFly: Float = currentFly
  def isFlying: Boolean = flying
  def getHeal: Float = ownHeal
  def getHealRange: Float = ownHealRange

  def setCurrentPosition(newPosition: Vector3f) = currentPosition = newPosition

  def setTeamColor(color: ColorRGBA) = teamColor = color

  def hasHeal: Boolean = ownHeal > 0

  def regenerate(): Unit = currentHealth += ownRegeneration

  def fixHealth(): Unit = if (currentHealth > ownMaxHealth) currentHealth = ownMaxHealth

  def resetTurn(): Unit = {
    turnDone = false
    meleeMode = 0
    rangedMode = 0
  }

  def resetSpeed(): Unit = {
    currentSpeed = ownMaxSpeed
    currentFly = ownMaxFly
  }

  def updateRelatedEntities(): Unit = {
    var relatedEntities : HashMap[VertexId, (GraphEntity,EntitiesRelationType.Value)] = HashMap.empty[VertexId,(GraphEntity,EntitiesRelationType.Value)]
    for (entity <- ownRelatedEntities) {
      if (entity._2._1.getHealth > 0) {
        relatedEntities += entity
      }
    }
    ownRelatedEntities = relatedEntities
  }

  def addRelativeEntity(vertexId: VertexId, entity: GraphEntity, relation: EntitiesRelationType.Value): Unit = {
    ownRelatedEntities += (vertexId -> (entity, relation))
  }
  // Functions that will be used for the simulation

  // Attack
  def attack(target:GraphEntity, damages: Double): Unit =
    println(s"I'm attacking ${target.toString} with the current damages $damages")


  // Regular movements on Land.
  /**
    * A walk represents unhurried but purposeful movement (3 miles per hour for an unencumbered adult human).
    * A character moving his speed one time in a single round, is walking when he or she moves.
    * @param x position on x axis
    * @param y position on y axis
    * @param z position on z axis
    */
  def walk(x: Double, y: Double, z: Double): Unit =
    println(s"I'm walking to ($x $y $z) position")

  /**
    * A hustle is a jog (about 6 miles per hour for an unencumbered human). A character moving his speed twice in a
    * single round, or moving that speed in the same round that he or she performs a standard action or another move
    * action, is hustling when he or she moves.
    * @param x position on x axis
    * @param y position on y axis
    * @param z position on z axis
    */
  def hustle(x: Double, y: Double, z: Double): Unit =
    println(s"I'm running to ($x $y $z) position")

  /**
    * Moving three times speed is a running pace for a character in heavy armor
    * (about 7 miles per hour for a human in full plate).
    * @param x position on x axis
    * @param y position on y axis
    * @param z position on z axis
    */
  def runTimes3(x: Double, y: Double, z: Double): Unit =
    println(s"I'm runningx3 to ($x $y $z) position")

  /**
    * Moving four times speed is a running pace for a character in light, medium, or no armor (about 12 miles per hour
    * for an unencumbered human, or 9 miles per hour for a human in chainmail.)
    * @param x position on x axis
    * @param y position on y axis
    * @param z position on z axis
    */
  def runTimes4(x: Double, y: Double, z: Double): Unit =
    println(s"I'm runningx4 to ($x $y $z) position")


  // ----- Others ------

  override def toString: String =
    s"Type: $getType, Position: $getCurrentPosition, Team: $getTeam Health: $getHealth, " +
      s"Armor: $getArmor, MeleeAttack: $getMeleeAttackDamage (rg: $getMeleeAttackRange), RangeAttack: $getRangedAttackDamage (rg: $getRangedAttackRange), Regeneration: $getRegeneration, Relations: ${getRelatedEntities.keySet}"


  /**
    * Find all the entities within a range and the closest enemy
    * @param range range of the search
    * @return tuple of the entities nearby and the closest enemy
    */
  def searchEntitiesNearby(range: Double) : (ArrayBuffer[(VertexId, (GraphEntity, EntitiesRelationType.Value))], (VertexId, (GraphEntity, EntitiesRelationType.Value))) = {
    var nearbyEntities = new ArrayBuffer[(VertexId, (GraphEntity, EntitiesRelationType.Value))]()
    var firstIteration = true
    var closestEnemy: (VertexId, (GraphEntity, EntitiesRelationType.Value)) = null
    for(entity <- ownRelatedEntities){
      if (entity._2._1.getCurrentPosition.distance(currentPosition) <= range) {
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
  def findMostWoundedAlly(nearbyEntities: ArrayBuffer[(VertexId, (GraphEntity, EntitiesRelationType.Value))], minHealth: Double, minPercentage: Double ):(Boolean, (VertexId,GraphEntity)) = {
    var foundOne = false
    var ally: (VertexId, (GraphEntity, EntitiesRelationType.Value)) = (0L,(this, EntitiesRelationType.Ally))
    var allyId: (VertexId,GraphEntity) = (0L, this)
    for (entity <- nearbyEntities){
      if (entity._2._2 == EntitiesRelationType.Ally){
        if (entity._2._1.getHealth <= minPercentage*entity._2._1.getMaxHealth && entity._2._1.getMaxHealth >= minHealth) {
          if(!foundOne){
            ally = entity
            allyId = (entity._1,entity._2._1)
            foundOne = true
          } else {
            if(entity._2._1.getHealth < ally._2._1.getHealth){
              ally = entity
              allyId = (entity._1,entity._2._1)
            }
          }
        }
      }
    }
    (foundOne, allyId)
  }

  /**
    * Search a goal :
    *    - Priority 1 : heal yourself (HP under 40%)
    *    - Priority 2 : find the most wounded ally to heal (HP under 40% and ally can't be killed in 1-2 hits)
    *    - Priority 3 : attack closest enemy
    *
    * @param myVertexId id of the current in case it needs to be its own goal
    * @param range how far should the research be
    * @return Tuple with a boolean to know if a goal exists and vertexId of the closest enemy (except if the goal is
    *         the entity itself)
    */
  def searchGoal(myVertexId: VertexId, range: Double): (Boolean, (VertexId, GraphEntity)) = {
    /* Search a goal :
     *    - Priority 1 : heal yourself (HP under 40%)
     *    - Priority 2 : find the most wounded ally to heal (HP under 40% and ally can't be killed in 1-2 hits)
     *    - Priority 3 : attack closest enemy
    */

    var goalFound = false
    var result = (false, (myVertexId,this))
    // Should the entity heal itself ?
    if (currentHealth <= 0.4*ownMaxHealth && hasHeal){
      ownGoal = (myVertexId, this)
      result = (true, (myVertexId,this))
      result
    } else {
      // Find all the entities nearby
      val entities = searchEntitiesNearby(range)
      val nearbyEntities = entities._1
      val closestEnemy = entities._2

      if (nearbyEntities.nonEmpty) {
        // Should the entity heal an ally ?
        if (hasHeal){
          val ally = findMostWoundedAlly(nearbyEntities,60,0.4)
          if (ally._1){
            goalFound = true
            ownGoal = ally._2
            result = (true, (closestEnemy._1,closestEnemy._2._1))
          }
        }
        if(!goalFound){
          goalFound = true
          ownGoal = (closestEnemy._1, closestEnemy._2._1)
          result = (true, (closestEnemy._1,closestEnemy._2._1))
        }
      } else {
        // Goal is the closest enemy
        ownGoal = (closestEnemy._1, closestEnemy._2._1)
        result = (false, (closestEnemy._1,closestEnemy._2._1))
      }
      //println(myVertexId + " TARGET : " + ownGoal._1 + " ("+ownGoal._2.getHealth+")")

      result
    }
  }


  def computeDamages(baseDamage: Float, precision: Array[Float], index: Int): Float = {

    val d20Dice = GameUtils.rollDice(20)
    var damages = 0.0
    // Test, depending on the throw if the attack is successful.
    d20Dice match {
      case 1 =>
        //println("Miss ...")

      case 20 =>
        val d1 = GameUtils.rollDice(6)
        val d2 = GameUtils.rollDice(6)
        val d3 = GameUtils.rollDice(6)
        damages = baseDamage + d1 + d2 + d3

      case value =>
        //println(" Let's test.. ")
        if (value +  precision(index) > ownGoal._2.getArmor) {
          //println(" HIT ! ")
          val d1 = GameUtils.rollDice(6)
          val d2 = GameUtils.rollDice(6)
          damages = baseDamage + d1 + d2
        } else {
          //println("Miss ... Value="+value+" precision="+precision+" // armor="+ownGoal._2.getArmor)
        }
    }
    damages.toFloat
  }

  def moveToGoal() {
    moveToGoal(0f)
  }

  /**
    * Move the entity towards it's goal
    */
  def moveToGoal(tpf: Float): Unit = {
    val targetPos = ownGoal._2.currentPosition
    var d = 0F
    if (flying) {
      val distance = Math.sqrt(Math.pow(currentPosition.x - targetPos.x, 2) + Math.pow(currentPosition.y - targetPos.y, 2))
      d = if (currentFly > distance) (currentFly - distance).toFloat else currentFly.toFloat
    } else {
      val distance = if(targetPos.z > 0) Math.sqrt(Math.pow(currentPosition.x - targetPos.x, 2) + Math.pow(currentPosition.y - targetPos.y, 2)) else targetPos.distance(currentPosition)
      d = if (currentSpeed > distance) (currentSpeed - distance).toFloat else currentSpeed.toFloat
    }

    val deltaX = targetPos.x - currentPosition.x
    val deltaY = targetPos.y - currentPosition.y

    var newDeltaX = deltaX
    var newDeltaY = deltaY

    if (deltaX == 0 && deltaY != 0) {
      newDeltaY = if (deltaY < 0) -d else d

    } else if (deltaY == 0 && deltaY != 0) {
      newDeltaX = if (deltaX < 0) -d else d

    } else if (deltaX != 0 && deltaY != 0) {
      /* Linear function :  deltaY  = a * deltaX
     *                    newDeltaY = a * newDeltaX
     * Pythagore : d² = newDeltaX² + newDeltaY² = newDeltaX² * (a²+1)
     *
     * => newDeltaX = d / sqrt(a²+1)
     */

      // Compute new coordinates
      val a = deltaY / deltaX
      newDeltaX = (d / Math.sqrt(a * a + 1)).toFloat
      newDeltaY = newDeltaX * a

      if (deltaX > 0) newDeltaX = Math.abs(newDeltaX) else newDeltaX = -Math.abs(newDeltaX)
      if (deltaY > 0) newDeltaY = Math.abs(newDeltaY) else newDeltaY = -Math.abs(newDeltaY)
    }

    // Update entity
    currentPosition.x += newDeltaX
    currentPosition.y += newDeltaY
    if (flying) {
      currentFly -= d
    } else {
      currentSpeed -= d
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
  def computeIA(relationType: EntitiesRelationType.Value, myVertexId: VertexId, itsVertexId: VertexId, distanceInit: Float, tpf: Float): (VertexId, Double) = {

    var action = (-3L,0D)
    var distance = distanceInit

    // Called to filter dead entities.
    updateRelatedEntities()

    // If the entity is Good+ at flying and has range attack, we should fly for better speed and avoid melee attacks + throwing axes
    if ((ownFlyParams.maneuverability == FlyQuality.Good || ownFlyParams.maneuverability == FlyQuality.Perfect) && ownRangedAttackRange > 15 && currentPosition.z != 15){
      if (ownFlyParams.maneuverability == FlyQuality.Good ) {
        currentFly -= 30
      } else {
        currentFly -= 15
      }
      flying = true
      currentPosition.z = 15
    }

    if (!turnDone) {
      // Search a goal
      var potentialAllyInRange = false
      var result: (Boolean,(VertexId,GraphEntity)) = (false, (1L,this))

      if (hasHeal) {
        val range = if (flying) Math.sqrt(Math.pow(ownHealRange + currentFly,2)-Math.pow(currentPosition.z,2)) else ownHealRange + currentSpeed
        result = searchGoal(myVertexId, range)
        potentialAllyInRange = true
      } else {
        val range = if (flying) Math.sqrt(Math.pow(ownRangedAttackRange + currentFly,2)-Math.pow(currentPosition.z,2)) else ownRangedAttackRange + currentSpeed
        result = searchGoal(myVertexId, range)
      }

      if (result._1) {
        // The goal is the entity itself i.e. it will heal itself
        if (ownGoal._1 == myVertexId) {
          action = (myVertexId, ownHeal + GameUtils.rollDice(10))
          turnDone = true
        } else {
          if (itsVertexId == ownGoal._1) {
            // That's our goal, what should the entity do?
            if (relationType == EntitiesRelationType.Ally) {
              // It's an ally, the entity should heal if it is in range
              if (distance < ownHealRange) {
                action = (ownGoal._1, ownHeal + GameUtils.rollDice(10))
                turnDone = true
              } else if (potentialAllyInRange){
                // Ally can be reached with movement
                moveToGoal()
                distance = currentPosition.distance(ownGoal._2.currentPosition)
                action = (ownGoal._1, ownHeal + GameUtils.rollDice(10))
                turnDone = true
              } else {
                // Ally isn't in range, entity should try to attack the closest enemy if it is in range
                ownGoal = result._2
                if (itsVertexId == ownGoal._1 && distance <= ownRangedAttackRange + currentSpeed) {

                  if (distance <= ownRangedAttackRange) {
                    // No need to move for a ranged attack but can we melee with a move?
                    if (distance <= ownMeleeAttackRange) {
                      action = (ownGoal._1, -computeDamages(ownMeleeAttackDamage, ownMeleeAttackPrecision, meleeMode))
                    } else if (distance <= ownMeleeAttackRange + currentSpeed) {
                      moveToGoal()
                      distance = currentPosition.distance(ownGoal._2.currentPosition)
                      action = (ownGoal._1, -computeDamages(ownMeleeAttackDamage, ownMeleeAttackPrecision, meleeMode))
                    }
                    // Can't melee, so ranged attack
                    action = (ownGoal._1, -computeDamages(ownRangedAttackDamage, ownRangedAttackPrecision, rangedMode))
                  } else {
                    // Move and attack
                    moveToGoal()
                    distance = currentPosition.distance(ownGoal._2.currentPosition)
                    action = (ownGoal._1, -computeDamages(ownRangedAttackDamage, ownRangedAttackPrecision, rangedMode))
                  }
                  turnDone = true
                }
              }
            } else {
              // It's an enemy, let's attack
              if (distance <= ownRangedAttackRange) {
                // No need to move for a ranged attack but can we melee with a move?
                if (distance <= ownMeleeAttackRange) {
                  action = (ownGoal._1, -computeDamages(ownMeleeAttackDamage, ownMeleeAttackPrecision, meleeMode))
                } else if (distance <= ownMeleeAttackRange + currentSpeed) {
                  moveToGoal()
                  distance = currentPosition.distance(ownGoal._2.currentPosition)
                  action = (ownGoal._1, -computeDamages(ownMeleeAttackDamage, ownMeleeAttackPrecision, meleeMode))
                }
                // Can't melee, so ranged attack
                action = (ownGoal._1, -computeDamages(ownRangedAttackDamage, ownRangedAttackPrecision, rangedMode))
              } else {
                // Move and attack
                moveToGoal()
                distance = currentPosition.distance(ownGoal._2.currentPosition)
                action = (ownGoal._1, -computeDamages(ownRangedAttackDamage, ownRangedAttackPrecision, rangedMode))
              }
              turnDone = true
            }
          }
        }
      } else {
        moveToGoal(tpf)
        distance = currentPosition.distance(ownGoal._2.currentPosition)
      }
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
    val coordinates : Array[Float] = makeArray(str)
    if(coordinates.length == 3) {
      position = new Vector3f(coordinates(0).toFloat, coordinates(1).toFloat, coordinates(2).toFloat)
    }
    position
  }

  def makeArray(str: String): Array[Float] = {
    val arrayString : Array[String] = str.replace("(", "").replace(")","").split("/")
    var arrayDouble : Array[Float] = new Array[Float](arrayString.length)
    for (i <- arrayString.indices){
      arrayDouble(i) = arrayString(i).toFloat
    }
    arrayDouble
  }
}
