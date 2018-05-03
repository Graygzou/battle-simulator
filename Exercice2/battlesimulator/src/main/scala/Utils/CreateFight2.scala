package Utils

import java.io.{BufferedWriter, File, FileWriter}


object CreateFight2 {

  def main(args: Array[String]): Unit = {


    val file = new File("entities.txt")
    val bw = new BufferedWriter(new FileWriter(file))

    // 1 Dragon
    bw.write("0,2,dragon1,391,37,21,5,33,25,70,31,0,(400/100/0),40,1,250,0,0\n")

    // 10 Angel Slayers
    for (i <- 1 to 5){
      bw.write(i+",2,angelSlayer"+i+",112,26,7,5,(21/16/11),6,110,(19/14/9),0,("+50+5*(i-1)+"/390/0),40,0,0,40,25\n")
    }
    for (i <- 6 to 10){
      bw.write(i+",2,angelSlayer"+i+",112,26,7,5,(21/16/11),6,110,(19/14/9),0,("+110+5*(i-6)+"/390/0),40,0,0,40,25\n")
    }

    // 200 Greataxe Orc
    var line = 0
    var done = 0
    for (i <- 11 to 210){
      bw.write(i+",2,orc"+i+",42,15,10,5,11,7,10,5,0,("+10*done+"/"+(380-10*line)+"/0),30,0,0,0,0\n")
      if (done == 10){
        done = 0
        line += 1
      } else {
        done += 1
      }
    }

    // 1 Solar
    bw.write("211,1,solar,363,44,18,5,(35/30/25/20),14,110,(31/26/21/16),15,(100/0/0),50,4,150,50,25\n")

    // 2 Planetar
    bw.write("212,1,planetar1,229,32,15,5,(27/22/17),0,0,0,10,(90/0/0),30,4,90,50,25\n")
    bw.write("213,1,planetar2,229,32,15,5,(27/22/17),0,0,0,10,(110/0/0),30,4,90,50,25\n")

    // 2 Movanic Deva
    bw.write("214,1,movanicDeva1,126,24,7,5,(17/12/7),0,0,0,0,(80/0/0),40,4,60,50,25\n")
    bw.write("215,1,movanicDeva1,126,24,7,5,(17/12/7),0,0,0,0,(120/0/0),40,4,60,50,25\n")

    // 5 Astral Deva
    bw.write("216,1,astralDeva1,172,29,14,5,(26/21/16),0,0,0,0,(80/10/0),50,4,100,50,25\n")
    bw.write("217,1,astralDeva2,172,29,14,5,(26/21/16),0,0,0,0,(90/10/0),50,4,100,50,25\n")
    bw.write("218,1,astralDeva3,172,29,14,5,(26/21/16),0,0,0,0,(100/10/0),50,4,100,50,25\n")
    bw.write("219,1,astralDeva4,172,29,14,5,(26/21/16),0,0,0,0,(110/10/0),50,4,100,50,25\n")
    bw.write("220,1,astralDeva5,172,29,14,5,(26/21/16),0,0,0,0,(120/10/0),50,4,100,50,25")

    bw.close()

    val file2 = new File("relations.txt")
    val bw2 = new BufferedWriter(new FileWriter(file2))

    // team 2 attack team 1
    for (i <- 0 to 210){
      for (j <- 211 to 220){
        bw2.write(i+","+j+",1\n")
      }
    }

    // team 2 ally
    for (i <- 1 to 10){
      for (j <- 0 to 210){
        if (i != j) {
          bw2.write(i + "," + j + ",2\n")
        }
      }
    }

    // team 1 attack team 2
    for (i <- 211 to 220){
      for (j <- 0 to 210){
        bw2.write(i+","+j+",1\n")
      }
    }

    // team 1 ally
    for (i <- 211 to 220){
      for (j <- 211 to 220){
        if (i != j) {
          bw2.write(i + "," + j + ",2\n")
        }
      }
    }

    bw2.close()


  }

}
