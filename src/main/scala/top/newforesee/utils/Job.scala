package top.newforesee.utils

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

trait Job {
  val logger: Logger = Logger.getLogger(this.getClass)
  val spark: SparkSession = Util.getSpark()
  spark.sparkContext.setLogLevel("WARN")

  def main(args: Array[String]): Unit = {

    val start: Long = System.currentTimeMillis()

    logger.log(Level.WARN, s"Starting Job.")
    // Try catch here
    run()
    logger.log(Level.WARN, s"Finished Job in ${(System.currentTimeMillis() - start) / 1000} seconds.")

  }

  def run()


}
