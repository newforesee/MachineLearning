package controllers


import java.nio.file.Paths

import org.codehaus.janino.Java
import ml.stats.TSeries.{normalize, zipWithShift}
import ml.workflow.data.DataSource
import ml.trading.OptionModel
import ml.Predef.{DblPair, DblVec}
import ml.reinforcement.qlearning.{QLConfig, QLModel, QLearning}

import scala.util.{Failure, Success, Try}
import play.api._
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc._

import scala.util.{Failure, Success, Try}

class API extends Controller {
  protected val name: String = "Q-learning"

  private var sPath = Paths.get((s"${"public/data/IBM.csv"}")).toAbsolutePath.toString
  private var oPath = Paths.get((s"${"public/data/IBM_O.csv"}")).toAbsolutePath.toString

  // Run configuration parameters
  private var STRIKE_PRICE = 190.0 // Option strike price
  private var MIN_TIME_EXPIRATION = 6 // Minimum expiration time for the option recorded
  private var QUANTIZATION_STEP = 32 // Quantization step (Double => Int)
  private var ALPHA = 0.2 // Learning rate
  private var DISCOUNT = 0.6 // Discount rate used in the Q-Value update equation
  private var MAX_EPISODE_LEN = 128 // Maximum number of iteration for an episode
  private var NUM_EPISODES = 20 // Number of episodes used for training.
  private var MIN_COVERAGE = 0.1

  private var NUM_NEIGHBOR_STATES = 3 // Number of states accessible from any other state
  private var REWARD_TYPE = "Maximum reward"
  private var ret = JsObject(Seq())
  private var retry = 0;

  private def run(
                   REWARD_TYPE: String,
                   quantizeR: Int,
                   alpha: Double,
                   gamma: Double) = {


    val maybeModel = createModel(createOptionModel(DataSource(sPath, false, false, 1).get, quantizeR), DataSource(oPath, false, false, 1).get.extract.get, alpha, gamma)
    if (maybeModel != None) {
      // Extract the historical price of the option and create a model
      // The for-comprehensive loop is used to process the sequence of
      // options as returned values
      val model = maybeModel.get

      if (REWARD_TYPE != "Random") {
        var value = JsArray(Seq())

        var x = model.bestPolicy.EQ.distinct.map(x => {
          value = value.append(JsObject(Seq("x" -> JsNumber(x._1), "y" -> JsNumber(x._2))))
        })

        ret = ret.+("OPTIMAL", value)
      }
    }


  }

  /*
   * Create an option model for a given stock with default strike
   * and minimum expiration time parameters.
   */
  private def createOptionModel(src: DataSource, quantizeR: Int): OptionModel =
    new OptionModel("IBM", STRIKE_PRICE, src, MIN_TIME_EXPIRATION, quantizeR)

  /*
     * Create a model for the profit and loss on an option given
     * the underlying security. The profit and loss is adjusted to
     * produce positive values.
     */
  private def createModel(
                           ibmOption: OptionModel,
                           oPrice: Seq[Double],
                           alpha: Double,
                           gamma: Double): Option[QLModel] = {

    // quantize the value of the option oPrice
    val qPriceMap = ibmOption.quantize(oPrice.toArray)
    val numStates = qPriceMap.size

    /**
      * Constraining method to limit the number of actions available
      * to any given state. This simple implementation identifies
      * the neighboring states within a predefined radius
      */
    val neighbors = (n: Int) => {
      // Compute the list of all the states within a radius
      // of this states.
      def getProximity(idx: Int, radius: Int): List[Int] = {
        val idx_max = if (idx + radius >= numStates) numStates - 1 else idx + radius
        val idx_min = if (idx < radius) 0 else idx - radius
        scala.collection.immutable.Range(idx_min, idx_max + 1).filter(_ != idx)./:(List[Int]())((xs, n) => n :: xs)
      }

      getProximity(n, NUM_NEIGHBOR_STATES)
    }

    // Compute the minimum value for the profit, loss so the maximum
    // loss is converted to a null profit
    val qPrice: DblVec = qPriceMap.values.toVector
    val profit: DblVec = normalize(zipWithShift(qPrice, 1).map { case (x, y) => y - x }).get
    val maxProfitIndex = profit.zipWithIndex.maxBy(_._1)._2

    val reward = (x: Double, y: Double) => Math.exp(30.0 * (y - x))
    val probabilities = (x: Double, y: Double) => if (y < 0.3 * x) 0.0 else 1.0
    ret = ret.+("GOAL_STATE_INDEX", JsNumber(maxProfitIndex))

    // Create a Q-learning algorithm
    if (!QLearning.validateConstraints(profit.size, neighbors)) {
      ret = ret.+("error", JsString("QLearningEval Incorrect states transition constraint"))
      throw new IllegalStateException("QLearningEval Incorrect states transition constraint")
    }

    val instances = qPriceMap.keySet.toSeq.drop(1)
    val config = QLConfig(alpha, gamma, MAX_EPISODE_LEN, NUM_EPISODES, MIN_COVERAGE)
    val qLearning = QLearning[Array[Int]](
      config,
      Array[Int](maxProfitIndex),
      profit,
      reward,
      probabilities,
      instances,
      Some(neighbors)
    )

    val modelO = qLearning.getModel
    if (modelO.isDefined) {
      val numTransitions = numStates * (numStates - 1)
      ret = ret.+("COVERAGE", JsNumber(modelO.get.coverage))
      ret = ret.+("COVERAGE_STATES", JsNumber(numStates))
      ret = ret.+("COVERAGE_TRANSITIONS", JsNumber(numTransitions))
      var value = JsArray()

      var x = qLearning._counters.last._2.distinct.map(x => {
        value = value.append(JsNumber(x))
      })


      ret = ret.+("Q_VALUE", value)
      modelO
    } else {
      if (retry > 5) {
        ret = ret.+("error", JsString(s"$name model undefined"))
        return None
      }
      retry += 1
      Thread.sleep(500)
      return createModel(ibmOption,
        oPrice,
        alpha,
        gamma)
    }
  }


  def compute = Action(parse.anyContent) { request =>
    try {
      if (request.body.asMultipartFormData != None) {
        val formData = request.body.asMultipartFormData.get
        if (formData.file("STOCK_PRICES").nonEmpty && formData.file("STOCK_PRICES").get.filename.nonEmpty)
          sPath = formData.file("STOCK_PRICES").get.ref.file.toString
        if (formData.file("OPTION_PRICES").nonEmpty && formData.file("OPTION_PRICES").get.filename.nonEmpty)
          oPath = formData.file("OPTION_PRICES").get.ref.file.toString

        val parts = formData.dataParts
        if (parts.get("STRIKE_PRICE") != None)
          STRIKE_PRICE = parts.get("STRIKE_PRICE").get.mkString("").toDouble
        if (parts.get("MIN_TIME_EXPIRATION") != None)
          MIN_TIME_EXPIRATION = parts.get("MIN_TIME_EXPIRATION").get.mkString("").toInt
        if (parts.get("QUANTIZATION_STEP") != None)
          QUANTIZATION_STEP = parts.get("QUANTIZATION_STEP").get.mkString("").toInt
        if (parts.get("ALPHA") != None)
          ALPHA = parts.get("ALPHA").get.mkString("").toDouble
        if (parts.get("DISCOUNT") != None)
          DISCOUNT = parts.get("DISCOUNT").get.mkString("").toDouble
        if (parts.get("MAX_EPISODE_LEN") != None)
          MAX_EPISODE_LEN = parts.get("MAX_EPISODE_LEN").get.mkString("").toInt
        if (parts.get("NUM_EPISODES") != None)
          NUM_EPISODES = parts.get("NUM_EPISODES").get.mkString("").toInt
        if (parts.get("MIN_COVERAGE") != None)
          MIN_COVERAGE = parts.get("MIN_COVERAGE").get.mkString("").toDouble
        if (parts.get("NUM_NEIGHBOR_STATES") != None)
          NUM_NEIGHBOR_STATES = parts.get("NUM_NEIGHBOR_STATES").get.mkString("").toInt
        if (parts.get("REWARD_TYPE") != None)
          REWARD_TYPE = parts.get("REWARD_TYPE").get.mkString("")
      }

      ret = JsObject(Seq(
        "STRIKE_PRICE" -> JsNumber(STRIKE_PRICE),
        "MIN_TIME_EXPIRATION" -> JsNumber(MIN_TIME_EXPIRATION),
        "QUANTIZATION_STEP" -> JsNumber(QUANTIZATION_STEP),
        "ALPHA" -> JsNumber(ALPHA),
        "DISCOUNT" -> JsNumber(DISCOUNT),
        "MAX_EPISODE_LEN" -> JsNumber(MAX_EPISODE_LEN),
        "NUM_EPISODES" -> JsNumber(NUM_EPISODES),
        "MIN_COVERAGE" -> JsNumber(MIN_COVERAGE),
        "NUM_NEIGHBOR_STATES" -> JsNumber(NUM_NEIGHBOR_STATES),
        "REWARD_TYPE" -> JsString(REWARD_TYPE)))


      run(REWARD_TYPE, QUANTIZATION_STEP, ALPHA, DISCOUNT)
    }
    catch {
      case e: Exception => {
        ret = ret.+("exception", JsString(e.toString))
      }
    }

    Ok(ret)
  }

}