/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.mailvalidator;

import junit.framework.TestCase;

/**
 * Verify MailValidation properly differentiates between valid and invalid email addresses.
 *
 * @author Jordan Ganoff (jganoff@pentaho.com)
 *
 */
public class MailValidationIT extends TestCase {
  public void testEmailValidation_simple_valid() {
    String email = "me@you.com";
    assertTrue( MailValidation.isRegExValid( email ) );
  }

  public void testEmailValidation_simple_caps_valid() {
    String email = "me@You.com";
    assertTrue( MailValidation.isRegExValid( email ) );
  }

  public void testEmailValidation_simple_invalid() {
    String email = "me@you";
    assertFalse( MailValidation.isRegExValid( email ) );
  }

  public void testEmailValidation_null() {
    String email = null;
    assertFalse( MailValidation.isRegExValid( email ) );
  }

  public void testEmailValidation_empty() {
    String email = "   ";
    assertFalse( MailValidation.isRegExValid( email ) );
  }
}
