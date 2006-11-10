 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package be.ibridge.kettle.trans.step.accessoutput;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;


/*
 * Created on 2-jun-2003
 *
 */
 
public class AccessOutputMeta extends BaseStepMeta implements StepMetaInterface
{
	private String       filename;
    private boolean      fileCreated;
	private String       tablename;
    private boolean      tableCreated;
	private boolean      tableTruncated;
    private int          commitSize;

    public AccessOutputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public Object clone()
	{
		AccessOutputMeta retval = (AccessOutputMeta)super.clone();
		return retval;
	}
	
    /**
     * @return Returns the tablename.
     */
    public String getTablename()
    {
        return tablename;
    }
    
    /**
     * @param tablename The tablename to set.
     */
    public void setTablename(String tablename)
    {
        this.tablename = tablename;
    }
    
    /**
     * @return Returns the truncate table flag.
     */
    public boolean truncateTable()
    {
        return tableTruncated;
    }
    
    /**
     * @param truncateTable The truncate table flag to set.
     */
    public void setTableTruncated(boolean truncateTable)
    {
        this.tableTruncated = truncateTable;
    }
    
	private void readData(Node stepnode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			filename = XMLHandler.getTagValue(stepnode, "filename");
			tablename     = XMLHandler.getTagValue(stepnode, "table");
			tableTruncated = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "truncate"));
            fileCreated = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "create_file"));
            tableCreated = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "create_table"));
            commitSize = Const.toInt( XMLHandler.getTagValue(stepnode, "commit_size"), AccessOutput.COMMIT_SIZE);
        }
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void setDefault()
	{
        fileCreated = true;
        tableCreated = true;
        tableTruncated = false;	
        commitSize = AccessOutput.COMMIT_SIZE;
    }

	public String getXML()
	{
		StringBuffer retval=new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("filename",      filename));
		retval.append("    "+XMLHandler.addTagValue("table",         tablename));
		retval.append("    "+XMLHandler.addTagValue("truncate",      tableTruncated));
        retval.append("    "+XMLHandler.addTagValue("create_file",   fileCreated));
        retval.append("    "+XMLHandler.addTagValue("create_table",  tableCreated));
        retval.append("    "+XMLHandler.addTagValue("commit_size",   commitSize));

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
            filename          =      rep.getStepAttributeString (id_step, "filename");
            tablename         =      rep.getStepAttributeString (id_step, "table");
			tableTruncated    =      rep.getStepAttributeBoolean(id_step, "truncate"); 
            fileCreated       =      rep.getStepAttributeBoolean(id_step, "create_file"); 
            tableCreated      =      rep.getStepAttributeBoolean(id_step, "create_table"); 
            commitSize        = (int)rep.getStepAttributeInteger(id_step, "commit_size"); 
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
            rep.saveStepAttribute(id_transformation, id_step, "filename",        filename);
			rep.saveStepAttribute(id_transformation, id_step, "table",       	 tablename);
			rep.saveStepAttribute(id_transformation, id_step, "truncate",        tableTruncated);
            rep.saveStepAttribute(id_transformation, id_step, "create_file",     fileCreated);
            rep.saveStepAttribute(id_transformation, id_step, "create_table",    tableCreated);
            rep.saveStepAttribute(id_transformation, id_step, "commit_size",     commitSize);
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	public Row getFields(Row r, String name, Row info) throws KettleStepException 
	{
		Row row;
		if (r == null)
			row = new Row(); // give back values
		else
			row = r; // add to the existing row of values...

		return row;
	}

	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
        // TODO: add file checking in case we don't create a table.
        
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AccessOutputMeta.CheckResult.ExpectedInputOk"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("AccessOutputMeta.CheckResult.ExpectedInputError"), stepMeta);
			remarks.add(cr);
		}
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new AccessOutputDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new AccessOutput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new AccessOutputData();
	}

    public Row getRequiredFields() throws KettleException
    {
        String realFilename = StringUtil.environmentSubstitute(filename);
        File file = new File(realFilename);
        Database db = null;
        try
        {
            if (!file.exists() || !file.isFile())
            {
                throw new KettleException(Messages.getString("AccessOutputMeta.Exception.FileDoesNotExist", realFilename));
            }
            
            // open the database and get the table
            db = Database.open(file);
            String realTablename = StringUtil.environmentSubstitute(tablename);
            Table table = db.getTable(realTablename);
            if (table==null)
            {
                throw new KettleException(Messages.getString("AccessOutputMeta.Exception.TableDoesNotExist", realTablename));
            }
            
            Row layout = getLayout(table);
            return layout;
        }
        catch(Exception e)
        {
            throw new KettleException(Messages.getString("AccessOutputMeta.Exception.ErrorGettingFields"), e);
        }
        finally
        {
            try
            {
                if (db!=null) db.close();
            }
            catch(IOException e)
            {
                throw new KettleException(Messages.getString("AccessOutputMeta.Exception.ErrorClosingDatabase"), e);
            }
        }
    }

    public static final Row getLayout(Table table) throws SQLException
    {
        Row row = new Row();
        List columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++)
        {
            Column column = (Column) columns.get(i);
            
            int valtype = Value.VALUE_TYPE_STRING;
            int length = -1;
            int precision = -1;
            
            int type = column.getType().getSQLType();
            switch(type)
            {
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR: 
            case java.sql.Types.LONGVARCHAR:  // Character Large Object
                valtype=Value.VALUE_TYPE_STRING;
                length=column.getLength();
                break;
                
            case java.sql.Types.CLOB:  
                valtype=Value.VALUE_TYPE_STRING;
                length=DatabaseMeta.CLOB_LENGTH;
                break;

            case java.sql.Types.BIGINT:
                valtype=Value.VALUE_TYPE_INTEGER;
                precision=0;   // Max 9.223.372.036.854.775.807
                length=15;
                break;
                
            case java.sql.Types.INTEGER:
                valtype=Value.VALUE_TYPE_INTEGER;
                precision=0;    // Max 2.147.483.647
                length=9;
                break;
                
            case java.sql.Types.SMALLINT:
                valtype=Value.VALUE_TYPE_INTEGER;
                precision=0;   // Max 32.767
                length=4;
                break;
                
            case java.sql.Types.TINYINT: 
                valtype=Value.VALUE_TYPE_INTEGER;
                precision=0;   // Max 127
                length=2;
                break;
                
            case java.sql.Types.DECIMAL:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.FLOAT:
            case java.sql.Types.REAL:
            case java.sql.Types.NUMERIC:
                valtype=Value.VALUE_TYPE_NUMBER;
                length=column.getLength(); 
                precision=column.getPrecision();
                if (length    >=126) length=-1;
                if (precision >=126) precision=-1;
                
                if (type==java.sql.Types.DOUBLE || type==java.sql.Types.FLOAT || type==java.sql.Types.REAL)
                {
                    if (precision==0) 
                    {
                        precision=-1; // precision is obviously incorrect if the type if Double/Float/Real
                    }
                }
                else
                {
                    if (precision==0 && length<18 && length>0)  // Among others Oracle is affected here.  
                    {
                        valtype=Value.VALUE_TYPE_INTEGER;
                    }
                }
                if (length>18 || precision>18) valtype=Value.VALUE_TYPE_BIGNUMBER;
                
                break;

            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP: 
                valtype=Value.VALUE_TYPE_DATE; 
                break;

            case java.sql.Types.BOOLEAN:
            case java.sql.Types.BIT:
                valtype=Value.VALUE_TYPE_BOOLEAN;
                break;
                
            case java.sql.Types.BINARY:
            case java.sql.Types.BLOB:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
                valtype=Value.VALUE_TYPE_BINARY;
                break;

            default:
                valtype=Value.VALUE_TYPE_STRING;
                length=column.getLength();                    
                break;
            }

            Value v=new Value(column.getName(), valtype);
            v.setLength(length, precision);
            
            row.addValue(v);
        }
            
        return row;
    }
    
    public static final List getColumns(Row row)
    {
        List list = new ArrayList();
        
        for (int i = 0; i < row.size(); i++)
        {
            Value value = row.getValue(i);
            Column column = new Column();
            column.setName(value.getName());
            
            int length = value.getLength();
            
            switch(value.getType())
            {
            case Value.VALUE_TYPE_INTEGER:
                if (length<3)
                {
                    column.setType(DataType.BYTE);
                }
                else
                {
                    if (length<5)
                    {
                        column.setType(DataType.INT);
                    }
                    else
                    {
                        column.setType(DataType.LONG);
                    }
                }
                break;
            case Value.VALUE_TYPE_NUMBER:
                column.setType(DataType.DOUBLE);
                break;
            case Value.VALUE_TYPE_DATE:
                column.setType(DataType.SHORT_DATE_TIME);
                break;
            case Value.VALUE_TYPE_STRING:
                if (length<255)
                {
                    column.setType(DataType.TEXT);
                }
                else
                {
                    column.setType(DataType.MEMO);
                }
                break;
            case Value.VALUE_TYPE_BINARY:
                column.setType(DataType.BINARY);
                break;
            case Value.VALUE_TYPE_BOOLEAN:
                column.setType(DataType.BOOLEAN);
                break;
            case Value.VALUE_TYPE_BIGNUMBER:
                column.setType(DataType.NUMERIC);
                break;
            default: break;
            }
            
            if (length>=0) column.setLength((short)length);
            if (value.getPrecision()>=1 && value.getPrecision()<=28) column.setPrecision((byte)value.getPrecision());
            
            list.add(column);
        }
        
        return list;
    }


    public static Object[] createObjectsForRow(Row r)
    {
        Object[] values = new Object[r.size()];
        for (int i=0;i<r.size();i++)
        {
            Value value = r.getValue(i);
            int length = value.getLength();
            
            switch(value.getType())
            {
            case Value.VALUE_TYPE_INTEGER:
                if (length<3)
                {
                    values[i] = new Byte((byte)value.getInteger());
                }
                else
                {
                    if (length<5)
                    {
                        values[i] = new Short((short)value.getInteger());
                    }
                    else
                    {
                        values[i] = new Long(value.getInteger());
                    }
                }
                break;
            case Value.VALUE_TYPE_NUMBER:
                values[i] = new Double(value.getNumber());
                break;
            case Value.VALUE_TYPE_DATE:
                values[i] = value.getDate();
                break;
            case Value.VALUE_TYPE_STRING:
                values[i] = value.getString();
                break;
            case Value.VALUE_TYPE_BINARY:
                values[i] = value.getBytes();
                break;
            case Value.VALUE_TYPE_BOOLEAN:
                values[i] = new Boolean(value.getBoolean());
                break;
            case Value.VALUE_TYPE_BIGNUMBER:
                values[i] = new Double(value.getNumber());
                break;
            default: break;
            }
        }
        return values;
    }

    /**
     * @return the fileCreated
     */
    public boolean isFileCreated()
    {
        return fileCreated;
    }

    /**
     * @param fileCreated the fileCreated to set
     */
    public void setFileCreated(boolean fileCreated)
    {
        this.fileCreated = fileCreated;
    }

    /**
     * @return the filename
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**
     * @return the tableCreated
     */
    public boolean isTableCreated()
    {
        return tableCreated;
    }

    /**
     * @param tableCreated the tableCreated to set
     */
    public void setTableCreated(boolean tableCreated)
    {
        this.tableCreated = tableCreated;
    }

    /**
     * @return the tableTruncated
     */
    public boolean isTableTruncated()
    {
        return tableTruncated;
    }

    /**
     * @return the commitSize
     */
    public int getCommitSize()
    {
        return commitSize;
    }

    /**
     * @param commitSize the commitSize to set
     */
    public void setCommitSize(int commitSize)
    {
        this.commitSize = commitSize;
    }

    public String[] getUsedLibraries()
    {
        return new String[] 
        { 
            "jackcess-1.1.5.jar", 
            "commons-collections-3.1.jar", 
            "commons-logging.jar", 
            "commons-lang-2.2.jar", 
            "commons-dbcp-1.2.1.jar", 
            "commons-pool-1.3.jar", 
        };
    }
}
