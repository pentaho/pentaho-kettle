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

package org.pentaho.di.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.methods.GetMethod;

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
    
    /**
     * Performs a get on urlAsString using username and password as credentials.
     * 
     * If the status code returned not -1 and 401 then the contents are returned.
     * If the status code is 401 an AuthenticationException is thrown.
     * 
     * All other values of status code are not dealt with but logic can be 
     * added as needed.
     * 
     * @param urlAsString
     * @param username
     * @param password
     * @param encoding
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public String get(String urlAsString, String username, String password) 
        throws MalformedURLException, IOException, AuthenticationException {
        
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(urlAsString);        
        httpClient.getParams().setAuthenticationPreemptive(true);
        Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
        httpClient.getState().setCredentials(AuthScope.ANY, defaultcreds);
        int statusCode = httpClient.executeMethod(getMethod);
        StringBuffer bodyBuffer = new StringBuffer();
        
        if (statusCode!= -1) {
            if (statusCode != 401) {
        
                // the response
                InputStreamReader inputStreamReader = new InputStreamReader(getMethod.getResponseBodyAsStream()); 
                 
                int c;
                while ( (c=inputStreamReader.read())!=-1) {
                    bodyBuffer.append((char)c);
                }
                inputStreamReader.close(); 
                
            }
            else {
                throw new AuthenticationException();
            }
        }
        
        // Display response
        return bodyBuffer.toString();  
    }
}
