package com.graygzou.Creatures;

import com.graygzou.Cluster.Team;
import com.graygzou.Cluster.TeamEntities;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.graygzou.Engine.StartScreenState.app;
import static com.graygzou.Engine.StartScreenState.mat_default;

public class Entity3D extends Entity implements Serializable {

  private static final long serialVersionUID = 78332556621456621L;

  private String modelPath;

  private Spatial spatial;

  //private var material = new Material()

  public Entity3D(String[] args) {
    super(args);
    // Special setup
    this.modelPath = args[args.length-1];
    this.spatial = createEntitySpatial();
  }

  public void setModelPath(String path) {
    this.modelPath = path;
  }

  public void setSpatial(Spatial spatial) {
    this.spatial = spatial;
    System.out.println(this.spatial);
  }

  public Spatial getSpatial() {
    return this.spatial;
  }

  public Spatial createEntitySpatial() {
    Spatial spatial = ((SimpleApplication) app).getAssetManager().loadModel(modelPath);
    // Set the life of the entity
    spatial.setUserData("health", this.getHealth());

    // Place the entity in the world
    spatial.setLocalTranslation(this.getCurrentPosition());
    spatial.center().move(this.getCurrentPosition().add(new Vector3f(0, 1f, 0)));

    // Set the scale and the rotation of the entity
    spatial.setLocalScale(1, 1, 1); // TODO better based on the creature

    // Material setup
    Material teamColor = mat_default.clone();
    teamColor.setColor("Color", this.getTeamColor());
    spatial.setMaterial(teamColor);

    return spatial;
  }

  @Override
  public void moveToGoal(float tpf) {
    // Retrieve the current enemy
    Entity3D enemy = (Entity3D) ownGoal()._2;
    // Make the movement
    System.out.println(super.getType());
    System.out.println(this.modelPath);
    this.spatial.move(enemy.getSpatial().getLocalRotation().mult(new Vector3f(0, 0, getCurrentSpeed() * tpf)));
  }

  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    // default deserialization
    modelPath = ois.readUTF();
    spatial = ((SimpleApplication) app).getAssetManager().loadModel(modelPath);
  }

  private void writeObject(ObjectOutputStream oos) throws IOException {
    // write the object
    oos.writeUTF(modelPath);
  }

}
