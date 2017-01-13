package org.pentaho.di.engine.api;

import org.pentaho.di.engine.api.reporting.IReportingEventSource;

import java.util.List;

public interface ITransformation extends IReportingEventSource {
  List<IOperation> getOperations();

  /**
   * @return the list of IOperations associated with this trans that have no "from" Op.
   */
  List<IOperation> getSourceOperations();

  /**
   * @return the list of IOperations associated with this trans that have no "to" Op.
   */
  List<IOperation> getSinkOperations();


  List<IHop> getHops();

  String getConfig();

  @Override String getId();
}
