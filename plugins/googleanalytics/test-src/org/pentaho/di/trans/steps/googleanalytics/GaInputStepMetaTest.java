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

package org.pentaho.di.trans.steps.googleanalytics;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.metastore.api.IMetaStore;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

/**
 * @author Andrey Khayrutdinov
 */
public class GaInputStepMetaTest {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void cloning() throws Exception {
    GaInputStepMeta sample = createSampleMeta();
    GaInputStepMeta clone = (GaInputStepMeta) sample.clone();
    assertMetasAreEqual( sample, clone );
  }

  @Test
  public void serialization_Xml() throws Exception {
    GaInputStepMeta sample = createSampleMeta();
    GaInputStepMeta another = new GaInputStepMeta();

    String xml = "<step>" + sample.getXML() + "</step>";
    another.loadXML( XMLHandler.getSubNode(
      XMLHandler.loadXMLFile( new ByteArrayInputStream( xml.getBytes() ) ), "step" ), null, (IMetaStore) null );

    assertMetasAreEqual( sample, another );
  }


  private GaInputStepMeta createSampleMeta() {
    GaInputStepMeta meta = new GaInputStepMeta();
    meta.setOauthServiceAccount( "account" );
    meta.setOAuthKeyFile( "/dev/null" );
    meta.setGaAppName( "appName" );
    meta.setGaProfileTableId( "profileTableId" );
    meta.setGaProfileName( "profileName" );
    meta.setUseCustomTableId( true );
    meta.setGaCustomTableId( "customTableId" );
    meta.setStartDate( "2000-01-01" );
    meta.setEndDate( "2010-01-01" );
    meta.setDimensions( "dimensions" );
    meta.setMetrics( "metrics" );
    meta.setFilters( "filters" );
    meta.setSort( "sort" );
    meta.setUseSegment( true );
    meta.setUseCustomSegment( true );
    meta.setRowLimit( 10 );
    meta.setCustomSegment( "customSegment" );
    meta.setSegmentId( "segmentId" );
    meta.setSegmentName( "segmentName" );

    meta.allocate( 2 );
    for ( int i = 0; i < 2; i++ ) {
      String str = Integer.toString( i );
      meta.getFeedField()[ i ] = str;
      meta.getFeedFieldType()[ i ] = str;
      meta.getOutputField()[ i ] = str;
      meta.getConversionMask()[ i ] = str;
    }
    meta.getOutputType()[ 0 ] = ValueMetaInterface.TYPE_INTEGER;
    meta.getOutputType()[ 1 ] = ValueMetaInterface.TYPE_STRING;

    return meta;
  }

  private void assertMetasAreEqual( GaInputStepMeta meta1, GaInputStepMeta meta2 ) {
    boolean eq = EqualsBuilder.reflectionEquals( meta1, meta2 );
    assertTrue( eq );
  }
}
