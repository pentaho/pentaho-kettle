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



package org.pentaho.di.trans.steps.jsonoutput;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.steps.file.BaseFileOutputMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.spy;

public class JsonOutputMetaTest {

  JsonOutputMeta jsonOutputMeta;
  SimpleDateFormat dateFormatter;
  SimpleDateFormat timeFormatter;


  @Before
  public void setUp() {
    jsonOutputMeta = spy( new JsonOutputMeta() );
    dateFormatter = new SimpleDateFormat();
    timeFormatter = new SimpleDateFormat();

    dateFormatter.applyPattern( BaseFileOutputMeta.DEFAULT_DATE_FORMAT );
    timeFormatter.applyPattern( BaseFileOutputMeta.DEFAULT_TIME_FORMAT );
  }

  @Test
  public void testBuildFilenameWithoutDateAndTimePassingFilenameAndDate() {

    String fileName = UUID.randomUUID().toString();
    String extension = UUID.randomUUID().toString();
    Date date = new Date();

    jsonOutputMeta.setDateInFilename( false );
    jsonOutputMeta.setTimeInFilename( false );
    jsonOutputMeta.setExtension( extension );

    String expected = fileName + '.' + extension;
    String result = jsonOutputMeta.buildFilename( fileName, date );
    Assert.assertEquals( expected, result );

  }

  @Test
  public void testBuildFilenameWithoutTimePassingFilenameAndDate() {

    String fileName = UUID.randomUUID().toString();
    String extension = UUID.randomUUID().toString();
    Date date = new Date();

    jsonOutputMeta.setDateInFilename( true );
    jsonOutputMeta.setTimeInFilename( false );
    jsonOutputMeta.setExtension( extension );

    String expected = fileName + '_' + dateFormatter.format( date ) + '.' + extension;
    String result = jsonOutputMeta.buildFilename( fileName, date );
    Assert.assertEquals( expected, result );

  }

  @Test
  public void testBuildFilenameWithoutDatePassingFilenameAndDate() {

    String fileName = UUID.randomUUID().toString();
    String extension = UUID.randomUUID().toString();
    Date date = new Date();

    jsonOutputMeta.setDateInFilename( false );
    jsonOutputMeta.setTimeInFilename( true );
    jsonOutputMeta.setExtension( extension );

    String expected = fileName + '_' + timeFormatter.format( date ) + '.' + extension;
    String result = jsonOutputMeta.buildFilename( fileName, date );
    Assert.assertEquals( expected, result );

  }

  @Test
  public void testBuildFilenamePassingFilenameAndDate() {

    String fileName = UUID.randomUUID().toString();
    String extension = UUID.randomUUID().toString();
    Date date = new Date();

    jsonOutputMeta.setDateInFilename( true );
    jsonOutputMeta.setTimeInFilename( true );
    jsonOutputMeta.setExtension( extension );

    String expected = fileName + '_' + dateFormatter.format( date ) + '_' + timeFormatter.format( date ) + '.' + extension;
    String result = jsonOutputMeta.buildFilename( fileName, date );
    Assert.assertEquals( expected, result );

  }
}
