/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.job.entries.ping;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import static org.junit.Assert.assertEquals;
import static org.pentaho.di.core.util.Assert.assertFalse;
import static org.pentaho.di.core.util.Assert.assertTrue;

public class JobEntryPingTest extends JobEntryLoadSaveTestSupport<JobEntryPing> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryPing> getJobEntryClass() {
    return JobEntryPing.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "hostname",
        "nbrPackets",
        "timeout",
        "pingtype" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "hostname", "getHostname",
        "nbrPackets", "getNbrPackets",
        "timeout", "getTimeOut" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "hostname", "setHostname",
        "nbrPackets", "setNbrPackets",
        "timeout", "setTimeOut" );
  }

  private final String[] posInt = { "22", "020", "   22   ", "2147483647" }; //Integer.MAX_VALUE = 2147483648
  private final  String[] notPosInt = { "-2", "0", "5.05", "", "\n", "abc", "ab412c", "2147483648", "-", "?", "3,400" };
  JobEntryPing jEPing = new JobEntryPing( "Ping" );

  @Test
  public void test_isPositiveNumber_ForPositiveIntegers() {
    for ( String value : posInt) {
      assertTrue( JobEntryPing.isPositiveInteger( value ) );
    }
  }

  @Test
  public void test_isPositiveNumber_ForValueOtherThanPositiveIntegers() {
    for ( String value : notPosInt) {
      assertFalse( JobEntryPing.isPositiveInteger( value ) );
    }
  }

  @Test
  public void test_isPositiveNumber_ForNull() {
    assertFalse( JobEntryPing.isPositiveInteger(null ) );
  }

  @Test
  public void test_getNbrPackets_ForPositiveIntegers() {
    for ( String value : posInt) {
      jEPing.setNbrPackets( value );
      assertEquals( value.trim(), jEPing.getNbrPackets() );
    }
  }

  @Test
  public void test_getNbrPackets_ForValueOtherThanPositiveIntegers() {
    for ( String value : notPosInt) {
      jEPing.setNbrPackets( value );
      assertEquals( "2", jEPing.getNbrPackets() );
    }
  }

  @Test
  public void test_getNbrPackets_ForNull() {
    jEPing.setNbrPackets( null );
    assertEquals( "2", jEPing.getNbrPackets() );
  }

  @Test
  public void test_getTimeOut_ForPositiveIntegers() {
    for ( String value : posInt ) {
      jEPing.setTimeOut( value );
      assertEquals( value.trim(), jEPing.getTimeOut() );
    }
  }
  @Test
  public void test_getTimeOut_ForValueOtherThanPositiveIntegers() {
    for ( String value : notPosInt ) {
    jEPing.setTimeOut( value );
    assertEquals( "3000", jEPing.getTimeOut() );
  }
}

  @Test
  public void test_getTimeOut_ForNull() {
    jEPing.setTimeOut( null );
    assertEquals( "3000", jEPing.getTimeOut() );
  }
}
