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

package org.pentaho.di.trans.steps.multimerge;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;

public class MultiMergeJoinHelperTest {

    private TransMeta transMeta;
    private MultiMergeJoinHelper underTest;

    @Before
    public void setUp() {
        underTest = new MultiMergeJoinHelper();
        transMeta = mock(TransMeta.class);
    }

    @Test
    public void testHandleStepAction_whenMethodNameIsPreviousKeys() throws Exception {
        String stepName = "multiMergeStep";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("stepName", stepName);

        StepMeta stepMeta = mock(StepMeta.class);
        MultiMergeJoinMeta multiMergeJoinMeta = mock(MultiMergeJoinMeta.class);
        when(stepMeta.getStepMetaInterface()).thenReturn(multiMergeJoinMeta);
        when(transMeta.findStep(stepName)).thenReturn(stepMeta);

        String[] inputStepNames = new String[] { "inputStep1", "inputStep2" };
        when(multiMergeJoinMeta.getInputSteps()).thenReturn(inputStepNames);

        StreamInterface stream1 = mock(StreamInterface.class);
        StepMeta inputStepMeta1 = mock(StepMeta.class);
        when(inputStepMeta1.getName()).thenReturn("inputStep1");
        when(stream1.getStepMeta()).thenReturn(inputStepMeta1);

        RowMetaInterface rowMeta1 = mock(RowMetaInterface.class);
        when(rowMeta1.getFieldNames()).thenReturn(new String[] { "fieldA", "fieldB" });
        when(transMeta.getStepFields(inputStepMeta1)).thenReturn(rowMeta1);

        StreamInterface stream2 = mock(StreamInterface.class);
        StepMeta inputStepMeta2 = mock(StepMeta.class);
        when(inputStepMeta2.getName()).thenReturn("inputStep2");
        when(stream2.getStepMeta()).thenReturn(inputStepMeta2);

        RowMetaInterface rowMeta2 = mock(RowMetaInterface.class);
        when(rowMeta2.getFieldNames()).thenReturn(new String[] { "fieldC" });
        when(transMeta.getStepFields(inputStepMeta2)).thenReturn(rowMeta2);

        List<StreamInterface> infoStreams = Arrays.asList(stream1, stream2);
        StepIOMeta stepIOMeta = mock(StepIOMeta.class);
        when(stepIOMeta.getInfoStreams()).thenReturn(infoStreams);
        when(multiMergeJoinMeta.getStepIOMeta()).thenReturn(stepIOMeta);

        JSONObject response = underTest.handleStepAction("previousKeys", transMeta, queryParams);

        assertNotNull(response);
        assertEquals(SUCCESS_RESPONSE, response.get(ACTION_STATUS));
        assertTrue(response.containsKey("stepKeys"));
        JSONObject stepKeys = (JSONObject) response.get("stepKeys");
        assertTrue(stepKeys.containsKey("inputStep1"));
        assertTrue(stepKeys.containsKey("inputStep2"));
        assertEquals(Arrays.asList("fieldA", "fieldB"), stepKeys.get("inputStep1"));
        assertEquals(Arrays.asList("fieldC"), stepKeys.get("inputStep2"));
    }

    @Test
    public void testHandleStepAction_whenMethodNameIsInvalid() {
        JSONObject response = underTest.handleStepAction("invalidMethod", transMeta, null);

        assertNotNull(response);
        assertEquals("Action failed with method not found", response.get(ACTION_STATUS));
    }

    @Test
    public void testPreviousKeysAction_withNoStepName() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        JSONObject response = underTest.previousKeysAction(transMeta, queryParams);

        assertNotNull(response);
        assertEquals(SUCCESS_RESPONSE, response.get(ACTION_STATUS));
        assertTrue(response.containsKey("stepKeys"));
        JSONObject stepKeys = (JSONObject) response.get("stepKeys");
        assertTrue(stepKeys.isEmpty());
    }

    @Test
    public void testPreviousKeysAction_withStepNotFound() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("stepName", "missingStep");
        when(transMeta.findStep("missingStep")).thenReturn(null);

        JSONObject response = underTest.previousKeysAction(transMeta, queryParams);

        assertNotNull(response);
        assertEquals(SUCCESS_RESPONSE, response.get(ACTION_STATUS));
        assertTrue(response.containsKey("stepKeys"));
        JSONObject stepKeys = (JSONObject) response.get("stepKeys");
        assertTrue(stepKeys.isEmpty());
    }
}
