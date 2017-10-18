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

package org.pentaho.di.job.entries.xslt;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'xslt' job entry.
 * 
 * @author Samatar Hassan
 * @since 02-03-2007
 * 
 */
@JobEntry( id = "XSLT", i18nPackageName = "org.pentaho.di.job.entries.xslt", image = "XSLT.svg", name = "XSLT.Name",
    description = "XSLT.Description", categoryDescription = "XSLT.Category",
    documentationUrl = "http://wiki.pentaho.com/display/EAI/XSL+Transformation+%28Job+Entry%29" )
public class JobEntryXSLT extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryXSLT.class; // for i18n purposes, needed by Translator2!!

  public static String FACTORY_JAXP = "JAXP";
  public static String FACTORY_SAXON = "SAXON";

  private String xmlfilename;
  private String xslfilename;
  private String outputfilename;
  public int iffileexists;
  private boolean addfiletoresult;
  private String xsltfactory;
  private boolean filenamesfromprevious;

  /** output property name */
  private String[] outputPropertyName;

  /** output property value */
  private String[] outputPropertyValue;

  /** parameter name */
  private String[] parameterName;

  /** parameter field */
  private String[] parameterField;

  private int nrParams;
  private String[] nameOfParams;
  private String[] valueOfParams;
  private boolean useParameters;

  private Properties outputProperties;
  private boolean setOutputProperties;

  public JobEntryXSLT( String n ) {
    super( n, "" );
    xmlfilename = null;
    xslfilename = null;
    outputfilename = null;
    iffileexists = 1;
    addfiletoresult = false;
    filenamesfromprevious = false;
    xsltfactory = FACTORY_JAXP;
    int nrparams = 0;
    int nroutputproperties = 0;
    allocate( nrparams, nroutputproperties );

    for ( int i = 0; i < nrparams; i++ ) {
      parameterField[i] = "param" + i;
      parameterName[i] = "param";
    }
    for ( int i = 0; i < nroutputproperties; i++ ) {
      outputPropertyName[i] = "outputprop" + i;
      outputPropertyValue[i] = "outputprop";
    }
  }

  public void allocate( int nrParameters, int outputProps ) {
    parameterName = new String[nrParameters];
    parameterField = new String[nrParameters];

    outputPropertyName = new String[outputProps];
    outputPropertyValue = new String[outputProps];
  }

  public JobEntryXSLT() {
    this( "" );
  }

  public Object clone() {
    JobEntryXSLT je = (JobEntryXSLT) super.clone();
    int nrparams = parameterName.length;
    int nroutputprops = outputPropertyName.length;
    je.allocate( nrparams, nroutputprops );

    for ( int i = 0; i < nrparams; i++ ) {
      je.parameterName[i] = parameterName[i];
      je.parameterField[i] = parameterField[i];
    }
    for ( int i = 0; i < nroutputprops; i++ ) {
      je.outputPropertyName[i] = outputPropertyName[i];
      je.outputPropertyValue[i] = outputPropertyValue[i];
    }

    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer( 50 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "xmlfilename", xmlfilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "xslfilename", xslfilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "outputfilename", outputfilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "iffileexists", iffileexists ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "addfiletoresult", addfiletoresult ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "filenamesfromprevious", filenamesfromprevious ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "xsltfactory", xsltfactory ) );

    retval.append( "    <parameters>" ).append( Const.CR );

    for ( int i = 0; i < parameterName.length; i++ ) {
      retval.append( "      <parameter>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field", parameterField[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", parameterName[i] ) );
      retval.append( "      </parameter>" ).append( Const.CR );
    }

    retval.append( "    </parameters>" ).append( Const.CR );
    retval.append( "    <outputproperties>" ).append( Const.CR );

    for ( int i = 0; i < outputPropertyName.length; i++ ) {
      retval.append( "      <outputproperty>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", outputPropertyName[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "value", outputPropertyValue[i] ) );
      retval.append( "      </outputproperty>" ).append( Const.CR );
    }

    retval.append( "    </outputproperties>" ).append( Const.CR );
    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep,
      IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      xmlfilename = XMLHandler.getTagValue( entrynode, "xmlfilename" );
      xslfilename = XMLHandler.getTagValue( entrynode, "xslfilename" );
      outputfilename = XMLHandler.getTagValue( entrynode, "outputfilename" );
      iffileexists = Const.toInt( XMLHandler.getTagValue( entrynode, "iffileexists" ), -1 );
      addfiletoresult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "addfiletoresult" ) );
      filenamesfromprevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "filenamesfromprevious" ) );
      xsltfactory = XMLHandler.getTagValue( entrynode, "xsltfactory" );
      if ( xsltfactory == null ) {
        xsltfactory = FACTORY_JAXP;
      }
      Node parametersNode = XMLHandler.getSubNode( entrynode, "parameters" );
      int nrparams = XMLHandler.countNodes( parametersNode, "parameter" );

      Node parametersOutputProps = XMLHandler.getSubNode( entrynode, "outputproperties" );
      int nroutputprops = XMLHandler.countNodes( parametersOutputProps, "outputproperty" );
      allocate( nrparams, nroutputprops );

      for ( int i = 0; i < nrparams; i++ ) {
        Node anode = XMLHandler.getSubNodeByNr( parametersNode, "parameter", i );
        parameterField[i] = XMLHandler.getTagValue( anode, "field" );
        parameterName[i] = XMLHandler.getTagValue( anode, "name" );
      }
      for ( int i = 0; i < nroutputprops; i++ ) {
        Node anode = XMLHandler.getSubNodeByNr( parametersOutputProps, "outputproperty", i );
        outputPropertyName[i] = XMLHandler.getTagValue( anode, "name" );
        outputPropertyValue[i] = XMLHandler.getTagValue( anode, "value" );
      }

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'xslt' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
      List<SlaveServer> slaveServers ) throws KettleException {
    try {
      xmlfilename = rep.getJobEntryAttributeString( id_jobentry, "xmlfilename" );
      xslfilename = rep.getJobEntryAttributeString( id_jobentry, "xslfilename" );
      outputfilename = rep.getJobEntryAttributeString( id_jobentry, "outputfilename" );
      iffileexists = (int) rep.getJobEntryAttributeInteger( id_jobentry, "iffileexists" );
      addfiletoresult = rep.getJobEntryAttributeBoolean( id_jobentry, "addfiletoresult" );
      filenamesfromprevious = rep.getJobEntryAttributeBoolean( id_jobentry, "filenamesfromprevious" );
      xsltfactory = rep.getJobEntryAttributeString( id_jobentry, "xsltfactory" );
      if ( xsltfactory == null ) {
        xsltfactory = FACTORY_JAXP;
      }

      int nrparams = rep.countNrJobEntryAttributes( id_jobentry, "param_name" );
      int nroutputprops = rep.countNrJobEntryAttributes( id_jobentry, "output_property_name" );
      allocate( nrparams, nroutputprops );

      for ( int i = 0; i < nrparams; i++ ) {
        parameterField[i] = rep.getJobEntryAttributeString( id_jobentry, i, "param_field" );
        parameterName[i] = rep.getJobEntryAttributeString( id_jobentry, i, "param_name" );
      }
      for ( int i = 0; i < nroutputprops; i++ ) {
        outputPropertyName[i] = rep.getJobEntryAttributeString( id_jobentry, i, "output_property_name" );
        outputPropertyValue[i] = rep.getJobEntryAttributeString( id_jobentry, i, "output_property_value" );
      }
    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'xslt' from the repository for id_jobentry="
          + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "xmlfilename", xmlfilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "xslfilename", xslfilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "outputfilename", outputfilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "iffileexists", iffileexists );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "addfiletoresult", addfiletoresult );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "filenamesfromprevious", filenamesfromprevious );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "xsltfactory", xsltfactory );

      for ( int i = 0; i < parameterName.length; i++ ) {
        rep.saveJobEntryAttribute( id_job, getObjectId(), i, "param_field", parameterField[i] );
        rep.saveJobEntryAttribute( id_job, getObjectId(), i, "param_name", parameterName[i] );
      }
      for ( int i = 0; i < outputPropertyName.length; i++ ) {
        rep.saveJobEntryAttribute( id_job, getObjectId(), i, "output_property_name", outputPropertyName[i] );
        rep.saveJobEntryAttribute( id_job, getObjectId(), i, "output_property_value", outputPropertyValue[i] );
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'xslt' to the repository for id_job=" + id_job, dbe );
    }
  }

  public String getXSLTFactory() {
    return xsltfactory;
  }

  public void setXSLTFactory( String xsltfactoryin ) {
    xsltfactory = xsltfactoryin;
  }

  public String getRealxmlfilename() {
    return environmentSubstitute( getxmlFilename() );
  }

  public String getoutputfilename() {
    return environmentSubstitute( getoutputFilename() );
  }

  public boolean isFilenamesFromPrevious() {
    return filenamesfromprevious;
  }

  public void setFilenamesFromPrevious( boolean filenamesfromprevious ) {
    this.filenamesfromprevious = filenamesfromprevious;
  }

  public String getRealxslfilename() {
    return environmentSubstitute( getxslFilename() );
  }

  public Result execute( Result previousResult, int nr ) throws KettleException {
    Result result = previousResult;
    int NrErrors = 0;
    int NrSuccess = 0;

    // Check output parameters
    int nrOutputProps = getOutputPropertyName() == null ? 0 : getOutputPropertyName().length;
    if ( nrOutputProps > 0 ) {
      outputProperties = new Properties();
      for ( int i = 0; i < nrOutputProps; i++ ) {
        outputProperties.put( getOutputPropertyName()[i], environmentSubstitute( getOutputPropertyValue()[i] ) );
      }
      setOutputProperties = true;
    }

    // Check parameters
    nrParams = getParameterField() == null ? 0 : getParameterField().length;
    if ( nrParams > 0 ) {
      nameOfParams = new String[nrParams];
      valueOfParams = new String[nrParams];
      for ( int i = 0; i < nrParams; i++ ) {
        String name = environmentSubstitute( getParameterName()[i] );
        String value = environmentSubstitute( getParameterField()[i] );
        if ( Utils.isEmpty( value ) ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "Xslt.Exception.ParameterFieldMissing", name, i ) );
        }
        nameOfParams[i] = name;
        valueOfParams[i] = value;
      }
      useParameters = true;
    }

    List<RowMetaAndData> rows = result.getRows();
    if ( isFilenamesFromPrevious() ) {
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryXSLT.Log.ArgFromPrevious.Found", ( rows != null ? rows
            .size() : 0 )
            + "" ) );
      }
    }

    if ( isFilenamesFromPrevious() && rows != null ) {
      // Copy the input row to the (command line) arguments
      RowMetaAndData resultRow = null;
      for ( int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++ ) {
        resultRow = rows.get( iteration );

        // Get filenames (xml, xsl, output filename)
        String xmlfilename_previous = resultRow.getString( 0, null );
        String xslfilename_previous = resultRow.getString( 1, null );
        String ouputfilename_previous = resultRow.getString( 2, null );

        if ( !Utils.isEmpty( xmlfilename_previous ) && !Utils.isEmpty( xslfilename_previous )
            && !Utils.isEmpty( ouputfilename_previous ) ) {
          if ( processOneXMLFile( xmlfilename_previous, xslfilename_previous, ouputfilename_previous, result, parentJob ) ) {
            NrSuccess++;
          } else {
            NrErrors++;
          }
        } else {
          // We failed!
          logError( BaseMessages.getString( PKG, "JobEntryXSLT.AllFilesNotNull.Label" ) );
          NrErrors++;
        }

      }
    } else {
      String realxmlfilename = getRealxmlfilename();
      String realxslfilename = getRealxslfilename();
      String realoutputfilename = getoutputfilename();
      if ( !Utils.isEmpty( realxmlfilename ) && !Utils.isEmpty( realxslfilename )
          && !Utils.isEmpty( realoutputfilename ) ) {
        if ( processOneXMLFile( realxmlfilename, realxslfilename, realoutputfilename, result, parentJob ) ) {
          NrSuccess++;
        } else {
          NrErrors++;
        }
      } else {
        // We failed!
        logError( BaseMessages.getString( PKG, "JobEntryXSLT.AllFilesNotNull.Label" ) );
        NrErrors++;
      }
    }

    result.setResult( NrErrors == 0 );
    result.setNrErrors( NrErrors );
    result.setNrLinesWritten( NrSuccess );

    return result;
  }

  private boolean processOneXMLFile( String xmlfilename, String xslfilename, String outputfilename, Result result,
      Job parentJob ) {
    boolean retval = false;
    FileObject xmlfile = null;
    FileObject xslfile = null;
    FileObject outputfile = null;

    try {
      xmlfile = KettleVFS.getFileObject( xmlfilename, this );
      xslfile = KettleVFS.getFileObject( xslfilename, this );
      outputfile = KettleVFS.getFileObject( outputfilename, this );

      if ( xmlfile.exists() && xslfile.exists() ) {
        if ( outputfile.exists() && iffileexists == 2 ) {
          // Output file exists
          // User want to fail
          logError( BaseMessages.getString( PKG, "JobEntryXSLT.OuputFileExists1.Label" ) + outputfilename
              + BaseMessages.getString( PKG, "JobEntryXSLT.OuputFileExists2.Label" ) );
          return retval;

        } else if ( outputfile.exists() && iffileexists == 1 ) {
          // Do nothing
          if ( log.isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "JobEntryXSLT.OuputFileExists1.Label" ) + outputfilename
                + BaseMessages.getString( PKG, "JobEntryXSLT.OuputFileExists2.Label" ) );
          }
          retval = true;
          return retval;

        } else {
          if ( outputfile.exists() && iffileexists == 0 ) {
            // the output file exists and user want to create new one with unique name
            // Format Date

            // Try to clean filename (without wildcard)
            String wildcard = outputfilename.substring( outputfilename.length() - 4, outputfilename.length() );
            if ( wildcard.substring( 0, 1 ).equals( "." ) ) {
              // Find wildcard
              outputfilename =
                  outputfilename.substring( 0, outputfilename.length() - 4 ) + "_"
                      + StringUtil.getFormattedDateTimeNow( true ) + wildcard;
            } else {
              // did not find wildcard
              outputfilename = outputfilename + "_" + StringUtil.getFormattedDateTimeNow( true );
            }
            if ( log.isDebug() ) {
              logDebug( BaseMessages.getString( PKG, "JobEntryXSLT.OuputFileExists1.Label" ) + outputfilename
                  + BaseMessages.getString( PKG, "JobEntryXSLT.OuputFileExists2.Label" ) );
              logDebug( BaseMessages.getString( PKG, "JobEntryXSLT.OuputFileNameChange1.Label" ) + outputfilename
                  + BaseMessages.getString( PKG, "JobEntryXSLT.OuputFileNameChange2.Label" ) );
            }
          }

          // Create transformer factory
          TransformerFactory factory = TransformerFactory.newInstance();

          if ( xsltfactory.equals( FACTORY_SAXON ) ) {
            // Set the TransformerFactory to the SAXON implementation.
            factory = new net.sf.saxon.TransformerFactoryImpl();
          }

          if ( log.isDetailed() ) {
            log.logDetailed( BaseMessages.getString( PKG, "JobEntryXSL.Log.TransformerFactoryInfos" ), BaseMessages
                .getString( PKG, "JobEntryXSL.Log.TransformerFactory", factory.getClass().getName() ) );
          }

          InputStream xslInputStream = KettleVFS.getInputStream( xslfile );
          InputStream xmlInputStream = KettleVFS.getInputStream( xmlfile );
          OutputStream os = null;
          try {
            // Use the factory to create a template containing the xsl file
            Templates template = factory.newTemplates( new StreamSource( xslInputStream ) );

            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();

            if ( log.isDetailed() ) {
              log.logDetailed( BaseMessages.getString( PKG, "JobEntryXSL.Log.TransformerClassInfos" ), BaseMessages
                  .getString( PKG, "JobEntryXSL.Log.TransformerClass", xformer.getClass().getName() ) );
            }

            // Do we need to set output properties?
            if ( setOutputProperties ) {
              xformer.setOutputProperties( outputProperties );
            }

            // Do we need to pass parameters?
            if ( useParameters ) {
              for ( int i = 0; i < nrParams; i++ ) {
                xformer.setParameter( nameOfParams[i], valueOfParams[i] );
              }
            }

            // Prepare the input and output files
            Source source = new StreamSource( xmlInputStream );
            os = KettleVFS.getOutputStream( outputfile, false );
            StreamResult resultat = new StreamResult( os );

            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform( source, resultat );

            if ( isAddFileToResult() ) {
              // Add output filename to output files
              ResultFile resultFile =
                  new ResultFile( ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject( outputfilename, this ),
                      parentJob.getJobname(), toString() );
              result.getResultFiles().put( resultFile.getFile().toString(), resultFile );
            }

            // Everything is OK
            retval = true;
          } finally {
            try {
              xslInputStream.close();
            } catch ( IOException ignored ) {
              // ignore IO Exception on close
            }
            try {
              xmlInputStream.close();
            } catch ( IOException ignored ) {
              // ignore IO Exception on close
            }
            try {
              if ( os != null ) {
                os.close();
              }
            } catch ( IOException ignored ) {
              // ignore IO Exception on close
            }
          }
        }
      } else {

        if ( !xmlfile.exists() ) {
          logError( BaseMessages.getString( PKG, "JobEntryXSLT.FileDoesNotExist1.Label" ) + xmlfilename
              + BaseMessages.getString( PKG, "JobEntryXSLT.FileDoesNotExist2.Label" ) );
        }
        if ( !xslfile.exists() ) {
          logError( BaseMessages.getString( PKG, "JobEntryXSLT.FileDoesNotExist1.Label" ) + xmlfilename
              + BaseMessages.getString( PKG, "JobEntryXSLT.FileDoesNotExist2.Label" ) );
        }
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobEntryXSLT.ErrorXLST.Label" )
          + BaseMessages.getString( PKG, "JobEntryXSLT.ErrorXLSTXML1.Label" ) + xmlfilename
          + BaseMessages.getString( PKG, "JobEntryXSLT.ErrorXLSTXML2.Label" )
          + BaseMessages.getString( PKG, "JobEntryXSLT.ErrorXLSTXSL1.Label" ) + xslfilename
          + BaseMessages.getString( PKG, "JobEntryXSLT.ErrorXLSTXSL2.Label" ) + e.getMessage() );
    } finally {
      try {
        if ( xmlfile != null ) {
          xmlfile.close();
        }

        if ( xslfile != null ) {
          xslfile.close();
        }
        if ( outputfile != null ) {
          outputfile.close();
        }
      } catch ( IOException e ) {
        logError( "Unable to close file", e );
      }
    }

    return retval;
  }

  public boolean evaluates() {
    return true;
  }

  public void setxmlFilename( String filename ) {
    this.xmlfilename = filename;
  }

  public String getxmlFilename() {
    return xmlfilename;
  }

  public String getoutputFilename() {
    return outputfilename;
  }

  public void setoutputFilename( String outputfilename ) {
    this.outputfilename = outputfilename;
  }

  public void setxslFilename( String filename ) {
    this.xslfilename = filename;
  }

  public String getxslFilename() {
    return xslfilename;
  }

  public void setAddFileToResult( boolean addfiletoresultin ) {
    this.addfiletoresult = addfiletoresultin;
  }

  public boolean isAddFileToResult() {
    return addfiletoresult;
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( ( !Utils.isEmpty( xslfilename ) ) && ( !Utils.isEmpty( xmlfilename ) ) ) {
      String realXmlFileName = jobMeta.environmentSubstitute( xmlfilename );
      String realXslFileName = jobMeta.environmentSubstitute( xslfilename );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realXmlFileName, ResourceType.FILE ) );
      reference.getEntries().add( new ResourceEntry( realXslFileName, ResourceType.FILE ) );
      references.add( reference );
    }
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    ValidatorContext ctx = new ValidatorContext();
    putVariableSpace( ctx, getVariables() );
    putValidators( ctx, notBlankValidator(), fileExistsValidator() );
    andValidator().validate( this, "xmlFilename", remarks, ctx );
    andValidator().validate( this, "xslFilename", remarks, ctx );

    andValidator().validate( this, "outputFilename", remarks, putValidators( notBlankValidator() ) );
  }

  /**
   * @return Returns the OutputPropertyName.
   */
  public String[] getOutputPropertyName() {
    return outputPropertyName;
  }

  /**
   * @param argumentDirection
   *          The OutputPropertyName to set.
   */
  public void setOutputPropertyName( String[] argumentDirection ) {
    this.outputPropertyName = argumentDirection;
  }

  /**
   * @return Returns the OutputPropertyField.
   */
  public String[] getOutputPropertyValue() {
    return outputPropertyValue;
  }

  /**
   * @param argumentDirection
   *          The outputPropertyValue to set.
   */
  public void setOutputPropertyValue( String[] argumentDirection ) {
    this.outputPropertyValue = argumentDirection;
  }

  /**
   * @return Returns the parameterName.
   */
  public String[] getParameterName() {
    return parameterName;
  }

  /**
   * @param argumentDirection
   *          The parameterName to set.
   */
  public void setParameterName( String[] argumentDirection ) {
    this.parameterName = argumentDirection;
  }

  /**
   * @return Returns the parameterField.
   */
  public String[] getParameterField() {
    return parameterField;
  }
}
