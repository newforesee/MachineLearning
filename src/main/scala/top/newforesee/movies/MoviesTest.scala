package top.newforesee.movies

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame
import top.newforesee.utils.{Job, Util}

object MoviesTest extends Job {

  case class Movie(id: Int, name: String)

  case class Rating(userId: Int, movieId: Int, rating: Int)

  override def run(): Unit = {
    import spark.implicits._
    val TRAIN_FILE = "ml-100k/ua.base"
    val TEST_FILE = "ml-100k/ua.test"
    val MOVIES = "ml-100k/u.item"
    //获取电影序号与名称
    val movie_id_name_rdd: RDD[(Int, String)] = spark.read.textFile(MOVIES)
      .rdd
      .map(line => {
        val feilds = line.split("\\|")
        (feilds(0).toInt, feilds(1))
      })

    val movie_id_name: collection.Map[Int, String] = movie_id_name_rdd.collectAsMap()

    //从评分中获取 用户ID,电影ID,评分
    val userid_movieid_rating: RDD[(Int, Int, Int)] = spark.read.textFile(TRAIN_FILE).rdd
      .map(line => {
        val feilds = line.split("\t")
        (feilds(0).toInt, feilds(1).toInt, feilds(2).toInt)
      })

    //计算每部电影被评分的次数
    val numRatingsPerMovie: RDD[(Int, Int)] = userid_movieid_rating
      .groupBy(_._2) //RDD[(Int, Iterable[(Int, Int, Int)])]
      .map(grouped => {
        (grouped._1, grouped._2.size)
      })

    //关联回电影信息 userId,movieId,rating,count
    val ratingsWithSize: RDD[(Int, Int, Int, Int)] = userid_movieid_rating
      .groupBy(_._2) //RDD[(Int, Iterable[(Int, Int, Int)])]
      .join(numRatingsPerMovie) //RDD[(Int, (Iterable[(Int, Int, Int)], Int))]
      .flatMap((joined: (Int, (Iterable[(Int, Int, Int)], Int))) => {
        joined._2._1.map(f => (f._1, f._2, f._3, joined._2._2))
      })

    // (userId,(userId,movieId,rating,count))
    val rating2: RDD[(Int, (Int, Int, Int, Int))] = ratingsWithSize.keyBy(_._1)

    val ratingPairs: RDD[(Int, ((Int, Int, Int, Int), (Int, Int, Int, Int)))] = ratingsWithSize
      .keyBy(_._1) // (userId,(userId,movieId,rating,count))
      .join(rating2) // (userId,((userId,movieId,rating,count),(userId,movieId,rating,count)))
      .filter(f => f._2._1._2 < f._2._2._2)

    val vectorCalcs: RDD[((Int, Int), (Int, Int, Int, Int, Double, Double, Int, Int))] = ratingPairs
      .map(data => {
        val key: (Int, Int) = (data._2._1._2, data._2._2._2) //电影a-电影b 两两对
        val rating_1: Int = data._2._1._3
        val rating_2: Int = data._2._2._3
        val stats: (Int, Int, Int, Double, Double, Int, Int) = (
          rating_1 * rating_2, // 两个电影的评分相乘
          rating_1, // 电影a的评分
          rating_2, // 电影b的评分
          math.pow(rating_1, 2), // 电影a评分的二次方
          math.pow(rating_2, 2), // 电影b评分的二次方
          data._2._1._4, // 电影a的评分人数
          data._2._2._4 // 电影b的评分人数
        )
        (key, stats)
      })
      .groupByKey()
      .map(data => {
        val key: (Int, Int) = data._1
        val vals: Iterable[(Int, Int, Int, Double, Double, Int, Int)] = data._2
        val size: Int = vals.size
        val dotProduct: Int = vals.map(_._1).sum //两电影评分相乘之和
        val ratingSum: Int = vals.map(_._2).sum
        val ratingSum2: Int = vals.map(_._3).sum
        val ratingSqSum: Double = vals.map(_._4).sum
        val ratingSqSum2: Double = vals.map(_._5).sum
        val numRaters: Int = vals.map(_._6).max
        val numRaters2: Int = vals.map(_._7).max

        (key, (size, dotProduct, ratingSum, ratingSum2, ratingSqSum, ratingSqSum2, numRaters, numRaters2))

      })

    vectorCalcs
      .map(fields => {
        val key: (Int, Int) = fields._1
        val (size, dotProduct, ratingSum, ratingSum2, ratingSqSum, ratingSqSum2, numRaters, numRaters2) = fields._2


      })



    /*   // ------另一种处理方式,使用Spark SQL
       val movies: RDD[Movie] = spark.read.textFile(MOVIES)
         .rdd
         .map(line => {
           val feilds = line.split("\\|")
           Movie(feilds(0).toInt, feilds(1))
         })
       val movies_df: DataFrame = spark.createDataFrame(movies)

       movies_df.show

       val movieRating = spark.read.textFile(TRAIN_FILE).rdd
         .map(line => {
           val feilds = line.split("\t")
           Rating(feilds(0).toInt, feilds(1).toInt, feilds(2).toInt)
         })
       val userid_movieid_rating_df: DataFrame = spark.createDataFrame(movieRating)

       userid_movieid_rating_df.show

       val ratingsWithSize_df: DataFrame = userid_movieid_rating_df
         .join(userid_movieid_rating_df.groupBy($"movieId") count() as "numRaters", Seq("movieId"), "left")
       ratingsWithSize_df.show()
       val ratingsWithSize_df_2: DataFrame = Util.formatCapitalizeNamesWithPrefix(ratingsWithSize_df, "2_")
         .withColumnRenamed("2_userId", "userId")


       ratingsWithSize_df
         .join(ratingsWithSize_df_2, Seq("userId"), "left")
         .where("movieId < 2_movieId")
         .show()
   */

  }

  def correlation(size: Double, dotProduct: Double, ratingSum: Double, ratingSum2: Double, ratingSqSum: Double, ratingSqSum2: Double) = {
    val numerator: Double = size * dotProduct - (ratingSum * ratingSum2)
    val denominator: Double = math.sqrt(size * ratingSqSum - ratingSum * ratingSum) * math.sqrt(size * ratingSqSum2 - ratingSum2 * ratingSum2)

    numerator/denominator

  }

  def regularizedCorrelation()={

  }


}
