package org.pentaho.di.core.exception;

import org.junit.Test;
import org.pentaho.di.repository.RepositoryObjectType;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Yury_Bakhmutski on 10/1/2015.
 */
public class LookupReferencesExceptionTest {

  @Test
  public void testObjectTypePairsToString() throws Exception {
    Exception cause = new NullPointerException();

    Map<String, RepositoryObjectType> notFoundedReferences = new HashMap<String, RepositoryObjectType>();
    String pathToJobStub = "/path/Job.ktr";
    String pathToTransStub = "/path/Trans.ktr";
    notFoundedReferences.put( pathToJobStub, RepositoryObjectType.JOB );
    notFoundedReferences.put( pathToTransStub, RepositoryObjectType.TRANSFORMATION );

    String expectedOutput =
        System.lineSeparator() + "\"/path/Trans.ktr\" [transformation] " + System.lineSeparator()
            + "\"/path/Job.ktr\" [job] ";
    try {
      throw new LookupReferencesException( cause, notFoundedReferences );
    } catch ( LookupReferencesException testedException ) {
      String actual = testedException.objectTypePairsToString();
      assertEquals( expectedOutput, actual );
      //check that cause exception was set
      assertNotNull( testedException.getCause() );
    }
  }
}
