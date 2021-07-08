
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/asif/PlayML/conf/routes
// @DATE:Sun Jan 21 06:47:20 PST 2018

import play.api.routing.JavaScriptReverseRoute
import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset

// @LINE:6
package controllers.javascript {
  import ReverseRouteContext.empty

  // @LINE:6
  class ReverseAPI(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:6
    def compute: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.API.compute",
      """
        function() {
        
          if (true) {
            return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "api/compute"})
          }
        
        }
      """
    )
  
  }

  // @LINE:9
  class ReverseAssets(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:9
    def at: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Assets.at",
      """
        function(file1) {
        
          if (file1 == """ + implicitly[JavascriptLiteral[String]].to("index.html") + """) {
            return _wA({method:"GET", url:"""" + _prefix + """"})
          }
        
          if (true) {
            return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("file", file1)})
          }
        
        }
      """
    )
  
  }


}
