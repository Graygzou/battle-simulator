package regular

object EntitiesRelationType extends Enumeration with Serializable {
  type relationType = Value
  val None, Enemy, Ally = Value
}