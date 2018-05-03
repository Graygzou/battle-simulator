/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  */

package Creatures

class Outsider (args: Array[String])
  extends GraphEntity(args) {

  //---------------------------------
  // Should be in the list of spells
  //---------------------------------
  // Heal
  def Heal(target: GraphEntity, amount: Float) =
    println(s"I'm healing $target with given $amount")


}
