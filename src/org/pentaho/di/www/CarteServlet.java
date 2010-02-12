package org.pentaho.di.www;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CarteServlet extends HttpServlet {

  private static final long serialVersionUID = 2434694833497859776L;
  // by
  private AddExportServlet addExportServlet = new AddExportServlet();
  private AddJobServlet addJobServlet = new AddJobServlet();
  private AddTransServlet addTransServlet = new AddTransServlet();
  private AllocateServerSocketServlet allocateServerSocketServlet = new AllocateServerSocketServlet();
  private CleanupTransServlet cleanupTransServlet = new CleanupTransServlet();
  private GetJobStatusServlet getJobStatusServlet = new GetJobStatusServlet();
  private GetSlavesServlet getSlavesServlet = new GetSlavesServlet();
  private GetStatusServlet getStatusServlet = new GetStatusServlet();
  private GetTransStatusServlet getTransStatusServlet = new GetTransStatusServlet();
  private PauseTransServlet pauseTransServlet = new PauseTransServlet();
  private PrepareExecutionTransServlet prepareExecutionTransServlet = new PrepareExecutionTransServlet();
  private RegisterSlaveServlet registerSlaveServlet = new RegisterSlaveServlet();
  private StartExecutionTransServlet startExecutionTransServlet = new StartExecutionTransServlet();
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
    } else if (uri.contains(StartJobServlet.CONTEXT_PATH)) {
      startJobServlet.doGet(req, resp);
    } else if (uri.contains(StartTransServlet.CONTEXT_PATH)) {
      startTransServlet.doGet(req, resp);
    } else if (uri.contains(StopJobServlet.CONTEXT_PATH)) {
      stopJobServlet.doGet(req, resp);
    } else if (uri.contains(StopTransServlet.CONTEXT_PATH)) {
      stopTransServlet.doGet(req, resp);
    }
  }

}
