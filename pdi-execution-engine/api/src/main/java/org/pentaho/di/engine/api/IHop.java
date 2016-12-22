package org.pentaho.di.engine.api;

import java.io.Serializable;

/**
 * @author nhudak
 */
public interface IHop extends Serializable {

  String TYPE_NORMAL = "NORMAL";

  IOperation getFrom();

  IOperation getTo();

  default String getType() {
    return TYPE_NORMAL;
  }

}
