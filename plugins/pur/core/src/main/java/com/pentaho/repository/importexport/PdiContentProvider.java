/*!
 * Copyright 2010 - 2023 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.pentaho.repository.importexport;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.util.IPdiContentProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;

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
          np = filterUserParameters( np );
          if ( !isEmpty( np ) ) {
            for( String s : np.listParameters() ) {
              if ( null == np.getParameterValue( s ) || np.getParameterValue( s ).equalsIgnoreCase( "" ) ) {
                userParams.put( s, np.getParameterDefault( s ) );
              } else {
                userParams.put( s, np.getParameterValue( s ) );
              }
            }
          }
        } catch ( KettleException e ) {
          log.error( e );
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

          if ( "ktr".equalsIgnoreCase( extension ) ) {
            TransMeta transMeta = new TransMeta( convertTransformation( file.getId() ), repo, true, null, null );
            userVariables = getTransVars( transMeta, file );
          } else if ( "kjb".equalsIgnoreCase( extension ) ) {
            JobMeta jobMeta = new JobMeta( convertJob( file.getId() ), repo, null );
            userVariables = getJobVars( jobMeta, file );
          }
        } catch ( KettleException e ) {
          log.error( e );
        }
      }
    }
    return userVariables;
  }

  private Map<String, String> getTransVars( TransMeta transMeta, RepositoryFile file ) {
    /*
     * The following code was more or less copy/pasted from SpoonTransformationDelegate and
     * TransExecutionConfigurationDialog
     */
    TransExecutionConfiguration transExeConf = new TransExecutionConfiguration();
    Map<String, String> variableMap = new HashMap<>();
    variableMap.putAll( transExeConf.getVariables() ); // the default
    variableMap.put( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, file.getName() );

    transExeConf.setVariables( variableMap );
    transExeConf.getUsedVariables( transMeta );

    return transExeConf.getVariables();
  }

  private Map<String, String> getJobVars( JobMeta jobMeta, RepositoryFile file ) {
    /*
     * The following code was more or less copy/pasted from SpoonJobDelegate and JobExecutionConfigurationDialog
     */
    JobExecutionConfiguration jobExeConf = new JobExecutionConfiguration();
    Map<String, String> variableMap = new HashMap<>();

    jobExeConf.setVariables( variableMap );
    jobExeConf.getUsedVariables( jobMeta );
    Map<String, String> jobVarsMap = jobExeConf.getVariables();
    jobVarsMap.put( Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, file.getName() );
    // jobVarsMap will also contain any parameters for the job, not just the variables
    // this is a quirk that happens elsewhere too, but breaks the PUC params UI, so clean it up here
    for (String paramName : ( (NamedParams) jobMeta ).listParameters() ) {
      jobVarsMap.remove( paramName );
    }

    return jobVarsMap;
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

    NamedParams meta = null;

    if ( file != null ) {

      String extension = FilenameUtils.getExtension( file.getName() );
      Repository repo = PDIImportUtil.connectToRepository( null );

      if ( "ktr".equalsIgnoreCase( extension ) ) {

        meta = new TransMeta( convertTransformation( file.getId() ), repo, true, null, null );

      } else if ( "kjb".equalsIgnoreCase( extension ) ) {

        meta = new JobMeta( convertJob( file.getId() ), repo, null );

      }
    }

    return meta;
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
