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
