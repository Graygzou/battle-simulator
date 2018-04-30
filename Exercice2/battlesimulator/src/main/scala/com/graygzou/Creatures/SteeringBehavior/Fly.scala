package com.graygzou.Creatures.SteeringBehavior

// Check : http://www.dandwiki.com/wiki/SRD:Fly

class Fly(mode : FlyQuality.Value) extends Serializable {

  var maneuverability: FlyQuality.Value = mode

  var minimumForwardSpeed = 0.5
  var hover = false
  var moveBackward = false
  var reverse: (Boolean, Int) = (false,0)
  var turn: (Int, Int) = (45,10)
  var turnInPlace: (Int, Int) = (0,0)
  var maximumTurn = 45
  var upAngle = 45
  var upSpeed = 0.5
  var downAngle = 45
  var downSpeed = 2
  var betweenDownUp = 20

  mode match {
    case FlyQuality.None =>
       minimumForwardSpeed = 0.5
       hover = false
       moveBackward = false
       reverse = (false,0)
       turn = (45,10)
       turnInPlace = (0,0)
       maximumTurn = 45
       upAngle = 45
       upSpeed = 0.5
       downAngle = 45
       downSpeed = 2
       betweenDownUp = 20

    case FlyQuality.Clumsy =>
       minimumForwardSpeed = 0.5
       hover = false
       moveBackward = false
       reverse = (false,0)
       turn = (45,10)
       turnInPlace = (0,0)
       maximumTurn = 45
       upAngle = 45
       upSpeed = 0.5
       downAngle = 45
       downSpeed = 2
       betweenDownUp = 20

    case FlyQuality.Poor =>
       minimumForwardSpeed = 0.5
       hover = false
       moveBackward = false
       reverse = (false,0)
       turn = (45,5)
       turnInPlace = (0,0)
       maximumTurn = 45
       upAngle = 45
       upSpeed = 0.5
       downAngle = 45
       downSpeed = 2
       betweenDownUp = 10

    case FlyQuality.Average =>
       minimumForwardSpeed = 0.5
       hover = false
       moveBackward = false
       reverse = (false,0)
       turn = (45,5)
       turnInPlace = (45,5)
       maximumTurn = 90
       upAngle = 60
       upSpeed = 0.5
       downAngle = 360
       downSpeed = 2
       betweenDownUp = 5

    case FlyQuality.Good =>
       minimumForwardSpeed = 0
       hover = true
       moveBackward = true
       reverse = (true,5)
       turn = (90,5)
       turnInPlace = (90,5)
       maximumTurn = 360
       upAngle = 360
       upSpeed = 0.5
       downAngle = 360
       downSpeed = 2
       betweenDownUp = 0

    case FlyQuality.Perfect =>
       minimumForwardSpeed = 0
       hover = true
       moveBackward = true
       reverse = (true,0)
       turn = (360,0)
       turnInPlace = (360,0)
       maximumTurn = 360
       upAngle = 360
       upSpeed = 1
       downAngle = 360
       downSpeed = 2
       betweenDownUp = 0
  }

}
