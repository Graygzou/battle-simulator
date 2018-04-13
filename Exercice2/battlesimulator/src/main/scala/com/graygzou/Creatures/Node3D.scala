package com.graygzou.Creatures

import com.jme3.math.Vector3f

trait Node3D {

  // Usefull for 3D representation
  var position : Vector3f = new Vector3f(0, 0, 0)
  var rotation : Vector3f = new Vector3f(0, 0, 0)
  var scale : Vector3f = new Vector3f(1.0f, 1.0f, 1.0f)

  var modelPath = ""



}
