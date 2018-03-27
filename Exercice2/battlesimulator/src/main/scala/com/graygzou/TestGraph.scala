package com.graygzou

import org.apache.spark.{SparkConf, SparkContext}

object quickTest extends App
{
  import scala.math.random
  val conf = new SparkConf().setAppName("petit test rapide").setMaster("local[*]")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")
  def test(nbrDecimales : Int = 1000) : Unit = {
    val slices = nbrDecimales
    val n = math.min(100000L * slices, Int.MaxValue).toInt
    val count = sc.parallelize(1 until n, slices).map { i =>
      val x = random * 2 - 1
      val y = random * 2 - 1
      if (x*x + y*y <= 1) 1 else 0
    }.reduce(_ + _)
    println("Pi is roughly " + 4.0 * count / (n - 1))
  }
  test(20)
}
