package src.main.scala.com.graygzou.Cluster

import java.nio.file.{Files, Paths}

import org.apache.spark.{SparkConf, SparkContext}
import src.main.scala.com.graygzou.Creatures.Creature

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class Crawler() {

  val conf = new SparkConf().setAppName("Crawler").setMaster("local[*]")
  val sc = SparkContext.getOrCreate(conf)
  var indexes = new Array[String](5)
  indexes(0) = "http://paizo.com/pathfinderRPG/prd/bestiary/monsterIndex.html"
  indexes(1) = "http://paizo.com/pathfinderRPG/prd/bestiary2/additionalMonsterIndex.html"
  indexes(2) = "http://paizo.com/pathfinderRPG/prd/bestiary3/monsterIndex.html"
  indexes(3) = "http://paizo.com/pathfinderRPG/prd/bestiary4/monsterIndex.html"
  indexes(4) = "http://paizo.com/pathfinderRPG/prd/bestiary5/index.html"
  val creatures = new ArrayBuffer[Creature]
  //val spell = "heal mass"
  //getCreaturesBySpells(spell)
  //getSpellsByCreature("angel solar")



  def getCreaturesBySpells (spell : String) : Unit = {
    crawl()
    val index = sc.objectFile[(String,List[String])]("RDDout/invertedIndex")
    index.foreach( pair =>
      if (pair._1 == spell) {
        println(spell + " : " + pair._2.mkString(", "))
      }
    )
  }

  def getSpellsByCreature (name : String) : ArrayBuffer[String] = {
    crawl()
    var spells = new ArrayBuffer[String]()
    val creaturesRdd = sc.objectFile[(String,ArrayBuffer[String])]("RDDout/index")
    creaturesRdd.collect.foreach( x =>
      if (x._1 == name) {
        spells = x._2
      }
    )
    return spells
  }

  def crawl() : Unit = {
    if (!Files.exists(Paths.get("RDDout/index"))) {
      for (i <- 0 to 4) {
        crawlIndex(i)
        println(creatures.toString())
        //val tab = Array(1, 2, 3, 4, 5)
      }
      val rdd = sc.parallelize(creatures)
      rdd.map(x => (x.name,x.spells)).saveAsObjectFile("RDDout/index")

      if (!Files.exists(Paths.get("RDDout/invertedIndex"))) {
        val invertedIndex = rdd.map(creature => creature.name + "," + creature.spells.mkString(","))
          .flatMap(x => x.split(",").drop(1).map(y => (y, x.split(",")(0))))
          .reduceByKey((y, z) => y + "," + z)
          .map(k => (k._1, k._2.split(",").toList))
        //invertedIndex.foreach(println)
        invertedIndex.saveAsObjectFile("RDDout/invertedIndex")
      }
    }
  }


  def crawlIndex(indexNb : Int) : Unit = {
    val index = Source.fromURL(indexes(indexNb)).mkString
    val indexReduced = index.substring(index.indexOf("""id="monster-index-wrapper""""),index.indexOf("footer")) //réduit le contenu pour ne garder que l'index
    val monsterLinks = Source.fromString(indexReduced).getLines().filter(line => line.contains("a href")) //récupère toutes les créatures de la page
    while (monsterLinks.hasNext) {  //parcours chaque lien
      val link = monsterLinks.next()
      if (link.contains("#")) {
        //val monsterType = link.substring(link.indexOf("a href") + 8, link.indexOf(".html"))
        val monsterName = link.substring(link.indexOf(".html") + 6, link.indexOf("""">""")) //récupère le nom de la créature dans l'url
        var monsterUrl = "http://paizo.com" + link.substring(link.indexOf("a href") + 8, link.indexOf("""">""")).replace(" ","").replace(""""""","")  //reconstruit l'url de la créature car le format n'est pas le même suivant les bestiaires
        if (indexNb == 0) { //le format du premier bestiaire est particulier
          monsterUrl = "http://paizo.com/pathfinderRPG/prd/bestiary/" + link.substring(link.indexOf("a href=") + 8, link.indexOf("""">"""))
        }
        crawlSpell(monsterUrl,monsterName, indexNb)
      }
    }
  }


  def crawlSpell(url : String, monsterName : String, indexNb : Int): Unit = {
    val source = Source.fromURL(url).mkString
    val monsterBody = source.substring(source.indexOf("body-content")+16,source.indexOf("footer")) //réduit le contenu pour ne garder que les informations sur la créature
    val monsters = monsterBody.split("<h1 ")  //chaque créature commmence par la balise h1 ce qui permet de séparer les créature rassemblées dans une catégorie (ex : angel, dragon)
    for (i <- 1 until monsters.length) {
      if (monsterName.equals(monsters(i).split(""""""")(1))) {  //on s'assure que le contenu de la créature correspond au nom provenant de l'index
        val creature = new Creature(monsterName.replace(",","").replace("-"," ")) //création d'un nouvel objet Créature
        if (monsters(i).contains("Special Abilities") && !monsterName.contains("dragon")) //si la créature possède des "Special Abilities", ces dernières ne correspondent pas à des sorts qu'elle possède mais à des descriptions faisant intervenir des spells d'autres créatures.
          monsters(i)=monsters(i).substring(0,monsters(i).indexOf("Special Abilities"))  //On supprime donc cette partie (Ce n'est pas le cas pour les dragons)
        val links = Source.fromString(monsters(i)).getLines().filter(line => line.contains("spells") && line.contains("href")).mkString.split("a href") //Fait la liste de tous les spells de la créature en s'intéressant aux liens contenant "spells"
        for (link <- links) {
          //println(link)
          if (link.contains("spells") && link.contains("html")) { //on s'assure qu'il s'agit bien d'un lien vers un spell car certaines descriptions peuvent contenir les mots "spells" ou "html"
            if (link.contains("fly.html"))  //url non conventionnelle, spells rajouté "manuellement"
              creature.addspell("fly")
            else if (link.contains("fogCloud.html")) //url non conventionnelle, spells rajouté "manuellement"
              creature.addspell("fog cloud")
            else  //découpe le lien pour récupérer le nom du spell et l'ajouter à la liste des spells de la créature
              creature.addspell(link.substring(link.indexOf(".html#")+6,link.indexOf(""">""")).replace(""""""","").replace(" ","").replace("-s-","'s ").replace("-"," "))
          }
        }
        if (!creatures.contains(creature.name) && creature.spells.nonEmpty) {
          println(creature.toString)
          creatures.append(creature)  //on ajoute la créature si elle possède au moins 1 spell
        }
      }
    }
  }

}
