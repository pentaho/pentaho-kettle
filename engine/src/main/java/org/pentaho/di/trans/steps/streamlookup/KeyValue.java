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


package org.pentaho.di.trans.steps.streamlookup;

public class KeyValue {
  private Object[] key;
  private Object[] value;

  public KeyValue( Object[] key, Object[] value ) {
    this.key = key;
    this.value = value;
  }

  public Object[] getKey() {
    return key;
  }

  public Object[] getValue() {
    return value;
  }
}
