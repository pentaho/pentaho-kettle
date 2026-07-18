/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
package org.pentaho.di.repovfs.plugin.config;

import org.apache.commons.configuration2.PropertiesConfiguration;

public class Config {

  private static final String CFG_USE_REMOTE_PUR = "use.remote.pur";

  private boolean useRemotePur = true;

  public Config( PropertiesConfiguration props ) {
    useRemotePur = props.getBoolean( CFG_USE_REMOTE_PUR, useRemotePur );
  }

  public Config() {
  }

  public boolean useRemotePur() {
    return useRemotePur;
  }

}
