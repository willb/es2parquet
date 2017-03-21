package com.redhat.et.es2parquet

import org.apache.spark.sql.SparkSession
import scopt.OptionParser

case class ES2PConfig(endpoint: String="", indices: String="", output: String = "out.parquet", saveMode: String = "overwrite", arrayFields: Seq[String] = Seq("tags"), scrollsize: String = "10000")

object Main {
  def main(args: Array[String]) {
    import org.elasticsearch.spark.sql._
    
    val parser = new OptionParser[ES2PConfig]("es2parquet") {
      head("es2parquet", "0.0.1")

      opt[String]("endpoint")
	.required()
	.action((x,c) => c.copy(endpoint=x))
	.text("elasticsearch endpoint to connect to")

      opt[String]("indices")
	.required()
	.action((x,c) => c.copy(indices=x))
	.text("elasticsearch indices to download")

      opt[String]('o', "output")
	.action((x,c) => c.copy(output=x))
	.text("destination for parquet output (default is \"out.parquet\")")

      opt[String]("savemode")
	.action((x,c) => c.copy(saveMode=x))
	.text("what to do if the output file already exists (default is \"overwrite\")")
      
      opt[String]("scrollsize")
	.action((x,c) => {
	  val ss = scala.util.Try(x.toInt.toString).recover({case _ => Console.println("expected an integer for --scrollsize"); System.exit(1); "0"})
	  c.copy(scrollsize=ss.get)
	})
	.text("Elasticsearch scroll size (default is 10000)")

      help("help").text("prints this usage text")
    }
    
    parser.parse(args, ES2PConfig()) match {
      case Some(config) =>
	val sesh = SparkSession.builder().master("local[*]").getOrCreate()
	val endpoint = config.endpoint
	val indices = config.indices
	val output = config.output
	val savemode = config.saveMode
	val scrollsize = config.scrollsize

	val sqlc = sesh.sqlContext
	val df = sqlc.esDF(indices, Map("es.nodes" -> endpoint, "es.nodes.wan.only" -> "true", "es.read.field.as.array.include" -> "tags", "es.scroll.size" -> scrollsize))
	df.write.mode(savemode).save(output)

	sesh.sparkContext.stop
      case None => ()
    }
  }
}
