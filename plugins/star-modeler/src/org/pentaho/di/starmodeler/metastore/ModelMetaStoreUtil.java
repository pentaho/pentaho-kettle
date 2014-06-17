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

package org.pentaho.di.starmodeler.metastore;

import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.util.MetaStoreUtil;
import org.pentaho.metastore.util.PentahoDefaults;

/**
 * Some utility methods to serialize to/from the metastore
 * @author matt
 *
 */
public class ModelMetaStoreUtil extends MetaStoreUtil {

  public static final String METASTORE_STAR_MODEL_TYPE_NAME = "Logical star model";
  public static final String METASTORE_STAR_MODEL_TYPE_DESCRIPTION = "This contains a logical star model with references to logical columns";

  public static final String METASTORE_LOGICAL_TABLE_TYPE_NAME = "Logical table";
  public static final String METASTORE_LOGICAL_TABLE_TYPE_DESCRIPTION = "This contains a logical table with logical columns in them";

  public enum Attribute {
    ID_MODEL_DESCRIPTION("model_description"),

    ID_LOGICAL_TABLES("logical_tables"),
    ID_LOGICAL_TABLE("logical_table"),
    ID_LOGICAL_TABLE_ID("logical_table_id"),
    ID_LOGICAL_TABLE_NAME("logical_table_name"),
    ;
    public String id;
    private Attribute(String id) {
      this.id = id;
    }
  }

  private static String defaultLocale = LanguageChoice.getInstance().getDefaultLocale().toString();

  public static IMetaStoreElementType getLogicalModelElementType(IMetaStore metaStore) throws MetaStoreException {
    verifyNamespaceCreated(metaStore, PentahoDefaults.NAMESPACE);

    IMetaStoreElementType elementType = metaStore.getElementTypeByName(PentahoDefaults.NAMESPACE, METASTORE_STAR_MODEL_TYPE_NAME);
    if (elementType==null) {
      // create the type
      //
      elementType = metaStore.newElementType(PentahoDefaults.NAMESPACE);
      elementType.setName(METASTORE_STAR_MODEL_TYPE_NAME);
      elementType.setDescription(METASTORE_STAR_MODEL_TYPE_DESCRIPTION);
      metaStore.createElementType(PentahoDefaults.NAMESPACE, elementType);
    }
    return elementType;
  }

  public static IMetaStoreElementType getLogicalTableElementType(IMetaStore metaStore) throws MetaStoreException {
    verifyNamespaceCreated(metaStore, PentahoDefaults.NAMESPACE);

    IMetaStoreElementType elementType = metaStore.getElementTypeByName(PentahoDefaults.NAMESPACE, METASTORE_LOGICAL_TABLE_TYPE_NAME);
    if (elementType==null) {
      // create the type
      //
      elementType = metaStore.newElementType(PentahoDefaults.NAMESPACE);
      elementType.setName(METASTORE_LOGICAL_TABLE_TYPE_NAME);
      elementType.setDescription(METASTORE_LOGICAL_TABLE_TYPE_DESCRIPTION);
      metaStore.createElementType(PentahoDefaults.NAMESPACE, elementType);
    }
    return elementType;
  }

  /**
   * Inflate a logical model from a metastore element.
   *
   * @param metaStore The metastore to read from
   * @param element The element to read from
   * @return The Logical Model
   * @throws MetaStoreException in case something goes wrong
   */
  public static LogicalModel buildLogicalModel(IMetaStore metaStore, IMetaStoreElement element) throws MetaStoreException {
    try {
      LogicalModel model = new LogicalModel();

      model.setName(new LocalizedString(defaultLocale, element.getName()));
      model.setDescription(new LocalizedString(defaultLocale, getChildString(element, Attribute.ID_MODEL_DESCRIPTION.id)));



      return model;
    } catch(Exception e) {
      throw new MetaStoreException("Unable to inflate logical model from metastore element", e);
    }
  }

  public static IMetaStoreElement saveLogicalModel(IMetaStore metaStore, LogicalModel model) throws MetaStoreException {
    IMetaStoreElementType elementType = getLogicalModelElementType(metaStore);
    IMetaStoreElement oldElement = metaStore.getElementByName(PentahoDefaults.NAMESPACE, elementType, model.getName(defaultLocale));
    if (oldElement==null) {
      // populate and create...
      //
      IMetaStoreElement newElement = populateElement(metaStore, model);
      metaStore.createElement(PentahoDefaults.NAMESPACE, elementType, newElement);
      return newElement;
    }  else {
      // The element exists, update...
      //
      IMetaStoreElement newElement = populateElement(metaStore, model);
      metaStore.updateElement(PentahoDefaults.NAMESPACE, elementType, oldElement.getId(), newElement);
      return newElement;
    }
  }

  private static IMetaStoreElement populateElement(IMetaStore metaStore, LogicalModel model) throws MetaStoreException {
    try {
      IMetaStoreElement element = metaStore.newElement();
      element.setName(model.getName(defaultLocale));
      element.addChild( metaStore.newAttribute(Attribute.ID_MODEL_DESCRIPTION.id, model.getDescription(defaultLocale)) );

      IMetaStoreAttribute logicalTablesAttribute = metaStore.newAttribute(Attribute.ID_LOGICAL_TABLES.id, model.getDescription(defaultLocale));
      element.addChild(logicalTablesAttribute);
      for (LogicalTable logicalTable : model.getLogicalTables()) {

        IMetaStoreAttribute logicalTableAttribute = metaStore.newAttribute(Attribute.ID_LOGICAL_TABLE.id, model.getDescription(defaultLocale));
        logicalTablesAttribute.addChild(logicalTableAttribute);

        //

        // Save the ID as well as the name (for safety)
        //
        logicalTableAttribute.addChild(metaStore.newAttribute(Attribute.ID_LOGICAL_TABLE_ID.id, logicalTable.getId()));
        logicalTableAttribute.addChild(metaStore.newAttribute(Attribute.ID_LOGICAL_TABLE_NAME.id, logicalTable.getName()));
      }

      return element;
    } catch(Exception e) {
      throw new MetaStoreException("Unable to populate metastore element from logical model", e);
    }
  }
}
