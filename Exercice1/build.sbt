name := "Exercice1"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.11" % "2.2.0" exclude ("org.apache.hadoop","hadoop-yarn-server-web-proxy"))