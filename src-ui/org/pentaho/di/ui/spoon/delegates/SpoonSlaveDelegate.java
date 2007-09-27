package org.pentaho.di.ui.spoon.delegates;

import org.eclipse.swt.SWT;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;
import org.pentaho.di.trans.HasSlaveServersInterface;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.cluster.dialog.SlaveServerDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonSlave;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.www.AddJobServlet;
import org.pentaho.di.www.AddTransServlet;
import org.pentaho.di.www.PrepareExecutionTransServlet;
import org.pentaho.di.www.StartExecutionTransServlet;
import org.pentaho.di.www.StartJobServlet;
import org.pentaho.di.www.WebResult;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

public class SpoonSlaveDelegate extends SpoonDelegate
{
	public SpoonSlaveDelegate(Spoon spoon)
	{
		super(spoon);
	}

	public void sendXMLToSlaveServer(TransMeta transMeta, TransExecutionConfiguration executionConfiguration) throws KettleException
	{
		SlaveServer slaveServer = executionConfiguration.getRemoteServer();

		if (slaveServer == null)
			throw new KettleException("No slave server specified");
		if (Const.isEmpty(transMeta.getName()))
			throw new KettleException(
					"The transformation needs a name to uniquely identify it by on the remote server.");

		String xml = new TransConfiguration(transMeta, executionConfiguration).getXML();
		try
		{
			String reply = slaveServer.sendXML(xml, AddTransServlet.CONTEXT_PATH + "/?xml=Y");
			WebResult webResult = WebResult.fromXMLString(reply);
			if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
			{
				throw new KettleException(
						"There was an error posting the transformation on the remote server: " + Const.CR
								+ webResult.getMessage());
			}

			reply = slaveServer.getContentFromServer(PrepareExecutionTransServlet.CONTEXT_PATH + "/?name="
					+ transMeta.getName() + "&xml=Y");
			webResult = WebResult.fromXMLString(reply);
			if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
			{
				throw new KettleException(
						"There was an error preparing the transformation for excution on the remote server: "
								+ Const.CR + webResult.getMessage());
			}

			reply = slaveServer.getContentFromServer(StartExecutionTransServlet.CONTEXT_PATH + "/?name="
					+ transMeta.getName() + "&xml=Y");
			webResult = WebResult.fromXMLString(reply);

			if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
			{
				throw new KettleException(
						"There was an error starting the transformation on the remote server: " + Const.CR
								+ webResult.getMessage());
			}
		} catch (Exception e)
		{
			throw new KettleException(e);
		}

	}

	public void addSpoonSlave(SlaveServer slaveServer)
	{
		TabSet tabfolder = spoon.tabfolder;

		// See if there is a SpoonSlave for this slaveServer...
		String tabName = spoon.delegates.tabs.makeSlaveTabName(slaveServer);
		TabItem tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_SLAVE_SERVER);
		if (tabItem == null)
		{
			SpoonSlave spoonSlave = new SpoonSlave(tabfolder.getSwtTabset(), SWT.NONE, spoon, slaveServer);
			tabItem = new TabItem(tabfolder, tabName, tabName);
			tabItem.setToolTipText("Status of slave server : " + slaveServer.getName() + " : "
					+ slaveServer.getServerAndPort());
			tabItem.setControl(spoonSlave);

			spoon.delegates.tabs.addTab(tabName, new TabMapEntry(tabItem, tabName, spoonSlave,
					TabMapEntry.OBJECT_TYPE_SLAVE_SERVER));
		}
		int idx = tabfolder.indexOf(tabItem);
		tabfolder.setSelected(idx);
	}

	public void delSlaveServer(HasSlaveServersInterface hasSlaveServersInterface, SlaveServer slaveServer) throws KettleException
	{

		Repository rep = spoon.getRepository();

		if (rep != null && slaveServer.getId() > 0)
		{
			// remove the slave server from the repository too...
			rep.delSlave(slaveServer.getId());
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
			spoon.refreshTree();
		}
	}
}
