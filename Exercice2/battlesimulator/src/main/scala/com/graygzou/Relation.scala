package com.graygzou

class Relation(args: Array[String]) extends Serializable {

  private var relationType : String = args(0)

  val getType: String = relationType

  override def toString: String =
    s"relationType: ${getType},"

}
