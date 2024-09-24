/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.starmodeler.StarDomain;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.util.MetaStoreUtil;
import org.pentaho.metastore.util.PentahoDefaults;

public class StarDomainMetaStoreUtil extends MetaStoreUtil {

  public static final String METASTORE_STAR_DOMAIN_TYPE_NAME = "Star domain";
  public static final String METASTORE_STAR_DOMAIN_TYPE_DESCRIPTION = "This contains a star domain, a collection of star models and shared dimensions";

  protected static String namespace = PentahoDefaults.NAMESPACE;

  public enum Attribute {
    ID_STAR_DOMAIN_DESCRIPTION("description"),
    ;
    public String id;
    private Attribute(String id) {
      this.id = id;
    }
  }

  protected static String defaultLocale = LanguageChoice.getInstance().getDefaultLocale().toString();

  public static IMetaStoreElementType getStarDomainElementType(IMetaStore metaStore) throws MetaStoreException {
    verifyNamespaceCreated(metaStore, namespace);

    IMetaStoreElementType elementType = metaStore.getElementTypeByName(namespace, METASTORE_STAR_DOMAIN_TYPE_NAME);
    if (elementType==null) {
      // create the type
      //
      elementType = createStarDomainElementType(metaStore);
    }
    return elementType;
  }

  public static IMetaStoreElementType createStarDomainElementType(IMetaStore metaStore) throws MetaStoreElementTypeExistsException, MetaStoreException {
    IMetaStoreElementType elementType = metaStore.newElementType(namespace);
    elementType.setName(METASTORE_STAR_DOMAIN_TYPE_NAME);
    elementType.setDescription(METASTORE_STAR_DOMAIN_TYPE_DESCRIPTION);
    metaStore.createElementType(namespace, elementType);

    return elementType;
  }

  public static void saveStarDomain(IMetaStore metaStore, StarDomain starDomain) throws MetaStoreException {
    IMetaStoreElementType elementType = getStarDomainElementType(metaStore);
    IMetaStoreElement element = null;
    if (starDomain.getObjectId()!=null) {
      // verify the ID!
      //
      element = metaStore.getElement(namespace, elementType, starDomain.getObjectId().toString());
    }

    if (element==null) {
      // Create a new element
      //
      element = metaStore.newElement();
      populateElementWithStarDomain(metaStore, starDomain, element, elementType);
      metaStore.createElement(namespace, elementType, element);
    } else {
      // Update an existing element
      //
      populateElementWithStarDomain(metaStore, starDomain, element, elementType);
      metaStore.updateElement(namespace, elementType, starDomain.getObjectId().toString(), element);
    }
    starDomain.setObjectId(new StringObjectId(element.getId().toString()));
  }

  private static void populateElementWithStarDomain(IMetaStore metaStore, StarDomain starDomain, IMetaStoreElement element, IMetaStoreElementType elementType) throws MetaStoreException {
    element.setElementType(elementType);
    element.setName(starDomain.getName());
    element.addChild(metaStore.newAttribute(Attribute.ID_STAR_DOMAIN_DESCRIPTION.id, starDomain.getDescription()));
  }

  public static List<IdNameDescription> getStarDomainList(IMetaStore metaStore) throws MetaStoreException {
    IMetaStoreElementType elementType = getStarDomainElementType(metaStore);
    List<IdNameDescription> list = new ArrayList<IdNameDescription>();
    for (IMetaStoreElement element : metaStore.getElements(namespace, elementType)) {
      IdNameDescription nameDescription = new IdNameDescription(element.getId(), element.getName(), getChildString(element, Attribute.ID_STAR_DOMAIN_DESCRIPTION.id));
      list.add(nameDescription);
    }
    return list;
  }

  public static StarDomain loadStarDomain( IMetaStore metaStore, String id ) throws MetaStoreException {
    IMetaStoreElementType elementType = getStarDomainElementType(metaStore);
    IMetaStoreElement element = metaStore.getElement(namespace, elementType, id);
    if (element==null) {
      return null;
    }
    StarDomain starDomain = new StarDomain();
    starDomain.setObjectId(new StringObjectId(id));
    starDomain.setName(element.getName());
    starDomain.setDescription(getChildString(element, Attribute.ID_STAR_DOMAIN_DESCRIPTION.id));

    return starDomain;
  }

}
