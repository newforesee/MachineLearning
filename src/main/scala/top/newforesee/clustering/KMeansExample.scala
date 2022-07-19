package top.newforesee.clustering

import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.ml.evaluation.ClusteringEvaluator
import org.apache.spark.sql.{DataFrame, SparkSession}
import top.newforesee.utils.Job

/**
 * (对Job的功能描述)
 * Create by 牛方兴 on 2022/7/5
 * READ : [TableName]
 * SAVE : [TableName]
 */
object KMeansExample extends Job{


  override def run(): Unit = {



    // $example on$
    // Loads data.
    val dataset: DataFrame = spark.read.format("libsvm").load("data/mllib/sample_kmeans_data.txt")

    dataset.show(false)

    // Trains a k-means model.
    val kmeans: KMeans = new KMeans().setK(2).setSeed(1L)
    val model: KMeansModel = kmeans.fit(dataset)

    // Make predictions
    val predictions: DataFrame = model.transform(dataset)

    predictions.show(false)

    // Evaluate clustering by computing Silhouette score
    val evaluator = new ClusteringEvaluator()

    val silhouette = evaluator.evaluate(predictions)
    println(s"Silhouette with squared euclidean distance = $silhouette")

    // Shows the result.
    println("Cluster Centers: ")
    model.clusterCenters.foreach(println)



    // $example off$

    spark.stop()


  }
}
