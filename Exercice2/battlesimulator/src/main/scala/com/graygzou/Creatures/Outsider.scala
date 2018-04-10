/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  */

package com.graygzou.Creatures

class Outsider (args: Array[String])
  extends Entity(args) {

  //---------------------------------
  // Should be in the list of spells
  //---------------------------------
  // Heal
  def Heal(target: Entity, amount: Float) =
    println(s"I'm healing $target with given $amount")


}
