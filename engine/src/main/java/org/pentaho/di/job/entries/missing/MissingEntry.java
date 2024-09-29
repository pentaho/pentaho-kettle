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


package org.pentaho.di.job.entries.missing;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.special.JobEntrySpecial;

public class MissingEntry extends JobEntrySpecial {

  private String missingPluginId;

  public MissingEntry() {
    this( null, null );
  }

  public MissingEntry( String name, String missingPluginId ) {
    super( name, false, false );
    setPluginId( "SPECIAL" );
    this.missingPluginId = missingPluginId;
  }

  @Override
  public Result execute( Result previousResult, int nr ) throws KettleJobException {
    previousResult.setResult( false );
    previousResult.setNrErrors( previousResult.getNrErrors() + 1 );
    getLogChannel().logError( BaseMessages.getString( MissingEntry.class, "MissingEntry.Log.CannotRunJob" ) );
    return previousResult;
  }

  public String getMissingPluginId() {
    return this.missingPluginId;
  }
}
