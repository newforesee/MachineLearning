package top.newforesee.movies

import java.io

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession}
import top.newforesee.movies.MoviesTest.spark
import top.newforesee.utils.Job

/**
 * (对Job的功能描述)
 * Create by ra144 on 2021/10/13
 * READ : [TableName]
 * SAVE : [TableName]
 */
object Test extends Job {

  import spark.implicits._

  val TRAIN_FILE = "data/ml-100k/ua.base"
  val TEST_FILE = "data/ml-100k/ua.test"
  val MOVIES = "data/ml-100k/u.item"

  override def run(): Unit = {

    //spark.conf.set("spark.sql.session.timeZone", "UTC")

    println(spark.conf.get("spark.sql.session.timeZone"))


    val data: DataFrame = spark.read.parquet("data/dbo.sfa_events.parquet")
    val data2: DataFrame = data.withColumn("created_on", date_format($"created_on", "yyyy-MM-dd HH:mm:ss"))

    data.show()
    data2.show()

    spark.conf.set("spark.sql.session.timeZone", "UTC")

    println(spark.conf.get("spark.sql.session.timeZone"))

    data.show()
    data2.show()

    reformatDate(data).show()


    def formatCapitalizeNames(dataFrame: DataFrame): DataFrame = {
      val capitalizedNames = dataFrame.columns.map(colname => colname.replaceAll("[(|)]", "").replaceAll(" ", "_").replaceAll("/", "").replaceAll("\\\\", "").replaceAll("-", "_").toUpperCase)
      val originalNames = dataFrame.columns
      dataFrame.select(List.tabulate(originalNames.length)(i => col(originalNames(i)).as(capitalizedNames(i))): _*)
    }



    // 计算被评分次数最多的电影


    // 计算平均分最高的前三部电影（没有并列仅取3部）


  }


  private def reformatDate(data: DataFrame): DataFrame = {
    data.schema.foldLeft(data) {
      case (acc: DataFrame, col: StructField) => {
        if (col.dataType == DateType)
          acc.withColumn(col.name, date_format(data(col.name), "yyyy-MM-dd HH:mm:ss"))
        else
          acc.withColumn(col.name, data(col.name))
      }
    }
  }

  case class Movie(id: Int, name: String)

  case class Rating(userId: Int, movieId: Int, rating: Int)

  val movies: RDD[Movie] = spark.read.textFile(MOVIES)
    .rdd
    .map(line => {
      val feilds = line.split("\\|")
      Movie(feilds(0).toInt, feilds(1))
    })
  val movies_df: DataFrame = spark.createDataFrame(movies)


  val movieRating = spark.read.textFile(TRAIN_FILE).rdd
    .map(line => {
      val feilds = line.split("\t")
      Rating(feilds(0).toInt, feilds(1).toInt, feilds(2).toInt)
    })
  val userid_movieid_rating_df: DataFrame = spark.createDataFrame(movieRating)

}
