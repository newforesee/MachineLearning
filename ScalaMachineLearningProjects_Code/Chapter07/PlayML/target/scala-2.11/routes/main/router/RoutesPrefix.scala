
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/asif/PlayML/conf/routes
// @DATE:Sun Jan 21 06:47:20 PST 2018


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
