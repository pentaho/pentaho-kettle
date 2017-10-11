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

public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {
  public static class UsernamePasswordAuthenticationProviderType implements AuthenticationProviderType {

    @Override
    public String getDisplayName() {
      return UsernamePasswordAuthenticationProvider.class.getName();
    }

    @Override
    public Class<? extends AuthenticationProvider> getProviderClass() {
      return UsernamePasswordAuthenticationProvider.class;
    }
  }
  private String id;
  private String username;
  private String password;

  public UsernamePasswordAuthenticationProvider() {

  }

  public UsernamePasswordAuthenticationProvider( String id, String username, String password ) {
    this.id = id;
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  @Override
  public String getDisplayName() {
    return username;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }
}
