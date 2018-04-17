package src.main.scala.com.graygzou.Creatures
import scala.collection.mutable.ArrayBuffer

class Creature(val creatureName : String) extends Serializable {
  var spells =  ArrayBuffer[String]()
  val name = creatureName

  def addspell(spell : String) : Unit = {
    if (!spells.contains(spell))
      spells += spell
  }

  def isEmpty: Boolean = {
    if (spells.isEmpty)
      true
    else
      false
  }

  def getSpells: ArrayBuffer[String] = {
    this.spells
  }

  override def toString: String = {
    "Name : " + name + "   Spells : " + spells.toString()
  }
}