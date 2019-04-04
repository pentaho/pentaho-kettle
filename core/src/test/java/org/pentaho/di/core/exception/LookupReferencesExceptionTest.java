/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
