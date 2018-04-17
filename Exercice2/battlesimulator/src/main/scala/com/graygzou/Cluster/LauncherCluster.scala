/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  * @version 0.0.1
  */

package com.graygzou.Cluster

import org.apache.spark.{SparkConf, SparkContext}

object LauncherCluster {

  /**
    * Main function. Call the launch a fight
    * @param args
    */
  def main(args: Array[String]): Unit = {

    // Init Scala Context
    // Create the SparkConf and the SparkContext with the correct value
    val conf = new SparkConf().setAppName("Fight 1").setMaster("local[1]")
    val sc = new SparkContext(conf)

    // Init basic variables
    val game = new BattleSimulationCluster(conf, sc)

    // Launch the simulation with the correct files
    // First file : contains data about the entities
    // Second file : contains data about the relationship
    game.launchGame("/FightConfigs/Fight1/entities.txt", "/FightConfigs/Fight1/relations.txt")


    // Stop the simulation
    game.cleanScalaContext(sc)

  }

}
