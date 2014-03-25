package com.seven.asimov.it.base.interfaces;

import java.io.IOException;
import java.net.HttpURLConnection;

public interface HttpUrlConnectionIF {

    void decorate(HttpURLConnection conn) throws IOException;
}
