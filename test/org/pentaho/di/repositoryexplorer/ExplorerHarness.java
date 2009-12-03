/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.di.repositoryexplorer;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.repository.Directory;
import org.pentaho.di.repository.ProfileMeta;
import org.pentaho.di.repository.RepositoryElementLocationInterface;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.ProfileMeta.Permission;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorerCallback;

public class ExplorerHarness {

  /**
   * @param args
   */
  @SuppressWarnings("nls")
  public static void main(String[] args) {
    KettleDatabaseRepositoryMeta repositoryMeta;
    KettleDatabaseRepository repository;
    UserInfo userInfo;

    repositoryMeta = new KettleDatabaseRepositoryMeta();
    repositoryMeta.setName("Kettle Database Repository");
    repositoryMeta.setDescription("Kettle database test repository");
    
    DatabaseMeta connection = new DatabaseMeta();
    connection.setDatabaseType("Hypersonic");
    connection.setHostname("localhost");
    connection.setDBName("kettle_repository_4x");
    connection.setDBPort("9002");
    connection.setUsername("sa");    
    
    repositoryMeta.setConnection(connection);
    
    ProfileMeta adminProfile = new ProfileMeta("admin", "Administrator");
    adminProfile.addPermission(Permission.ADMIN);
    
    userInfo = new UserInfo("admin", "admin", "Administrator", "The system administrator", true, adminProfile);
    
    repository = new KettleDatabaseRepository();
    repository.init(repositoryMeta, userInfo);
    
    RepositoryExplorerCallback cb = new RepositoryExplorerCallback() {

        public boolean open(RepositoryElementLocationInterface element, String revision) {
          System.out.println("Name: ".concat(element.getName()));
          System.out.println("Type: ".concat(element.getRepositoryElementType().name()));
          System.out.println("Directory: ".concat(element.getRepositoryDirectory().toString()));
          System.out.println("Revision: ".concat(revision==null?"null":revision));
          return false; // do not close explorer
        }
    };

    
    try {
      repository.connect();
      Directory root = repository.loadRepositoryDirectoryTree();
      RepositoryExplorer explorer = new RepositoryExplorer(root, repository, cb);
      explorer.show();
    } catch (KettleSecurityException e) {
      e.printStackTrace();
    } catch (KettleException e) {
      e.printStackTrace();
    }
    

  }

}
