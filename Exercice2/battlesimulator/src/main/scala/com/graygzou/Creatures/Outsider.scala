/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  */

package com.graygzou.Creatures

class Outsider (name: String, health: Double, armor : Double, strength: Double, regeneration: Double)
  extends Entity(name, health, armor, strength, regeneration) {

  //---------------------------------
  // Should be in the list of spells
  //---------------------------------
  // Heal
  def Heal(target: Entity, amount: Float) =
    println(s"I'm healing $target with given $amount")


}
