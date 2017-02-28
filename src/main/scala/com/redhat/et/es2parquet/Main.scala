import org.apache.spark.sql.SparkSession

object Main {
  def main(args: Array[String]) {
    import org.elasticsearch.spark.sql._

    val sesh = SparkSession.builder().master("local[*]").getOrCreate()

    val sqlc = sesh.sqlContext
    val df = sqlc.esDF(args(1), Map("es.nodes" -> args(0), "es.nodes.wan.only" -> "true", "es.read.field.as.array.include" -> "tags"))
    df.write.save(args(2))
  }
}
