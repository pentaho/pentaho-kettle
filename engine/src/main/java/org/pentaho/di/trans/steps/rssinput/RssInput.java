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

package org.pentaho.di.trans.steps.rssinput;

import it.sauronsoftware.feed4j.FeedParser;
import it.sauronsoftware.feed4j.FeedXMLParseException;
import it.sauronsoftware.feed4j.UnsupportedFeedException;
import it.sauronsoftware.feed4j.bean.FeedItem;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.dom4j.DocumentException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.xml.sax.SAXParseException;

/**
 * Read data from RSS and writes these to one or more output streams. <br/>
 * <br/>
 * When error handling is turned on:<br/>
 * <ul>
 * <li>The input row will be passed through if it is present</li>
 * <li>The "Nr of errors" field will always be 1 (we do not presently check for multiple errors)</li>
 * <li>The "Error descriptions" field contains a .toString of the caught exception (Usually contains useful info, such
 * as the HTTP return code)</li>
 * <li>The "Error fields" contains the URL that caused the failure</li>
 * <li>The "Error code" field contains one of the following Strings:
 * <ul>
 * <li>UnknownError - Unexpected; Check the "Error description" field</li>
 * <li>XMLError - Typically the file is not XML; Could be non-xml HTML</li>
 * <li>FileNotFound - Can be caused by a HTTP/404</li>
 * <li>UnknownHost - Domain name cannot be resolved; May be caused by network outage</li>
 * <li>TransferError - Can be caused by any Server error code (401, 403, 500, 502, etc...)</li>
 * <li>BadURL - Url cannot be understood; May lack protocol (e.g.- http://) or use an unrecognized protocol</li>
 * <li>BadRSSFormat - Typically the file is valid XML, but is not RSS</li>
 * </ul>
 * </li>
 * </ul>
 *
 * Notes:
 *
 * Turn on debug logging to see the full stack trace from a handled error. <br />
 *
 * @author Samatar
 * @since 13-10-2007
 */
public class RssInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = RssInput.class; // for i18n purposes, needed by Translator2!!

  private RssInputMeta meta;
  private RssInputData data;

  public RssInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private boolean readNextUrl() throws Exception {
    // Clear out previous feed
    data.feed = null;
    data.itemssize = 0;

    if ( meta.urlInField() ) {
      data.readrow = getRow(); // Grab another row ...
      if ( data.readrow == null ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "RssInput.Log.FinishedProcessing" ) );
        }
        return false;
      }
      if ( first ) {
        first = false;
        data.inputRowMeta = getInputRowMeta();
        data.outputRowMeta = data.inputRowMeta.clone();
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

        // Get total previous fields
        data.totalpreviousfields = data.inputRowMeta.size();

        // Create convert meta-data objects that will contain Date & Number formatters
        data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );

        // Check is URL field is provided
        if ( Utils.isEmpty( meta.getUrlFieldname() ) ) {
          logError( BaseMessages.getString( PKG, "RssInput.Log.UrlFieldNameMissing" ) );
          throw new KettleException( BaseMessages.getString( PKG, "RssInput.Log.UrlFieldNameMissing" ) );
        }

        // cache the position of the field
        if ( data.indexOfUrlField < 0 ) {
          data.indexOfUrlField = data.inputRowMeta.indexOfValue( meta.getUrlFieldname() );
          if ( data.indexOfUrlField < 0 ) {
            // The field is unreachable !
            logError( BaseMessages.getString( PKG, "RssInput.Log.ErrorFindingField" )
              + "[" + meta.getUrlFieldname() + "]" );
            throw new KettleException( BaseMessages.getString( PKG, "RssInput.Exception.ErrorFindingField", meta
              .getUrlFieldname() ) );
          }
        }

      }
      // get URL field value
      data.currenturl = data.inputRowMeta.getString( data.readrow, data.indexOfUrlField );

    } else {
      if ( data.last_url ) {
        return false;
      }
      if ( data.urlnr >= data.urlsize ) { // finished processing!

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "RssInput.Log.FinishedProcessing" ) );
        }
        return false;
      }
      // Is this the last url?
      data.last_url = ( data.urlnr == data.urlsize - 1 );
      data.currenturl = environmentSubstitute( meta.getUrl()[data.urlnr] );
    }

    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "RssInput.Log.ReadingUrl", data.currenturl ) );
    }

    try {

      URL rss = new URL( data.currenturl );
      data.feed = FeedParser.parse( rss );
      data.itemssize = data.feed.getItemCount();
    } finally {

      // Move url pointer ahead!
      data.urlnr++;
      data.itemsnr = 0;

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "RssInput.Log.UrlReadFailed", data.currenturl ) );
      }
    }

    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "RssInput.Log.UrlReaded", data.currenturl, data.itemssize ) );
    }

    return true;
  }

  private Object[] getOneRow() throws Exception {

    if ( meta.urlInField() ) {
      while ( ( data.itemsnr >= data.itemssize ) ) {
        if ( !readNextUrl() ) {
          return null;
        }
      }
    } else {
      while ( ( data.itemsnr >= data.itemssize ) || data.feed == null ) {
        if ( !readNextUrl() ) {
          return null;
        }
      }
    }

    // Create new row
    Object[] outputRowData = buildEmptyRow();

    if ( data.readrow != null ) {
      System.arraycopy( data.readrow, 0, outputRowData, 0, data.readrow.length );
    }

    // Get item
    FeedItem item = data.feed.getItem( data.itemsnr );

    if ( ( Utils.isEmpty( meta.getRealReadFrom() ) || ( !Utils.isEmpty( meta.getRealReadFrom() ) && item
      .getPubDate().compareTo( data.readfromdatevalide ) > 0 ) ) ) {

      // Execute for each Input field...
      for ( int j = 0; j < meta.getInputFields().length; j++ ) {
        RssInputField RSSInputField = meta.getInputFields()[j];

        String valueString = null;
        switch ( RSSInputField.getColumn() ) {
          case RssInputField.COLUMN_TITLE:
            valueString = item.getTitle();
            break;
          case RssInputField.COLUMN_LINK:
            valueString = item.getLink() == null ? "" : item.getLink().toString();
            break;
          case RssInputField.COLUMN_DESCRIPTION_AS_TEXT:
            valueString = item.getDescriptionAsText();
            break;
          case RssInputField.COLUMN_DESCRIPTION_AS_HTML:
            valueString = item.getDescriptionAsHTML();
            break;
          case RssInputField.COLUMN_COMMENTS:
            valueString = item.getComments() == null ? "" : item.getComments().toString();
            break;
          case RssInputField.COLUMN_GUID:
            valueString = item.getGUID();
            break;
          case RssInputField.COLUMN_PUB_DATE:
            valueString = item.getPubDate() == null ? "" : DateFormat.getInstance().format( item.getPubDate() );
            break;
          default:
            break;
        }

        // Do trimming
        switch ( RSSInputField.getTrimType() ) {
          case RssInputField.TYPE_TRIM_LEFT:
            valueString = Const.ltrim( valueString );
            break;
          case RssInputField.TYPE_TRIM_RIGHT:
            valueString = Const.rtrim( valueString );
            break;
          case RssInputField.TYPE_TRIM_BOTH:
            valueString = Const.trim( valueString );
            break;
          default:
            break;
        }

        // Do conversions
        //
        ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta( data.totalpreviousfields + j );
        ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta( data.totalpreviousfields + j );
        outputRowData[data.totalpreviousfields + j] = targetValueMeta.convertData( sourceValueMeta, valueString );

        // Do we need to repeat this field if it is null?
        if ( meta.getInputFields()[j].isRepeated() ) {
          if ( data.previousRow != null && Utils.isEmpty( valueString ) ) {
            outputRowData[data.totalpreviousfields + j] = data.previousRow[data.totalpreviousfields + j];
          }
        }

      } // end of loop over fields ...

      int rowIndex = data.nrInputFields;

      // See if we need to add the url to the row...
      if ( meta.includeUrl() ) {
        outputRowData[data.totalpreviousfields + rowIndex++] = data.currenturl;
      }
      // See if we need to add the row number to the row...
      if ( meta.includeRowNumber() ) {
        outputRowData[data.totalpreviousfields + rowIndex++] = new Long( data.rownr );
      }

      RowMetaInterface irow = getInputRowMeta();

      data.previousRow = irow == null ? outputRowData : irow.cloneRow( outputRowData ); // copy it to make
      // surely the next step doesn't change it in between...

      data.rownr++;

      putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);

      if ( meta.getRowLimit() > 0 && data.rownr > meta.getRowLimit() ) { // limit has been reached: stop now.
        return null;
      }
    }
    data.itemsnr++;
    return outputRowData;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] outputRowData = null;

    try {
      // Grab a row
      outputRowData = getOneRow();
      if ( outputRowData == null ) {
        setOutputDone(); // signal end to receiver(s)
        return false; // end of data or error.
      }
    } catch ( Exception e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        RowMeta errorMeta = new RowMeta();
        Object[] errorData = new Object[0];

        if ( this.data.readrow != null ) {
          errorMeta.addRowMeta( getInputRowMeta() );
          errorData = this.data.readrow;
        }

        String errorCode = "UnknownError";

        // Determine error code
        if ( e instanceof FeedXMLParseException ) {
          if ( e.getCause() instanceof DocumentException ) {
            if ( ( (DocumentException) e.getCause() ).getNestedException() instanceof SAXParseException ) {
              errorCode = "XMLError";
            } else if ( ( (DocumentException) e.getCause() ).getNestedException() instanceof FileNotFoundException ) {
              errorCode = "FileNotFound";
            } else if ( ( (DocumentException) e.getCause() ).getNestedException() instanceof IOException ) {
              if ( ( (DocumentException) e.getCause() ).getNestedException() instanceof UnknownHostException ) {
                errorCode = "UnknownHost";
              } else {
                errorCode = "TransferError";
              }
            }
          }
        } else if ( e instanceof MalformedURLException ) {
          errorCode = "BadURL";
        } else if ( e instanceof UnsupportedFeedException ) {
          errorCode = "BadRSSFormat";
        }

        putError( errorMeta, errorData, 1, e.toString(), this.data.currenturl, errorCode );
        logError( BaseMessages.getString( PKG, "RssInput.ErrorProcessing.Run", e.toString() ) );

        ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
        e.printStackTrace( new PrintStream( byteOS ) );
        logDebug( byteOS.toString() );
      } else {
        logError( BaseMessages.getString( PKG, "RssInput.Exception.Run", e.toString() ) );
        logError( Const.getStackTracker( e ) );
        setErrors( 1 );
        throw new KettleException( e );

      }
    }

    return true;

  }

  /**
   * Build an empty row based on the meta-data.
   *
   * @return
   */
  private Object[] buildEmptyRow() {
    Object[] rowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );

    return rowData;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (RssInputMeta) smi;
    data = (RssInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( meta.includeRowNumber() && Utils.isEmpty( meta.getRowNumberField() ) ) {
        logError( BaseMessages.getString( PKG, "RssInput.Error.RowNumberFieldMissing" ) );
        return false;
      }
      if ( meta.includeUrl() && Utils.isEmpty( meta.geturlField() ) ) {
        logError( BaseMessages.getString( PKG, "RssInput.Error.UrlFieldMissing" ) );
        return false;
      }

      if ( !Utils.isEmpty( meta.getReadFrom() ) ) {
        // Let's check validity of the read from date
        try {
          SimpleDateFormat fdrss = new SimpleDateFormat( "yyyy-MM-dd" );
          fdrss.setLenient( false );
          data.readfromdatevalide = fdrss.parse( meta.getRealReadFrom() );
        } catch ( Exception e ) {
          logError( "can not validate ''Read From date'' : " + environmentSubstitute( meta.getReadFrom() ) );
          return false;
        }
      }
      if ( meta.urlInField() ) {
        if ( meta.getUrl() == null && meta.getUrl().length == 0 ) {
          logError( BaseMessages.getString( PKG, "RssInput.Log.UrlMissing" ) );
          return false;
        }

      } else {
        data.urlsize = meta.getUrl().length;
        try {
          // Create the output row meta-data
          data.outputRowMeta = new RowMeta();

          meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

          // Create convert meta-data objects that will contain Date & Number formatters
          data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );
        } catch ( Exception e ) {
          logError( "Error initializing step: " + e.toString() );
          logError( Const.getStackTracker( e ) );
          return false;
        }

      }

      data.rownr = 1L;
      data.urlnr = 0;

      data.nrInputFields = meta.getInputFields().length;

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (RssInputMeta) smi;
    data = (RssInputData) sdi;
    if ( data.feed != null ) {
      data.feed = null;
    }

    super.dispose( smi, sdi );
  }

}
