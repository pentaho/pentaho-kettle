/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.auth;

import org.pentaho.di.core.auth.core.AuthenticationProvider;

public class KerberosAuthenticationProvider implements AuthenticationProvider {
  public static class KerberosAuthenticationProviderType implements AuthenticationProviderType {

    @Override
    public String getDisplayName() {
      return KerberosAuthenticationProvider.class.getName();
    }

    @Override
    public Class<? extends AuthenticationProvider> getProviderClass() {
      return KerberosAuthenticationProvider.class;
    }
  }
  private String principal;
  private boolean useExternalCredentials;
  private String password;
  private boolean useKeytab;
  private String keytabLocation;
  private String id;

  public KerberosAuthenticationProvider() {

  }

  public KerberosAuthenticationProvider( String id, String principal, boolean useExternalCredentials, String password,
      boolean useKeytab, String keytabLocation ) {
    this.id = id;
    this.principal = principal;
    this.useExternalCredentials = useExternalCredentials;
    this.password = password;
    this.useKeytab = useKeytab;
    this.keytabLocation = keytabLocation;
  }

  public String getPrincipal() {
    return principal;
  }

  public void setPrincipal( String principal ) {
    this.principal = principal;
  }

  public boolean isUseExternalCredentials() {
    return useExternalCredentials;
  }

  public void setUseExternalCredentials( boolean useExternalCredentials ) {
    this.useExternalCredentials = useExternalCredentials;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  public boolean isUseKeytab() {
    return useKeytab;
  }

  public void setUseKeytab( boolean useKeytab ) {
    this.useKeytab = useKeytab;
  }

  public String getKeytabLocation() {
    return keytabLocation;
  }

  public void setKeytabLocation( String keytabLocation ) {
    this.keytabLocation = keytabLocation;
  }

  @Override
  public String getDisplayName() {
    return principal;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }
}
