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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;

/**
 * This class contains and handles all the translations for the keys specified in the Java source code.
 * 
 * @author matt
 *
 */
public class TranslationsStore {
	private List<String> localeList;
	
	private List<String> messagesPackages;
	
	private Map<String, LocaleStore> translationsMap; 
	
	private String mainLocale;
	
	/**
	 * @param localeList
	 * @param messagesPackages
	 */
	public TranslationsStore(List<String> localeList, List<String> messagesPackages, String mainLocale) {
		super();
		this.localeList = localeList;
		this.messagesPackages = messagesPackages;
		this.mainLocale = mainLocale;
		
		translationsMap = new Hashtable<String, LocaleStore>();
	}
	
	/**
	 * Read all the translated messages for all the specified locale and all the specified locale
	 * @param directories The reference source directories to search packages in
	 * @throws KettleException
	 */
	public void read(String[] directories) throws KettleException {
		
		// The first locale (en_US) takes the lead: we need to find all of those 
		// The others are optional.
		
		for (String locale : localeList) {
			LocaleStore localeStore = new LocaleStore(locale, messagesPackages, mainLocale);
			localeStore.read(directories);
			
			translationsMap.put(locale, localeStore);
		}
	}

	/**
	 * @return the localeList
	 */
	public List<String> getLocaleList() {
		return localeList;
	}

	/**
	 * @param localeList the localeList to set
	 */
	public void setLocaleList(List<String> localeList) {
		this.localeList = localeList;
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
	 * @return the translationsMap
	 */
	public Map<String, LocaleStore> getTranslationsMap() {
		return translationsMap;
	}

	/**
	 * @param translationsMap the translationsMap to set
	 */
	public void setTranslationsMap(Map<String, LocaleStore> translationsMap) {
		this.translationsMap = translationsMap;
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
	 * Look up the translation for a key in a certain locale
	 * @param locale the locale to hunt for
	 * @param messagesPackage the messages package to look in
	 * @param key the key
	 * @return the translation for the specified key in the desired locale, from the requested package
	 */
	public String lookupKeyValue(String locale, String messagesPackage, String key) {
		LocaleStore localeStore = translationsMap.get(locale);
		if (localeStore!=null) {
			MessagesStore messagesStore = localeStore.getLocaleMap().get(messagesPackage);
			if (messagesStore!=null) {
				return messagesStore.getMessagesMap().get(key);
			}
		}
		return null;
	}

	public void removeValue(String locale, String messagesPackage, String key)  {
		LocaleStore localeStore = translationsMap.get(locale);
		if (localeStore!=null) {
			MessagesStore messagesStore = localeStore.getLocaleMap().get(messagesPackage);
			if (messagesStore!=null) {
				messagesStore.getMessagesMap().remove(key);
				messagesStore.setChanged();
			}
		}
	}

	public void storeValue(String locale, String messagesPackage, String key, String value) {
		LocaleStore localeStore = translationsMap.get(locale);
		if (localeStore==null) {
			localeStore = new LocaleStore(locale, messagesPackages, mainLocale);
			translationsMap.put(locale, localeStore);
		}
		MessagesStore messagesStore = localeStore.getLocaleMap().get(messagesPackage);
		if (messagesStore==null) {
			messagesStore=new MessagesStore(locale, messagesPackage);
			localeStore.getLocaleMap().put(messagesPackage, messagesStore);
		}
		messagesStore.getMessagesMap().put(key, value);
		messagesStore.setChanged();
	}
	
	/**
	 * @return the list of changed messages stores.
	 */
	public List<MessagesStore> getChangedMessagesStores() {
		List<MessagesStore> list = new ArrayList<MessagesStore>();
		
		for(LocaleStore localeStore : translationsMap.values()) {
			for (MessagesStore messagesStore : localeStore.getLocaleMap().values()) {
				if (messagesStore.hasChanged()) {
					list.add(messagesStore);
				}
			}
		}
		
		return list;
	}
	
	
	/**
	 * @param searchLocale the locale the filter on.
	 * @param messagesPackage the messagesPackage to filter on.  Specify null to get all message stores.
	 * @return the list of messages stores for the main locale
	 */
	public List<MessagesStore> getMessagesStores(String searchLocale, String messagesPackage) {
		List<MessagesStore> list = new ArrayList<MessagesStore>();
		
		for(LocaleStore localeStore : translationsMap.values()) {
			for (MessagesStore messagesStore : localeStore.getLocaleMap().values()) {
				if (messagesStore.getLocale().equals(searchLocale)) {
					if (messagesPackage==null || messagesStore.getMessagesPackage().equals(messagesPackage))
					{
						list.add(messagesStore);
					}
				}
			}
		}
		
		return list;
	}

	public MessagesStore findMainLocaleMessagesStore(String messagesPackage) {
		LocaleStore localeStore = translationsMap.get(mainLocale);
		return localeStore.getLocaleMap().get(messagesPackage);
	}
	
}
