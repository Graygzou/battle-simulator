package com.graygzou.Creatures;

import com.graygzou.Engine.GameScreenState;
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

import static com.graygzou.Engine.GameScreenState.app;
import static com.graygzou.Engine.GameScreenState.mat_default;

public class VisualizationEntity3D extends GraphEntity implements Serializable {

  private static final long serialVersionUID = 78332556621456621L;
  private final float MAXHEALTH = 3f;

  private String modelPath;

  // 3D components
  private Node node;
  private Spatial spatial;
  private Geometry healthbar;

  private Quaternion rot = new Quaternion();
  private boolean updated = false;

  public VisualizationEntity3D(String[] args) {
    super(args);
    // Special setup
    this.modelPath = args[args.length-1];

    // Regroup all the entity
    createEntityNode();
  }

  public VisualizationEntity3D(GraphEntity entity) {
      this(entity.getInitialAttributes().split(","));
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
    setMaterialSpatial(this.getTeamColor());
  }

  public void setMaterialSpatial(ColorRGBA color) {
    // Material setup
    Material teamColor = mat_default.clone();
    teamColor.setColor("Color", color);
    this.spatial.setMaterial(teamColor);
  }

  public Spatial getSpatial() {
    return this.spatial;
  }

  public Node getNode() {
    return this.node;
  }

  public boolean hasBeenUpdated() {
      return updated;
  }

  public void resetUpdateState() {
      updated = false;
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
    // Set the life of the entity. Useless here
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
    this.healthbar = new Geometry("healthbar", new Quad(MAXHEALTH-2, 0.2f));
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
    setMaterialSpatial(this.getTeamColor());
    createHealthBar();

    // Add them
    this.node.attachChild(this.healthbar);
    this.node.attachChild(this.spatial);
  }

  // ------------------------
  // End region
  // ------------------------

  // ------------------------
  // Region Steering Behavior
  // ------------------------

  public void update(float tpf, GraphEntity baseEntity) {
      updated = true;
      // Steering Behavior
      updateHealthBar(baseEntity);
      // Health update
      moveToGoal(tpf, baseEntity);
  }

  public void moveToGoal(float tpf, GraphEntity baseEntity) {
      if(!baseEntity.getCurrentPosition().equals(this.currentPosition())) {
          // Retrieve the current enemy
          GraphEntity enemy = baseEntity.getGoal()._2;

          // Look at the target
          lookAtEnemy(tpf, enemy);
          // Move
          moveToward(tpf, baseEntity);
      }
  }

  private void updateHealthBar(GraphEntity baseEntity) {
      float size = baseEntity.getHealth() / baseEntity.getMaxHealth() * MAXHEALTH;
      ((Quad) ((Geometry) this.healthbar).getMesh()).updateGeometry(size, 0.2f);
  }

  public void lookAtEnemy(float tpf, GraphEntity enemy) {
      Vector3f dir = this.currentPosition().subtract(enemy.getCurrentPosition()).normalizeLocal();
      // Create the rotation
      rot = this.spatial.getLocalRotation();
      rot.lookAt(new Vector3f(dir.z, dir.y, dir.x).multLocal(1f, 0f, 1f), Vector3f.UNIT_Y);
      this.spatial.setLocalRotation(this.spatial.getLocalRotation());
  }

  public void moveToward(float tpf, GraphEntity baseEntity) {
      float move = tpf * 2f; //speed

      float remainingDist = this.spatial.getLocalTranslation().distance(baseEntity.getCurrentPosition()); //distance between 2 vectors
      if (remainingDist != 0) this.spatial.setLocalTranslation(FastMath.interpolateLinear(move / remainingDist, this.spatial.getLocalTranslation(), baseEntity.getCurrentPosition()));
      // move the healthbar too
      this.healthbar.center();
      this.healthbar.move(this.spatial.getLocalTranslation().add(Vector3f.ZERO));
  }

    public void kill() {
      this.spatial.setLocalScale(1, 0, 1);
      this.setMaterialSpatial(ColorRGBA.Red);

      // Set health to zero
        ((Quad) ((Geometry) this.healthbar).getMesh()).updateGeometry(0.0f, 0.2f);
    }
    // ------------------------
  // End region steering behavior
  // ------------------------
}
