/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.core.util;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;

public class UtilsTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Test
  public void testResolvePassword() {
    String password = "password";
    // is supposed the password stays the same
    Assert.assertSame( password, Utils.resolvePassword( Variables.getADefaultVariableSpace(), password ).intern() );
  }

  @Test
  public void testResolvePasswordEncrypted() {
    String decPassword = "password";
    // is supposed encrypted with Encr.bat util
    String encPassword = "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde";
    Assert.assertSame( decPassword,
        Utils.resolvePassword( Variables.getADefaultVariableSpace(), encPassword ).intern() );
  }

  @Test
  public void testResolvePasswordNull() {
    String password = null;
    // null is valid input parameter
    Assert.assertSame( password, Utils.resolvePassword( Variables.getADefaultVariableSpace(), password ) );
  }

  @Test
  public void testResolvePasswordVariable() {
    String passwordKey = "PASS_VAR";
    String passwordVar = "${" + passwordKey + "}";
    String passwordValue = "password";
    Variables vars = new Variables();
    vars.setVariable( passwordKey, passwordValue );
    //resolvePassword gets variable
    Assert.assertSame( passwordValue, Utils.resolvePassword( vars, passwordVar ).intern() );
  }

}
