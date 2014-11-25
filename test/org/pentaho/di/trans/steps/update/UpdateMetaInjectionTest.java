package org.pentaho.di.trans.steps.update;

import junit.framework.TestCase;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;

import java.util.List;

public class UpdateMetaInjectionTest extends TestCase {

  public static String SCHEMA_NAME = "schema";
  public static String TABLE_NAME = "${TABLE_NAME}";
  public static String COMMIT_SIZE = "1000";
  public static boolean BATCH_UPDATE = true;
  public static boolean SKIP_LOOKUP = true;
  public static boolean IGNORE_LOOKUP_FAILURE = true;
  public static String FLAG_FIELD = "flag_field";

  public static int NR_FIELDS = 10;

  public static String DATABASE_FIELDNAME = "dbField";
  public static String STREAM_FIELDNAME = "inField";

  public static String COMPARE_DATABASE_FIELD = "keyDbField";
  public static String COMPARATOR = "BETWEEN";
  public static String COMPARE_STREAM_FIELD = "keyInField";
  public static String COMPARE_STREAM_FIELD2 = "keyInField2";

  public void testInjectionExtraction() throws Exception {

    // Test Strategy :
    //
    // Populate a new object, extract the metadata,
    // then inject into another set of metadata, compare the results.
    //
    UpdateMeta meta = populateUpdateMeta();

    List<StepInjectionMetaEntry> entries = meta.extractStepMetadataEntries();

    assertEquals( 9, entries.size() );

    UpdateMeta newMeta = new UpdateMeta();
    newMeta.getStepMetaInjectionInterface().injectStepMetadataEntries( entries );

    // Automatic compare
    //
    List<StepInjectionMetaEntry> cmpEntries = newMeta.extractStepMetadataEntries();
    StepInjectionUtil.compareEntryValues( entries, cmpEntries );
  }

  public void testInjectionEntries() throws Exception {
    UpdateMeta meta = populateUpdateMeta();
    List<StepInjectionMetaEntry> entries = meta.getStepMetaInjectionInterface().getStepInjectionMetadataEntries();
    assertEquals( 9, entries.size() );

    assertNotNull( StepInjectionUtil.findEntry( entries, UpdateMetaInjection.Entry.TARGET_SCHEMA ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, UpdateMetaInjection.Entry.TARGET_TABLE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, UpdateMetaInjection.Entry.COMMIT_SIZE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, UpdateMetaInjection.Entry.BATCH_UPDATE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, UpdateMetaInjection.Entry.SKIP_LOOKUP ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, UpdateMetaInjection.Entry.IGNORE_LOOKUP_FAILURE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, UpdateMetaInjection.Entry.FLAG_FIELD ) );

    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.findEntry( entries,
      UpdateMetaInjection.Entry.DATABASE_FIELDS );
    assertNotNull( fieldsEntry );
    StepInjectionMetaEntry fieldEntry = StepInjectionUtil.findEntry( fieldsEntry.getDetails(),
      UpdateMetaInjection.Entry.DATABASE_FIELD );
    assertNotNull( fieldEntry );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      UpdateMetaInjection.Entry.DATABASE_FIELDNAME ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      UpdateMetaInjection.Entry.STREAM_FIELDNAME ) );

    StepInjectionMetaEntry compareFieldsEntry = StepInjectionUtil.findEntry( entries,
      UpdateMetaInjection.Entry.COMPARE_FIELDS );
    assertNotNull( compareFieldsEntry );
    StepInjectionMetaEntry compareFieldEntry = StepInjectionUtil.findEntry( compareFieldsEntry.getDetails(),
      UpdateMetaInjection.Entry.COMPARE_FIELD );
    assertNotNull( compareFieldEntry );
    assertNotNull( StepInjectionUtil.findEntry( compareFieldEntry.getDetails(),
      UpdateMetaInjection.Entry.COMPARE_DATABASE_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( compareFieldEntry.getDetails(),
      UpdateMetaInjection.Entry.COMPARATOR ) );
    assertNotNull( StepInjectionUtil.findEntry( compareFieldEntry.getDetails(),
      UpdateMetaInjection.Entry.COMPARE_STREAM_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( compareFieldEntry.getDetails(),
      UpdateMetaInjection.Entry.COMPARE_STREAM_FIELD2 ) );
  }

  private UpdateMeta populateUpdateMeta() {
    UpdateMeta meta = new UpdateMeta();
    meta.allocate( NR_FIELDS, NR_FIELDS );

    meta.setSchemaName( SCHEMA_NAME );
    meta.setTableName( TABLE_NAME );
    meta.setCommitSize( COMMIT_SIZE );
    meta.setUseBatchUpdate( BATCH_UPDATE );
    meta.setSkipLookup( SKIP_LOOKUP );
    meta.setErrorIgnored( IGNORE_LOOKUP_FAILURE );
    meta.setIgnoreFlagField( FLAG_FIELD );

    // CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < NR_FIELDS; i++ ) {
      meta.getUpdateLookup()[i] = DATABASE_FIELDNAME + i;
      meta.getUpdateStream()[i] = STREAM_FIELDNAME + i;
    }

    for ( int i = 0; i < NR_FIELDS; i++ ) {
      meta.getKeyLookup()[i] = COMPARE_DATABASE_FIELD + i;
      meta.getKeyCondition()[i] = COMPARATOR;
      meta.getKeyStream()[i] = COMPARE_STREAM_FIELD + i;
      meta.getKeyStream2()[i] = COMPARE_STREAM_FIELD2 + i;
    }

    return meta;
  }

}
