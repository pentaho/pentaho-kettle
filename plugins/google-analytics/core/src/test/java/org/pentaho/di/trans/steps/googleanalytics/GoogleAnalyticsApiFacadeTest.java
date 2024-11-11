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
