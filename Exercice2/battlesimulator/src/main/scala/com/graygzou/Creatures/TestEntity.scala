/**
  * @author Grégoire Boiron <gregoire.boiron@gmail.com>
  */

package com.graygzou.Creatures

object TestEntity {
  def main(args: Array[String]){
    //println( "Hello World!" )

    // Test the entity class

    // Create an orc
    val monster1 = new Entity(Array("2","angel solar", "1D", "2D", "3D", "4D", "5D","6D","7D","(0,0,0)"))
    printf("Name of the monster %s\n", monster1.getType)
    printf("Health %f\n", monster1.getHealth)
    printf("Armor %f\n", monster1.getArmor)
    printf("MeleeAttack %f\n", monster1.getMeleeAttack)
    printf("RangeAttack %f\n", monster1.getRangedAttack)
    printf("Regeneration %f\n", monster1.getRegeneration)
    //println("Spells " + monster1.getSpells)
    print("\n")

    // Create a dragon
    val monster2 = new Entity(Array("2","dragon", "100D", "200D", "300D", "400D", "500D","600D","700D","(0,0,0)"))
    printf("Name of the monster %s\n", monster2.getType)
    printf("Health %f\n", monster2.getHealth)
    printf("Armor %f\n", monster2.getArmor)
    printf("MeleeAttack %f\n", monster1.getMeleeAttack)
    printf("RangeAttack %f\n", monster1.getRangedAttack)
    printf("Regeneration %f\n", monster2.getRegeneration)

  }
}