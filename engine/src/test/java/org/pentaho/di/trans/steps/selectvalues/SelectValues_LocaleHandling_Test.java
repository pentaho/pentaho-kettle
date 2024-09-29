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


package org.pentaho.di.trans.steps.selectvalues;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.TransTestingUtil;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta.SelectField;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import java.util.Collections;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Note: In Europe (e.g. in UK), week starts on Monday. In USA, it starts on Sunday.
 *
 * @author Andrey Khayrutdinov
 */
public class SelectValues_LocaleHandling_Test {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  private SelectValues step;
  private Locale current;
  private StepMockHelper<SelectValuesMeta, StepDataInterface> helper;

  @Before
  public void setUp() throws Exception {
    current = Locale.getDefault();
    Locale.setDefault( Locale.UK );

    helper =
      StepMockUtil.getStepMockHelper( SelectValuesMeta.class, "SelectValues_LocaleHandling_Test" );
    when( helper.stepMeta.isDoingErrorHandling() ).thenReturn( true );

    step = new SelectValues( helper.stepMeta, helper.stepDataInterface, 1, helper.transMeta, helper.trans );
    step = spy( step );

    // Dec 28, 2015
    Calendar calendar = Calendar.getInstance();
    calendar.set( 2015, Calendar.DECEMBER, 28, 0, 0, 0 );
    doReturn( new Object[] { calendar.getTime() } ).doReturn( null )
      .when( step ).getRow();
  }

  @After
  public void tearDown() throws Exception {
    step = null;

    Locale.setDefault( current );
    current = null;

    helper.cleanUp();
  }


  @Test
  public void returns53_ForNull() throws Exception {
    executeAndCheck( null, "53" );
  }

  @Test
  public void returns53_ForEmpty() throws Exception {
    executeAndCheck( "", "53" );
  }

  @Test
  public void returns53_ForEn_GB() throws Exception {
    executeAndCheck( "en_GB", "53" );
  }

  @Test
  public void returns01_ForEn_US() throws Exception {
    executeAndCheck( "en_US", "01" );
  }

  private void executeAndCheck( String locale, String expectedWeekNumber ) throws Exception {
    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta( new ValueMetaDate( "field" ) );
    step.setInputRowMeta( inputRowMeta );

    SelectValuesMeta stepMeta = new SelectValuesMeta();
    stepMeta.allocate( 1, 0, 1 );
    stepMeta.getSelectFields()[0] = new SelectField();
    stepMeta.getSelectFields()[0].setName( "field" );
    stepMeta.getMeta()[ 0 ] =
      new SelectMetadataChange( stepMeta, "field", null, ValueMetaInterface.TYPE_STRING, -2, -2,
        ValueMetaInterface.STORAGE_TYPE_NORMAL, "ww", false, locale, null, false, null, null, null );

    SelectValuesData stepData = new SelectValuesData();
    stepData.select = true;
    stepData.metadata = true;
    stepData.firstselect = true;
    stepData.firstmetadata = true;

    List<Object[]> execute = TransTestingUtil.execute( step, stepMeta, stepData, 1, true );
    TransTestingUtil.assertResult( execute, Collections.singletonList( new Object[] { expectedWeekNumber } ) );
  }
}
