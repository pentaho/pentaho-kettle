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



package org.pentaho.di.job.entry;

public interface JobEntryRunConfigurableInterface {

  String getRunConfiguration();

  void setRunConfiguration( String runConfiguration );

  void setRemoteSlaveServerName( String remoteSlaveServerName );

  void setLoggingRemoteWork( boolean loggingRemoteWork );

  void setChanged();

}
