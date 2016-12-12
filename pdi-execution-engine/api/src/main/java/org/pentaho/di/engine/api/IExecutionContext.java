package org.pentaho.di.engine.api;

import java.util.Map;

/**
 * Created by nbaker on 5/31/16.
 */
public interface IExecutionContext {

  Map<String, Object> getParameters();

  Map<String, Object> getEnvironment();

  IEngine getEngine();

}
