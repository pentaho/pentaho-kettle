package org.pentaho.di.trans.steps.insertupdate;

import junit.framework.TestCase;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;

import java.util.List;

public class InsertUpdateMetaInjectionTest extends TestCase {

  public static String SCHEMA_NAME = "schema";
  public static String TABLE_NAME = "${TABLE_NAME}";
  public static String COMMIT_SIZE = "1000";
  public static boolean DONT_UPDATE = true;

  public static int NR_FIELDS = 10;

  public static String DATABASE_FIELDNAME = "dbField";
  public static String STREAM_FIELDNAME = "inField";
  public static boolean UPDATE_FIELD = true;

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
    InsertUpdateMeta meta = populateInsertUpdateMeta();

    List<StepInjectionMetaEntry> entries = meta.extractStepMetadataEntries();

    assertEquals( 6, entries.size() );

    InsertUpdateMeta newMeta = new InsertUpdateMeta();
    newMeta.getStepMetaInjectionInterface().injectStepMetadataEntries( entries );

    // Automatic compare
    //
    List<StepInjectionMetaEntry> cmpEntries = newMeta.extractStepMetadataEntries();
    StepInjectionUtil.compareEntryValues( entries, cmpEntries );
  }

  public void testInjectionEntries() throws Exception {
    InsertUpdateMeta meta = populateInsertUpdateMeta();
    List<StepInjectionMetaEntry> entries = meta.getStepMetaInjectionInterface().getStepInjectionMetadataEntries();
    assertEquals( 6, entries.size() );

    assertNotNull( StepInjectionUtil.findEntry( entries, InsertUpdateMetaInjection.Entry.TARGET_SCHEMA ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, InsertUpdateMetaInjection.Entry.TARGET_TABLE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, InsertUpdateMetaInjection.Entry.COMMIT_SIZE ) );
    assertNotNull( StepInjectionUtil.findEntry( entries, InsertUpdateMetaInjection.Entry.DONT_UPDATE ) );

    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.findEntry( entries,
      InsertUpdateMetaInjection.Entry.DATABASE_FIELDS );
    assertNotNull( fieldsEntry );
    StepInjectionMetaEntry fieldEntry = StepInjectionUtil.findEntry( fieldsEntry.getDetails(),
      InsertUpdateMetaInjection.Entry.DATABASE_FIELD );
    assertNotNull( fieldEntry );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      InsertUpdateMetaInjection.Entry.DATABASE_FIELDNAME ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      InsertUpdateMetaInjection.Entry.STREAM_FIELDNAME ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      InsertUpdateMetaInjection.Entry.UPDATE_FIELD ) );

    StepInjectionMetaEntry compareFieldsEntry = StepInjectionUtil.findEntry( entries,
      InsertUpdateMetaInjection.Entry.COMPARE_FIELDS );
    assertNotNull( compareFieldsEntry );
    StepInjectionMetaEntry compareFieldEntry = StepInjectionUtil.findEntry( compareFieldsEntry.getDetails(),
      InsertUpdateMetaInjection.Entry.COMPARE_FIELD );
    assertNotNull( compareFieldEntry );
    assertNotNull( StepInjectionUtil.findEntry( compareFieldEntry.getDetails(),
      InsertUpdateMetaInjection.Entry.COMPARE_DATABASE_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( compareFieldEntry.getDetails(),
      InsertUpdateMetaInjection.Entry.COMPARATOR ) );
    assertNotNull( StepInjectionUtil.findEntry( compareFieldEntry.getDetails(),
      InsertUpdateMetaInjection.Entry.COMPARE_STREAM_FIELD ) );
    assertNotNull( StepInjectionUtil.findEntry( compareFieldEntry.getDetails(),
      InsertUpdateMetaInjection.Entry.COMPARE_STREAM_FIELD2 ) );
  }

  private InsertUpdateMeta populateInsertUpdateMeta() {
    InsertUpdateMeta meta = new InsertUpdateMeta();
    meta.allocate( NR_FIELDS, NR_FIELDS );

    meta.setSchemaName( SCHEMA_NAME );
    meta.setTableName( TABLE_NAME );
    meta.setCommitSize( COMMIT_SIZE );
    meta.setUpdateBypassed( DONT_UPDATE );

    // CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < NR_FIELDS; i++ ) {
      meta.getUpdateLookup()[i] = DATABASE_FIELDNAME + i;
      meta.getUpdateStream()[i] = STREAM_FIELDNAME + i;
      meta.getUpdate()[i] = UPDATE_FIELD;
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
