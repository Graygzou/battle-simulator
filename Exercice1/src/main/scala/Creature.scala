import scala.collection.mutable.ArrayBuffer

class Creature(val creatureName : String) extends Serializable {
  var spells =  ArrayBuffer[String]()
  val name = creatureName
  def addspell(spell : String) : Unit = {
    if (!spells.contains(spell))
      spells += spell
  }

  def isEmpty(): Boolean = {
    if (spells.isEmpty)
      return true
    else
      return false
  }

  override def toString() : String = {
    return "Name : " + name + "   Spells : " + spells.toString()
  }
}