package com.seven.asimov.test.tool.serialization;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.net.URI;

/**
 * Test - serializable class.
 *
 * @author Maksim Selivanov
 */

@Root
public class TestItem implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8277845627571143673L;

    @Element
    public int id;

    @Element(required = false)
    public String requestHeaders;

    @Element(required = false)
    public String requestContent;

    @Element(required = false)
    public int reiterations;

    @Element(required = false)
    public String belongs;

    @Element(required = false)
    public String scope;

    @Element(required = false)
    public String proxy;

    @Element(required = false)
    public Integer proxyPort;

    private String mHttpMethod;
    private URI mUri;

    public void setHttpMethod(String httpMethod) {
        this.mHttpMethod = httpMethod;
    }

    public String getHttpMethod() {
        return mHttpMethod;
    }

    public void setUri(URI uri) {
        this.mUri = uri;
    }

    public URI getUri() {
        return mUri;
    }

    public TestItem() {
    }

    public void setTestItem(TestItem testItem) {
        this.requestHeaders = testItem.getRequestHeaders();
        this.requestContent = testItem.getRequestContent();
        if (testItem.getHttpMethod() != null) {
            this.mHttpMethod = testItem.getHttpMethod();
        }
        if (testItem.getUri() != null) {
            this.mUri = testItem.getUri();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestheaders) {
        this.requestHeaders = requestheaders;
    }

    public String getRequestContent() {
        return requestContent;
    }

    public void setRequestContent(String requestcontent) {
        this.requestContent = requestcontent;
    }

    public int getReiterations() {
        return reiterations;
    }

    public void setReiterations(int reiterations) {
        this.reiterations = reiterations;
    }

    public String getBelongs() {
        return belongs;
    }

    public void setBelongs(String belongs) {
        this.belongs = belongs;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    private Boolean mPassedOk;

    public void setPassedOk(Boolean passedOk) {
        this.mPassedOk = passedOk;
    }

    public Boolean isPassedOk() {
        return mPassedOk;
    }
}
