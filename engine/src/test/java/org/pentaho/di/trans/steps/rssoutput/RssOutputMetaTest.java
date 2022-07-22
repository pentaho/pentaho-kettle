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
package org.pentaho.di.trans.steps.rssoutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class RssOutputMetaTest implements InitializerInterface<StepMetaInterface> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  LoadSaveTester loadSaveTester;
  Class<RssOutputMeta> testMetaClass = RssOutputMeta.class;
  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "channeltitle", "channeldescription", "channellink", "channelpubdate", "channelcopyright", "channelimagetitle",
            "channelimagelink", "channelimageurl", "channelimagedescription", "channellanguage", "channelauthor", "itemtitle",
            "itemdescription", "itemlink", "itempubdate", "itemauthor", "geopointlat", "geopointlong", "AddToResult",
            "fileName", "extension", "stepNrInFilename", "partNrInFilename", "dateInFilename", "timeInFilename",
            "createparentfolder", "version", "encoding", "addimage", "addgeorss", "usegeorssgml", "filenamefield",
            "isfilenameinfield", "customrss", "displayitem", "ChannelCustomFields", "NameSpaces", "NameSpacesTitle",
            "ChannelCustomTags", "ItemCustomFields", "ItemCustomTags" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "channeltitle", "getChannelTitle" );
        put( "channeldescription", "getChannelDescription" );
        put( "channellink", "getChannelLink" );
        put( "channelpubdate", "getChannelPubDate" );
        put( "channelcopyright", "getChannelCopyright" );
        put( "channelimagetitle", "getChannelImageTitle" );
        put( "channelimagelink", "getChannelImageLink" );
        put( "channelimageurl", "getChannelImageUrl" );
        put( "channelimagedescription", "getChannelImageDescription" );
        put( "channellanguage", "getChannelLanguage" );
        put( "channelauthor", "getChannelAuthor" );
        put( "itemtitle", "getItemTitle" );
        put( "itemdescription", "getItemDescription" );
        put( "itemlink", "getItemLink" );
        put( "itempubdate", "getItemPubDate" );
        put( "itemauthor", "getItemAuthor" );
        put( "geopointlat", "getGeoPointLat" );
        put( "geopointlong", "getGeoPointLong" );
        put( "AddToResult", "AddToResult" );
        put( "fileName", "getFileName" );
        put( "extension", "getExtension" );
        put( "stepNrInFilename", "isStepNrInFilename" );
        put( "partNrInFilename", "isPartNrInFilename" );
        put( "dateInFilename", "isDateInFilename" );
        put( "timeInFilename", "isTimeInFilename" );
        put( "createparentfolder", "isCreateParentFolder" );
        put( "version", "getVersion" );
        put( "encoding", "getEncoding" );
        put( "addimage", "AddImage" );
        put( "addgeorss", "AddGeoRSS" );
        put( "usegeorssgml", "useGeoRSSGML" );
        put( "filenamefield", "getFileNameField" );
        put( "isfilenameinfield", "isFilenameInField" );
        put( "customrss", "isCustomRss" );
        put( "displayitem", "isDisplayItem" );
        put( "ChannelCustomFields", "getChannelCustomFields" );
        put( "NameSpaces", "getNameSpaces" );
        put( "NameSpacesTitle", "getNameSpacesTitle" );
        put( "ChannelCustomTags", "getChannelCustomTags" );
        put( "ItemCustomFields", "getItemCustomFields" );
        put( "ItemCustomTags", "getItemCustomTags" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "channeltitle", "setChannelTitle" );
        put( "channeldescription", "setChannelDescription" );
        put( "channellink", "setChannelLink" );
        put( "channelpubdate", "setChannelPubDate" );
        put( "channelcopyright", "setChannelCopyright" );
        put( "channelimagetitle", "setChannelImageTitle" );
        put( "channelimagelink", "setChannelImageLink" );
        put( "channelimageurl", "setChannelImageUrl" );
        put( "channelimagedescription", "setChannelImageDescription" );
        put( "channellanguage", "setChannelLanguage" );
        put( "channelauthor", "setChannelAuthor" );
        put( "itemtitle", "setItemTitle" );
        put( "itemdescription", "setItemDescription" );
        put( "itemlink", "setItemLink" );
        put( "itempubdate", "setItemPubDate" );
        put( "itemauthor", "setItemAuthor" );
        put( "geopointlat", "setGeoPointLat" );
        put( "geopointlong", "setGeoPointLong" );
        put( "AddToResult", "setAddToResult" );
        put( "fileName", "setFileName" );
        put( "extension", "setExtension" );
        put( "stepNrInFilename", "setStepNrInFilename" );
        put( "partNrInFilename", "setPartNrInFilename" );
        put( "dateInFilename", "setDateInFilename" );
        put( "timeInFilename", "setTimeInFilename" );
        put( "createparentfolder", "setCreateParentFolder" );
        put( "version", "setVersion" );
        put( "encoding", "setEncoding" );
        put( "addimage", "setAddImage" );
        put( "addgeorss", "setAddGeoRSS" );
        put( "usegeorssgml", "setUseGeoRSSGML" );
        put( "filenamefield", "setFileNameField" );
        put( "isfilenameinfield", "setFilenameInField" );
        put( "customrss", "setCustomRss" );
        put( "displayitem", "setDisplayItem" );
        put( "ChannelCustomFields", "setChannelCustomFields" );
        put( "NameSpaces", "setNameSpaces" );
        put( "NameSpacesTitle", "setNameSpacesTitle" );
        put( "ChannelCustomTags", "setChannelCustomTags" );
        put( "ItemCustomFields", "setItemCustomFields" );
        put( "ItemCustomTags", "setItemCustomTags" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );


    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "ChannelCustomFields", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "NameSpaces", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "NameSpacesTitle", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "ChannelCustomTags", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "ItemCustomFields", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "ItemCustomTags", stringArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof RssOutputMeta ) {
      RssOutputMeta rom = (RssOutputMeta) someMeta;
      rom.allocate( 5 );
      rom.allocateitem( 5 );
      rom.allocatenamespace( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

}
