/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.row;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;
import static org.junit.Assert.assertNull;

public class RowMetaEsr7102IT {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  RowMetaInterface rowMeta = new RowMeta();
  ValueMetaInterface string;
  ValueMetaInterface integer;
  ValueMetaInterface date;

  ValueMetaInterface charly;
  public Exception remoteException;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    string = ValueMetaFactory.createValueMeta( "string", ValueMetaInterface.TYPE_STRING );
    rowMeta.addValueMeta( string );
    integer = ValueMetaFactory.createValueMeta( "integer", ValueMetaInterface.TYPE_INTEGER );
    rowMeta.addValueMeta( integer );
    date = ValueMetaFactory.createValueMeta( "date", ValueMetaInterface.TYPE_DATE );
    rowMeta.addValueMeta( date );

    charly = ValueMetaFactory.createValueMeta( "charly", ValueMetaInterface.TYPE_SERIALIZABLE );
  }

  @Test
  /*
  This test runs a series addMetaValue, indexOfValue, and removeValueMeta calls while another thread clones the
  rowMeta.  This simulates the environment that cause the failure in ESR-7102.  When it failed it would rarely get by
  5000 iterations
   */
  public void esr7201FailTest() throws Exception {
    int count = 50000;
    ThreadRunnngIndexOfValue threadRunnngIndexOfValue = new ThreadRunnngIndexOfValue( rowMeta, count, this );
    ThreadRunnngClone threadRunnngClone = new ThreadRunnngClone( rowMeta, count, this );
    threadRunnngIndexOfValue.start();
    threadRunnngClone.start();
    threadRunnngClone.join();
    threadRunnngIndexOfValue.join();
    if ( remoteException != null ) {
      remoteException.printStackTrace();
      throw remoteException;
    }
    assertNull( remoteException );
  }

  public class ThreadRunnngIndexOfValue extends Thread {
    private RowMetaInterface rowMeta;
    private int count;
    private RowMetaEsr7102IT testInstance;

    public ThreadRunnngIndexOfValue( RowMetaInterface rowMeta, int count, RowMetaEsr7102IT testInstance ) {
      this.rowMeta = rowMeta;
      this.count = count;
      this.testInstance = testInstance;
    }

    public void run() {
      for ( int i = 0; i < count; i++ ) {
        if ( testInstance.remoteException != null ) {
          break;
        }
        System.out.println( "I" + i );
        try {
          if ( i % 2 == 0 ) {
            rowMeta.addValueMeta( 1, charly );
            rowMeta.indexOfValue( "charly" );
          } else {
            rowMeta.removeValueMeta( "charly" );
          }
        } catch ( Exception e ) {
          testInstance.remoteException = e;
          break;
        }
      }
    }
  }

  public class ThreadRunnngClone extends Thread {
    private RowMetaInterface rowMeta;
    private int count;
    private RowMetaEsr7102IT testInstance;

    public ThreadRunnngClone( RowMetaInterface rowMeta, int count, RowMetaEsr7102IT testInstance ) {
      this.rowMeta = rowMeta;
      this.count = count;
      this.testInstance = testInstance;
    }

    public void run() {
      Object[] o = new Object[] { "string", 123, 0, "this" };
      for ( int i = 0; i < count; i++ ) {
        if ( testInstance.remoteException != null ) {
          break;
        }
        System.out.println( "C" + i );
        try {
          rowMeta.cloneRow( o );
        } catch ( Exception e ) {
          testInstance.remoteException = e;
          break;
        }
      }
    }
  }
}
