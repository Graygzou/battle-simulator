/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  * @version 0.0.1
  */

package com.graygzou.Cluster

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object GameUtils {

  /**
    * Roll a dice with the maximum face number is n.
    * @param n the maximum number we can get.
    * @return an Int which is between 1 and n
    */
  def rollDice(n: Int) : Int = {
    return Random.nextInt(n) + 1
  }




}
