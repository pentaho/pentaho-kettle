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


package org.pentaho.di.trans.steps.rest;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.metastore.api.IMetaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RestMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void beforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
      Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Test
  public void testLoadSaveRoundTrip() throws KettleException {
    List<String> attributes =
      Arrays.asList( "applicationType", "method", "url", "urlInField", "dynamicMethod", "methodFieldName",
        "urlField", "bodyField", "httpLogin", "httpPassword", "proxyHost", "proxyPort", "preemptive",
        "trustStoreFile", "trustStorePassword", "ignoreSsl", "headerField", "headerName", "parameterField",
        "parameterName", "matrixParameterField", "matrixParameterName", "fieldName", "resultCodeFieldName",
        "responseTimeFieldName", "responseHeaderFieldName" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    // Arrays need to be consistent length
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 );
    fieldLoadSaveValidatorAttributeMap.put( "headerField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "headerName", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "parameterField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "parameterName", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "matrixParameterField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "matrixParameterName", stringArrayLoadSaveValidator );

    LoadSaveTester<RestMeta> loadSaveTester =
      new LoadSaveTester<RestMeta>( RestMeta.class, attributes, new HashMap<String, String>(),
        new HashMap<String, String>(), fieldLoadSaveValidatorAttributeMap,
        new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testSerialization();
  }

  @Test
  public void testStepChecks() {
    RestMeta meta = new RestMeta();
    List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
    TransMeta transMeta = new TransMeta();
    StepMeta step = new StepMeta();
    RowMetaInterface prev = new RowMeta();
    RowMetaInterface info = new RowMeta();
    String[] input = new String[0];
    String[] output = new String[0];
    VariableSpace variables = new Variables();
    Repository repo = null;
    IMetaStore metaStore = null;

    // In a default configuration, it's expected that some errors will occur.
    // For this, we'll grab a baseline count of the number of errors
    // as the error count should decrease as we change configuration settings to proper values.
    remarks.clear();
    meta.check( remarks, transMeta, step, prev, input, output, info, variables, repo, metaStore );
    final int errorsDefault = getCheckResultErrorCount( remarks );
    assertTrue( errorsDefault > 0 );

    // Setting the step to read the URL from a field should fix one of the check() errors
    meta.setUrlInField( true );
    meta.setUrlField( "urlField" );
    prev.addValueMeta( new ValueMetaString( "urlField" ) );
    remarks.clear();
    meta.check( remarks, transMeta, step, prev, input, output, info, variables, repo, metaStore );
    int errorsCurrent = getCheckResultErrorCount( remarks );
    assertTrue( errorsDefault > errorsCurrent );
  }

  private static int getCheckResultErrorCount( List<CheckResultInterface> remarks ) {
    return remarks.stream()
      .filter( p -> p.getType() == CheckResultInterface.TYPE_RESULT_ERROR )
      .collect( Collectors.toList() ).size();
  }

  @Test
  public void testEntityEnclosingMethods() {
    assertTrue( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_POST ) );
    assertTrue( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_PUT ) );
    assertTrue( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_PATCH ) );

    assertFalse( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_GET ) );
    assertFalse( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_DELETE ) );
    assertFalse( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_HEAD ) );
    assertFalse( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_OPTIONS ) );
  }
}
