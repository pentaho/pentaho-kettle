/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.job.entries.ping;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.steps.loadsave.MemoryRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
      assertEquals( value, jEPing.getNbrPackets() );
      assertEquals( value.trim(), jEPing.getRealNbrPackets() );
    }
  }

  @Test
  public void test_getRealNbrPackets_ForValueOtherThanPositiveIntegers() {
    for ( String value : notPosInt) {
      jEPing.setNbrPackets( value );
      assertEquals( "2", jEPing.getRealNbrPackets() );
      assertEquals( value, jEPing.getNbrPackets() );
    }
  }

  @Test
  public void test_getNbrPackets_ForNull() {
    jEPing.setNbrPackets( null );
    assertEquals( "2", jEPing.getRealNbrPackets() );
    assertNull( jEPing.getNbrPackets() );
  }

  @Test
  public void test_getTimeOut_ForPositiveIntegers() {
    for ( String value : posInt ) {
      jEPing.setTimeOut( value );
      assertEquals( value, jEPing.getTimeOut() );
      assertEquals( value.trim(), jEPing.getRealTimeOut() );
    }
  }
  @Test
  public void test_getRealTimeOut_ForValueOtherThanPositiveIntegers() {
    for ( String value : notPosInt ) {
      jEPing.setTimeOut( value );
      assertEquals( "3000", jEPing.getRealTimeOut() );
      assertEquals( value, jEPing.getTimeOut() );
    }
  }

  @Test
  public void test_getTimeOut_ForNull() {
    jEPing.setTimeOut( null );
    assertEquals( "3000", jEPing.getRealTimeOut() );
    assertNull( jEPing.getTimeOut() );
  }

  @Test
  public void testVariableValuesAreResolvedBeforeValidation() {
    jEPing.setNbrPackets( "${ping}" );
    jEPing.setTimeOut( "${ping}" );
    for ( String value : posInt ) {
      jEPing.setVariable( "ping", value );
      assertEquals( value.trim(), jEPing.getRealNbrPackets() );
      assertEquals( value.trim(), jEPing.getRealTimeOut() );
      assertConfiguredValues( jEPing, "${ping}" );
    }
  }

  @Test
  public void testInvalidVariableValuesUseDefaultsWithoutChangingMetadata() {
    jEPing.setNbrPackets( "${ping}" );
    jEPing.setTimeOut( "${ping}" );
    for ( String value : notPosInt ) {
      jEPing.setVariable( "ping", value );
      assertRuntimeDefaults( jEPing );
      assertConfiguredValues( jEPing, "${ping}" );
    }
    // A later execution can resolve the same expression to a valid value.
    jEPing.setVariable( "ping", "10" );
    assertEquals( "10", jEPing.getRealNbrPackets() );
    assertEquals( "10", jEPing.getRealTimeOut() );
  }

  @Test
  public void testUnresolvedVariablesUseDefaultsWithoutChangingMetadata() {
    jEPing.setNbrPackets( "${unresolved_ping_packets}" );
    jEPing.setTimeOut( "${unresolved_ping_timeout}" );
    assertRuntimeDefaults( jEPing );
    assertEquals( "${unresolved_ping_packets}", jEPing.getNbrPackets() );
    assertEquals( "${unresolved_ping_timeout}", jEPing.getTimeOut() );
  }

  @Test
  public void testXmlRoundTripPreservesConfiguredValues() throws Exception {
    for ( String value : new String[] { "0", "${ping}", "25", "   22   " } ) {
      jEPing.setNbrPackets( value );
      jEPing.setTimeOut( value );
      assertConfiguredValues( jEPing, value );
      String xml = "<entry>" + jEPing.getXML() + "</entry>";
      JobEntryPing restored = new JobEntryPing();
      restored.loadXML( XMLHandler.loadXMLString( xml ).getDocumentElement(), null, null, null, null );
      assertConfiguredValues( restored, value );
      // Reading runtime values must not change a subsequent export.
      restored.getRealNbrPackets();
      restored.getRealTimeOut();
      assertEquals( xml, "<entry>" + restored.getXML() + "</entry>" );
    }
  }

  @Test
  public void testRepositoryRoundTripPreservesConfiguredValues() throws Exception {
    for ( String value : new String[] { "0", "${ping}", "25", "   22   " } ) {
      jEPing.setNbrPackets( value );
      jEPing.setTimeOut( value );
      assertConfiguredValues( jEPing, value );
      Repository repository = new MemoryRepository();
      jEPing.saveRep( repository, null, null );
      JobEntryPing restored = new JobEntryPing();
      restored.loadRep( repository, null, null, null, null );
      assertConfiguredValues( restored, value );
      restored.getRealNbrPackets();
      restored.getRealTimeOut();
      repository = new MemoryRepository();
      restored.saveRep( repository, null, null );
      assertEquals( value, repository.getJobEntryAttributeString( null, "nbr_packets" ) );
      assertEquals( value, repository.getJobEntryAttributeString( null, "nbrpaquets" ) );
      assertEquals( value, repository.getJobEntryAttributeString( null, "timeout" ) );
    }
  }

  @Test
  public void testLegacyXmlPreservesConfiguredValues() throws Exception {
    for ( String value : new String[] { "0", "${ping}" } ) {
      String xml = "<entry><nbrpaquets>" + value + "</nbrpaquets><timeout>" + value + "</timeout></entry>";
      JobEntryPing restored = new JobEntryPing();
      restored.loadXML( XMLHandler.loadXMLString( xml ).getDocumentElement(), null, null, null, null );
      assertConfiguredValues( restored, value );
    }
  }

  private void assertConfiguredValues( JobEntryPing entry, String value ) {
    assertEquals( value, entry.getNbrPackets() );
    assertEquals( value, entry.getTimeOut() );
  }

  private void assertRuntimeDefaults( JobEntryPing entry ) {
    assertEquals( "2", entry.getRealNbrPackets() );
    assertEquals( "3000", entry.getRealTimeOut() );
  }
}
