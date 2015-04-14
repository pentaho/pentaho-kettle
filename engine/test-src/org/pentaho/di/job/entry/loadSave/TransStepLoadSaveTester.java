/* ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job.entry.loadSave;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.MemoryRepository;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.metastore.api.IMetaStore;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Khayrutdinov
 */
public class TransStepLoadSaveTester<T extends StepMetaInterface> extends LoadSaveBase<T> {
  private static final ObjectId ID_STEP = new LongObjectId( 1 );
  private static final ObjectId ID_TRANS = new LongObjectId( 0 );

  public TransStepLoadSaveTester( Class<T> clazz,
                                  List<String> commonAttributes,
                                  List<String> xmlAttributes,
                                  List<String> repoAttributes,
                                  Map<String, String> getterMap, Map<String, String> setterMap,
                                  Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap,
                                  Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap ) {
    super( clazz, commonAttributes, xmlAttributes, repoAttributes, getterMap, setterMap, fieldLoadSaveValidatorAttributeMap, fieldLoadSaveValidatorTypeMap );
  }

  public void testXmlRoundTrip() throws KettleException {
    T metaToSave = createMeta();
    Map<String, FieldLoadSaveValidator<?>> validatorMap =
      createValidatorMapAndInvokeSetters( xmlAttributes, metaToSave );

    T metaLoaded = createMeta();
    String xml = "<transformation>" + metaToSave.getXML() + "</transformation>";

    InputStream is = new ByteArrayInputStream( xml.getBytes() );
    metaLoaded.loadXML( XMLHandler.getSubNode( XMLHandler.loadXMLFile( is, null, false, false ), "transformation" ), null, (IMetaStore) null );
    validateLoadedMeta( xmlAttributes, validatorMap, metaToSave, metaLoaded );
  }

  public void testRepoRoundTrip() throws KettleException {
    T metaToSave = createMeta();
    Map<String, FieldLoadSaveValidator<?>> validatorMap =
      createValidatorMapAndInvokeSetters( repoAttributes, metaToSave );

    T metaLoaded = createMeta();
    Repository rep = new MemoryRepository();

    metaToSave.saveRep( rep, null, ID_TRANS, ID_STEP );
    metaLoaded.readRep( rep, null, ID_STEP, null );
    validateLoadedMeta( repoAttributes, validatorMap, metaToSave, metaLoaded );
  }
}
