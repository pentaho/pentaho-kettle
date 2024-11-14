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


package org.pentaho.di.trans.steps;

import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseObjectFactory;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumerProvider;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumerProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author mburgess
 */
public class MetaverseTestUtils {

  private static IMetaverseObjectFactory metaverseObjectFactory = new MetaverseObjectFactory();

  public static IMetaverseObjectFactory getMetaverseObjectFactory() {
    return metaverseObjectFactory;
  }

  public static IDocumentController getDocumentController() {
    IDocumentController documentController = mock( IDocumentController.class );
    IMetaverseBuilder metaverseBuilder = mock( IMetaverseBuilder.class );
    when( metaverseBuilder.getMetaverseObjectFactory() ).thenReturn( getMetaverseObjectFactory() );
    when( documentController.getMetaverseObjectFactory() ).thenReturn( getMetaverseObjectFactory() );
    when( documentController.getMetaverseBuilder() ).thenReturn( metaverseBuilder );
    return documentController;
  }

  public static IStepExternalResourceConsumerProvider getStepExternalResourceConsumerProvider() {
    IStepExternalResourceConsumerProvider provider = mock( IStepExternalResourceConsumerProvider.class );
    // TODO
    return provider;
  }

  public static IJobEntryExternalResourceConsumerProvider getJobEntryExternalResourceConsumerProvider() {
    IJobEntryExternalResourceConsumerProvider provider = mock( IJobEntryExternalResourceConsumerProvider.class );
    // TODO
    return provider;
  }
}
