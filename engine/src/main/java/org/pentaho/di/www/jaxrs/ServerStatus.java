/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.www.jaxrs;

import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

@XmlRootElement
public class ServerStatus {

  private String statusDescription;
  private String errorDescription;

  private long memoryFree;
  private long memoryTotal;

  private int cpuCores;
  private long cpuProcessTime;

  private long uptime;

  private int threadCount;

  private double loadAvg;

  private String osName;

  private String osVersion;

  private String osArchitecture;

  public ServerStatus() {
    OperatingSystemMXBean operatingSystemMXBean =
      java.lang.management.ManagementFactory.getOperatingSystemMXBean();
    ThreadMXBean threadMXBean = java.lang.management.ManagementFactory.getThreadMXBean();
    RuntimeMXBean runtimeMXBean = java.lang.management.ManagementFactory.getRuntimeMXBean();

    int cores = Runtime.getRuntime().availableProcessors();

    long freeMemory = Runtime.getRuntime().freeMemory();
    long totalMemory = Runtime.getRuntime().totalMemory();
    String osArch = operatingSystemMXBean.getArch();
    String osName = operatingSystemMXBean.getName();
    String osVersion = operatingSystemMXBean.getVersion();
    double loadAvg = operatingSystemMXBean.getSystemLoadAverage();

    int threadCount = threadMXBean.getThreadCount();
    long allThreadsCpuTime = 0L;

    long[] threadIds = threadMXBean.getAllThreadIds();
    for ( int i = 0; i < threadIds.length; i++ ) {
      allThreadsCpuTime += threadMXBean.getThreadCpuTime( threadIds[i] );
    }

    long uptime = runtimeMXBean.getUptime();

    setCpuCores( cores );
    setCpuProcessTime( allThreadsCpuTime );
    setUptime( uptime );
    setThreadCount( threadCount );
    setLoadAvg( loadAvg );
    setOsName( osName );
    setOsVersion( osVersion );
    setOsArchitecture( osArch );
    setMemoryFree( freeMemory );
    setMemoryTotal( totalMemory );
  }

  public ServerStatus( String statusDescription ) {
    this();
    this.statusDescription = statusDescription;
  }

  public ServerStatus( Node statusNode ) throws KettleException {
    this();
    statusDescription = XMLHandler.getTagValue( statusNode, "statusdesc" );

    memoryFree = Const.toLong( XMLHandler.getTagValue( statusNode, "memory_free" ), -1L );
    memoryTotal = Const.toLong( XMLHandler.getTagValue( statusNode, "memory_total" ), -1L );

    String cpuCoresStr = XMLHandler.getTagValue( statusNode, "cpu_cores" );
    cpuCores = Const.toInt( cpuCoresStr, -1 );
    String cpuProcessTimeStr = XMLHandler.getTagValue( statusNode, "cpu_process_time" );
    cpuProcessTime = Utils.isEmpty( cpuProcessTimeStr ) ? 0L : Long.valueOf( cpuProcessTimeStr );

    uptime = Const.toLong( XMLHandler.getTagValue( statusNode, "uptime" ), -1 );
    threadCount = Const.toInt( XMLHandler.getTagValue( statusNode, "thread_count" ), -1 );
    loadAvg = Const.toDouble( XMLHandler.getTagValue( statusNode, "load_avg" ), -1.0 );

    osName = XMLHandler.getTagValue( statusNode, "os_name" );
    osVersion = XMLHandler.getTagValue( statusNode, "os_version" );
    osArchitecture = XMLHandler.getTagValue( statusNode, "os_arch" );
  }

  /**
   * @return the statusDescription
   */
  public String getStatusDescription() {
    return statusDescription;
  }

  /**
   * @param statusDescription
   *          the statusDescription to set
   */
  public void setStatusDescription( String statusDescription ) {
    this.statusDescription = statusDescription;
  }

  /**
   * @return the errorDescription
   */
  public String getErrorDescription() {
    return errorDescription;
  }

  /**
   * @param errorDescription
   *          the errorDescription to set
   */
  public void setErrorDescription( String errorDescription ) {
    this.errorDescription = errorDescription;
  }

  /**
   * @return the memoryFree
   */
  public double getMemoryFree() {
    return memoryFree;
  }

  /**
   * @param memoryFree
   *          the memoryFree to set
   */
  public void setMemoryFree( long memoryFree ) {
    this.memoryFree = memoryFree;
  }

  /**
   * @return the memoryTotal
   */
  public double getMemoryTotal() {
    return memoryTotal;
  }

  /**
   * @param memoryTotal
   *          the memoryTotal to set
   */
  public void setMemoryTotal( long memoryTotal ) {
    this.memoryTotal = memoryTotal;
  }

  /**
   * @return the cpuCores
   */
  public int getCpuCores() {
    return cpuCores;
  }

  /**
   * @param cpuCores
   *          the cpuCores to set
   */
  public void setCpuCores( int cpuCores ) {
    this.cpuCores = cpuCores;
  }

  /**
   * @return the cpuProcessTime
   */
  public long getCpuProcessTime() {
    return cpuProcessTime;
  }

  /**
   * @param cpuProcessTime
   *          the cpuProcessTime to set
   */
  public void setCpuProcessTime( long cpuProcessTime ) {
    this.cpuProcessTime = cpuProcessTime;
  }

  public void setUptime( long uptime ) {
    this.uptime = uptime;
  }

  public long getUptime() {
    return uptime;
  }

  public void setThreadCount( int threadCount ) {
    this.threadCount = threadCount;
  }

  public int getThreadCount() {
    return threadCount;
  }

  public void setLoadAvg( double loadAvg ) {
    this.loadAvg = loadAvg;
  }

  public double getLoadAvg() {
    return loadAvg;
  }

  public void setOsName( String osName ) {
    this.osName = osName;
  }

  public String getOsName() {
    return osName;
  }

  public void setOsVersion( String osVersion ) {
    this.osVersion = osVersion;
  }

  public String getOsVersion() {
    return osVersion;
  }

  public void setOsArchitecture( String osArch ) {
    this.osArchitecture = osArch;
  }

  public String getOsArchitecture() {
    return osArchitecture;
  }
}
