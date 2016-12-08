package org.pentaho.di.engine.kettlenative.impl;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.TransMeta;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EngineITest {

  TransMeta testMeta;

  @Before
  public void before() throws KettleException {
    KettleEnvironment.init();
    testMeta = new TransMeta( getClass().getClassLoader().getResource( "test2.ktr" ).getFile() );
    System.out.println( EngineITest.class.getClassLoader().getResource("org/apache/xerce‌​s/jaxp/DocumentBuild‌​erFactoryImpl.class"));
  }

  @Test
  public void testExec() throws KettleXMLException, KettleMissingPluginsException, InterruptedException {
    TransMeta meta = new TransMeta( getClass().getClassLoader().getResource( "test2.ktr" ).getFile() );
    ITransformation trans = Transformation.convert( meta );

    IEngine engine = new Engine();
    engine.execute( trans );


  }

}