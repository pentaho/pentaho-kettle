/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.jsoninput.json.node;

import java.util.Collections;
import java.util.List;

/**
 * Created by bmorrise on 8/24/18.
 */
public class ValueNode<T> extends Node {
  private T value;

  public ValueNode( String key, T value ) {
    super( key );
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  public void setValue( T value ) {
    this.value = value;
  }

  public String getType() {
    if ( value != null ) {
      return value.getClass().getSimpleName();
    }
    return null;
  }

  @Override
  public List<Node> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    String output = value instanceof String ? "" + value + "" : String.valueOf( value );
    return key + ": " + output + "\n";
  }

  @Override
  public void dedupe() {
    // Do nothing
  }
}
