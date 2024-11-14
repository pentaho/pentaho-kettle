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


package org.pentaho.di.core.auth;

public interface KerberosAuthenticationProviderProxyInterface {

  public String getPrincipal();

  public void setPrincipal( String principal );

  public boolean isUseExternalCredentials();

  public void setUseExternalCredentials( boolean useExternalCredentials );

  public String getPassword();

  public void setPassword( String password );

  public boolean isUseKeytab();

  public void setUseKeytab( boolean useKeytab );

  public String getKeytabLocation();

  public void setKeytabLocation( String keytabLocation );

  public String getDisplayName();

  public String getId();

  public void setId( String id );
}
