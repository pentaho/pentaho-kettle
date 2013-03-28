/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.repository.kdr.delegates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositoryMetaStoreDelegate extends KettleDatabaseRepositoryBaseDelegate {

  public KettleDatabaseRepositoryMetaStoreDelegate(KettleDatabaseRepository repository) {
    super(repository);
  }

  /**
   * Retrieve the ID for a namespace
   * 
   * @param namespace The namespace to look up
   * @return the ID of the namespace
   * @throws KettleException
   */
  public synchronized ObjectId getNamespaceId(String namespace) throws KettleException {
    return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_NAMESPACE),
        quote(KettleDatabaseRepository.FIELD_NAMESPACE_ID_NAMESPACE),
        quote(KettleDatabaseRepository.FIELD_NAMESPACE_NAME), namespace);
  }

  public synchronized ObjectId getElementTypeId(ObjectId namespaceId, String elementTypeName) throws KettleException {
    return repository.connectionDelegate.getIDWithValue(
        quoteTable(KettleDatabaseRepository.TABLE_R_ELEMENT_TYPE),
        quote(KettleDatabaseRepository.FIELD_ELEMENT_TYPE_ID_ELEMENT_TYPE),
        quote(KettleDatabaseRepository.FIELD_ELEMENT_TYPE_NAME),
        elementTypeName,
        new String[] { quote(KettleDatabaseRepository.FIELD_ELEMENT_TYPE_ID_NAMESPACE), },
        new ObjectId[] { namespaceId, }
      );
  }

  public synchronized ObjectId getElementId(ObjectId elementTypeId, String elementName) throws KettleException {
    return repository.connectionDelegate.getIDWithValue(
        quoteTable(KettleDatabaseRepository.TABLE_R_ELEMENT),
        quote(KettleDatabaseRepository.FIELD_ELEMENT_ID_ELEMENT),
        quote(KettleDatabaseRepository.FIELD_ELEMENT_NAME), 
        elementName,
        new String[] { quote(KettleDatabaseRepository.FIELD_ELEMENT_ID_ELEMENT_TYPE), },
        new ObjectId[] { elementTypeId, }
      );
  }

  public RowMetaAndData getElementType(ObjectId elementTypeId) throws KettleException {
    return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_ELEMENT_TYPE),
        quote(KettleDatabaseRepository.FIELD_ELEMENT_TYPE_ID_ELEMENT_TYPE), elementTypeId);
  }

  public RowMetaAndData getElement(ObjectId elementId) throws KettleException {
    return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_ELEMENT),
        quote(KettleDatabaseRepository.FIELD_ELEMENT_ID_ELEMENT), elementId);
  }

  public RowMetaAndData getElementAttribute(ObjectId elementAttributeId) throws KettleException {
    return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_ELEMENT_ATTRIBUTE),
        quote(KettleDatabaseRepository.FIELD_ELEMENT_ATTRIBUTE_ID_ELEMENT_ATTRIBUTE), elementAttributeId);
  }

  public Collection<RowMetaAndData> getNamespaces() throws KettleDatabaseException, KettleValueException {
    List<RowMetaAndData> attrs = new ArrayList<RowMetaAndData>();
    
    String sql = "SELECT * FROM " + quoteTable(KettleDatabaseRepository.TABLE_R_NAMESPACE);
    
    List<Object[]> rows = repository.connectionDelegate.getRows(sql, 0);
    for (Object[] row : rows) {
      RowMetaAndData rowWithMeta = new RowMetaAndData(repository.connectionDelegate.getReturnRowMeta(), row);
      long id = rowWithMeta.getInteger(quote(KettleDatabaseRepository.FIELD_NAMESPACE_ID_NAMESPACE), 0);
      if (id > 0) {
        attrs.add(rowWithMeta);
      }
    }
    return attrs;
  }
  
  public Collection<RowMetaAndData> getElementTypes(ObjectId namespaceId) throws KettleDatabaseException, KettleValueException {
    List<RowMetaAndData> attrs = new ArrayList<RowMetaAndData>();
    
    String sql = "SELECT * FROM " + quoteTable(KettleDatabaseRepository.TABLE_R_ELEMENT_TYPE)
        + " WHERE "+quote(KettleDatabaseRepository.FIELD_ELEMENT_TYPE_ID_NAMESPACE)+" = " + namespaceId.getId();
    
    List<Object[]> rows = repository.connectionDelegate.getRows(sql, 0);
    for (Object[] row : rows) {
      RowMetaAndData rowWithMeta = new RowMetaAndData(repository.connectionDelegate.getReturnRowMeta(), row);
      long id = rowWithMeta.getInteger(quote(KettleDatabaseRepository.FIELD_ELEMENT_TYPE_ID_ELEMENT_TYPE), 0);
      if (id > 0) {
        attrs.add(rowWithMeta);
      }
    }
    return attrs;
  }

  public Collection<RowMetaAndData> getElements(ObjectId elementTypeId) throws KettleDatabaseException, KettleValueException {
    List<RowMetaAndData> attrs = new ArrayList<RowMetaAndData>();
    
    String sql = "SELECT * FROM " + quoteTable(KettleDatabaseRepository.TABLE_R_ELEMENT)
        + " WHERE "+quote(KettleDatabaseRepository.FIELD_ELEMENT_ID_ELEMENT_TYPE)+" = " + elementTypeId.getId();
    
    List<Object[]> rows = repository.connectionDelegate.getRows(sql, 0);
    for (Object[] row : rows) {
      RowMetaAndData rowWithMeta = new RowMetaAndData(repository.connectionDelegate.getReturnRowMeta(), row);
      long id = rowWithMeta.getInteger(quote(KettleDatabaseRepository.FIELD_ELEMENT_ID_ELEMENT_TYPE), 0);
      if (id > 0) {
        attrs.add(rowWithMeta);
      }
    }
    return attrs;
  }

  public Collection<RowMetaAndData> getElementAttributes(ObjectId elementId, ObjectId parentAttributeId) throws KettleDatabaseException, KettleValueException {
    List<RowMetaAndData> attrs = new ArrayList<RowMetaAndData>();
    
    String sql = "SELECT * FROM " + quoteTable(KettleDatabaseRepository.TABLE_R_ELEMENT_ATTRIBUTE)
        + " WHERE "+quote(KettleDatabaseRepository.FIELD_ELEMENT_ATTRIBUTE_ID_ELEMENT)+" = " + elementId.getId();
    if (parentAttributeId!=null) {
      sql+=" AND "+quote(KettleDatabaseRepository.FIELD_ELEMENT_ATTRIBUTE_ID_ELEMENT_ATTRIBUTE_PARENT)+" = " + parentAttributeId.getId();
    }
    
    List<Object[]> rows = repository.connectionDelegate.getRows(sql, 0);
    for (Object[] row : rows) {
      RowMetaAndData rowWithMeta = new RowMetaAndData(repository.connectionDelegate.getReturnRowMeta(), row);
      long id = rowWithMeta.getInteger(quote(KettleDatabaseRepository.FIELD_ELEMENT_ATTRIBUTE_ID_ELEMENT_ATTRIBUTE), 0);
      if (id > 0) {
        attrs.add(rowWithMeta);
      }
    }
    return attrs;
  }

  public ObjectId insertNamespace(String namespace) throws KettleException {
    ObjectId idNamespace = repository.connectionDelegate.getNextID(
        quoteTable(KettleDatabaseRepository.TABLE_R_NAMESPACE), 
        quote(KettleDatabaseRepository.FIELD_NAMESPACE_ID_NAMESPACE)
      );
    RowMetaAndData table = new RowMetaAndData();

    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NAMESPACE_ID_NAMESPACE, ValueMetaInterface.TYPE_INTEGER), idNamespace);
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NAMESPACE_NAME, ValueMetaInterface.TYPE_STRING), namespace);

    repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), KettleDatabaseRepository.TABLE_R_NAMESPACE);
    repository.connectionDelegate.getDatabase().setValuesInsert(table);
    repository.connectionDelegate.getDatabase().insertRow();
    repository.connectionDelegate.getDatabase().closeInsert();
    
    if (log.isDebug()) log.logDebug("Saved namespace ["+namespace+"]");
    
    return idNamespace;
  }
  
}
