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

package org.pentaho.di.repository;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;
import org.pentaho.metastore.api.security.MetaStoreObjectPermission;
import org.pentaho.metastore.api.security.MetaStoreOwnerPermissions;
import org.pentaho.metastore.util.PentahoDefaults;

@Ignore
public class KettleMetaStoreTestBase extends TestCase {

  // Namespace: Hitachi Vantara
  //
  protected static String namespace = PentahoDefaults.NAMESPACE;

  // Element type: Shared Dimension
  //
  protected static final String SHARED_DIMENSION_NAME = "Shared Dimension";
  protected static final String SHARED_DIMENSION_DESCRIPTION = "Star modeler shared dimension";

  // Element: customer dimension
  //
  protected static final String CUSTOMER_DIMENSION_NAME = "Customer dimension";

  public void testFunctionality( IMetaStore metaStore ) throws MetaStoreException {
    if ( !metaStore.namespaceExists( namespace ) ) {
      metaStore.createNamespace( namespace );
    }
    List<String> namespaces = metaStore.getNamespaces();
    assertEquals( 1, namespaces.size() );

    IMetaStoreElementType elementType = metaStore.newElementType( namespace );
    elementType.setName( SHARED_DIMENSION_NAME );
    elementType.setDescription( SHARED_DIMENSION_DESCRIPTION );
    metaStore.createElementType( namespace, elementType );
    assertNotNull( elementType.getId() );

    List<IMetaStoreElementType> elementTypes = metaStore.getElementTypes( namespace );
    assertEquals( 1, elementTypes.size() );

    try {
      metaStore.createElementType( namespace, elementType );
      fail( "Duplicate creation error expected!" );
    } catch ( MetaStoreElementTypeExistsException e ) {
      // OK!
    } catch ( MetaStoreException e ) {
      e.printStackTrace();
      fail( "Create exception needs to be MetaStoreDataTypesExistException" );
    }

    // Try to delete the namespace, should error out
    //
    try {
      metaStore.deleteNamespace( namespace );
      fail( "Expected error while deleting namespace with content!" );
    } catch ( MetaStoreDependenciesExistsException e ) {
      // OK!
      List<String> dependencies = e.getDependencies();
      assertNotNull( dependencies );
      assertEquals( 1, dependencies.size() );
      assertEquals( elementType.getId(), dependencies.get( 0 ) );
    }

    IMetaStoreElement customerDimension = generateCustomerDimensionElement( metaStore, elementType );
    IMetaStoreElementOwner elementOwner = customerDimension.getOwner();
    assertNotNull( elementOwner );
    assertEquals( "joe", elementOwner.getName() );
    assertEquals( MetaStoreElementOwnerType.USER, elementOwner.getOwnerType() );

    metaStore.createElement( namespace, elementType, customerDimension );
    assertNotNull( customerDimension.getId() );
    List<IMetaStoreElement> elements = metaStore.getElements( namespace, elementType );
    assertEquals( 1, elements.size() );
    assertNotNull( elements.get( 0 ) );
    assertEquals( CUSTOMER_DIMENSION_NAME, elements.get( 0 ).getName() );

    // Try to delete the data type, should error out
    //
    try {
      metaStore.deleteElementType( namespace, elementType );
      fail( "Expected error while deleting data type with content!" );
    } catch ( MetaStoreDependenciesExistsException e ) {
      // OK!
      List<String> dependencies = e.getDependencies();
      assertNotNull( dependencies );
      assertEquals( 1, dependencies.size() );
      assertEquals( customerDimension.getId(), dependencies.get( 0 ) );
    }

    // Some lookup-by-name tests...
    //
    assertNotNull( metaStore.getElementTypeByName( namespace, SHARED_DIMENSION_NAME ) );
    assertNotNull( metaStore.getElementByName( namespace, elementType, CUSTOMER_DIMENSION_NAME ) );

    // Clean up shop!
    //
    metaStore.deleteElement( namespace, elementType, customerDimension.getId() );
    elements = metaStore.getElements( namespace, elementType );
    assertEquals( 0, elements.size() );

    metaStore.deleteElementType( namespace, elementType );
    elementTypes = metaStore.getElementTypes( namespace );
    assertEquals( 0, elementTypes.size() );

    metaStore.deleteNamespace( namespace );
    namespaces = metaStore.getNamespaces();
    assertEquals( 0, namespaces.size() );
  }

  private IMetaStoreElement generateCustomerDimensionElement( IMetaStore metaStore,
    IMetaStoreElementType elementType ) throws MetaStoreException {
    IMetaStoreElement element = metaStore.newElement();
    element.setElementType( elementType );
    element.setName( CUSTOMER_DIMENSION_NAME );

    element.addChild( metaStore.newAttribute( "description", "This is the shared customer dimension" ) );
    element.addChild( metaStore.newAttribute( "physical_table", "DIM_CUSTOMER" ) );
    IMetaStoreAttribute fieldsElement = metaStore.newAttribute( "fields", null );
    element.addChild( fieldsElement );

    // A technical key
    //
    IMetaStoreAttribute fieldElement = metaStore.newAttribute( "field_0", null );
    fieldsElement.addChild( fieldElement );
    fieldElement.addChild( metaStore.newAttribute( "field_name", "Customer TK" ) );
    fieldElement.addChild( metaStore.newAttribute( "field_description", "Customer Technical key" ) );
    fieldElement.addChild( metaStore.newAttribute( "field_phyiscal_name", "customer_tk" ) );
    fieldElement.addChild( metaStore.newAttribute( "field_kettle_type", "Integer" ) );

    // A version field
    //
    fieldElement = metaStore.newAttribute( "field_1", null );
    fieldsElement.addChild( fieldElement );
    fieldElement.addChild( metaStore.newAttribute( "field_name", "version field" ) );
    fieldElement.addChild( metaStore.newAttribute( "field_description", "dimension version field (1..N)" ) );
    fieldElement.addChild( metaStore.newAttribute( "field_phyiscal_name", "version" ) );
    fieldElement.addChild( metaStore.newAttribute( "field_kettle_type", "Integer" ) );

    // Natural key
    //
    fieldElement = metaStore.newAttribute( "field_2", null );
    fieldsElement.addChild( fieldElement );
    fieldElement.addChild( metaStore.newAttribute( "field_name", "Customer ID" ) );
    fieldElement.addChild( metaStore.newAttribute(
      "field_description", "Customer ID as a natural key of this dimension" ) );
    fieldElement.addChild( metaStore.newAttribute( "field_phyiscal_name", "customer_id" ) );
    fieldElement.addChild( metaStore.newAttribute( "field_kettle_type", "Integer" ) );

    // Start date
    //
    fieldElement = metaStore.newAttribute( "field_3", null );
    fieldsElement.addChild( fieldElement );
    fieldElement.addChild( metaStore.newAttribute( "field_name", "Start date" ) );
    fieldElement.addChild( metaStore.newAttribute(
      "field_description", "Start of validity of this dimension entry" ) );
    fieldElement.addChild( metaStore.newAttribute( "field_phyiscal_name", "start_date" ) );
    fieldElement.addChild( metaStore.newAttribute( "field_kettle_type", "Date" ) );

    // End date
    //
    fieldElement = metaStore.newAttribute( "field_4", null );
    fieldsElement.addChild( fieldElement );
    fieldElement.addChild( metaStore.newAttribute( "field_name", "End date" ) );
    fieldElement
      .addChild( metaStore.newAttribute( "field_description", "End of validity of this dimension entry" ) );
    fieldElement.addChild( metaStore.newAttribute( "field_phyiscal_name", "end_date" ) );
    fieldElement.addChild( metaStore.newAttribute( "field_kettle_type", "Date" ) );

    // A few columns...
    //
    for ( int i = 5; i <= 10; i++ ) {
      fieldElement = metaStore.newAttribute( "field_" + i, null );
      fieldsElement.addChild( fieldElement );
      fieldElement.addChild( metaStore.newAttribute( "field_name", "Field name " + i ) );
      fieldElement.addChild( metaStore.newAttribute( "field_description", "Field description " + i ) );
      fieldElement.addChild( metaStore.newAttribute( "field_phyiscal_name", "physical_name_" + i ) );
      fieldElement.addChild( metaStore.newAttribute( "field_kettle_type", "String" ) );
    }

    // Some security
    //
    element.setOwner( metaStore.newElementOwner( "joe", MetaStoreElementOwnerType.USER ) );

    // The "users" role has read/write permissions
    //
    IMetaStoreElementOwner usersRole = metaStore.newElementOwner( "users", MetaStoreElementOwnerType.ROLE );
    MetaStoreOwnerPermissions usersRoleOwnerPermissions =
      new MetaStoreOwnerPermissions( usersRole, MetaStoreObjectPermission.READ, MetaStoreObjectPermission.UPDATE );
    element.getOwnerPermissionsList().add( usersRoleOwnerPermissions );

    return element;
  }
}
