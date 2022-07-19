package top.newforesee.recommend

import org.apache.spark.ml.recommendation.{ALS, ALSModel}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * (对Job的功能描述)
 * Create by 牛方兴 on 2022/7/4
 * READ : [TableName]
 * SAVE : [TableName]
 */
object ALSTest extends App {

  private val spark: SparkSession = SparkSession.builder().master("local[*]").getOrCreate()
  //user id | item id | rating | timestamp


  val data: DataFrame = spark.read
    .schema("user_id INT, item_id INT, rating INT, timesp long")
    .option("sep", "\t")
    .csv("data/ml-100k/u.data")
  val Array(train, test) = data.randomSplit(Array(.8, .2))


  val als: ALS = new ALS()
    .setMaxIter(5)
    .setRegParam(0.1)
    .setUserCol("user_id")
    .setItemCol("item_id")
    .setRatingCol("rating")

  val model: ALSModel = als.fit(train)


  model.recommendForUserSubset(test,10).show(false)


}
