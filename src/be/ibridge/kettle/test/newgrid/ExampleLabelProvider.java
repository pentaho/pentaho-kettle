package be.ibridge.kettle.test.newgrid;

/**
 * (c) Copyright Mirasol Op'nWorks Inc. 2002, 2003. 
 * http://www.opnworks.com
 * Created on Apr 2, 2003 by lgauthier@opnworks.com
 */


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITableLabelProvider;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


/**
 * Label provider for the TableViewerExample
 * 
 * @see org.eclipse.jface.viewers.LabelProvider 
 */
public class ExampleLabelProvider 
    extends LabelProvider
    implements ITableLabelProvider {

    // Names of images used to represent checkboxes
    public static final String CHECKED_IMAGE    = "checked";
    public static final String UNCHECKED_IMAGE  = "unchecked";

    // For the checkbox images
    private static ImageRegistry imageRegistry = new ImageRegistry();

    /**
     * Note: An image registry owns all of the image objects registered with it,
     * and automatically disposes of them the SWT Display is disposed.
     */ 
    static {
        String iconPath = "icons/"; 
        imageRegistry.put(CHECKED_IMAGE, ImageDescriptor.createFromFile(
                TableViewerExample.class, 
                iconPath + CHECKED_IMAGE + ".gif"
                )
            );
        imageRegistry.put(UNCHECKED_IMAGE, ImageDescriptor.createFromFile(
                TableViewerExample.class, 
                iconPath + UNCHECKED_IMAGE + ".gif"
                )
            );  
    }
    
    /**
     * Returns the image with the given key, or <code>null</code> if not found.
     */
    private Image getImage(boolean isSelected) {
        String key = isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE;
        return  imageRegistry.get(key);
    }

    /**
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
        String result = "";
        ExampleTask task = (ExampleTask) element;
        switch (columnIndex) {
            case 0:  // COMPLETED_COLUMN
                break;
            case 1 :
                result = task.getDescription();
                break;
            case 2 :
                result = task.getOwner();
                break;
            case 3 :
                result = task.getPercentComplete() + "";
                break;
            default :
                break;  
        }
        return result;
    }

    /**
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        return (columnIndex == 0) ?   // COMPLETED_COLUMN?
            getImage(((ExampleTask) element).isCompleted()) :
            null;
    }

}
