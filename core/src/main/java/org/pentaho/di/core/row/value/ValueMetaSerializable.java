/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
