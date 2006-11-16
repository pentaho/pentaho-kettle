package be.ibridge.kettle.www;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class WrappedHttpRequest implements HttpServletRequest {
    private final HttpServletRequest httpRequest;

    public WrappedHttpRequest(HttpServletRequest httpRequest) {
        super();
        this.httpRequest = httpRequest;
    }

    public String getAuthType() {

        return httpRequest.getAuthType();
    }

    public Cookie[] getCookies() {

        return httpRequest.getCookies();
    }

    public String getMethod() {

        return httpRequest.getMethod();
    }

    public String getParameter(String name) {

        return httpRequest.getParameter(name);
    }

    public String getRemoteAddr() {

        return httpRequest.getRemoteAddr();
    }

    public String getRemoteHost() {

        return httpRequest.getRemoteHost();
    }

    public StringBuffer getRequestURL() {

        return httpRequest.getRequestURL();
    }


    public String getScheme() {

        return httpRequest.getScheme();
    }

    public Principal getUserPrincipal() {

        return httpRequest.getUserPrincipal();
    }

    public boolean isUserInRole(String role) {

        return httpRequest.isUserInRole(role);
    }

    public Object getAttribute(String name) {

        return httpRequest.getAttribute(name);
    }

    public Enumeration getAttributeNames() {

        return httpRequest.getAttributeNames();
    }

    public String getCharacterEncoding() {

        return httpRequest.getCharacterEncoding();
    }

    public int getContentLength() {

        return httpRequest.getContentLength();
    }

    public String getContentType() {

        return httpRequest.getContentType();
    }

    public void removeAttribute(String name) {

        httpRequest.removeAttribute(name);
    }

    public long getDateHeader(String arg0) {

        return 0;
    }

    public Enumeration getHeaders(String arg0) {

        return null;
    }

    public Enumeration getHeaderNames() {

        return null;
    }

    public int getIntHeader(String arg0) {

        return 0;
    }

    public String getPathInfo() {

        return null;
    }

    public String getPathTranslated() {

        return null;
    }

    public String getContextPath() {

        return null;
    }

    public String getQueryString() {

        return null;
    }

    public String getRemoteUser() {

        return null;
    }

    public String getRequestedSessionId() {

        return null;
    }

    public String getRequestURI() {

        return null;
    }

    public String getServletPath() {

        return null;
    }

    public HttpSession getSession(boolean arg0) {

        return null;
    }

    public HttpSession getSession() {

        return null;
    }

    public boolean isRequestedSessionIdValid() {

        return false;
    }

    public boolean isRequestedSessionIdFromCookie() {

        return false;
    }

    public boolean isRequestedSessionIdFromURL() {

        return false;
    }

    public boolean isRequestedSessionIdFromUrl() {

        return false;
    }

    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {

    }

    public Map getParameterMap() {

        return null;
    }

    public String getProtocol() {

        return null;
    }

    public String getServerName() {

        return null;
    }

    public int getServerPort() {

        return 0;
    }

    public BufferedReader getReader() throws IOException {

        return null;
    }

    public Locale getLocale() {

        return null;
    }

    public Enumeration getLocales() {

        return null;
    }

    public boolean isSecure() {

        return false;
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {

        return null;
    }

    public String getRealPath(String arg0) {

        return null;
    }

    public int getRemotePort() {

        return 0;
    }

    public String getLocalName() {

        return null;
    }

    public String getLocalAddr() {

        return null;
    }

    public int getLocalPort() {

        return 0;
    }

    public ServletInputStream getInputStream() throws IOException {
        return new WrappedServletInputStream(httpRequest.getInputStream());
    }

    public Enumeration getParameterNames() {

        return null;
    }

    public String[] getParameterValues(String arg0) {

        return null;
    }

    public void setAttribute(String arg0, Object arg1) {

    }

    public String getHeader(String arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
