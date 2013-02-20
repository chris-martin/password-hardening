import sbt._
import Keys._

object SecLoginBuild extends Build {

  lazy val project = Project(
    id = "seclogin",
    base = file("."),
    settings = Defaults.defaultSettings ++ Seq(
      organization := "seclogin",
      version := "1.0-SNAPSHOT",
      scalaVersion := "2.10.0",
      scalacOptions ++= Seq(
        "-unchecked",
        "-deprecation",
        "-feature",
        "-language:implicitConversions"
      ),
      javacOptions ++= Seq(
        "-source", "1.6",
        "-target", "1.6"
      ),
      publishMavenStyle := true,
      publishTo := Some(
        Resolver.file(
          "file",
          new File(Path.userHome.absolutePath + "/.m2/repository")
        )
      ),
      libraryDependencies ++= Seq(
        "com.google.guava" % "guava" % "14.0-rc3",
        "net.sourceforge.argparse4j" % "argparse4j" % "0.3.2",
        "org.scala-lang" % "jline" % "2.10.0"
      ),
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "1.9.1",
        "junit" % "junit" % "4.11",
        "com.dadrox" %% "sbt-junit" % "0.1"
      ) map (_ % "test"),
      testFrameworks += new TestFramework("com.dadrox.sbt.junit.JunitFramework"),
      compileOrder := CompileOrder.ScalaThenJava
    )
  )

}
