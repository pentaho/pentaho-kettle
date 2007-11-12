/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.core.gui;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.xul.Messages;
import org.pentaho.xul.swt.menu.Menu;
import org.pentaho.xul.swt.menu.MenuBar;
import org.pentaho.xul.swt.menu.MenuHelper;
import org.pentaho.xul.swt.toolbar.Toolbar;
import org.w3c.dom.Document;

public class XulHelper
{
	private XulHelper()
	{
	}

	public static MenuBar createMenuBar(String menuFile, Shell shell,Messages xulMessages) throws KettleException
	{

		// first get the XML document
		try
		{
			URL xulFile = getAndValidate(menuFile);
			Document doc = XMLHandler.loadXMLFile(xulFile);
			MenuBar menuBar = MenuHelper.createMenuBarFromXul(doc, shell, xulMessages);
			shell.setMenuBar((org.eclipse.swt.widgets.Menu) menuBar.getNativeObject());
			return menuBar;

		} catch (IOException e)
		{
			throw new KettleException(org.pentaho.di.ui.spoon.Messages.getString("Spoon.Exception.XULFileNotFound.Message", menuFile));
		}

	}

	public static URL getAndValidate(String url) throws KettleException, IOException
	{
		File file = new File(url);
		if (file.exists())
			return file.toURI().toURL();

		URL resource = Thread.currentThread().getContextClassLoader().getResource(url);

		if (resource == null)
			resource = XulHelper.class.getResource(url);

		if (resource == null)
			throw new KettleException(org.pentaho.di.ui.spoon.Messages.getString("Spoon.Exception.XULFileNotFound.Message", url));

		URLConnection conn = resource.openConnection();
		if (conn instanceof HttpURLConnection
				&& ((HttpURLConnection) conn).getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
			throw new KettleException(org.pentaho.di.ui.spoon.Messages.getString("Spoon.Exception.XULFileNotFound.Message", url));

		return resource;
	}

	public static Map<String, Menu> createPopupMenus(String menusFile, Shell shell,Messages xulMessages,List<String> ids)
			throws KettleException
	{
		try
		{
			URL xulFile = getAndValidate(menusFile); //$NON-NLS-1$

			Document doc = XMLHandler.loadXMLFile(xulFile);
			return MenuHelper.createPopupMenusFromXul(doc, shell, xulMessages, ids);
		} catch (IOException e)
		{
			throw new KettleException(e);
		}

	}

	public static Map<String, Menu> createPopupMenus(String menusFile, Shell shell,Messages xulMessages,String... ids)
			throws KettleException
	{
		return createPopupMenus(menusFile, shell, xulMessages,Arrays.asList(ids));
	}

	public static Toolbar createToolbar(String resource, Shell shell, Object caller,Messages xulMessages) throws KettleException
	{

		try
		{
			// first get the XML document
			URL xulFile = getAndValidate(resource); //$NON-NLS-1$

			Document doc = XMLHandler.loadXMLFile(xulFile);
			return MenuHelper.createToolbarFromXul(doc, shell, xulMessages, caller);
		} catch (IOException e)
		{
			throw new KettleException(e);
		}
	}

}
