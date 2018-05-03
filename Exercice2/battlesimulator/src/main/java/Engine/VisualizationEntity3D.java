package Engine;

import Creatures.GraphEntity;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static Engine.GameScreenState.app;
import static Engine.GameScreenState.mat_default;

public class VisualizationEntity3D extends GraphEntity implements Serializable {

    private static final long serialVersionUID = 78332556621456621L;
    private final float MAXHEALTH = 3f;

    // 3D components
    private Node node;
    private Spatial spatial;
    private Geometry healthbar;

    // Others 3D stuffs
    private Material owmMaterial;
    BitmapText ownLabel;

    private Quaternion rot = new Quaternion();
    private boolean updated = false;

    public VisualizationEntity3D(String[] args) {
        super(args);
        owmMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        // Regroup all the entity
        createEntityNode();
    }

    public VisualizationEntity3D(GraphEntity entity) {
        this(entity.getInitialAttributes().split(","));
        if(entity.getTeam().id() == 0) {
            this.setTeamColor(ColorRGBA.Red);
        } else {
            this.setTeamColor(ColorRGBA.Blue);
        }

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
        /*owmMaterial.setTexture("DiffuseMap",
                app.getAssetManager().loadTexture(super.getTexturePath()));*/
        Texture currentTexture = app.getAssetManager().loadTexture(super.getTexturePath());
        owmMaterial.setTexture("ColorMap", currentTexture);
        //owmMaterial.setBoolean("UseMaterialColors",true);
        //owmMaterial.setColor("Diffuse", color);
        this.spatial.setMaterial(owmMaterial);
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
        super.setModelPath(ois.readUTF());
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
        oos.writeUTF(super.getModelPath());
    }

    /*
     * Vector3f extent = ((BoundingBox) spatial.getWorldBound()).getExtent
     * float x = ( (BoundingBox)spatial.getWorldBound()).getXExtent();
     * float y = ( (BoundingBox)spatial.getWorldBound()).getYExtent();
     * float z = ( (BoundingBox)spatial.getWorldBound()).getZExtent();
     */

    // ------------------------
    // region 3D component
    // ------------------------
    private void createEntitySpatial() {
        this.spatial = ((SimpleApplication) app).getAssetManager().loadModel(this.getModelPath());
        // Set the life of the entity. Useless here
        this.spatial.setUserData("health", this.getHealth());

        // Place the entity in the world
        this.spatial.setLocalTranslation(this.getCurrentPosition());
        System.out.println("Current position: " + this.getCurrentPosition());
        this.spatial.center().move(this.getCurrentPosition().add(new Vector3f(0, 1f, 0)));

        // Set the scale and the rotation of the entity
        System.out.println(this.getScale());
        this.spatial.setLocalScale(this.getScale());

    }

    private void createHealthBar() {
        BillboardControl billboard = new BillboardControl();
        this.healthbar = new Geometry("healthbar", new Quad(MAXHEALTH, 0.2f));
        Material mathb = mat_default.clone();
        mathb.setColor("Color", ColorRGBA.Red);
        this.healthbar.setMaterial(mathb);

        //Geometry text = new Geometry("text", new )
        // Center health bar
        /*
        Vector3f extent = ((BoundingBox) this.spatial.getWorldBound()).getExtent(new Vector3f());
        float x = ( (BoundingBox)spatial.getWorldBound()).getXExtent();
        float y = ( (BoundingBox)spatial.getWorldBound()).getYExtent();
        float z = ( (BoundingBox)spatial.getWorldBound()).getZExtent();*/
        this.healthbar.setLocalTranslation(this.spatial.getLocalTranslation().add(0f, 2.1f, 0f));
        //this.healthbar.move(this.spatial.getLocalTranslation().add(Vector3f.ZERO));
        this.healthbar.addControl(billboard);
    }

    private void createLabel() {
        BillboardControl billboard = new BillboardControl();
        ((Engine3D) app).setGuiFont("Interface/Fonts/Default.fnt");
        this.ownLabel = new BitmapText(((Engine3D) app).getGuiFont(), false);
        this.ownLabel.setSize(0.5f);
        this.ownLabel.setText(super.getType()); // crosshairs
        this.ownLabel.setQueueBucket(RenderQueue.Bucket.Transparent);
        this.ownLabel.setColor(new ColorRGBA(255f,255f,255f,1f));

        this.ownLabel.center().move(this.spatial.getLocalTranslation());
        this.ownLabel.setLocalTranslation(this.ownLabel.getLocalTranslation().add(0f, 2.05f, 0));
        this.ownLabel.addControl(billboard);
    }

    public void createEntityNode() {
        // Create the central node
        this.node = new Node(this.getType());

        // Create components
        createEntitySpatial();
        setMaterialSpatial(this.getTeamColor());
        createHealthBar();
        createLabel();

        // Add them
        this.node.attachChild(this.healthbar);
        this.node.attachChild(this.spatial);
        this.node.attachChild(this.ownLabel);
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
        updateGUI(baseEntity);
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

    private void updateGUI(GraphEntity baseEntity) {
        // Update the health bar
        float size = baseEntity.getHealth() / baseEntity.getMaxHealth() * MAXHEALTH;
        ((Quad) ((Geometry) this.healthbar).getMesh()).updateGeometry(size, 0.2f);
    }

    public void lookAtEnemy(float tpf, GraphEntity enemy) {
        Vector3f dir = enemy.getCurrentPosition().subtract(this.currentPosition()).normalizeLocal();
        // Create the rotation
        rot = this.spatial.getLocalRotation();
        rot.lookAt(new Vector3f(dir.x, dir.y, dir.z).multLocal(1f, 0f, 1f), Vector3f.UNIT_Y);
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
        this.spatial.setLocalScale(ownScale().x, 0, ownScale().z);
        this.setMaterialSpatial(ColorRGBA.Red);

        // Set health to zero
        ((Quad) ((Geometry) this.healthbar).getMesh()).updateGeometry(0.0f, 0.2f);
    }
    // ------------------------
    // End region steering behavior
    // ------------------------
}
