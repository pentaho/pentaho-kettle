/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.excelwriter;

import com.google.common.io.Files;
import org.apache.commons.vfs2.FileObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.utils.TestUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ExcelWriterStepTest {

  private static final String SHEET_NAME = "Sheet1";

  private HSSFWorkbook wb;
  private StepMockHelper<ExcelWriterStepMeta, ExcelWriterStepData> mockHelper;
  private ExcelWriterStep step;

  private ExcelWriterStepMeta stepMeta;
  private ExcelWriterStepMeta metaMock;
  private ExcelWriterStepData dataMock;

  @Before
  public void setUp() throws Exception {
    String path = TestUtils.createRamFile( getClass().getSimpleName() + "/testXLSProtect.xls" );
    FileObject xlsFile = TestUtils.getFileObject( path );
    wb = createWorkbook( xlsFile );
    mockHelper =
      new StepMockHelper<ExcelWriterStepMeta, ExcelWriterStepData>(
        "Excel Writer Test", ExcelWriterStepMeta.class, ExcelWriterStepData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        mockHelper.logChannelInterface );
    step = spy( new ExcelWriterStep(
        mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans ) );

    stepMeta = new ExcelWriterStepMeta();
    metaMock = mock( ExcelWriterStepMeta.class );
    dataMock = mock( ExcelWriterStepData.class );
  }

  @Test
  public void testProtectSheet() throws Exception {
    step.protectSheet( wb.getSheet( SHEET_NAME ), "aa" );
    assertTrue( wb.getSheet( SHEET_NAME ).getProtect() );
  }

  @Test
  public void testMaxSheetNameLength() throws Exception {
    PrintStream err = System.err;
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      System.setErr( new PrintStream( baos ) );

      when( mockHelper.initStepMetaInterface.getSheetname() )
        .thenReturn( "12345678901234567890123456789012" ); // 32 character
      step.init( mockHelper.initStepMetaInterface, mockHelper.initStepDataInterface );
      try {
        step.prepareNextOutputFile();
        fail();
      } catch ( KettleException expected ) {
        String content = expected.getMessage();
        if ( !content.contains( "12345678901234567890123456789012" ) ) {
          fail();
          //CHECKSTYLE IGNORE EmptyBlock FOR NEXT 3 LINES
        } else {
          // We expected this error message, the sheet name is too long for Excel
        }
      }
    } finally {
      System.setErr( err );
    }
  }

  @Test
  public void testTopLevelMetadataEntries() {

    try {
      List<StepInjectionMetaEntry> entries =
          stepMeta.getStepMetaInjectionInterface().getStepInjectionMetadataEntries();

      String masterKeys = "FIELDS";

      for ( StepInjectionMetaEntry entry : entries ) {
        String key = entry.getKey();
        assertTrue( masterKeys.contains( key ) );
        masterKeys = masterKeys.replace( key, "" );

      }

      assertTrue( masterKeys.trim().length() == 0 );

    } catch ( KettleException e ) {
      fail( e.getMessage() );
    }

  }

  @Test
  public void testChildLevelMetadataEntries() {

    try {
      List<StepInjectionMetaEntry> entries =
          stepMeta.getStepMetaInjectionInterface().getStepInjectionMetadataEntries();

      String childKeys = "NAME TYPE FORMAT STYLECELL FIELDTITLE TITLESTYLE "
          + "FORMULA HYPERLINKFIELD CELLCOMMENT COMMENTAUTHOR";

      StepInjectionMetaEntry mappingEntry = null;

      for ( StepInjectionMetaEntry entry : entries ) {
        String key = entry.getKey();
        if ( key.equals( "FIELDS" ) ) {
          mappingEntry = entry;
          break;
        }
      }

      assertNotNull( mappingEntry );

      List<StepInjectionMetaEntry> fieldAttributes = mappingEntry.getDetails().get( 0 ).getDetails();

      for ( StepInjectionMetaEntry attribute : fieldAttributes ) {
        String key = attribute.getKey();
        assertTrue( childKeys.contains( key ) );
        childKeys = childKeys.replace( key, "" );

      }

      assertTrue( childKeys.trim().length() == 0 );

    } catch ( KettleException e ) {
      fail( e.getMessage() );
    }

  }

  @Test
  public void testInjection() {

    ExcelWriterStepMeta meta = new ExcelWriterStepMeta();

    try {
      List<StepInjectionMetaEntry> entries =
          stepMeta.getStepMetaInjectionInterface().getStepInjectionMetadataEntries();

      for ( StepInjectionMetaEntry entry : entries ) {
        switch ( entry.getValueType() ) {
          case ValueMetaInterface.TYPE_STRING:
            entry.setValue( "new_".concat( entry.getKey() ) );
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            entry.setValue( Boolean.TRUE );
            break;
          default:
            break;
        }

        if ( !entry.getDetails().isEmpty() ) {

          List<StepInjectionMetaEntry> childEntries = entry.getDetails().get( 0 ).getDetails();
          for ( StepInjectionMetaEntry childEntry : childEntries ) {
            switch ( childEntry.getValueType() ) {
              case ValueMetaInterface.TYPE_STRING:
                childEntry.setValue( "new_".concat( childEntry.getKey() ) );
                break;
              case ValueMetaInterface.TYPE_BOOLEAN:
                childEntry.setValue( Boolean.TRUE );
                break;
              default:
                break;
            }
          }
        }
      }

      stepMeta.getStepMetaInjectionInterface().injectStepMetadataEntries( entries );

      assertEquals( "Cell comment not properly injected... ", "new_CELLCOMMENT",
          stepMeta.getOutputFields()[ 0 ].getCommentField() );
      assertEquals( "Format not properly injected... ", "new_FORMAT", stepMeta.getOutputFields()[ 0 ].getFormat() );
      assertEquals( "Hyperlink not properly injected... ", "new_HYPERLINKFIELD",
          stepMeta.getOutputFields()[ 0 ].getHyperlinkField() );
      assertEquals( "Name not properly injected... ", "new_NAME", stepMeta.getOutputFields()[ 0 ].getName() );
      assertEquals( "Style cell not properly injected... ", "new_STYLECELL",
          stepMeta.getOutputFields()[ 0 ].getStyleCell() );
      assertEquals( "Title not properly injected... ", "new_FIELDTITLE", stepMeta.getOutputFields()[ 0 ].getTitle() );
      assertEquals( "Title style cell not properly injected... ", "new_TITLESTYLE",
          stepMeta.getOutputFields()[ 0 ].getTitleStyleCell() );
      assertEquals( "Type not properly injected... ", 0, stepMeta.getOutputFields()[ 0 ].getType() );
      assertEquals( "Comment author not properly injected... ", "new_COMMENTAUTHOR",
          stepMeta.getOutputFields()[ 0 ].getCommentAuthorField() );

    } catch ( KettleException e ) {
      fail( e.getMessage() );
    }

  }

  @Test
  public void testPrepareNextOutputFile() throws Exception {
    assertTrue( step.init( metaMock, dataMock ) );
    File outDir = Files.createTempDir();
    String testFileOut = outDir.getAbsolutePath() + File.separator + "test.xlsx";
    when( step.buildFilename( 0 ) ).thenReturn( testFileOut );
    when( metaMock.isTemplateEnabled() ).thenReturn( true );
    when( metaMock.isStreamingData() ).thenReturn( true );
    when( metaMock.isHeaderEnabled() ).thenReturn( true );
    when( metaMock.getExtension() ).thenReturn( "xlsx" );
    dataMock.createNewFile = true;
    dataMock.realTemplateFileName = getClass().getResource( "template_test.xlsx" ).getFile();
    dataMock.realSheetname = "Sheet1";
    step.prepareNextOutputFile();
  }

  private HSSFWorkbook createWorkbook( FileObject file ) throws Exception {
    HSSFWorkbook wb = null;
    OutputStream os = null;
    try {
      os = file.getContent().getOutputStream();
      wb = new HSSFWorkbook();
      wb.createSheet( SHEET_NAME );
      wb.write( os );
    } finally {
      os.flush();
      os.close();
    }
    return wb;
  }

}
