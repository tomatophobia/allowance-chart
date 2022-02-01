ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.easywritten"
ThisBuild / organizationName := "easywritten"

lazy val root = (project in file("."))
  .settings(
    name := "allowance-chart",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.13",
      "dev.zio" %% "zio-test" % "1.0.13" % Test,
      "org.typelevel" %% "cats-core" % "2.7.0",
      "com.beachape" %% "enumeratum" % "1.7.0"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    wartremoverErrors ++= Warts.allBut(
      // for zio
      Wart.Any,
      Wart.Nothing,
      // 필요함
      Wart.Var
    ),
    // TODO warning 많아지면 잘 안보게 될테니 어떻게든 없애던가 아니면 그냥 전체 허용을 하던가
    wartremoverWarnings ++= Seq(Wart.Var)
  )
