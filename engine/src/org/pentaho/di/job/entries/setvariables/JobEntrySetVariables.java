/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.setvariables;

import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.exception.KettleXMLException;
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
 * This defines a 'Set variables' job entry.
 *
 * @author Samatar Hassan
 * @since 06-05-2007
 */
public class JobEntrySetVariables extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntrySetVariables.class; // for i18n purposes, needed by Translator2!!

  public boolean replaceVars;

  public String[] variableName;

  public String[] variableValue;

  public int[] variableType;

  public String filename;

  public int fileVariableType;

  public static final int VARIABLE_TYPE_JVM = 0;
  public static final int VARIABLE_TYPE_CURRENT_JOB = 1;
  public static final int VARIABLE_TYPE_PARENT_JOB = 2;
  public static final int VARIABLE_TYPE_ROOT_JOB = 3;

  public static final String[] variableTypeCode = { "JVM", "CURRENT_JOB", "PARENT_JOB", "ROOT_JOB" };
  private static final String[] variableTypeDesc = {
    BaseMessages.getString( PKG, "JobEntrySetVariables.VariableType.JVM" ),
    BaseMessages.getString( PKG, "JobEntrySetVariables.VariableType.CurrentJob" ),
    BaseMessages.getString( PKG, "JobEntrySetVariables.VariableType.ParentJob" ),
    BaseMessages.getString( PKG, "JobEntrySetVariables.VariableType.RootJob" ), };

  public JobEntrySetVariables( String n ) {
    super( n, "" );
    replaceVars = true;
    variableName = null;
    variableValue = null;
  }

  public JobEntrySetVariables() {
    this( "" );
  }

  public void allocate( int nrFields ) {
    variableName = new String[nrFields];
    variableValue = new String[nrFields];
    variableType = new int[nrFields];
  }

  public Object clone() {
    JobEntrySetVariables je = (JobEntrySetVariables) super.clone();
    if ( variableName != null ) {
      int nrFields = variableName.length;
      je.allocate( nrFields );
      System.arraycopy( variableName, 0, je.variableName, 0, nrFields );
      System.arraycopy( variableValue, 0, je.variableValue, 0, nrFields );
      System.arraycopy( variableType, 0, je.variableType, 0, nrFields );
    }
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );
    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "replacevars", replaceVars ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "filename", filename ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "file_variable_type", getVariableTypeCode( fileVariableType ) ) );

    retval.append( "      <fields>" ).append( Const.CR );
    if ( variableName != null ) {
      for ( int i = 0; i < variableName.length; i++ ) {
        retval.append( "        <field>" ).append( Const.CR );
        retval.append( "          " ).append( XMLHandler.addTagValue( "variable_name", variableName[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "variable_value", variableValue[i] ) );
        retval.append( "          " ).append(
          XMLHandler.addTagValue( "variable_type", getVariableTypeCode( variableType[i] ) ) );
        retval.append( "        </field>" ).append( Const.CR );
      }
    }
    retval.append( "      </fields>" ).append( Const.CR );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      replaceVars = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "replacevars" ) );

      filename = XMLHandler.getTagValue( entrynode, "filename" );
      fileVariableType = getVariableType( XMLHandler.getTagValue( entrynode, "file_variable_type" ) );

      Node fields = XMLHandler.getSubNode( entrynode, "fields" );
      // How many field variableName?
      int nrFields = XMLHandler.countNodes( fields, "field" );
      allocate( nrFields );

      // Read them all...
      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        variableName[i] = XMLHandler.getTagValue( fnode, "variable_name" );
        variableValue[i] = XMLHandler.getTagValue( fnode, "variable_value" );
        variableType[i] = getVariableType( XMLHandler.getTagValue( fnode, "variable_type" ) );

      }
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobEntrySetVariables.Meta.UnableLoadXML", xe
        .getMessage() ), xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      replaceVars = rep.getJobEntryAttributeBoolean( id_jobentry, "replacevars" );

      filename = rep.getJobEntryAttributeString( id_jobentry, "filename" );
      fileVariableType = getVariableType( rep.getJobEntryAttributeString( id_jobentry, "file_variable_type" ) );

      // How many variableName?
      int argnr = rep.countNrJobEntryAttributes( id_jobentry, "variable_name" );
      allocate( argnr );

      // Read them all...
      for ( int a = 0; a < argnr; a++ ) {
        variableName[a] = rep.getJobEntryAttributeString( id_jobentry, a, "variable_name" );
        variableValue[a] = rep.getJobEntryAttributeString( id_jobentry, a, "variable_value" );
        variableType[a] = getVariableType( rep.getJobEntryAttributeString( id_jobentry, a, "variable_type" ) );
      }
    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntrySetVariables.Meta.UnableLoadRep", String
        .valueOf( id_jobentry ), dbe.getMessage() ), dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "replacevars", replaceVars );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "filename", filename );
      rep.saveJobEntryAttribute(
        id_job, getObjectId(), "file_variable_type", getVariableTypeCode( fileVariableType ) );

      // save the variableName...
      if ( variableName != null ) {
        for ( int i = 0; i < variableName.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "variable_name", variableName[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "variable_value", variableValue[i] );
          rep.saveJobEntryAttribute(
            id_job, getObjectId(), i, "variable_type", getVariableTypeCode( variableType[i] ) );
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntrySetVariables.Meta.UnableSaveRep", String
        .valueOf( id_job ), dbe.getMessage() ), dbe );
    }
  }

  public Result execute( Result result, int nr ) throws KettleException {
    result.setResult( true );
    result.setNrErrors( 0 );
    try {

      List<String> variables = new ArrayList<String>();
      List<String> variableValues = new ArrayList<String>();
      List<Integer> variableTypes = new ArrayList<Integer>();

      String realFilename = environmentSubstitute( filename );
      if ( !Utils.isEmpty( realFilename ) ) {
        try ( InputStream is = KettleVFS.getInputStream( realFilename );
            // for UTF8 properties files
            InputStreamReader isr = new InputStreamReader( is, "UTF-8" );
            BufferedReader reader = new BufferedReader( isr );
            ) {
          Properties properties = new Properties();
          properties.load( reader );
          for ( Object key : properties.keySet() ) {
            variables.add( (String) key );
            variableValues.add( (String) properties.get( key ) );
            variableTypes.add( fileVariableType );
          }
        } catch ( Exception e ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "JobEntrySetVariables.Error.UnableReadPropertiesFile", realFilename ) );
        }
      }
      for ( int i = 0; i < variableName.length; i++ ) {
        variables.add( variableName[i] );
        variableValues.add( variableValue[i] );
        variableTypes.add( variableType[i] );
      }

      for ( int i = 0; i < variables.size(); i++ ) {
        String varname = variables.get( i );
        String value = variableValues.get( i );
        int type = variableTypes.get( i );

        if ( replaceVars ) {
          varname = environmentSubstitute( varname );
          value = environmentSubstitute( value );
        }

        // OK, where do we set this value...
        switch ( type ) {
          case VARIABLE_TYPE_JVM:
            System.setProperty( varname, value );
            setVariable( varname, value );
            Job parentJobTraverse = parentJob;
            while ( parentJobTraverse != null ) {
              parentJobTraverse.setVariable( varname, value );
              parentJobTraverse = parentJobTraverse.getParentJob();
            }
            break;

          case VARIABLE_TYPE_ROOT_JOB:
            // set variable in this job entry
            setVariable( varname, value );
            Job rootJob = parentJob;
            while ( rootJob != null ) {
              rootJob.setVariable( varname, value );
              rootJob = rootJob.getParentJob();
            }
            break;

          case VARIABLE_TYPE_CURRENT_JOB:
            setVariable( varname, value );
            if ( parentJob != null ) {
              parentJob.setVariable( varname, value );
            } else {
              throw new KettleJobException( BaseMessages.getString(
                PKG, "JobEntrySetVariables.Error.UnableSetVariableCurrentJob", varname ) );
            }
            break;

          case VARIABLE_TYPE_PARENT_JOB:
            setVariable( varname, value );

            if ( parentJob != null ) {
              parentJob.setVariable( varname, value );
              Job gpJob = parentJob.getParentJob();
              if ( gpJob != null ) {
                gpJob.setVariable( varname, value );
              } else {
                throw new KettleJobException( BaseMessages.getString(
                  PKG, "JobEntrySetVariables.Error.UnableSetVariableParentJob", varname ) );
              }
            } else {
              throw new KettleJobException( BaseMessages.getString(
                PKG, "JobEntrySetVariables.Error.UnableSetVariableCurrentJob", varname ) );
            }
            break;

          default:
            break;
        }

        // ok we can process this line
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntrySetVariables.Log.SetVariableToValue", varname, value ) );
        }
      }
    } catch ( Exception e ) {
      result.setResult( false );
      result.setNrErrors( 1 );

      logError( BaseMessages.getString( PKG, "JobEntrySetVariables.UnExcpectedError", e.getMessage() ) );
    }

    return result;
  }

  public void setReplaceVars( boolean replaceVars ) {
    this.replaceVars = replaceVars;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isReplaceVars() {
    return replaceVars;
  }

  public String[] getVariableValue() {
    return variableValue;
  }

  /**
   * @param fieldValue
   *          The fieldValue to set.
   */
  public void setVariableName( String[] fieldValue ) {
    this.variableName = fieldValue;
  }

  /**
   * @return Returns the local variable flag: true if this variable is only valid in the parents job.
   */
  public int[] getVariableType() {
    return variableType;
  }

  /**
   * @param variableType
   *          The variable type, see also VARIABLE_TYPE_...
   * @return the variable type code for this variable type
   */
  public static final String getVariableTypeCode( int variableType ) {
    return variableTypeCode[variableType];
  }

  /**
   * @param variableType
   *          The variable type, see also VARIABLE_TYPE_...
   * @return the variable type description for this variable type
   */
  public static final String getVariableTypeDescription( int variableType ) {
    return variableTypeDesc[variableType];
  }

  /**
   * @param variableType
   *          The code or description of the variable type
   * @return The variable type
   */
  public static final int getVariableType( String variableType ) {
    for ( int i = 0; i < variableTypeCode.length; i++ ) {
      if ( variableTypeCode[i].equalsIgnoreCase( variableType ) ) {
        return i;
      }
    }
    for ( int i = 0; i < variableTypeDesc.length; i++ ) {
      if ( variableTypeDesc[i].equalsIgnoreCase( variableType ) ) {
        return i;
      }
    }
    return VARIABLE_TYPE_JVM;
  }

  /**
   * @param localVariable
   *          The localVariable to set.
   */
  public void setVariableType( int[] localVariable ) {
    this.variableType = localVariable;
  }

  public static final String[] getVariableTypeDescriptions() {
    return variableTypeDesc;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    boolean res = JobEntryValidatorUtils.andValidator().validate( this, "variableName", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );

    if ( res == false ) {
      return;
    }

    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notNullValidator(), JobEntryValidatorUtils.fileExistsValidator() );

    for ( int i = 0; i < variableName.length; i++ ) {
      JobEntryValidatorUtils.andValidator().validate( this, "variableName[" + i + "]", remarks, ctx );
    }
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( variableName != null ) {
      ResourceReference reference = null;
      for ( int i = 0; i < variableName.length; i++ ) {
        String filename = jobMeta.environmentSubstitute( variableName[i] );
        if ( reference == null ) {
          reference = new ResourceReference( this );
          references.add( reference );
        }
        reference.getEntries().add( new ResourceEntry( filename, ResourceType.FILE ) );
      }
    }
    return references;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename
   *          the filename to set
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  /**
   * @return the fileVariableType
   */
  public int getFileVariableType() {
    return fileVariableType;
  }

  /**
   * @param fileVariableType
   *          the fileVariableType to set
   */
  public void setFileVariableType( int fileVariableType ) {
    this.fileVariableType = fileVariableType;
  }
}
