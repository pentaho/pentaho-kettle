/*
 * (c) COPYRIGHT 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * $Id: SACMediaList.java,v 1.1 2008/12/03 15:25:51 rsternber Exp $
 */
package org.w3c.css.sac;

/**
 * @version $Revision: 1.1 $
 * @author  Philippe Le Hegaret
 */
public interface SACMediaList {

    /**
     * Returns the length of this media list
     */    
    public int getLength();

    /**
     * Returns the medium at the specified index, or <code>null</code> if this
     * is not a valid index.  
     */
    public String item(int index);
}
