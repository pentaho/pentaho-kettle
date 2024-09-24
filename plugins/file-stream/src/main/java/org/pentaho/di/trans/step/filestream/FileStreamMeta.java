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

package org.pentaho.di.trans.step.filestream;

import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.metastore.api.IMetaStore;

import java.util.List;



@Step( id = "FileStream", image = "FileStream.svg", name = "File Stream",
  description = "Streams lines from a file as they are added.  aka tail -f", categoryDescription = "Streaming" )
@InjectionSupported( localizationPrefix = "FileStreamMeta.Injection." )
public class FileStreamMeta extends BaseStreamStepMeta implements StepMetaInterface, Cloneable {

  private static Class<?> PKG = FileStream.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  public static final String SOURCE_PATH = "sourcePath";


  @Injection( name = SOURCE_PATH )
  public String sourcePath;

  public FileStreamMeta() {
    super();
    setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  public void setDefault() {
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    super.readRep( rep, metaStore, id_step, databases );
    setSourcePath( rep.getStepAttributeString( id_step, SOURCE_PATH ) );
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId transId, ObjectId stepId )
    throws KettleException {
    super.saveRep( rep, metaStore, transId, stepId );
    rep.saveStepAttribute( transId, stepId, SOURCE_PATH, sourcePath );
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
                                Trans trans ) {
    return new FileStream( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new FileStreamData();
  }

  public String getDialogClassName() {
    return "org.pentaho.di.trans.step.filestream.FileStreamDialog";
  }

  public String getSourcePath() {
    return sourcePath;
  }

  public void setSourcePath( String sourcePath ) {
    this.sourcePath = sourcePath;
  }
}
