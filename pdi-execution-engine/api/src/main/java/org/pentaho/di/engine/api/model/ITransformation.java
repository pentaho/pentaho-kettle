package org.pentaho.di.engine.api.model;

import org.pentaho.di.engine.api.IHasConfig;

import java.util.List;

public interface ITransformation extends ILogicalModelElement, IHasConfig {
  List<IOperation> getOperations();


  List<IHop> getHops();

}
