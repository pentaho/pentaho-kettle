package be.ibridge.kettle.trans.step.socketwriter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerClientSocket implements Runnable
{
    private ServerSocket serverSocket;
    private int port;
    private boolean stopped;
    
    public ServerClientSocket(int port)
    {
        this.port = port;
        stopped=false;
    }
    
    public void run()
    {
        try
        {
            serverSocket = new ServerSocket(port);
            while (!stopped && !serverSocket.isClosed())
            {
                try { Thread.sleep(10); } catch(InterruptedException e) {};
            }
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public Socket accept() throws IOException
    {
        return serverSocket.accept();
    }

    /**
     * @return the port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * @return the serverSocket
     */
    public ServerSocket getServerSocket()
    {
        return serverSocket;
    }

    /**
     * @param serverSocket the serverSocket to set
     */
    public void setServerSocket(ServerSocket serverSocket)
    {
        this.serverSocket = serverSocket;
    }

    /**
     * @return the stopped
     */
    public boolean isStopped()
    {
        return stopped;
    }

    /**
     * @param stopped the stopped to set
     */
    public void setStopped(boolean stopped)
    {
        this.stopped = stopped;
    }
}

