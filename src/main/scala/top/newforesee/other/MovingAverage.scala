package top.newforesee.other

import org.apache.spark.sql.expressions.{UserDefinedFunction, Window, WindowSpec}
import org.apache.spark.sql.functions.{col, _}
import org.apache.spark.sql.{Column, DataFrame}
import top.newforesee.utils.Job

object MovingAverage extends Job {

  import spark.implicits._

  override def run(): Unit = {

  }

  /**
   * 计算 EMA2 对于单一subpopulation的EMA2，直接将COUNT_NBR,COST_NBR...这些值复制过来即可。
   *
   * @param dataFrame
   * @param partitionColumn
   * @param orderColumn
   * @param calcCols
   * @param windowSize
   * @return
   */
  def exponentialMovingAverage(dataFrame: DataFrame, partitionColumn: Seq[Column], orderColumn: String, calcCols: Seq[String], windowSize: Int): DataFrame = {
    val window: WindowSpec = Window.partitionBy(partitionColumn: _*)
    val exponentialMovingAveragePrefix = "_MA"

    val emaUDF: UserDefinedFunction = udf((rowNumber: Int, columnPartitionValues: Seq[Double]) => {
      val alpha: Double = 2.0 / (windowSize + 1)
      val adjustedWeights: Array[Double] = (0 until rowNumber + 1).foldLeft(new Array[Double](rowNumber + 1)) { (accumulator, index) =>
        accumulator(index) = math.pow(1 - alpha, rowNumber - index)
        accumulator
      }
      (adjustedWeights, columnPartitionValues.slice(0, rowNumber + 1)).zipped.map(_ * _).sum / adjustedWeights.sum
    })

    var df = dataFrame

    if (calcCols.nonEmpty) {
      calcCols.foreach(column => {
        df = df
          .withColumn("row_nr", row_number().over(window.orderBy(orderColumn)) - lit(1))
          .withColumn(s"$column$exponentialMovingAveragePrefix$windowSize", emaUDF(col("row_nr"), collect_list(column).over(window)))
          .withColumn(s"$column$exponentialMovingAveragePrefix$windowSize",
            when(count($"$column").over(window.orderBy(orderColumn)) === 1, $"$column")
              .otherwise($"$column$exponentialMovingAveragePrefix$windowSize"))
          .drop("row_nr")
      })
    }
    df
  }

  /**
   * 计算 MA[4,8,12,16.20,24] 当subpopulation的数量 < 要计算的MA的要求 附空值即可;
   *
   * @param df
   * @return
   */
  def movingAverage(df: DataFrame, n: Int, partitionColumn: Seq[Column], orderColumn: String, calcCols: Seq[String]): DataFrame = {

    val maSpec: WindowSpec = Window.partitionBy(partitionColumn: _*)
    var result = df

    if (calcCols.nonEmpty) {
      calcCols.foreach(col => {
        result = result
          .withColumn(s"${col}_MA$n", avg($"$col").over(maSpec.orderBy(orderColumn).rowsBetween(-(n - 1), Window.currentRow)))
          .withColumn(s"${col}_MA$n", when(count($"CALC_DATE_INDEX").over(maSpec) < n, null)
            .otherwise(when($"CALC_DATE_INDEX" < date_add(min($"CALC_DATE").over(maSpec), (n - 1) * 7), null)
              .otherwise($"${col}_MA$n")))
      })
    }
    result

  }

}
