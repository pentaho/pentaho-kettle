/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.denormaliser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class DenormaliserTest {

    private StepMockHelper<DenormaliserMeta, DenormaliserData> mockHelper;

    @Before
    public void setup() {
        mockHelper = new StepMockHelper<>( "Denormaliser", DenormaliserMeta.class, DenormaliserData.class );
        when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
                .thenReturn( mockHelper.logChannelInterface );
    }

    @Test
    public void testGetAggregationTypesAction() throws Exception {
        Denormaliser denormaliser = new Denormaliser(
                mockHelper.stepMeta, mockHelper.stepDataInterface,
                0, mockHelper.transMeta, mockHelper.trans
        );
        Method method = Denormaliser.class.getDeclaredMethod( "getAggregationTypesAction", Map.class );
        method.setAccessible( true );
        JSONObject jsonObject = ( JSONObject ) method.invoke( denormaliser, new HashMap<>() );

        Assert.assertNotNull( jsonObject );

        JSONArray aggregationTypes = ( JSONArray ) jsonObject.get( "aggregationTypes" );
        Assert.assertNotNull( aggregationTypes );
        Assert.assertEquals( DenormaliserTargetField.typeAggrDesc.length, aggregationTypes.size() );

        for ( int i = 0; i < aggregationTypes.size(); i++ ) {
            JSONObject aggregationType = ( JSONObject ) aggregationTypes.get( i );
            Assert.assertEquals( DenormaliserTargetField.typeAggrDesc[i], aggregationType.get( "id" ) );
            Assert.assertEquals( DenormaliserTargetField.typeAggrLongDesc[i], aggregationType.get( "name" ) );
        }
    }

    @Test
    public void testGetLookupFieldsAction() throws Exception {
        DenormaliserMeta mockMeta = mock( DenormaliserMeta.class );
        when( mockMeta.getGroupField() ).thenReturn( new String[] { "groupField1" } );
        when( mockMeta.getKeyField() ).thenReturn( "keyField" );
        RowMetaInterface rowMeta = new RowMeta();
        ValueMetaString includedField = new ValueMetaString( "includedField" );
        includedField.setLength( 100 );
        rowMeta.addValueMeta( includedField );

        Denormaliser denormaliser = spy( new Denormaliser(
                mockHelper.stepMeta, mockHelper.stepDataInterface,
                0, mockHelper.transMeta, mockHelper.trans ) );
        Field metaField = Denormaliser.class.getDeclaredField( "meta" );
        metaField.setAccessible( true );
        metaField.set( denormaliser, mockMeta );

        doReturn( "MockStep" ).when( denormaliser ).getStepname();
        doReturn( mockHelper.transMeta ).when( denormaliser ).getTransMeta();
        when( mockHelper.transMeta.getPrevStepFields( "MockStep" ) ).thenReturn( rowMeta );

        Method method = Denormaliser.class.getDeclaredMethod( "getLookupFieldsAction", Map.class );
        method.setAccessible( true );
        JSONObject jsonObject = ( JSONObject ) method.invoke( denormaliser, new HashMap<>() );

        // Assertions
        Assert.assertNotNull( jsonObject );
        JSONArray fields = ( JSONArray ) jsonObject.get( "denormaliserFields" );
        Assert.assertNotNull( fields );
        Assert.assertEquals( 1, fields.size() );

        JSONObject field = ( JSONObject ) fields.get( 0 );
        Assert.assertEquals( "includedField", field.get( "fieldName" ) );
        Assert.assertEquals( "includedField", field.get( "targetName" ) );
        Assert.assertEquals( 100, field.get( "targetLength" ) );
        Assert.assertEquals(DenormaliserTargetField.getAggregationTypeDesc(DenormaliserTargetField.TYPE_AGGR_NONE),
                field.get( "aggregationType" ) );
    }

}
