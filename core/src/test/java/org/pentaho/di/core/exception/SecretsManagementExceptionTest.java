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

package org.pentaho.di.core.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SecretsManagementExceptionTest {

  private String errorMessage = "Secret retrieval failed";
  private String causeExceptionMessage = "Network timeout";
  private Throwable cause = new RuntimeException( causeExceptionMessage );

  @Test
  public void testConstructorWithReasonAndMessage() {
    try {
      throw new SecretsManagementException( SecretsManagementException.Reason.UNAUTHORIZED, errorMessage );
    } catch ( SecretsManagementException e ) {
      assertEquals( "\n" + errorMessage + "\n", e.getMessage() );
      assertEquals( SecretsManagementException.Reason.UNAUTHORIZED, e.getReason() );
      assertNull( e.getCause() );
    }
  }

  @Test
  public void testConstructorWithReasonAndMessageNotFound() {
    try {
      throw new SecretsManagementException( SecretsManagementException.Reason.NOT_FOUND, errorMessage );
    } catch ( SecretsManagementException e ) {
      assertEquals( "\n" + errorMessage + "\n", e.getMessage() );
      assertEquals( SecretsManagementException.Reason.NOT_FOUND, e.getReason() );
      assertNull( e.getCause() );
    }
  }

  @Test
  public void testConstructorWithReasonAndMessageUnavailable() {
    try {
      throw new SecretsManagementException( SecretsManagementException.Reason.UNAVAILABLE, errorMessage );
    } catch ( SecretsManagementException e ) {
      assertEquals( "\n" + errorMessage + "\n", e.getMessage() );
      assertEquals( SecretsManagementException.Reason.UNAVAILABLE, e.getReason() );
      assertNull( e.getCause() );
    }
  }

  @Test
  public void testConstructorWithReasonAndMessageInvalidResponse() {
    try {
      throw new SecretsManagementException( SecretsManagementException.Reason.INVALID_RESPONSE, errorMessage );
    } catch ( SecretsManagementException e ) {
      assertEquals( "\n" + errorMessage + "\n", e.getMessage() );
      assertEquals( SecretsManagementException.Reason.INVALID_RESPONSE, e.getReason() );
      assertNull( e.getCause() );
    }
  }

  @Test
  public void testConstructorWithReasonMessageAndCause() {
    try {
      throw new SecretsManagementException( SecretsManagementException.Reason.UNAUTHORIZED, errorMessage, cause );
    } catch ( SecretsManagementException e ) {
      assertEquals( "\n" + errorMessage + "\n" + causeExceptionMessage + "\n", e.getMessage() );
      assertEquals( SecretsManagementException.Reason.UNAUTHORIZED, e.getReason() );
      assertEquals( cause, e.getCause() );
      assertEquals( causeExceptionMessage, e.getCause().getMessage() );
    }
  }

  @Test
  public void testConstructorWithReasonMessageAndCauseNotFound() {
    try {
      throw new SecretsManagementException( SecretsManagementException.Reason.NOT_FOUND, errorMessage, cause );
    } catch ( SecretsManagementException e ) {
      assertEquals( "\n" + errorMessage + "\n" + causeExceptionMessage + "\n", e.getMessage() );
      assertEquals( SecretsManagementException.Reason.NOT_FOUND, e.getReason() );
      assertEquals( cause, e.getCause() );
    }
  }

  @Test
  public void testConstructorWithReasonMessageAndCauseUnavailable() {
    try {
      throw new SecretsManagementException( SecretsManagementException.Reason.UNAVAILABLE, errorMessage, cause );
    } catch ( SecretsManagementException e ) {
      assertEquals( "\n" + errorMessage + "\n" + causeExceptionMessage + "\n", e.getMessage() );
      assertEquals( SecretsManagementException.Reason.UNAVAILABLE, e.getReason() );
      assertEquals( cause, e.getCause() );
    }
  }

  @Test
  public void testConstructorWithReasonMessageAndCauseInvalidResponse() {
    try {
      throw new SecretsManagementException( SecretsManagementException.Reason.INVALID_RESPONSE, errorMessage, cause );
    } catch ( SecretsManagementException e ) {
      assertEquals( "\n" + errorMessage + "\n" + causeExceptionMessage + "\n", e.getMessage() );
      assertEquals( SecretsManagementException.Reason.INVALID_RESPONSE, e.getReason() );
      assertEquals( cause, e.getCause() );
    }
  }

  @Test
  public void testReasonEnumValues() {
    assertEquals( 4, SecretsManagementException.Reason.values().length );
    assertNotNull( SecretsManagementException.Reason.UNAUTHORIZED );
    assertNotNull( SecretsManagementException.Reason.NOT_FOUND );
    assertNotNull( SecretsManagementException.Reason.UNAVAILABLE );
    assertNotNull( SecretsManagementException.Reason.INVALID_RESPONSE );
  }

  @Test
  public void testSerializability() {
    try {
      throw new SecretsManagementException( SecretsManagementException.Reason.UNAUTHORIZED, errorMessage );
    } catch ( SecretsManagementException e ) {
      assertEquals( true, e instanceof java.io.Serializable );
    }
  }

  @Test
  public void testIsKettleDatabaseException() {
    try {
      throw new SecretsManagementException( SecretsManagementException.Reason.UNAUTHORIZED, errorMessage );
    } catch ( KettleDatabaseException e ) {
      assertEquals( true, e instanceof SecretsManagementException );
    }
  }

  @Test
  public void testNullMessage() {
    try {
      throw new SecretsManagementException( SecretsManagementException.Reason.UNAUTHORIZED, null );
    } catch ( SecretsManagementException e ) {
      assertEquals( "\nnull\n", e.getMessage() );
      assertEquals( SecretsManagementException.Reason.UNAUTHORIZED, e.getReason() );
    }
  }

  @Test
  public void testEmptyMessage() {
    try {
      throw new SecretsManagementException( SecretsManagementException.Reason.NOT_FOUND, "" );
    } catch ( SecretsManagementException e ) {
      assertEquals( "\n\n", e.getMessage() );
      assertEquals( SecretsManagementException.Reason.NOT_FOUND, e.getReason() );
    }
  }

  @Test
  public void testMultipleReasons() {
    for ( SecretsManagementException.Reason reason : SecretsManagementException.Reason.values() ) {
      try {
        throw new SecretsManagementException( reason, "Test message for " + reason );
      } catch ( SecretsManagementException e ) {
        assertEquals( reason, e.getReason() );
        assertEquals( "\nTest message for " + reason + "\n", e.getMessage() );
      }
    }
  }
}
