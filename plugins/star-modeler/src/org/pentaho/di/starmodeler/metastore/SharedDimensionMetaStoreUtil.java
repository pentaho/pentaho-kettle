/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.util.MetaStoreUtil;
import org.pentaho.metastore.util.PentahoDefaults;

public class SharedDimensionMetaStoreUtil extends MetaStoreUtil {

  public static final String METASTORE_SHARED_DIMENSION_TYPE_NAME = "Star domain shared dimension";
  public static final String METASTORE_SHARED_DIMENSION_TYPE_DESCRIPTION = "This contains a shared dimension for a particular star domain";

  private static String namespace = PentahoDefaults.NAMESPACE;

  public enum Attribute {
    ID_SHARED_DIMENSION_DESCRIPTION("smsd_description"),
    ID_SHARED_DIMENSION_COLUMNS("smsd_columns"),
    ID_SHARED_DIMENSION_COLUMN("smsd_column"),
    ID_SHARED_DIMENSION_COLUMN_NAME("smsd_column_name"),
    ID_SHARED_DIMENSION_COLUMN_DESCRIPTION("smsd_column_description"),
    ;

    public String id;
    private Attribute(String id) {
      this.id = id;
    }
  }

  public static void saveSharedDimension(IMetaStore metaStore, LogicalTable sharedDimension, String locale) throws MetaStoreException {
    IMetaStoreElementType elementType = getSharedDimensionElementType(metaStore);
    IMetaStoreElement element = null;
    if (sharedDimension.getId()!=null) {
      element = metaStore.getElement(namespace, elementType, sharedDimension.getId());
    }

    if (element!=null) {
      // Update the shared dimension!
      //
      populateElementWithSharedDimension(metaStore, sharedDimension, locale, elementType, element);
      metaStore.updateElement(namespace, elementType, sharedDimension.getId(), element);
    } else {
      // New shared dimension
      //
      element = metaStore.newElement();
      populateElementWithSharedDimension(metaStore, sharedDimension, locale, elementType, element);
      metaStore.createElement(namespace, elementType, element);
    }

    sharedDimension.setId(element.getId());
  }

  private static void populateElementWithSharedDimension(IMetaStore metaStore, LogicalTable sharedDimension, String locale, IMetaStoreElementType elementType, IMetaStoreElement element) throws MetaStoreException {
    element.setElementType(elementType);
    element.setName(sharedDimension.getName(locale));
    element.addChild(metaStore.newAttribute(Attribute.ID_SHARED_DIMENSION_DESCRIPTION.id, sharedDimension.getDescription(locale)));
    IMetaStoreAttribute columnsAttribute = metaStore.newAttribute(Attribute.ID_SHARED_DIMENSION_COLUMNS.id, null);
    element.addChild(columnsAttribute);
    for (LogicalColumn column : sharedDimension.getLogicalColumns()) {
      IMetaStoreAttribute columnAttribute = metaStore.newAttribute(Attribute.ID_SHARED_DIMENSION_COLUMN.id, null);
      columnsAttribute.addChild(columnAttribute);
      columnAttribute.addChild(metaStore.newAttribute(Attribute.ID_SHARED_DIMENSION_COLUMN_NAME.id, column.getName(locale)));
      columnAttribute.addChild(metaStore.newAttribute(Attribute.ID_SHARED_DIMENSION_COLUMN_DESCRIPTION.id, column.getDescription(locale)));
    }
  }

  public static IMetaStoreElementType getSharedDimensionElementType(IMetaStore metaStore) throws MetaStoreException {
    verifyNamespaceCreated(metaStore, namespace);

    IMetaStoreElementType elementType = metaStore.getElementTypeByName(namespace, METASTORE_SHARED_DIMENSION_TYPE_NAME);
    if (elementType==null) {
      // create the type
      //
      elementType = metaStore.newElementType(namespace);
      elementType.setName(METASTORE_SHARED_DIMENSION_TYPE_NAME);
      elementType.setDescription(METASTORE_SHARED_DIMENSION_TYPE_DESCRIPTION);
      metaStore.createElementType(namespace, elementType);
    }
    return elementType;
  }
}
