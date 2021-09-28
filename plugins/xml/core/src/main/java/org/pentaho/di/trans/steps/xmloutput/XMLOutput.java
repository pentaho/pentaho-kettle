/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.xmloutput;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.xmloutput.XMLField.ContentType;

/**
 * Converts input rows to one or more XML files.
 *
 * @author Matt
 * @since 14-jan-2006
 */
public class XMLOutput extends BaseStep implements StepInterface {
  private static final String EOL = "\n"; // force EOL char because woodstox library encodes CRLF incorrectly

  private static final XMLOutputFactory XML_OUT_FACTORY = XMLOutputFactory.newInstance();

  private XMLOutputMeta meta;

  private XMLOutputData data;

  private OutputStream outputStream;

  public XMLOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (XMLOutputMeta) smi;
    data = (XMLOutputData) sdi;

    Object[] r;
    boolean result = true;

    r = getRow(); // This also waits for a row to be finished.

    if ( first && meta.isDoNotOpenNewFileInit() ) {
      // no more input to be expected...
      // In this case, no file was opened.
      if ( r == null ) {
        setOutputDone();
        return false;
      }

      if ( openNewFile() ) {
        data.OpenedNewFile = true;
      } else {
        logError( "Couldn't open file " + meta.getFileName() );
        setErrors( 1L );
        return false;
      }
    }

    if ( ( r != null && getLinesOutput() > 0 && meta.getSplitEvery() > 0 && ( getLinesOutput() % meta.getSplitEvery() ) == 0 ) ) {
      // Done with this part or with everything.
      closeFile();

      // Not finished: open another file...
      if ( r != null ) {
        if ( !openNewFile() ) {
          logError( "Unable to open new file (split #" + data.splitnr + "..." );
          setErrors( 1 );
          return false;
        }
      }
    }

    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    writeRowToFile( getInputRowMeta(), r );

    data.outputRowMeta = getInputRowMeta().clone();
    meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
    putRow( data.outputRowMeta, r ); // in case we want it to go further...

    if ( checkFeedback( getLinesOutput() ) ) {
      logBasic( "linenr " + getLinesOutput() );
    }

    return result;
  }

  private void writeRowToFile( RowMetaInterface rowMeta, Object[] r ) throws KettleException {
    try {
      if ( first ) {
        data.formatRowMeta = rowMeta.clone();

        first = false;

        data.fieldnrs = new int[meta.getOutputFields().length];
        for ( int i = 0; i < meta.getOutputFields().length; i++ ) {
          data.fieldnrs[i] = data.formatRowMeta.indexOfValue( meta.getOutputFields()[i].getFieldName() );
          if ( data.fieldnrs[i] < 0 ) {
            throw new KettleException( "Field [" + meta.getOutputFields()[i].getFieldName()
                + "] couldn't be found in the input stream!" );
          }

          // Apply the formatting settings to the valueMeta object...
          //
          ValueMetaInterface valueMeta = data.formatRowMeta.getValueMeta( data.fieldnrs[i] );
          XMLField field = meta.getOutputFields()[i];
          valueMeta.setConversionMask( field.getFormat() );
          valueMeta.setLength( field.getLength(), field.getPrecision() );
          valueMeta.setDecimalSymbol( field.getDecimalSymbol() );
          valueMeta.setGroupingSymbol( field.getGroupingSymbol() );
          valueMeta.setCurrencySymbol( field.getCurrencySymbol() );
        }
      }

      if ( meta.getOutputFields() == null || meta.getOutputFields().length == 0 ) {
        /*
         * Write all values in stream to text file.
         */

        // OK, write a new row to the XML file:
        data.writer.writeStartElement( meta.getRepeatElement() );

        for ( int i = 0; i < data.formatRowMeta.size(); i++ ) {
          // Put a space between the XML elements of the row
          //
          if ( i > 0 ) {
            data.writer.writeCharacters( " " );
          }

          ValueMetaInterface valueMeta = data.formatRowMeta.getValueMeta( i );
          Object valueData = r[i];

          writeField( valueMeta, valueData, valueMeta.getName() );
        }
      } else {
        /*
         * Only write the fields specified!
         */
        // Write a new row to the XML file:
        data.writer.writeStartElement( meta.getRepeatElement() );

        // First do the attributes and write them...
        writeRowAttributes( r );

        // Now write the elements
        //
        for ( int i = 0; i < meta.getOutputFields().length; i++ ) {
          XMLField outputField = meta.getOutputFields()[i];
          if ( outputField.getContentType() == ContentType.Element ) {
            if ( i > 0 ) {
              data.writer.writeCharacters( " " ); // a space between
              // elements
            }

            ValueMetaInterface valueMeta = data.formatRowMeta.getValueMeta( data.fieldnrs[i] );
            Object valueData = r[data.fieldnrs[i]];

            String elementName = outputField.getElementName();
            if ( Utils.isEmpty( elementName ) ) {
              elementName = outputField.getFieldName();
            }

            if ( !( valueMeta.isNull( valueData ) && meta.isOmitNullValues() ) ) {
              writeField( valueMeta, valueData, elementName );
            }
          }
        }
      }

      data.writer.writeEndElement();
      data.writer.writeCharacters( EOL );
    } catch ( Exception e ) {
      throw new KettleException( "Error writing XML row :" + e.toString() + Const.CR + "Row: "
          + getInputRowMeta().getString( r ), e );
    }

    incrementLinesOutput();
  }

  void writeRowAttributes( Object[] r ) throws KettleValueException, XMLStreamException {
    for ( int i = 0; i < meta.getOutputFields().length; i++ ) {
      XMLField xmlField = meta.getOutputFields()[i];
      if ( xmlField.getContentType() == ContentType.Attribute ) {
        ValueMetaInterface valueMeta = data.formatRowMeta.getValueMeta( data.fieldnrs[i] );
        Object valueData = r[data.fieldnrs[i]];

        String elementName = xmlField.getElementName();
        if ( Utils.isEmpty( elementName ) ) {
          elementName = xmlField.getFieldName();
        }

        if ( valueData != null ) {

          data.writer.writeAttribute( elementName, valueMeta.getString( valueData ) );
        } else if ( isNullValueAllowed( valueMeta.getType() ) ) {

          data.writer.writeAttribute( elementName, "null" );
        }
      }
    }
  }

  private boolean isNullValueAllowed( int valueMetaType ) {

    //Check if retro compatibility is set or not, to guaranty compatibility with older versions.
    //In 6.1 null values were written with string "null". Since then the attribute is not written.

    String val = getVariable( Const.KETTLE_COMPATIBILITY_XML_OUTPUT_NULL_VALUES, "N" );

    return ValueMetaBase.convertStringToBoolean( Const.NVL( val, "N" ) ) && valueMetaType == ValueMetaInterface.TYPE_STRING;
  }

  private void writeField( ValueMetaInterface valueMeta, Object valueData, String element ) throws KettleStepException {
    try {
      String value = valueMeta.getString( valueData );
      if ( value != null ) {
        data.writer.writeStartElement( element );
        data.writer.writeCharacters( value );
        data.writer.writeEndElement();
      } else {
        data.writer.writeEmptyElement( element );
      }
    } catch ( Exception e ) {
      throw new KettleStepException( "Error writing line :", e );
    }
  }

  public String buildFilename( boolean ziparchive ) {
    return meta.buildFilename( this, getCopy(), data.splitnr, ziparchive );
  }

  public boolean openNewFile() {
    boolean retval = false;
    data.writer = null;

    try {
      if ( meta.isServletOutput() ) {
        data.writer = XML_OUT_FACTORY.createXMLStreamWriter( getTrans().getServletPrintWriter() );
        if ( meta.getEncoding() != null && meta.getEncoding().length() > 0 ) {
          data.writer.writeStartDocument( meta.getEncoding(), "1.0" );
        } else {
          data.writer.writeStartDocument( Const.XML_ENCODING, "1.0" );
        }
        data.writer.writeCharacters( EOL );
      } else {

        FileObject file = KettleVFS.getFileObject( buildFilename( true ), getTransMeta() );

        if ( meta.isAddToResultFiles() ) {
          // Add this to the result file names...
          ResultFile resultFile =
              new ResultFile( ResultFile.FILE_TYPE_GENERAL, file, getTransMeta().getName(), getStepname() );
          resultFile.setComment( "This file was created with a xml output step" );
          addResultFile( resultFile );
        }

        if ( meta.isZipped() ) {
          OutputStream fos = KettleVFS.getOutputStream( file, false );
          data.zip = new ZipOutputStream( fos );
          File entry = new File( buildFilename( false ) );
          ZipEntry zipentry = new ZipEntry( entry.getName() );
          zipentry.setComment( "Compressed by Kettle" );
          data.zip.putNextEntry( zipentry );
          outputStream = data.zip;
        } else {
          outputStream = KettleVFS.getOutputStream( file, false );
        }
        if ( meta.getEncoding() != null && meta.getEncoding().length() > 0 ) {
          logBasic( "Opening output stream in encoding: " + meta.getEncoding() );
          data.writer = XML_OUT_FACTORY.createXMLStreamWriter( outputStream, meta.getEncoding() );
          data.writer.writeStartDocument( meta.getEncoding(), "1.0" );
        } else {
          logBasic( "Opening output stream in default encoding : " + Const.XML_ENCODING );
          data.writer = XML_OUT_FACTORY.createXMLStreamWriter( outputStream );
          data.writer.writeStartDocument( Const.XML_ENCODING, "1.0" );
        }
        data.writer.writeCharacters( EOL );
      }

      // OK, write the header & the parent element:
      data.writer.writeStartElement( meta.getMainElement() );
      // Add the name space if defined
      String namespace = meta.getNameSpace();
      if ( !Utils.isEmpty( namespace ) ) {
        if ( isValidNamespace( namespace ) ) {
          data.writer.writeDefaultNamespace( namespace );
        } else {
          throw new KettleException( "Error: Namespace \"" + namespace + "\" is invalid." );
        }
      }
      data.writer.writeCharacters( EOL );

      retval = true;
    } catch ( KettleException e ) {
      logError( e.toString() );
    } catch ( Exception e ) {
      logError( "Error opening new file : " + e.toString() );
    }

    data.splitnr++;

    return retval;
  }

  /**
   *
   * @param ns - namespace string
   * @return - true if ns is a valid URI, false otherwise
   */
  @SuppressWarnings( { "squid:S1854", "squid:S1481" } )
  @VisibleForTesting
  static boolean isValidNamespace( String ns ) {
    try {
      URI uri = new URI( ns );
      return true;
    } catch ( Exception e ) {
      return false;
    }
  }

  void closeOutputStream( OutputStream stream ) {
    try {
      if ( stream != null ) {
        stream.close();
      }
    } catch ( Exception e ) {
      logError( "Error closing output stream : " + e.toString() );
    }
  }

  private boolean closeFile() {
    boolean retval = false;
    if ( data.OpenedNewFile ) {
      try {
        // Close the parent element
        data.writer.writeEndElement();
        data.writer.writeCharacters( EOL );

        // System.out.println("Closed xml file...");

        data.writer.writeEndDocument();
        data.writer.close();

        if ( meta.isZipped() ) {
          // System.out.println("close zip entry ");
          data.zip.closeEntry();
          // System.out.println("finish file...");
          data.zip.finish();
          data.zip.close();
        }

        closeOutputStream( outputStream );

        retval = true;
      } catch ( Exception e ) {
        // Ignore errors
      }
    }
    return retval;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (XMLOutputMeta) smi;
    data = (XMLOutputData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.splitnr = 0;
      if ( !meta.isDoNotOpenNewFileInit() ) {
        if ( openNewFile() ) {
          data.OpenedNewFile = true;
          return true;
        } else {
          logError( "Couldn't open file " + meta.getFileName() );
          setErrors( 1L );
          stopAll();
        }
      } else {
        return true;
      }
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (XMLOutputMeta) smi;
    data = (XMLOutputData) sdi;

    closeFile();

    super.dispose( smi, sdi );
  }

}
