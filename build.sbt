name := "es2parquet"

organization := "com.redhat.et"

version := "0.0.1"

scalaVersion := "2.11.8"

val SPARK_VERSION = "2.1.0"
val SCALA_VERSION = "2.11.8"

def commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % SPARK_VERSION,
    "org.apache.spark" %% "spark-sql" % SPARK_VERSION,
    "org.scala-lang" % "scala-reflect" % SCALA_VERSION,
    "org.elasticsearch" %% "elasticsearch-spark-20" % "5.1.1"
  )
)

seq(commonSettings:_*)

licenses += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0"))

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

scalacOptions in (Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value+"/root-doc.txt")

(dependencyClasspath in Test) <<= (dependencyClasspath in Test).map(
  _.filterNot(_.data.name.contains("slf4j-log4j12"))
)

lazy val es2parquet = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("com.redhat.et.es2parquet.Main"),
    assemblyMergeStrategy in assembly := { 
      case "META-INF/MANIFEST.MF" => MergeStrategy.concat
      case x => {
	val oldStrategy = (assemblyMergeStrategy in assembly).value
	oldStrategy(x)
      }
    }
  )
