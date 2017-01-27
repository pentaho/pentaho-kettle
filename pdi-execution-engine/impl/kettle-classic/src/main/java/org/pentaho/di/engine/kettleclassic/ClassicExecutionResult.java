package org.pentaho.di.engine.kettleclassic;

import com.google.common.collect.ImmutableMap;
import org.pentaho.di.engine.api.ExecutionResult;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.reporting.Metrics;

import java.util.Map;

/**
 * Created by nbaker on 1/5/17.
 */
public class ClassicExecutionResult implements ExecutionResult {
  @Override public Map<Operation, Metrics> getDataEventReport() {
    return ImmutableMap.of();
  }
}
