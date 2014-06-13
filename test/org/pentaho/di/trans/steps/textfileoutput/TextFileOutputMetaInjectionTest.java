package org.pentaho.di.trans.steps.textfileoutput;

import junit.framework.TestCase;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMetaInjection;

import java.util.List;

public class TextFileOutputMetaInjectionTest extends TestCase {

  public static String FILENAME = "${internal.transformation.filename.directory}/file";
  public static boolean RUN_AS_COMMAND = true;
  public static boolean PASS_TO_SERVLET = true;
  public static boolean CREATE_PARENT_FOLDER = true;
  public static boolean FILENAME_IN_FIELD = true;
  public static String FILENAME_FIELD = "filename";
  public static String EXTENSION = "csv";
  public static boolean INC_STEPNR_IN_FILENAME = true;
  public static boolean INC_PARTNR_IN_FILENAME = true;
  public static boolean INC_DATE_IN_FILENAME = true;
  public static boolean INC_TIME_IN_FILENAME = true;
  public static boolean SPECIFY_DATE_FORMAT = true;
  public static String DATE_FORMAT = "yyyyMMddHHmmss";
  public static boolean ADD_TO_RESULT = true;

  public static boolean APPEND = true;
  public static String SEPARATOR = "|";
  public static String ENCLOSURE = "'";
  public static boolean FORCE_ENCLOSURE = true;
  public static boolean DISABLE_ENCLOSURE_FIX = true;
  public static boolean HEADER = true;
  public static boolean FOOTER = true;
  public static String FORMAT = "None";
  public static String COMPRESSION = "GZip";
  public static String ENCODING = "UTF-8";
  public static boolean RIGHT_PAD_FIELDS = true;
  public static boolean FAST_DATA_DUMP = true;
  public static int SPLIT_EVERY = 8;
  public static String ADD_ENDING_LINE = "The ended line.";

  public static int NR_FIELDS = 10;

  public static String OUTPUT_FIELDNAME = "inField";
  public static String OUTPUT_TYPE = "Integer";
  public static String OUTPUT_FORMAT = "#,###";
  public static int OUTPUT_LENGTH = 5;
  public static int OUTPUT_PRECISION = 0;
  public static String OUTPUT_CURRENCY = "$";
  public static String OUTPUT_DECIMAL = ",";
  public static String OUTPUT_GROUP = ".";
  public static String OUTPUT_TRIM = "both";
  public static String OUTPUT_NULL = "null";

  public void testInjectionExtraction() throws Exception {

    // Test Strategy :
    //
    // Populate a new object, extract the metadata,
    // then inject into another set of metadata, compare the results.
    //
    TextFileOutputMeta meta = populateTextFileOutputMeta();

    List<StepInjectionMetaEntry> entries = meta.extractStepMetadataEntries();

    assertEquals( 29, entries.size() );

    TextFileOutputMeta newMeta = new TextFileOutputMeta();
    newMeta.getStepMetaInjectionInterface().injectStepMetadataEntries( entries );

    // Automatic compare
    //
    List<StepInjectionMetaEntry> cmpEntries = newMeta.extractStepMetadataEntries();
    StepInjectionUtil.compareEntryValues( entries, cmpEntries );
  }

  public void testInjectionEntries() throws Exception {
    TextFileOutputMeta meta = populateTextFileOutputMeta();
    List<StepInjectionMetaEntry> entries = meta.getStepMetaInjectionInterface().getStepInjectionMetadataEntries();
    assertEquals( 29, entries.size() );

    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.FILENAME ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.RUN_AS_COMMAND ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.PASS_TO_SERVLET ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.CREATE_PARENT_FOLDER ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.FILENAME_IN_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.FILENAME_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.EXTENSION ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.INC_STEPNR_IN_FILENAME ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.INC_PARTNR_IN_FILENAME ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.INC_DATE_IN_FILENAME ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.INC_TIME_IN_FILENAME ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.SPECIFY_DATE_FORMAT ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.DATE_FORMAT ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.ADD_TO_RESULT ) );

    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.APPEND ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.SEPARATOR ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.ENCLOSURE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.FORCE_ENCLOSURE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.DISABLE_ENCLOSURE_FIX ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.HEADER ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.FOOTER ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.FORMAT ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.COMPRESSION ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.ENCODING ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.RIGHT_PAD_FIELDS ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.FAST_DATA_DUMP ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.SPLIT_EVERY ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, TextFileOutputMetaInjection.Entry.ADD_ENDING_LINE ) );

    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.findEntry( entries,
      TextFileOutputMetaInjection.Entry.OUTPUT_FIELDS );
    assertNotNull( fieldsEntry );
    StepInjectionMetaEntry fieldEntry = StepInjectionUtil.findEntry( fieldsEntry.getDetails(),
      TextFileOutputMetaInjection.Entry.OUTPUT_FIELD );
    assertNotNull( fieldEntry );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      TextFileOutputMetaInjection.Entry.OUTPUT_FIELDNAME ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      TextFileOutputMetaInjection.Entry.OUTPUT_TYPE ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      TextFileOutputMetaInjection.Entry.OUTPUT_FORMAT ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      TextFileOutputMetaInjection.Entry.OUTPUT_LENGTH ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      TextFileOutputMetaInjection.Entry.OUTPUT_PRECISION ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      TextFileOutputMetaInjection.Entry.OUTPUT_CURRENCY ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      TextFileOutputMetaInjection.Entry.OUTPUT_DECIMAL ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      TextFileOutputMetaInjection.Entry.OUTPUT_GROUP ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      TextFileOutputMetaInjection.Entry.OUTPUT_TRIM ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      TextFileOutputMetaInjection.Entry.OUTPUT_NULL ) );

  }

  private TextFileOutputMeta populateTextFileOutputMeta() {
    TextFileOutputMeta meta = new TextFileOutputMeta();
    meta.allocate( NR_FIELDS );

    meta.setFileName( FILENAME );
    meta.setFileAsCommand( RUN_AS_COMMAND );
    meta.setServletOutput( PASS_TO_SERVLET );
    meta.setCreateParentFolder( CREATE_PARENT_FOLDER );
    meta.setFileNameInField( FILENAME_IN_FIELD );
    meta.setFileNameField( FILENAME_FIELD );
    meta.setExtension( EXTENSION );
    meta.setDateInFilename( INC_DATE_IN_FILENAME );
    meta.setTimeInFilename( INC_TIME_IN_FILENAME );
    meta.setStepNrInFilename( INC_STEPNR_IN_FILENAME );
    meta.setPartNrInFilename( INC_PARTNR_IN_FILENAME );
    meta.setSpecifyingFormat( SPECIFY_DATE_FORMAT );
    meta.setDateTimeFormat( DATE_FORMAT );
    meta.setAddToResultFiles( ADD_TO_RESULT );

    meta.setFileAppended( APPEND );
    meta.setSeparator( SEPARATOR );
    meta.setEnclosure( ENCLOSURE );
    meta.setEnclosureForced( FORCE_ENCLOSURE );
    meta.setEnclosureFixDisabled( DISABLE_ENCLOSURE_FIX );
    meta.setHeaderEnabled( HEADER );
    meta.setFooterEnabled( FOOTER );
    meta.setFileFormat( FORMAT );
    meta.setFileCompression( COMPRESSION );
    meta.setEncoding( ENCODING );
    meta.setPadded( RIGHT_PAD_FIELDS );
    meta.setFastDump( FAST_DATA_DUMP );
    meta.setSplitEvery( SPLIT_EVERY );
    meta.setEndedLine( ADD_ENDING_LINE );

    // CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < NR_FIELDS; i++ ) {
      meta.getOutputFields()[i] = new TextFileField();
      meta.getOutputFields()[i].setName( OUTPUT_FIELDNAME + i );
      meta.getOutputFields()[i].setType( OUTPUT_TYPE );
      meta.getOutputFields()[i].setFormat( OUTPUT_FORMAT );
      meta.getOutputFields()[i].setLength( OUTPUT_LENGTH );
      meta.getOutputFields()[i].setPrecision( OUTPUT_PRECISION );
      meta.getOutputFields()[i].setCurrencySymbol( OUTPUT_CURRENCY );
      meta.getOutputFields()[i].setDecimalSymbol( OUTPUT_DECIMAL );
      meta.getOutputFields()[i].setGroupingSymbol( OUTPUT_GROUP );
      meta.getOutputFields()[i].setTrimType( ValueMeta.getTrimTypeByDesc( OUTPUT_TRIM ) );
      meta.getOutputFields()[i].setNullString( OUTPUT_NULL );
    }

    return meta;
  }

}
