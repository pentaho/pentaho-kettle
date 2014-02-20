package org.pentaho.di.trans.steps.delete;

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

public class DeleteMetaTest extends TestCase {

  private StepMeta stepMeta;
  private Delete del;
  private DeleteData dd;
  private DeleteMeta dmi;

  @Before
  protected void setUp() {
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "delete1" );

    Map<String, String> vars = new HashMap<String, String>();
    vars.put( "max.sz", "10" );
    transMeta.injectVariables( vars );

    dmi = new DeleteMeta();
    dd = new DeleteData();

    PluginRegistry plugReg = PluginRegistry.getInstance();
    String deletePid = plugReg.getPluginId( StepPluginType.class, dmi );

    stepMeta = new StepMeta( deletePid, "delete", dmi );
    Trans trans = new Trans( transMeta );
    transMeta.addStep( stepMeta );
    del = new Delete( stepMeta, dd, 1, transMeta, trans );
    del.copyVariablesFrom( transMeta );
  }

  @Test
  public void testCommitCountFixed() {
    dmi.setCommitSize( "100" );
    assertTrue( dmi.getCommitSize( del ) == 100 );
  }

  @Test
  public void testCommitCountVar() {
    dmi.setCommitSize( "${max.sz}" );
    assertTrue( dmi.getCommitSize( del ) == 10 );
  }

  @Test
  public void testCommitCountMissedVar() {
    dmi.setCommitSize( "missed-var" );
    try {
      dmi.getCommitSize( del );
      fail();
    } catch ( Exception ex ) {
    }
  }

}
