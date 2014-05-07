package com.seven.asimov.it.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.HttpParams;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

//import n.security.util.AuthResources;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/18/14
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class RestClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(RestClientFactory.class);

    private String host;
    private String saPort;
    public ClientExecutor executor;
    public DefaultHttpClient httpClient;

    public static final String REST_BASE_URL = "http://{0}:{1}/sa-rest/rest{2}";

/*
    static {
        ResteasyProviderFactory.setRegisterBuiltinByDefault(false);}
*/

    public RestClientFactory(String host, String port) throws Exception {
        this.host = host;
        this.saPort = port;

        logger.debug("Rest server:"+host+" port:"+port);


        CookieSpecFactory csf = new CookieSpecFactory() {
            public CookieSpec newInstance(HttpParams params) {
                return new BrowserCompatSpec() {
                    @Override
                    public void validate(Cookie cookie, CookieOrigin origin)
                            throws MalformedCookieException {
                    }
                };
            }
        };


        httpClient = new DefaultHttpClient();
        httpClient.getCookieSpecs().register("easy", csf);
        httpClient.getParams().setParameter(
                ClientPNames.COOKIE_POLICY, "easy");
       /* httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                CookiePolicy.BROWSER_COMPATIBILITY);*/

        HttpResponse res=authoriseClient();
        if (!res.getStatusLine().toString().contains("200")){
            throw new Exception("Login in sa system failed");
        }

        initExecutor();
    }

    public <T> T getClient(String path, Class<T> classz) throws Exception{
        String url = MessageFormat.format(REST_BASE_URL, host, saPort, path);
        logger.debug("Getting service provider from :"+url);

        T simple = ProxyFactory.create(classz, url, executor);

        return simple;

    }


    private HttpResponse authoriseClient() throws Exception {
        HttpResponse responseLogin=null;

        String login = MessageFormat.format(REST_BASE_URL, host, saPort, "/login");
        logger.debug("Rest server login service url:"+login);

        HttpPost post = new HttpPost(login);
        post.addHeader("accept", "application/json,application/xml");

        try {
            StringEntity input = new StringEntity("{\"username\":\"admin\",\"password\":\"admin\"}");
            input.setContentType("application/json");
            post.setEntity(input);
            responseLogin = httpClient.execute(post);

            logger.trace(responseLogin.getStatusLine().toString());
        } finally {
            post.abort();
        }

        return responseLogin;

    }

    private void initExecutor() {
        executor = new ApacheHttpClient4Executor(httpClient);
    }

    public boolean isSessionExpired(){
        return true;
    }

    public void closeRestClient(){
        httpClient.getConnectionManager().shutdown();
    }


}
