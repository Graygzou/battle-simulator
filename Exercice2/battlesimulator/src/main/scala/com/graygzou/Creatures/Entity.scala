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
import com.graygzou.Team
import com.jme3.math.Vector3f
import org.apache.spark.{SparkConf, SparkContext}
import src.main.scala.com.graygzou.Cluster.Crawler

import scala.collection.mutable.ArrayBuffer

/**
  * Hint : We do not used case classes for representing entities because those are not immutable data
  * (they will take damages, move around and maybe die).
  */
class Entity(args: Array[String]) extends Serializable {


  //var crawler = new Crawler

  // Basic members fields.
  private var ownType = ""
  private var ownHealth = 0.0
  private var ownArmor = 0.0 // Should be 10 + armor bonus + shield bonus + Dexterity modifier + other modifiers
  private var ownMeleeAttack = 0.0
  private var ownRangeAttack = 0.0
  private var ownRegeneration = 0.0

  // Set his team to a default value.
  private var ownTeam = Team(0);
  private var currentPosition : Vector3f = new Vector3f(0, 0, 0)
  private var ownSpells : ArrayBuffer[String] = new ArrayBuffer[String]()

  // Used to make the agent move in the game
  //var steeringBehaviorModule = new SteeringBehavior()

  // no arguments constructor.
  def this() {
    this(Array("1","dummy","0","0","0","0","0","(0,0,0)",""))
  }

  // Function that initialize class members
  def initClassFields() = {
    ownTeam = Team(args(0).toInt - 1) // In the .txt file, we start counting team at '1' and not '0'
    ownType = args(1)
    ownHealth = args(2).toDouble
    ownArmor = args(3).toDouble
    ownMeleeAttack = args(4).toDouble
    ownRangeAttack = args(5).toDouble
    ownRegeneration = args(6).toDouble
    // Special case for position
    currentPosition = retrievePosition(args(7))
    //ownSpells = crawler.getSpellsByCreature(ownType)
  }

  // Call the "constructor" like.
  initClassFields()

  // World coordinate
  //var ownPosition = position;

  // Accessors
  def getTeam = ownTeam
  def getCurrentPosition = currentPosition
  def getType = ownType
  def getHealth = ownHealth
  def getArmor = ownArmor
  def getMeleeAttack = ownMeleeAttack
  def getRangeAttack = ownRangeAttack
  def getRegeneration = ownRegeneration
  def getSpells = ownSpells

  // Functions that will be used for the simulation

  // Attack
  def Attack(target:Entity, damages: Double) =
    println(s"I'm attacking ${target.toString} with the current damages $damages")


  // Regular movements on Land.
  /**
    * A walk represents unhurried but purposeful movement (3 miles per hour for an unencumbered adult human).
    * A character moving his speed one time in a single round, is walking when he or she moves.
    * @param x
    * @param y
    * @param z
    */
  def Walk(x: Double, y: Double, z: Double) =
    println(s"I'm walking to ($x $y $z) position")

  /**
    * A hustle is a jog (about 6 miles per hour for an unencumbered human). A character moving his speed twice in a
    * single round, or moving that speed in the same round that he or she performs a standard action or another move
    * action, is hustling when he or she moves.
    * @param x
    * @param y
    * @param z
    */
  def Hustle(x: Double, y: Double, z: Double) =
    println(s"I'm running to ($x $y $z) position")

  /**
    * Moving three times speed is a running pace for a character in heavy armor
    * (about 7 miles per hour for a human in full plate).
    * @param x
    * @param y
    * @param z
    */
  def RunTimes3(x: Double, y: Double, z: Double) =
    println(s"I'm runningx3 to ($x $y $z) position")

  /**
    * Moving four times speed is a running pace for a character in light, medium, or no armor (about 12 miles per hour
    * for an unencumbered human, or 9 miles per hour for a human in chainmail.)
    * @param x
    * @param y
    * @param z
    */
  def RunTimes4(x: Double, y: Double, z: Double) =
    println(s"I'm runningx4 to ($x $y $z) position")


  // ----- Others ------

  override def toString: String =
    s"Type: ${getType}, Position: ${getCurrentPosition}, Team: ${getTeam} Health: ${getHealth}, " +
      s"Armor: ${getArmor}, MeleeAttack: ${getMeleeAttack}, RangeAttack: ${getRangeAttack}, Regeneration: ${getRegeneration}."

  /**
    *
    * @param unit TODO
    * @return The amount of damage/heal the entity want to affect the enemies/allies (always >= 0)
    */
  def computeIA(unit: Unit): Float = {
    var target = Unit

    val d20Dice = GameUtils.rollDice(20)
    // Test, depending on the throw if the attack is successful.
    d20Dice match {
      case 1 => {
        println("Miss ...")
      }
      case 20 => println("HIT ! Maybe critical ?")
      case value => {
        println(" Let's test.. ")
        if(value + 10 > 20) {
          println(" HIT ! ")
        } else {
          println("Miss ...")
        }
      }
    }

    return(10)
  }

  /**
    * Add the given amount of health to the entity
    * Note : This value can be negative if we want to damage the life of this entity.
    * @param amount total of life we want to give back or retrieve.
    */
  def takeDamages(amount: Float): Unit = {
    ownHealth += amount
  }

  def retrievePosition(str: String) : Vector3f = {
    var position = new Vector3f(0,0,0)
    val coordinates : Array[String] = str.replace("(", "").replace(")","").split(",")
    if(coordinates.length == 3) {
      val x : Float = coordinates(0).toFloat
      val y : Float = coordinates(1).toFloat
      val z : Float = {
        coordinates(2).toFloat
      }
      position = new Vector3f(x, y, z)
    }
    return position
  }

}
