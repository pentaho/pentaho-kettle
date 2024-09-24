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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.EnumLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.MapLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.pentahoreporting.PentahoReportingOutputMeta.ProcessorType;

public class PentahoReportingOutputLoadSaveTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Test
  public void testSerialization() throws KettleException {
    List<String> attributes = Arrays.asList( "InputFileField", "OutputFileField", "InputFile", "OutputFile", "ParameterFieldMap", "OutputProcessorType", "CreateParentfolder", "UseValuesFromFields" );
    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attributeMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attributeMap.put( "ParameterFieldMap", new MapLoadSaveValidator<String, String>(
      new StringLoadSaveValidator(), new StringLoadSaveValidator() ) );
    Map<String, FieldLoadSaveValidator<?>> typeMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeMap.put( ProcessorType.class.getCanonicalName(),
      new EnumLoadSaveValidator<ProcessorType>( ProcessorType.class ) );

    LoadSaveTester<PentahoReportingOutputMeta> tester = new LoadSaveTester<PentahoReportingOutputMeta>(
      PentahoReportingOutputMeta.class, attributes, getterMap, setterMap, attributeMap, typeMap );

    tester.testSerialization();
  }
}
