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


package org.pentaho.di.core.exception;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;
import org.pentaho.di.repository.RepositoryObjectType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Yury Bakhmutski
 * @since 02-10-2015
 */
public class IdNotFoundExceptionTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  private String message = "messageStub";
  String expectedNullMessage = System.lineSeparator() + "null" + System.lineSeparator();
  private String causeExceptionMessage = "Cause Exception";
  private Throwable cause = new RuntimeException( causeExceptionMessage );
  private String objectName = "Trans.ktr";
  private String pathToObject = "/pathToTrans/";
  private RepositoryObjectType objectType = RepositoryObjectType.TRANSFORMATION;

  @Test
  public void testConstructor1() {
    try {
      throw new IdNotFoundException( objectName, pathToObject, objectType );
    } catch ( IdNotFoundException testedException ) {
      assertEquals( expectedNullMessage, testedException.getMessage() );
      assertEquals( null, testedException.getCause() );
      assertEquals( objectName, testedException.getObjectName() );
      assertEquals( pathToObject, testedException.getPathToObject() );
      assertEquals( objectType, testedException.getObjectType() );
    }
  }

  @Test
  public void testConstructor2() {
    try {
      throw new IdNotFoundException( message, objectName, pathToObject, objectType );
    } catch ( IdNotFoundException testedException ) {
      assertTrue( testedException.getMessage().contains( message ) );
      assertEquals( null, testedException.getCause() );
      assertEquals( objectName, testedException.getObjectName() );
      assertEquals( pathToObject, testedException.getPathToObject() );
      assertEquals( objectType, testedException.getObjectType() );
    }
  }

  @Test
  public void testConstructor3() {
    try {
      throw new IdNotFoundException( cause, objectName, pathToObject, objectType );
    } catch ( IdNotFoundException testedException ) {
      assertEquals( this.cause, testedException.getCause() );
      //check that cause exception's message is not lost
      assertTrue( testedException.getMessage().contains( causeExceptionMessage ) );
      assertEquals( objectName, testedException.getObjectName() );
      assertEquals( pathToObject, testedException.getPathToObject() );
      assertEquals( objectType, testedException.getObjectType() );
    }
  }

  @Test
  public void testConstructor4() {
    try {
      throw new IdNotFoundException( message, cause, objectName, pathToObject, objectType );
    } catch ( IdNotFoundException testedException ) {
      //check that message is not lost
      assertTrue( testedException.getMessage().contains( message ) );
      //check that cause exception's message is not lost
      assertTrue( testedException.getMessage().contains( causeExceptionMessage ) );
      assertEquals( cause, testedException.getCause() );
      assertEquals( objectName, testedException.getObjectName() );
      assertEquals( pathToObject, testedException.getPathToObject() );
      assertEquals( objectType, testedException.getObjectType() );
    }
  }
}
