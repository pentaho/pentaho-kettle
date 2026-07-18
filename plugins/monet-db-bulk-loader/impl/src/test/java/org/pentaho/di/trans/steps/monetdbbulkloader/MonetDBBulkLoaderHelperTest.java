/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.trans.steps.monetdbbulkloader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
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

    @Test
    public void testGetTableFieldAndType_NullQueryParams() {
        JSONObject response = helper.getTableFieldAndType(transMeta, null);

        assertEquals(StepInterface.FAILURE_RESPONSE, response.get("actionStatus"));
        assertEquals("Missing query parameters.", response.get("error"));
    }

    @Test
    public void testGetTableFieldAndType_MissingConnection() {
        queryParams.put(MonetDBBulkLoaderHelper.TABLE, "my_table");
        when(transMeta.environmentSubstitute("my_table")).thenReturn("my_table");

        JSONObject response = helper.getTableFieldAndType(transMeta, queryParams);

        assertEquals(StepInterface.FAILURE_RESPONSE, response.get("actionStatus"));
        assertNotNull(response.get("error"));
    }

    @Test
    public void testGetTableFieldAndType_BlankConnection() {
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "   ");
        queryParams.put(MonetDBBulkLoaderHelper.TABLE, "my_table");
        when(transMeta.environmentSubstitute("my_table")).thenReturn("my_table");

        JSONObject response = helper.getTableFieldAndType(transMeta, queryParams);

        assertEquals(StepInterface.FAILURE_RESPONSE, response.get("actionStatus"));
        assertNotNull(response.get("error"));
    }

    @Test
    public void testGetTableFieldAndType_MissingTable() {
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "conn");

        JSONObject response = helper.getTableFieldAndType(transMeta, queryParams);

        assertEquals(StepInterface.FAILURE_RESPONSE, response.get("actionStatus"));
        assertNotNull(response.get("error"));
    }

    @Test
    public void testGetTableFieldAndType_NullSchema_ReturnsFailure() {
        // MonetDB requires schema to build schema.table; absent schema → FAILURE.
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "conn");
        queryParams.put(MonetDBBulkLoaderHelper.TABLE, "my_table");
        when(transMeta.environmentSubstitute("my_table")).thenReturn("my_table");

        JSONObject response = helper.getTableFieldAndType(transMeta, queryParams);

        assertEquals(StepInterface.FAILURE_RESPONSE, response.get("actionStatus"));
        assertNotNull(response.get("error"));
    }

    @Test
    public void testGetTableFieldAndType_BlankSchema_ReturnsFailure() {
        // MonetDB requires schema; whitespace-only schema is treated as missing → FAILURE.
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "conn");
        queryParams.put(MonetDBBulkLoaderHelper.SCHEMA, "   ");
        queryParams.put(MonetDBBulkLoaderHelper.TABLE, "my_table");
        when(transMeta.environmentSubstitute("   ")).thenReturn("   ");
        when(transMeta.environmentSubstitute("my_table")).thenReturn("my_table");

        JSONObject response = helper.getTableFieldAndType(transMeta, queryParams);

        assertEquals(StepInterface.FAILURE_RESPONSE, response.get("actionStatus"));
        assertNotNull(response.get("error"));
    }

    @Test
    public void testGetTableFieldAndType_DatabaseNotFound() {
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "conn");
        queryParams.put(MonetDBBulkLoaderHelper.SCHEMA, "public");
        queryParams.put(MonetDBBulkLoaderHelper.TABLE, "my_table");
        when(transMeta.environmentSubstitute("public")).thenReturn("public");
        when(transMeta.environmentSubstitute("my_table")).thenReturn("my_table");
        when(transMeta.findDatabase("conn")).thenReturn(null);

        JSONObject response = helper.getTableFieldAndType(transMeta, queryParams);

        assertEquals(StepInterface.FAILURE_RESPONSE, response.get("actionStatus"));
        assertEquals("An unexpected error occurred.", response.get("error"));
    }

    @Test
    public void testGetTableFieldAndType_Success_WithSchema() {
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "conn");
        queryParams.put(MonetDBBulkLoaderHelper.SCHEMA, "public");
        queryParams.put(MonetDBBulkLoaderHelper.TABLE, "my_table");
        when(transMeta.findDatabase("conn")).thenReturn(databaseMeta);
        when(transMeta.environmentSubstitute("public")).thenReturn("public");
        when(transMeta.environmentSubstitute("my_table")).thenReturn("my_table");

        RowMeta rowMeta = new RowMeta();
        rowMeta.addValueMeta(new ValueMetaString("id"));
        rowMeta.addValueMeta(new ValueMetaString("name"));

        try (MockedConstruction<Database> ignored = mockConstruction(Database.class,
                (mockDb, context) -> {
                    doNothing().when(mockDb).connect();
                    when(mockDb.getTableFieldsMeta("public", "my_table")).thenReturn(rowMeta);
                })) {
            JSONObject response = helper.getTableFieldAndType(transMeta, queryParams);

            assertEquals(StepInterface.SUCCESS_RESPONSE, response.get("actionStatus"));
            JSONArray columns = (JSONArray) response.get("columns");
            assertNotNull(columns);
            assertEquals(2, columns.size());
            assertEquals("id", ((JSONObject) columns.get(0)).get("columnName"));
            assertEquals("name", ((JSONObject) columns.get(1)).get("columnName"));
        }
    }

    @Test
    public void testGetTableFieldAndType_Success_NullRowMeta() {
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "conn");
        queryParams.put(MonetDBBulkLoaderHelper.SCHEMA, "public");
        queryParams.put(MonetDBBulkLoaderHelper.TABLE, "my_table");
        when(transMeta.findDatabase("conn")).thenReturn(databaseMeta);
        when(transMeta.environmentSubstitute("public")).thenReturn("public");
        when(transMeta.environmentSubstitute("my_table")).thenReturn("my_table");

        try (MockedConstruction<Database> ignored = mockConstruction(Database.class,
                (mockDb, context) -> {
                    doNothing().when(mockDb).connect();
                    when(mockDb.getTableFieldsMeta("public", "my_table")).thenReturn(null);
                })) {
            JSONObject response = helper.getTableFieldAndType(transMeta, queryParams);

            assertEquals(StepInterface.SUCCESS_RESPONSE, response.get("actionStatus"));
            JSONArray columns = (JSONArray) response.get("columns");
            assertNotNull(columns);
            assertEquals(0, columns.size());
        }
    }

    @Test
    public void testGetTableFieldAndType_DatabaseException() {
        queryParams.put(MonetDBBulkLoaderHelper.CONNECTION, "conn");
        queryParams.put(MonetDBBulkLoaderHelper.SCHEMA, "public");
        queryParams.put(MonetDBBulkLoaderHelper.TABLE, "my_table");
        when(transMeta.findDatabase("conn")).thenReturn(databaseMeta);
        when(transMeta.environmentSubstitute("public")).thenReturn("public");
        when(transMeta.environmentSubstitute("my_table")).thenReturn("my_table");

        try (MockedConstruction<Database> ignored = mockConstruction(Database.class,
                (mockDb, context) ->
                        doThrow(new Exception("DB connect failed")).when(mockDb).connect())) {
            JSONObject response = helper.getTableFieldAndType(transMeta, queryParams);

            assertEquals(StepInterface.FAILURE_RESPONSE, response.get("actionStatus"));
            assertEquals("An unexpected error occurred.", response.get("error"));
        }
    }

    @Test
    public void testHandleStepAction_GetTableFieldAndType_Routing() {
        JSONObject response = helper.handleStepAction("getTableFieldAndType", transMeta, null);

        assertEquals(StepInterface.FAILURE_RESPONSE, response.get("actionStatus"));
        assertEquals("Missing query parameters.", response.get("error"));
    }

    private RowMetaInterface validRowMeta() {
        RowMeta rowMeta = new RowMeta();
        rowMeta.addValueMeta(new ValueMetaString("col1"));
        return rowMeta;
    }
}
