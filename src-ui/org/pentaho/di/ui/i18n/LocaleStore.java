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
package org.pentaho.di.ui.i18n;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

/**
 * This class stores all the messages for a locale for all the used packages... 
 * 
 * @author matt
 *
 */
public class LocaleStore {
	
	/** The locale to handle */
	private String locale;
	
	/** The list of messages packages */
	private List<String> messagesPackages;

	private Map<String, MessagesStore> localeMap; 
	
	private String mainLocale;

	private Map<String, List<KeyOccurrence>> packageOccurrences;

	private LogChannelInterface	log;
	
	/**
	 * Create a new LocaleStore 
	 * @param locale The locale to handle
	 * @param messagesPackages the packages to handle
	 * @param packageOccurrences 
	 */
	public LocaleStore(LogChannelInterface log, String locale, List<String> messagesPackages, String mainLocale, Map<String, List<KeyOccurrence>> packageOccurrences) {
		this.log = log;
		this.locale = locale;
		this.messagesPackages = messagesPackages;
		this.mainLocale = mainLocale;
		localeMap = new Hashtable<String, MessagesStore>();
		this.packageOccurrences = packageOccurrences;
	}
	
	/**
	 * Read all the messages stores from the specified locale from all the specified packages
	 * @param directories The source directories to reference the packages from
	 * @throws KettleException
	 */
	public void read(List<String> directories) throws KettleException {
		for (String messagePackage : messagesPackages) {
			MessagesStore messagesStore = new MessagesStore(locale, messagePackage, packageOccurrences);
			
			try {
				messagesStore.read(directories);
				localeMap.put(messagePackage, messagesStore);
			}
			catch(Exception e) {
				if (locale.equals(mainLocale)) {
					throw new KettleException(e);
				}
				else {
					log.logDetailed("No translations found for locale '"+locale+"' in package '"+messagePackage+"'");
				}

			}
		}
	}

	/**
	 * @return the messagesPackages
	 */
	public List<String> getMessagesPackages() {
		return messagesPackages;
	}

	/**
	 * @param messagesPackages the messagesPackages to set
	 */
	public void setMessagesPackages(List<String> messagesPackages) {
		this.messagesPackages = messagesPackages;
	}

	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @return the mainLocale
	 */
	public String getMainLocale() {
		return mainLocale;
	}

	/**
	 * @param mainLocale the mainLocale to set
	 */
	public void setMainLocale(String mainLocale) {
		this.mainLocale = mainLocale;
	}

	/**
	 * @return the localeMap
	 */
	public Map<String, MessagesStore> getLocaleMap() {
		return localeMap;
	}

	/**
	 * @param localeMap the localeMap to set
	 */
	public void setLocaleMap(Map<String, MessagesStore> localeMap) {
		this.localeMap = localeMap;
	}

	/**
	 * @return the packagesOccurrences
	 */
	public Map<String, List<KeyOccurrence>> getPackagesOccurrences() {
		return packageOccurrences;
	}

	/**
	 * @param packagesOccurrences the packagesOccurrences to set
	 */
	public void setPackagesOccurrences(Map<String, List<KeyOccurrence>> packagesOccurrences) {
		this.packageOccurrences = packagesOccurrences;
	}
	
	
}
