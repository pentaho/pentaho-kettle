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
 
package be.ibridge.kettle.trans.step;

import be.ibridge.kettle.repository.Repository;

/**
 * This interface is used to launch Step Dialogs.
 * All dialogs that implement this simple interface can be opened by Spoon.
 * 
 * @author Matt
 * @since 4-aug-2004
 * 
 */
public interface StepDialogInterface 
{
	public String open();
    public void setRepository(Repository repository);
}
