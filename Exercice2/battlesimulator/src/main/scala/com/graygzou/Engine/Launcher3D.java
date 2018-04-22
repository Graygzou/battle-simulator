/**
 * @author : Grégoire Boiron <gregoire.boiron@gmail.com>
 * @version 0.0.1
 */

package com.graygzou.Engine;

/** This class call the JME3 engine to render the fight in 3D
 * It has to be in Java since the engine need specific method
 * */
public class Launcher3D {

    public static void main(String[] args){
        Engine3D game = new Engine3D();
        game.start(); // start the game
    }

}