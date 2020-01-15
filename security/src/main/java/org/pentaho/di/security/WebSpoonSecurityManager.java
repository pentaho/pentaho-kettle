/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2018 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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

package org.pentaho.di.security;

import java.io.File;
import java.io.FilePermission;
import java.security.CodeSource;
import java.security.Permission;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * We check only FilePermission when called by end-users (UIThread and its child threads).
 */
public class WebSpoonSecurityManager extends SecurityManager {

  private final InheritableThreadLocal<String> userName = new InheritableThreadLocal<String>();
  private final Pattern p;
  private final String myself;

  public WebSpoonSecurityManager() {
    super();
    String path = System.getProperty( "user.home" ) + File.separator + ".(kettle|pentaho)" + File.separator + "users" + File.separator;
    p = Pattern.compile( "(file:)*" + path.replaceAll( "/", "\\/" ) );
    CodeSource src = WebSpoonSecurityManager.class.getProtectionDomain().getCodeSource();
    myself = src.getLocation().getPath();
  }

  @Override
  public void checkPermission( Permission perm, Object context ) {
    return;
  }

  @Override
  public void checkPermission( Permission perm ) {
    String userName = this.userName.get();
    if ( userName != null ) {
      if ( perm instanceof FilePermission ) {
        /*
         * As a work-around to the following issue
         * https://bugs.openjdk.java.net/browse/JDK-8166366
         */
        if ( perm.getName().equals( myself ) && perm.getActions().equals( "read" ) ) {
          return;
        }
        try {
          // See if the policy file grants permissions
          super.checkPermission( perm );
        } catch ( Exception e ) {
          // See if the access is to $HOME/.kettle/users/$username or $HOME/.pentaho/users/$username
          Matcher m = p.matcher( perm.getName() );
          if ( m.lookingAt() && m.replaceFirst( "" ).startsWith( userName ) ) {
            return;
          }
          throw new SecurityException( "access denied " + perm );
        }
      }
    }
  }

  public void setUserName( String userName ) {
    if ( this.userName.get() == null ) { // can be set only once
      this.userName.set( userName );
    }
  }

  public String getUserName() {
    return this.userName.get();
  }
}
