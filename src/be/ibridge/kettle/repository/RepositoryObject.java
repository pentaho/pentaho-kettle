package be.ibridge.kettle.repository;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.widgets.TreeItem;

import be.ibridge.kettle.core.Const;

/**
 * Contains some common object details, extracted from a repository
 *   
 * @author Matt
 */
public class RepositoryObject
{
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static final String STRING_OBJECT_TYPE_TRANSFORMATION = "Transformation";
    public static final String STRING_OBJECT_TYPE_JOB =            "Job";
    
    private String name;
    private String modifiedUser;
    private Date   modifiedDate;
    
    public RepositoryObject()
    {
    }
    
    /**
     * @param name
     * @param modifiedUser
     * @param modifiedDate
     */
    public RepositoryObject(String name, String modifiedUser, Date modifiedDate)
    {
        this();
        this.name = name;
        this.modifiedUser = modifiedUser;
        this.modifiedDate = modifiedDate;
    }

    /**
     * @return the modifiedDate
     */
    public Date getModifiedDate()
    {
        return modifiedDate;
    }

    /**
     * @param modifiedDate the modifiedDate to set
     */
    public void setModifiedDate(Date modifiedDate)
    {
        this.modifiedDate = modifiedDate;
    }

    /**
     * @return the modifiedUser
     */
    public String getModifiedUser()
    {
        return modifiedUser;
    }

    /**
     * @param modifiedUser the modifiedUser to set
     */
    public void setModifiedUser(String modifiedUser)
    {
        this.modifiedUser = modifiedUser;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    
    public void setTreeItem(TreeItem treeItem, String type)
    {
        treeItem.setText(0, Const.NVL(name, ""));
        treeItem.setText(1, Const.NVL(type, ""));
        treeItem.setText(2, Const.NVL(modifiedUser, ""));
        treeItem.setText(3, modifiedDate!=null ? simpleDateFormat.format(modifiedDate) : "");
    }
    
}
