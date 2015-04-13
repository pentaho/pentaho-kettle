/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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
