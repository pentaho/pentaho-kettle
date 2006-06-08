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

/* 
 * 
 * Created on 4-apr-2003
 * 
 */

package be.ibridge.kettle.trans.step.getfilenames;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
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
import be.ibridge.kettle.trans.step.fileinput.FileInputList;

public class GetFileNamesMeta extends BaseStepMeta implements StepMetaInterface
{
    private static final String NO = "N";

	private static final String YES = "Y";

    /** Array of filenames */
    private String             fileName[];

    /** Wildcard or filemask (regular expression) */
    private String             fileMask[];
    
    /** Array of boolean values as string, indicating if a file is required. */
    private String             fileRequired[];
    
    public GetFileNamesMeta()
    {
        super(); // allocate BaseStepMeta
    }

     /**
     * @return Returns the fileMask.
     */
    public String[] getFileMask()
    {
        return fileMask;
    }
    
    /**
     * @return Returns the fileRequired.
     */
    public String[] getFileRequired() {
    	return fileRequired;
	}

    /**
     * @param fileMask The fileMask to set.
     */
    public void setFileMask(String[] fileMask)
    {
        this.fileMask = fileMask;
    }
    
    /**
     * @param fileRequired The fileRequired to set.
     */
    public void setFileRequired(String[] fileRequired)
    {
        this.fileRequired = fileRequired;
    }

    /**
     * @return Returns the fileName.
     */
    public String[] getFileName()
    {
        return fileName;
    }

    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String[] fileName)
    {
        this.fileName = fileName;
    }

    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException
    {
        readData(stepnode);
    }

    public Object clone()
    {
        GetFileNamesMeta retval = (GetFileNamesMeta) super.clone();

        int nrfiles = fileName.length;

        retval.allocate(nrfiles);

        return retval;
    }

    public void allocate(int nrfiles)
    {
        fileName = new String[nrfiles];
        fileMask = new String[nrfiles];
        fileRequired = new String[nrfiles];
    }

    public void setDefault()
    {
        int nrfiles = 0;

        allocate(nrfiles);

        for (int i = 0; i < nrfiles; i++)
        {
            fileName[i] = "filename" + (i + 1);
            fileMask[i] = "";
            fileRequired[i] = NO;
        }
    }

    public Row getFields(Row r, String name, Row info)
    {
        Row row;
        if (r == null)
            row = new Row(); // give back values
        else
            row = r; // add to the existing row of values...
        
        // the filename
        Value filename = new Value("filename", Value.VALUE_TYPE_STRING);
        filename.setLength(500,-1);
        filename.setOrigin(name);
        row.addValue(filename);

        // the short filename
        Value short_filename = new Value("short_filename", Value.VALUE_TYPE_STRING);
        short_filename.setLength(500,-1);
        short_filename.setOrigin(name);
        row.addValue(short_filename);

        // the path
        Value path = new Value("path", Value.VALUE_TYPE_STRING);
        path.setLength(500,-1);
        path.setOrigin(name);
        row.addValue(path);

        return row;
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("    <file>" + Const.CR);
        for (int i = 0; i < fileName.length; i++)
        {
            retval.append("      " + XMLHandler.addTagValue("name", fileName[i]));
            retval.append("      " + XMLHandler.addTagValue("filemask", fileMask[i]));
            retval.append("      " + XMLHandler.addTagValue("file_required", fileRequired[i]));
        }
        retval.append("  </file>" + Const.CR);

        return retval.toString();
    }

    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            Node filenode    = XMLHandler.getSubNode(stepnode, "file");
            int nrfiles   = XMLHandler.countNodes(filenode, "name");

            allocate(nrfiles);

            for (int i = 0; i < nrfiles; i++)
            {
                Node filenamenode = XMLHandler.getSubNodeByNr(filenode, "name", i);
                Node filemasknode = XMLHandler.getSubNodeByNr(filenode, "filemask", i);
                Node fileRequirednode = XMLHandler.getSubNodeByNr(filenode, "file_required", i);
                fileName[i] = XMLHandler.getNodeValue(filenamenode);
                fileMask[i] = XMLHandler.getNodeValue(filemasknode);
                fileRequired[i] = XMLHandler.getNodeValue(fileRequirednode);
            }
        }
        catch (Exception e)
        {
            throw new KettleXMLException("Unable to load step info from XML", e);
        }
    }

    public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters) throws KettleException
    {
        try
        {
            int nrfiles = rep.countNrStepAttributes(id_step, "file_name");

            allocate(nrfiles);

            for (int i = 0; i < nrfiles; i++)
            {
                fileName[i] = rep.getStepAttributeString(id_step, i, "file_name");
                fileMask[i] = rep.getStepAttributeString(id_step, i, "file_mask");
                fileRequired[i] = rep.getStepAttributeString(id_step, i, "file_required");
                if(!YES.equalsIgnoreCase(fileRequired[i])) fileRequired[i] = NO;
            }
        }
        catch (Exception e)
        {
            throw new KettleException("Unexpected error reading step information from the repository", e);
        }
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        try
        {
            for (int i = 0; i < fileName.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "file_name", fileName[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "file_mask", fileMask[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "file_required", fileRequired[i]);
            }
        }
        catch (Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
        }
    }

    public String[] getFilePaths()
    {
    	return FileInputList.createFilePathList(fileName, fileMask, fileRequired);
    }
    
    public FileInputList getTextFileList()
    {
    	return FileInputList.createFileList(fileName, fileMask, fileRequired);
    }

    public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
    {
        CheckResult cr;

        // See if we get input...
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GetFileNamesMeta.CheckResult.NoInputError"), stepinfo);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GetFileNamesMeta.CheckResult.NoInputOk"), stepinfo);
            remarks.add(cr);
        }

        FileInputList textFileList = getTextFileList();
        if (textFileList.nrOfFiles() == 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GetFileNamesMeta.CheckResult.ExpectedFilesError"), stepinfo);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GetFileNamesMeta.CheckResult.ExpectedFilesOk", ""+textFileList.nrOfFiles()), stepinfo);
            remarks.add(cr);
        }
    }

    public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
    {
        return new GetFileNamesDialog(shell, info, transMeta, name);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new GetFileNames(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new GetFileNamesData();
    }	
}
