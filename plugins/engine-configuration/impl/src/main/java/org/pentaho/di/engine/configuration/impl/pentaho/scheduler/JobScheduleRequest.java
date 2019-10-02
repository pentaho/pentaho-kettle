/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.di.engine.configuration.impl.pentaho.scheduler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement( name = "jobScheduleRequest" )
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( propOrder = { "inputFile", "jobParameters", "pdiParameters" } )
public class JobScheduleRequest implements Serializable {

  private static final long serialVersionUID = -485489832281790257L;

  private String inputFile;

  private List<JobScheduleParam> jobParameters = new ArrayList<>();

  private Map<String, String> pdiParameters = new HashMap<>();

  public String getInputFile() {
    return inputFile;
  }

  public void setInputFile( String file ) {
    this.inputFile = file;
  }

  public List<JobScheduleParam> getJobParameters() {
    return jobParameters;
  }

  public void setJobParameters( List<JobScheduleParam> jobParameters ) {
    this.jobParameters = jobParameters;
  }

  public Map<String, String> getPdiParameters() {
    return pdiParameters;
  }

  public void setPdiParameters( Map<String, String> pdiParameters ) {
    this.pdiParameters = pdiParameters;
  }

}
