package com.graygzou

class Entity(name: String, health: Double, armor : Double, strength: Double, regeneration: Double) {

  // Basic constructor
  var ownName = name
  var ownHealth = health
  var ownArmor = armor
  var ownStrength = strength
  var ownRegeneration = regeneration

  // Accessors
  def getName = ownName
  def getHealth = ownHealth
  def getArmor = ownArmor
  def getStrength = ownStrength
  def getRegeneration = ownRegeneration

  // Functions that will be used for the simulation

  // Attack
  def Attack(target:Entity, damages: Double) =
    println(s"I'm attacking ${target.toString} with the current damages $damages")

  // Move
  def Move(x: Double, y: Double, z: Double) =
    println(s"I'm moving to ($x $y $z) position")

  // TODO : right the correct body of this function
  override def toString: String = super.toString

  // ----- Others ------

  // Heal

  // Buff

  // Flying => interfaces ?

}
