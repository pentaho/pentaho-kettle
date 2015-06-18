package org.pentaho.di.trans.steps.janino;

import junit.framework.TestCase;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;

import java.util.List;

public class JaninoMetaInjectionTest extends TestCase {

  public static int NR_FIELDS = 10;

  public static String NEW_FIELDNAME = "javaField";
  public static String JAVA_EXPRESSION = "inField + otherField";
  public static String VALUE_TYPE = "Integer";
  public static int LENGTH = 10;
  public static int PRECISION = 1;
  public static String REPLACE_VALUE = "inField";

  public void testInjectionExtraction() throws Exception {

    // Test Strategy :
    //
    // Populate a new object, extract the metadata,
    // then inject into another set of metadata, compare the results.
    //
    JaninoMeta meta = populateJaninoMeta();

    List<StepInjectionMetaEntry> entries = meta.extractStepMetadataEntries();

    assertEquals( 1, entries.size() );

    JaninoMeta newMeta = new JaninoMeta();
    newMeta.getStepMetaInjectionInterface().injectStepMetadataEntries( entries );

    // Automatic compare
    //
    List<StepInjectionMetaEntry> cmpEntries = newMeta.extractStepMetadataEntries();
    StepInjectionUtil.compareEntryValues( entries, cmpEntries );
  }

  public void testInjectionEntries() throws Exception {
    JaninoMeta meta = populateJaninoMeta();
    List<StepInjectionMetaEntry> entries = meta.getStepMetaInjectionInterface().getStepInjectionMetadataEntries();
    assertEquals( 1, entries.size() );

    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.findEntry( entries,
      JaninoMetaInjection.Entry.EXPRESSION_FIELDS );
    assertNotNull( fieldsEntry );
    StepInjectionMetaEntry fieldEntry = StepInjectionUtil.findEntry( fieldsEntry.getDetails(),
      JaninoMetaInjection.Entry.EXPRESSION_FIELD );
    assertNotNull( fieldEntry );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      JaninoMetaInjection.Entry.NEW_FIELDNAME ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      JaninoMetaInjection.Entry.JAVA_EXPRESSION ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      JaninoMetaInjection.Entry.VALUE_TYPE ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      JaninoMetaInjection.Entry.LENGTH ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      JaninoMetaInjection.Entry.PRECISION ) );
    assertNotNull( StepInjectionUtil.findEntry( fieldEntry.getDetails(),
      JaninoMetaInjection.Entry.REPLACE_VALUE ) );
  }

  private JaninoMeta populateJaninoMeta() {
    JaninoMeta meta = new JaninoMeta();
    meta.allocate( NR_FIELDS );

    // CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < NR_FIELDS; i++ ) {
      meta.getFormula()[i] = new JaninoMetaFunction( NEW_FIELDNAME + i, JAVA_EXPRESSION + i,
        ValueMeta.getType( VALUE_TYPE ), LENGTH + i, PRECISION + i, REPLACE_VALUE + i );
    }

    return meta;
  }

}
