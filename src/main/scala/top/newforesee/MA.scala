package top.newforesee

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.{Window, WindowSpec}
import org.apache.spark.sql.functions._
import top.newforesee.utils.Job

/**
 * (对Job的功能描述)
 * Create by 牛方兴 on 2022/7/4
 * READ : [TableName]
 * SAVE : [TableName]
 */
object MA extends Job {

  import spark.implicits._

  override def run(): Unit = {

    val data: DataFrame = spark.read
      .schema("user_id INT, item_id INT, rating INT, timesp String")
      .option("sep", "\t")
      .csv("C:\\Users\\ra144\\IdeaProjects\\MachineLearning\\ml-100k\\u.data")
      .withColumn("timesp", to_timestamp(from_unixtime($"timesp", "yyyy-MM-dd"), "yyyy-MM-dd"))



    data
      .groupBy($"timesp")
      .agg(count("rating") as "rating")
      .orderBy($"timesp")
      .groupBy(window($"timesp", "4 days", "2 days")).avg("rating")
      .selectExpr("window.end", "`avg(rating)` as rating")
      .orderBy($"end")
      .show(false)


    //val spec: WindowSpec = Window.partitionBy("item_id").orderBy("rating")


    data.withColumn("rn",
      row_number().over(Window.partitionBy("item_id").orderBy("rating")))

      .where(($"rn" === 1) and ($"rating" > 3)
      )
      .orderBy("item_id")
      .show(false)


  }
}
