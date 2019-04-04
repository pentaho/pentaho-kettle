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

package org.pentaho.di.trans.steps.googleanalytics;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Pavel Sakun
 */
@RunWith( Parameterized.class )
public class GoogleAnalyticsApiFacadeTest {

  @Parameterized.Parameters
  public static List<Object[]> primeNumbers() {
    return Arrays.asList(
      new Object[] { "C:/key.p12", FileNotFoundException.class },
      new Object[] { "C:\\key.p12", FileNotFoundException.class },
      new Object[] { "/key.p12", FileNotFoundException.class },
      new Object[] { "file:///C:/key.p12", FileNotFoundException.class },
      new Object[] { "file:///C:\\key.p12", FileNotFoundException.class },
      // KettleFileException on Windows, FileNotFoundException on Ubuntu
      new Object[] { "file:///key.p12", Exception.class }
    );
  }

  @Rule
  public final ExpectedException expectedException;

  private final String path;

  public GoogleAnalyticsApiFacadeTest( String path, Class<Exception> expectedExceptionClass ) {
    this.path = path;

    this.expectedException = ExpectedException.none();
    this.expectedException.expect( expectedExceptionClass );
  }

  @Test
  public void exceptionIsThrowsForNonExistingFiles() throws Exception {
    GoogleAnalyticsApiFacade.createFor( "application-name", "account", path );
  }
}
