/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.base;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.util.CurrentDirectoryResolver;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.jobexecutor.JobExecutorMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import static org.pentaho.di.core.Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY;
import static org.pentaho.di.core.Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY;
import static org.pentaho.di.core.Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME;
import static org.pentaho.di.core.Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY;

/**
 * Unit tests for this class can be found in JobExecutorMetaTest, StepWithMetaMetaTest, JobEntryJobTest, and
 * JobEntryTransTest.  This class attempts to consolidate and refactor to some extent, the logic that was originally
 * present in these four classes and moved here.  This allowed the fileMetaCache to implemented without repeating code
 * over these 4 classes.  Due to the divergence over time of the 4 original classes, there is still some significant
 * differences with meta load logic that was beyond the scope of this case to analyze.
 *
 * @param <T> Either a TransMeta or JobMeta
 */
public class MetaFileLoaderImpl<T> implements IMetaFileLoader<T> {

  JobEntryBase jobEntryBase;

  private ObjectLocationSpecificationMethod specificationMethod;
  private Class<? extends AbstractMeta> persistentClass;  //The class that T represents
  private IMetaFileCache metaFileCache;
  private final String metaName;
  private final String directory;
  private final ObjectId metaObjectId;
  private final String friendlyMetaType; //For error text
  private String filename;
  private boolean useCache = false;

  private BaseStepMeta baseStepMeta;

  public static final String KJB = ".kjb";
  public static final String KTR = ".ktr";

  /**
   * @param jobEntryBase        either a JobEntryTrans or JobEntryJob Object
   * @param specificationMethod
   */
  public MetaFileLoaderImpl( JobEntryBase jobEntryBase, ObjectLocationSpecificationMethod specificationMethod ) {
    //Constructor adapting supported JobEntries
    if ( jobEntryBase instanceof JobEntryJob || jobEntryBase instanceof JobEntryTrans ) {
      this.jobEntryBase = jobEntryBase;
      this.specificationMethod = specificationMethod;
      this.filename = jobEntryBase.getFilename();
      //collect fields from the super classes
      if ( jobEntryBase instanceof JobEntryTrans ) {
        JobEntryTrans jobEntryTrans = (JobEntryTrans) jobEntryBase;
        this.metaName = jobEntryTrans.getTransname();
        this.directory = jobEntryTrans.getDirectory();
        this.metaObjectId = jobEntryTrans.getTransObjectId();
        this.friendlyMetaType = "Transformation";
        this.persistentClass = TransMeta.class;
      } else {
        JobEntryJob jobEntryJob = (JobEntryJob) jobEntryBase;
        this.metaName = jobEntryJob.getJobName();
        this.directory = jobEntryJob.getDirectory();
        this.metaObjectId = jobEntryJob.getJobObjectId();
        this.friendlyMetaType = "Job";
        this.persistentClass = JobMeta.class;
      }
    } else {
      throw new IllegalArgumentException( "JobEntryBase must be a JobEntryTrans or JobEntryJob object" );
    }
    useCache = "Y".equalsIgnoreCase( System.getProperty( Const.KETTLE_USE_META_FILE_CACHE, Const.KETTLE_USE_META_FILE_CACHE_DEFAULT ) );
  }

  /**
   * @param baseStepMeta        either a StepWithMappingMeta or JobExecutorMeta Object (BaseStepMeta is a shared
   *                            subclass )
   * @param specificationMethod
   */
  public MetaFileLoaderImpl( BaseStepMeta baseStepMeta, ObjectLocationSpecificationMethod specificationMethod ) {
    //Constructor adapting supported Steps
    if ( baseStepMeta instanceof StepWithMappingMeta || baseStepMeta instanceof JobExecutorMeta ) {
      this.baseStepMeta = baseStepMeta;
      this.specificationMethod = specificationMethod;
      //collect fields from the super classes
      if ( baseStepMeta instanceof StepWithMappingMeta ) {
        StepWithMappingMeta stepWithMappingMeta = (StepWithMappingMeta) baseStepMeta;
        this.metaName = stepWithMappingMeta.getTransName();
        this.directory = stepWithMappingMeta.getDirectoryPath();
        this.metaObjectId = stepWithMappingMeta.getTransObjectId();
        this.friendlyMetaType = "Transformation";
        this.persistentClass = TransMeta.class;
        //added for steps
        this.filename = stepWithMappingMeta.getFileName();
      } else {
        JobExecutorMeta jobExecutorMeta = (JobExecutorMeta) baseStepMeta;
        this.metaName = jobExecutorMeta.getJobName();
        this.directory = jobExecutorMeta.getDirectoryPath();
        this.metaObjectId = jobExecutorMeta.getJobObjectId();
        this.friendlyMetaType = "Job";
        this.persistentClass = JobMeta.class;
        //added for steps
        this.filename = jobExecutorMeta.getFileName();
      }
    } else {
      throw new IllegalArgumentException( "JobEntryBase must be a JobEntryTrans or JobEntryJob object" );
    }
    useCache = "Y".equalsIgnoreCase( System.getProperty( Const.KETTLE_USE_META_FILE_CACHE, Const.KETTLE_USE_META_FILE_CACHE_DEFAULT ) );
  }

  public T getMetaForEntry( Repository rep, IMetaStore metaStore, VariableSpace space ) throws KettleException {
    try {
      T theMeta = null;
      if ( jobEntryBase.getParentJob() != null ) {
        metaFileCache = jobEntryBase.getParentJobMeta().getMetaFileCache(); //Get the cache from the parent or create it
      }
      CurrentDirectoryResolver r = new CurrentDirectoryResolver();
      VariableSpace tmpSpace = r.resolveCurrentDirectory(
        specificationMethod, space, rep, jobEntryBase.getParentJob(), filename );

      final String[] idContainer = new String[ 1 ]; //unigue portion of cache key passed though argument
      switch ( specificationMethod ) {
        case FILENAME:
          String realFilename = tmpSpace.environmentSubstitute( filename );
          try {
            theMeta = attemptLoadMeta( realFilename, rep, metaStore, tmpSpace, null, idContainer );
          } catch ( KettleException e ) {
            // try to load from repository, this trans may have been developed locally and later uploaded to the
            // repository
            if ( rep == null ) {
              theMeta = isTransMeta()
                      ? (T) new TransMeta( realFilename, metaStore, null, true, jobEntryBase.getParentVariableSpace(), null )
                      : (T) new JobMeta( jobEntryBase.getParentVariableSpace(), realFilename, rep, metaStore, null );
            } else {
              theMeta = getMetaFromRepository( rep, r, realFilename, tmpSpace );
            }
            if ( theMeta != null ) {
              idContainer[ 0 ] = realFilename;
            }
          }
          break;
        case REPOSITORY_BY_NAME:
          String realDirectory = tmpSpace.environmentSubstitute( directory != null ? directory : "" );
          String realName = tmpSpace.environmentSubstitute( metaName );

          String metaPath = StringUtil.trimEnd( realDirectory, '/' ) + RepositoryFile.SEPARATOR + StringUtil
            .trimStart( realName, '/' );

          if ( metaPath.startsWith( "file://" ) || metaPath.startsWith( "zip:file://" ) || metaPath.startsWith(
            "hdfs://" ) ) {
            String extension = isTransMeta() ? RepositoryObjectType.TRANSFORMATION.getExtension()
              : RepositoryObjectType.JOB.getExtension();
            if ( !metaPath.endsWith( extension ) ) {
              metaPath = metaPath + extension;
            }
            theMeta = attemptCacheRead( metaPath ); //try to get from the cache first
            if ( theMeta == null ) {
              if ( isTransMeta() ) {
                theMeta =
                  (T) new TransMeta( metaPath, metaStore, null, true, jobEntryBase.getParentVariableSpace(), null );
              } else {
                theMeta = (T) new JobMeta( tmpSpace, metaPath, rep, metaStore, null );
              }
              idContainer[ 0 ] = metaPath;
            }
          } else {
            theMeta = attemptCacheRead( metaPath ); //try to get from the cache first
            if ( theMeta == null ) {
              if ( isTransMeta() ) {
                theMeta = rep == null
                  ? (T) new TransMeta( metaPath, metaStore, null, true, jobEntryBase.getParentVariableSpace(), null )
                  : getMetaFromRepository( rep, r, metaPath, tmpSpace );
              } else {
                theMeta = getMetaFromRepository( rep, r, metaPath, tmpSpace );
              }
              if ( theMeta != null ) {
                idContainer[ 0 ] = metaPath;
              }
            }
          }
          break;
        case REPOSITORY_BY_REFERENCE:
          if ( metaObjectId == null ) {
            if ( isTransMeta() ) {
              throw new KettleException( BaseMessages.getString( persistentClass,
                "JobTrans.Exception.ReferencedTransformationIdIsNull" ) );
            } else {
              throw new KettleException( BaseMessages.getString( persistentClass,
                "JobJob.Exception.ReferencedTransformationIdIsNull" ) );
            }
          }

          if ( rep != null ) {
            theMeta = attemptCacheRead( metaObjectId.toString() ); //try to get from the cache first
            if ( theMeta == null ) {
              // Load the last revision
              if ( isTransMeta() ) {
                theMeta = (T) rep.loadTransformation( metaObjectId, null );
              } else {
                theMeta = (T) rep.loadJob( metaObjectId, null );
              }
              idContainer[ 0 ] = metaObjectId.toString();
            }
          } else {
            throw new KettleException(
              "Could not execute " + friendlyMetaType + " specified in a repository since we're not connected to one" );
          }
          break;
        default:
          throw new KettleException( "The specified object location specification method '"
            + specificationMethod + "' is not yet supported in this " + friendlyMetaType + " entry." );
      }

      cacheMeta( idContainer[ 0 ], theMeta );
      return theMeta;
    } catch ( final KettleException ke ) {
      // if we get a KettleException, simply re-throw it
      throw ke;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( persistentClass, "JobTrans.Exception.MetaDataLoad" ), e );
    }
  }

  private void cacheMeta( String cacheKey, T theMeta ) {
    if ( useCache && theMeta != null && metaFileCache != null ) {
      if ( isTransMeta() ) {
        TransMeta transMeta = (TransMeta) theMeta;
        transMeta.setMetaFileCache( metaFileCache );
        if ( cacheKey != null ) {
          metaFileCache.cacheMeta( metaFileCache.getKey( specificationMethod, cacheKey.endsWith( KTR ) ? cacheKey : cacheKey + KTR ), transMeta );
        }
      } else {
        JobMeta jobMeta = (JobMeta) theMeta;
        jobMeta.setMetaFileCache( metaFileCache );
        if ( cacheKey != null ) {
          metaFileCache.cacheMeta( metaFileCache.getKey( specificationMethod, cacheKey.endsWith( KJB ) ? cacheKey : cacheKey + KJB ), jobMeta );
        }
      }
    }
  }

  private T attemptLoadMeta( String realFilename, Repository rep, IMetaStore metaStore, VariableSpace jobSpace,
                             VariableSpace transSpace, String[] idContainer )
    throws KettleException {
    T theMeta = null;
    //try to get from the cache first
    theMeta = attemptCacheRead( realFilename );

    if ( theMeta == null ) {
      theMeta = isTransMeta()
        ? (T) new TransMeta( realFilename, metaStore, null, true, transSpace, null )
        : (T) new JobMeta( jobSpace, realFilename, rep, metaStore, null );
      idContainer[ 0 ] = realFilename;  //only pass back the id used in the cache, if a cache entry should be created
    }
    return theMeta;
  }

  private T getMetaFromRepository( Repository rep, CurrentDirectoryResolver r, String metaPath, VariableSpace tmpSpace )
    throws KettleException {
    String realName = "";
    String realDirectory = "/";

    if ( StringUtils.isBlank( metaPath ) ) {
      if ( isTransMeta() ) {
        throw new KettleException(
          BaseMessages.getString( persistentClass, "JobTrans.Exception.MissingTransFileName" ) );
      } else {
        throw new KettleException(
          BaseMessages.getString( persistentClass, "JobJob.Exception.MissingJobFileName" ) );
      }
    }

    int index = metaPath.lastIndexOf( RepositoryFile.SEPARATOR );
    if ( index != -1 ) {
      realName = metaPath.substring( index + 1 );
      realDirectory = index == 0 ? RepositoryFile.SEPARATOR : metaPath.substring( 0, index );
    }
    realDirectory = r.normalizeSlashes( realDirectory );

    RepositoryDirectoryInterface repositoryDirectory = rep.loadRepositoryDirectoryTree().findDirectory( realDirectory );
    if ( repositoryDirectory == null ) {
      throw new KettleException( "Unable to find repository directory [" + Const.NVL( realDirectory, "" ) + "]" );
    }

    T theMeta = null;
    if ( isTransMeta() ) {
      theMeta = (T) rep.loadTransformation( realName, repositoryDirectory, null, true, null );
    } else {
      JobMeta jobMeta = rep.loadJob( realName, repositoryDirectory, null, null );
      if ( jobMeta != null ) {
        jobMeta.initializeVariablesFrom( tmpSpace );
      }
      theMeta = (T) jobMeta;
    }
    return theMeta;
  }

  private T attemptCacheRead( String realFilename ) {
    if ( !useCache || metaFileCache == null ) {
      return null;
    }
    return isTransMeta()
      ? (T) metaFileCache.getCachedTransMeta( metaFileCache.getKey( specificationMethod, realFilename.endsWith( KTR ) ? realFilename : realFilename + KTR ) )
      : (T) metaFileCache.getCachedJobMeta( metaFileCache.getKey( specificationMethod, realFilename.endsWith( KJB ) ? realFilename : realFilename + KJB ) );
  }

  private boolean isTransMeta() {
    return TransMeta.class.isAssignableFrom( persistentClass );
  }

  @Override
  public T getMetaForStep( Repository rep, IMetaStore metaStore, VariableSpace space )
    throws KettleException {
    // Note - was a synchronized static method, but as no static variables are manipulated, this is entirely unnecessary

    // baseStepMeta.getParentStepMeta() or getParantTransMeta() is only null when running unit tests
    metaFileCache =
      baseStepMeta.getParentStepMeta() == null || baseStepMeta.getParentStepMeta().getParentTransMeta() == null
        ? null : baseStepMeta.getParentStepMeta().getParentTransMeta().getMetaFileCache();
    T theMeta = null;
    CurrentDirectoryResolver r = new CurrentDirectoryResolver();

    VariableSpace tmpSpace;
    if ( isTransMeta() ) {
      // send restricted parentVariables with several important options
      // Otherwise we destroy child variables and the option "Inherit all variables from the transformation" is enabled
      // always.
      tmpSpace = r.resolveCurrentDirectory( specificationMethod, getVarSpaceOnlyWithRequiredParentVars( space ),
        rep, baseStepMeta.getParentStepMeta(), filename );
    } else {
      tmpSpace =
        r.resolveCurrentDirectory( specificationMethod, space, rep, baseStepMeta.getParentStepMeta(), filename );
    }
    final String[] idContainer = new String[ 1 ]; //unigue portion of cache key passed though argument

    switch ( specificationMethod ) {
      case FILENAME:
        String realFilename = tmpSpace.environmentSubstitute( filename );
        if ( isTransMeta() && space != null ) {
          // This is a parent transformation and parent variable should work here. A child file name can be resolved
          // via parent space.
          realFilename = space.environmentSubstitute( realFilename );
        }
        theMeta = attemptCacheRead( realFilename ); //try to get from the cache first
        if ( theMeta == null ) {
          try {
            // OK, load the meta-data from file...
            // Don't set internal variables: they belong to the parent thread!
            if ( rep != null ) {
              theMeta = getMetaFromRepository2( realFilename, rep, r, idContainer );
            }
            if ( theMeta == null ) {
              theMeta = attemptLoadMeta( realFilename, rep, metaStore, null, tmpSpace, idContainer );
              LogChannel.GENERAL.logDetailed( "Loading " + friendlyMetaType + " from repository",
                friendlyMetaType + " was loaded from XML file [" + realFilename + "]" );
            }
          } catch ( Exception e ) {
            if ( isTransMeta() ) {
              throw new KettleException(
                BaseMessages.getString( persistentClass, "StepWithMappingMeta.Exception.UnableToLoadTrans" ), e );
            } else {
              throw new KettleException(
                BaseMessages.getString( persistentClass, "JobExecutorMeta.Exception.UnableToLoadJob" ), e );
            }
          }
        }
        break;

      case REPOSITORY_BY_NAME:
        String realMetaName = tmpSpace.environmentSubstitute( Const.NVL( metaName, "" ) );
        String realDirectory = tmpSpace.environmentSubstitute( Const.NVL( directory, "" ) );

        if ( isTransMeta() && space != null ) {
          // This is a parent transformation and parent variable should work here. A child file name can be
          // resolved via
          // parent space.
          realMetaName = space.environmentSubstitute( realMetaName );
          realDirectory = space.environmentSubstitute( realDirectory );
        }

        if ( Utils.isEmpty( realDirectory ) && !Utils.isEmpty( realMetaName ) ) {
          int index = realMetaName.lastIndexOf( '/' );
          String transPath = realMetaName;
          realMetaName = realMetaName.substring( index + 1 );
          realDirectory = transPath.substring( 0, index );
        }

        //We will use this key in cache no matter what the final successful path is so that we don't need to hit the
        //  repo the next time it comes in. (ie: rep.findDirectory )
        String cacheKey = realDirectory + "/" + realMetaName;
        theMeta = attemptCacheRead( cacheKey ); //try to get from the cache first
        if ( theMeta == null ) {
          if ( rep != null ) {
            if ( !Utils.isEmpty( realMetaName ) && !Utils.isEmpty( realDirectory ) ) {

              realDirectory = r.normalizeSlashes( realDirectory );
              RepositoryDirectoryInterface repdir = rep.findDirectory( realDirectory );
              if ( repdir != null ) {
                try {
                  // reads the last revision in the repository...
                  theMeta = isTransMeta() ? (T) rep.loadTransformation( realMetaName, repdir, null, true, null )
                    : (T) rep.loadJob( realMetaName, repdir, null, null );
                  if ( theMeta != null ) {
                    idContainer[ 0 ] = cacheKey;
                  }
                  LogChannel.GENERAL.logDetailed( "Loading " + friendlyMetaType + " from repository",
                    "Executor " + friendlyMetaType + " [" + realMetaName + "] was loaded from the repository" );
                } catch ( Exception e ) {
                  throw new KettleException( "Unable to load " + friendlyMetaType + " [" + realMetaName + "]", e );
                }
              }
            }
          } else {
            // rep is null, let's try loading by filename
            try {
              theMeta = attemptLoadMeta( cacheKey, rep, metaStore, null, tmpSpace, idContainer );
            } catch ( KettleException ke ) {
              try {
                // add .ktr extension and try again
                String extension = isTransMeta() ? Const.STRING_TRANS_DEFAULT_EXT : Const.STRING_JOB_DEFAULT_EXT;
                theMeta =
                  attemptLoadMeta( cacheKey + "." + extension, rep, metaStore, null,
                    tmpSpace, idContainer );
                if ( idContainer[ 0 ] != null ) {
                  //It successfully read in the meta but we don't want to cache it with the extension so we override
                  // it here
                  idContainer[ 0 ] = cacheKey;
                }
              } catch ( KettleException ke2 ) {
                if ( isTransMeta() ) {
                  throw new KettleException(
                    BaseMessages.getString( persistentClass, "StepWithMappingMeta.Exception.UnableToLoadTrans",
                      realMetaName ) + realDirectory );
                } else {
                  throw new KettleException(
                    BaseMessages.getString( persistentClass, "JobExecutorMeta.Exception.UnableToLoadJob",
                      realMetaName ) + realDirectory );
                }
              }
            }
          }
        }
        break;

      case REPOSITORY_BY_REFERENCE:
        // Read the last revision by reference...
        theMeta = attemptCacheRead( metaObjectId.toString() );
        if ( theMeta == null ) {
          theMeta = isTransMeta() ? (T) rep.loadTransformation( metaObjectId, null )
            : (T) rep.loadJob( metaObjectId, null );
          if ( theMeta != null ) {
            idContainer[ 0 ] = metaObjectId.toString(); //Only set when not found in cache
          }
        }
        break;
      default:
        break;
    }

    //If theMeta is present and idContainer[0] != null, ( meaning it read it from repo/file ), then cache it
    cacheMeta( idContainer[ 0 ], theMeta );
    return theMeta;
  }

  /**
   * @return new var space with follow vars from parent space or just new space if parent was null
   * <p>
   * {@link org.pentaho.di.core.Const#INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY} {@link
   * org.pentaho.di.core.Const#INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY}
   * {@link org.pentaho.di.core.Const#INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY}
   * {@link org.pentaho.di.core.Const#INTERNAL_VARIABLE_JOB_FILENAME_NAME}
   */
  private static VariableSpace getVarSpaceOnlyWithRequiredParentVars( VariableSpace parentSpace ) {
    Variables tmpSpace = new Variables();
    if ( parentSpace != null ) {
      tmpSpace.setVariable( INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY,
        parentSpace.getVariable( INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
      tmpSpace.setVariable( INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY,
        parentSpace.getVariable( INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY ) );
      tmpSpace.setVariable( INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY,
        parentSpace.getVariable( INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY ) );
      tmpSpace.setVariable( INTERNAL_VARIABLE_JOB_FILENAME_NAME,
        parentSpace.getVariable( INTERNAL_VARIABLE_JOB_FILENAME_NAME ) );
    }
    return tmpSpace;
  }

  private T getMetaFromRepository2( String realFilename, Repository rep, CurrentDirectoryResolver r,
                                    String[] idContainer ) throws KettleException {
    T theMeta = null;

    // need to try to load from the repository
    String normalizedFilename = r.normalizeSlashes( realFilename );
    try {
      String dirStr = normalizedFilename.substring( 0, normalizedFilename.lastIndexOf( "/" ) );
      String tmpFilename = normalizedFilename.substring( normalizedFilename.lastIndexOf( "/" ) + 1 );
      RepositoryDirectoryInterface dir = rep.findDirectory( dirStr );
      theMeta = isTransMeta() ? (T) rep.loadTransformation( tmpFilename, dir, null, true, null )
        : (T) rep.loadJob( tmpFilename, dir, null, null );
    } catch ( KettleException ke ) {
      // try without extension
      String extension = isTransMeta() ? Const.STRING_TRANS_DEFAULT_EXT : Const.STRING_JOB_DEFAULT_EXT;
      if ( normalizedFilename.endsWith( extension ) ) {
        try {
          String tmpFilename = normalizedFilename.substring( realFilename.lastIndexOf( "/" ) + 1,
            normalizedFilename.indexOf( "." + extension ) );
          String dirStr = normalizedFilename.substring( 0, normalizedFilename.lastIndexOf( "/" ) );
          RepositoryDirectoryInterface dir = rep.findDirectory( dirStr );
          theMeta = isTransMeta() ? (T) rep.loadTransformation( tmpFilename, dir, null, true, null )
            : (T) rep.loadJob( tmpFilename, dir, null, null );
        } catch ( KettleException ke2 ) {
          // fall back to try loading from file system (transMeta is going to be null)
        }
      }
    }
    if ( theMeta != null ) {
      idContainer[ 0 ] = realFilename;  //remember the key to cache
    }
    return theMeta;
  }
}
