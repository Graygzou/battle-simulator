package com.graygzou.Creatures

import com.jme3.scene.Spatial

class Entity3D(args: Array[String]) extends Entity(args) {

  // Usefull for 3D representation
  var engineRepresentation : Spatial = _

  var modelPath = ""

}
