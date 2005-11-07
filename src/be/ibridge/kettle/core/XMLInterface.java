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
 
package be.ibridge.kettle.core;

/**
 * Implementing classes of this interface know how to express themselves using XML
 * They also can construct themselves using XML.
 * 
 * @author Matt
 * @since  29-jan-2004
 */
public interface XMLInterface
{
	public String getXML();
}
