/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2021 by Hitachi Vantara : http://www.pentaho.com
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
