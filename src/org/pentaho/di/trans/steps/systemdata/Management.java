/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.trans.steps.systemdata;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

import org.pentaho.di.core.Const;

import com.sun.management.OperatingSystemMXBean;

public class Management {
	private static RuntimeMXBean mx=null ;
	private static OperatingSystemMXBean bean=null;
	private static ThreadMXBean tbean = null;
	/**
	 * Return infos like current PID, JVM memory, ...
	 * 
	 * @author Samatar
	 * @since 2008-apr-29
	 */
	
	 /** 
     * @return Process CPU time in nanoseconds 
     */
    public static long getPID()
    {
    	String pid=null;
    	if(mx==null)
    	{
    		mx = ManagementFactory.getRuntimeMXBean();
    	}
    		
		pid=mx.getName();
		int indexOf=pid.indexOf("@");
		if(indexOf>0) pid=pid.substring(0,indexOf);
        return Const.toInt(pid,-1);
    }
    /** 
     * @return JVM CPU time in nanoseconds 
     */
    public static long getJVMCpuTime( ) {
    	setOperatingSystemMXBean();
    	if ( ! (bean instanceof OperatingSystemMXBean) )
    		return 0L;
    	return ((OperatingSystemMXBean)bean).getProcessCpuTime();
    }
    /** 
     * @return the amount of free physical memory in bytes
     */
    public static long getFreePhysicalMemorySize() {
    	setOperatingSystemMXBean();

        if ( ! (bean instanceof OperatingSystemMXBean) )
            return 0L;
        return ((OperatingSystemMXBean)bean).getFreePhysicalMemorySize();
    }
    /** 
     * @return the amount of free swap space in bytes
     */
    public static long getFreeSwapSpaceSize() {
    	setOperatingSystemMXBean();

        if ( ! (bean instanceof OperatingSystemMXBean) )
            return 0L;
        return ((OperatingSystemMXBean)bean).getFreeSwapSpaceSize();
    }
    /** 
     * @return the total amount of physical memory in bytes
     */
    public static long getTotalPhysicalMemorySize() {
    	setOperatingSystemMXBean();

        if ( ! (bean instanceof OperatingSystemMXBean) )
            return 0L;
        return ((OperatingSystemMXBean)bean).getTotalPhysicalMemorySize( );
    }
    /** 
     * @return the total amount of swap space in bytes.
     */
    public static long getTotalSwapSpaceSize() {
    	setOperatingSystemMXBean();

        if ( ! (bean instanceof OperatingSystemMXBean) )
            return 0L;
        return ((OperatingSystemMXBean)bean).getTotalSwapSpaceSize();
    }
    
    /** 
     * @return the amount of virtual memory that is guaranteed to be available to the running process in bytes
     */
    public static long getCommittedVirtualMemorySize() {
    	setOperatingSystemMXBean();

        if ( ! (bean instanceof OperatingSystemMXBean) )
            return 0L;
        return ((OperatingSystemMXBean)bean).getCommittedVirtualMemorySize();
    }
    /**
     *  @return CPU time in nanoseconds. 
     */
   public static long getCpuTime( long id ) {
	   setThreadMXBean();
        if ( ! tbean.isThreadCpuTimeSupported( ) )
            return 0L;
        return  tbean.getThreadCpuTime( id );
    }
   
    private static void setOperatingSystemMXBean(){
    	if(bean==null){
            bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean( );
    	}
    }
    private static void setThreadMXBean(){
    	if(tbean==null){
    		tbean = ManagementFactory.getThreadMXBean( );
    	}
    } 
}
