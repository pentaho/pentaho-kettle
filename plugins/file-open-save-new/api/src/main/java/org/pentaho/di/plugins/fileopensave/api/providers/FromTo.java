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


package org.pentaho.di.plugins.fileopensave.api.providers;

/**
 * Created by bmorrise on 3/5/19.
 */
public class FromTo {

  private org.pentaho.di.plugins.fileopensave.api.providers.File from;
  private org.pentaho.di.plugins.fileopensave.api.providers.File to;

  public FromTo() {
  }

  public org.pentaho.di.plugins.fileopensave.api.providers.File getFrom() {
    return from;
  }

  public void setFrom( org.pentaho.di.plugins.fileopensave.api.providers.File from ) {
    this.from = from;
  }

  public org.pentaho.di.plugins.fileopensave.api.providers.File getTo() {
    return to;
  }

  public void setTo( org.pentaho.di.plugins.fileopensave.api.providers.File to ) {
    this.to = to;
  }
}
