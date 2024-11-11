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


package org.pentaho.di.engine.api.events;

import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.pentaho.di.engine.api.model.Row;
import org.pentaho.di.engine.api.model.Rows;

/**
 * A {@link PDIEvent} associated with a list of {@link Row} elements.
 * <p>
 * Created by nbaker on 5/30/16.
 */
public class DataEvent<S extends LogicalModelElement> extends BaseEvent<S, Rows> {

  private static final long serialVersionUID = 106147921259300759L;

  public DataEvent( S source, Rows rows ) {
    super( source, rows );
  }

}
