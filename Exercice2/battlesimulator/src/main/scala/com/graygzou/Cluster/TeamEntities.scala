package com.graygzou.Cluster

import com.graygzou.Creatures.GraphEntity
import com.jme3.math.ColorRGBA

class TeamEntities(color:ColorRGBA, nbMember: Int) {

  private val teamColor: ColorRGBA = color
  private val teamScore: Int = 0

  private var aliveEntities: Array[GraphEntity] = new Array(nbMember)
  private var actualCount: Int = 0

  def addEntity(entity: GraphEntity): Unit = {
    aliveEntities(actualCount) = entity
    actualCount += 1
  }

  def removeEntity(entity: GraphEntity): Unit = {
    aliveEntities = aliveEntities.filter(e => !e.eq(entity)  )
  }

  def getEntities(): Array[GraphEntity] = {
    return aliveEntities
  }

  def countAliveEntity(): Int = {
    return actualCount
  }

  def getTeamColor() : ColorRGBA = {
    return teamColor
  }

  override def toString: String = {
    return "{" + teamColor + ',' + teamScore + ", Members :" + actualCount + ',' + ',' + aliveEntities.filter(e => e != null).foreach(e => print(e.toString)) + "}"
  }

}