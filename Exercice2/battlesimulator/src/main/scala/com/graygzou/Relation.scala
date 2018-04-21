package com.graygzou

class Relation(args: Array[String]) extends Serializable {

  private var relationType = EntitiesRelationType(args(0).toInt)

  val getType : EntitiesRelationType.Value = relationType

  override def toString: String =
    s"relationType: ${getType},"

}
