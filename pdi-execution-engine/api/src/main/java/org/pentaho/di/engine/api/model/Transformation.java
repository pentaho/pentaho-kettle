package org.pentaho.di.engine.api.model;

import org.pentaho.di.engine.api.HasConfig;

import java.util.List;

public interface Transformation extends LogicalModelElement, HasConfig {
  List<Operation> getOperations();


  List<Hop> getHops();

}
