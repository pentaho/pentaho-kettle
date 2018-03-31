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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Output rows to RSS feed and create a file.
 *
 * @author Samatar
 * @since 6-nov-2007
 */

public class RssOutputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = RssOutput.class; // for i18n purposes, needed by Translator2!!

  private String channeltitle;
  private String channeldescription;
  private String channellink;
  private String channelpubdate;
  private String channelcopyright;
  private String channelimagetitle;
  private String channelimagelink;
  private String channelimageurl;
  private String channelimagedescription;
  private String channellanguage;
  private String channelauthor;

  private String itemtitle;
  private String itemdescription;
  private String itemlink;
  private String itempubdate;
  private String itemauthor;
  private String geopointlat;
  private String geopointlong;

  private boolean AddToResult;

  /** The base name of the output file */
  private String fileName;

  /** The file extention in case of a generated filename */
  private String extension;

  /** Flag: add the stepnr in the filename */
  private boolean stepNrInFilename;

  /** Flag: add the partition number in the filename */
  private boolean partNrInFilename;

  /** Flag: add the date in the filename */
  private boolean dateInFilename;

  /** Flag: add the time in the filename */
  private boolean timeInFilename;

  /** Flag: create parent folder if needed */
  private boolean createparentfolder;

  /** Rss version **/
  private String version;

  /** Rss encoding **/
  private String encoding;

  /** Flag : add image to RSS feed **/
  private boolean addimage;

  private boolean addgeorss;

  private boolean usegeorssgml;

  /** The field that contain filename */
  private String filenamefield;

  /** Flag : is filename defined in a field **/
  private boolean isfilenameinfield;

  /** which fields do we use for Channel Custom ? */
  private String[] ChannelCustomFields;

  /** add namespaces? */
  private String[] NameSpaces;

  private String[] NameSpacesTitle;

  /** which fields do we use for getChannelCustomTags Custom ? */
  private String[] ChannelCustomTags;

  /** which fields do we use for Item Custom Field? */
  private String[] ItemCustomFields;

  /** which fields do we use for Item Custom tag ? */
  private String[] ItemCustomTags;

  /** create custom RSS ? */
  private boolean customrss;

  /** display item tag in output ? */
  private boolean displayitem;

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {

    RssOutputMeta retval = (RssOutputMeta) super.clone();
    int nrfields = ChannelCustomFields.length;
    retval.allocate( nrfields );

    // Read custom channel fields
    System.arraycopy( ChannelCustomFields, 0, retval.ChannelCustomFields, 0, nrfields );
    System.arraycopy( ChannelCustomTags, 0, retval.ChannelCustomTags, 0, nrfields );

    // items
    int nritemfields = ItemCustomFields.length;
    retval.allocateitem( nritemfields );
    System.arraycopy( ItemCustomFields, 0, retval.ItemCustomFields, 0, nritemfields );
    System.arraycopy( ItemCustomTags, 0, retval.ItemCustomTags, 0, nritemfields );

    // Namespaces
    int nrNameSpaces = NameSpaces.length;
    retval.allocatenamespace( nrNameSpaces );
    System.arraycopy( NameSpacesTitle, 0, retval.NameSpacesTitle, 0, nrNameSpaces );
    System.arraycopy( NameSpaces, 0, retval.NameSpaces, 0, nrNameSpaces );

    return retval;

  }

  public void allocate( int nrfields ) {
    ChannelCustomFields = new String[nrfields];
    ChannelCustomTags = new String[nrfields];
  }

  public void allocateitem( int nrfields ) {
    ItemCustomFields = new String[nrfields];
    ItemCustomTags = new String[nrfields];
  }

  public void allocatenamespace( int nrnamespaces ) {
    NameSpaces = new String[nrnamespaces];
    NameSpacesTitle = new String[nrnamespaces];
  }

  /**
   * @return Returns the version.
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param version
   *          The version to set.
   */
  public void setVersion( String version ) {
    this.version = version;
  }

  /**
   * @return Returns the encoding.
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @param encoding
   *          The encoding to set.
   */
  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  /**
   * @return Returns the filenamefield.
   */
  public String getFileNameField() {
    return filenamefield;
  }

  /**
   * @param encoding
   *          The encoding to set.
   */
  public void setFileNameField( String filenamefield ) {
    this.filenamefield = filenamefield;
  }

  /**
   * @return Returns the extension.
   */
  public String getExtension() {
    return extension;
  }

  /**
   * @param extension
   *          The extension to set.
   */
  public void setExtension( String extension ) {
    this.extension = extension;
  }

  /**
   * @return Returns the fileName.
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @return Returns the stepNrInFilename.
   */
  public boolean isStepNrInFilename() {
    return stepNrInFilename;
  }

  /**
   * @param stepNrInFilename
   *          The stepNrInFilename to set.
   */
  public void setStepNrInFilename( boolean stepNrInFilename ) {
    this.stepNrInFilename = stepNrInFilename;
  }

  /**
   * @return Returns the timeInFilename.
   */
  public boolean isTimeInFilename() {
    return timeInFilename;
  }

  /**
   * @return Returns the dateInFilename.
   */
  public boolean isDateInFilename() {
    return dateInFilename;
  }

  /**
   * @param dateInFilename
   *          The dateInFilename to set.
   */
  public void setDateInFilename( boolean dateInFilename ) {
    this.dateInFilename = dateInFilename;
  }

  /**
   * @param timeInFilename
   *          The timeInFilename to set.
   */
  public void setTimeInFilename( boolean timeInFilename ) {
    this.timeInFilename = timeInFilename;
  }

  /**
   * @param fileName
   *          The fileName to set.
   */
  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  /**
   * @return Returns the Add to result filesname flag.
   */
  public boolean AddToResult() {
    return AddToResult;
  }

  /**
   * @param AddToResult
   *          The Add file to result to set.
   */
  public void setAddToResult( boolean AddToResult ) {
    this.AddToResult = AddToResult;
  }

  /**
   * @param customrss
   *          The custom RSS flag to set.
   */
  public void setCustomRss( boolean customrss ) {
    this.customrss = customrss;
  }

  /**
   * @return Returns the custom RSS flag.
   */
  public boolean isCustomRss() {
    return customrss;
  }

  /**
   * @param displayitem
   *          The display itema ta flag.
   */
  public void setDisplayItem( boolean displayitem ) {
    this.displayitem = displayitem;
  }

  /**
   * @return Returns the displayitem.
   */
  public boolean isDisplayItem() {
    return displayitem;
  }

  /**
   * @return Returns the addimage flag.
   */
  public boolean AddImage() {
    return addimage;
  }

  /**
   * @param addimage
   *          The addimage to set.
   */
  public void setAddImage( boolean addimage ) {
    this.addimage = addimage;
  }

  /**
   * @return Returns the addgeorss flag.
   */
  public boolean AddGeoRSS() {
    return addgeorss;
  }

  /**
   * @param addgeorss
   *          The addgeorss to set.
   */
  public void setAddGeoRSS( boolean addgeorss ) {
    this.addgeorss = addgeorss;
  }

  /**
   * @return Returns the addgeorss flag.
   */
  public boolean useGeoRSSGML() {
    return usegeorssgml;
  }

  /**
   * @param usegeorssgml
   *          The usegeorssgml to set.
   */
  public void setUseGeoRSSGML( boolean usegeorssgml ) {
    this.usegeorssgml = usegeorssgml;
  }

  /**
   * @return Returns the isfilenameinfield flag.
   */
  public boolean isFilenameInField() {
    return isfilenameinfield;
  }

  /**
   * @param isfilenameinfield
   *          The isfilenameinfield to set.
   */
  public void setFilenameInField( boolean isfilenameinfield ) {
    this.isfilenameinfield = isfilenameinfield;
  }

  /**
   * @return Returns the ChannelCustomFields (names in the stream).
   */
  public String[] getChannelCustomFields() {
    return ChannelCustomFields;
  }

  /**
   * @param ChannelCustomFields
   *          The ChannelCustomFields to set.
   */
  public void setChannelCustomFields( String[] ChannelCustomFields ) {
    this.ChannelCustomFields = ChannelCustomFields;
  }

  /**
   * @return Returns the NameSpaces.
   */
  public String[] getNameSpaces() {
    return NameSpaces;
  }

  /**
   * @param NameSpaces
   *          The NameSpaces to set.
   */
  public void setNameSpaces( String[] NameSpaces ) {
    this.NameSpaces = NameSpaces;
  }

  /**
   * @return Returns the NameSpaces.
   */
  public String[] getNameSpacesTitle() {
    return NameSpacesTitle;
  }

  /**
   * @param NameSpacesTitle
   *          The NameSpacesTitle to set.
   */
  public void setNameSpacesTitle( String[] NameSpacesTitle ) {
    this.NameSpacesTitle = NameSpacesTitle;
  }

  /**
   * @return Returns the getChannelCustomTags (names in the stream).
   */
  public String[] getChannelCustomTags() {
    return ChannelCustomTags;
  }

  /**
   * @param getChannelCustomTags
   *          The getChannelCustomTags to set.
   */
  public void setChannelCustomTags( String[] ChannelCustomTags ) {
    this.ChannelCustomTags = ChannelCustomTags;
  }

  /**
   * @return Returns the getChannelCustomTags (names in the stream).
   */
  public String[] getItemCustomTags() {
    return ItemCustomTags;
  }

  /**
   * @param getChannelCustomTags
   *          The getChannelCustomTags to set.
   */
  public void setItemCustomTags( String[] ItemCustomTags ) {
    this.ItemCustomTags = ItemCustomTags;
  }

  /**
   * @return Returns the ItemCustomFields (names in the stream).
   */
  public String[] getItemCustomFields() {
    return ItemCustomFields;
  }

  /**
   * @param value
   *          The ItemCustomFields to set.
   */
  public void setItemCustomFields( String[] value ) {
    this.ItemCustomFields = value;
  }

  /**
   * @return Returns the create parent folder flag.
   */
  public boolean isCreateParentFolder() {
    return createparentfolder;
  }

  /**
   * @param createparentfolder
   *          The create parent folder flag to set.
   */
  public void setCreateParentFolder( boolean createparentfolder ) {
    this.createparentfolder = createparentfolder;
  }

  public boolean isPartNrInFilename() {
    return partNrInFilename;
  }

  public void setPartNrInFilename( boolean value ) {
    partNrInFilename = value;
  }

  public String[] getFiles( VariableSpace space ) throws KettleStepException {
    int copies = 1;
    int parts = 1;

    if ( stepNrInFilename ) {
      copies = 3;
    }

    if ( partNrInFilename ) {
      parts = 3;
    }

    int nr = copies * parts;
    if ( nr > 1 ) {
      nr++;
    }

    String[] retval = new String[nr];

    int i = 0;
    for ( int copy = 0; copy < copies; copy++ ) {
      for ( int part = 0; part < parts; part++ ) {
        retval[i] = buildFilename( space, copy );
        i++;
      }
    }
    if ( i < nr ) {
      retval[i] = "...";
    }

    return retval;
  }

  private String getFilename( VariableSpace space ) throws KettleStepException {
    FileObject file = null;
    try {
      file = KettleVFS.getFileObject( space.environmentSubstitute( getFileName() ) );
      return KettleVFS.getFilename( file );
    } catch ( Exception e ) {
      throw new KettleStepException( BaseMessages
        .getString( PKG, "RssOutput.Meta.ErrorGettingFile", getFileName() ), e );
    } finally {
      if ( file != null ) {
        try {
          file.close();
        } catch ( Exception e ) { /* Ignore */
        }
      }
    }
  }

  public String buildFilename( VariableSpace space, int stepnr ) throws KettleStepException {

    SimpleDateFormat daf = new SimpleDateFormat();

    // Replace possible environment variables...
    String retval = getFilename( space );

    Date now = new Date();

    if ( dateInFilename ) {
      daf.applyPattern( "yyyMMdd" );
      String d = daf.format( now );
      retval += "_" + d;
    }
    if ( timeInFilename ) {
      daf.applyPattern( "HHmmss" );
      String t = daf.format( now );
      retval += "_" + t;
    }
    if ( stepNrInFilename ) {
      retval += "_" + stepnr;
    }

    if ( extension != null && extension.length() != 0 ) {
      retval += "." + extension;
    }

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {

      displayitem = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "displayitem" ) );
      customrss = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "customrss" ) );
      channeltitle = XMLHandler.getTagValue( stepnode, "channel_title" );
      channeldescription = XMLHandler.getTagValue( stepnode, "channel_description" );
      channellink = XMLHandler.getTagValue( stepnode, "channel_link" );
      channelpubdate = XMLHandler.getTagValue( stepnode, "channel_pubdate" );
      channelcopyright = XMLHandler.getTagValue( stepnode, "channel_copyright" );

      channelimagetitle = XMLHandler.getTagValue( stepnode, "channel_image_title" );
      channelimagelink = XMLHandler.getTagValue( stepnode, "channel_image_link" );
      channelimageurl = XMLHandler.getTagValue( stepnode, "channel_image_url" );
      channelimagedescription = XMLHandler.getTagValue( stepnode, "channel_image_description" );
      channellanguage = XMLHandler.getTagValue( stepnode, "channel_language" );
      channelauthor = XMLHandler.getTagValue( stepnode, "channel_author" );

      version = XMLHandler.getTagValue( stepnode, "version" );
      encoding = XMLHandler.getTagValue( stepnode, "encoding" );

      addimage = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "addimage" ) );

      // Items ...
      itemtitle = XMLHandler.getTagValue( stepnode, "item_title" );
      itemdescription = XMLHandler.getTagValue( stepnode, "item_description" );
      itemlink = XMLHandler.getTagValue( stepnode, "item_link" );
      itempubdate = XMLHandler.getTagValue( stepnode, "item_pubdate" );
      itemauthor = XMLHandler.getTagValue( stepnode, "item_author" );

      addgeorss = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "addgeorss" ) );
      usegeorssgml = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "usegeorssgml" ) );
      geopointlat = XMLHandler.getTagValue( stepnode, "geopointlat" );
      geopointlong = XMLHandler.getTagValue( stepnode, "geopointlong" );

      filenamefield = XMLHandler.getTagValue( stepnode, "file", "filename_field" );
      fileName = XMLHandler.getTagValue( stepnode, "file", "name" );

      isfilenameinfield =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "is_filename_in_field" ) );
      createparentfolder =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "create_parent_folder" ) );
      extension = XMLHandler.getTagValue( stepnode, "file", "extention" );
      stepNrInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "split" ) );
      partNrInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "haspartno" ) );
      dateInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "add_date" ) );
      timeInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "add_time" ) );
      AddToResult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "AddToResult" ) );

      Node keys = XMLHandler.getSubNode( stepnode, "fields" );
      // Custom Channel fields
      int nrchannelfields = XMLHandler.countNodes( keys, "channel_custom_fields" );
      allocate( nrchannelfields );

      for ( int i = 0; i < nrchannelfields; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( keys, "channel_custom_fields", i );
        ChannelCustomTags[i] = XMLHandler.getTagValue( knode, "tag" );
        ChannelCustomFields[i] = XMLHandler.getTagValue( knode, "field" );
      }
      // Custom Item fields
      int nritemfields = XMLHandler.countNodes( keys, "item_custom_fields" );
      allocateitem( nritemfields );

      for ( int i = 0; i < nritemfields; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( keys, "item_custom_fields", i );
        ItemCustomTags[i] = XMLHandler.getTagValue( knode, "tag" );
        ItemCustomFields[i] = XMLHandler.getTagValue( knode, "field" );
      }
      // NameSpaces
      Node keysNameSpaces = XMLHandler.getSubNode( stepnode, "namespaces" );
      int nrnamespaces = XMLHandler.countNodes( keysNameSpaces, "namespace" );
      allocatenamespace( nrnamespaces );
      for ( int i = 0; i < nrnamespaces; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( keysNameSpaces, "namespace", i );
        NameSpacesTitle[i] = XMLHandler.getTagValue( knode, "namespace_tag" );
        NameSpaces[i] = XMLHandler.getTagValue( knode, "namespace_value" );
      }

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void setDefault() {
    displayitem = true;
    customrss = false;
    channeltitle = null;
    channeldescription = null;
    channellink = null;
    channelpubdate = null;
    channelcopyright = null;
    channelimagetitle = null;
    channelimagelink = null;
    channelimageurl = null;
    channelimagedescription = null;
    channellanguage = null;
    channelauthor = null;
    createparentfolder = false;
    isfilenameinfield = false;
    version = "rss_2.0";
    encoding = "iso-8859-1";
    filenamefield = null;
    isfilenameinfield = false;

    // Items ...
    itemtitle = null;
    itemdescription = null;
    itemlink = null;
    itempubdate = null;
    itemauthor = null;
    geopointlat = null;
    geopointlong = null;
    int nrchannelfields = 0;
    allocate( nrchannelfields );
    // channel custom fields
    for ( int i = 0; i < nrchannelfields; i++ ) {
      ChannelCustomFields[i] = "field" + i;
      ChannelCustomTags[i] = "tag" + i;
    }

    int nritemfields = 0;
    allocateitem( nritemfields );
    // Custom Item Fields
    for ( int i = 0; i < nritemfields; i++ ) {
      ItemCustomFields[i] = "field" + i;
      ItemCustomTags[i] = "tag" + i;
    }
    // Namespaces
    int nrnamespaces = 0;
    allocatenamespace( nrnamespaces );
    // Namespaces
    for ( int i = 0; i < nrnamespaces; i++ ) {
      NameSpacesTitle[i] = "namespace_title" + i;
      NameSpaces[i] = "namespace" + i;
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " + XMLHandler.addTagValue( "displayitem", displayitem ) );
    retval.append( "    " + XMLHandler.addTagValue( "customrss", customrss ) );
    retval.append( "    " + XMLHandler.addTagValue( "channel_title", channeltitle ) );
    retval.append( "    " + XMLHandler.addTagValue( "channel_description", channeldescription ) );
    retval.append( "    " + XMLHandler.addTagValue( "channel_link", channellink ) );
    retval.append( "    " + XMLHandler.addTagValue( "channel_pubdate", channelpubdate ) );
    retval.append( "    " + XMLHandler.addTagValue( "channel_copyright", channelcopyright ) );

    retval.append( "    " + XMLHandler.addTagValue( "channel_image_title", channelimagetitle ) );
    retval.append( "    " + XMLHandler.addTagValue( "channel_image_link", channelimagelink ) );
    retval.append( "    " + XMLHandler.addTagValue( "channel_image_url", channelimageurl ) );
    retval.append( "    " + XMLHandler.addTagValue( "channel_image_description", channelimagedescription ) );
    retval.append( "    " + XMLHandler.addTagValue( "channel_language", channellanguage ) );
    retval.append( "    " + XMLHandler.addTagValue( "channel_author", channelauthor ) );

    retval.append( "    " + XMLHandler.addTagValue( "version", version ) );
    retval.append( "    " + XMLHandler.addTagValue( "encoding", encoding ) );

    retval.append( "    " + XMLHandler.addTagValue( "addimage", addimage ) );

    // Items ...

    retval.append( "    " + XMLHandler.addTagValue( "item_title", itemtitle ) );
    retval.append( "    " + XMLHandler.addTagValue( "item_description", itemdescription ) );
    retval.append( "    " + XMLHandler.addTagValue( "item_link", itemlink ) );
    retval.append( "    " + XMLHandler.addTagValue( "item_pubdate", itempubdate ) );
    retval.append( "    " + XMLHandler.addTagValue( "item_author", itemauthor ) );
    retval.append( "    " + XMLHandler.addTagValue( "addgeorss", addgeorss ) );
    retval.append( "    " + XMLHandler.addTagValue( "usegeorssgml", usegeorssgml ) );
    retval.append( "    " + XMLHandler.addTagValue( "geopointlat", geopointlat ) );
    retval.append( "    " + XMLHandler.addTagValue( "geopointlong", geopointlong ) );

    retval.append( "    <file>" + Const.CR );
    retval.append( "      " + XMLHandler.addTagValue( "filename_field", filenamefield ) );
    retval.append( "      " + XMLHandler.addTagValue( "name", fileName ) );
    retval.append( "      " + XMLHandler.addTagValue( "extention", extension ) );
    retval.append( "      " + XMLHandler.addTagValue( "split", stepNrInFilename ) );
    retval.append( "      " + XMLHandler.addTagValue( "haspartno", partNrInFilename ) );
    retval.append( "      " + XMLHandler.addTagValue( "add_date", dateInFilename ) );
    retval.append( "      " + XMLHandler.addTagValue( "add_time", timeInFilename ) );
    retval.append( "      " + XMLHandler.addTagValue( "is_filename_in_field", isfilenameinfield ) );
    retval.append( "      " + XMLHandler.addTagValue( "create_parent_folder", createparentfolder ) );
    retval.append( "    " + XMLHandler.addTagValue( "addtoresult", AddToResult ) );
    retval.append( "      </file>" + Const.CR );

    retval.append( "      <fields>" ).append( Const.CR );
    for ( int i = 0; i < ChannelCustomFields.length; i++ ) {
      retval.append( "        <channel_custom_fields>" ).append( Const.CR );
      retval.append( "          " ).append( XMLHandler.addTagValue( "tag", ChannelCustomTags[i] ) );
      retval.append( "          " ).append( XMLHandler.addTagValue( "field", ChannelCustomFields[i] ) );
      retval.append( "        </channel_custom_fields>" ).append( Const.CR );
    }
    for ( int i = 0; i < ItemCustomFields.length; i++ ) {
      retval.append( "        <Item_custom_fields>" ).append( Const.CR );
      retval.append( "          " ).append( XMLHandler.addTagValue( "tag", ItemCustomTags[i] ) );
      retval.append( "          " ).append( XMLHandler.addTagValue( "field", ItemCustomFields[i] ) );
      retval.append( "        </Item_custom_fields>" ).append( Const.CR );
    }
    retval.append( "      </fields>" ).append( Const.CR );

    retval.append( "      <namespaces>" ).append( Const.CR );
    for ( int i = 0; i < NameSpaces.length; i++ ) {
      retval.append( "        <namespace>" ).append( Const.CR );
      retval.append( "          " ).append( XMLHandler.addTagValue( "namespace_tag", NameSpacesTitle[i] ) );
      retval.append( "          " ).append( XMLHandler.addTagValue( "namespace_value", NameSpaces[i] ) );
      retval.append( "        </namespace>" ).append( Const.CR );
    }
    retval.append( "      </namespaces>" ).append( Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {

      displayitem = rep.getStepAttributeBoolean( id_step, "displayitem" );
      customrss = rep.getStepAttributeBoolean( id_step, "customrss" );
      channeltitle = rep.getStepAttributeString( id_step, "channel_title" );
      channeldescription = rep.getStepAttributeString( id_step, "channel_description" );
      channellink = rep.getStepAttributeString( id_step, "channel_link" );
      channelpubdate = rep.getStepAttributeString( id_step, "channel_pubdate" );
      channelcopyright = rep.getStepAttributeString( id_step, "channel_copyright" );

      channelimagetitle = rep.getStepAttributeString( id_step, "channel_image_title" );
      channelimagelink = rep.getStepAttributeString( id_step, "channel_image_link" );
      channelimageurl = rep.getStepAttributeString( id_step, "channel_image_url" );
      channelimagedescription = rep.getStepAttributeString( id_step, "channel_image_description" );
      channellanguage = rep.getStepAttributeString( id_step, "channel_language" );
      channelauthor = rep.getStepAttributeString( id_step, "channel_author" );

      version = rep.getStepAttributeString( id_step, "version" );
      encoding = rep.getStepAttributeString( id_step, "encoding" );

      addimage = rep.getStepAttributeBoolean( id_step, "addimage" );

      // items ...

      itemtitle = rep.getStepAttributeString( id_step, "item_title" );
      itemdescription = rep.getStepAttributeString( id_step, "item_description" );
      itemlink = rep.getStepAttributeString( id_step, "item_link" );
      itempubdate = rep.getStepAttributeString( id_step, "item_pubdate" );
      itemauthor = rep.getStepAttributeString( id_step, "item_author" );

      addgeorss = rep.getStepAttributeBoolean( id_step, "addgeorss" );
      usegeorssgml = rep.getStepAttributeBoolean( id_step, "usegeorssgml" );
      geopointlat = rep.getStepAttributeString( id_step, "geopointlat" );
      geopointlong = rep.getStepAttributeString( id_step, "geopointlong" );

      filenamefield = rep.getStepAttributeString( id_step, "filename_field" );
      fileName = rep.getStepAttributeString( id_step, "file_name" );
      extension = rep.getStepAttributeString( id_step, "file_extention" );
      stepNrInFilename = rep.getStepAttributeBoolean( id_step, "file_add_stepnr" );
      partNrInFilename = rep.getStepAttributeBoolean( id_step, "file_add_partnr" );
      dateInFilename = rep.getStepAttributeBoolean( id_step, "file_add_date" );
      timeInFilename = rep.getStepAttributeBoolean( id_step, "file_add_time" );

      isfilenameinfield = rep.getStepAttributeBoolean( id_step, "is_filename_in_field" );
      createparentfolder = rep.getStepAttributeBoolean( id_step, "create_parent_folder" );
      AddToResult = rep.getStepAttributeBoolean( id_step, "addtoresult" );
      // Channel Custom
      int nrchannel = rep.countNrStepAttributes( id_step, "channel_custom_field" );
      allocate( nrchannel );
      for ( int i = 0; i < nrchannel; i++ ) {
        ChannelCustomTags[i] = rep.getStepAttributeString( id_step, i, "channel_custom_tag" );
        ChannelCustomFields[i] = rep.getStepAttributeString( id_step, i, "channel_custom_field" );
      }
      // Item Custom
      int nritem = rep.countNrStepAttributes( id_step, "item_custom_field" );
      allocateitem( nritem );
      for ( int i = 0; i < nritem; i++ ) {
        ItemCustomTags[i] = rep.getStepAttributeString( id_step, i, "item_custom_tag" );
        ItemCustomFields[i] = rep.getStepAttributeString( id_step, i, "item_custom_field" );
      }

      // Namespaces
      int nrnamespaces = rep.countNrStepAttributes( id_step, "namespace_tag" );
      allocatenamespace( nrnamespaces );

      for ( int i = 0; i < nrnamespaces; i++ ) {
        NameSpacesTitle[i] = rep.getStepAttributeString( id_step, i, "namespace_tag" );
        NameSpaces[i] = rep.getStepAttributeString( id_step, i, "namespace_value" );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {

      rep.saveStepAttribute( id_transformation, id_step, "displayitem", displayitem );
      rep.saveStepAttribute( id_transformation, id_step, "customrss", customrss );
      rep.saveStepAttribute( id_transformation, id_step, "channel_title", channeltitle );
      rep.saveStepAttribute( id_transformation, id_step, "channel_description", channeldescription );
      rep.saveStepAttribute( id_transformation, id_step, "channel_link", channellink );
      rep.saveStepAttribute( id_transformation, id_step, "channel_pubdate", channelpubdate );
      rep.saveStepAttribute( id_transformation, id_step, "channel_copyright", channelcopyright );

      rep.saveStepAttribute( id_transformation, id_step, "channel_image_title", channelimagetitle );
      rep.saveStepAttribute( id_transformation, id_step, "channel_image_link", channelimagelink );
      rep.saveStepAttribute( id_transformation, id_step, "channel_image_url", channelimageurl );
      rep.saveStepAttribute( id_transformation, id_step, "channel_image_description", channelimagedescription );

      rep.saveStepAttribute( id_transformation, id_step, "channel_author", channelauthor );
      rep.saveStepAttribute( id_transformation, id_step, "channel_language", channellanguage );
      rep.saveStepAttribute( id_transformation, id_step, "version", version );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", encoding );

      rep.saveStepAttribute( id_transformation, id_step, "addimage", addimage );
      // items ...

      rep.saveStepAttribute( id_transformation, id_step, "item_title", itemtitle );
      rep.saveStepAttribute( id_transformation, id_step, "item_description", itemdescription );
      rep.saveStepAttribute( id_transformation, id_step, "item_link", itemlink );
      rep.saveStepAttribute( id_transformation, id_step, "item_pubdate", itempubdate );
      rep.saveStepAttribute( id_transformation, id_step, "item_author", itemauthor );
      rep.saveStepAttribute( id_transformation, id_step, "addgeorss", addgeorss );
      rep.saveStepAttribute( id_transformation, id_step, "usegeorssgml", usegeorssgml );
      rep.saveStepAttribute( id_transformation, id_step, "geopointlat", geopointlat );
      rep.saveStepAttribute( id_transformation, id_step, "geopointlong", geopointlong );

      rep.saveStepAttribute( id_transformation, id_step, "filename_field", filenamefield );
      rep.saveStepAttribute( id_transformation, id_step, "file_name", fileName );
      rep.saveStepAttribute( id_transformation, id_step, "file_extention", extension );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_stepnr", stepNrInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_partnr", partNrInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_date", dateInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_time", timeInFilename );

      rep.saveStepAttribute( id_transformation, id_step, "is_filename_in_field", isfilenameinfield );
      rep.saveStepAttribute( id_transformation, id_step, "create_parent_folder", createparentfolder );
      rep.saveStepAttribute( id_transformation, id_step, "addtoresult", AddToResult );

      for ( int i = 0; i < ChannelCustomFields.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "channel_custom_field", ChannelCustomFields[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "channel_custom_tag", ChannelCustomTags[i] );
      }
      for ( int i = 0; i < ItemCustomFields.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "item_custom_field", ItemCustomFields[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "item_custom_tag", ItemCustomTags[i] );
      }

      for ( int i = 0; i < NameSpaces.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "namespace_tag", NameSpacesTitle[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "namespace_value", NameSpaces[i] );
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    CheckResult cr;

    // String error_message = "";
    // boolean error_found = false;
    // OK, we have the table fields.
    // Now see what we can find as previous step...
    if ( prev != null && prev.size() > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "RssOutputMeta.CheckResult.FieldsReceived", "" + prev.size() ), stepMeta );
      remarks.add( cr );

      // Starting from prev...
      /*
       * for (int i=0;i<prev.size();i++) { Value pv = prev.getValue(i); int idx = r.searchValueIndex(pv.getName()); if
       * (idx<0) { error_message+="\t\t"+pv.getName()+" ("+pv.getTypeDesc()+")"+Const.CR; error_found=true; } } if
       * (error_found) { error_message=BaseMessages.getString(PKG, "RssOutputMeta.CheckResult.FieldsNotFoundInOutput",
       * error_message);
       *
       * cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta); remarks.add(cr); } else { cr =
       * new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG,
       * "RssOutputMeta.CheckResult.AllFieldsFoundInOutput"), stepMeta); remarks.add(cr); }
       */

      // Starting from table fields in r...
      /*
       * for (int i=0;i<r.size();i++) { Value rv = r.getValue(i); int idx = prev.searchValueIndex(rv.getName()); if
       * (idx<0) { error_message+="\t\t"+rv.getName()+" ("+rv.getTypeDesc()+")"+Const.CR; error_found=true; } } if
       * (error_found) { error_message=BaseMessages.getString(PKG, "RssOutputMeta.CheckResult.FieldsNotFound",
       * error_message);
       *
       * cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, error_message, stepMeta); remarks.add(cr); } else { cr =
       * new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG,
       * "RssOutputMeta.CheckResult.AllFieldsFound"), stepMeta); remarks.add(cr); }
       */
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "RssOutputMeta.CheckResult.NoFields" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "RssOutputMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "RssOutputMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepDataInterface getStepData() {
    return new RssOutputData();
  }

  /**
   * @return the channeltitle
   */
  public String getChannelTitle() {
    return channeltitle;
  }

  /**
   * @return the channeldescription
   */
  public String getChannelDescription() {
    return channeldescription;
  }

  /**
   * @return the channellink
   */
  public String getChannelLink() {
    return channellink;
  }

  /**
   * @return the channelpubdate
   */
  public String getChannelPubDate() {
    return channelpubdate;
  }

  /**
   * @return the channelimagelink
   */
  public String getChannelImageLink() {
    return channelimagelink;
  }

  /**
   * @return the channelimageurl
   */
  public String getChannelImageUrl() {
    return channelimageurl;
  }

  /**
   * @return the channelimagedescription
   */
  public String getChannelImageDescription() {
    return channelimagedescription;
  }

  /**
   * @return the channelimagetitle
   */
  public String getChannelImageTitle() {
    return channelimagetitle;
  }

  /**
   * @return the channellanguage
   */
  public String getChannelLanguage() {
    return channellanguage;
  }

  /**
   * @return the channelauthor
   */
  public String getChannelAuthor() {
    return channelauthor;
  }

  /**
   * @param channelauthor
   *          the channelauthor to set
   */
  public void setChannelAuthor( String channelauthor ) {
    this.channelauthor = channelauthor;
  }

  /**
   * @param channeltitle
   *          the channeltitle to set
   */
  public void setChannelTitle( String channeltitle ) {
    this.channeltitle = channeltitle;
  }

  /**
   * @param channellink
   *          the channellink to set
   */
  public void setChannelLink( String channellink ) {
    this.channellink = channellink;
  }

  /**
   * @param channelpubdate
   *          the channelpubdate to set
   */
  public void setChannelPubDate( String channelpubdate ) {
    this.channelpubdate = channelpubdate;
  }

  /**
   * @param channelimagetitle
   *          the channelimagetitle to set
   */
  public void setChannelImageTitle( String channelimagetitle ) {
    this.channelimagetitle = channelimagetitle;
  }

  /**
   * @param channelimagelink
   *          the channelimagelink to set
   */
  public void setChannelImageLink( String channelimagelink ) {
    this.channelimagelink = channelimagelink;
  }

  /**
   * @param channelimageurl
   *          the channelimageurl to set
   */
  public void setChannelImageUrl( String channelimageurl ) {
    this.channelimageurl = channelimageurl;
  }

  /**
   * @param channelimagedescription
   *          the channelimagedescription to set
   */
  public void setChannelImageDescription( String channelimagedescription ) {
    this.channelimagedescription = channelimagedescription;
  }

  /**
   * @param channellanguage
   *          the channellanguage to set
   */
  public void setChannelLanguage( String channellanguage ) {
    this.channellanguage = channellanguage;
  }

  /**
   * @param channeldescription
   *          the channeldescription to set
   */
  public void setChannelDescription( String channeldescription ) {
    this.channeldescription = channeldescription;
  }

  /**
   * @return the itemtitle
   */
  public String getItemTitle() {
    return itemtitle;
  }

  /**
   * @return the geopointlat
   */
  public String getGeoPointLat() {
    return geopointlat;
  }

  /**
   * @param geopointlat
   *          the geopointlat to set
   */
  public void setGeoPointLat( String geopointlat ) {
    this.geopointlat = geopointlat;
  }

  /**
   * @return the geopointlong
   */
  public String getGeoPointLong() {
    return geopointlong;
  }

  /**
   * @param geopointlong
   *          the geopointlong to set
   */
  public void setGeoPointLong( String geopointlong ) {
    this.geopointlong = geopointlong;
  }

  /**
   * @return the itemdescription
   */
  public String getItemDescription() {
    return itemdescription;
  }

  /**
   * @return the itemlink
   */
  public String getItemLink() {
    return itemlink;
  }

  /**
   * @return the itempubdate
   */
  public String getItemPubDate() {
    return itempubdate;
  }

  /**
   * @return the itemauthor
   */
  public String getItemAuthor() {
    return itemauthor;
  }

  /**
   * @param itemtitle
   *          the itemtitle to set
   */
  public void setItemTitle( String itemtitle ) {
    this.itemtitle = itemtitle;
  }

  /**
   * @param itemdescription
   *          the itemdescription to set
   */
  public void setItemDescription( String itemdescription ) {
    this.itemdescription = itemdescription;
  }

  /**
   * @param itemlink
   *          the itemlink to set
   */
  public void setItemLink( String itemlink ) {
    this.itemlink = itemlink;
  }

  /**
   * @param itempubdate
   *          the itempubdate to set
   */
  public void setItemPubDate( String itempubdate ) {
    this.itempubdate = itempubdate;
  }

  /**
   * @param itemauthor
   *          the itemauthor to set
   */
  public void setItemAuthor( String itemauthor ) {
    this.itemauthor = itemauthor;
  }

  /**
   * @return channelcopyrightt
   */
  public String getChannelCopyright() {
    return channelcopyright;
  }

  /**
   * @param channelcopyright
   *          the channelcopyright to set
   */
  public void setChannelCopyright( String channelcopyright ) {
    this.channelcopyright = channelcopyright;
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new RssOutput( stepMeta, stepDataInterface, cnr, tr, trans );
  }
}
