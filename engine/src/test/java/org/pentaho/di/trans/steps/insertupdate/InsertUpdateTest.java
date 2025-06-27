package org.pentaho.di.trans.steps.insertupdate;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.core.SQLStatement;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
public class InsertUpdateTest {

    private StepMockHelper<InsertUpdateMeta, InsertUpdateData> mockHelper;

    @Before
    public void setUp() {
        mockHelper = new StepMockHelper<>( "InsertUpdate", InsertUpdateMeta.class, InsertUpdateData.class );
        when( mockHelper.logChannelInterfaceFactory.create( any(), any() ) )
                .thenReturn( mockHelper.logChannelInterface );
    }

    @Test
    public void testGetComparatorsAction() throws Exception {
        InsertUpdate insertUpdate = new InsertUpdate(
                mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );

        Method method = InsertUpdate.class.getDeclaredMethod( "getComparatorsAction", Map.class );
        method.setAccessible( true );

        JSONObject response = ( JSONObject ) method.invoke( insertUpdate, new HashMap<>() );

        Assert.assertNotNull( response );
        JSONArray comparators = ( JSONArray ) response.get( "comparators" );
        Assert.assertNotNull( comparators );
        Assert.assertEquals( 11, comparators.size() );
    }

    @Test
    public void testGetSQLAction() throws Exception {
        InsertUpdateMeta mockMeta = mock( InsertUpdateMeta.class );
        InsertUpdate insertUpdate = spy( new InsertUpdate(
                mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans ) );

        doReturn( mockMeta ).when( insertUpdate ).getStepMetaInterface();
        doReturn( "InsertUpdateStep" ).when( insertUpdate ).getStepname();
        doReturn( mockHelper.transMeta ).when( insertUpdate ).getTransMeta();

        RowMetaInterface mockRowMeta = new RowMeta();
        doReturn( mockRowMeta ).when( mockHelper.transMeta ).getPrevStepFields( "InsertUpdateStep" );

        DatabaseMeta mockDbMeta = mock( DatabaseMeta.class );
        SQLStatement mockSQL = new SQLStatement( "InsertUpdateStep", mockDbMeta, "SELECT * FROM dummy_table" );
        when( mockMeta.getSQLStatements( any(), any(), any(), any(), any() ) ).thenReturn( mockSQL );

        // Reflectively invoke the method
        Method method = InsertUpdate.class.getDeclaredMethod( "getSQLAction", Map.class );
        method.setAccessible( true );
        JSONObject response = ( JSONObject ) method.invoke( insertUpdate, new HashMap<>() );

        // Assertions
        Assert.assertNotNull( response );
        Assert.assertTrue( response.containsKey( "sql" ) );
        Assert.assertEquals( "SELECT * FROM dummy_table", response.get( "sql" ) );
    }
}
