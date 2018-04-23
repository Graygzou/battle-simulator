/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  * @version 0.0.1
  */

package com.graygzou.Cluster

object LauncherCluster {

  /**
    * Main function. Call the launch a fight
    * @param args
    */
  def main(args: Array[String]): Unit = {

    //
    // Init basic variables
    //
    val game = new BattleSimulationCluster("Fight 1", "local[1]")

    //
    // Launch the simulation with the correct files
    //
    // First file : contains data about the entities
    // Second file : contains data about the relationship
    val mainGraph = game.initGame("/FightConfigs/Fight1/entities.txt", "/FightConfigs/Fight1/relations.txt", false)

    //
    // Start the gameloop to play the simulation
    //
    game.gameloop(mainGraph)

    //
    // Stop the simulation
    //
    game.cleanScalaContext()

  }

}
