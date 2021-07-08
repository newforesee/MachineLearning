
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/asif/PlayML/conf/routes
// @DATE:Sun Jan 21 06:47:20 PST 2018

package controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.ReverseAPI API = new controllers.ReverseAPI(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseAssets Assets = new controllers.ReverseAssets(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.javascript.ReverseAPI API = new controllers.javascript.ReverseAPI(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseAssets Assets = new controllers.javascript.ReverseAssets(RoutesPrefix.byNamePrefix());
  }

}
