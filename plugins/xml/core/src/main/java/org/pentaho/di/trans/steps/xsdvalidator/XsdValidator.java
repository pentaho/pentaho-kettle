/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.xsdvalidator;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.xml.sax.SAXException;

/**
 * Executes a xsd validator on the values in the input stream. New fields were calculated values can then be put on the
 * output stream.
 * 
 * @author Samatar
 * @since 14-08-2007
 * 
 */
public class XsdValidator extends BaseStep implements StepInterface {
  private static Class<?> PKG = XsdValidatorMeta.class; // for i18n purposes, needed by Translator2!!

  private XsdValidatorMeta meta;
  private XsdValidatorData data;

  static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

  static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

  public XsdValidator( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (XsdValidatorMeta) smi;
    data = (XsdValidatorData) sdi;

    Object[] row = getRow();

    if ( row == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Check if XML stream is given
      if ( meta.getXMLStream() != null ) {
        // Try to get XML Field index
        data.xmlindex = getInputRowMeta().indexOfValue( meta.getXMLStream() );
        // Let's check the Field
        if ( data.xmlindex < 0 ) {
          // The field is unreachable !
          logError( BaseMessages.getString( PKG, "XsdValidator.Log.ErrorFindingField" ) + "[" + meta.getXMLStream()
              + "]" );
          throw new KettleStepException( BaseMessages.getString( PKG, "XsdValidator.Exception.CouldnotFindField", meta
              .getXMLStream() ) );
        }

        // Let's check that Result Field is given
        if ( meta.getResultfieldname() == null ) {
          // Result field is missing !
          logError( BaseMessages.getString( PKG, "XsdValidator.Log.ErrorResultFieldMissing" ) );
          throw new KettleStepException( BaseMessages.getString( PKG, "XsdValidator.Exception.ErrorResultFieldMissing" ) );
        }

        // Is XSD file is provided?
        if ( meta.getXSDSource().equals( meta.SPECIFY_FILENAME ) ) {
          if ( meta.getXSDFilename() == null ) {
            logError( BaseMessages.getString( PKG, "XsdValidator.Log.ErrorXSDFileMissing" ) );
            throw new KettleStepException( BaseMessages.getString( PKG, "XsdValidator.Exception.ErrorXSDFileMissing" ) );
          } else {
            // Is XSD file exists ?
            FileObject xsdfile = null;
            try {
              xsdfile = KettleVFS.getFileObject( environmentSubstitute( meta.getXSDFilename() ), getTransMeta() );
              if ( !xsdfile.exists() ) {
                logError( BaseMessages.getString( PKG, "XsdValidator.Log.Error.XSDFileNotExists" ) );
                throw new KettleStepException( BaseMessages.getString( PKG, "XsdValidator.Exception.XSDFileNotExists" ) );
              }

            } catch ( Exception e ) {
              logError( BaseMessages.getString( PKG, "XsdValidator.Log.Error.GettingXSDFile" ) );
              throw new KettleStepException( BaseMessages.getString( PKG, "XsdValidator.Exception.GettingXSDFile" ) );
            } finally {
              try {
                if ( xsdfile != null ) {
                  xsdfile.close();
                }
              } catch ( IOException e ) {
                // Ignore errors
              }
            }
          }
        }

        // Is XSD field is provided?
        if ( meta.getXSDSource().equals( meta.SPECIFY_FIELDNAME ) ) {
          if ( meta.getXSDDefinedField() == null ) {
            logError( BaseMessages.getString( PKG, "XsdValidator.Log.Error.XSDFieldMissing" ) );
            throw new KettleStepException( BaseMessages.getString( PKG, "XsdValidator.Exception.XSDFieldMissing" ) );
          } else {
            // Let's check if the XSD field exist
            // Try to get XML Field index
            data.xsdindex = getInputRowMeta().indexOfValue( meta.getXSDDefinedField() );

            if ( data.xsdindex < 0 ) {
              // The field is unreachable !
              logError( BaseMessages
                  .getString( PKG, "XsdValidator.Log.ErrorFindingXSDField", meta.getXSDDefinedField() ) );
              throw new KettleStepException( BaseMessages.getString( PKG,
                  "XsdValidator.Exception.ErrorFindingXSDField", meta.getXSDDefinedField() ) );
            }
          }
        }

      } else {
        // XML stream field is missing !
        logError( BaseMessages.getString( PKG, "XsdValidator.Log.Error.XmlStreamFieldMissing" ) );
        throw new KettleStepException( BaseMessages.getString( PKG, "XsdValidator.Exception.XmlStreamFieldMissing" ) );
      }
    }

    try {

      // Get the XML field value
      String XMLFieldvalue = getInputRowMeta().getString( row, data.xmlindex );

      boolean isvalid = false;

      // XSD filename
      String xsdfilename = null;

      if ( meta.getXSDSource().equals( meta.SPECIFY_FILENAME ) ) {
        xsdfilename = environmentSubstitute( meta.getXSDFilename() );
      } else if ( meta.getXSDSource().equals( meta.SPECIFY_FIELDNAME ) ) {
        // Get the XSD field value
        xsdfilename = getInputRowMeta().getString( row, data.xsdindex );
      }

      // Get XSD filename
      FileObject xsdfile = null;
      String validationmsg = null;
      try {

        SchemaFactory factoryXSDValidator = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );

        xsdfile = KettleVFS.getFileObject( xsdfilename, getTransMeta() );

        // Get XML stream
        Source sourceXML = new StreamSource( new StringReader( XMLFieldvalue ) );

        if ( meta.getXMLSourceFile() ) {

          // We deal with XML file
          // Get XML File
          FileObject xmlfileValidator = KettleVFS.getFileObject( XMLFieldvalue );
          if ( xmlfileValidator == null || !xmlfileValidator.exists() ) {
            logError( BaseMessages.getString( PKG, "XsdValidator.Log.Error.XMLfileMissing", XMLFieldvalue ) );
            throw new KettleStepException( BaseMessages.getString( PKG, "XsdValidator.Exception.XMLfileMissing",
                XMLFieldvalue ) );
          }
          sourceXML = new StreamSource( xmlfileValidator.getContent().getInputStream() );
        }

        // create the schema
        Schema SchematXSD = null;
        if ( xsdfile instanceof AbstractFileObject ) {
          if ( xsdfile.getName().getURI().contains( "ram:///" ) ) {
            SchematXSD = factoryXSDValidator.newSchema( new StreamSource( xsdfile.getContent().getInputStream() ) );
          } else {
            SchematXSD = factoryXSDValidator.newSchema( new File( KettleVFS.getFilename( xsdfile ) ) );
          }
        } else {
          // we should not get here as anything entered in that does not look like
          // a url should be made a FileObject.
          throw new KettleStepException( BaseMessages.getString( PKG, "XsdValidator.Exception.CannotCreateSchema",
              xsdfile.getClass().getName() ) );
        }

        if ( meta.getXSDSource().equals( meta.NO_NEED ) ) {
          // ---Some documents specify the schema they expect to be validated against,
          // ---typically using xsi:noNamespaceSchemaLocation and/or xsi:schemaLocation attributes
          // ---Schema SchematXSD = factoryXSDValidator.newSchema();
          SchematXSD = factoryXSDValidator.newSchema();
        }

        // Create XSDValidator
        Validator xsdValidator = SchematXSD.newValidator();

        // Prevent against XML Entity Expansion (XEE) attacks.
        // https://www.owasp.org/index.php/XML_Security_Cheat_Sheet#XML_Entity_Expansion
        if ( !meta.isAllowExternalEntities() ) {
          xsdValidator.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", true );
          xsdValidator.setFeature( "http://xml.org/sax/features/external-general-entities", false );
          xsdValidator.setFeature( "http://xml.org/sax/features/external-parameter-entities", false );
          xsdValidator.setProperty( "http://apache.org/xml/properties/internal/entity-resolver",
            (XMLEntityResolver) xmlResourceIdentifier -> {
              String message = BaseMessages.getString( PKG, "XsdValidator.Exception.DisallowedDocType" );
              throw new IOException( message );
            } );
        }

        // Validate XML / XSD
        xsdValidator.validate( sourceXML );

        isvalid = true;

      } catch ( SAXException ex ) {
        validationmsg = ex.getMessage();
      } catch ( IOException ex ) {
        validationmsg = ex.getMessage();
      } finally {
        try {
          if ( xsdfile != null ) {
            xsdfile.close();
          }
        } catch ( IOException e ) {
          // Ignore errors
        }
      }

      Object[] outputRowData = null;
      Object[] outputRowData2 = null;

      if ( meta.getOutputStringField() ) {
        // Output type=String
        if ( isvalid ) {
          outputRowData =
              RowDataUtil.addValueData( row, getInputRowMeta().size(), environmentSubstitute( meta.getIfXmlValid() ) );
        } else {
          outputRowData =
              RowDataUtil.addValueData( row, getInputRowMeta().size(), environmentSubstitute( meta.getIfXmlInvalid() ) );
        }
      } else {
        outputRowData = RowDataUtil.addValueData( row, getInputRowMeta().size(), isvalid );
      }

      if ( meta.useAddValidationMessage() ) {
        outputRowData2 = RowDataUtil.addValueData( outputRowData, getInputRowMeta().size() + 1, validationmsg );
      } else {
        outputRowData2 = outputRowData;
      }

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "XsdValidator.Log.ReadRow" ) + " "
            + getInputRowMeta().getString( row ) );
      }

      // add new values to the row.
      putRow( data.outputRowMeta, outputRowData2 ); // copy row to output rowset(s);
    } catch ( KettleException e ) {
      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      }

      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), row, 1, errorMessage, null, "XSD001" );
      } else {
        logError( BaseMessages.getString( PKG, "XsdValidator.ErrorProcesing" + " : " + e.getMessage() ) );
        throw new KettleStepException( BaseMessages.getString( PKG, "XsdValidator.ErrorProcesing" ), e );
      }
    }

    return true;

  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (XsdValidatorMeta) smi;
    data = (XsdValidatorData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.
      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (XsdValidatorMeta) smi;
    data = (XsdValidatorData) sdi;

    super.dispose( smi, sdi );
  }

}
