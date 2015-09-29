package org.pentaho.di.core.exception;

import junit.framework.TestCase;
import org.pentaho.di.repository.RepositoryObjectType;

/**
 * Created by Yury_Bakhmutski on 10/2/2015.
 */
public class IdNotFoundExceptionTest extends TestCase {

  private String message = "messageStub";
  String expectedNullMessage = System.lineSeparator() + "null" + System.lineSeparator();
  private String causeExceptionMessage = "Cause Exception";
  private Throwable cause = new RuntimeException( causeExceptionMessage );
  private String objectName = "Trans.ktr";
  private String pathToObject = "/pathToTrans/";
  private RepositoryObjectType objectType = RepositoryObjectType.TRANSFORMATION;

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

  public void testConstructor3() {
    try {
      throw new IdNotFoundException( cause, objectName, pathToObject, objectType );
    } catch ( IdNotFoundException testedException ) {
      Throwable cause = testedException.getCause();
      assertEquals( this.cause, testedException.getCause() );
      //check that cause exception's message is not lost
      assertTrue( testedException.getMessage().contains( causeExceptionMessage ) );
      assertEquals( objectName, testedException.getObjectName() );
      assertEquals( pathToObject, testedException.getPathToObject() );
      assertEquals( objectType, testedException.getObjectType() );
    }
  }

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
