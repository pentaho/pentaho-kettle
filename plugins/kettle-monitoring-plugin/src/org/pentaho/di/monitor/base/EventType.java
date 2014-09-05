/*!
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
* Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
*/
package org.pentaho.di.monitor.base;

public class EventType {

  public static enum Transformation {

    BEGIN_PREPARE_EXECUTION(1),
    META_LOADED(2),
    BEGIN_START(3),
    STARTED(4),
    FINISHED(5);

    int snmpId;

    Transformation(int snmpId){
      this.snmpId = snmpId;
    }

    public int getSnmpId(){
      return snmpId;
    }
  }

  public static enum Job {

    META_LOADED(1),
    STARTED(2),
    BEFORE_JOB_ENTRY(3),
    BEGIN_JOB_PROCESSING(4),
    AFTER_JOB_ENTRY(5),
    FINISHED(6);

    int snmpId;

    Job(int snmpId){
      this.snmpId = snmpId;
    }

    public int getSnmpId(){
      return snmpId;
    }
  }

  public static enum Step {

    BEFORE_INIT(1), AFTER_INIT(2), BEFORE_START(3), FINISHED(4);

    int snmpId;

    Step(int snmpId){
      this.snmpId = snmpId;
    }

    public int getSnmpId(){
      return snmpId;
    }
  }

  public static enum Database {

    DISCONNECTED(0),
    CONNECTED(1);

    int snmpId;

    Database(int snmpId){
      this.snmpId = snmpId;
    }

    public int getSnmpId(){
      return snmpId;
    }
  }

  public static enum Carte {

    SHUTDOWN(0),
    STARTUP(1);

    int snmpId;

    Carte(int snmpId){
      this.snmpId = snmpId;
    }

    public int getSnmpId(){
      return snmpId;
    }
  }

}
