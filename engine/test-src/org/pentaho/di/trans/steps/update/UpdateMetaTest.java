package org.pentaho.di.trans.steps.update;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

public class UpdateMetaTest extends TestCase {

  private StepMeta stepMeta;
  private Update upd;
  private UpdateData ud;
  private UpdateMeta umi;

  @Before
  protected void setUp() {
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "delete1" );

    Map<String, String> vars = new HashMap<String, String>();
    vars.put( "max.sz", "10" );
    transMeta.injectVariables( vars );

    umi = new UpdateMeta();
    ud = new UpdateData();

    PluginRegistry plugReg = PluginRegistry.getInstance();
    String deletePid = plugReg.getPluginId( StepPluginType.class, umi );

    stepMeta = new StepMeta( deletePid, "delete", umi );
    Trans trans = new Trans( transMeta );
    transMeta.addStep( stepMeta );
    upd = new Update( stepMeta, ud, 1, transMeta, trans );
    upd.copyVariablesFrom( transMeta );
  }

  @Test
  public void testCommitCountFixed() {
    umi.setCommitSize( "100" );
    assertTrue( umi.getCommitSize( upd ) == 100 );
  }

  @Test
  public void testCommitCountVar() {
    umi.setCommitSize( "${max.sz}" );
    assertTrue( umi.getCommitSize( upd ) == 10 );
  }

  @Test
  public void testCommitCountMissedVar() {
    umi.setCommitSize( "missed-var" );
    try {
      umi.getCommitSize( upd );
      fail();
    } catch ( Exception ex ) {
    }
  }

}
