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
object Test extends Job{
  import spark.implicits._
  val TRAIN_FILE = "ml-100k/ua.base"
  val TEST_FILE = "ml-100k/ua.test"
  val MOVIES = "ml-100k/u.item"

  override def run(): Unit = {



    movies_df.show()

    userid_movieid_rating_df.show()



    SparkSession.builder().master("spark://cummins.sparkcluster.com:7077")



    // 计算被评分次数最多的电影


    // 计算平均分最高的前三部电影（没有并列仅取3部）




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
