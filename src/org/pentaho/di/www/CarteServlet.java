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
package org.pentaho.di.www;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CarteServlet extends HttpServlet {

  private static final long serialVersionUID = 2434694833497859776L;

  private AddExportServlet addExportServlet = new AddExportServlet();
  private AddJobServlet addJobServlet = new AddJobServlet();
  private AddTransServlet addTransServlet = new AddTransServlet();
  private AllocateServerSocketServlet allocateServerSocketServlet = new AllocateServerSocketServlet();
  private ListServerSocketServlet listServerSocketServlet = new ListServerSocketServlet();
  private NextSequenceValueServlet nextSequenceValueServlet = new NextSequenceValueServlet();
  private CleanupTransServlet cleanupTransServlet = new CleanupTransServlet();
  private GetJobStatusServlet getJobStatusServlet = new GetJobStatusServlet();
  private GetSlavesServlet getSlavesServlet = new GetSlavesServlet();
  private GetStatusServlet getStatusServlet = new GetStatusServlet();
  private GetTransStatusServlet getTransStatusServlet = new GetTransStatusServlet();
  private PauseTransServlet pauseTransServlet = new PauseTransServlet();
  private PrepareExecutionTransServlet prepareExecutionTransServlet = new PrepareExecutionTransServlet();
  private RegisterSlaveServlet registerSlaveServlet = new RegisterSlaveServlet();
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

}
