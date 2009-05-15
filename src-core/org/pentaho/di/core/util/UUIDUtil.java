/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Mar 18, 2005 
 * @author Marc Batchelor
 * 
 */

package org.pentaho.di.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

public class UUIDUtil {
	private static Class<?> PKG = UUIDUtil.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private static Log log = LogFactory.getLog(UUIDUtil.class);

    static boolean nativeInitialized = false;

    static UUIDGenerator ug;

    static org.safehaus.uuid.EthernetAddress eAddr;

    static {
        // Try loading the EthernetAddress library. If this fails, then fallback
        // to
        // using another method for generating UUID's.
        /*
         * This is always going to fail at the moment try {
         * System.loadLibrary("EthernetAddress"); //$NON-NLS-1$
         * nativeInitialized = true; } catch (Throwable t) { //
         * log.warn(BaseMessages.getString(PKG, "UUIDUtil.ERROR_0001_LOADING_ETHERNET_ADDRESS") );
         * //$NON-NLS-1$ //$NON-NLS-2$ // Ignore for now. }
         */
        ug = UUIDGenerator.getInstance();
        if (nativeInitialized) {
            try {
                com.ccg.net.ethernet.EthernetAddress ea = com.ccg.net.ethernet.EthernetAddress.getPrimaryAdapter();
                eAddr = new org.safehaus.uuid.EthernetAddress(ea.getBytes());
            } catch (Exception ex) {
                log.error(BaseMessages.getString(PKG, "UUIDUtil.ERROR_0002_GET_MAC_ADDR"), ex); //$NON-NLS-1$
            } catch (UnsatisfiedLinkError ule) {
                log.error(BaseMessages.getString(PKG, "UUIDUtil.ERROR_0002_GET_MAC_ADDR"), ule); //$NON-NLS-1$
                nativeInitialized = false;
            }
        }

        /*
         * Add support for running in clustered environments. In this way, the MAC address of the
         * running server can be added to the environment with a -DMAC_ADDRESS=00:50:56:C0:00:01
         */
        if (eAddr == null) {
          String macAddr = System.getProperty("MAC_ADDRESS"); //$NON-NLS-1$
          if (macAddr != null) {
            // On Windows machines, people would be inclined to get the MAC
            // address with ipconfig /all. The format of this would be
            // something like 00-50-56-C0-00-08. So, replace '-' with ':' before
            // creating the address.
            // 
            macAddr = macAddr.replace('-', ':');
            eAddr = new org.safehaus.uuid.EthernetAddress(macAddr);
          }
        }

        if (eAddr == null) {
            // Still don't have an Ethernet Address - generate a dummy one.
            eAddr = ug.getDummyAddress();
        }

        // Generate a UUID to make sure everything is running OK.
        UUID olduuId = ug.generateTimeBasedUUID(eAddr);
        if (olduuId == null) {
          log.error(BaseMessages.getString(PKG, "UUIDUtil.ERROR_0003_GENERATEFAILED")); //$NON-NLS-1$
        }

    }

    public static String getUUIDAsString() {
        return getUUID().toString();
    }

    public static UUID getUUID() {
        return ug.generateTimeBasedUUID(eAddr);
    }

}
