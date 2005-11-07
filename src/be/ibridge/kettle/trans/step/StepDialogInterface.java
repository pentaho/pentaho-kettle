 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.trans.step;

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
}
