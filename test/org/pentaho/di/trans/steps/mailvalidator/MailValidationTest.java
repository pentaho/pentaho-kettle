/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
package org.pentaho.di.trans.steps.mailvalidator;

import junit.framework.TestCase;

/**
 * Verify MailValidation properly differentiates between valid and invalid email addresses.
 * 
 * @author Jordan Ganoff (jganoff@pentaho.com)
 *
 */
public class MailValidationTest extends TestCase {
  public void testEmailValidation_simple_valid() {
    String email = "me@you.com"; //$NON-NLS-1$
    assertTrue(MailValidation.isRegExValid(email));
  }

  public void testEmailValidation_simple_caps_valid() {
    String email = "me@You.com"; //$NON-NLS-1$
    assertTrue(MailValidation.isRegExValid(email));
  }

  public void testEmailValidation_simple_invalid() {
    String email = "me@you"; //$NON-NLS-1$
    assertFalse(MailValidation.isRegExValid(email));
  }
  
  public void testEmailValidation_null() {
    String email = null;
    assertFalse(MailValidation.isRegExValid(email));
  }
  
  public void testEmailValidation_empty() {
    String email = "   "; //$NON-NLS-1$
    assertFalse(MailValidation.isRegExValid(email));
  }
}
