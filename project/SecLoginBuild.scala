import sbt._
import sbt.Keys._

object SecLoginBuild extends Build {

  val junit = {
    val framework = new TestFramework("com.dadrox.sbt.junit.JunitFramework")
    Seq(
      testFrameworks += framework,
      testOptions in Test += Tests.Argument(framework, "-vo", "-tv")
    )
  }

  lazy val project = Project(
    id = "seclogin",
    base = file("."),
    settings = Defaults.defaultSettings ++ junit ++ Seq(
      organization := "seclogin",
      version := "1.0-SNAPSHOT",
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
        "org.scala-lang" % "jline" % "2.10.0",
        "org.apache.commons" % "commons-math3" % "3.1.1",
        "com.google.code.findbugs" % "jsr305" % "2.0.1"
      ),
      libraryDependencies ++= Seq(
        "junit" % "junit" % "4.11",
        "com.dadrox" %% "sbt-junit" % "0.1",
        "org.mockito" % "mockito-all" % "1.9.5"
      ) map (_ % "test"),
      compileOrder := CompileOrder.ScalaThenJava
    )
  )

}
