package com.graygzou

import com.graygzou.Creatures.{Entity, Entity3D}
import com.jme3.math.ColorRGBA

class TeamEntities(color:ColorRGBA) {

  val teamColor: ColorRGBA = color
  val teamScore: Int = 0

  var aliveEntities: List[Entity3D] = List.empty

  def addEntity(entity: Entity): Unit = {
    aliveEntities.::(entity)
  }

  def removeEntity(entity: Entity): Unit = {
    aliveEntities = aliveEntities.filter(e => !e.eq(entity)  )
  }

  def countAliveEntity(): Int = {
    return aliveEntities.length
  }

}