package org.pentaho.di.shared;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.RepositoryElementInterface;

import com.google.errorprone.annotations.Var;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This Manager wraps another manager and shares variables with all SharedObjects retrieved from the parent manager.
 *
*/
public class VariableSharingSharedObjectManager<T extends SharedObjectInterface<T> & RepositoryElementInterface & VariableSpace>
  implements SharedObjectsManagementInterface<T> {

  private final SharedObjectsManagementInterface<T> parent;
  private final VariableSpace variables;

  public VariableSharingSharedObjectManager( VariableSpace variables, SharedObjectsManagementInterface<T> parent ) {
    this.variables = variables;
    this.parent = parent;
  }

  @Override
  public void add( T object ) throws KettleException {
    parent.add( object );
  }

  @Override
  public T get( String name) throws KettleException {
    T object = parent.get( name );
    if ( object != null ) {
      object.shareVariablesWith( variables );
    }
    return object;
  }

  @Override
  public List<T> getAll() throws KettleException {
    List<T> all = parent.getAll();
    all = all.stream().peek( o -> o.shareVariablesWith( variables ) ).collect( Collectors.toList() );
    return all;
  }

  @Override
  public void clear() throws KettleException {
    parent.clear();
  }

  @Override
  public void remove( T object ) throws KettleException {
    parent.remove( object );
  }

  @Override
  public void remove( String name) throws KettleException {
    parent.remove( name );
  }

}
