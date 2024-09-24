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
package org.pentaho.di.trans.steps.joinrows;

import static org.mockito.Mockito.mock;
import static junit.framework.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ConditionLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

public class JoinRowsMetaTest {
  LoadSaveTester loadSaveTester;
  Class<JoinRowsMeta> testMetaClass = JoinRowsMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "directory", "prefix", "cacheSize", "mainStepname", "condition" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "condition", new ConditionLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testCleanAfterHopToRemove_NullParameter() {
    JoinRowsMeta joinRowsMeta = new JoinRowsMeta();
    StepMeta stepMeta1 = new StepMeta( "Step1", mock( StepMetaInterface.class ) );
    joinRowsMeta.setMainStep( stepMeta1 );
    joinRowsMeta.setMainStepname( stepMeta1.getName() );

    // This call must not throw an exception
    joinRowsMeta.cleanAfterHopToRemove( null );

    // And no change to the step should be made
    assertEquals( stepMeta1, joinRowsMeta.getMainStep() );
    assertEquals( stepMeta1.getName(), joinRowsMeta.getMainStepname() );
  }

  @Test
  public void testCleanAfterHopToRemove_UnknownStep() {
    JoinRowsMeta joinRowsMeta = new JoinRowsMeta();

    StepMeta stepMeta1 = new StepMeta( "Step1", mock( StepMetaInterface.class ) );
    StepMeta stepMeta2 = new StepMeta( "Step2", mock( StepMetaInterface.class ) );
    joinRowsMeta.setMainStep( stepMeta1 );
    joinRowsMeta.setMainStepname( stepMeta1.getName() );

    joinRowsMeta.cleanAfterHopToRemove( stepMeta2 );

    // No change to the step should be made
    assertEquals( stepMeta1, joinRowsMeta.getMainStep() );
    assertEquals( stepMeta1.getName(), joinRowsMeta.getMainStepname() );
  }

  @Test
  public void testCleanAfterHopToRemove_ReferredStep() {
    JoinRowsMeta joinRowsMeta = new JoinRowsMeta();

    StepMeta stepMeta1 = new StepMeta( "Step1", mock( StepMetaInterface.class ) );
    joinRowsMeta.setMainStep( stepMeta1 );
    joinRowsMeta.setMainStepname( stepMeta1.getName() );

    joinRowsMeta.cleanAfterHopToRemove( stepMeta1 );

    // No change to the step should be made
    assertNull( joinRowsMeta.getMainStep() );
    assertNull( joinRowsMeta.getMainStepname() );
  }
}
