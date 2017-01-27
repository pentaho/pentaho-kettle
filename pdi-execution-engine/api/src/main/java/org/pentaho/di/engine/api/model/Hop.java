package org.pentaho.di.engine.api.model;

/**
 * @author nhudak
 */
public interface Hop extends LogicalModelElement {
  @Override default String getId() {
    return getFrom().getId() + " -> " + getTo().getId();
  }

  String TYPE_NORMAL = "NORMAL";

  Operation getFrom();

  Operation getTo();

  default String getType() {
    return TYPE_NORMAL;
  }

}
