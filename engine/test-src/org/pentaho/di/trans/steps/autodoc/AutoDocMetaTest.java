package org.pentaho.di.trans.steps.autodoc;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.EnumLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Khayrutdinov
 */
public class AutoDocMetaTest {

  private LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {

    List<String> attributes =
      Arrays.asList( "filenameField", "fileTypeField", "targetFilename", "includingName", "includingDescription",
        "includingExtendedDescription", "includingCreated", "includingModified",
        "includingImage", "includingLoggingConfiguration", "includingLastExecutionResult",
        "includingImageAreaList", "outputType" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "outputType", new EnumLoadSaveValidator<KettleReportBuilder.OutputType>( KettleReportBuilder.OutputType.PDF ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
      new LoadSaveTester( AutoDocMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
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
