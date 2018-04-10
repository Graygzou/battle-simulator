/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  */

package com.graygzou.Creatures

object TestEntity {
  def main(args: Array[String]){
    //println( "Hello World!" )

    // Test the entity class

    // Create an orc
    val monster1 = new Entity(Array("2","Orc", "1D", "2D", "3D", "4D", "5D"))
    printf("Name of the monster %s\n", monster1.getType)
    printf("Health %f\n", monster1.getHealth)
    printf("Armor %f\n", monster1.getArmor)
    printf("MeleeAttack %f\n", monster1.getMeleeAttack)
    printf("RangeAttack %f\n", monster1.getRangeAttack)
    printf("Regeneration %f\n", monster1.getRegeneration)
    print("\n")

    // Create a dragon
    val monster2 = new Entity(Array("2","Dragon", "100D", "200D", "300D", "400D", "500D"))
    printf("Name of the monster %s\n", monster2.getType)
    printf("Health %f\n", monster2.getHealth)
    printf("Armor %f\n", monster2.getArmor)
    printf("MeleeAttack %f\n", monster1.getMeleeAttack)
    printf("RangeAttack %f\n", monster1.getRangeAttack)
    printf("Regeneration %f\n", monster2.getRegeneration)

  }
}