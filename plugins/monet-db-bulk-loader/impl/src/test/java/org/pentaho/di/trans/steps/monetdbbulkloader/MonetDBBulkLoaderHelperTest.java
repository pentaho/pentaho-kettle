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

package org.pentaho.di.trans.steps.monetdbbulkloader;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MonetDBBulkLoaderHelperTest {

    @Mock
    private TransMeta transMeta;
    @Mock
    private StepMeta stepMeta;
    @Mock
    private MonetDBBulkLoaderMeta meta;
    @Mock
    private SQLStatement sqlStatement;
    @Mock
    private RowMetaInterface prevFields;
    @Mock
    private DatabaseMeta databaseMeta;

    private MonetDBBulkLoaderHelper helper;
    private Map<String, String> queryParams;

    @Before
    public void setUp() throws Exception {
        if (!KettleEnvironment.isInitialized()) {
            KettleEnvironment.init();
        }
        helper = new MonetDBBulkLoaderHelper();
        queryParams = new HashMap<>();
    }

    @Test
    public void testHandleStepAction_GetSQL_Success() throws Exception {
        queryParams.put(MonetDBBulkLoaderHelper.STEP_NAME, "monetStep");
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "conn");

        when(transMeta.findStep("monetStep")).thenReturn(stepMeta);
        when(stepMeta.getStepMetaInterface()).thenReturn(meta);
        when(transMeta.findDatabase("conn")).thenReturn(databaseMeta);
        when(transMeta.getPrevStepFields("monetStep")).thenReturn(validRowMeta());
        when(meta.getFieldStream()).thenReturn(new String[]{"col1"});
        when(meta.getSQLStatements(any(), any(), any(), eq(false), isNull(), eq(false))).thenReturn(sqlStatement);
        when(sqlStatement.hasError()).thenReturn(false);
        when(sqlStatement.hasSQL()).thenReturn(true);
        when(sqlStatement.getSQL()).thenReturn("CREATE TABLE x (... )");

        JSONObject response = helper.handleStepAction("getSQL", transMeta, queryParams);

        assertEquals(StepInterface.SUCCESS_RESPONSE, response.get("actionStatus"));
        assertEquals("CREATE TABLE x (... )", response.get("sqlString"));
    }

    @Test
    public void testHandleStepAction_InvalidMethod() {
        JSONObject response = helper.handleStepAction("invalidMethod", transMeta, queryParams);

        assertEquals(BaseStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get(BaseStepHelper.ACTION_STATUS));
    }

    @Test
    public void testHandleStepAction_ExceptionHandling() {
        queryParams.put(MonetDBBulkLoaderHelper.STEP_NAME, "monetStep");

        JSONObject response = helper.handleStepAction("getSQL", transMeta, queryParams);

        assertEquals(BaseStepHelper.FAILURE_RESPONSE, response.get(BaseStepHelper.ACTION_STATUS));
        assertEquals(
            BaseMessages.getString(MonetDBBulkLoaderHelper.class, "MonetDBBulkLoaderHelper.GetSQL.MissingStepAndConnection"),
            response.get(MonetDBBulkLoaderHelper.DETAILS).toString());
    }

    @Test
    public void testGetSQLAction_Success_NoSQLNeeded() throws Exception {
        queryParams.put(MonetDBBulkLoaderHelper.STEP_NAME, "monetStep");
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "conn");

        when(transMeta.findStep("monetStep")).thenReturn(stepMeta);
        when(stepMeta.getStepMetaInterface()).thenReturn(meta);
        when(transMeta.findDatabase("conn")).thenReturn(databaseMeta);
        when(transMeta.getPrevStepFields("monetStep")).thenReturn(validRowMeta());
        when(meta.getFieldStream()).thenReturn(new String[]{"col1"});
        when(meta.getSQLStatements(any(), any(), any(), eq(false), isNull(), eq(false))).thenReturn(sqlStatement);
        when(sqlStatement.hasError()).thenReturn(false);
        when(sqlStatement.hasSQL()).thenReturn(false);

        JSONObject response = helper.getSQLAction(transMeta, queryParams);

        assertEquals(StepInterface.SUCCESS_RESPONSE, response.get("actionStatus"));
        assertNotNull(response.get(MonetDBBulkLoaderHelper.DETAILS));
    }

    @Test
    public void testGetSQLAction_SQLError() throws Exception {
        queryParams.put(MonetDBBulkLoaderHelper.STEP_NAME, "monetStep");
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "conn");

        when(transMeta.findStep("monetStep")).thenReturn(stepMeta);
        when(stepMeta.getStepMetaInterface()).thenReturn(meta);
        when(transMeta.findDatabase("conn")).thenReturn(databaseMeta);
        when(transMeta.getPrevStepFields("monetStep")).thenReturn(validRowMeta());
        when(meta.getFieldStream()).thenReturn(new String[]{"col1"});
        when(meta.getSQLStatements(any(), any(), any(), eq(false), isNull(), eq(false))).thenReturn(sqlStatement);
        when(sqlStatement.hasError()).thenReturn(true);
        when(sqlStatement.getError()).thenReturn("SQL Error occurred");

        JSONObject response = helper.getSQLAction(transMeta, queryParams);

        assertEquals(StepInterface.FAILURE_RESPONSE, response.get("actionStatus"));
        assertEquals("SQL Error occurred", response.get(MonetDBBulkLoaderHelper.DETAILS));
    }

    @Test
    public void testSQL_ExceptionHandling() throws Exception {
        when(transMeta.findStep("monetStep")).thenReturn(stepMeta);
        when(stepMeta.getStepMetaInterface()).thenReturn(meta);
        when(transMeta.findDatabase("conn")).thenReturn(databaseMeta);
        when(transMeta.getPrevStepFields("monetStep")).thenThrow(new KettleStepException("Test exception"));

        try {
            helper.sql(transMeta, "monetStep", "conn");
            fail("Expected KettleStepException");
        } catch (KettleStepException e) {
            assertEquals("Test exception", e.getMessage().trim());
        }
    }

    @Test
    public void testHandleStepAction_NullQueryParams() {
        JSONObject response = helper.handleStepAction("getSQL", transMeta, null);

        assertEquals(BaseStepHelper.FAILURE_RESPONSE, response.get(BaseStepHelper.ACTION_STATUS));
        assertNotNull(response.get(MonetDBBulkLoaderHelper.DETAILS));
    }

    @Test
    public void testHandleStepAction_MissingQueryParams() {
        queryParams.put(MonetDBBulkLoaderHelper.STEP_NAME, "monetStep");

        JSONObject response = helper.handleStepAction("getSQL", transMeta, queryParams);

        assertEquals(BaseStepHelper.FAILURE_RESPONSE, response.get(BaseStepHelper.ACTION_STATUS));
        assertNotNull(response.get(MonetDBBulkLoaderHelper.DETAILS));
    }

    @Test
    public void testGetSQLAction_NullSQL() throws Exception {
        queryParams.put(MonetDBBulkLoaderHelper.STEP_NAME, "monetStep");
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "conn");

        when(transMeta.findStep("monetStep")).thenReturn(stepMeta);
        when(stepMeta.getStepMetaInterface()).thenReturn(meta);
        when(transMeta.findDatabase("conn")).thenReturn(databaseMeta);
        when(transMeta.getPrevStepFields("monetStep")).thenReturn(prevFields);
        when(prevFields.size()).thenReturn(0);

        JSONObject response = helper.getSQLAction(transMeta, queryParams);

        assertEquals(StepInterface.FAILURE_RESPONSE, response.get("actionStatus"));
        assertNotNull(response.get(MonetDBBulkLoaderHelper.DETAILS));
    }

    @Test
    public void testGetSqlActionKettleStepException() throws Exception {
        MonetDBBulkLoaderHelper spyHelper = spy(helper);
        doThrow(new KettleStepException("Kettle exception")).when(spyHelper).sql(any(), anyString(), anyString());

        queryParams.put(MonetDBBulkLoaderHelper.STEP_NAME, "monetStep");
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "conn");

        JSONObject response = spyHelper.getSQLAction(transMeta, queryParams);

        assertEquals(StepInterface.FAILURE_METHOD_NOT_RESPONSE, response.get("actionStatus"));
        assertEquals("Kettle exception", response.get(MonetDBBulkLoaderHelper.DETAILS).toString().trim());
    }

    @Test
    public void testSQL_NullStepName() throws Exception {
        assertNull(helper.sql(transMeta, null, "conn"));
    }

    @Test
    public void testSQL_EmptyStepName() throws Exception {
        assertNull(helper.sql(transMeta, "", "conn"));
    }

    @Test
    public void testSQL_MissingStepMeta() throws Exception {
        when(transMeta.findStep("monetStep")).thenReturn(null);
        assertNull(helper.sql(transMeta, "monetStep", "conn"));
    }

    @Test
    public void testSQL_WrongStepType() throws Exception {
        when(transMeta.findStep("monetStep")).thenReturn(stepMeta);
        when(stepMeta.getStepMetaInterface()).thenReturn(mock(org.pentaho.di.trans.step.StepMetaInterface.class));
        assertNull(helper.sql(transMeta, "monetStep", "conn"));
    }

    @Test
    public void testSQL_InvalidRowMeta_Null() throws Exception {
        when(transMeta.findStep("monetStep")).thenReturn(stepMeta);
        when(stepMeta.getStepMetaInterface()).thenReturn(meta);
        when(transMeta.findDatabase("conn")).thenReturn(databaseMeta);
        when(transMeta.getPrevStepFields("monetStep")).thenReturn(null);

        assertNull(helper.sql(transMeta, "monetStep", "conn"));
        verify(meta).setDatabaseMeta(databaseMeta);
    }

    @Test
    public void testSQL_FieldStream_Null() throws Exception {
        when(transMeta.findStep("monetStep")).thenReturn(stepMeta);
        when(stepMeta.getStepMetaInterface()).thenReturn(meta);
        when(transMeta.findDatabase("conn")).thenReturn(databaseMeta);
        when(transMeta.getPrevStepFields("monetStep")).thenReturn(validRowMeta());
        when(meta.getFieldStream()).thenReturn(null);

        assertNull(helper.sql(transMeta, "monetStep", "conn"));
    }

    @Test
    public void testSQL_FieldStream_Empty() throws Exception {
        when(transMeta.findStep("monetStep")).thenReturn(stepMeta);
        when(stepMeta.getStepMetaInterface()).thenReturn(meta);
        when(transMeta.findDatabase("conn")).thenReturn(databaseMeta);
        when(transMeta.getPrevStepFields("monetStep")).thenReturn(validRowMeta());
        when(meta.getFieldStream()).thenReturn(new String[0]);

        assertNull(helper.sql(transMeta, "monetStep", "conn"));
    }

    @Test
    public void testSQL_Valid_ReturnsStatement() throws Exception {
        RowMetaInterface prev = validRowMeta();

        when(transMeta.findStep("monetStep")).thenReturn(stepMeta);
        when(stepMeta.getStepMetaInterface()).thenReturn(meta);
        when(transMeta.findDatabase("conn")).thenReturn(databaseMeta);
        when(transMeta.getPrevStepFields("monetStep")).thenReturn(prev);
        when(meta.getFieldStream()).thenReturn(new String[]{"col1"});
        when(meta.getSQLStatements(any(), any(), eq(prev), anyBoolean(), isNull(), anyBoolean())).thenReturn(sqlStatement);

        SQLStatement result = helper.sql(transMeta, "monetStep", "conn");

        assertSame(sqlStatement, result);
        verify(meta).setDatabaseMeta(databaseMeta);
    }

    private RowMetaInterface validRowMeta() {
        RowMeta rowMeta = new RowMeta();
        rowMeta.addValueMeta(new ValueMetaString("col1"));
        return rowMeta;
    }
}
