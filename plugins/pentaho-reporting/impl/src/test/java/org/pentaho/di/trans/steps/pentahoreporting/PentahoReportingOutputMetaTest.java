/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.pentahoreporting;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class PentahoReportingOutputMetaTest extends BaseMetadataInjectionTest<PentahoReportingOutputMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new PentahoReportingOutputMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "INPUT_FILE_FIELD", new StringGetter() {
      public String get() {
        return meta.getInputFileField();
      }
    } );
    check( "OUTPUT_FILE_FIELD", new StringGetter() {
      public String get() {
        return meta.getOutputFileField();
      }
    } );
    check( "INPUT_FILE", new StringGetter() {
      public String get() {
        return meta.getInputFile();
      }
    } );
    check( "OUTPUT_FILE", new StringGetter() {
      public String get() {
        return meta.getOutputFile();
      }
    } );
    check( "USE_VALUES_FROM_FIELDS", new BooleanGetter() {
      public boolean get() {
        return meta.getUseValuesFromFields();
      }
    } );
    ValueMetaInterface valueMeta = new ValueMetaString( "f" );
    injector.setProperty( meta, "PARAMETER_NAME", setValue( valueMeta, "f1", "f2" ), "f" );
    injector.setProperty( meta, "FIELDNAME", setValue( valueMeta, "v1", "v2" ), "f" );
    assertEquals( "v1", meta.getParameterFieldMap().get( "f1" ) );
    assertEquals( "v2", meta.getParameterFieldMap().get( "f2" ) );

    skipPropertyTest( "PARAMETER_NAME" );
    skipPropertyTest( "FIELDNAME" );

    check( "OUTPUT_PROCESSOR_TYPE", new EnumGetter() {
      public Enum<?> get() {
        return meta.getOutputProcessorType();
      }
    }, PentahoReportingOutputMeta.ProcessorType.class );
    check( "CREATE_PARENT_FOLDER", new BooleanGetter() {
      public boolean get() {
        return meta.getCreateParentfolder();
      }
    } );
  }
}
