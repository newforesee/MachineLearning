package top.newforesee.utils

import java.util.Properties

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.col
import top.newforesee.constants.Constants

object Util {
  val spark = getSpark()


  def getSpark(): SparkSession = {
    try {
      SparkSession.builder().enableHiveSupport()
        .config("spark.shuffle.service.enabled", true)
        .config("spark.driver.maxResultSize", "4G")
        .config("spark.sql.parquet.writeLegacyFormat", true)
        .getOrCreate()
    } catch {
      case e: Exception => {
        SparkSession.builder().
          master("local[*]").
          appName("Spark Locally").
          getOrCreate()
      }
    }
  }
  import spark.implicits._

  /**
   * 为了简化加载Properties
   * 返回一个加载好的Properties实例，使用传入的name/path，在此之前应该在resource目录下准备好properties文件
   * name也可以是相对路径，如区分环境时，可使用"${ENV}/log4j.properties"
   *
   * @param name
   * @return
   */
  def loadPropertie(name: String) = {
    val properties = new Properties()
    properties.load(this.getClass.getClassLoader.getResourceAsStream(s"$name"))
    properties
  }

  def formatCapitalizeNamesWithPrefix(dataFrame: DataFrame, prefix: String): DataFrame = {
    val capitalizedNames = dataFrame.columns.map(colname => prefix + colname.replaceAll("[(|)]", "").replaceAll(" ", "_").toUpperCase)
    val originalNames = dataFrame.columns
    dataFrame.select(List.tabulate(originalNames.length)(i => col(originalNames(i)).as(capitalizedNames(i))): _*)
  }


  def formatCapitalizeNames(dataFrame: DataFrame): DataFrame = {
    val capitalizedNames = dataFrame.columns.map(colname => colname.replaceAll("[(|)]", "").replaceAll("[ /\\\\\\-]", "_").toUpperCase)
    val originalNames = dataFrame.columns
    dataFrame.select(List.tabulate(originalNames.length)(i => col(originalNames(i)).as(capitalizedNames(i))): _*)
  }


  def getWarehousePath(warehouseRootPath: String, database: String = "default", tableName: String): String = {
    var dbname = database.toLowerCase
    var layer = "na"
    if (dbname.contains("_raw")) {
      dbname = dbname.replaceAll("_raw", "")
      layer = "raw"
    }
    else if (dbname.contains("_primary")) {
      dbname = dbname.replaceAll("_primary", "")
      layer = "primary"
    }
    else if (dbname.contains("_features") || dbname.contains("_feature")) {
      dbname = dbname.replaceAll("_features", "")
      dbname = dbname.replaceAll("_feature", "")
      layer = "features"
    }
    else if (dbname.contains("_mi") || dbname.contains("_modelinput")) {
      dbname = dbname.replaceAll("_modelinput", "")
      dbname = dbname.replaceAll("_mi", "")
      layer = "mi"
    }
    else if (dbname.contains("_mo") || dbname.contains("_modeloutput")) {
      dbname = dbname.replaceAll("_modeloutput", "")
      dbname = dbname.replaceAll("_mo", "")
      layer = "mo"
    }
    else if (dbname.contains("_rpt") || dbname.contains("_report")) {
      dbname = dbname.replaceAll("_report", "")
      dbname = dbname.replaceAll("_rpt", "")
      layer = "rpt"
    }
    else if (dbname.contains("_reference")) {
      dbname = dbname.replaceAll("_reference", "")
      layer = "reference"
    }
    warehouseRootPath.toLowerCase + "/" + layer + "/" + dbname + "/" + tableName.toLowerCase
  }


  /**
   * Saves data frame to target table.
   *
   * @param df     data to save
   * @param db     database name
   * @param tbl    table name
   * @param path   warehouse path
   * @param mode   write option mode, default = overwrite
   * @param format write format, default = parquet
   */
  def saveData(df: DataFrame, db: String, tbl: String, path: String = Constants.HDFS_PATH, mode: String = "overwrite", format: String = "parquet"): Unit = {

    formatCapitalizeNames(df).write.
      options(Map("path" -> Util.getWarehousePath(path, db, tbl))).
      mode(mode).format(format).
      saveAsTable(s"$db.$tbl")
  }


}
