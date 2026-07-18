/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.core.row.value;

import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaSerializable extends ValueMetaBase implements ValueMetaInterface {

  public ValueMetaSerializable() {
    this( null );
  }

  public ValueMetaSerializable( String name ) {
    super( name, ValueMetaInterface.TYPE_SERIALIZABLE );
  }

}
