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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class takes care of crawling through the source code
 * @author matt
 *
 */
public class MessagesSourceCrawler {
	
	private static final String MESSAGES_GETSTRING = "Messages.getString(";

	/**
	 * The source directories to crawl through
	 */
	private String[] sourceDirectories;
	
	/**
	 * The directories to search for XUL files in
	 */
	private String[] xulDirectories;
	
	
	/**
	 * The key occurrences, sorted by  
	 */
	private List<KeyOccurrence> occurrences;

	/**
	 * The file names to avoid (base names)
	 */
	private String[] filesToAvoid;
	

	/**
	 * @param sourceDirectories The source directories to crawl through
	 */
	public MessagesSourceCrawler(String[] sourceDirectories) {
		super();
		this.sourceDirectories = sourceDirectories;
		this.occurrences = new ArrayList<KeyOccurrence>();
		this.filesToAvoid = new String[] {};
		this.xulDirectories = new String[] {};
	}

	/**
	 * @return The source directories to crawl through
	 */
	public String[] getSourceDirectories() {
		return sourceDirectories;
	}

	/**
	 * @param sourceDirectories The source directories to crawl through
	 */
	public void setSourceDirectories(String[] sourceDirectories) {
		this.sourceDirectories = sourceDirectories;
	}
	

	/**
	 * @return the occurrences
	 */
	public List<KeyOccurrence> getOccurrences() {
		return occurrences;
	}

	/**
	 * @param occurrences the occurrences to set
	 */
	public void setOccurrences(List<KeyOccurrence> occurrences) {
		this.occurrences = occurrences;
	}
	
	/**
	 * @return the filesToAvoid
	 */
	public String[] getFilesToAvoid() {
		return filesToAvoid;
	}

	/**
	 * @param filesToAvoid the filesToAvoid to set
	 */
	public void setFilesToAvoid(String[] filesToAvoid) {
		this.filesToAvoid = filesToAvoid;
	}

	
	/**
	 * Add a key occurrence to the list of occurrences.  The list is kept sorted on key and message package.
	 * If the key already exists, we increment the number of occurrences.
	 * @param occ The key occurrence to add
	 */
	public void addKeyOccurrence(KeyOccurrence occ) {
		int index = Collections.binarySearch(occurrences, occ);
		if (index<0) {
			occurrences.add(-index-1, occ);
		} 
		else {
			KeyOccurrence keyOccurrence = occurrences.get(index);
			keyOccurrence.incrementOccurrences();
		}
	}
	
	public void crawl() throws IOException {
		String[] masks = new String[sourceDirectories.length];
		String[] req = new String[sourceDirectories.length];
		boolean[] subdirs = new boolean[sourceDirectories.length];
		
		for (int i=0;i<masks.length;i++) {
			masks[i] = ".*\\.java$";
			req[i] = "N";
			subdirs[i] = true;
		}
		FileInputList fileInputList = FileInputList.createFileList(new Variables(), sourceDirectories, masks, req, subdirs);
		
		/**
		 * We don't want the Messages.java files, there is nothing in there for us.
		 */
		for (FileObject fileObject : new ArrayList<FileObject>(fileInputList.getFiles())) {
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
		String[] xulMasks = new String[xulDirectories.length];
		String[] xulReq = new String[xulDirectories.length];
		boolean[] xulSubdirs = new boolean[xulDirectories.length];
		
		for (int i=0;i<xulMasks.length;i++) {
			xulMasks[i] = ".*\\.xul$";
			xulReq[i] = "N";
			xulSubdirs[i] = true;
		}
		FileInputList xulFileInputList = FileInputList.createFileList(new Variables(), xulDirectories, xulMasks, xulReq, xulSubdirs);
		for (FileObject fileObject : xulFileInputList.getFiles()) {
			try {
				Document doc = XMLHandler.loadXMLFile(fileObject);
				
				// The menus...
				//
				addLabelOccurrences(fileObject, doc.getElementsByTagName("menu"));
				addLabelOccurrences(fileObject, doc.getElementsByTagName("menuitem"));
				addLabelOccurrences(fileObject, doc.getElementsByTagName("toolbar"));
				addLabelOccurrences(fileObject, doc.getElementsByTagName("toolbarbutton"));
			}
			catch(KettleXMLException e) {
				LogWriter.getInstance().logError(toString(), "Unable to open XUL / XML document: "+fileObject);
			}
		}
	}
	
	private void addLabelOccurrences(FileObject fileObject, NodeList nodeList) {
		if (nodeList==null) return;
		
		for (int i=0;i<nodeList.getLength();i++) {
			Node node = nodeList.item(i);
			String labelString = XMLHandler.getTagAttribute(node, "label");
			if (labelString!=null && labelString.startsWith("%")) {
				String key = labelString.substring(1);
				
				// TODO: figure out a way to do this cleaner...
				// 
				String messagesPackage = Spoon.class.getPackage().getName();
				if (key.startsWith("JobGraph.")) messagesPackage = JobGraph.class.getPackage().getName();
				
				KeyOccurrence keyOccurrence = new KeyOccurrence(fileObject, messagesPackage, -1, -1, key, "?", node.toString());
				occurrences.add(keyOccurrence);
			}
		}
	}

	/**
	 * Look for additional occurrences of keys in the specified file.
	 * @param fileObject The java source file to examine
	 * @throws IOException In case there is a problem accessing the specified source file.
	 */
	public void lookForOccurrencesInFile(FileObject fileObject) throws IOException {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(KettleVFS.getInputStream(fileObject)));
		
		String messagesPackage = null;
		int row=0;

		String line = reader.readLine();
		while (line!=null) {
			row++;
			
			// Examine the line...
			
			// What we first look for is the import of the messages package.
			//
			// "package org.pentaho.di.trans.steps.sortedmerge;"
			//
			if (line.matches("^[ \t]*package .*;[ \t]*$")) {
				int beginIndex = line.indexOf("org.pentaho.");
				int endIndex = line.indexOf(';');
				messagesPackage = line.substring(beginIndex, endIndex); // this is the default
			}
			
			// This is the alternative location of the messages package:
			//
			// "import org.pentaho.di.trans.steps.sortedmerge.Messages;"
			//
			if (line.matches("^[ \t]*import [a-z\\._]*\\.Messages;[ \t]*$")) {
				int beginIndex = line.indexOf("org.pentaho.");
				int endIndex = line.indexOf(".Messages;");
				messagesPackage = line.substring(beginIndex, endIndex); // if there is any specified, we take this one.
			}
			
			// Now look for occurrences of Messages.getString(
			//
			int index = line.indexOf(MESSAGES_GETSTRING);
			while (index>=0) {
				addLineOccurrence(fileObject, messagesPackage, line, row, index);
				index = line.indexOf(MESSAGES_GETSTRING, index+1);
			}
			
			line=reader.readLine();
		}
		
		reader.close();
	}

	/** 
	 * Extract the needed information from the line and the index on which Messages.getString() occurs.
	 * @param fileObject the file we're reading
	 * @param messagesPackage the messages package
	 * @param line the line
	 * @param row the row number
	 * @param index the index in the line on which "Messages.getString(" is located.
	 */
	private void addLineOccurrence(FileObject fileObject, String messagesPackage, String line, int row, int index) {
		// Right after the "Messages.getString(" string is the key, quoted (") until the next comma...
		//
		int column = index+MESSAGES_GETSTRING.length();
		String arguments = "";
		
		// we start at the double quote...
		//
		int startKeyIndex = line.indexOf('"', column)+1;
		int endKeyIndex = line.indexOf('"', startKeyIndex); 
		
		String key;
		if (endKeyIndex>=0) {
			key = line.substring(startKeyIndex, endKeyIndex);
			
			// Can we also determine the arguments?
			// No, not always: only if the arguments are all on the same line.
			// 
			
			// Look for the next closing bracket...
			//
			int bracketIndex = endKeyIndex;
			int nrOpen = 1;
			while (bracketIndex<line.length() && nrOpen!=0) {
				int c = line.charAt(bracketIndex);
				if (c=='(') nrOpen++;
				if (c==')') nrOpen--;
				bracketIndex++;
			}
			
			if (bracketIndex+1<line.length()) {
				arguments = line.substring(endKeyIndex+1, bracketIndex);
			} else {
				arguments = line.substring(endKeyIndex+1);
			}

		} else {
			key = line.substring(startKeyIndex);
		}
		
		// Sanity check...
		//
		if (key.contains("\t") || key.contains(" ")) {
			System.out.println("Suspect key found: ["+key+"] in file ["+fileObject+"]");
		}
				
		// OK, add the occurrence to the list...
		//
		// Make sure we pass the System key occurrences to the correct package.
		//
		if (key.startsWith("System.")) {
			String i18nPackage = BaseMessages.class.getPackage().getName();
			KeyOccurrence keyOccurrence = new KeyOccurrence(fileObject, i18nPackage, row, column, key, arguments, line);
			
			// If we just add this key, we'll get doubles in the i18n package
			//
			KeyOccurrence lookup = getKeyOccurrence(key, i18nPackage);
			if (lookup==null) {
				addKeyOccurrence(keyOccurrence);
			} else {
				// Adjust the line of code...
				//
				lookup.setSourceLine(lookup.getSourceLine()+Const.CR+keyOccurrence.getSourceLine());
				lookup.incrementOccurrences();
			}
		} else {
			KeyOccurrence keyOccurrence = new KeyOccurrence(fileObject, messagesPackage, row, column, key, arguments, line);
			addKeyOccurrence(keyOccurrence);
		}
	}
	
	/**
	 * @return A sorted list of distinct occurrences of the used message package names
	 */
	public List<String> getMessagesPackagesList() {
		Map<String, String> table = new Hashtable<String, String>();
		
		for (KeyOccurrence keyOccurrence : occurrences) {
			table.put(keyOccurrence.getMessagesPackage(), keyOccurrence.getMessagesPackage());
		}
		
		List<String> list = new ArrayList<String>( table.keySet() );
		Collections.sort(list);
		
		return list;
	}
	
	/**
	 * Get all the key occurrences for a certain messsages package. 
	 * @param messagesPackage the package to hunt for
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

	public static void main(String[] args) throws IOException {
		MessagesSourceCrawler crawler = new MessagesSourceCrawler(new String[] { "src", "src-ui", } );
		crawler.setFilesToAvoid(
				new String[] { 
						"MessagesSourceCrawler.java", "KeyOccurence.java", "TransLator.java", 
						"MenuHelper.java", "Messages.java", "XulMessages.java", 
						"AnnotatedStepsConfigManager.java", "AnnotatedJobConfigManager.java", 
						"JobEntryValidatorUtils.java", "Const.java", "XulHelper.java", 
					});
		crawler.crawl();
		int mis=0;
		LanguageChoice.getInstance().setDefaultLocale(Locale.US);
		for (KeyOccurrence occ : crawler.getOccurrences()) {
			
			// Try to get a value attached to each of these >6k occurrences...
			//
			String translation = BaseMessages.getString(occ.getMessagesPackage(), occ.getKey());
			
			if (translation.startsWith("!")) {
				mis++;
				System.out.println(mis+"\t"+occ.getKey()+"\t"+occ.getRow()+"\t"+occ.getMessagesPackage()+"\t"+occ.getFileObject().getName().getBaseName()+"\t"+occ.getFileObject().getParent());
			}
		}
		System.out.println("-------------------------------------------------");
		System.out.println("Found "+crawler.getOccurrences().size());
		System.out.println("-------------------------------------------------");
		
		List<String> packageNames = crawler.getMessagesPackagesList();

		System.out.println("Packages found : "+packageNames.size());
		/*
		for (String packageName : packageNames) {
			System.out.println("["+packageName+"]");
		}
		*/
	}

	public String[] getXulDirectories() {
		return xulDirectories;
	}

	public void setXulDirectories(String[] xulDirectories) {
		this.xulDirectories = xulDirectories;
	}

	public KeyOccurrence getKeyOccurrence(String key, String selectedMessagesPackage) {
		for (KeyOccurrence keyOccurrence : occurrences) {
			if (keyOccurrence.getKey().equals(key) && keyOccurrence.getMessagesPackage().equals(selectedMessagesPackage)) {
				return keyOccurrence;
			}
		}
		return null;
	}

}
