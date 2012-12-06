package org.pentaho.di.profiling.datacleaner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.pentaho.di.core.logging.LogChannelInterface;

/**
 * Reads an output stream from an external process. Implemented as a thread.
 */
class ProcessStreamReader extends Thread {

    BufferedReader in;
    LogChannelInterface log;
    boolean logWarn;

    final static int BUFFER_SIZE = 1024;

    /**
     * Creates new ProcessStreamReader object.
     * 
     * @param in
     * @param logChannelInterface
     */
    ProcessStreamReader(InputStream in, LogChannelInterface log, boolean logWarn) {
        super();

        this.log = log;
        this.logWarn = logWarn;
        this.in = new BufferedReader(new InputStreamReader(in));

        super.setName("process stream reader");
    }

    public void run() {
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                if (logWarn) {
                    log.logError("DC: " + line);
                } else {
                    log.logBasic("DC: " + line);
                }
            }
        } catch (IOException e) {
            log.logError("DC: Unexpected IO exception", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

}
