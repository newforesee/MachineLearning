
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._


object InServiceDateHBDataPrepare {
  val spark = SparkSession.builder.appName("InServiceDateDataPrepareAAA").enableHiveSupport().getOrCreate()
  val sc = spark.sparkContext
  val sqlContext = spark.sqlContext
  spark.sqlContext.setConf("hive.exec.dynamic.partition.mode", "nonstrict")
  import spark.implicits._
  spark.conf.set("spark.sql.autoBroadcastJoinThreshold", -1)
  spark.conf.set("spark.sql.shuffle.partitions", 400)

  def main(args: Array[String]): Unit = {
    //    val start = args(0)
    //    val end = args(1)
    val start = "2021-04-10"
    val end = "2021-04-11"
    val format1 = new SimpleDateFormat("yyyy-MM-dd")
    val format2 = new SimpleDateFormat("yyyyMMdd")
    val startDate: Date = format1.parse(start)
    val endDate: Date = format1.parse(end)



    val in_service_date = spark.read.format("csv").option("header","true").
      load("wasbs://test@sa02cnprd19909.blob.core.chinacloudapi.cn/czy/nsvi_in_service_date_data/").
      withColumn("rank",row_number() over(Window.partitionBy("esn").orderBy(asc_nulls_last("actual_in_service_date")))).
      filter("rank = 1").
      filter("actual_in_service_date is not null").
      filter("datediff(actual_in_service_date,rel_build_date) > 0").
      select("esn","actual_in_service_date").
      withColumn("start_date",date_format(date_sub(to_date($"actual_in_service_date"),7),"yyyyMMdd")).
      withColumn("end_date",date_format(date_add(to_date($"actual_in_service_date"),7),"yyyyMMdd"))


    val startDateString = format1.format(startDate)
    val endDateString = format1.format(endDate)
    val in_service_date_calculated = in_service_date.filter(s"to_date(actual_in_service_date) between date_sub(to_date('${startDateString}'),8) and date_add(to_date('${endDateString}'),8)")
    spark.catalog.dropTempView("t1")
    in_service_date_calculated.createTempView("t1")
    val hb_data = spark.table("pri_tel.tel_hb_labeled").
      filter(s"report_date between date_format(to_date('${startDateString}'),'yyyyMMdd') and date_format(to_date('${endDateString}'),'yyyyMMdd') and engine_serial_number in (select esn from t1)")

    val resultDF = hb_data.join(in_service_date_calculated,
      hb_data("engine_serial_number") === in_service_date_calculated("esn") and hb_data("report_date") >= in_service_date_calculated("start_date") and hb_data("report_date") <= in_service_date_calculated("end_date"),
      "inner").
      drop("esn","start_date","end_date")

    resultDF.write.mode("append").partitionBy("actual_in_service_date").
      orc("wasbs://test@sa04cnstg19909.blob.core.chinacloudapi.cn/czy/in_service_date/hb_data/")


  }


}
