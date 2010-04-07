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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class takes care of crawling through the source code
 * 
 * @author matt
 * 
 */
public class MessagesSourceCrawler {

	private String scanPhrases[];

	/**
	 * The source directories to crawl through
	 */
	private List<String> sourceDirectories;

	/**
	 * The key occurrences, sorted by
	 */
	private List<KeyOccurrence> occurrences;
	
	/**
	 * A map between the package name and all the occurrences in there *
	 */
	private Map<String, List<KeyOccurrence>> packageOccurrences;

	/**
	 * The file names to avoid (base names)
	 */
	private List<String> filesToAvoid;

	private String singleMessagesFile;

	/**
	 * The folders with XML files to scan for keys in
	 */
	private List<SourceCrawlerXMLFolder> xmlFolders;
	
	
	private Pattern packagePattern;
	private Pattern importPattern;
	private Pattern importMessagesPattern;
	private Pattern stringPkgPattern;
	private Pattern	classPkgPattern;

	private LogChannelInterface	log;

	/**
	 * @param sourceDirectories
	 *            The source directories to crawl through
	 * @param singleMessagesFile
	 *            the messages file if there is only one, otherwise: null
	 */
	public MessagesSourceCrawler(LogChannelInterface log, List<String> sourceDirectories, String singleMessagesFile, List<SourceCrawlerXMLFolder> xmlFolders) {
		super();
		this.log = log;
		this.sourceDirectories = sourceDirectories;
		this.singleMessagesFile = singleMessagesFile;
		this.occurrences = new ArrayList<KeyOccurrence>();
		this.filesToAvoid = new ArrayList<String>();
		this.xmlFolders = xmlFolders;
		
		this.packageOccurrences = new Hashtable<String, List<KeyOccurrence>>();
		
		packagePattern = Pattern.compile("^\\s*package .*;[ \t]*$");
		importPattern = Pattern.compile("^\\s*import [a-z\\._0-9]*\\.[A-Z].*;[ \t]*$");
		importMessagesPattern = Pattern.compile("^\\s*import [a-z\\._0-9]*\\.Messages;[ \t]*$");
		stringPkgPattern = Pattern.compile("^.*private static String PKG.*=.*$");
		classPkgPattern = Pattern.compile("^.*private static Class.*\\sPKG\\s*=.*$");
	}

	/**
	 * @return The source directories to crawl through
	 */
	public List<String> getSourceDirectories() {
		return sourceDirectories;
	}

	/**
	 * @param sourceDirectories
	 *            The source directories to crawl through
	 */
	public void setSourceDirectories(List<String> sourceDirectories) {
		this.sourceDirectories = sourceDirectories;
	}

	/**
	 * @return the occurrences
	 */
	public List<KeyOccurrence> getOccurrences() {
		return occurrences;
	}

	/**
	 * @param occurrences
	 *            the occurrences to set
	 */
	public void setOccurrences(List<KeyOccurrence> occurrences) {
		this.occurrences = occurrences;
	}

	/**
	 * @return the files to avoid
	 */
	public List<String> getFilesToAvoid() {
		return filesToAvoid;
	}

	/**
	 * @param filesToAvoid
	 *            the files to avoid
	 */
	public void setFilesToAvoid(List<String> filesToAvoid) {
		this.filesToAvoid = filesToAvoid;
	}

	/**
	 * Add a key occurrence to the list of occurrences. The list is kept sorted
	 * on key and message package. If the key already exists, we increment the
	 * number of occurrences.
	 * 
	 * @param occ
	 *            The key occurrence to add
	 */
	public void addKeyOccurrence(KeyOccurrence occ) {
		int index = Collections.binarySearch(occurrences, occ);
		if (index < 0) {
			// Add it to the list, keep it sorted...
			//
			occurrences.add(-index - 1, occ);
			
			// Also add it to the packages occurrences map...
			//
			List<KeyOccurrence> list = packageOccurrences.get(occ.getMessagesPackage());
			if (list==null) {
				list = new ArrayList<KeyOccurrence>();
				packageOccurrences.put(occ.getMessagesPackage(), list);
			}
			list.add(occ);
		} else {
			KeyOccurrence keyOccurrence = occurrences.get(index);
			keyOccurrence.incrementOccurrences();
		}
	}

	public void crawl() throws Exception {
		String[] dirs = new String[sourceDirectories.size()];
		String[] masks = new String[sourceDirectories.size()];
		String[] req = new String[sourceDirectories.size()];
		boolean[] subdirs = new boolean[sourceDirectories.size()];

		for (int i = 0; i < masks.length; i++) {
			dirs[i] = sourceDirectories.get(i);
			masks[i] = ".*\\.java$";
			req[i] = "N";
			subdirs[i] = true;
		}
		FileInputList fileInputList = FileInputList.createFileList(
				new Variables(), dirs, masks, req, subdirs);

		/**
		 * We don't want the Messages.java files, there is nothing in there for
		 * us.
		 */
		for (FileObject fileObject : new ArrayList<FileObject>(fileInputList
				.getFiles())) {
			for (String filename : filesToAvoid) {
				if (fileObject.getName().getBaseName().equals(filename)) {
					fileInputList.getFiles().remove(fileObject);
				}
			}
		}

		for (FileObject fileObject : fileInputList.getFiles()) {

			// For each of these files we look for keys...
			//
			lookForOccurrencesInFile(fileObject);
		}

		// Also search for keys in the XUL files...
		//
		for (SourceCrawlerXMLFolder xmlFolder : xmlFolders) {
			String[] xmlDirs = { xmlFolder.getFolder(), };
			String[] xmlMasks = { xmlFolder.getWildcard(), };
			String[] xmlReq = { "N", };
			boolean[] xmlSubdirs = { true, }; // search sub-folders too

			FileInputList xulFileInputList = FileInputList.createFileList(
					new Variables(), xmlDirs, xmlMasks, xmlReq, xmlSubdirs);
			for (FileObject fileObject : xulFileInputList.getFiles()) {
				try {
					Document doc = XMLHandler.loadXMLFile(fileObject);

					// Scan for elements and tags in this file...
					//
					for (SourceCrawlerXMLElement xmlElement : xmlFolder.getElements()) {
						addLabelOccurrences(
								fileObject, 
								doc.getElementsByTagName(xmlElement.getSearchElement()), 
								xmlFolder.getKeyPrefix(), 
								xmlElement.getKeyTag(),
								xmlElement.getKeyAttribute(), 
								xmlFolder.getDefaultPackage(), 
								xmlFolder.getPackageExceptions()
							);
					}
				} catch (KettleXMLException e) {
					log.logError("Unable to open XUL / XML document: " + fileObject);
				}
			}
		}
	}

	private void addLabelOccurrences(FileObject fileObject, NodeList nodeList,
			String keyPrefix, String tag, String attribute,
			String defaultPackage,
			List<SourceCrawlerPackageException> packageExcpeptions)
			throws Exception {
		if (nodeList == null)
			return;

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String labelString = null;

			if (!Const.isEmpty(attribute)) {
				labelString = XMLHandler.getTagAttribute(node, attribute);
			} else if (!Const.isEmpty(tag)) {
				labelString = XMLHandler.getTagValue(node, tag);
			}

			if (labelString != null && labelString.startsWith(keyPrefix)) {
				String key = labelString.substring(1);

				String messagesPackage = defaultPackage;
				for (SourceCrawlerPackageException packageException : packageExcpeptions) {
					if (key.startsWith(packageException.getStartsWith()))
						messagesPackage = packageException.getPackageName();
				}

				StringWriter bodyXML = new StringWriter();
				transformer.transform(new DOMSource(node), new StreamResult(
						bodyXML));
				String xml = bodyXML.getBuffer().toString();

				KeyOccurrence keyOccurrence = new KeyOccurrence(fileObject,
						messagesPackage, -1, -1, key, "?", xml);
				if (!occurrences.contains(keyOccurrence)) {
					occurrences.add(keyOccurrence);
				}
			}
		}
	}

	/**
	 * Look for additional occurrences of keys in the specified file.
	 * 
	 * @param fileObject
	 *            The java source file to examine
	 * @throws IOException
	 *             In case there is a problem accessing the specified source
	 *             file.
	 */
	public void lookForOccurrencesInFile(FileObject fileObject)
			throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				KettleVFS.getInputStream(fileObject)));

		String messagesPackage = null;
		int row = 0;
		String classPackage = null;
		
		Map<String, String> importedClasses = new Hashtable<String, String>(); // Remember the imports we do...
		
		String line = reader.readLine();
		while (line != null) {
			row++;
			String line2=line;
			boolean extraLine;
			do {
				extraLine = false;
				for (String scanPhrase : scanPhrases) {
					if (line2.endsWith(scanPhrase)) {
						extraLine=true;
						break;
					}
				}
				if (extraLine) {
					line2 = reader.readLine();
					line+=line2;
				}
			} while (extraLine);
			
			// Examine the line...

			// What we first look for is the import of the messages package.
			//
			// "package org.pentaho.di.trans.steps.sortedmerge;"
			//
			if (packagePattern.matcher(line).matches()) {
				int beginIndex = line.indexOf("org.pentaho.");
				int endIndex = line.indexOf(';');
				if (beginIndex>=0 && endIndex>=0) {
					messagesPackage = line.substring(beginIndex, endIndex); // this is the default
					classPackage = messagesPackage;
				}
			}
			
			// Remember all the imports...
			//
			if (importPattern.matcher(line).matches()) {
				int beginIndex = line.indexOf("import")+"import".length()+1;
				int endIndex = line.indexOf(";", beginIndex);
				String expression = line.substring(beginIndex, endIndex);
				// The last word is the Class imported...
				// If it's * we ignore it.
				//
				int lastDotIndex = expression.lastIndexOf('.');
				if (lastDotIndex>0) {
					String packageName = expression.substring(0, lastDotIndex);
					String className = expression.substring(lastDotIndex+1);
					if (!"*".equals(className)) {
						importedClasses.put(className, packageName);
					}
				}
			}

			// This is the alternative location of the messages package:
			//
			// "import org.pentaho.di.trans.steps.sortedmerge.Messages;"
			//
			if (importMessagesPattern.matcher(line).matches()) {
				int beginIndex = line.indexOf("org.pentaho.");
				int endIndex = line.indexOf(".Messages;");
				messagesPackage = line.substring(beginIndex, endIndex); // if there is any specified, we take this one.
			}
			
			// Look for the value of the PKG value...
			//
			// 	private static String PKG = "org.pentaho.foo.bar.somepkg";
			//
			if (stringPkgPattern.matcher(line).matches()) {
				int beginIndex = line.indexOf('"')+1;
				int endIndex = line.indexOf('"', beginIndex);
				messagesPackage = line.substring(beginIndex, endIndex);   
			}
			
			// Look for the value of the PKG value as a fully qualified class...
			//
			// 	private static Class<?> PKG = Abort.class;
			// 
			if (classPackage!=null && classPkgPattern.matcher(line).matches()) {

				int fromIndex=line.indexOf('=')+1;
				int toIndex=line.indexOf(".class", fromIndex);
				String expression = Const.trim(line.substring(fromIndex, toIndex));
				// System.out.println("expression : "+expression);
				
				// If the expression doesn't contain any package, we'll look up the package in the imports.  If not found there, it's a local package.
				//
				if (expression.contains(".")) {
					int lastDotIndex = expression.lastIndexOf('.');
					messagesPackage = expression.substring(0, lastDotIndex);
				} else {
					String packageName = importedClasses.get(expression);
					if (packageName==null) {
						messagesPackage = classPackage; // Local package
					} else {
						messagesPackage = packageName; // imported
					}
				}
				
			}
			

			// Now look for occurrences of "Messages.getString(", "BaseMessages.getString(PKG", ...
			//
			for (String scanPhrase : scanPhrases) {
				int index = line.indexOf(scanPhrase);
				while (index >= 0) {
					// see if there's a character [a-z][A-Z] before the search string...
					// Otherwise we're looking at BaseMessages.getString(), etc.
					//
					if (index==0 || (index>0 & !Character.isJavaIdentifierPart(line.charAt(index-1)))) {
						addLineOccurrence(fileObject, messagesPackage, line, row, index, scanPhrase);
					}
					index = line.indexOf(scanPhrase, index + 1);
				}
			}

			line = reader.readLine();
		}

		reader.close();
	}

	/**
	 * Extract the needed information from the line and the index on which
	 * Messages.getString() occurs.
	 * 
	 * @param fileObject
	 *            the file we're reading
	 * @param messagesPackage
	 *            the messages package
	 * @param line
	 *            the line
	 * @param row
	 *            the row number
	 * @param index
	 *            the index in the line on which "Messages.getString(" is
	 *            located.
	 */
	private void addLineOccurrence(FileObject fileObject,
			String messagesPackage, String line, int row, int index, String scanPhrase) {
		// Right after the "Messages.getString(" string is the key, quoted (")
		// until the next comma...
		//
		int column = index + scanPhrase.length();
		String arguments = "";

		// we start at the double quote...
		//
		int startKeyIndex = line.indexOf('"', column) + 1;
		int endKeyIndex = line.indexOf('"', startKeyIndex);

		String key;
		if (endKeyIndex >= 0) {
			key = line.substring(startKeyIndex, endKeyIndex);

			// Can we also determine the arguments?
			// No, not always: only if the arguments are all on the same line.
			// 

			// Look for the next closing bracket...
			//
			int bracketIndex = endKeyIndex;
			int nrOpen = 1;
			while (bracketIndex < line.length() && nrOpen != 0) {
				int c = line.charAt(bracketIndex);
				if (c == '(')
					nrOpen++;
				if (c == ')')
					nrOpen--;
				bracketIndex++;
			}

			if (bracketIndex + 1 < line.length()) {
				arguments = line.substring(endKeyIndex + 1, bracketIndex);
			} else {
				arguments = line.substring(endKeyIndex + 1);
			}

		} else {
			key = line.substring(startKeyIndex);
		}

		// Sanity check...
		//
		if (key.contains("\t") || key.contains(" ")) {
			System.out.println("Suspect key found: [" + key + "] in file ["
					+ fileObject + "]");
		}

		// OK, add the occurrence to the list...
		//
		// Make sure we pass the System key occurrences to the correct package.
		//
		if (key.startsWith("System.")) {
			String i18nPackage = BaseMessages.class.getPackage().getName();
			KeyOccurrence keyOccurrence = new KeyOccurrence(fileObject,
					i18nPackage, row, column, key, arguments, line);

			// If we just add this key, we'll get doubles in the i18n package
			//
			KeyOccurrence lookup = getKeyOccurrence(key, i18nPackage);
			if (lookup == null) {
				addKeyOccurrence(keyOccurrence);
			} else {
				// Adjust the line of code...
				//
				lookup.setSourceLine(lookup.getSourceLine() + Const.CR
						+ keyOccurrence.getSourceLine());
				lookup.incrementOccurrences();
			}
		} else {
			KeyOccurrence keyOccurrence = new KeyOccurrence(fileObject,
					messagesPackage, row, column, key, arguments, line);
			addKeyOccurrence(keyOccurrence);
		}
	}

	/**
	 * @return A sorted list of distinct occurrences of the used message package
	 *         names
	 */
	public List<String> getMessagesPackagesList() {
		Map<String, String> table = new Hashtable<String, String>();

		for (KeyOccurrence keyOccurrence : occurrences) {
			table.put(keyOccurrence.getMessagesPackage(), keyOccurrence
					.getMessagesPackage());
		}

		List<String> list = new ArrayList<String>(table.keySet());
		Collections.sort(list);

		return list;
	}

	/**
	 * Get all the key occurrences for a certain messsages package.
	 * 
	 * @param messagesPackage
	 *            the package to hunt for
	 * @return all the key occurrences for a certain messages package.
	 */
	public List<KeyOccurrence> getOccurrencesForPackage(String messagesPackage) {
		List<KeyOccurrence> list = new ArrayList<KeyOccurrence>();
		for (KeyOccurrence keyOccurrence : occurrences) {
			if (keyOccurrence.getMessagesPackage().equals(messagesPackage)) {
				list.add(keyOccurrence);
			}
		}

		return list;
	}

	public static void main(String[] args) throws Exception {
		List<String> directories = new ArrayList<String>();
		directories.add("src-core");
		directories.add("src");
		directories.add("src-ui");

		List<String> filesToAvoid = new ArrayList<String>();
		filesToAvoid.add("MessagesSourceCrawler.java");
		filesToAvoid.add("KeyOccurence.java");
		filesToAvoid.add("TransLator.java");
		filesToAvoid.add("MenuHelper.java");
		filesToAvoid.add("Messages.java");
		filesToAvoid.add("XulMessages.java");
		filesToAvoid.add("AnnotatedStepsConfigManager.java");
		filesToAvoid.add("AnnotatedJobConfigManager.java");
		filesToAvoid.add("JobEntryValidatorUtils.java");
		filesToAvoid.add("Const.java");
		filesToAvoid.add("XulHelper.java");

		List<SourceCrawlerXMLFolder> xmlFolders = new ArrayList<SourceCrawlerXMLFolder>();
		SourceCrawlerXMLFolder xmlFolder = new SourceCrawlerXMLFolder("ui",
				".*\\.xul$", "%");
		xmlFolder.getElements().add(
				new SourceCrawlerXMLElement("menu", null, "label"));
		xmlFolder.getElements().add(
				new SourceCrawlerXMLElement("menuitem", null, "label"));
		xmlFolder.getElements().add(
				new SourceCrawlerXMLElement("toolbar", null, "label"));
		xmlFolder.getElements().add(
				new SourceCrawlerXMLElement("toolbarbutton", null, "label"));
		xmlFolders.add(xmlFolder);

		MessagesSourceCrawler crawler = new MessagesSourceCrawler(new LogChannel("Source crawler"), directories, null, xmlFolders);
		crawler.setFilesToAvoid(filesToAvoid);
		crawler.crawl();
		int mis = 0;
		LanguageChoice.getInstance().setDefaultLocale(Locale.US);
		for (KeyOccurrence occ : crawler.getOccurrences()) {

			// Try to get a value attached to each of these >6k occurrences...
			//
			String translation = BaseMessages.getString(occ
					.getMessagesPackage(), occ.getKey());

			if (translation.startsWith("!")) {
				mis++;
				System.out.println(mis + "\t" + occ.getKey() + "\t"
						+ occ.getRow() + "\t" + occ.getMessagesPackage() + "\t"
						+ occ.getFileObject().getName().getBaseName() + "\t"
						+ occ.getFileObject().getParent());
			}
		}
		System.out.println("-------------------------------------------------");
		System.out.println("Found " + crawler.getOccurrences().size());
		System.out.println("-------------------------------------------------");

		List<String> packageNames = crawler.getMessagesPackagesList();

		System.out.println("Packages found : " + packageNames.size());
		/*
		 * for (String packageName : packageNames) {
		 * System.out.println("["+packageName+"]"); }
		 */
	}

	public KeyOccurrence getKeyOccurrence(String key,
			String selectedMessagesPackage) {
		for (KeyOccurrence keyOccurrence : occurrences) {
			if (keyOccurrence.getKey().equals(key)
					&& keyOccurrence.getMessagesPackage().equals(
							selectedMessagesPackage)) {
				return keyOccurrence;
			}
		}
		return null;
	}

	/**
	 * @return the singleMessagesFile
	 */
	public String getSingleMessagesFile() {
		return singleMessagesFile;
	}

	/**
	 * @param singleMessagesFile
	 *            the singleMessagesFile to set
	 */
	public void setSingleMessagesFile(String singleMessagesFile) {
		this.singleMessagesFile = singleMessagesFile;
	}

	/**
	 * @return the scanPhrases
	 */
	public String[] getScanPhrases() {
		return scanPhrases;
	}

	/**
	 * @param scanPhrases the scanPhrases to set
	 */
	public void setScanPhrases(String[] scanPhrases) {
		this.scanPhrases = scanPhrases;
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
