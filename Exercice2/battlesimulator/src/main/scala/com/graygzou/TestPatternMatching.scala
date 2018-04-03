package com.graygzou

  abstract class Tree
  case class Sum(l: Tree, r: Tree) extends Tree
  case class Var(n: String) extends Tree
  case class Const(v: Int) extends Tree
/*
  object TestPatternMatching {
    def main(args: Array[String]) {
      val exp: Tree = Sum(Sum(Var("x"), Var("x")), Sum(Const(7), Var("y")))
      val env: Environment = {
        case "x" => 5
        case "y" => 7
      }
      /*
      println("Expression: " + exp)
      println("Evaluation with x=5, y=7: " + eval(exp, env))
      println("Derivative relative to x:\n " + derive(exp, "x"))
      println("Derivative relative to y:\n " + derive(exp, "y"))*/
    }
  }*/