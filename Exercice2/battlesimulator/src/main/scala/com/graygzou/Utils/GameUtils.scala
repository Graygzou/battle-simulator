/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  * @version 0.0.1
  */

package com.graygzou.Utils

import org.apache.spark.graphx.Graph

import scala.util.Random

object GameUtils {

  /**
    * Roll a dice with the maximum face number is n.
    * @param n the maximum number we can get.
    * @return an Int which is between 1 and n
    */
  def rollDice(n: Int) : Int = {
    Random.nextInt(n) + 1
  }

  def printGraph(mainGraph: Graph[_,_]): Unit = {
    mainGraph.triplets.map(
      triplet => triplet.srcAttr + " = " + triplet.attr + " = " + triplet.dstAttr
    ).collect.foreach(println(_))
  }

}
