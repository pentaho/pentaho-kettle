package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.engine.api.IDataEvent;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.IProgressReporting;

import java.util.List;

/**
 * Created by nbaker on 1/5/17.
 */
public class ClassicExecutionResult implements IExecutionResult {
  @Override public List<IProgressReporting<IDataEvent>> getDataEventReport() {
    return null;
  }
}
