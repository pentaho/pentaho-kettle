package org.pentaho.di.trans.steps.rowgenerator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class RowGeneratorUnitTest {

  private RowGenerator rowGenerator;

  @Before
  public void setUp() throws KettleException {
    // add variable to row generator step
    StepMetaInterface stepMetaInterface = spy( new RowGeneratorMeta() );
    ( (RowGeneratorMeta) stepMetaInterface ).setRowLimit( "${ROW_LIMIT}" );
    String[] strings = {};
    when( ( (RowGeneratorMeta) stepMetaInterface ).getFieldName() ).thenReturn( strings );

    StepMeta stepMeta = new StepMeta();
    stepMeta.setStepMetaInterface( stepMetaInterface );
    stepMeta.setName( "ROW_STEP_META" );
    StepDataInterface stepDataInterface = stepMeta.getStepMetaInterface().getStepData();

    // add variable to transformation variable space
    Map<String, String> map = new HashMap<String, String>();
    map.put( "ROW_LIMIT", "1440" );
    TransMeta transMeta = spy( new TransMeta() );
    transMeta.injectVariables( map );
    when( transMeta.findStep( anyString() ) ).thenReturn( stepMeta );

    Trans trans = spy( new Trans( transMeta, null ) );
    when( trans.getSocketRepository() ).thenReturn( null );
    when( trans.getLogChannelId() ).thenReturn( "ROW_LIMIT" );

    //prepare row generator, substitutes variable by value from transformation variable space
    rowGenerator = spy( new RowGenerator( stepMeta, stepDataInterface, 0, transMeta, trans ) );
    rowGenerator.initializeVariablesFrom( trans );
    rowGenerator.init( stepMetaInterface, stepDataInterface );
  }

  @Test
  public void testReadRowLimitAsTransformationVar() throws KettleException {
    long rowLimit = ( (RowGeneratorData) rowGenerator.getStepDataInterface() ).rowLimit;
    assertEquals( rowLimit,  1440 );
  }

}
