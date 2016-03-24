/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.row.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleValueException;

public class ValueMetaInternetAddressTest {

  @Test
  public void testCompare() throws UnknownHostException, KettleValueException {
    ValueMetaInternetAddress vm = new ValueMetaInternetAddress();
    InetAddress smaller = InetAddress.getByName( "127.0.0.1" );
    InetAddress larger = InetAddress.getByName( "127.0.1.1" );
    assertTrue( vm.isSortedAscending() );
    assertFalse( vm.isSortedDescending() );
    assertEquals( 0, vm.compare( null, null ) );
    assertEquals( -1, vm.compare( null, smaller ) );
    assertEquals( 1, vm.compare( smaller, null ) );
    assertEquals( 0, vm.compare( smaller, smaller ) );
    assertEquals( -1, vm.compare( smaller, larger ) );
    assertEquals( 1, vm.compare( larger, smaller ) );

    vm.setSortedDescending( true );
    assertFalse( vm.isSortedAscending() );
    assertTrue( vm.isSortedDescending() );
    assertEquals( 0, vm.compare( null, null ) );
    assertEquals( 1, vm.compare( null, smaller ) );
    assertEquals( -1, vm.compare( smaller, null ) );
    assertEquals( 0, vm.compare( smaller, smaller ) );
    assertEquals( 1, vm.compare( smaller, larger ) );
    assertEquals( -1, vm.compare( larger, smaller ) );
  }
}
