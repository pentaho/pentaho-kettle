package org.pentaho.di.trans.steps.dimensionlookup;

import junit.framework.TestCase;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;

import java.util.List;

public class DimensionLookupMetaInjectionTest extends TestCase {

  public static boolean UPDATE_DIMENSION = true;
  public static String SCHEMA_NAME = "schema";
  public static String TABLE_NAME = "${TABLE_NAME}";
  public static int COMMIT_SIZE = 1000;
  public static boolean PRELOAD_CACHE = true;
  public static int CACHE_SIZE = 1;

  public static String TECHNICAL_KEY_FIELD = "tech_key";
  public static String TECHNICAL_KEY_NEW_NAME = "new_tech_key";
  public static String TECHNICAL_KEY_CREATION = "sequence";
  public static String TECHNICAL_KEY_SEQUENCE = "db_sequence";
  public static String VERSION_FIELD = "the_version";
  public static String DATE_FIELD = "the_date";
  public static String DATE_RANGE_START_FIELD = "start_date";
  public static int MIN_YEAR = 2000;
  public static boolean USE_ALTERNATIVE_START_DATE = true;
  public static int ALTERNATIVE_START_OPTION = DimensionLookupMeta.START_DATE_ALTERNATIVE_COLUMN_VALUE;
  public static String ALTERNATIVE_START_COLUMN = "alternative_date";
  public static String DATE_RANGE_END_FIELD = "end_date";
  public static int MAX_YEAR = 2019;

  public static int NR_FIELDS = 10;

  public static String DATABASE_FIELDNAME = "dbField";
  public static String STREAM_FIELDNAME = "inField";
  public static int UPDATE_TYPE = DimensionLookupMeta.TYPE_UPDATE_LAST_VERSION;

  public static String COMPARE_DATABASE_FIELD = "keyDbField";
  public static String COMPARE_STREAM_FIELD = "keyInField";

  public void testInjectionExtraction() throws Exception {

    // Test Strategy :
    //
    // Populate a new object, extract the metadata,
    // then inject into another set of metadata, compare the results.
    //
    DimensionLookupMeta meta = populateDimensionLookupMeta();

    List<StepInjectionMetaEntry> entries = meta.extractStepMetadataEntries();

    assertEquals( 21, entries.size() );

    DimensionLookupMeta newMeta = new DimensionLookupMeta();
    newMeta.getStepMetaInjectionInterface().injectStepMetadataEntries( entries );

    // Automatic compare
    //
    List<StepInjectionMetaEntry> cmpEntries = newMeta.extractStepMetadataEntries();
    StepInjectionUtil.compareEntryValues( entries, cmpEntries );
  }

  public void testInjectionEntries() throws Exception {
    DimensionLookupMeta meta = populateDimensionLookupMeta();
    List<StepInjectionMetaEntry> entries = meta.getStepMetaInjectionInterface().getStepInjectionMetadataEntries();
    assertEquals( 21, entries.size() );

    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.UPDATE_DIMENSION ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.TARGET_SCHEMA ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.TARGET_TABLE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.COMMIT_SIZE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.PRELOAD_CACHE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.CACHE_SIZE ) );

    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.TECHNICAL_KEY_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.TECHNICAL_KEY_NEW_NAME ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.TECHNICAL_KEY_CREATION ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.TECHNICAL_KEY_SEQUENCE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.VERSION_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.STREAM_DATE_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.DATE_RANGE_START_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.MIN_YEAR ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.USE_ALTERNATIVE_START_DATE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.ALTERNATIVE_START_OPTION ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.ALTERNATIVE_START_COLUMN ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.DATE_RANGE_END_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, DimensionLookupMetaInjection.Entry.MAX_YEAR ) );


    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.findEntry( entries,
      DimensionLookupMetaInjection.Entry.DATABASE_FIELDS );
    assertNotNull( fieldsEntry );
    StepInjectionMetaEntry fieldEntry = StepInjectionUtil.findEntry( fieldsEntry.getDetails(),
      DimensionLookupMetaInjection.Entry.DATABASE_FIELD );
    assertNotNull( fieldEntry );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      DimensionLookupMetaInjection.Entry.DATABASE_FIELDNAME ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      DimensionLookupMetaInjection.Entry.STREAM_FIELDNAME ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      DimensionLookupMetaInjection.Entry.UPDATE_TYPE ) );

    StepInjectionMetaEntry compareFieldsEntry = StepInjectionUtil.findEntry( entries,
      DimensionLookupMetaInjection.Entry.KEY_FIELDS );
    assertNotNull( compareFieldsEntry );
    StepInjectionMetaEntry compareFieldEntry = StepInjectionUtil.findEntry( compareFieldsEntry.getDetails(),
      DimensionLookupMetaInjection.Entry.KEY_FIELD );
    assertNotNull( compareFieldEntry );
    assertNotNull( StepInjectionUtil.findEntry( compareFieldEntry.getDetails(),
      DimensionLookupMetaInjection.Entry.KEY_DATABASE_FIELDNAME ) );
    assertNotNull( StepInjectionUtil.findEntry( compareFieldEntry.getDetails(),
      DimensionLookupMetaInjection.Entry.KEY_STREAM_FIELDNAME ) );
  }

  private DimensionLookupMeta populateDimensionLookupMeta() {
    DimensionLookupMeta meta = new DimensionLookupMeta();
    meta.allocate( NR_FIELDS, NR_FIELDS );

    meta.setUpdate( UPDATE_DIMENSION );
    meta.setSchemaName( SCHEMA_NAME );
    meta.setTableName( TABLE_NAME );
    meta.setCommitSize( COMMIT_SIZE );
    meta.setPreloadingCache( PRELOAD_CACHE );
    meta.setCacheSize( CACHE_SIZE );

    meta.setKeyField( TECHNICAL_KEY_FIELD );
    meta.setKeyRename( TECHNICAL_KEY_NEW_NAME );
    meta.setTechKeyCreation( TECHNICAL_KEY_CREATION );
    meta.setSequenceName( TECHNICAL_KEY_SEQUENCE );
    meta.setVersionField( VERSION_FIELD );
    meta.setDateField( DATE_FIELD );
    meta.setDateFrom( DATE_RANGE_START_FIELD );
    meta.setMinYear( MIN_YEAR );
    meta.setUsingStartDateAlternative( USE_ALTERNATIVE_START_DATE );
    meta.setStartDateAlternative( ALTERNATIVE_START_OPTION );
    meta.setStartDateFieldName( ALTERNATIVE_START_COLUMN );
    meta.setDateTo( DATE_RANGE_END_FIELD );
    meta.setMaxYear( MAX_YEAR );

    // CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < NR_FIELDS; i++ ) {
      meta.getFieldLookup()[i] = DATABASE_FIELDNAME + i;
      meta.getFieldStream()[i] = STREAM_FIELDNAME + i;
      meta.getFieldUpdate()[i] = UPDATE_TYPE;
    }

    for ( int i = 0; i < NR_FIELDS; i++ ) {
      meta.getKeyLookup()[i] = COMPARE_DATABASE_FIELD + i;
      meta.getKeyStream()[i] = COMPARE_STREAM_FIELD + i;
    }

    return meta;
  }

}
