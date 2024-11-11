/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.metainject;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;

import static org.mockito.Mockito.*;

/**
 * Created by Vasilina_Terehova on 3/31/2017.
 */
public class OpenMappingExtensionTest {

  public static final String TRANS_META_NAME = "Test name";
  private static LogChannelInterface logChannelInterface;
  private TransMeta transMeta;
  private StepMeta stepMeta;
  private Object[] metaData;

  @Before
  public void setup() {
    setKettleLogFactoryWithMock();
    transMeta = spy( new TransMeta() );
    stepMeta = mock( StepMeta.class );
    metaData = new Object[] { stepMeta, transMeta };
  }

  @Test
  public void testLocalizedMessage() throws KettleException {
    OpenMappingExtension openMappingExtension = new OpenMappingExtension();
    Class PKG = SpoonLifecycleListener.class;
    String afterInjectionMessageAdded = BaseMessages.getString( PKG, "TransGraph.AfterInjection" );
    transMeta.setName( TRANS_META_NAME );
    doReturn( mock( MetaInjectMeta.class ) ).when( stepMeta ).getStepMetaInterface();
    openMappingExtension.callExtensionPoint( logChannelInterface, metaData );
    assert ( transMeta.getName().contains( afterInjectionMessageAdded ) );
  }

  private void setKettleLogFactoryWithMock() {
    LogChannelInterfaceFactory logChannelInterfaceFactory = mock( LogChannelInterfaceFactory.class );
    logChannelInterface = mock( LogChannelInterface.class );
    when( logChannelInterfaceFactory.create( any() ) ).thenReturn( logChannelInterface );
    KettleLogStore.setLogChannelInterfaceFactory( logChannelInterfaceFactory );
  }
}
