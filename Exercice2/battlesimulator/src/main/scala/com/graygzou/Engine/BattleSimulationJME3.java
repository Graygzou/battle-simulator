package com.graygzou.Engine;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/** Sample 1 - how to get started with the most simple JME 3 application.
 * Display a blue 3D cube and view from all sides by
 * moving the mouse and pressing the WASD keys. */
public class BattleSimulationJME3 extends SimpleApplication {

    public static void main(String[] args){
        BattleSimulationJME3 app = new BattleSimulationJME3();
        app.start(); // start the game
    }

    @Override
    public void simpleInitApp() {
        // You initialize game objects:
        //      create or load objects and position them.
        //      make objects appear in the scene by attaching them to the rootNode.
        //
        // You initialize variables:
        //      create variables to track the game state.
        //      set variables to their start values.
        //
        //You initialize keys and mouse actions:
        //      the following input bindings are pre-configured:
        //      W A S D keys – Move around in the scene
        //      Mouse movement and arrow keys – Turn the camera
        //      Esc key – Quit the game
        //      Define your own additional keys and mouse click actions.
    }

    /* Use the main event loop to trigger repeating actions. */
    @Override
    public void simpleUpdate(float tpf) {
        // tpf : time par frame

    }
}