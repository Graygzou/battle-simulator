import scala.io.Source

object Crawler extends App{

  var bestiaries = new Array[String](5)
  bestiaries(0) = "http://paizo.com/pathfinderRPG/prd/bestiary/monsterIndex.html"
  bestiaries(1) = "http://paizo.com/pathfinderRPG/prd/bestiary2/additionalMonsterIndex.html"
  bestiaries(2) = "http://paizo.com/pathfinderRPG/prd/bestiary3/monsterIndex.html"
  bestiaries(3) = "http://paizo.com/pathfinderRPG/prd/bestiary4/monsterIndex.html"
  bestiaries(4) = "http://paizo.com/pathfinderRPG/prd/bestiary5/index.html"

  crawlIndex(bestiaries(0))

  def crawlIndex(url : String) : Unit = {
    val index = Source.fromURL(url).mkString
    val indexReduced = index.substring(index.indexOf("""id="monster-index-wrapper""""),index.indexOf("footer"))
    val monsterLinks = Source.fromString(indexReduced).getLines().filter(line => line.contains("a href"))
    //crawlSpell("http://paizo.com/pathfinderRPG/prd/bestiary/choker.html#choker","choker")
    while (monsterLinks.hasNext) {
      val link = monsterLinks.next()
      if (link.contains("#")) {
        val monsterType = link.substring(link.indexOf("a href=") + 8, link.indexOf(".html"))
        val monsterName = link.substring(link.indexOf(".html") + 6, link.indexOf("""">"""))
        val url = "http://paizo.com/pathfinderRPG/prd/bestiary/" + link.substring(link.indexOf("a href=") + 8, link.indexOf("""">"""))
        println("Type : " + monsterType + " Name : " + monsterName)
        crawlSpell(url,monsterName)

      }
    }
  }


  def crawlSpell(url : String, monsterName : String): Unit = {
    val source = Source.fromURL(url).mkString
    val monsterBody = source.substring(source.indexOf("body-content")+16,source.indexOf("footer"))
    val monsters = monsterBody.split("<h1 ")
    for (i <- 1 until monsters.length) {
      if (monsterName.equals(monsters(i).split(""""""")(1))) {
        val creature = new Creature(monsterName)
        if (monsters(i).contains("Special Abilities") && !monsterName.contains("dragon")) {
          //println(monsters(i).substring(monsters(i).indexOf("Special Abilities")))
          monsters(i)=monsters(i).substring(0,monsters(i).indexOf("Special Abilities"))
        }
        val links = Source.fromString(monsters(i)).getLines().filter(line => line.contains("spells") && line.contains("href")).mkString.split("<a href=")
        for (link <- links) {
          //println(link)
          if (link.contains("spells") && link.contains("html")) {
            creature.addspell(link.substring(link.indexOf(">")+1,link.indexOf("<")))
          }
        }

        println(creature.toString)
      }
    }
  }

}
