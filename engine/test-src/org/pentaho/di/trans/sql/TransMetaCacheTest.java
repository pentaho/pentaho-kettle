package org.pentaho.di.trans.sql;

import junit.framework.TestCase;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.trans.TransMeta;

public class TransMetaCacheTest extends TestCase {

  @Test
  public void testBasic() throws Exception {
    KettleEnvironment.init();
    TransMetaCache cache = new TransMetaCache( null, 100 );
    assertTrue( cache.getFileMap().isEmpty() );

    String filename = "testfiles/blackbox/tests/trans/steps/csvinput/csvinput-test1.ktr";

    TransMeta transMeta = cache.loadTransMeta( filename );
    assertNotNull( transMeta );
    cache.loadTransMeta( filename );
    cache.loadTransMeta( filename );
    cache.loadTransMeta( filename );
    TransMeta verify = cache.loadTransMeta( filename );
    assertEquals( transMeta, verify );
    assertTrue( transMeta == verify ); // same object even!

    assertEquals( 1, cache.getFileMap().size() );
  }
}
