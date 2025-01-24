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


package org.pentaho.di.trans.steps.rest.analyzer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.rest.Rest;
import org.pentaho.di.trans.steps.rest.RestMeta;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class RestClientExternalResourceConsumerTest {

  RestClientExternalResourceConsumer consumer;

  @Mock RestMeta meta;
  @Mock Rest step;
  @Mock RowMetaInterface rmi;
  Object[] row;

  String[] headerFields = new String[]{ "header" };
  String[] headerNames = new String[]{ "header1" };
  String[] paramFields = new String[]{ "param" };
  String[] paramNames = new String[]{ "param1" };

  @Before
  public void setUp() throws Exception {
    consumer = new RestClientExternalResourceConsumer();

    row = new Object[3];
    row[0] = "http://my.url";
    row[1] = "POST";
    row[2] = "xyz";

    when( rmi.getString( row, "url", null ) ).thenReturn( row[0].toString() );

    when( step.getStepMetaInterface() ).thenReturn( meta );

  }

  @Test
  public void testGetResourcesFromMeta() throws Exception {
    when( meta.getUrl() ).thenReturn( row[ 0 ].toString() );
    Collection<IExternalResourceInfo> resourcesFromMeta = consumer.getResourcesFromMeta( meta );

    assertEquals( 1, resourcesFromMeta.size() );
    assertEquals( row[ 0 ], resourcesFromMeta.toArray( new IExternalResourceInfo[ 1 ] )[ 0 ].getName() );
  }

  @Test
  public void testGetResourcesFromRow() throws Exception {
    when( meta.isUrlInField() ).thenReturn( true );
    when( meta.getAttUrlField() ).thenReturn( "url" );
    when( meta.getHeaderField() ).thenReturn( headerFields );
    when( meta.getParameterField() ).thenReturn( paramFields );
    when( meta.getHeaderName() ).thenReturn( headerNames );
    when( meta.getParameterName() ).thenReturn( paramNames );

    when( rmi.getString( row, "header", null ) ).thenReturn( row[ 2 ].toString() );
    when( rmi.getString( row, "param", null ) ).thenReturn( row[ 2 ].toString() );


    Collection<IExternalResourceInfo> resourcesFromMeta = consumer.getResourcesFromRow( step, rmi, row );

    assertEquals( 1, resourcesFromMeta.size() );
    IExternalResourceInfo resourceInfo = resourcesFromMeta.toArray( new IExternalResourceInfo[ 1 ] )[ 0 ];
    assertEquals( row[ 0 ], resourceInfo.getName() );
    assertNotNull( resourceInfo.getAttributes() );
  }

  @Test
  public void testGetResourcesFromRow_fieldsForMethodAndBody() throws Exception {
    when( meta.isUrlInField() ).thenReturn( true );
    when( meta.getAttUrlField() ).thenReturn( "url" );
    when( meta.getHeaderField() ).thenReturn( null );
    when( meta.getParameterField() ).thenReturn( null );
    when( meta.isDynamicMethod() ).thenReturn( true );
    when( meta.getAttMethodFieldName() ).thenReturn( "method" );
    when( meta.getAttBodyField() ).thenReturn( "body" );
    when( rmi.getString( row, "method", null ) ).thenReturn( row[ 2 ].toString() );
    when( rmi.getString( row, "body", null ) ).thenReturn( row[ 2 ].toString() );

    Collection<IExternalResourceInfo> resourcesFromMeta = consumer.getResourcesFromRow( step, rmi, row );

    assertEquals( 1, resourcesFromMeta.size() );
    IExternalResourceInfo resourceInfo = resourcesFromMeta.toArray( new IExternalResourceInfo[ 1 ] )[ 0 ];
    assertEquals( row[ 0 ], resourceInfo.getName() );
    assertNotNull( resourceInfo.getAttributes() );
  }

  @Test
  public void testIsDataDriven() throws Exception {
    assertTrue( consumer.isDataDriven( meta ) );
  }

  @Test
  public void testGetMetaClass() throws Exception {
    assertEquals( RestMeta.class, consumer.getMetaClass() );
  }
}
