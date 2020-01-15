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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilePermission;
import java.net.SocketPermission;
import java.security.AllPermission;
import java.util.PropertyPermission;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WebSpoonSecurityManagerTest {
  WebSpoonSecurityManager securityManager;

  @Before
  public void setUp() {
    securityManager = new WebSpoonSecurityManager();
    System.setSecurityManager( securityManager );
  }

  @After
  public void teardown() {
    System.setSecurityManager( null );
  }

  @Test
  public void shouldNotThrowSecurityExceptionWhenCalledBySystem() {
    try {
      securityManager.checkPermission( new FilePermission( "<<ALL FILES>>", "read,write,delete" ) );
      securityManager.checkPermission( new FilePermission( System.getProperty( "user.home" ), "read,write,delete" ) );
      securityManager.checkPermission( new FilePermission( System.getProperty( "java.home" ), "read,write,delete" ) );
      securityManager.checkPermission( new RuntimePermission( "shutdownHooks" ) );
      securityManager.checkPermission( new PropertyPermission( "java.home", "read" ) );
      securityManager.checkPermission( new SocketPermission( "example.com", "connect" ) );
      securityManager.checkPermission( new AllPermission() );
    } catch ( SecurityException e ) {
      fail( e.getMessage() );
    }
  }

  @Test
  public void shouldNotThrowSecurityExceptionWhenNotFilePermission() {
    try {
      securityManager.setUserName( "user" );
      securityManager.checkPermission( new RuntimePermission( "shutdownHooks" ) );
      securityManager.checkPermission( new PropertyPermission( "java.home", "read" ) );
      securityManager.checkPermission( new SocketPermission( "example.com", "connect" ) );
    } catch ( SecurityException e ) {
      fail( e.getMessage() );
    }
  }

  @Test
  public void shouldNotThrowSecurityExceptionForUserData() {
    try {
      securityManager.setUserName( "user" );
      securityManager.checkPermission(
          new FilePermission( System.getProperty( "user.home" ) + File.separator + ".kettle" + File.separator + "users" + File.separator + "user",
              "read,write,delete" ) );
      securityManager.checkPermission(
          new FilePermission( System.getProperty( "user.home" ) + File.separator + ".pentaho" + File.separator + "users" + File.separator + "user",
              "read,write,delete" ) );
    } catch ( SecurityException e ) {
      fail( e.getMessage() );
    }
  }

  @Test( expected = SecurityException.class )
  public void shouldThrowSecurityExceptionForOutsideUserData() {
    securityManager.setUserName( "user" );
    securityManager.checkPermission( new FilePermission( "<<ALL FILES>>", "read" ) );
  }
}
