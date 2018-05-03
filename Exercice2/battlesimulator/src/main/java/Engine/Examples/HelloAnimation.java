package Engine.Examples;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/** Sample 7 - how to load an OgreXML model and play an animation,
 * using channels, a controller, and an AnimEventListener. */
public class HelloAnimation extends SimpleApplication
        implements AnimEventListener {
    private AnimChannel channel;
    private AnimControl control;
    Node player;
    public static void main(String[] args) {
        HelloAnimation app = new HelloAnimation();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.LightGray);
        initKeys();
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -1f, -1).normalizeLocal());
        rootNode.addLight(dl);
        player = (Node) assetManager.loadModel("/Models/Oto/Oto.mesh.xml");
        /*
        Material owmMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        owmMaterial.setTexture("DiffuseMap",
                assetManager.loadTexture("/Models/Oto/Oto.jpg"));
        //owmMaterial.setBoolean("UseMaterialColors",true);
        //owmMaterial.setColor("Diffuse", color);
        player.setMaterial(owmMaterial);*/
        Material sphereMat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        sphereMat.setColor("Diffuse",ColorRGBA.Blue);

        sphereMat.setColor("Specular",ColorRGBA.Red);
        sphereMat.setFloat("Shininess", 64f);  // [0,128]
        player.setMaterial(sphereMat);

        player.setLocalScale(0.5f);
        rootNode.attachChild(player);
        control = player.getControl(AnimControl.class);
        control.addListener(this);
        channel = control.createChannel();
        channel.setAnim("stand");
    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if (animName.equals("Walk")) {
            /*
            channel.setAnim("stand", 0.50f);
            channel.setLoopMode(LoopMode.Loop);
            channel.setSpeed(1f);
            */
        }
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        // unused
    }

    /** Custom Keybinding: Map named actions to inputs. */
    private void initKeys() {
        inputManager.addMapping("Walk", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "Walk");

        inputManager.addMapping("Push", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addListener(actionListener, "Push");

        inputManager.addMapping("Pull", new KeyTrigger(KeyInput.KEY_O));
        inputManager.addListener(actionListener, "Pull");

        inputManager.addMapping("Dodge", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addListener(actionListener, "Dodge");
    }
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Walk") && !keyPressed) {
                if (!channel.getAnimationName().equals("Walk")) {
                    channel.setAnim("Walk", 0.50f);
                    channel.setLoopMode(LoopMode.Loop);
                }
            } else if (name.equals("Push") && !keyPressed) {
                if (!channel.getAnimationName().equals("push")) {
                    channel.setAnim("push", 0.50f);
                    channel.setLoopMode(LoopMode.Loop);
                }
            } else if (name.equals("Pull") && !keyPressed) {
                if (!channel.getAnimationName().equals("pull")) {
                    channel.setAnim("pull", 0.50f);
                    channel.setLoopMode(LoopMode.Loop);
                }
            } else if (name.equals("Dodge") && !keyPressed) {
                if (!channel.getAnimationName().equals("Dodge")) {
                    channel.setAnim("Dodge", 0.50f);
                    channel.setLoopMode(LoopMode.Loop);
                }
            }
        }
    };
}