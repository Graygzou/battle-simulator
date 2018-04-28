package com.graygzou

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object devoir {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("ex1").setMaster("local[1]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")

    val entities = getClass.getResource("/devoir_views.txt")
    val viewsfile: RDD[String] = sc.textFile(entities.getPath)
    val newrdd: RDD[Array[String]] = viewsfile.map(_.split(' '))

    val req2 = newrdd.map( line => ( line(0).replace("\\","-") + "," + line(2), line(1) ) )

    val calcul2 = req2.reduceByKey((a,b) =>
      (
        if(a.contains(b)) a
        else a+" "+b
      )
    )

    calcul2.coalesce(1)

    //calcul2.saveAsTextFile("/outputExo1.txt")

    //calcul2.collect().foreach(x => println(x))

    /// EXO 3

    val rdd1 = sc.makeRDD(Array(("A",1),("B","2"),("C","W")))
    val rdd2 : RDD[(String, Any)] = sc.makeRDD(Array(("A","a"),("C","c"),("D","d")))

    val rdd4 = rdd1.join(rdd2).collect()
    val rdd3 = rdd1.union(rdd2).reduceByKey((a, b) => if(a.toString < b.toString) a else b ).reduce((a, b) => if(a._1 < b._1) a else b )._2

    println(rdd3)

  }

}
