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

    // Init basic variables
    val game = new BattleSimulationCluster("Fight 1", "local[1]")

    /*
    Launch the simulation with the correct files
    First file : contains data about the entities
    Second file : contains data about the relationship
    */
    game.setupGame("/FightConfigs/Fight1/entities.txt", "/FightConfigs/Fight1/relations.txt", false)

    /*
    Start the gameloop to play the simulation
    While their is still two teams in competition
     */
    while (game.isFightNotFinished()) {

      println("Turn n°" + game.getCurrentTurnNumber())

      game.playOneTurn()

      game.printCurrentGraph()

      // Wait one second
      Thread.sleep(1000)
    }

    println("The fight is done in " + game.getCurrentTurnNumber() + " turn(s).")

    // Print the result
    game.getFightResult() match {
      case -2 => println("You are all dead !! HAHAHAHA")
      case -1 => println("It's a tie !")
      case result => println("The winning team is : " + Team(result))
    }
    // Clean the simulation
    game.cleanScalaContext()
  }
}
