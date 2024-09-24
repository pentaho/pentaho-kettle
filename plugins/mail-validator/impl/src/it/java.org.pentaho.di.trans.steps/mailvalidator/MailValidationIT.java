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
