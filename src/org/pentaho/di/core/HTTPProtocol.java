package org.pentaho.di.core;

/**
 * HTTP
 * 
 * This class contains HTTP protocol properties such as request headers.
 * Response headers and other properties of the HTTP protocol 
 * can be added to this class.
 * 
 * @author sflatley
 *
 */
public class HTTPProtocol {

    /* Array of HTTP request headers- this list is incomplete and
     * more headers can be added as needed.
     */
    
    private final static String[] requestHeaders = 
            {"accept","accept-charset","cache-control", "content-type" };
    
    /**
     * @return array of HTTP request headers
     */
    public static String[] getRequestHeaders() {
        return requestHeaders;
    }
}
