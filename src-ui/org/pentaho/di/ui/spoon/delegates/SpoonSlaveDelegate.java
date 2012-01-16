/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.delegates;

import org.eclipse.swt.SWT;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.HasSlaveServersInterface;
import org.pentaho.di.ui.cluster.dialog.SlaveServerDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonSlave;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.spoon.TabMapEntry.ObjectType;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

public class SpoonSlaveDelegate extends SpoonDelegate
{
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  
	public SpoonSlaveDelegate(Spoon spoon)
	{
		super(spoon);
	}


	public void addSpoonSlave(SlaveServer slaveServer)
	{
		TabSet tabfolder = spoon.tabfolder;

		// See if there is a SpoonSlave for this slaveServer...
		String tabName = spoon.delegates.tabs.makeSlaveTabName(slaveServer);
		TabMapEntry tabMapEntry = spoon.delegates.tabs.findTabMapEntry(tabName, ObjectType.SLAVE_SERVER);
		if (tabMapEntry == null)
		{
			SpoonSlave spoonSlave = new SpoonSlave(tabfolder.getSwtTabset(), SWT.NONE, spoon, slaveServer);
			TabItem tabItem = new TabItem(tabfolder, tabName, tabName);
			tabItem.setToolTipText("Status of slave server : " + slaveServer.getName() + " : " + slaveServer.getServerAndPort());
			tabItem.setControl(spoonSlave);

			tabMapEntry = new TabMapEntry(tabItem, null, tabName, null, null, spoonSlave, ObjectType.SLAVE_SERVER);
			spoon.delegates.tabs.addTab(tabMapEntry);
		}
		int idx = tabfolder.indexOf(tabMapEntry.getTabItem());
		tabfolder.setSelected(idx);
	}

	public void delSlaveServer(HasSlaveServersInterface hasSlaveServersInterface, SlaveServer slaveServer) throws KettleException
	{

		Repository rep = spoon.getRepository();

		if (rep != null && slaveServer.getObjectId() != null)
		{
			// remove the slave server from the repository too...
			rep.deleteSlave(slaveServer.getObjectId());
		}

		int idx = hasSlaveServersInterface.getSlaveServers().indexOf(slaveServer);
		hasSlaveServersInterface.getSlaveServers().remove(idx);
		spoon.refreshTree();

	}

	public void newSlaveServer(HasSlaveServersInterface hasSlaveServersInterface)
	{
		SlaveServer slaveServer = new SlaveServer();

		SlaveServerDialog dialog = new SlaveServerDialog(spoon.getShell(), slaveServer);
		if (dialog.open())
		{
			hasSlaveServersInterface.getSlaveServers().add(slaveServer);
			
      if (spoon.rep!=null) {
        try {
          if (!spoon.rep.getSecurityProvider().isReadOnly()) {
            spoon.rep.save(slaveServer, Const.VERSION_COMMENT_INITIAL_VERSION, null);
          } else {
            throw new KettleException(BaseMessages.getString(PKG, "Spoon.Dialog.Exception.ReadOnlyRepositoryUser"));
          }
        } catch (KettleException e) {
          new ErrorDialog(spoon.getShell(), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingSlave.Title"), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingSlave.Message", slaveServer.getName()), e);
        }
      }
			
			spoon.refreshTree();
		}
	}
}
