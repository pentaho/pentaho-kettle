/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.shared;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.exception.KettleException;

/**
 * SharedObjects tests
 * 
 * @author Yury Bakhmutski
 * @see SharedObjects
 */
public class SharedObjectsTest {

  @Mock
  SharedObjects sharedObjectsMock;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks( this );
  }

  @Test
  public void writeToFileTest() throws KettleException, IOException {
    doCallRealMethod().when( sharedObjectsMock ).writeToFile( any( FileObject.class ), anyString() );

    when( sharedObjectsMock.initOutputStreamUsingKettleVFS( any( FileObject.class ) ) ).thenThrow(
        new RuntimeException() );

    try {
      sharedObjectsMock.writeToFile( any( FileObject.class ), anyString() );
    } catch ( KettleException e ) {
      // NOP: catch block throws an KettleException after calling sharedObjectsMock method
    }

    // check if file restored in case of exception is occurred
    verify( sharedObjectsMock ).restoreFileFromBackup( anyString() );
  }

}
