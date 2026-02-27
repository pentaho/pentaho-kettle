/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package com.pentaho.repository.importexport;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.util.IPdiContentProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PdiContentProvider implements IPdiContentProvider {

  private Log log = LogFactory.getLog( PdiContentProvider.class );

  IUnifiedRepository unifiedRepository = PentahoSystem.get( IUnifiedRepository.class, null );

  @Override
  public boolean hasUserParameters( String kettleFilePath ) {

    if ( !StringUtils.isEmpty( kettleFilePath ) ) {

      RepositoryFile file = unifiedRepository.getFile( kettleFilePath );

      if ( file != null ) {

        try {

          return hasUserParameters( getMeta( file ) );

        } catch ( KettleException e ) {
          log.error( e );
        }
      }
    }

    return false;
  }

  @Override
  public Map<String, String> getUserParameters( String kettleFilePath ) {
    Map<String, String> userParams = new HashMap<>();
    if ( !StringUtils.isEmpty( kettleFilePath ) ) {
      RepositoryFile file = unifiedRepository.getFile( kettleFilePath );
      if ( file != null ) {
        try {
          NamedParams np = getMeta( file );
          userParams = getUserParametersFromMeta( np );
        } catch ( KettleException e ) {
          log.error( e );
        }
      }
    }
    return userParams;
  }

  @Override
  public Map<String, String> getUserParameters( Object obj ) {
    Map<String, String> userParams = new HashMap<>();
    if ( obj instanceof FileObject fileObject ) {
      try {
        NamedParams np = getMeta( fileObject );
        userParams = getUserParametersFromMeta( np );
      } catch ( KettleException e ) {
        log.error( e );
      }
    }
    return userParams;
  }

  private Map<String, String> getUserParametersFromMeta( NamedParams np ) throws UnknownParamException {
    Map<String, String> userParams = new HashMap<>();
    np = filterUserParameters( np );
    if ( !isEmpty( np ) ) {
      for ( String s : np.listParameters() ) {
        if ( null == np.getParameterValue( s ) || np.getParameterValue( s ).equalsIgnoreCase( "" ) ) {
          userParams.put( s, np.getParameterDefault( s ) );
        } else {
          userParams.put( s, np.getParameterValue( s ) );
        }
      }
    }
    return userParams;
  }

  @Override
  public Map<String, String> getVariables( String kettleFilePath ) {
    Map<String, String> userVariables = new HashMap<>();

    if ( !StringUtils.isEmpty( kettleFilePath ) ) {
      RepositoryFile file = unifiedRepository.getFile( kettleFilePath );

      if ( file != null ) {
        try {
          String extension = FilenameUtils.getExtension( file.getName() );
          Repository repo = PDIImportUtil.connectToRepository( null );
          InputStream inputStream = "ktr".equalsIgnoreCase( extension )
            ? convertTransformation( file.getId() )
            : convertJob( file.getId() );
          userVariables = extractVariablesFromStream( inputStream, file.getName(), extension, repo );
        } catch ( KettleException e ) {
          log.error( e );
        }
      }
      filterInternalVariables( userVariables );
    }
    return userVariables;
  }

  @Override
  public Map<String, String> getVariables( Object obj ) {
    Map<String, String> userVariables = new HashMap<>();

    if ( obj instanceof FileObject fileObject ) {
      try {
        String extension = FilenameUtils.getExtension( fileObject.getName().getBaseName() );
        Repository repo = PDIImportUtil.connectToRepository( null );
        try ( InputStream inputStream = fileObject.getContent().getInputStream() ) {
          userVariables = extractVariablesFromStream( inputStream, fileObject.getName().getBaseName(), extension, repo );
        }
      } catch ( IOException | KettleException e ) {
        log.error( e );
      }
      filterInternalVariables( userVariables );
    }
    return userVariables;
  }

  private Map<String, String> getTransVars( TransMeta transMeta, String fileName ) {
    /*
     * The following code was more or less copy/pasted from SpoonTransformationDelegate and
     * TransExecutionConfigurationDialog
     */
    TransExecutionConfiguration transExeConf = new TransExecutionConfiguration();
    Map<String, String> variableMap = new HashMap<>();
    variableMap.putAll( transExeConf.getVariables() ); // the default
    variableMap.put( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, fileName );

    transExeConf.setVariables( variableMap );
    transExeConf.getUsedVariables( transMeta );

    return transExeConf.getVariables();
  }

  private Map<String, String> getJobVars( JobMeta jobMeta, String fileName ) {
    /*
     * The following code was more or less copy/pasted from SpoonJobDelegate and JobExecutionConfigurationDialog
     */
    JobExecutionConfiguration jobExeConf = new JobExecutionConfiguration();
    Map<String, String> variableMap = new HashMap<>();

    jobExeConf.setVariables( variableMap );
    jobExeConf.getUsedVariables( jobMeta );
    Map<String, String> jobVarsMap = jobExeConf.getVariables();
    jobVarsMap.put( Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, fileName );
    // jobVarsMap will also contain any parameters for the job, not just the variables
    // this is a quirk that happens elsewhere too, but breaks the PUC params UI, so clean it up here
    for (String paramName : ( (NamedParams) jobMeta ).listParameters() ) {
      jobVarsMap.remove( paramName );
    }

    return jobVarsMap;
  }

  private Map<String, String> extractVariablesFromStream( InputStream inputStream, String fileName,
                                                          String extension, Repository repo ) throws KettleException {
    if ( "ktr".equalsIgnoreCase( extension ) ) {
      TransMeta transMeta = new TransMeta( inputStream, repo, true, null, null );
      return getTransVars( transMeta, fileName );
    } else if ( "kjb".equalsIgnoreCase( extension ) ) {
      JobMeta jobMeta = new JobMeta( inputStream, repo, null );
      return getJobVars( jobMeta, fileName );
    }
    return new HashMap<>();
  }

  private void filterInternalVariables( Map<String, String> variables ) {
    /*Updating variables to remove based on the HideInternalVariable setting. If no other variables other than
    Internal variables, then it won't show the Internal variable screen.*/
    if ( ValueMetaString.convertStringToBoolean( System.getProperty( Const.HIDE_INTERNAL_VARIABLES,
      Const.HIDE_INTERNAL_VARIABLES_DEFAULT ) ) ) {
      variables.keySet().removeIf( key -> key.contains( "Internal." ) );
    }
  }

  private NamedParams filterUserParameters( NamedParams params ) {

    NamedParams userParams = new NamedParamsDefault();

    if ( !isEmpty( params ) ) {

      for ( String paramName : params.listParameters() ) {

        if ( isUserParameter( paramName ) ) {
          try {
            userParams.addParameterDefinition( paramName, StringUtils.EMPTY, StringUtils.EMPTY );
          } catch ( DuplicateParamException e ) {
            // ignore
          }
        }
      }
    }

    return userParams;
  }

  private NamedParams getMeta( RepositoryFile file ) throws KettleException {
    if ( file != null ) {
      String extension = FilenameUtils.getExtension( file.getName() );
      Repository repo = PDIImportUtil.connectToRepository( null );
      InputStream inputStream = "ktr".equalsIgnoreCase( extension )
        ? convertTransformation( file.getId() )
        : convertJob( file.getId() );
      return createMetaFromStream( inputStream, extension, repo );
    }
    return null;
  }

  private NamedParams getMeta( FileObject fileObject ) throws KettleException {
    if ( fileObject != null ) {
      String extension = FilenameUtils.getExtension( fileObject.getName().getBaseName() );
      Repository repo = PDIImportUtil.connectToRepository( null );
      try {
        return createMetaFromStream( fileObject.getContent().getInputStream(), extension, repo );
      } catch ( FileSystemException e ) {
        throw new KettleException( e );
      }
    }
    return null;
  }

  private NamedParams createMetaFromStream( InputStream inputStream, String extension,
                                            Repository repo ) throws KettleException {
    if ( "ktr".equalsIgnoreCase( extension ) ) {
      return new TransMeta( inputStream, repo, true, null, null );
    } else if ( "kjb".equalsIgnoreCase( extension ) ) {
      return new JobMeta( inputStream, repo, null );
    }
    return null;
  }

  private InputStream convertTransformation( Serializable fileId ) {
    return new StreamToTransNodeConverter( unifiedRepository ).convert( fileId );
  }

  private InputStream convertJob( Serializable fileId ) {
    return new StreamToJobNodeConverter( unifiedRepository ).convert( fileId );
  }

  private boolean isUserParameter( String paramName ) {

    if ( !StringUtils.isEmpty( paramName ) ) {
      // prevent rendering of protected/hidden/system parameters
      if ( paramName.startsWith( IPdiContentProvider.PROTECTED_PARAMETER_PREFIX ) ) {
        return false;
      }
    }
    return true;
  }

  private boolean hasUserParameters( NamedParams params ) {
    return !isEmpty( filterUserParameters( params ) );
  }

  private boolean isEmpty( NamedParams np ) {
    return np == null || np.listParameters() == null || np.listParameters().length == 0;
  }
}
