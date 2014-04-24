package com.seven.asimov.it.rest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    private static final String REST_BASE_URL = "http://{0}:{1}/sa-rest/rest{2}";

    public RestClientFactory(String host, String port) {
        this.host = host;
        this.saPort = port;
    }

    public <T> T getClient(String path, Class<T> classz) throws Exception {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response=authoriseClient(httpClient);
        if (!response.getStatusLine().toString().contains("200")){
            throw new Exception("SA login failed");
        }
        logger.debug("Http client login successful");

        String url = MessageFormat.format(REST_BASE_URL, host, saPort, path);
        ClientExecutor executor = new ApacheHttpClient4Executor(httpClient);

        T simple = ProxyFactory.create(classz, url, executor);
        return simple;


    }

    private HttpResponse authoriseClient(DefaultHttpClient client) throws Exception {
        String login = MessageFormat.format(REST_BASE_URL, host, saPort, "/login");
        HttpPost post = new HttpPost(login);
        post.addHeader("accept", "application/json,application/xml");

        try {
            StringEntity input = new StringEntity("{\"username\":\"admin\",\"password\":\"admin\"}");
            input.setContentType("application/json");
            post.setEntity(input);
            HttpResponse responseLogin = client.execute(post);

            logger.trace(responseLogin.getStatusLine().toString());
            return responseLogin;
        } catch (IOException e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
            throw new Exception(e);
        } finally {
            post.releaseConnection();
        }

    }

}
