/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.www;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.pms.util.Const;

public class CarteServlet extends HttpServlet {

  private static final long serialVersionUID = 2434694833497859776L;
  
  private final List<SlaveServerDetection> detections = Collections.synchronizedList(new ArrayList<SlaveServerDetection>());

  private AddExportServlet addExportServlet = new AddExportServlet();
  private AddJobServlet addJobServlet = new AddJobServlet();
  private AddTransServlet addTransServlet = new AddTransServlet();
  private AllocateServerSocketServlet allocateServerSocketServlet = new AllocateServerSocketServlet();
  private ListServerSocketServlet listServerSocketServlet = new ListServerSocketServlet();
  private NextSequenceValueServlet nextSequenceValueServlet = new NextSequenceValueServlet();
  private CleanupTransServlet cleanupTransServlet = new CleanupTransServlet();
  private GetJobStatusServlet getJobStatusServlet = new GetJobStatusServlet();
  private GetSlavesServlet getSlavesServlet = new GetSlavesServlet(detections, false);
  private GetStatusServlet getStatusServlet = new GetStatusServlet();
  private GetTransStatusServlet getTransStatusServlet = new GetTransStatusServlet();
  private PauseTransServlet pauseTransServlet = new PauseTransServlet();
  private PrepareExecutionTransServlet prepareExecutionTransServlet = new PrepareExecutionTransServlet();
  private RegisterSlaveServlet registerSlaveServlet = new RegisterSlaveServlet(detections, false);
  private RemoveTransServlet removeTransServlet = new RemoveTransServlet();
  private RemoveJobServlet removeJobServlet = new RemoveJobServlet();
  private SniffStepServlet sniffStepServlet = new SniffStepServlet();
  private StartExecutionTransServlet startExecutionTransServlet = new StartExecutionTransServlet();
  private ExecuteTransServlet executeTransServlet = new ExecuteTransServlet();
  private StartJobServlet startJobServlet = new StartJobServlet();
  private StartTransServlet startTransServlet = new StartTransServlet();
  private StopJobServlet stopJobServlet = new StopJobServlet();
  private StopTransServlet stopTransServlet = new StopTransServlet();
  
  public CarteServlet() {
  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req, resp);
  }

  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String uri = req.getRequestURI();
    if (uri.contains(AddExportServlet.CONTEXT_PATH)) {
      addExportServlet.doGet(req, resp);
    } else if (uri.contains(AddJobServlet.CONTEXT_PATH)) {
      addJobServlet.doGet(req, resp);
    } else if (uri.contains(AddTransServlet.CONTEXT_PATH)) {
      addTransServlet.doGet(req, resp);
    } else if (uri.contains(AllocateServerSocketServlet.CONTEXT_PATH)) {
      allocateServerSocketServlet.doGet(req, resp);
    } else if (uri.contains(ListServerSocketServlet.CONTEXT_PATH)) {
      listServerSocketServlet.doGet(req, resp);
    } else if (uri.contains(NextSequenceValueServlet.CONTEXT_PATH)) {
      nextSequenceValueServlet.doGet(req, resp);
    } else if (uri.contains(CleanupTransServlet.CONTEXT_PATH)) {
      cleanupTransServlet.doGet(req, resp);
    } else if (uri.contains(GetJobStatusServlet.CONTEXT_PATH)) {
      getJobStatusServlet.doGet(req, resp);
    } else if (uri.contains(GetSlavesServlet.CONTEXT_PATH)) {
      getSlavesServlet.doGet(req, resp);
    } else if (uri.contains(GetStatusServlet.CONTEXT_PATH)) {
      getStatusServlet.doGet(req, resp);
    } else if (uri.contains(GetTransStatusServlet.CONTEXT_PATH)) {
      getTransStatusServlet.doGet(req, resp);
    } else if (uri.contains(PauseTransServlet.CONTEXT_PATH)) {
      pauseTransServlet.doGet(req, resp);
    } else if (uri.contains(PrepareExecutionTransServlet.CONTEXT_PATH)) {
      prepareExecutionTransServlet.doGet(req, resp);
    } else if (uri.contains(RegisterSlaveServlet.CONTEXT_PATH)) {
      registerSlaveServlet.doGet(req, resp);
    } else if (uri.contains(StartExecutionTransServlet.CONTEXT_PATH)) {
      startExecutionTransServlet.doGet(req, resp);
    } else if (uri.contains(ExecuteTransServlet.CONTEXT_PATH)) {
      executeTransServlet.doGet(req, resp);
    } else if (uri.contains(StartJobServlet.CONTEXT_PATH)) {
      startJobServlet.doGet(req, resp);
    } else if (uri.contains(StartTransServlet.CONTEXT_PATH)) {
      startTransServlet.doGet(req, resp);
    } else if (uri.contains(StopJobServlet.CONTEXT_PATH)) {
      stopJobServlet.doGet(req, resp);
    } else if (uri.contains(StopTransServlet.CONTEXT_PATH)) {
      stopTransServlet.doGet(req, resp);
    } else if (uri.contains(RemoveTransServlet.CONTEXT_PATH)) {
      removeTransServlet.doGet(req, resp);
    } else if (uri.contains(RemoveJobServlet.CONTEXT_PATH)) {
      removeJobServlet.doGet(req, resp);
    } else if (uri.contains(SniffStepServlet.CONTEXT_PATH)) {
      sniffStepServlet.doGet(req, resp);
    }
  }

  @Override 
  public void init(ServletConfig config) throws ServletException {
     
    try {
       String startJobClass = config.getInitParameter("startJobServletClass"); 
       if (!Const.isEmpty(startJobClass)) {
          Class clazz = Class.forName(startJobClass);
          startJobServlet = (StartJobServlet)clazz.newInstance();
       } 
       
       String startExecutionTransClass = config.getInitParameter("startExecutionTransServletClass"); 
       if (!Const.isEmpty(startExecutionTransClass)) {
          Class clazz = Class.forName(startExecutionTransClass);
          startExecutionTransServlet = (StartExecutionTransServlet)clazz.newInstance();
       }
       
       String startTransClass = config.getInitParameter("startTransServletClass");
       if (!Const.isEmpty(startTransClass)) {
          Class clazz = Class.forName(startTransClass);
          this.startTransServlet = (StartTransServlet)clazz.newInstance();
       }
       
       String executeTransClass = config.getInitParameter("executeTransServletClass");
       if (!Const.isEmpty(executeTransClass)) {
          Class clazz = Class.forName(executeTransClass);
          this.executeTransServlet = (ExecuteTransServlet)clazz.newInstance();
       }
     }
     catch (ClassNotFoundException cnfe) {
        throw new ServletException(cnfe);
     }    
     catch (IllegalAccessException iae) {
        throw new ServletException(iae);
     }
     catch (InstantiationException ie) {
        throw new ServletException(ie);
     }
     catch (ClassCastException cce) {
        throw new ServletException(cce);
     }
  }
}
