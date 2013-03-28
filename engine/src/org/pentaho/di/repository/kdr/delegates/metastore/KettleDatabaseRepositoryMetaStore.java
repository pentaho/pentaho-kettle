package org.pentaho.di.repository.kdr.delegates.metastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

public class KettleDatabaseRepositoryMetaStore extends MemoryMetaStore implements IMetaStore {

  protected KettleDatabaseRepository repository;
  
  public KettleDatabaseRepositoryMetaStore(KettleDatabaseRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<String> getNamespaces() throws MetaStoreException {
    
    try {
      List<String> namespaces = new ArrayList<String>();
      Collection<RowMetaAndData> namespaceRows = repository.metaStoreDelegate.getNamespaces();
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
      ObjectId namespaceId = repository.metaStoreDelegate.getNamespaceId(namespace);
      if (namespaceId!=null) {
        throw new MetaStoreNamespaceExistsException("Namespace with name '"+namespace+"' already exists");
      }
      
      // insert namespace into R_NAMESPACE
      //
      repository.metaStoreDelegate.insertNamespace(namespace);
      
    } catch(Exception e) {
      throw new MetaStoreException(e);
    }
  }
  
  @Override
  public void createElementType(String namespace, IMetaStoreElementType elementType) throws MetaStoreException, MetaStoreElementTypeExistsException {
    try {
      ObjectId namespaceId = repository.metaStoreDelegate.getNamespaceId(namespace);
      ObjectId elementTypeId = repository.metaStoreDelegate.getElementTypeId(namespaceId, elementType.getName());
      if (elementTypeId!=null) {
        throw new MetaStoreNamespaceExistsException("Element type with name '"+namespace+"' already exists in namespace '"+namespace+"'");
      }
      
      // TODO: insert element type into R_ELEMENT_TYPE
      
    } catch(Exception e) {
      throw new MetaStoreException(e);
    } finally {
    }
  }
  
  // TODO: complete implementation of element types and elements.
  //
  
}
