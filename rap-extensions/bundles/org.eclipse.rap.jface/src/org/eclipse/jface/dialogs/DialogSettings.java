/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Marc R. Hoffmann <hoffmann@mountainminds.com> - Bug 284265 [JFace] 
 *                  DialogSettings.save() silently ignores IOException
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Concrete implementation of a dialog settings (<code>IDialogSettings</code>)
 * using a hash table and XML. The dialog store can be read
 * from and saved to a stream. All keys and values must be strings or array of
 * strings. Primitive types are converted to strings.
 * <p>
 * This class was not designed to be subclassed.
 *
 * Here is an example of using a DialogSettings:
 * </p>
 * <pre>
 * <code>
 * DialogSettings settings = new DialogSettings("root");
 * settings.put("Boolean1",true);
 * settings.put("Long1",100);
 * settings.put("Array1",new String[]{"aaaa1","bbbb1","cccc1"});
 * DialogSettings section = new DialogSettings("sectionName");
 * settings.addSection(section);
 * section.put("Int2",200);
 * section.put("Float2",1.1);
 * section.put("Array2",new String[]{"aaaa2","bbbb2","cccc2"});
 * settings.save("c:\\temp\\test\\dialog.xml");
 * </code>
 * </pre>
 * @noextend This class is not intended to be subclassed by clients.
 */

public class DialogSettings implements IDialogSettings {
    // The name of the DialogSettings.
    private String name;

    /* A Map of DialogSettings representing each sections in a DialogSettings.
     It maps the DialogSettings' name to the DialogSettings */
    private Map sections;

    /* A Map with all the keys and values of this sections.
     Either the keys an values are restricted to strings. */
    private Map items;

    // A Map with all the keys mapped to array of strings.
    private Map arrayItems;

    private static final String TAG_SECTION = "section";//$NON-NLS-1$

    private static final String TAG_NAME = "name";//$NON-NLS-1$

    private static final String TAG_KEY = "key";//$NON-NLS-1$

    private static final String TAG_VALUE = "value";//$NON-NLS-1$

    private static final String TAG_LIST = "list";//$NON-NLS-1$

    private static final String TAG_ITEM = "item";//$NON-NLS-1$

    /**
     * Create an empty dialog settings which loads and saves its
     * content to a file.
     * Use the methods <code>load(String)</code> and <code>store(String)</code>
     * to load and store this dialog settings.
     *
     * @param sectionName the name of the section in the settings.
     */
    public DialogSettings(String sectionName) {
        name = sectionName;
        items = new HashMap();
        arrayItems = new HashMap();
        sections = new HashMap();
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public IDialogSettings addNewSection(String sectionName) {
        DialogSettings section = new DialogSettings(sectionName);
        addSection(section);
        return section;
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public void addSection(IDialogSettings section) {
        sections.put(section.getName(), section);
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public String get(String key) {
        return (String) items.get(key);
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public String[] getArray(String key) {
        return (String[]) arrayItems.get(key);
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public boolean getBoolean(String key) {
        return Boolean.valueOf((String) items.get(key)).booleanValue();
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public double getDouble(String key) throws NumberFormatException {
        String setting = (String) items.get(key);
        if (setting == null) {
			throw new NumberFormatException(
                    "There is no setting associated with the key \"" + key + "\"");//$NON-NLS-1$ //$NON-NLS-2$
		}

        return new Double(setting).doubleValue();
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public float getFloat(String key) throws NumberFormatException {
        String setting = (String) items.get(key);
        if (setting == null) {
			throw new NumberFormatException(
                    "There is no setting associated with the key \"" + key + "\"");//$NON-NLS-1$ //$NON-NLS-2$
		}

        return new Float(setting).floatValue();
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public int getInt(String key) throws NumberFormatException {
        String setting = (String) items.get(key);
        if (setting == null) {
            //new Integer(null) will throw a NumberFormatException and meet our spec, but this message
            //is clearer.
            throw new NumberFormatException(
                    "There is no setting associated with the key \"" + key + "\"");//$NON-NLS-1$ //$NON-NLS-2$
        }

        return new Integer(setting).intValue();
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public long getLong(String key) throws NumberFormatException {
        String setting = (String) items.get(key);
        if (setting == null) {
            //new Long(null) will throw a NumberFormatException and meet our spec, but this message
            //is clearer.
            throw new NumberFormatException(
                    "There is no setting associated with the key \"" + key + "\"");//$NON-NLS-1$ //$NON-NLS-2$
        }

        return new Long(setting).longValue();
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public String getName() {
        return name;
    }

	/**
	 * Returns a section with the given name in the given dialog settings. If
	 * the section doesn't exist yet, then it is first created.
	 * 
	 * @param settings
	 *            the parent settings
	 * @param sectionName
	 *            the name of the section
	 * @return the section
	 * 
	 * @since 1.4
	 */
	public static IDialogSettings getOrCreateSection(IDialogSettings settings,
			String sectionName) {
		IDialogSettings section = settings.getSection(sectionName);
		if (section == null) {
			section = settings.addNewSection(sectionName);
		}
		return section;
	}

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public IDialogSettings getSection(String sectionName) {
        return (IDialogSettings) sections.get(sectionName);
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public IDialogSettings[] getSections() {
        Collection values = sections.values();
        DialogSettings[] result = new DialogSettings[values.size()];
        values.toArray(result);
        return result;
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public void load(Reader r) {
        Document document = null;
        try {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            //		parser.setProcessNamespace(true);
            document = parser.parse(new InputSource(r));

            //Strip out any comments first
            Node root = document.getFirstChild();
            while (root.getNodeType() == Node.COMMENT_NODE) {
                document.removeChild(root);
                root = document.getFirstChild();
            }
            load(document, (Element) root);
        } catch (ParserConfigurationException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        } catch (SAXException e) {
            // ignore
        }
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public void load(String fileName) throws IOException {
        FileInputStream stream = new FileInputStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                stream, "utf-8"));//$NON-NLS-1$
        load(reader);
        reader.close();
    }

    /* (non-Javadoc)
     * Load the setting from the <code>document</code>
     */
    private void load(Document document, Element root) {
        name = root.getAttribute(TAG_NAME);
        NodeList l = root.getElementsByTagName(TAG_ITEM);
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (root == n.getParentNode()) {
                String key = ((Element) l.item(i)).getAttribute(TAG_KEY);
                String value = ((Element) l.item(i)).getAttribute(TAG_VALUE);
                items.put(key, value);
            }
        }
        l = root.getElementsByTagName(TAG_LIST);
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (root == n.getParentNode()) {
                Element child = (Element) l.item(i);
                String key = child.getAttribute(TAG_KEY);
                NodeList list = child.getElementsByTagName(TAG_ITEM);
                List valueList = new ArrayList();
                for (int j = 0; j < list.getLength(); j++) {
                    Element node = (Element) list.item(j);
                    if (child == node.getParentNode()) {
                        valueList.add(node.getAttribute(TAG_VALUE));
                    }
                }
                String[] value = new String[valueList.size()];
                valueList.toArray(value);
                arrayItems.put(key, value);
            }
        }
        l = root.getElementsByTagName(TAG_SECTION);
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (root == n.getParentNode()) {
                DialogSettings s = new DialogSettings("NoName");//$NON-NLS-1$
                s.load(document, (Element) n);
                addSection(s);
            }
        }
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public void put(String key, String[] value) {
        arrayItems.put(key, value);
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public void put(String key, double value) {
        put(key, String.valueOf(value));
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public void put(String key, float value) {
        put(key, String.valueOf(value));
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public void put(String key, int value) {
        put(key, String.valueOf(value));
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public void put(String key, long value) {
        put(key, String.valueOf(value));
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public void put(String key, String value) {
        items.put(key, value);
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public void put(String key, boolean value) {
        put(key, String.valueOf(value));
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
	public void save(Writer writer) throws IOException {
    	final XMLWriter xmlWriter = new XMLWriter(writer);
    	save(xmlWriter);
    	xmlWriter.flush();
    }

    /* (non-Javadoc)
     * Method declared on IDialogSettings.
     */
    public void save(String fileName) throws IOException {
        FileOutputStream stream = new FileOutputStream(fileName);
        XMLWriter writer = new XMLWriter(stream);
        save(writer);
        writer.close();
    }

    /* (non-Javadoc)
     * Save the settings in the <code>document</code>.
     */
    private void save(XMLWriter out) throws IOException {
    	HashMap attributes = new HashMap(2);
    	attributes.put(TAG_NAME, name == null ? "" : name); //$NON-NLS-1$
        out.startTag(TAG_SECTION, attributes);
        attributes.clear();

        for (Iterator i = items.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            attributes.put(TAG_KEY, key == null ? "" : key); //$NON-NLS-1$
            String string = (String) items.get(key);
            attributes.put(TAG_VALUE, string == null ? "" : string); //$NON-NLS-1$
            out.printTag(TAG_ITEM, attributes, true);
        }

        attributes.clear();
        for (Iterator i = arrayItems.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            attributes.put(TAG_KEY, key == null ? "" : key); //$NON-NLS-1$
            out.startTag(TAG_LIST, attributes);
            String[] value = (String[]) arrayItems.get(key);
            attributes.clear();
            if (value != null) {
                for (int index = 0; index < value.length; index++) {
                    String string = value[index];
                    attributes.put(TAG_VALUE, string == null ? "" : string); //$NON-NLS-1$
                    out.printTag(TAG_ITEM, attributes, true);
                }
            }
            out.endTag(TAG_LIST);
            attributes.clear();
        }
        for (Iterator i = sections.values().iterator(); i.hasNext();) {
            ((DialogSettings) i.next()).save(out);
        }
        out.endTag(TAG_SECTION);
    }
    
    /**
     * A simple XML writer.  Using this instead of the javax.xml.transform classes allows
     * compilation against JCL Foundation (bug 80059).
     */
    private static class XMLWriter extends BufferedWriter {
    	
    	/** current number of tabs to use for indent */
    	protected int tab;

    	/** the xml header */
    	protected static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; //$NON-NLS-1$

    	/**
    	 * Create a new XMLWriter
    	 * @param output the stream to write the output to
    	 * @throws IOException 
    	 */
    	public XMLWriter(OutputStream output) throws IOException {
    		this(new OutputStreamWriter(output, "UTF8")); //$NON-NLS-1$
    	}

    	/**
    	 * Create a new XMLWriter
    	 * @param output the write to used when writing to
    	 * @throws IOException 
    	 */
    	public XMLWriter(Writer output) throws IOException {
    		super(output);
    		tab = 0;
    		writeln(XML_VERSION);
    	}

    	private  void writeln(String text) throws IOException {
    		write(text);
    		newLine();
    	}

    	/**
    	 * write the intended end tag
    	 * @param name the name of the tag to end
    	 * @throws IOException 
    	 */
    	public void endTag(String name) throws IOException {
    		tab--;
    		printTag("/" + name, null, false); //$NON-NLS-1$
    	}

    	private void printTabulation() throws IOException {
    		for (int i = 0; i < tab; i++) {
				super.write('\t');
			}
    	}

    	/**
    	 * write the tag to the stream and format it by itending it and add new line after the tag
    	 * @param name the name of the tag
    	 * @param parameters map of parameters
    	 * @param close should the tag be ended automatically (=> empty tag)
    	 * @throws IOException 
    	 */
    	public void printTag(String name, HashMap parameters, boolean close) throws IOException {
    		printTag(name, parameters, true, true, close);
    	}

    	private void printTag(String name, HashMap parameters, boolean shouldTab, boolean newLine, boolean close) throws IOException {
    		StringBuffer sb = new StringBuffer();
    		sb.append('<');
    		sb.append(name);
    		if (parameters != null) {
				for (Enumeration e = Collections.enumeration(parameters.keySet()); e.hasMoreElements();) {
    				sb.append(" "); //$NON-NLS-1$
    				String key = (String) e.nextElement();
    				sb.append(key);
    				sb.append("=\""); //$NON-NLS-1$
    				sb.append(getEscaped(String.valueOf(parameters.get(key))));
    				sb.append("\""); //$NON-NLS-1$
    			}
			}
    		if (close) {
				sb.append('/');
			}
    		sb.append('>');
    		if (shouldTab) {
				printTabulation();
			}
    		if (newLine) {
				writeln(sb.toString());
			} else {
				write(sb.toString());
			}
    	}

    	/**
    	 * start the tag
    	 * @param name the name of the tag
    	 * @param parameters map of parameters
    	 * @throws IOException 
    	 */
    	public void startTag(String name, HashMap parameters) throws IOException {
    		startTag(name, parameters, true);
    		tab++;
    	}

    	private void startTag(String name, HashMap parameters, boolean newLine) throws IOException {
    		printTag(name, parameters, true, newLine, false);
    	}

    	private static void appendEscapedChar(StringBuffer buffer, char c) {
    		String replacement = getReplacement(c);
    		if (replacement != null) {
    			buffer.append('&');
    			buffer.append(replacement);
    			buffer.append(';');
    		} else {
    			buffer.append(c);
    		}
    	}

    	private static String getEscaped(String s) {
    		StringBuffer result = new StringBuffer(s.length() + 10);
    		for (int i = 0; i < s.length(); ++i) {
				appendEscapedChar(result, s.charAt(i));
			}
    		return result.toString();
    	}

    	private static String getReplacement(char c) {
    		// Encode special XML characters into the equivalent character references.
    		// The first five are defined by default for all XML documents.
    		// The next three (#xD, #xA, #x9) are encoded to avoid them
			// being converted to spaces on deserialization
    		switch (c) {
    			case '<' :
    				return "lt"; //$NON-NLS-1$
    			case '>' :
    				return "gt"; //$NON-NLS-1$
    			case '"' :
    				return "quot"; //$NON-NLS-1$
    			case '\'' :
    				return "apos"; //$NON-NLS-1$
    			case '&' :
    				return "amp"; //$NON-NLS-1$
    			case '\r':
					return "#x0D"; //$NON-NLS-1$
				case '\n':
					return "#x0A"; //$NON-NLS-1$
				case '\u0009':
					return "#x09"; //$NON-NLS-1$
    		}
    		return null;
    	}
    }
}
