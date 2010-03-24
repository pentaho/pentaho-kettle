/***** BEGIN LICENSE BLOCK *****
The contents of this package are subject to the GNU Lesser Public License
 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.gnu.org/licenses/lgpl-2.1.txt

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the
License.

The Original Code is Pentaho Data Integration

The Initial Developer of the Original Code is
Pentaho Corporation
Portions created by the Initial Developer are Copyright (C) 2010
the Initial Developer. All Rights Reserved.

Contributor(s):
Matt Casters mcaster@pentaho.com
Daniel Einspanjer deinspanjer@mozilla.com

 ***** END LICENSE BLOCK *****/

package org.pentaho.di.ui.trans.steps.userdefinedjavaclass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClass;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class UserDefinedJavaClassCodeSnippits {
	private static Class<?> PKG = UserDefinedJavaClass.class;
	private static UserDefinedJavaClassCodeSnippits snippitsHelper = null;
	
	private final List<Snippit> snippits = new ArrayList<Snippit>();
	private final Map<String, Snippit> snippitsMap = new HashMap<String, Snippit>();
	private final LogChannel log = new LogChannel("UserDefinedJavaClassCodeSnippits");

	public static synchronized UserDefinedJavaClassCodeSnippits getSnippitsHelper() throws KettleXMLException {
		if (snippitsHelper == null) {
			snippitsHelper = new UserDefinedJavaClassCodeSnippits();
			snippitsHelper.addSnippits("codeSnippits.xml");
		}
		return snippitsHelper;
	}
	private UserDefinedJavaClassCodeSnippits() {
	}
	public void addSnippits(String strFileName)
			throws KettleXMLException {
		Document doc = XMLHandler.loadXMLFile(
				UserDefinedJavaClassCodeSnippits.class
						.getResourceAsStream(strFileName), null, false, false);
		buildSnippitList(doc);
	}

	public enum Category {
		COMMON(BaseMessages.getString(PKG, "UserDefinedJavaClassCodeSnippits.categories.COMMON")), STATUS(
				BaseMessages.getString(PKG, "UserDefinedJavaClassCodeSnippits.categories.STATUS")), LOGGING(
				BaseMessages.getString(PKG, "UserDefinedJavaClassCodeSnippits.categories.LOGGING")), LISTENERS(
				BaseMessages.getString(PKG,
						"UDJC.snippits.categories.LISTENERS")), ROW(
				BaseMessages.getString(PKG, "UserDefinedJavaClassCodeSnippits.categories.ROW")), OTHER(
				BaseMessages.getString(PKG, "UserDefinedJavaClassCodeSnippits.categories.OTHER"));

		private String description;

		private Category(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		@Override
		public String toString() {
			return description;
		}
	}

	public static class Snippit {
		private Snippit(Category category, String name, String sample,
				String code) {
			this.category = category;
			this.name = name;
			this.sample = sample;
			this.code = code;
		}

		public final Category category;
		public final String name;
		public final String sample;
		public final String code;
	}

	public List<Snippit> getSnippits() {
		return Collections.unmodifiableList(snippits);
	}

	public String getDefaultCode() {
		return getCode("Implement processRow");
	}
	public String getCode(String snippitName) {
		Snippit snippit = snippitsMap.get(snippitName);
		return (snippit == null) ? "" : snippit.code;
	}

	public String getSample(String snippitName) {
		Snippit snippit = snippitsMap.get(snippitName);
		return (snippit == null) ? "" : snippit.sample;
	}

	private void buildSnippitList(Document doc) {
		List<Node> nodes = XMLHandler.getNodes(XMLHandler.getSubNode(doc,
				"codeSnippits"), "codeSnippit");
		for (Node node : nodes) {
			Snippit snippit = new Snippit(Category.valueOf(XMLHandler
					.getTagValue(node, "category")), XMLHandler.getTagValue(
					node, "name"), XMLHandler.getTagValue(node, "sample"),
					XMLHandler.getTagValue(node, "code"));
			snippits.add(snippit);
			Snippit oldSnippit = snippitsMap.put(snippit.name, snippit);
			if (oldSnippit != null) {
				log.logError("Multiple code snippits for name: " + snippit.name);
			}
		}
	}
}
