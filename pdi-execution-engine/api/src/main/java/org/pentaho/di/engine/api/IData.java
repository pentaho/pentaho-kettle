package org.pentaho.di.engine.api;

import java.io.Serializable;

public interface IData<T> extends Serializable {
  Object[] getData();


}
