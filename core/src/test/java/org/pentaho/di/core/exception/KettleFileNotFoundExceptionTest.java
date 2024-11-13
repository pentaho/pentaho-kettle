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
import org.pentaho.di.core.Const;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KettleFileNotFoundExceptionTest {

  private String expectedNullMessage = Const.CR + "null" + Const.CR;
  private String errorMessage = "error message";
  private String causeExceptionMessage = "Cause exception";
  private String filepath = "file.txt";
  private Throwable cause = new RuntimeException( causeExceptionMessage );

  @Test
  public void testConstructor() {
    try {
      throw new KettleFileNotFoundException();
    } catch ( KettleFileNotFoundException e ) {
      assertEquals( null, e.getCause() );
      assertTrue( e.getMessage().contains( expectedNullMessage ) );
      assertEquals( null, e.getFilepath() );
    }
  }

  @Test
  public void testConstructorMessage() {
    try {
      throw new KettleFileNotFoundException( errorMessage );
    } catch ( KettleFileNotFoundException e ) {
      assertEquals( null, e.getCause() );
      assertTrue( e.getMessage().contains( errorMessage ) );
      assertEquals( null, e.getFilepath() );
    }
  }

  @Test
  public void testConstructorMessageAndFilepath() {
    try {
      throw new KettleFileNotFoundException( errorMessage, filepath );
    } catch ( KettleFileNotFoundException e ) {
      assertEquals( null, e.getCause() );
      assertTrue( e.getMessage().contains( errorMessage ) );
      assertEquals( filepath, e.getFilepath() );
    }
  }

  @Test
  public void testConstructorThrowable() {
    try {
      throw new KettleFileNotFoundException( cause );
    } catch ( KettleFileNotFoundException e ) {
      assertEquals( cause, e.getCause() );
      assertTrue( e.getMessage().contains( causeExceptionMessage ) );
      assertEquals( null, e.getFilepath() );
    }
  }

  @Test
  public void testConstructorMessageAndThrowable() {
    Throwable cause = new RuntimeException( causeExceptionMessage );
    try {
      throw new KettleFileNotFoundException( errorMessage, cause );
    } catch ( KettleFileNotFoundException e ) {
      assertTrue( e.getMessage().contains( errorMessage ) );
      assertTrue( e.getMessage().contains( causeExceptionMessage ) );
      assertEquals( cause, e.getCause() );
      assertEquals( null, e.getFilepath() );
    }
  }
}
