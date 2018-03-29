import scala.collection.mutable.ArrayBuffer

class Creature(val name : String) extends Serializable {
  var spells =  ArrayBuffer[String]()
  def addspell(spell : String) : Unit = {
    if (!spells.contains(spell))
      spells += spell
  }

  override def toString() : String = {
    return "Name : " + name + "   Spells : " + spells.toString()
  }
}