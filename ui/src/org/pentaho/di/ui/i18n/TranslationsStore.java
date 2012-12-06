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

package org.pentaho.di.ui.i18n;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

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

	private Map<String, List<KeyOccurrence>> packageOccurrences;

	private LogChannelInterface	log;
	
	/**
	 * @param localeList
	 * @param messagesPackages
	 * @param map 
	 */
	public TranslationsStore(LogChannelInterface log, List<String> localeList, List<String> messagesPackages, String mainLocale, Map<String, List<KeyOccurrence>> packageOccurrences) {
		super();
		this.log = log;
		this.localeList = localeList;
		this.messagesPackages = messagesPackages;
		this.mainLocale = mainLocale;
		this.packageOccurrences = packageOccurrences;
		
		translationsMap = new Hashtable<String, LocaleStore>();
	}
	
	/**
	 * Read all the translated messages for all the specified locale and all the specified locale
	 * @param directories The reference source directories to search packages in
	 * @throws KettleException
	 */
	public void read(List<String> directories) throws KettleException {
		
		// The first locale (en_US) takes the lead: we need to find all of those 
		// The others are optional.
		
		for (String locale : localeList) {
			LocaleStore localeStore = new LocaleStore(log, locale, messagesPackages, mainLocale, packageOccurrences);
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
			localeStore = new LocaleStore(log, locale, messagesPackages, mainLocale, packageOccurrences);
			translationsMap.put(locale, localeStore);
		}
		MessagesStore messagesStore = localeStore.getLocaleMap().get(messagesPackage);
		if (messagesStore==null) {
			messagesStore=new MessagesStore(locale, messagesPackage, packageOccurrences);
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

	/**
	 * @return the packageOccurrences
	 */
	public Map<String, List<KeyOccurrence>> getPackageOccurrences() {
		return packageOccurrences;
	}

	/**
	 * @param packageOccurrences the packageOccurrences to set
	 */
	public void setPackageOccurrences(Map<String, List<KeyOccurrence>> packageOccurrences) {
		this.packageOccurrences = packageOccurrences;
	}
	
}
