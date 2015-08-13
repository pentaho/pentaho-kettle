package org.pentaho.di.trans.steps.newfileinput;

import java.io.FileInputStream;
import java.util.ArrayList;

import junit.framework.Assert;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.NoneCompressionProvider;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.steps.baseinput.IBaseInputStepControl;
import org.pentaho.di.trans.steps.newfileinput.NewFileInputMeta.Content;

public class NewFileInputReaderTest {
  private static final String FILE_COMPRESSION = "None";
  
  private static final String CSV_FILE_UTF8 = "PDI-11425-UTF8.csv";
  private static final String CSV_FILE_UTF8_WITH_BOM = "PDI-11425-UTF8-with-BOM.csv";
  
  private static final String CHECKED_STRING = "IDName;IDAA;GEBDAT;GESLACHT;IDAABA;LENGTE;GEWICHT";

  /**
   * Registration new plugin.
   */
  @BeforeClass
  public static void setup() throws KettlePluginException {
    TestPluggin plugin = Mockito.mock( TestPluggin.class );
    Mockito.when( plugin.loadClass( CompressionProvider.class ) ).thenReturn( new NoneCompressionProvider() );
    Mockito.when( plugin.getIds() ).thenReturn( new String[] { "Not null value" } );
    PluginRegistry.getInstance().registerPlugin( CompressionPluginType.class, plugin );
  }
  
  @Test
  public void testNewFileInputStreamUTF8WithBOMCreation() throws Exception {
    parseFileAndAssert( CSV_FILE_UTF8_WITH_BOM );
  }
  
  @Test
  public void testNewFileInputStreamUTF8Creation() throws Exception {
    parseFileAndAssert( CSV_FILE_UTF8 );
  }
  
  @SuppressWarnings("unused")
  private void parseFileAndAssert(String path) throws Exception {
    FileInputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream(
          NewFileInputReaderTest.class.getResource( path ).getFile() );
      IBaseInputStepControl step = Mockito.mock( IBaseInputStepControl.class );
      NewFileInputMeta meta = Mockito.mock( NewFileInputMeta.class );
      meta.content = Mockito.mock( Content.class );
      meta.content.fileCompression = FILE_COMPRESSION;
      NewFileInputData data = Mockito.mock( NewFileInputData.class );
      data.lineStringBuilder = new StringBuilder();
      data.lineBuffer = new ArrayList<>();
      data.filterProcessor = Mockito.mock( NewFileFilterProcessor.class );
      Mockito.when( data.filterProcessor.doFilters( Mockito.anyString() ) ).thenReturn( true );
      FileObject fileObject = Mockito.mock( FileObject.class );
      FileContent fileContent = Mockito.mock( FileContent.class );
      Mockito.when( fileContent.getInputStream() ).thenReturn( 
          fileInputStream );
      Mockito.when( fileObject.getContent() ).thenReturn( fileContent );
      LogChannelInterface log = Mockito.mock( LogChannelInterface.class );

      @SuppressWarnings("resource")
      NewFileInputReader newFileInputReader = new NewFileInputReader( step, meta, data, fileObject, log );

      Assert.assertEquals( data.lineBuffer.get( 0 ).line, CHECKED_STRING );
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      fileInputStream.close();
    }
  }
  
  private interface TestPluggin extends ClassLoadingPluginInterface, PluginInterface {
    
  }
}
