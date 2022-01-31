ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "allowance-chart",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.13",
      "dev.zio" %% "zio-test" % "1.0.13" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
