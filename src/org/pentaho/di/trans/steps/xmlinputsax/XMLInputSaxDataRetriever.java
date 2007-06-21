package org.pentaho.di.trans.steps.xmlinputsax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author Youssef
 * @since 22-may-2006
 */
public class XMLInputSaxDataRetriever extends DefaultHandler
{

	XMLInputSaxMeta meta;

	XMLInputSaxData data;

	int[] position = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };

	// list of elements to the root element
	private ArrayList pathToRootElement = new ArrayList();

	// list of elements to the root element
	private ArrayList _pathToRootElement = new ArrayList();

	private ArrayList fields = new ArrayList();

	private int fieldToFill = -1;

	/** Empty row */
	private Object[] emptyRow;

	/** Temporary row of data */
	private Object[] row;

	/** List of datarows retreived from the xml */
	private ArrayList rowSet = new ArrayList();

	// count the deep to the current element in pathToStartElement
	private int counter = 0;

	// count the deep to the current element in xml file
	private int _counter = -1;

	// true when the root element is reached
	private boolean rootFound = false;

	// source xml file name
	private String sourceFile;

	private String tempVal;

	/**
	 * Constructor of xmlDataRetreiver class.
	 * 
	 * @param sourceFile
	 *            The XML file containing data.
	 * @param pathToRootElement
	 *            Array of ordered XMLvInputFieldPosition objects. Define the
	 *            path to the root element.
	 * @param fieldsDefinition
	 *            list of fields to be retreived from the Root element, with
	 *            their definition .
	 * @param definingAttribute
	 * 
	 */
	public XMLInputSaxDataRetriever(String sourceFile, XMLInputSaxMeta meta, XMLInputSaxData data)
	{

		this.meta = meta;

		this.data = data;

		for (int i = 0; i < meta.getInputPosition().length; i++)
		{
			this.pathToRootElement.add(meta.getInputPosition()[i]);
		}
		for (int i = 0; i < meta.getInputFields().length; i++)
		{
			this.fields.add(meta.getInputFields()[i]);
		}
		this.sourceFile = sourceFile;

		this.emptyRow = buildEmptyRow();

		System.arraycopy(emptyRow, 0, this.row, 0, emptyRow.length);

	}

	public void runExample()
	{
		parseDocument();
	}

	private void parseDocument()
	{
		// get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try
		{
			// get a new instance of parser
			SAXParser sp = spf.newSAXParser();
			// parse the file and also register this class for call backs
			sp.parse(sourceFile, this);

		} catch (SAXException se)
		{
			LogWriter.getInstance().logError(toString(), Const.getStackTracker(se));
		} catch (ParserConfigurationException pce)
		{
			LogWriter.getInstance().logError(toString(), Const.getStackTracker(pce));
		} catch (IOException ie)
		{
			LogWriter.getInstance().logError(toString(), Const.getStackTracker(ie));
		}
	}

	private XMLInputSaxFieldPosition[] pathFromRoot()
	{
		int s = _pathToRootElement.size() - pathToRootElement.size();
		if (s > 0)
		{
			XMLInputSaxFieldPosition[] ret = new XMLInputSaxFieldPosition[s];
			for (int i = 0; i < s; i++)
			{
				ret[i] = (XMLInputSaxFieldPosition) _pathToRootElement.get(i + pathToRootElement.size());
			}
			return ret;
		}
		return null;
	}

	private String naming(XMLInputSaxFieldPosition[] path)
	{
		String ret = "";
		for (int i = 0; i < path.length; i++)
		{
			String name;
			if (path[i].getType() == XMLInputSaxFieldPosition.XML_ELEMENT_ATT)
			{
				name = path[i].getAttributeValue();
			} else
			{
				name = path[i].getName() + path[i].getElementNr();
			}
			if (i > 0)
			{
				ret += "_" + name;
			} else
			{
				ret += name;
			}
		}
		return ret;
	}

	/**
	 * Build an empty row based on the meta-data...
	 * 
	 * @return
	 */
	private Object[] buildEmptyRow()
	{

		XMLInputSaxField fields[] = meta.getInputFields();
		Object[] row = new Object[fields.length];
		for (int i = 0; i < fields.length; i++)
		{
			XMLInputSaxField field = fields[i];

			ValueMetaInterface mvalue = new ValueMeta(field.getName(), field.getType());
			mvalue.setLength(field.getLength(), field.getPrecision());

			row[i] = new ValueMetaAndData(mvalue, null);
		}

		return row;
	}

	private void counterUp()
	{
		if (counter == pathToRootElement.size() - 1)
		{
			rootFound = true;
			counter++;
		} else
		{
			counter++;
		}
	}

	private boolean comparePaths(int count)
	{
		for (int i = 0; i <= count; i++)
		{
			if (!((XMLInputSaxFieldPosition) pathToRootElement.get(i))
					.equals((XMLInputSaxFieldPosition) pathToRootElement.get(i)))
			{
				return false;
			}
		}
		return true;
	}

	private void counterDown()
	{
		if ((counter - 1 == _counter) && comparePaths(_counter))
		{
			_pathToRootElement.remove(_counter);
			counter--;
			_counter--;
			rootFound = false;
			rowSet.add(Arrays.asList(row));
			// System.out.println(row.toString());
			this.row = null;
			System.arraycopy(emptyRow, 0, this.row, 0, emptyRow.length);
		} else
		{
			_pathToRootElement.remove(_counter);
			_counter--;
		}
	}

	// Event Handlers
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException
	{
		// set the _counter level
		position[_counter + 1] += 1;
		_counter++;

		if (qName.equalsIgnoreCase("meetdata"))
		{
			System.out.println("qName=" + qName);
		}
		try
		{
			if (!rootFound)
			{
				XMLInputSaxFieldPosition el = (XMLInputSaxFieldPosition) pathToRootElement.get(counter);
				if ((counter == _counter) && qName.equalsIgnoreCase(el.getName()))
				{
					if (el.getType() == XMLInputSaxFieldPosition.XML_ELEMENT_ATT)
					{
						String att1 = attributes.getValue(el.getAttribute());
						String att2 = el.getAttributeValue();
						if (att1.equals(att2))
						{
							_pathToRootElement.add(new XMLInputSaxFieldPosition(qName, el.getAttribute(), el
									.getAttributeValue()));
							if (counter == pathToRootElement.size() - 1)
							{
								int i = 0;
								while (i < attributes.getLength())
								{
									XMLInputSaxFieldPosition tempP = new XMLInputSaxFieldPosition(attributes
											.getQName(i), XMLInputSaxFieldPosition.XML_ATTRIBUTE, i + 1);
									XMLInputSaxField tempF = new XMLInputSaxField(tempP.getName(),
											new XMLInputSaxFieldPosition[] { tempP });

									int p = fields.indexOf(tempF);
									if (p >= 0)
									{
										ValueMetaAndData v = (ValueMetaAndData) row[p];
										v.setValueData(attributes.getValue(i));
									}
									i++;
								}
							}
							counterUp();
						} else
						{
							_pathToRootElement.add(new XMLInputSaxFieldPosition(qName,
									XMLInputSaxFieldPosition.XML_ELEMENT_POS, position[_counter] + 1));
						}
					} else
					{
						_pathToRootElement.add(new XMLInputSaxFieldPosition(qName,
								XMLInputSaxFieldPosition.XML_ELEMENT_POS, position[_counter] + 1));
						counterUp();
					}
				} else
				{
					_pathToRootElement.add(new XMLInputSaxFieldPosition(qName,
							XMLInputSaxFieldPosition.XML_ELEMENT_POS, position[_counter] + 1));
				}
			} else
			{
				XMLInputSaxField tempF = null;
				if (attributes.getLength() == 0)
				{
					_pathToRootElement.add(new XMLInputSaxFieldPosition(qName,
							XMLInputSaxFieldPosition.XML_ELEMENT_POS, position[_counter] + 1));
					XMLInputSaxFieldPosition[] path = pathFromRoot();
					tempF = new XMLInputSaxField(naming(path), path);
				} else
				{
					String attribute = meta.getDefiningAttribute(qName);
					_pathToRootElement.add(new XMLInputSaxFieldPosition(qName, attribute, attributes
							.getValue(attribute)));
					XMLInputSaxFieldPosition[] path = pathFromRoot();
					tempF = new XMLInputSaxField(naming(path), path);
				}
				int p = fields.indexOf(tempF);
				if (p >= 0)
				{
					this.fieldToFill = p;
				}
			}
		} catch (KettleValueException e)
		{
			LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException
	{
		try
		{
			tempVal = new String(ch, start, length);

			if (tempVal.equals("1"))
			{
				System.out.println("tempVal=" + tempVal);
			}

			if (this.fieldToFill >= 0)
			{
				ValueMetaAndData v = (ValueMetaAndData) row[fieldToFill];
				if (tempVal != "")
				{
					v.setValueData(tempVal);
				} else
				{
					v.setValueData("");
				}

				XMLInputSaxField xmlInputField = (XMLInputSaxField) fields.get(fieldToFill);

				switch (xmlInputField.getTrimType())
				{
				case XMLInputSaxField.TYPE_TRIM_LEFT:
					v.setValueData(Const.ltrim(v.getValueData().toString()));
					break;
				case XMLInputSaxField.TYPE_TRIM_RIGHT:
					v.setValueData(Const.rtrim(v.getValueData().toString()));
					break;
				case XMLInputSaxField.TYPE_TRIM_BOTH:
					v.setValueData(v.getValueData().toString().trim());
					break;
				default:
					break;
				}

				// System.out.println("after trim, field #"+i+" : "+v);

				// DO CONVERSIONS...
				switch (xmlInputField.getType())
				{
				case ValueMeta.TYPE_STRING:
					// System.out.println("Convert value to String :"+v);
					break;
				case ValueMeta.TYPE_NUMBER:
					// System.out.println("Convert value to Number :"+v);
					if (xmlInputField.getFormat() != null && xmlInputField.getFormat().length() > 0)
					{
						if (xmlInputField.getDecimalSymbol() != null
								&& xmlInputField.getDecimalSymbol().length() > 0)
						{
							if (xmlInputField.getGroupSymbol() != null
									&& xmlInputField.getGroupSymbol().length() > 0)
							{
								if (xmlInputField.getCurrencySymbol() != null
										&& xmlInputField.getCurrencySymbol().length() > 0)
								{
									v.setValueData(new Double(StringUtil.str2num(xmlInputField.getFormat(),
											xmlInputField.getGroupSymbol(), xmlInputField.getGroupSymbol(),
											xmlInputField.getCurrencySymbol(), v.getValueData().toString())));
								} else
								{
									v.setValueData(new Double(StringUtil.str2num(xmlInputField.getFormat(),
											xmlInputField.getGroupSymbol(), xmlInputField.getGroupSymbol(),
											null, v.getValueData().toString())));
								}
							} else
							{
								v.setValueData(new Double(StringUtil.str2num(xmlInputField.getFormat(),
										xmlInputField.getGroupSymbol(), null, null, v.getValueData()
												.toString())));
							}
						} else
						{
							v.setValueData(new Double(StringUtil.str2num(xmlInputField.getFormat(), null,
									null, null, v.getValueData().toString()))); // just
							// a
							// format
							// mask
						}
					} else
					{
						v.setValueData(new Double(StringUtil.str2num(null, null, null, null, v.getValueData()
								.toString())));
					}
					v.getValueMeta().setLength(xmlInputField.getLength(), xmlInputField.getPrecision());
					break;
				case ValueMeta.TYPE_INTEGER:
					// System.out.println("Convert value to integer :"+v);
					//v.setValue(v.getInteger());
					v.getValueMeta().setLength(xmlInputField.getLength(), xmlInputField.getPrecision());
					break;
				case ValueMeta.TYPE_BIGNUMBER:
					// System.out.println("Convert value to BigNumber :"+v);
					//v.setValue(v.getBigNumber());
					v.getValueMeta().setLength(xmlInputField.getLength(), xmlInputField.getPrecision());
					break;
				case ValueMeta.TYPE_DATE:
					// System.out.println("Convert value to Date :"+v);

					if (xmlInputField.getFormat() != null && xmlInputField.getFormat().length() > 0)
					{
						v.setValueData(StringUtil.str2dat(xmlInputField.getFormat(),null,v.getValueData().toString()));
					} else
					{
						//is this necessary?
						//v.setValue(v.getDate());
					}
					break;
				case ValueMeta.TYPE_BOOLEAN:
					//v.setValue(v.getBoolean());
					break;
				default:
					break;
				}

				// Do we need to repeat this field if it is null?
				if (xmlInputField.isRepeated())
				{
					if (v.getValueData()==null && data.previousRow != null)
					{
						ValueMetaAndData previous = (ValueMetaAndData)data.previousRow[fieldToFill];
						v.setValueData(previous.getValueData());
					}
				}
			}
			fieldToFill = -1;
		} catch (KettleValueException e)
		{
			LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		position[_counter + 1] = -1;
		counterDown();
	}

	public boolean hasNext()
	{
		synchronized (rowSet)
		{
			return !rowSet.isEmpty();
		}
	}

	public Object[] getNext()
	{
		synchronized (rowSet)
		{
			if (!rowSet.isEmpty())
			{
				Object[] ret = (Object[]) rowSet.get(0);
				rowSet.remove(0);
				return ret;
			} else
			{
				return null;
			}
		}
	}

	/*
	 * public static void main(String[] args){ XMLvInputFieldPosition[] path=new
	 * XMLvInputFieldPosition[3]; try { path[0]=new
	 * XMLvInputFieldPosition("Ep=raml"); path[1]=new
	 * XMLvInputFieldPosition("Ep=cmData"); path[2]=new
	 * XMLvInputFieldPosition("Ea=managedObject/class:BTS"); } catch
	 * (KettleValueException e) { // TODO Auto-generated catch block
	 * LogWriter.getInstance().logError(toString(), Const.getStackTracker(e)); }
	 * //System.out.println(new xmlElement("hello","hello","hello").equals(new
	 * xmlElement("hello","hello","hello"))); XMLvSaxFieldRetreiver spe = new
	 * XMLvSaxFieldRetreiver("D:\\NOKIA\\Project\\Ressources\\CASA-1.XML",path,"name");
	 * 
	 * ArrayList l=spe.getFields(); XMLvInputData data=new XMLvInputData();
	 * XMLvInputMeta meta=new XMLvInputMeta();
	 * 
	 * XMLvInputField [] a=new XMLvInputField[l.size()]; for(int i=0;i<l.size();i++){
	 * XMLvInputField f=(XMLvInputField)l.get(i); XMLvInputField field=new
	 * XMLvInputField(); field.setName(f.getName()); try {
	 * field.setFieldPosition(f.getFieldPositionsCode(path.length)); } catch
	 * (KettleException e) { LogWriter.getInstance().logError(toString(),
	 * Const.getStackTracker(e)); } a[i]=field; }
	 * 
	 * meta.setInputFields(a); System.out.println(a.length);
	 * meta.setInputPosition(path);
	 * 
	 * XMLvSaxDataRetreiver r=new
	 * XMLvSaxDataRetreiver("D:\\NOKIA\\Project\\Ressources\\CASA-1.XML",meta,data,"name");
	 * r.runExample(); }
	 */
}
