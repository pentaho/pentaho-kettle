/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.ui.repository.pur.repositoryexplorer.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.pur.model.ObjectAce;
import org.pentaho.di.repository.pur.model.ObjectAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.ui.xul.XulEventSourceAdapter;

/**
 * TODO mlowery This class represents an ACL, not an ACLs.
 */
public class UIRepositoryObjectAcls extends XulEventSourceAdapter implements java.io.Serializable {

  private static final long serialVersionUID = -4576328356619980808L; /* EESOURCE: UPDATE SERIALVERUID */

  protected ObjectAcl obj;

  private List<UIRepositoryObjectAcl> selectedAclList = new ArrayList<UIRepositoryObjectAcl>();

  private boolean removeEnabled;

  private boolean modelDirty;

  private boolean hasManageAclAccess;

  public UIRepositoryObjectAcls() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public void setObjectAcl( ObjectAcl obj ) {
    this.obj = obj;
    this.firePropertyChange( "acls", null, getAcls() ); //$NON-NLS-1$
    this.firePropertyChange( "entriesInheriting", null, isEntriesInheriting() ); //$NON-NLS-1$
  }

  public ObjectAcl getObjectAcl() {
    return this.obj;
  }

  public List<UIRepositoryObjectAcl> getAcls() {
    if ( obj != null ) {
      List<UIRepositoryObjectAcl> acls = new ArrayList<UIRepositoryObjectAcl>();
      for ( ObjectAce ace : obj.getAces() ) {
        acls.add( new UIRepositoryObjectAcl( ace ) );
      }
      return acls;
    }
    return null;
  }

  public void setAcls( List<UIRepositoryObjectAcl> acls ) {
    List<UIRepositoryObjectAcl> prevousVal = new ArrayList<UIRepositoryObjectAcl>();
    prevousVal.addAll( getAcls() );

    this.obj.getAces().clear();
    if ( acls != null ) {
      for ( UIRepositoryObjectAcl acl : acls ) {
        obj.getAces().add( acl.getAce() );
      }
    }
    this.firePropertyChange( "acls", prevousVal, getAcls() ); //$NON-NLS-1$
  }

  public void addAcls( List<UIRepositoryObjectAcl> aclsToAdd ) {
    addAcls( aclsToAdd, false );
  }

  public void addDefaultAcls( List<UIRepositoryObjectAcl> aclsToAdd ) {
    addAcls( aclsToAdd, true );
  }

  private void addAcls( List<UIRepositoryObjectAcl> aclsToAdd, boolean initializePermissions ) {
    for ( UIRepositoryObjectAcl acl : aclsToAdd ) {
      addAcl( acl, initializePermissions );
    }
    this.firePropertyChange( "acls", null, getAcls() ); //$NON-NLS-1$
    // Setting the selected index to the first item in the list
    if ( obj.getAces().size() > 0 ) {
      List<UIRepositoryObjectAcl> aclList = new ArrayList<UIRepositoryObjectAcl>();
      aclList.add( new UIRepositoryObjectAcl( getAceAtIndex( 0 ) ) );
      setSelectedAclList( aclList );
    }
    setRemoveEnabled( !obj.isEntriesInheriting() && !isEmpty() && hasManageAclAccess() );
    setModelDirty( true );
  }

  public void addAcl( UIRepositoryObjectAcl aclToAdd ) {
    addAcl( aclToAdd, false );
  }

  /**
   * This method will modify the permissions in the aclToAdd argument
   * 
   * @param aclToAdd
   */
  public void addDefaultAcl( UIRepositoryObjectAcl aclToAdd ) {
    addAcl( aclToAdd, true );
  }

  private void addAcl( UIRepositoryObjectAcl aclToAdd, boolean initializePermissions ) {
    if ( initializePermissions ) {
      // By default the user or role will get a READ when a user or role is added
      EnumSet<RepositoryFilePermission> initialialPermisson = EnumSet.of( RepositoryFilePermission.READ );
      aclToAdd.setPermissionSet( initialialPermisson );
    }
    this.obj.getAces().add( aclToAdd.getAce() );
  }

  public void removeAcls( List<UIRepositoryObjectAcl> aclsToRemove ) {
    for ( UIRepositoryObjectAcl acl : aclsToRemove ) {
      removeAcl( acl.getRecipientName() );
    }

    this.firePropertyChange( "acls", null, getAcls() ); //$NON-NLS-1$
    if ( obj.getAces().size() > 0 ) {
      List<UIRepositoryObjectAcl> aclList = new ArrayList<UIRepositoryObjectAcl>();
      aclList.add( new UIRepositoryObjectAcl( getAceAtIndex( 0 ) ) );
      setSelectedAclList( aclList );
    } else {
      setSelectedAclList( null );
    }
    setRemoveEnabled( !obj.isEntriesInheriting() && !isEmpty() && hasManageAclAccess() );
    setModelDirty( true );
  }

  public void removeAcl( String recipientName ) {
    ObjectAce aceToRemove = null;

    for ( ObjectAce ace : obj.getAces() ) {
      if ( ace.getRecipient().getName().equals( recipientName ) ) {
        aceToRemove = ace;
        break;
      }
    }
    obj.getAces().remove( aceToRemove );
  }

  public void removeSelectedAcls() {
    // side effect deletes multiple acls when only one selected.
    List<UIRepositoryObjectAcl> removalList = new ArrayList<UIRepositoryObjectAcl>();
    for ( UIRepositoryObjectAcl rem : getSelectedAclList() ) {
      removalList.add( rem );
    }
    removeAcls( removalList );
  }

  public void updateAcl( UIRepositoryObjectAcl aclToUpdate ) {
    List<ObjectAce> aces = obj.getAces();
    for ( ObjectAce ace : aces ) {
      if ( ace.getRecipient().getName().equals( aclToUpdate.getRecipientName() ) ) {
        ace.setPermissions( aclToUpdate.getPermissionSet() );
      }
    }
    UIRepositoryObjectAcl acl = getAcl( aclToUpdate.getRecipientName() );
    acl.setPermissionSet( aclToUpdate.getPermissionSet() );
    this.firePropertyChange( "acls", null, getAcls() ); //$NON-NLS-1$

    // above firePropertyChange replaces all elements in the listBox and therefore clears any selected elements;
    // however, the selectedAclList field is never updated because no selectedIndices event is ever called; manually
    // update it to reflect the selected state of the user/role list now (no selection)
    selectedAclList.clear();

    // Setting the selected index
    List<UIRepositoryObjectAcl> aclList = new ArrayList<UIRepositoryObjectAcl>();
    aclList.add( aclToUpdate );
    setSelectedAclList( aclList );

    setModelDirty( true );
  }

  public UIRepositoryObjectAcl getAcl( String recipient ) {
    for ( ObjectAce ace : obj.getAces() ) {
      if ( ace.getRecipient().getName().equals( recipient ) ) {
        return new UIRepositoryObjectAcl( ace );
      }
    }
    return null;
  }

  public List<UIRepositoryObjectAcl> getSelectedAclList() {
    return selectedAclList;
  }

  public void setSelectedAclList( List<UIRepositoryObjectAcl> list ) {
    if ( this.selectedAclList != null && this.selectedAclList.equals( list ) ) {
      return;
    }

    List<UIRepositoryObjectAcl> previousVal = new ArrayList<UIRepositoryObjectAcl>();
    previousVal.addAll( selectedAclList );
    selectedAclList.clear();
    if ( list != null ) {
      selectedAclList.addAll( list );
      this.firePropertyChange( "selectedAclList", previousVal, list ); //$NON-NLS-1$
    }
    setRemoveEnabled( !isEntriesInheriting() && !isEmpty() && hasManageAclAccess() );
  }

  public boolean isEntriesInheriting() {
    if ( obj != null ) {
      return obj.isEntriesInheriting();
    } else {
      return false;
    }
  }

  public void setEntriesInheriting( boolean entriesInheriting ) {
    if ( obj != null ) {
      boolean previousVal = isEntriesInheriting();
      obj.setEntriesInheriting( entriesInheriting );
      this.firePropertyChange( "entriesInheriting", previousVal, entriesInheriting ); //$NON-NLS-1$
      setSelectedAclList( null );
      setRemoveEnabled( !entriesInheriting && !isEmpty() && hasManageAclAccess() );
      // Only dirty the model if the value has changed
      if ( previousVal != entriesInheriting ) {
        setModelDirty( true );
      }
    }
  }

  public ObjectRecipient getOwner() {
    if ( obj != null ) {
      return obj.getOwner();
    } else {
      return null;
    }
  }

  public void setRemoveEnabled( boolean removeEnabled ) {
    this.removeEnabled = removeEnabled;
    this.firePropertyChange( "removeEnabled", null, removeEnabled ); //$NON-NLS-1$
  }

  public boolean isRemoveEnabled() {
    return removeEnabled;
  }

  public int getAceIndex( ObjectAce ace ) {
    List<ObjectAce> aceList = obj.getAces();
    for ( int i = 0; i < aceList.size(); i++ ) {
      if ( ace.equals( aceList.get( i ) ) ) {
        return i;
      }
    }
    return -1;
  }

  public ObjectAce getAceAtIndex( int index ) {
    if ( index >= 0 ) {
      return obj.getAces().get( index );
    } else {
      return null;
    }
  }

  public void setModelDirty( boolean modelDirty ) {
    this.modelDirty = modelDirty;
  }

  public boolean isModelDirty() {
    return modelDirty;
  }

  public boolean hasManageAclAccess() {
    return hasManageAclAccess;
  }

  public void setHasManageAclAccess( boolean hasManageAclAccess ) {
    this.hasManageAclAccess = hasManageAclAccess;
  }

  public void clear() {
    setRemoveEnabled( false );
    setModelDirty( false );
    setAcls( null );
    setSelectedAclList( null );
    setHasManageAclAccess( false );
  }

  private boolean isEmpty() {
    return getSelectedAclList() == null || getSelectedAclList().size() <= 0;
  }
}
