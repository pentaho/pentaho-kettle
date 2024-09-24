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

package org.pentaho.di.engine.api.events;

import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.pentaho.di.engine.api.reporting.SubTransCreation;

public class SubTransCreationEvent<S extends LogicalModelElement> extends BaseEvent<S, SubTransCreation> {
  private static final long serialVersionUID = -3994003778796690304L;

  public SubTransCreationEvent( S source, SubTransCreation subTransCreation ) {
    super( source, subTransCreation );
  }
}
