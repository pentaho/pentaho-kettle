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
import org.pentaho.di.engine.api.remote.Message;

import java.io.Serializable;

/**
 * Execution event with a logical-model source.
 * <p>
 * This event object should be serializable if the source is too.
 * Data should always be serializable.
 * <p>
 * Created by nbaker on 6/9/16.
 */
public interface PDIEvent<S extends LogicalModelElement, D extends Serializable> extends Message {
  S getSource();

  D getData();

}
