package org.pentaho.di.engine.api;

import java.util.List;

/**
 * IOperation roughly corresponds to a Step in PDI.
 * An IOperation represents the logical structure of
 * a step within the trans graph.  Materialization of
 * an IOperation converts it to an ICallableOperation,
 * which is associated with the behavior specific to
 * an IEngine.
 */
public interface IOperation {

  String getId();

  List<IOperation> getFrom();
  List<IOperation> getTo();

  String getConfig();

  // how to represent and manage config metadata?

  /**
   * Accepts an IOperationVisitor.  Used for inspecting or modifying
   * the Trans graph.
   */
  <T> T accept( IOperationVisitor<T> visitor );


}
