package com.graygzou.Engine;

import com.graygzou.Cluster.BattleSimulationCluster;
import com.jme3.app.SimpleApplication;

/** This class call the JME3 engine to render the fight in 3D
 * It has to be in Java since the engine need specific method
 * */
public class BattleSimulation3D extends SimpleApplication {

    private BattleSimulationCluster game;

    public static void main(String[] args){
        BattleSimulation3DOld app = new BattleSimulation3DOld();
        app.start(); // start the game
    }

    /**
     * // You initialize game objects:
     *         //      create or load objects and position them.
     *         //      make objects appear in the scene by attaching them to the rootNode.
     *         //
     *         // You initialize variables:
     *         //      create variables to track the game state.
     *         //      set variables to their start values.
     *         //
     *         //You initialize keys and mouse actions:
     *         //      the following input bindings are pre-configured:
     *         //      W A S D keys – Move around in the scene
     *         //      Mouse movement and arrow keys – Turn the camera
     *         //      Esc key – Quit the game
     *         //      Define your own additional keys and mouse click actions.
     */
    @Override
    public void simpleInitApp() {
        // Create the regular graph
        //game = new BattleSimulationCluster("Fight 1","local[*]");

        // You initialize game objects:

        // You initialize variables:

        //You initialize keys and mouse actions:

    }

    /* Use the main event loop to trigger repeating actions. */
    @Override
    public void simpleUpdate(float tpf) {
        // tpf : time par frame




    }
}