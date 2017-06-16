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
package org.pentaho.di.repository.pur;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.di.repository.pur.model.ObjectAce;
import org.pentaho.di.repository.pur.model.ObjectAcl;
import org.pentaho.di.repository.pur.model.RepositoryObjectAce;
import org.pentaho.di.repository.pur.model.RepositoryObjectAcl;
import org.pentaho.di.repository.pur.model.RepositoryObjectRecipient;
import org.pentaho.di.ui.repository.pur.services.IConnectionAclService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;

public class UnifiedRepositoryConnectionAclService implements IConnectionAclService {
  private final IUnifiedRepository pur;

  public UnifiedRepositoryConnectionAclService( IUnifiedRepository pur ) {
    this.pur = pur;
  }

  @Override
  public ObjectAcl getAcl( ObjectId fileId, boolean forceParentInheriting ) throws KettleException {
    RepositoryFileAcl acl = null;
    try {
      acl = pur.getAcl( fileId.getId() );
    } catch ( Exception drfe ) {
      // The user does not have rights to view the acl information.
      throw new KettleException( drfe );
    }
    RepositoryFileSid sid = acl.getOwner();
    ObjectRecipient owner = new RepositoryObjectRecipient( sid.getName() );
    if ( sid.getType().equals( RepositoryFileSid.Type.USER ) ) {
      owner.setType( Type.USER );
    } else {
      owner.setType( Type.ROLE );
    }

    ObjectAcl objectAcl = new RepositoryObjectAcl( owner );
    List<RepositoryFileAce> aces;

    // This flag (forceParentInheriting) is here to allow us to query the acl AS IF 'inherit from parent'
    // were true, without committing the flag to the repository. We need this for state representation
    // while a user is changing the acl in the client dialogs.

    if ( forceParentInheriting ) {
      objectAcl.setEntriesInheriting( true );
      aces = pur.getEffectiveAces( acl.getId(), true );
    } else {
      objectAcl.setEntriesInheriting( acl.isEntriesInheriting() );
      aces = ( acl.isEntriesInheriting() ) ? pur.getEffectiveAces( acl.getId() ) : acl.getAces();
    }
    List<ObjectAce> objectAces = new ArrayList<ObjectAce>();
    for ( RepositoryFileAce ace : aces ) {
      EnumSet<RepositoryFilePermission> permissions = ace.getPermissions();
      EnumSet<RepositoryFilePermission> permissionSet = EnumSet.noneOf( RepositoryFilePermission.class );
      RepositoryFileSid aceSid = ace.getSid();
      ObjectRecipient recipient = new RepositoryObjectRecipient( aceSid.getName() );
      if ( aceSid.getType().equals( RepositoryFileSid.Type.USER ) ) {
        recipient.setType( Type.USER );
      } else {
        recipient.setType( Type.ROLE );
      }
      permissionSet.addAll( permissions );

      objectAces.add( new RepositoryObjectAce( recipient, permissionSet ) );

    }
    objectAcl.setAces( objectAces );
    return objectAcl;
  }

  @Override
  public void setAcl( ObjectId fileId, ObjectAcl objectAcl ) throws KettleException {
    try {
      RepositoryFileAcl acl = pur.getAcl( fileId.getId() );
      RepositoryFileAcl.Builder newAclBuilder =
          new RepositoryFileAcl.Builder( acl ).entriesInheriting( objectAcl.isEntriesInheriting() ).clearAces();
      if ( !objectAcl.isEntriesInheriting() ) {
        List<ObjectAce> aces = objectAcl.getAces();
        for ( ObjectAce objectAce : aces ) {

          EnumSet<RepositoryFilePermission> permissions = objectAce.getPermissions();
          EnumSet<RepositoryFilePermission> permissionSet = EnumSet.noneOf( RepositoryFilePermission.class );
          ObjectRecipient recipient = objectAce.getRecipient();
          RepositoryFileSid sid;
          if ( recipient.getType().equals( Type.ROLE ) ) {
            sid = new RepositoryFileSid( recipient.getName(), RepositoryFileSid.Type.ROLE );
          } else {
            sid = new RepositoryFileSid( recipient.getName() );
          }
          if ( permissions != null ) {
            permissionSet.addAll( permissions );
          }
          newAclBuilder.ace( sid, permissionSet );
        }
      }
      pur.updateAcl( newAclBuilder.build() );
    } catch ( Exception drfe ) {
      // The user does not have rights to view or set the acl information.
      throw new KettleException( drfe );
    }
  }

  @Override
  public boolean hasAccess( ObjectId id, RepositoryFilePermission perm ) throws KettleException {
    RepositoryFile repositoryFile = pur.getFileById( id.getId() );
    return pur.hasAccess( repositoryFile.getPath(), EnumSet.of( perm ) );
  }

}
