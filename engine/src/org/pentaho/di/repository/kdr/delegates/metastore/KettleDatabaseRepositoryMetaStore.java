package org.pentaho.di.repository.kdr.delegates.metastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryMetaStoreDelegate;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementExistException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.ITwoWayPasswordEncoder;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;

public class KettleDatabaseRepositoryMetaStore implements IMetaStore {

  protected KettleDatabaseRepository repository;
  private KettleDatabaseRepositoryMetaStoreDelegate delegate;
  
  public KettleDatabaseRepositoryMetaStore(KettleDatabaseRepository repository) {
    this.repository = repository;
    delegate = repository.metaStoreDelegate;
  }

  // Handle namespaces...
  
  @Override
  public List<String> getNamespaces() throws MetaStoreException {
    
    try {
      List<String> namespaces = new ArrayList<String>();
      Collection<RowMetaAndData> namespaceRows = delegate.getNamespaces();
      for (RowMetaAndData namespaceRow : namespaceRows) {
        String namespace = namespaceRow.getString(KettleDatabaseRepository.FIELD_NAMESPACE_NAME, null);
        if (!Const.isEmpty(namespace)) {
          namespaces.add(namespace);
        }
      }
      return namespaces;
      
    } catch (Exception e) {
      throw new MetaStoreException(e);
    }
  }
  
  @Override
  public void createNamespace(String namespace) throws MetaStoreException, MetaStoreNamespaceExistsException {
    try {
      ObjectId namespaceId = delegate.getNamespaceId(namespace);
      if (namespaceId!=null) {
        throw new MetaStoreNamespaceExistsException("Namespace with name '"+namespace+"' already exists");
      }
      
      // insert namespace into R_NAMESPACE
      //
      delegate.insertNamespace(namespace);
      
    } catch(Exception e) {
      throw new MetaStoreException(e);
    }
  }

  @Override
  public void deleteNamespace(String namespace) throws MetaStoreException, MetaStoreDependenciesExistsException {
    try {
      ObjectId namespaceId = delegate.getNamespaceId(namespace);
      if (namespaceId==null) {
        throw new MetaStoreException("Unable to find namespace with name '"+namespace+"'");
      }
      Collection<RowMetaAndData> elementTypeRows = delegate.getElementTypes(namespaceId);
      if (!elementTypeRows.isEmpty()) {
        List<String> dependencies = new ArrayList<String>();
        for (RowMetaAndData elementTypeRow : elementTypeRows) {
          Long elementTypeId = elementTypeRow.getInteger(KettleDatabaseRepository.TABLE_R_ELEMENT_TYPE);
          dependencies.add(Long.toString(elementTypeId));
        }
        throw new MetaStoreDependenciesExistsException(dependencies, "The namespace to delete, '"+namespace+"' is not empty");
      }
      
    } catch(MetaStoreException e) {
      throw e;
    } catch(Exception e) {    
      throw new MetaStoreException(e);
    }
  }

  @Override
  public boolean namespaceExists(String namespace) throws MetaStoreException {
    try {
      return delegate.getNamespaceId(namespace)!=null;
    } catch(Exception e) {
      throw new MetaStoreException(e);
    }
  }
  
  // Handle the element types
  
  
  public void createElementType(String namespace, IMetaStoreElementType elementType) throws MetaStoreException, MetaStoreElementTypeExistsException {
    try {
      ObjectId namespaceId = delegate.getNamespaceId(namespace);
      ObjectId elementTypeId = delegate.getElementTypeId(namespaceId, elementType.getName());
      if (elementTypeId!=null) {
        throw new MetaStoreNamespaceExistsException("Element type with name '"+elementType.getName()+"' already exists in namespace '"+namespace+"'");
      }
      
      KDBRMetaStoreElementType newElementType = new KDBRMetaStoreElementType(delegate, namespace, namespaceId, elementType.getName(), elementType.getDescription());
      newElementType.save();      
    } catch(MetaStoreException e) {
      throw e;
    } catch(Exception e) {
      throw new MetaStoreException(e);
    }
  }

  @Override
  public List<IMetaStoreElementType> getElementTypes(String namespace) throws MetaStoreException {
    
    return null;
  }

  @Override
  public List<String> getElementTypeIds(String namespace) throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMetaStoreElementType getElementType(String namespace, String elementTypeId) throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMetaStoreElementType getElementTypeByName(String namespace, String elementTypeName) throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateElementType(String namespace, IMetaStoreElementType elementType) throws MetaStoreException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteElementType(String namespace, IMetaStoreElementType elementType) throws MetaStoreException,
      MetaStoreDependenciesExistsException {
    try {
      ObjectId namespaceId = delegate.getNamespaceId(namespace);
      if (namespaceId==null) {
        throw new MetaStoreException("Unable to find namespace with name '"+namespace+"'");
      }
      
      Collection<RowMetaAndData> elementTypeRows = delegate.getElementTypes(namespaceId);
      if (!elementTypeRows.isEmpty()) {
        List<String> dependencies = new ArrayList<String>();
        for (RowMetaAndData elementTypeRow : elementTypeRows) {
          Long elementTypeId = elementTypeRow.getInteger(KettleDatabaseRepository.TABLE_R_ELEMENT_TYPE);
          dependencies.add(Long.toString(elementTypeId));
        }
        throw new MetaStoreDependenciesExistsException(dependencies, "The namespace to delete, '"+namespace+"' is not empty");
      }
      
    } catch(MetaStoreException e) {
      throw e;
    } catch(Exception e) {    
      throw new MetaStoreException(e);
    }
  }

  @Override
  public List<IMetaStoreElement> getElements(String namespace, IMetaStoreElementType elementType)
      throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getElementIds(String namespace, IMetaStoreElementType elementType) throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMetaStoreElement getElement(String namespace, IMetaStoreElementType elementType, String elementId)
      throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMetaStoreElement getElementByName(String namespace, IMetaStoreElementType elementType, String name)
      throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void createElement(String namespace, IMetaStoreElementType elementType, IMetaStoreElement element)
      throws MetaStoreException, MetaStoreElementExistException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteElement(String namespace, IMetaStoreElementType elementType, String elementId)
      throws MetaStoreException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateElement(String namespace, IMetaStoreElementType elementType, String elementId,
      IMetaStoreElement element) throws MetaStoreException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public IMetaStoreElementType newElementType(String namespace) throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMetaStoreElement newElement() throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMetaStoreElement newElement(IMetaStoreElementType elementType, String id, Object value)
      throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMetaStoreAttribute newAttribute(String id, Object value) throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMetaStoreElementOwner newElementOwner(String name, MetaStoreElementOwnerType ownerType)
      throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDescription() throws MetaStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setTwoWayPasswordEncoder(ITwoWayPasswordEncoder encoder) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ITwoWayPasswordEncoder getTwoWayPasswordEncoder() {
    // TODO Auto-generated method stub
    return null;
  }
  
  // TODO: complete implementation of element types and elements.
  //
  
}
