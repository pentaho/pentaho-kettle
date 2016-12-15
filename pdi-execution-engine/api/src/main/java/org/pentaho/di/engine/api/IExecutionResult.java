package org.pentaho.di.engine.api;

import java.util.List;

/**
 * Created by nbaker on 6/22/16.
 */
public interface IExecutionResult  {

  default IProgressReporting.Status getStatus() {
    // Man I wish we could make this final

    return IProgressReporting.Status.FINISHED;
  }

  List<IProgressReporting<IDataEvent>> getDataEventReport();


}
