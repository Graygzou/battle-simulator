package com.graygzou.Creatures;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class Entity3D extends Entity {

  private String modelPath;

  private transient Spatial spatial;

  //private var material = new Material()

  public Entity3D(String[] args) {
    super(args);
  }

  public void setModelPath(String path) {
    this.modelPath = path;
  }

  public void setSpatial(Spatial spatial) {
    this.spatial = spatial;
  }

  public Spatial getSpatial() {
    return spatial;
  }

  @Override
  public void moveToGoal(float tpf) {
    // Retrieve the current enemy
    Entity3D enemy = (Entity3D) ownGoal()._2;
    // Make the movement
    this.spatial.move(enemy.getSpatial().getLocalRotation().mult(new Vector3f(0, 0, getCurrentSpeed() * tpf)));
  }

}
