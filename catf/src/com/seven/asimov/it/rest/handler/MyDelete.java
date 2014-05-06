package com.seven.asimov.it.rest.handler;

import org.apache.http.client.methods.HttpPost;

import javax.ws.rs.HttpMethod;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/28/14
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyDelete extends HttpPost {
    public MyDelete(String url){
        super(url);

    }

    @Override
    public String getMethod(){

        return HttpMethod.DELETE;
    }
}
