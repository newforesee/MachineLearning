
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/asif/PlayML/conf/routes
// @DATE:Sun Jan 21 06:47:20 PST 2018

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._

import _root_.controllers.Assets.Asset

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:6
  API_0: controllers.API,
  // @LINE:9
  Assets_1: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:6
    API_0: controllers.API,
    // @LINE:9
    Assets_1: controllers.Assets
  ) = this(errorHandler, API_0, Assets_1, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, API_0, Assets_1, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/compute""", """controllers.API.compute"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/compute""", """controllers.API.compute"""),
    ("""GET""", this.prefix, """controllers.Assets.at(path:String = "/public", file:String = "index.html")"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """""" + "$" + """file<.+>""", """controllers.Assets.at(path:String = "/public", file:String)"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:6
  private[this] lazy val controllers_API_compute0_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/compute")))
  )
  private[this] lazy val controllers_API_compute0_invoker = createInvoker(
    API_0.compute,
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.API",
      "compute",
      Nil,
      "POST",
      """ Home page""",
      this.prefix + """api/compute"""
    )
  )

  // @LINE:7
  private[this] lazy val controllers_API_compute1_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/compute")))
  )
  private[this] lazy val controllers_API_compute1_invoker = createInvoker(
    API_0.compute,
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.API",
      "compute",
      Nil,
      "GET",
      """""",
      this.prefix + """api/compute"""
    )
  )

  // @LINE:9
  private[this] lazy val controllers_Assets_at2_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_Assets_at2_invoker = createInvoker(
    Assets_1.at(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "at",
      Seq(classOf[String], classOf[String]),
      "GET",
      """""",
      this.prefix + """"""
    )
  )

  // @LINE:10
  private[this] lazy val controllers_Assets_at3_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_at3_invoker = createInvoker(
    Assets_1.at(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "at",
      Seq(classOf[String], classOf[String]),
      "GET",
      """""",
      this.prefix + """""" + "$" + """file<.+>"""
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:6
    case controllers_API_compute0_route(params) =>
      call { 
        controllers_API_compute0_invoker.call(API_0.compute)
      }
  
    // @LINE:7
    case controllers_API_compute1_route(params) =>
      call { 
        controllers_API_compute1_invoker.call(API_0.compute)
      }
  
    // @LINE:9
    case controllers_Assets_at2_route(params) =>
      call(Param[String]("path", Right("/public")), Param[String]("file", Right("index.html"))) { (path, file) =>
        controllers_Assets_at2_invoker.call(Assets_1.at(path, file))
      }
  
    // @LINE:10
    case controllers_Assets_at3_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at3_invoker.call(Assets_1.at(path, file))
      }
  }
}
