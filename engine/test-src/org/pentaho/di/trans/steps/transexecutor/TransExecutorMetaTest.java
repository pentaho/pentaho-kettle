package org.pentaho.di.trans.steps.transexecutor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class TransExecutorMetaTest {

  LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {

    List<String> attributes =
        Arrays.asList( "fileName", "transName", "directoryPath", "groupSize", "groupField", "groupTime",
            "executionTimeField", "executionFilesRetrievedField", "executionLogTextField",
            "executionLogChannelIdField", "executionResultField", "executionNrErrorsField", "executionLinesReadField",
            "executionLinesWrittenField", "executionLinesInputField", "executionLinesOutputField",
            "executionLinesRejectedField", "executionLinesUpdatedField", "executionLinesDeletedField",
            "executionExitStatusField" );

    // executionResultTargetStepMeta -? (see for switch case meta)
    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    loadSaveTester =
        new LoadSaveTester( TransExecutorMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testLoadSaveXML() throws KettleException {
    loadSaveTester.testXmlRoundTrip();
  }

  @Test
  public void testLoadSaveRepo() throws KettleException {
    loadSaveTester.testRepoRoundTrip();
  }

}
