/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.trans.dialog;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransPreviewProgressDialogTest {

  private static String ERROR_MSG;
  private static String EXPECTED_ERROR_MSG;
  private static String FAILED_TO_INIT_MSG;
  private static String SAME_DATE_STR = "2021/07/27 10:58:20";
  private static String BEFORE_DATE_STR = "2021/07/27 10:58:19";
  private static String AFTER_DATE_STR = "2021/07/27 10:58:21";

  @Before
  public void setup() {
    FAILED_TO_INIT_MSG = "We failed to initialize at least one step.  Execution can not begin!\n";
    EXPECTED_ERROR_MSG =
      "2021/07/27 10:58:20 - /home/19222.ktr : 19222 - Dispatching started for trans [/home/19222.ktr : 19222]\n"
      + "2021/07/27 10:58:20 - Table input.0 - ERROR : You need to specify a database connection.\n"
      + "2021/07/27 10:58:20 - Table input.0 - ERROR : Error initializing step [Table input]\n"
      + "2021/07/27 10:58:20 - /home/19222.ktr : 19222 - ERROR : Step [Table input.0] failed to initialize!\n"
      + "2021/07/27 10:58:20 - Table input.0 - Finished reading query, closing connection\n";
    // Logging messages has a leading \nat runtime so this is needed for testing
    ERROR_MSG = "\n" + FAILED_TO_INIT_MSG
      + "2021/07/27 09:56:05 - General - Logging plugin type found with ID: CheckpointLogTable\n"
      + "2021/07/27 09:56:09 - RepositoriesMeta - Reading repositories XML file: /.kettle/repositories.xml\n"
      + "2021/07/27 09:56:21 - RepositoriesMeta - Reading repositories XML file: /.kettle/repositories.xml\n"
      + "2021/07/27 09:56:22 - Carte - Installing timer to purge stale objects after 1440 minutes.\n"
      + EXPECTED_ERROR_MSG;


  }

  @Test
  public void parseErrorMessageUsingBeforeDateTest() {
    String result = TransPreviewProgressDialog.parseErrorMessage( ERROR_MSG, parseDate( BEFORE_DATE_STR ) );
    assertEquals( FAILED_TO_INIT_MSG + EXPECTED_ERROR_MSG, result );
  }

  @Test
  public void parseErrorMessageUsingSameDateTest() {
    String result = TransPreviewProgressDialog.parseErrorMessage( ERROR_MSG, parseDate( SAME_DATE_STR ) );
    assertEquals( FAILED_TO_INIT_MSG + EXPECTED_ERROR_MSG, result );
  }

  @Test
  public void parseErrorMessageUsingAfterDateTest() {
    String result = TransPreviewProgressDialog.parseErrorMessage( ERROR_MSG, parseDate( AFTER_DATE_STR ) );
    assertEquals( FAILED_TO_INIT_MSG, result );
  }

  private static Date parseDate( String date ) {
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
    try {
      return sdf.parse( date );
    } catch ( Exception e ) {
      return null;
    }
  }

  @Test
  public void isDateAfterOrSame_BeforeTest() {
    assertTrue( TransPreviewProgressDialog.isDateAfterOrSame( BEFORE_DATE_STR, AFTER_DATE_STR ) );
  }

  @Test
  public void isDateAfterOrSame_AfterTest() {
    assertFalse( TransPreviewProgressDialog.isDateAfterOrSame( AFTER_DATE_STR, BEFORE_DATE_STR ) );
  }

  @Test
  public void isDateAfterOrSame_SameTest() {
    assertTrue( TransPreviewProgressDialog.isDateAfterOrSame( SAME_DATE_STR, SAME_DATE_STR ) );
  }
}
