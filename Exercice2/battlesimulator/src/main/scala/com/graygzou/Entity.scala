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

}
