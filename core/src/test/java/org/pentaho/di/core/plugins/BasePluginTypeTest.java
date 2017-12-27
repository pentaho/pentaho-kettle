package org.pentaho.di.core.plugins;

import org.junit.Test;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;

import java.io.InputStream;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;

public class BasePluginTypeTest {
  @Test
  public void testRegisterNativesCloseResAsStream() throws Exception {
    BasePluginType bpt = spy( DatabasePluginType.getInstance() );

    InputStream is = mock( InputStream.class );

    doReturn( is ).when( bpt ).getResAsStreamExternal( anyString() );
    doNothing().when( bpt ).registerPlugins( is );

    bpt.registerNatives();

    verify( is ).close();
  }

  @Test
  public void testRegisterNativesCloseFileInStream() throws Exception {
    BasePluginType bpt = spy( TwoWayPasswordEncoderPluginType.getInstance() );

    InputStream is = mock( InputStream.class );

    doReturn( "foo" ).when( bpt ).getPropertyExternal( anyString(), anyString() );
    doReturn( null ).when( bpt ).getResAsStreamExternal( anyString() );
    doReturn( is ).when( bpt ).getFileInputStreamExternal( anyString() );
    doNothing().when( bpt ).registerPlugins( is );

    bpt.registerNatives();

    verify( is ).close();
  }
}
