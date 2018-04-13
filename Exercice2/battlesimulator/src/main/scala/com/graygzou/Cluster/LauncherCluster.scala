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

    val game = new BattleSimulationCluster()

    // Init basic variables
    game.initScalaContext("Fight 1","local[*]")


    // Launch the simulation with the correct files
    // First file : contains data about the entities
    // Second file : contains data about the relationship
    game.launchGame("/FightConfigs/Fight1/entities.txt", "/FightConfigs/Fight1/relations.txt")


    // Stop the simulation
    game.cleanScalaContext

  }

}
