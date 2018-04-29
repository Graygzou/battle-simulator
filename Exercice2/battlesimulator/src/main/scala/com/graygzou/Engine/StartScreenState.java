package com.graygzou.Engine;

import com.graygzou.Cluster.BattleSimulationCluster;
import com.graygzou.Creatures.Entity3D;
import com.graygzou.TeamEntities;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.ChaseCamera;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.chatcontrol.ChatEntryModelClass;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

import java.util.List;

public class StartScreenState extends BaseAppState implements ScreenController {

    // Parameters from the bind
    private Nifty nifty;
    private Screen screen;
    // End

    private boolean gameFinished = false;
    private float gameFinishCountDown = 5f;

    private Node localRootNode = new Node("Start Screen RootNode");
    private Node localGuiNode = new Node("Start Screen GuiNode");
    private final ColorRGBA backgroundColor = ColorRGBA.Gray;
    private Material mat_default;

    private Application app;

    // OTHERS
    private BattleSimulationCluster game;

    protected Geometry player;

    private Node[] gameEntities;
    private int currentEntityIdFocused;

    private ChaseCamera camera;

    public StartScreenState(Engine3D engine3D) {
        game = new BattleSimulationCluster("Fight1", "local[1]");
    }


    /**
     * Fill the listbox with items. In this case with Strings.
     */
    public void fillMyListBoxTeams() {

        // TEAM 1
        ListBox<ChatEntryModelClass> listBox1 = (ListBox<ChatEntryModelClass>) this.screen.findNiftyControl("listBoxTeam1", ListBox.class);
        NiftyImage newImage = nifty.getRenderEngine().createImage(this.screen, "/Interface/teapot.png",false);
        // false means don't linear filter the image, true would apply linear filtering
        listBox1.addItem(new ChatEntryModelClass("orc1", newImage));
        listBox1.addItem(new ChatEntryModelClass("orc2", newImage));

        // TEAM 2
        ListBox<ChatEntryModelClass> listBox2 = (ListBox<ChatEntryModelClass>) screen.findNiftyControl("listBoxTeam2", ListBox.class);
        // false means don't linear filter the image, true would apply linear filtering
        listBox2.addItem(new ChatEntryModelClass("Angel Random", newImage));
        listBox2.addItem(new ChatEntryModelClass("Angel Slayer d'orcs", newImage));
    }

    @Override
    protected void initialize(Application app) {
        this.app = app;

        //It is technically safe to do all initialization and cleanup in the
        //onEnable()/onDisable() methods. Choosing to use initialize() and
        //cleanup() for this is a matter of performance specifics for the
        //implementor.
        //TODO: initialize your AppState, e.g. attach spatials to rootNode
        ((SimpleApplication) app).getRootNode().attachChild(localRootNode);
        ((SimpleApplication) app).getGuiNode().attachChild(localGuiNode);
        ((SimpleApplication) app).getViewPort().setBackgroundColor(backgroundColor);


        /** init the screen */

        mat_default = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");

        // Create the regular graph
        game.initGame("/FightConfigs/Fight1/entities.txt", "/FightConfigs/Fight1/relations.txt", true);
        //game = new BattleSimulationCluster("Fight 1","local[*]");
        gameEntities = new Node[game.screenEntities().length];

        int actualNbEntity = 0;
        // You initialize game objects:
        for(TeamEntities currentTeam : game.screenTeams()) {
            System.out.println(currentTeam.countAliveEntity());
            int i = 0;
            while(i < currentTeam.countAliveEntity()) {
                // Create the node
                gameEntities[actualNbEntity] = new Node(currentTeam.getEntities()[i].getType());
                Entity3D currentEntity = (Entity3D) currentTeam.getEntities()[i];

                // Create the model of the entity
                currentEntity.setModelPath("Models/Teapot/Teapot.obj");
                Spatial current_spatial = ((SimpleApplication) app).getAssetManager().loadModel("Models/Teapot/Teapot.obj");
                // Set the life of the entity
                current_spatial.setUserData("health", currentEntity.getHealth());

                // Place the entity in the world
                current_spatial.setLocalTranslation(currentEntity.getCurrentPosition());
                current_spatial.center().move(currentEntity.getCurrentPosition().add(new Vector3f(0, 1f, 0)));

                // Set the scale and the rotation of the entity
                current_spatial.setLocalScale(1, 1, 1); // TODO better

                // Material setup
                Material teamColor = mat_default.clone();
                teamColor.setColor("Color", currentTeam.getTeamColor());
                current_spatial.setMaterial(teamColor);

                // Add an health bar
                BillboardControl billboard = new BillboardControl();
                Geometry healthbar = new Geometry("healthbar", new Quad(3f, 0.2f));
                Material mathb = mat_default.clone();
                mathb.setColor("Color", ColorRGBA.Red);
                healthbar.setMaterial(mathb);
                gameEntities[actualNbEntity].attachChild(healthbar);
                healthbar.center();
                healthbar.move(current_spatial.getLocalTranslation().add(new Vector3f(0, 0, 0)));
                healthbar.addControl(billboard);


                // Add the entity representation
                gameEntities[actualNbEntity].attachChild(current_spatial);

                // Attach the current entity to the rootNode
                ((SimpleApplication) app).getRootNode().attachChild(gameEntities[actualNbEntity]);

                i++;
                actualNbEntity++;
            }
        }

        fillMyListBoxTeams();

        // initialize camera and variables
        ((SimpleApplication) app).getFlyByCamera().setEnabled(false);
        currentEntityIdFocused = 0;
        camera = new ChaseCamera(((SimpleApplication) app).getCamera(), gameEntities[currentEntityIdFocused].getChild(1), ((SimpleApplication) app).getInputManager());
        // Smooth camera motion
        camera.setSmoothMotion(true);

    }

    public void nextEntityCameraFocus() {
        currentEntityIdFocused = (currentEntityIdFocused + 1) % (gameEntities.length - 1);
        camera.setSpatial(gameEntities[currentEntityIdFocused].getChild(1));

        // Smooth camera motion
        camera.setSmoothMotion(true);
    }

    public void previousEntityCameraFocus() {
        currentEntityIdFocused = (currentEntityIdFocused - 1) % (gameEntities.length - 1);
        if (currentEntityIdFocused < 0) currentEntityIdFocused += gameEntities.length - 1;
        camera.setSpatial(gameEntities[currentEntityIdFocused].getChild(1));

        // Smooth camera motion
        camera.setSmoothMotion(true);
    }

    @Override
    protected void cleanup(Application app) {
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
        ((SimpleApplication) app).getRootNode().detachChild(localRootNode);
        ((SimpleApplication) app).getGuiNode().detachChild(localGuiNode);
    }

    //onEnable()/onDisable() can be used for managing things that should
    //only exist while the state is enabled. Prime examples would be scene
    //graph attachment or input listener attachment.
    @Override
    protected void onEnable() {
        //Called when the state is fully enabled, ie: is attached and
        //isEnabled() is true or when the setEnabled() status changes after the
        //state is attached.
    }

    @Override
    protected void onDisable() {
        //Called when the state was previously enabled but is now disabled
        //either because setEnabled(false) was called or the state is being
        //cleaned up.
    }

    @Override
    public void update(float tpf) {
        //TODO: implement behavior during runtime

        // check if game is over
        if (gameFinished) {
            if (gameFinishCountDown <= 0) {
                cleanup();
                //this.stop();
                return;
            }
            gameFinishCountDown -= tpf;
            return;
        }

        //checkGameState(enemyHealth, playerHealth);

        //updateLasers(tpf);

        //updateEnemy(tpf);

        updateHealthBars();

    }

    private void updateHealthBars() {
        // update health bars of all entities
        for(Node currentNode : gameEntities) {
            // TODO : Retrieve the life of the entity
            //((Quad) ((Geometry) currentNode.getChild("healthbar")).getMesh()).updateGeometry(enemyHealth / 100 * 4, 0.2f);
        }
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        System.out.println("bind(" + screen.getScreenId() + ")");
        this.nifty = nifty;
        this.screen = screen;
    }

    @Override
    public void onStartScreen() {
        System.out.println("onStartScreen");
    }

    @Override
    public void onEndScreen() {
        System.out.println("onEndScreen");
    }

}