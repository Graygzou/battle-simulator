/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  * @version 0.0.1
  */

package Utils

import com.jme3.math.Vector3f
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
      triplet => "printGraphUtils : " + triplet.srcAttr + " = " + triplet.attr + " = " + triplet.dstAttr
    ).collect.foreach(println(_))
  }

  // Others stuffs
  def makeVector3f(str: String) : Vector3f = {
    var position = new Vector3f(0,0,0)
    val coordinates : Array[Float] = GameUtils.makeArray(str)
    if(coordinates.length == 3) {
      position = new Vector3f(coordinates(0), coordinates(1), coordinates(2))
    }
    position
  }

  def makeArray(str: String): Array[Float] = {
    val arrayString : Array[String] = str.replace("(", "").replace(")","").split("/")
    var arrayDouble : Array[Float] = new Array[Float](arrayString.length)
    for (i <- arrayString.indices){
      arrayDouble(i) = arrayString(i).toFloat
    }
    arrayDouble
  }

  def makeString(array: Array[Float]): String = {
    var res = "("
    for(indice <- 0 to array.length-1) {
      res += array.apply(indice)
      if(indice != array.length-1) {
        res += "/"
      }
    }
    res += ")"
    res
  }

  def makeString(position: Vector3f): String = {
    "(" + position.x + "/" + position.y + "/" + position.z + ")"
  }

}
