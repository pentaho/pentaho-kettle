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


package org.pentaho.di.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.step.StepMeta;

/**
 * This class resolve and update system variables
 * {@link org.pentaho.di.core.Const#INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY}
 * {@link org.pentaho.di.core.Const#INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY}
 * {@link org.pentaho.di.core.Const#INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY}
 * {@link org.pentaho.di.core.Const#INTERNAL_VARIABLE_JOB_FILENAME_NAME}
 *
 */
public class CurrentDirectoryResolver {

  public CurrentDirectoryResolver() {
  }

  /**
   * The logic of this method:
   * 
   * if we have directory we return the child var space with directory used as system property
   * if we have not directory we return the child var space with directory extracted from filanme
   * if we don not have directory and filename we will return the child var space without updates
   * 
   * 
   * @param parentVariables - parent variable space which can be inherited
   * @param directory - current path which will be used as path for start trans/job
   * @param filename - is file which we use at this moment
   * @param inheritParentVar - flag which indicate should we inherit variables from parent var space to child var space
   * @return new var space if inherit was set false or child var space with updated system variables
   * @deprecated use the version with the Bowl
   */
  @Deprecated
  public VariableSpace resolveCurrentDirectory( VariableSpace parentVariables, RepositoryDirectoryInterface directory,
                                                String filename ) {
    return resolveCurrentDirectory( DefaultBowl.getInstance(), parentVariables, directory, filename );
  }

  /**
   * The logic of this method:
   *
   * if we have directory we return the child var space with directory used as system property
   * if we have not directory we return the child var space with directory extracted from filanme
   * if we don not have directory and filename we will return the child var space without updates
   *
   *
   * @param bowl - context for the operation
   * @param parentVariables - parent variable space which can be inherited
   * @param directory - current path which will be used as path for start trans/job
   * @param filename - is file which we use at this moment
   * @param inheritParentVar - flag which indicate should we inherit variables from parent var space to child var space
   * @return new var space if inherit was set false or child var space with updated system variables
   */
  public VariableSpace resolveCurrentDirectory( Bowl bowl, VariableSpace parentVariables,
                                                RepositoryDirectoryInterface directory, String filename ) {
    Variables tmpSpace = new Variables();
    tmpSpace.setParentVariableSpace( parentVariables );
    tmpSpace.initializeVariablesFrom( parentVariables );

    if ( directory != null ) {
      tmpSpace.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, directory.toString() );
      tmpSpace.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, directory.toString() );
      tmpSpace.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, directory.toString() );
    } else if ( filename != null ) {
      try {
        FileObject fileObject = KettleVFS.getInstance( bowl ).getFileObject( filename, tmpSpace );

        if ( !fileObject.exists() ) {
          // don't set variables if the file doesn't exist
          return tmpSpace;
        }

        FileName fileName = fileObject.getName();

        // The filename of the transformation
        tmpSpace.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, fileName.getBaseName() );

        // The directory of the transformation
        FileName fileDir = fileName.getParent();
        tmpSpace.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, fileDir.getURI() );
        tmpSpace.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, fileDir.getURI() );
        tmpSpace.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, fileDir.getURI() );
      } catch ( Exception e ) {
      }
    }
    return tmpSpace;
  }

  @Deprecated
  public VariableSpace resolveCurrentDirectory( ObjectLocationSpecificationMethod specificationMethod,
      VariableSpace parentVariables, Repository repository, StepMeta stepMeta, String filename ) {
    return resolveCurrentDirectory( DefaultBowl.getInstance(), specificationMethod, parentVariables, repository,
                                    stepMeta, filename );
  }

  public VariableSpace resolveCurrentDirectory( Bowl bowl, ObjectLocationSpecificationMethod specificationMethod,
      VariableSpace parentVariables, Repository repository, StepMeta stepMeta, String filename ) {
    RepositoryDirectoryInterface directory = null;
    if ( repository != null && specificationMethod.equals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME )
        && stepMeta != null && stepMeta.getParentTransMeta() != null
        && stepMeta.getParentTransMeta().getRepositoryDirectory() != null ) {
      directory = stepMeta.getParentTransMeta().getRepositoryDirectory();
    } else if ( repository == null && stepMeta != null && stepMeta.getParentTransMeta() != null ) {
      filename = stepMeta.getParentTransMeta().getFilename();
    } else if ( stepMeta != null && stepMeta.getParentTransMeta() != null && repository != null
        && specificationMethod.equals( ObjectLocationSpecificationMethod.FILENAME ) ) {
      // we're using FILENAME but we are connected to a repository
      directory = stepMeta.getParentTransMeta().getRepositoryDirectory();
    } else if ( stepMeta != null && stepMeta.getParentTransMeta() != null && filename == null ) {
      filename = stepMeta.getParentTransMeta().getFilename();
    }
    // If directory is null or root directory, fall back to using filename
    // For vfs, though connected to repository, we have no directory info - fall back to filename
    if ( directory == null || "/".equals( directory.toString() ) ) {
      directory = null;
      if ( stepMeta != null && stepMeta.getParentTransMeta() != null ) {
        filename = stepMeta.getParentTransMeta().getFilename();
      }
    }
    return resolveCurrentDirectory( bowl, parentVariables, directory, filename );
  }

  @Deprecated
  public VariableSpace resolveCurrentDirectory( ObjectLocationSpecificationMethod specificationMethod,
      VariableSpace parentVariables, Repository repository, Job job, String filename ) {
    return resolveCurrentDirectory( DefaultBowl.getInstance(), specificationMethod, parentVariables, repository, job,
                                    filename );
  }

  public VariableSpace resolveCurrentDirectory( Bowl bowl, ObjectLocationSpecificationMethod specificationMethod,
      VariableSpace parentVariables, Repository repository, Job job, String filename ) {
    RepositoryDirectoryInterface directory = null;
    if ( repository != null && specificationMethod.equals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME )
        && job != null && job.getJobMeta() != null && job.getJobMeta().getRepositoryDirectory() != null ) {
      directory = job.getJobMeta().getRepositoryDirectory();
    } else if ( job != null && repository == null
        && specificationMethod.equals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME ) ) {
      filename = job.getFilename();
    } else if ( job != null && job.getJobMeta() != null && repository != null
        && specificationMethod.equals( ObjectLocationSpecificationMethod.FILENAME ) ) {
      // we're using FILENAME but we are connected to a repository
      directory = job.getJobMeta().getRepositoryDirectory();
    } else if ( job != null && filename == null ) {
      filename = job.getFilename();
    } else if ( repository != null && JobMeta.class.isAssignableFrom( parentVariables.getClass() ) ) {
      // fallback protection for design mode: the parentVariables may actually be a JobMeta which
      // may have the required directory
      JobMeta realParent = null;
      realParent = (JobMeta) parentVariables;
      if ( realParent != null && realParent.getRepositoryDirectory() != null ) {
        directory = realParent.getRepositoryDirectory();
      }
    } else if ( JobMeta.class.isAssignableFrom( parentVariables.getClass() ) ) {
      // additional fallback protection for design mode
      JobMeta realParent = null;
      realParent = (JobMeta) parentVariables;
      filename = realParent.getFilename();
    }
    // If directory is null or root directory, fall back to using filename
    // For vfs, though connected to repository, we have no directory info - fall back to filename
    if ( directory == null || "/".equals( directory.toString() ) ) {
      directory = null;
      if ( job != null && job.getJobMeta() != null ) {
        filename = job.getJobMeta().getFilename();
      }
    }
    return resolveCurrentDirectory( bowl, parentVariables, directory, filename );
  }

  public String normalizeSlashes( String str ) {
    if ( StringUtils.isBlank( str ) ) {
      return str;
    }
    while ( str.contains( "\\" ) ) {
      str = str.replace( "\\", "/" );
    }
    while ( str.contains( "//" ) ) {
      str = str.replace( "//", "/" );
    }
    return str;
  }

}
