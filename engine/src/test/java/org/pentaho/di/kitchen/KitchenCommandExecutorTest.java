/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.kitchen;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.Job;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KitchenCommandExecutorTest {

  private MockedKitchenCommandExecutor mockedKitchenCommandExecutor;

  @Before
  public void setUp() throws Exception {

    mockedKitchenCommandExecutor = mock( MockedKitchenCommandExecutor.class );

    // call real methods for loadTransFromFilesystem(), loadTransFromRepository();
    when( mockedKitchenCommandExecutor.loadJobFromFilesystem( anyString(), anyString(), anyString() ) ).thenCallRealMethod();
    when( mockedKitchenCommandExecutor.loadJobFromRepository( anyObject(), anyString(), anyString() ) ).thenCallRealMethod();
    when( mockedKitchenCommandExecutor.decodeBase64StringToFile( anyString(), anyString() ) ).thenCallRealMethod();
  }

  @After
  public void tearDown() {
    mockedKitchenCommandExecutor = null;
  }

  @Test
  public void testFilesystemBase64Zip() throws Exception {
    String fileName = "hello-world.kjb";
    File zipFile = new File( getClass().getResource( "testKjbArchive.zip" ).toURI() );
    String base64Zip = Base64.getEncoder().encodeToString( FileUtils.readFileToByteArray( zipFile ) );
    Job job = mockedKitchenCommandExecutor.loadJobFromFilesystem( Const.getDIHomeDirectory(), fileName, base64Zip );
    assertNotNull( job );
  }

  /**
   * Inner class, used solely for the purpose of raising methods visibility ( so that we are able to test those )
   */
  private class MockedKitchenCommandExecutor extends KitchenCommandExecutor {

    public MockedKitchenCommandExecutor( Class<?> pkgClazz ) {
      super( pkgClazz );
    }

    @Override
    public File decodeBase64StringToFile( String base64String, String filePath ) throws IOException {
      return super.decodeBase64StringToFile( base64String, filePath );
    }
  }
}
