package org.pentaho.di.trans.steps.reservoirsampling;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class ReservoirSamplingMetaTest {

  @Test
  public void testLoadSaveMeta() throws KettleException {

    List<String> attributes = Arrays.asList( "sample_size", "seed" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "sample_size", "getSampleSize" );
    getterMap.put( "seed", "getSeed" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "sample_size", "setSampleSize" );
    setterMap.put( "seed", "setSeed" );

    LoadSaveTester tester = new LoadSaveTester( ReservoirSamplingMeta.class, attributes, getterMap, setterMap );
    tester.testXmlRoundTrip();
    tester.testRepoRoundTrip();
  }
}
