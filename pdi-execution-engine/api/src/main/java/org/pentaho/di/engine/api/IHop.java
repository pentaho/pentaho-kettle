package org.pentaho.di.engine.api;

/**
 * @author nhudak
 */
public interface IHop {

  String TYPE_NORMAL = "NORMAL";

  IOperation getFrom();

  IOperation getTo();

  default String getType() {
    return TYPE_NORMAL;
  }

  void setTo( IOperation to );
}
