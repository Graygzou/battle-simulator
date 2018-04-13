/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  * @version 0.0.1
  */

package com.graygzou.Engine

object Launcher3D {

  /**
    * Main function. Call the launch a fight
    * @param args
    */
  def main(args: Array[String]): Unit = {

    val game = new BattleSimulation3D()

    game.initScalaContext("","")

    game.cleanScalaContext

  }

}
