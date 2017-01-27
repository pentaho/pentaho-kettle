package org.pentaho.di.engine.api.model;

import org.pentaho.di.engine.api.HasConfig;

import java.util.List;

/**
 * IOperation roughly corresponds to a Step in PDI.
 * An IOperation represents the logical structure of
 * a step within the trans graph.  Materialization of
 * an IOperation converts it to an ICallableOperation,
 * which is associated with the behavior specific to
 * an IEngine.
 */
public interface Operation extends LogicalModelElement, HasConfig {

  List<Operation> getFrom();
  List<Operation> getTo();


  List<Hop> getHopsIn();
  List<Hop> getHopsOut();

}
