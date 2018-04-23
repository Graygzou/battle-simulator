package com.graygzou.Creatures

import com.jme3.material.Material
import com.jme3.scene.Spatial

class Entity3D(args: Array[String]) extends Entity(args) {

  // Usefull for 3D representation
  //private var engineRepresentation : Spatial = _

  private var modelPath = ""

  //private var material = new Material()

  def setModelPath(path: String): Unit = {
    modelPath = path
  }

  /*
  def setSpatial(spatial: Spatial): Unit = {
    engineRepresentation = spatial

    initSpatial()
  }

  def initSpatial(): Unit = {
    // Place the entity in the world
    engineRepresentation.setLocalTranslation(getCurrentPosition);

    // Set the scale and the rotation of the entity
    engineRepresentation.setLocalScale(1, 1, 1); // TODO better

    // Load and set the material
    /*Material mat_default = new Material(
            assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");*/

  }

  def setMaterial(mat: Material): Unit = {
    material = mat
    engineRepresentation.setMaterial(mat);
  }

  def getSpatial(): Spatial = {
    return engineRepresentation
  }*/

}
