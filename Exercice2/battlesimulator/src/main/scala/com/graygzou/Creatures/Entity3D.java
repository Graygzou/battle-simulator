package com.graygzou.Creatures;

import com.graygzou.Cluster.TeamEntities;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static com.graygzou.Engine.StartScreenState.app;
import static com.graygzou.Engine.StartScreenState.mat_default;

public class Entity3D extends Entity implements Serializable {

  private static final long serialVersionUID = 78332556621456621L;

  private String modelPath;

  private Quaternion currentRotation;

  // 3D components
  private Node node;
  private Spatial spatial;
  private Geometry healthbar;

  public Entity3D(String[] args) {
    super(args);
    // Special setup
    this.modelPath = args[args.length-1];

    // Regroup all the entity
    createEntityNode();
  }

  public void setModelPath(String path) {
    this.modelPath = path;
  }

  public void setSpatial(Spatial spatial) {
    this.spatial = spatial;
    System.out.println("Set spatial: " + this.spatial);
  }

  @Override
  public void setTeamColor(ColorRGBA color) {
    super.setTeamColor(color);
    setMaterialSpatial();
  }

  public void setMaterialSpatial() {
    // Material setup
    Material teamColor = mat_default.clone();
    teamColor.setColor("Color", this.getTeamColor());
    this.spatial.setMaterial(teamColor);
  }

  public Spatial getSpatial() {
    return this.spatial;
  }

  public Node getNode() {
    return this.node;
  }

  /**
   *
   * @param ois
   * @throws ClassNotFoundException
   * @throws IOException
   */
  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    modelPath = ois.readUTF();
    // Recreate the entity node.
    createEntityNode();
  }

  /**
   *
   * @param oos
   * @throws IOException
   */
  private void writeObject(ObjectOutputStream oos) throws IOException {
    // write the object
    oos.writeUTF(modelPath);
  }

  // ------------------------
  // region 3D component
  // ------------------------
  private void createEntitySpatial() {
    this.spatial = ((SimpleApplication) app).getAssetManager().loadModel(this.modelPath);
    // Set the life of the entity
    this.spatial.setUserData("health", this.getHealth());

    // Place the entity in the world
    this.spatial.setLocalTranslation(this.getCurrentPosition());
    System.out.println("Current position: " + this.getCurrentPosition());
    this.spatial.center().move(this.getCurrentPosition().add(new Vector3f(0, 1f, 0)));

    // Set the scale and the rotation of the entity
    this.spatial.setLocalScale(1, 1, 1); // TODO better based on the creature

  }

  private void createHealthBar() {
    BillboardControl billboard = new BillboardControl();
    this.healthbar = new Geometry("healthbar", new Quad(3f, 0.2f));
    Material mathb = mat_default.clone();
    mathb.setColor("Color", ColorRGBA.Red);
    this.healthbar.setMaterial(mathb);

    this.healthbar.center();
    this.healthbar.move(this.spatial.getLocalTranslation().add(Vector3f.ZERO));
    this.healthbar.addControl(billboard);
  }

  public void createEntityNode() {
    // Create the central node
    this.node = new Node(this.getType());

    // Create components
    createEntitySpatial();
    setMaterialSpatial();
    createHealthBar();

    // Add them
    this.node.attachChild(this.healthbar);
    this.node.attachChild(this.spatial);
  }

  // ------------------------
  // End region
  // ------------------------

  // ------------------------
  // Region IA
  // ------------------------
  @Override
  public void moveToGoal(float tpf) {
    System.out.println("DEPLACEMENT !");
    // Retrieve the current enemy
    Entity3D enemy = (Entity3D) ownGoal()._2;
    // Make the movement
    System.out.println("tpf : "  + tpf);
    System.out.println("type: " + enemy.getType());

    super.setCurrentPosition(currentPosition().add(new Vector3f(0,0,8f * tpf)));
    
    //this.spatial = this.spatial.rotate(0, (FastMath.DEG_TO_RAD * tpf) * 25f, 0).getLocalRotation();
    //this.spatial.lookAt(enemy.getCurrentPosition(), Vector3f.UNIT_Y); //lookAt(0, (FastMath.DEG_TO_RAD * tpf) * 10, 0);
    this.spatial.move(this.currentPosition());
  }
  // ------------------------
  // End region
  // ------------------------
}
