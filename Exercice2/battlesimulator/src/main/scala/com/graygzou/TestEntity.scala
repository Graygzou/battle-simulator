package com.graygzou

object TestEntity {
  def main(args: Array[String]){
    //println( "Hello World!" )

    // Test the entity class

    // Create an orc
    val monster1 = new Entity("Orc", 1D, 2D, 3D, 4D)
    printf("Name of the monster %s\n", monster1.getName)
    printf("Health %f\n", monster1.getHealth)
    printf("Armor %f\n", monster1.getArmor)
    printf("Strength %f\n", monster1.getStrength)
    printf("Regeneration %f\n", monster1.getRegeneration)
    print("\n")

    // Create a dragon
    val monster2 = new Entity("Dragon", 100D, 200D, 300D, 400D)
    printf("Name of the monster %s\n", monster2.getName)
    printf("Health %f\n", monster2.getHealth)
    printf("Armor %f\n", monster2.getArmor)
    printf("Strength %f\n", monster2.getStrength)
    printf("Regeneration %f\n", monster2.getRegeneration)

  }
}