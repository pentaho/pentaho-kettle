package org.pentaho.di.trans.steps.hl7input.common;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobListener;

import ca.uhn.hl7v2.protocol.StreamSource;
import ca.uhn.hl7v2.protocol.impl.MLLPTransport;
import ca.uhn.hl7v2.protocol.impl.ServerSocketStreamSource;

public class MLLPSocketCache {

  private static MLLPSocketCache cache;
  
  private Map<String, MLLPSocketCacheEntry> map;
  
  public static MLLPSocketCache getInstance() {
    if (cache==null) {
      cache = new MLLPSocketCache();
    }
    return cache;
  }
  
  public MLLPSocketCache() {
    map = new HashMap<String, MLLPSocketCacheEntry>();
  }
  
  public MLLPSocketCacheEntry getServerSocketStreamSource(String server, int port) throws Exception {
    final String key = createKey(server,port);
    MLLPSocketCacheEntry s = map.get(key);
    if (s!=null) return s;
    
    // Open the socket for this server/port combination.
    //
    ServerSocket serverSocket = new ServerSocket(port);
    StreamSource streamSource = new ServerSocketStreamSource(serverSocket, server);
    MLLPTransport transport = new MLLPTransport(streamSource);
    transport.connect();
    
    final MLLPSocketCacheEntry entry = new MLLPSocketCacheEntry(serverSocket, streamSource, transport);
    entry.setJobListener(new JobListener() {
      
      @Override
      public void jobFinished(Job job) throws KettleException {
        KettleException exception = null;
        try {
          entry.getTransport().disconnect();
        } catch(Exception e) {
          exception=new KettleException(e);
        }
        try {
          entry.getStreamSource().disconnect();
        } catch(Exception e) {
          exception=new KettleException(e);
        }
        try {
          entry.getServerSocket().close();
        } catch(Exception e) {
          exception=new KettleException(e);
        }
        
        map.remove(key);
        
        if (exception!=null) {
          throw exception;
        }
      }
    });
    // Store a copy in our map to make sure that only the first return value contains the job listener.
    //
    map.put(key, new MLLPSocketCacheEntry(serverSocket, streamSource, transport));
    
    return entry;
  }
  
  private String createKey(String server, int port) {
    return server+":"+port;
  }
}
