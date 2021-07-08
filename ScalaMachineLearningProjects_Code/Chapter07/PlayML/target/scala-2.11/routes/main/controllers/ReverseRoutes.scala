
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/asif/PlayML/conf/routes
// @DATE:Sun Jan 21 06:47:20 PST 2018

import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset

// @LINE:6
package controllers {

  // @LINE:6
  class ReverseAPI(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:6
    def compute(): Call = {
    
      () match {
      
        // @LINE:6
        case ()  =>
          import ReverseRouteContext.empty
          Call("POST", _prefix + { _defaultPrefix } + "api/compute")
      
      }
    
    }
  
  }

  // @LINE:9
  class ReverseAssets(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:9
    def at(file:String): Call = {
    
      (file: @unchecked) match {
      
        // @LINE:9
        case (file) if file == "index.html" =>
          implicit val _rrc = new ReverseRouteContext(Map(("path", "/public"), ("file", "index.html")))
          Call("GET", _prefix)
      
        // @LINE:10
        case (file)  =>
          implicit val _rrc = new ReverseRouteContext(Map(("path", "/public")))
          Call("GET", _prefix + { _defaultPrefix } + implicitly[PathBindable[String]].unbind("file", file))
      
      }
    
    }
  
  }


}
