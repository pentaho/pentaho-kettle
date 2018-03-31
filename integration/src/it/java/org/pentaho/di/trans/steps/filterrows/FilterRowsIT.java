/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.filterrows;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

public class FilterRowsIT {

  public static int MAX_COUNT = 100;

  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta = {
      new ValueMetaString( "KEY1" ),
      new ValueMetaString( "KEY2" ),
    };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[ i ] );
    }

    return rm;
  }

  public List<RowMetaAndData> createIntegerData() {
    // Create
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    String old_key1 = null;

    RowMetaInterface rm = createRowMetaInterface();

    Random rand = new Random();
    for ( int idx = 0; idx < MAX_COUNT; idx++ ) {
      int key1 = Math.abs( rand.nextInt() % 1000000 );
      int key2 = Math.abs( rand.nextInt() % 1000000 );

      String key1_string = "" + key1 + "." + idx;
      String key2_string = "" + key2 + "." + idx;
      if ( ( ( idx % 100 ) == 0 ) && old_key1 != null ) {
        // have duplicate key1's sometimes
        key1_string = old_key1;
      }
      Object[] r1 = new Object[] { key1_string, key2_string };
      list.add( new RowMetaAndData( rm, r1 ) );
      old_key1 = key1_string;
    }
    return list;
  }

  @Test
  public void testFilterConditionRefersToNonExistingFields() throws Exception {
    KettleEnvironment.init();

    // Create a new transformation...
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "filterrowstest" );
    PluginRegistry registry = PluginRegistry.getInstance();

    // create an injector step...
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.
    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    // Create a filter rows step
    String filterStepName = "filter rows step";
    FilterRowsMeta frm = new FilterRowsMeta();
    Condition condition = new Condition();
    String nonExistingFieldName = "non-existing-field";
    condition.setLeftValuename( nonExistingFieldName );
    condition.setFunction( 8 ); //IS NOT
    condition.setRightValuename( null );
    condition.setOperator( 0 );
    frm.setCondition( condition );

    String filterRowsStepPid = registry.getPluginId( StepPluginType.class, frm );
    StepMeta filterRowsStep = new StepMeta( filterRowsStepPid, filterStepName, frm );
    transMeta.addStep( filterRowsStep );

    TransHopMeta hi = new TransHopMeta( injectorStep, filterRowsStep );
    transMeta.addTransHop( hi );

    // Now execute the transformation
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );

    // add rows
    List<RowMetaAndData> inputList = createIntegerData();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( 1, trans.getErrors() ); //expect errors

  }

}
