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

import org.junit.Test;
import org.pentaho.di.repository.RepositoryObjectType;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Yury Bakhmutski
 * @since 10-01-2015
 *
 */
public class LookupReferencesExceptionTest {

  @Test
  public void testObjectTypePairsToString() throws Exception {
    Exception cause = new NullPointerException();

    Map<String, RepositoryObjectType> notFoundedReferences = new LinkedHashMap<String, RepositoryObjectType>();
    String pathToTransStub = "/path/Trans.ktr";
    String pathToJobStub = "/path/Job.ktr";
    notFoundedReferences.put( pathToTransStub, RepositoryObjectType.TRANSFORMATION );
    notFoundedReferences.put( pathToJobStub, RepositoryObjectType.JOB );

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
