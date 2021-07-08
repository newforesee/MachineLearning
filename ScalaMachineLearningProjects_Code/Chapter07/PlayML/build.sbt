name := "PlayML"

version := "1.0"

lazy val `playml` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(filters, cache, ws, "org.apache.commons" % "commons-math3" % "3.6",
  "com.typesafe.play" %% "play-json" % "2.5",
  "org.jfree" % "jfreechart" % "1.0.17",
  "com.typesafe.akka" %% "akka-actor" % "2.3.8",
  "org.apache.spark" %% "spark-core" % "2.1.0",
  "org.apache.spark" %% "spark-mllib" % "2.1.0",
  "org.apache.spark" %% "spark-streaming" % "2.1.0")
// Resolver for Apache Spark framework
resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

// Options for the Scala compiler should be customize
scalacOptions ++= Seq("-unchecked", "-optimize", "-language:postfixOps")

// Specifies the content for the root package used in Scaladoc
scalacOptions in(Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value + "/root-doc.txt")

excludeFilter in doc in Compile := "*.java" || "*.jar"

// Set the path to the unmanaged, 3rd party libraries
unmanagedClasspath in Compile ++= Seq(
  file("lib/CRF-1.1.jar"),
  file("lib/Trove-3.0.2.jar"),
  file("lib/colt.jar"),
  file("lib/libsvm_sml-3.18.jar")
)



      