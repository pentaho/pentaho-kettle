package org.pentaho.di.engine.kettleclassic;

import com.google.common.collect.ImmutableMap;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.model.IOperation;
import org.pentaho.di.engine.api.reporting.Metrics;

import java.util.Map;

/**
 * Created by nbaker on 1/5/17.
 */
public class ClassicExecutionResult implements IExecutionResult {
  @Override public Map<IOperation, Metrics> getDataEventReport() {
    return ImmutableMap.of();
  }
}
