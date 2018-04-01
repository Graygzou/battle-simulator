import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object Crawler extends App{

  var indexes = new Array[String](5)
  //val baseUrl = "http://paizo.com/pathfinderRPG/prd/bestiary"
  indexes(0) = "http://paizo.com/pathfinderRPG/prd/bestiary/monsterIndex.html"
  indexes(1) = "http://paizo.com/pathfinderRPG/prd/bestiary2/additionalMonsterIndex.html"
  indexes(2) = "http://paizo.com/pathfinderRPG/prd/bestiary3/monsterIndex.html"
  indexes(3) = "http://paizo.com/pathfinderRPG/prd/bestiary4/monsterIndex.html"
  indexes(4) = "http://paizo.com/pathfinderRPG/prd/bestiary5/index.html"
  val creatures = new ArrayBuffer[Creature]

  //crawlIndex(4)

  for (i <- 0 to 4) {
    crawlIndex(i)
    println(creatures.toString())

  }

  def crawlIndex(indexNb : Int) : Unit = {
    val index = Source.fromURL(indexes(indexNb)).mkString
    val indexReduced = index.substring(index.indexOf("""id="monster-index-wrapper""""),index.indexOf("footer"))
    val monsterLinks = Source.fromString(indexReduced).getLines().filter(line => line.contains("a href"))
    while (monsterLinks.hasNext) {
      val link = monsterLinks.next()
      if (link.contains("#")) {
        //val monsterType = link.substring(link.indexOf("a href") + 8, link.indexOf(".html"))
        val monsterName = link.substring(link.indexOf(".html") + 6, link.indexOf("""">"""))
        var monsterUrl = "http://paizo.com" + link.substring(link.indexOf("a href") + 8, link.indexOf("""">""")).replace(" ","").replace(""""""","")
        if (indexNb == 0) {
          monsterUrl = "http://paizo.com/pathfinderRPG/prd/bestiary/" + link.substring(link.indexOf("a href=") + 8, link.indexOf("""">"""))
        }
        crawlSpell(monsterUrl,monsterName, indexNb)
      }
    }
  }


  def crawlSpell(url : String, monsterName : String, indexNb : Int): Unit = {
    val source = Source.fromURL(url).mkString
    val monsterBody = source.substring(source.indexOf("body-content")+16,source.indexOf("footer"))
    val monsters = monsterBody.split("<h1 ")
    for (i <- 1 until monsters.length) {
      if (monsterName.equals(monsters(i).split(""""""")(1))) {
        val creature = new Creature(monsterName.replace(",","").replace("-"," "))
        if (monsters(i).contains("Special Abilities") && !monsterName.contains("dragon"))
          monsters(i)=monsters(i).substring(0,monsters(i).indexOf("Special Abilities"))
        val links = Source.fromString(monsters(i)).getLines().filter(line => line.contains("spells") && line.contains("href")).mkString.split("a href")
        for (link <- links) {
          if (link.contains("spells") && link.contains("html")) {
            if (link.contains("fly.html"))
              creature.addspell("fly")
            else
              creature.addspell(link.substring(link.indexOf(".html#")+6,link.indexOf(""">""")).replace(""""""","").replace(" ","").replace("-s-","'s ").replace("-"," "))
          }
        }
        if (!creatures.contains(creature.name) && creature.spells.nonEmpty) {
          println(creature.toString)
          creatures.append(creature)
        }
      }
    }
  }

}
