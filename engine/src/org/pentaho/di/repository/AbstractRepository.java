/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.repository;

import java.util.List;

import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.namedconfig.model.NamedConfiguration;

/**
 * Implementing convenience methods that can be described in terms of other methods in the interface
 */
public abstract class AbstractRepository implements Repository {

  @Override
  public long getJobEntryAttributeInteger( ObjectId id_jobentry, String code ) throws KettleException {
    return getJobEntryAttributeInteger( id_jobentry, 0, code );
  }

  @Override
  public String getJobEntryAttributeString( ObjectId id_jobentry, String code ) throws KettleException {
    return getJobEntryAttributeString( id_jobentry, 0, code );
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, String code ) throws KettleException {
    return getJobEntryAttributeBoolean( id_jobentry, 0, code );
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, String code, boolean def ) throws KettleException {
    return getJobEntryAttributeBoolean( id_jobentry, 0, code, def );
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, int nr, String code ) throws KettleException {
    return getJobEntryAttributeBoolean( id_jobentry, nr, code, false );
  }

  public abstract boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, int nr, String code, boolean def ) throws KettleException;

  @Override
  public boolean getStepAttributeBoolean( ObjectId id_step, String code ) throws KettleException {
    return getStepAttributeBoolean( id_step, 0, code );
  }

  @Override
  public boolean getStepAttributeBoolean( ObjectId id_step, int nr, String code ) throws KettleException {
    return getStepAttributeBoolean( id_step, nr, code, false );
  }

  @Override
  public long getStepAttributeInteger( ObjectId id_step, String code ) throws KettleException {
    return getStepAttributeInteger( id_step, 0, code );
  }

  @Override
  public String getStepAttributeString( ObjectId id_step, String code ) throws KettleException {
    return getStepAttributeString( id_step, 0, code );
  }

  @Override
  public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, boolean value ) throws KettleException {
    saveStepAttribute( id_transformation, id_step, 0, code, value );
  }

  @Override
  public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, double value ) throws KettleException {
    saveStepAttribute( id_transformation, id_step, 0, code, value );
  }

  @Override
  public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, long value ) throws KettleException {
    saveStepAttribute( id_transformation, id_step, 0, code, value );
  }

  @Override
  public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, String value ) throws KettleException {
    saveStepAttribute( id_transformation, id_step, 0, code, value );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String code, boolean value ) throws KettleException {
    saveJobEntryAttribute( id_job, id_jobentry, 0, code, value );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String code, long value ) throws KettleException {
    saveJobEntryAttribute( id_job, id_jobentry, 0, code, value );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String code, String value ) throws KettleException {
    saveJobEntryAttribute( id_job, id_jobentry, 0, code, value );
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute( ObjectId id_jobentry, String nameCode, String idCode,
    List<DatabaseMeta> databases ) throws KettleException {
    return loadDatabaseMetaFromJobEntryAttribute( id_jobentry, nameCode, 0, idCode, databases );
  }

  @Override
  public void save( RepositoryElementInterface repoElement, String versionComment, ProgressMonitorListener monitor ) throws KettleException {
    save( repoElement, versionComment, monitor, false );
  }

  @Override
  public void saveDatabaseMetaJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String nameCode,
    String idCode, DatabaseMeta database ) throws KettleException {
    saveDatabaseMetaJobEntryAttribute( id_job, id_jobentry, 0, nameCode, idCode, database );
  }
  
  /////////////////////////////////////////////////////////////////
  ////   NamedConfiguration API
  /////////////////////////////////////////////////////////////////
  @Override
  public void deleteNamedConfiguration (String configurationName )
      throws KettleException {
  }
  
  @Override
  public ObjectId getNamedConfigurationID( String name ) throws KettleException {
    return null;
  }
  
  @Override
  public ObjectId[] getNamedConfigurationIDs( boolean includeDeleted ) throws KettleException {
    return null;
  }
  
  @Override
  public String[] getNamedConfigurationNames( boolean includeDeleted ) throws KettleException {
    return null;
  }

  @Override
  public NamedConfiguration loadNamedConfiguration( ObjectId id_namedconfiguration, String revision )
      throws KettleException {
    return null;
  }
  
  @Override
  public List<NamedConfiguration> readNamedConfigurations() throws KettleException {
    return null;
  }
  
  @Override
  public String[] getJobsUsingNamedConfiguration(ObjectId id_namedconfiguration) throws KettleException {
    return null;
  }

  @Override
  public String[] getTransformationsUsingNamedConfiguration(ObjectId id_namedconfiguration) throws KettleException {
    return null;
  }

  @Override
  public void insertJobEntryNamedConfiguration(ObjectId id_job, ObjectId id_jobentry, ObjectId id_namedconfiguration)
      throws KettleException {
  }

  @Override
  public void insertStepNamedConfiguration(ObjectId id_transformation, ObjectId id_step, ObjectId id_namedconfiguration)
      throws KettleException {
  }

  @Override
  public NamedConfiguration loadNamedConfigurationFromJobEntryAttribute(ObjectId id_jobentry, String nameCode, int nr,
      String idCode, List<NamedConfiguration> namedConfigurations) throws KettleException {
    return null;
  }

  @Override
  public NamedConfiguration loadNamedConfigurationFromJobEntryAttribute(ObjectId id_jobentry, String nameCode,
      String idCode, List<NamedConfiguration> namedConfigurations) throws KettleException {
    return null;
  }

  @Override
  public NamedConfiguration loadNamedConfigurationFromStepAttribute(ObjectId id_step, String code,
      List<NamedConfiguration> namedConfigurations) throws KettleException {
    return null;
  }
  
  @Override
  public void saveNamedConfigurationJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String nameCode,
      String idCode, NamedConfiguration namedConfiguration) throws KettleException {
  }

  @Override
  public void saveNamedConfigurationsJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, int nr, String nameCode,
      String idCode, NamedConfiguration namedConfiguration) throws KettleException {
  }

  @Override
  public void saveNamedConfigurationStepAttribute(ObjectId id_namedconfiguration, ObjectId id_step, String code,
      NamedConfiguration namedConfiguration) throws KettleException {
  }
  
}
