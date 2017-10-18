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

package org.pentaho.di.lineage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * This class will help calculate and contain the data lineage for all values in the transformation.<br>
 * What we will get is a List of ValueLineage objects for all the values steps in the transformation.<br>
 * Each of these ValueLineage objects contains a list of all the steps it passed through.<br>
 * As such, it's a hierarchical view of the transformation.<br>
 *
 * This view will allow us to see immediately where a certain value is being manipulated.<br>
 *
 * @author matt
 *
 */
public class TransDataLineage {
  private TransMeta transMeta;

  private List<ValueLineage> valueLineages;

  private Map<ValueMetaInterface, List<StepMeta>> fieldStepsMap;

  public TransDataLineage( TransMeta transMeta ) {
    this.transMeta = transMeta;
    this.valueLineages = new ArrayList<ValueLineage>();
  }

  public TransMeta getTransMeta() {
    return transMeta;
  }

  public void setTransMeta( TransMeta transMeta ) {
    this.transMeta = transMeta;
  }

  /**
   * @return the valueLineages
   */
  public List<ValueLineage> getValueLineages() {
    return valueLineages;
  }

  /**
   * @param valueLineages
   *          the valueLineages to set
   */
  public void setValueLineages( List<ValueLineage> valueLineages ) {
    this.valueLineages = valueLineages;
  }

  /**
   * Using the transformation, we will calculate the data lineage for each field in each step.
   *
   * @throws KettleStepException
   *           In case there is an exception calculating the lineage. This is usually caused by unavailable data sources
   *           etc.
   */
  public void calculateLineage() throws KettleStepException {

    // After sorting the steps we get a map of all the previous steps of a certain step.
    //
    final Map<StepMeta, Map<StepMeta, Boolean>> stepMap = transMeta.sortStepsNatural();

    // However, the we need a sorted list of previous steps per step, not a map.
    // So lets sort the maps, turn them into lists...
    //
    Map<StepMeta, List<StepMeta>> previousStepListMap = new HashMap<StepMeta, List<StepMeta>>();
    for ( StepMeta stepMeta : stepMap.keySet() ) {
      List<StepMeta> previousSteps = new ArrayList<StepMeta>();
      previousStepListMap.put( stepMeta, previousSteps );

      previousSteps.addAll( stepMap.get( stepMeta ).keySet() );

      // Sort this list...
      //
      Collections.sort( previousSteps, new Comparator<StepMeta>() {

        public int compare( StepMeta o1, StepMeta o2 ) {

          Map<StepMeta, Boolean> beforeMap = stepMap.get( o1 );
          if ( beforeMap != null ) {
            if ( beforeMap.get( o2 ) == null ) {
              return -1;
            } else {
              return 1;
            }
          } else {
            return o1.getName().compareToIgnoreCase( o2.getName() );
          }
        }
      } );

      System.out.println( "Step considered: " + stepMeta.getName() );
      for ( StepMeta prev : previousSteps ) {
        System.out.println( "      --> previous step: " + prev.getName() );
      }
    }

    fieldStepsMap = new HashMap<ValueMetaInterface, List<StepMeta>>();

    List<StepMeta> usedSteps = transMeta.getUsedSteps();
    for ( StepMeta stepMeta : usedSteps ) {
      calculateLineage( stepMeta );
    }
  }

  /**
   * Calculate the lineage for the specified step only...
   *
   * @param stepMeta
   *          The step to calculate the lineage for.
   * @throws KettleStepException
   *           In case there is an exception calculating the lineage. This is usually caused by unavailable data sources
   *           etc.
   */
  private void calculateLineage( StepMeta stepMeta ) throws KettleStepException {
    RowMetaInterface outputMeta = transMeta.getStepFields( stepMeta );

    // The lineage is basically a calculation of origin for each output of a certain step.
    //
    for ( ValueMetaInterface valueMeta : outputMeta.getValueMetaList() ) {

      StepMeta originStepMeta = transMeta.findStep( valueMeta.getOrigin(), stepMeta );
      if ( originStepMeta != null ) {
        /* List<StepMeta> list = */fieldStepsMap.get( originStepMeta );
      }
    }
  }

}
