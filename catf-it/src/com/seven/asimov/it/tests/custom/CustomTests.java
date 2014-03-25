package com.seven.asimov.it.tests.custom;

import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.testcases.CustomTestCase;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class CustomTests extends CustomTestCase {

    private static final String TAG = CustomTests.class.getSimpleName();
    final int SOCKET_TIMEOUT = 60 * 1000;
    URL url;
    HttpRequest[] requests;
    HttpRequest request;

    private static final String TRANSPARENT_PROPERTY_NAME = "transparent";
    private static final String WO_OC_PROPERTY_NAME = "enabled";
    private static final String HTTPS_WHITELIST_PATH = "@asimov@application@com.seven.asimov.it@ssl";
    private static final String HTTP_WHITELIST_PATH = "@asimov@http";
    private static final String TRANSPARENT_PATH = "@asimov";
    private static final String WO_OC_PATH = "@asimov";
    private static final String TRANSPARENT_ON = "1";
    private static final String OC_OFF = "0";

    //----------------------------------------HTTP lists----------------------------------------------------------------

    private static List<String> uris1kb = new ArrayList<String>() {{
        //add("https://upload.wikimedia.org/wikipedia/commons/f/f6/1kb.gif");
        add(createTestResourceUri("https_test_resourse_with_1Kb_response"));
    }};
    private static List<String> uris10kb = new ArrayList<String>() {{
        //add("https://upload.wikimedia.org/wikipedia/commons/f/f6/1kb.gif");
        add(createTestResourceUri("https_test_resourse_with_1Kb_response"));
    }};
    private static List<String> uris50kb = new ArrayList<String>() {{
        //add("https://upload.wikimedia.org/wikipedia/commons/f/f6/1kb.gif");
        add(createTestResourceUri("https_test_resourse_with_1Kb_response"));
    }};
    private static List<String> uris100kb = new ArrayList<String>() {{
        //add("https://upload.wikimedia.org/wikipedia/commons/f/f6/1kb.gif");
        add(createTestResourceUri("https_test_resourse_with_1Kb_response"));
    }};
    private static List<String> uris500kb = new ArrayList<String>() {{
        //add("https://upload.wikimedia.org/wikipedia/commons/f/f6/1kb.gif");
        add(createTestResourceUri("https_test_resourse_with_1Kb_response"));
    }};
    private static List<String> uris1Mb = new ArrayList<String>() {{
        //add("https://upload.wikimedia.org/wikipedia/commons/f/f6/1kb.gif");
        add(createTestResourceUri("https_test_resourse_with_1Kb_response"));
    }};

    //----------------------------------------HTTPS lists---------------------------------------------------------------
    private static List<String> uris1kbhttps = new ArrayList<String>() {{
        //add("https://upload.wikimedia.org/wikipedia/commons/f/f6/1kb.gif");
        add(createTestResourceUri("https_test_resourse_with_1Kb_response", true));
    }};
    private static List<String> uris10kbhttps = new ArrayList<String>() {{
        //add("https://upload.wikimedia.org/wikipedia/commons/4/49/Meyers_-_S%C3%A9l%C3%A9n%C3%A9.png");
        add(createTestResourceUri("https_test_resourse_with_10Kb_response", true));
    }};
    private static List<String> uris50kbhttps = new ArrayList<String>() {{
        //add("https://upload.wikimedia.org/wikipedia/commons/4/49/Meyers_-_S%C3%A9l%C3%A9n%C3%A9.png");
        add(createTestResourceUri("https_test_resourse_with_50Kb_response", true));
    }};
    private static List<String> uris100kbhttps = new ArrayList<String>() {{
        //add("https://upload.wikimedia.org/wikipedia/commons/4/49/Meyers_-_S%C3%A9l%C3%A9n%C3%A9.png");
        add(createTestResourceUri("https_test_resourse_with_100Kb_response", true));
    }};
    private static List<String> uris500kbhttps = new ArrayList<String>() {{
        //add("https://upload.wikimedia.org/wikipedia/commons/4/49/Meyers_-_S%C3%A9l%C3%A9n%C3%A9.png");
        add(createTestResourceUri("https_test_resourse_with_500Kb_response", true));
    }};
    private static List<String> uris1Mbhttps = new ArrayList<String>() {{
        //add("https://upload.wikimedia.org/wikipedia/commons/4/49/Meyers_-_S%C3%A9l%C3%A9n%C3%A9.png");
        add(createTestResourceUri("https_test_resourse_with_1000Kb_response", true));
    }};


    @LargeTest
    public void testHttpsMISSCertificateNotCached() throws Throwable {

        PrepareHttpsResources();
        String[] resources = new String[]{
//                // 1 kb
//                "https://q.bstatic.com/images/hotel/square40/461/4613613.jpg",
//                "https://bm.img.com.ua/a/berlin/common/img/label-exclusive_article.png",
//                //"https://fbcdn-photos-a.akamaihd.net/photos-ak-snc7/v43/70/45439413586/app_2_45439413586_6053.gif",
//                httpTestUri1Kb,
//
//                // 10 kb
//                // "https://fbcdn-sphotos-c-a.akamaihd.net/hphotos-ak-prn1/c7.0.133.133/p133x133/45034_10151225178885894_1060092258_n.jpg",
//                "https://wiki.pp-international.net/wiki/images/8/85/Flag_of_Belarus.svg",
//                "https://www.noao.edu/image_gallery/images/d3/4094a.jpg",
//                "https://www.mathworks.com/matlabcentral/fx_files/12262/1/figure1.png",
//
//                // 50 kb
//                "https://storage.ie6countdown.com/assets/100/images/banners/warning_bar_0000_us.jpg",
//                "https://www.intonow.com/img/ci/home_ipad.jpg",
//                "https://boatfloater.files.wordpress.com/2012/02/dragon-50kb-ycbcr.jpg?w=800",
//
//                // 100 kb
////                "https://bm.img.com.ua/a/berlin/project/ivon/tmp/gillette/i/footer.png",
//                "https://i.annihil.us/u/prod/marvel//universe3zx/images/4/41/Icarus_black.jpg",
//                //"http://clip2net.com/clip/m40267/1270379912-clip-100kb.jpg",
//                httpTestUri100Kb,
//                "https://images.wikia.com/dragonball/images/e/e3/Trunks_Ep.118.JPG",
//
//                // 500 kb
//                "https://lh6.googleusercontent.com/-yJcgnMxXf-U/T3Np4drtvUI/AAAAAAAAFvw/KJZ0Ofk8s2o/s800/pupsy_001.jpg",
//                "https://www.long-mcquade.com/files/2154/lg_100kb.jpg",
//                "https://fc05.deviantart.net/fs71/f/2010/038/8/d/Star_Burst_no_2_by_KevLewis.jpg", // 540
//
//                // 1 mb
//                "https://upload.wikimedia.org/wikipedia/commons/0/07/Quite_Disinterested%21.jpg",
//                "https://www.kpmg.com/NZ/en/PublishingImages/KPMG-Building-Wrap-1MB.jpg",
//                "https://static.cdn.ea.com/battlelog/prod/61d099d23fe104fe673091d470c96970/en_US/blog/en/files/2012/09/B2K_5F00_Wake_5F00_Island_5F00_Concept-1mb.jpg?v=1348677714.67"
                uris1kbhttps.get(0),
                uris10kbhttps.get(0),
                uris50kbhttps.get(0),
                uris100kbhttps.get(0),
                uris500kbhttps.get(0),
                uris1Mbhttps.get(0)
        };

        HttpRequest request = createRequest().setMethod("GET")
                .addHeaderField("Connection", "close")
                .addHeaderField("Cache-Control", "no-cache, no-store")
                .addHeaderField("Pragma", "no-cache")
                .getRequest();
        try {
            for (String resource : resources) {
                HttpRequest copy = request.copy();
                copy.setUri(resource);
                url = new URL(resource);
                sendQueue(SOCKET_TIMEOUT, url.getHost(), copy, true, false, 1);
            }
        }catch (Throwable e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    @LargeTest
    public void testHttpsMISSCertificateCached() throws Throwable {
        PrepareHttpsResources();

        requests = new HttpRequest[uris1kbhttps.size()];
        try {
            for (int i = 0; i < uris1kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris1kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris1kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10);
            }

            requests = new HttpRequest[uris10kbhttps.size()];
            for (int i = 0; i < uris10kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris10kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris10kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10);
            }

            requests = new HttpRequest[uris50kbhttps.size()];
            for (int i = 0; i < uris50kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris50kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris50kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10);
            }

            requests = new HttpRequest[uris100kbhttps.size()];
            for (int i = 0; i < uris100kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris100kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris100kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10);
            }

            requests = new HttpRequest[uris500kbhttps.size()];
            for (int i = 0; i < uris500kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris500kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris500kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10);
            }

            requests = new HttpRequest[uris1Mbhttps.size()];
            for (int i = 0; i < uris1Mbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris1Mbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris1Mbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10);
            }
        } catch (Throwable e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    @LargeTest
    public void testHttpsHIT() throws Throwable {
        PrepareHttpsResources();

        requests = new HttpRequest[uris1kbhttps.size()];
        try {
            for (int i = 0; i < uris1kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris1kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris1kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, false, 11);
            }

            requests = new HttpRequest[uris10kbhttps.size()];
            for (int i = 0; i < uris10kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris10kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris10kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, false, 11);
            }

            requests = new HttpRequest[uris50kbhttps.size()];
            for (int i = 0; i < uris50kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris50kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris50kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, false, 11);
            }

            requests = new HttpRequest[uris100kbhttps.size()];
            for (int i = 0; i < uris100kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris100kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris100kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, false, 11);
            }

            for (int i = 0; i < uris500kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris500kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris500kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, false, 11);
            }

            requests = new HttpRequest[uris1Mbhttps.size()];
            for (int i = 0; i < uris1Mbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris1Mbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris1Mbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, false, 11);
            }
        } catch (Throwable e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    @LargeTest
    public void testHttpMISSOnly() throws Throwable {
        PrepareHttpResources();

        requests = new HttpRequest[uris1kb.size()];
        try {
            for (int i = 0; i < uris1kb.size(); i++) {
                requests[i] = createRequest().setUri(uris1kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris1kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, true, 10);
            }

            requests = new HttpRequest[uris10kb.size()];
            for (int i = 0; i < uris10kb.size(); i++) {
                requests[i] = createRequest().setUri(uris10kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris10kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, true, 10);
            }

            requests = new HttpRequest[uris50kb.size()];
            for (int i = 0; i < uris50kb.size(); i++) {
                requests[i] = createRequest().setUri(uris50kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris50kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, true, 10);
            }

            requests = new HttpRequest[uris100kb.size()];
            for (int i = 0; i < uris100kb.size(); i++) {
                requests[i] = createRequest().setUri(uris100kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris100kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, true, 10);
            }

            requests = new HttpRequest[uris500kb.size()];
            for (int i = 0; i < uris500kb.size(); i++) {
                requests[i] = createRequest().setUri(uris500kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris500kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, true, 10);
            }

            requests = new HttpRequest[uris1Mb.size()];
            for (int i = 0; i < uris1Mb.size(); i++) {
                requests[i] = createRequest().setUri(uris1Mb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris1Mb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, true, 10);
            }
        } catch (Throwable e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    @LargeTest
    public void testHttpStream() throws Throwable {
        PrepareHttpResources();
        requests = new HttpRequest[uris1kb.size()];
        try {

            for (int i = 0; i < uris1kb.size(); i++) {
                requests[i] = createRequest().setUri(uris1kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris1kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, true, 10, HTTP_VERSION.HTTP21);
            }
            requests = new HttpRequest[uris10kb.size()];
            for (int i = 0; i < uris10kb.size(); i++) {
                requests[i] = createRequest().setUri(uris10kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris10kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, true, 10, HTTP_VERSION.HTTP21);
            }

            requests = new HttpRequest[uris50kb.size()];
            for (int i = 0; i < uris50kb.size(); i++) {
                requests[i] = createRequest().setUri(uris50kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris50kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, true, 10, HTTP_VERSION.HTTP21);
            }

            requests = new HttpRequest[uris100kb.size()];
            for (int i = 0; i < uris100kb.size(); i++) {
                requests[i] = createRequest().setUri(uris100kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris100kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, true, 10, HTTP_VERSION.HTTP21);
            }

            requests = new HttpRequest[uris500kb.size()];
            for (int i = 0; i < uris500kb.size(); i++) {
                requests[i] = createRequest().setUri(uris500kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris500kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, true, 10, HTTP_VERSION.HTTP21);
            }

            requests = new HttpRequest[uris1Mb.size()];
            for (int i = 0; i < uris1Mb.size(); i++) {
                requests[i] = createRequest().setUri(uris1Mb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris1Mb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, true, 10, HTTP_VERSION.HTTP21);
            }
        } catch (Throwable e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    @LargeTest
    public void testHttpsStream() throws Throwable {
        PrepareHttpsResources();
        requests = new HttpRequest[uris1kbhttps.size()];
        try {
            Log.i(TAG, " 1. Size is " + uris1kbhttps.size());
            for (int i = 0; i < uris1kbhttps.size(); i++) {
                Log.i(TAG, "Size is " + uris1kbhttps.size());
                requests[i] = createRequest().setUri(uris1kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris1kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10, HTTP_VERSION.HTTP21);
            }

            requests = new HttpRequest[uris10kbhttps.size()];
            for (int i = 0; i < uris10kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris10kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris10kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10, HTTP_VERSION.HTTP21);
            }

            requests = new HttpRequest[uris50kbhttps.size()];
            for (int i = 0; i < uris50kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris50kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris50kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10, HTTP_VERSION.HTTP21);
            }

            requests = new HttpRequest[uris100kbhttps.size()];
            for (int i = 0; i < uris100kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris100kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris100kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10, HTTP_VERSION.HTTP21);
            }

            for (int i = 0; i < uris500kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris500kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris500kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10, HTTP_VERSION.HTTP21);
            }

            requests = new HttpRequest[uris1Mbhttps.size()];
            for (int i = 0; i < uris1Mbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris1Mbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .addHeaderField("Cache-Control", "no-cache, no-store")
                        .addHeaderField("Pragma", "no-cache")
                        .getRequest();
                url = new URL(uris1Mbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10, HTTP_VERSION.HTTP21);
            }
        } catch (Throwable e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    @LargeTest
    public void testHttpHIT() throws Throwable {
        PrepareHttpResources();

        int count = 11;
        requests = new HttpRequest[uris1kb.size()];
        try {

            for (int i = 0; i < uris1kb.size(); i++) {
                requests[i] = createRequest().setUri(uris1kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris1kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, false, count);
            }
            requests = new HttpRequest[uris10kb.size()];
            for (int i = 0; i < uris10kb.size(); i++) {
                requests[i] = createRequest().setUri(uris10kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris10kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, false, count);
            }

            requests = new HttpRequest[uris50kb.size()];
            for (int i = 0; i < uris50kb.size(); i++) {
                requests[i] = createRequest().setUri(uris50kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris50kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, false, count);
            }

            requests = new HttpRequest[uris100kb.size()];
            for (int i = 0; i < uris100kb.size(); i++) {
                requests[i] = createRequest().setUri(uris100kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris100kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, false, count);
            }

            requests = new HttpRequest[uris500kb.size()];
            for (int i = 0; i < uris500kb.size(); i++) {
                requests[i] = createRequest().setUri(uris500kb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris500kb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, false, count);
            }

            requests = new HttpRequest[uris1Mb.size()];
            for (int i = 0; i < uris1Mb.size(); i++) {
                requests[i] = createRequest().setUri(uris1Mb.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris1Mb.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], false, false, count);
            }
        } catch (Throwable e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    //This test is deprecated
    @Ignore
    @LargeTest
    public void testHttpsHITLocalRemoteHS() throws Throwable {
        requests = new HttpRequest[uris1kbhttps.size()];
        try {
            for (int i = 0; i < uris1kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris1kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris1kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, false, 4);
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10);
            }

            requests = new HttpRequest[uris10kbhttps.size()];
            for (int i = 0; i < uris10kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris10kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris10kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, false, 4);
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10);
            }

            requests = new HttpRequest[uris50kbhttps.size()];
            for (int i = 0; i < uris50kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris50kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris50kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, false, 4);
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10);
            }

            requests = new HttpRequest[uris100kbhttps.size()];
            for (int i = 0; i < uris100kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris100kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris100kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, false, 4);
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10);
            }

            requests = new HttpRequest[uris500kbhttps.size()];
            for (int i = 0; i < uris500kbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris500kbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris500kbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, false, 4);
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10);
            }

            requests = new HttpRequest[uris1Mbhttps.size()];
            for (int i = 0; i < uris1Mbhttps.size(); i++) {
                requests[i] = createRequest().setUri(uris1Mbhttps.get(i)).setMethod("GET")
                        .addHeaderField("Connection", "close")
                        .getRequest();
                url = new URL(uris1Mbhttps.get(i));
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, false, 4);
                sendQueue(SOCKET_TIMEOUT, url.getHost(), requests[i], true, true, 10);
            }
        } catch (Throwable e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    public void testHttpTransparent() throws Throwable {
        Log.i(TAG, "Test HTTP Transparent started");
        String transparentPropertyId = null;
        try {
            transparentPropertyId = PMSUtil.createPersonalScopeProperty(TRANSPARENT_PROPERTY_NAME,
                    TRANSPARENT_PATH, TRANSPARENT_ON, true);
            assertTrue("Expected that \"HTTP transparent\" personal scope property was created! Therefore test was missed", transparentPropertyId != null);
            Log.i(TAG, "\"HTTP transparent\" personal scope property was created with id = \"" + transparentPropertyId + "\"");
            testHttpMISSOnly();
        } finally {
            if (transparentPropertyId != null) {
                PMSUtil.deleteProperty(transparentPropertyId);
            }
        }
        Log.i(TAG, "Test HTTP Transparent finished");
    }

    public void testHttpWhiteList() throws Throwable {
        Policy policy = new Policy("blacklist", AsimovTestCase.TEST_RESOURCE_HOST, HTTP_WHITELIST_PATH, true);
        Log.i(TAG, "Test HTTP White List started");
        try {
            PMSUtil.addPolicies(new Policy[]{policy});
            testHttpMISSOnly();
        } finally {
            PMSUtil.cleanPaths(new String[]{HTTP_WHITELIST_PATH});
        }
        Log.i(TAG, "Test HTTP White List finished");
    }

    public void testHttpWithoutOC() throws Throwable {
        Log.i(TAG, "Test HTTP without OC started");
        String propertyId = null;
        try {
            propertyId = PMSUtil.createPersonalScopeProperty(WO_OC_PROPERTY_NAME, WO_OC_PATH, OC_OFF, true);
            assertTrue("Expected that \"HTTP Disable OC\" personal scope property was created! Therefore test was missed", propertyId != null);
            Log.i(TAG, "\"HTTP Disable OC\" personal scope property was created with id = " + propertyId);
            testHttpMISSOnly();
        } finally {
            if (null != propertyId) {
                PMSUtil.deleteProperty(propertyId);
            }
        }
        Log.i(TAG, "Test HTTP without OC finished");
    }

    public void testHttpsTransparent() throws Throwable {
        Log.i(TAG, "Test HTTPS Transparent started");
        String propertyId = null;
        try {
            propertyId = PMSUtil.createPersonalScopeProperty(TRANSPARENT_PROPERTY_NAME,
                    TRANSPARENT_PATH, TRANSPARENT_ON, true);
            assertTrue("Expected that \"HTTPS transparent\" personal scope property was created! Therefore test was missed", propertyId != null);
            Log.i(TAG, "\"HTTPS transparent\" personal scope property was created with id = " + propertyId);
            testHttpsMISSCertificateCached();
        } finally {
            if (null != propertyId) {
                PMSUtil.deleteProperty(propertyId);
            }
        }
        Log.i(TAG, "Test HTTPS Transparent finished");
    }

    public void testHttpsWithoutOC() throws Throwable {
        Log.i(TAG, "Test HTTPS without OC started");
        String propertyId = null;
        try {
            propertyId = PMSUtil.createPersonalScopeProperty(WO_OC_PROPERTY_NAME, WO_OC_PATH, OC_OFF, true);
            assertTrue("Expected that \"Disable OC\" personal scope property was created! Therefore test was missed", propertyId != null);
            Log.i(TAG, "\"Disable OC\" personal scope property was created with id = " + propertyId);
            testHttpsMISSCertificateCached();
        } finally {
            if (null != propertyId) {
                PMSUtil.deleteProperty(propertyId);
                Log.i(TAG, "\"Disable OC\" personal scope property was deleted with id = " + propertyId);
            }
        }
        Log.i(TAG, "Test HTTPS without OC finished");
    }

    public void testHttpsWhiteList() throws Throwable {
        Policy policy = new Policy("enabled", "false", HTTPS_WHITELIST_PATH, true);
        Log.i(TAG, "Test HTTPS White List started");
        try {
            PMSUtil.addPolicies(new Policy[]{policy});
            TestUtil.sleep(60 * 1000);
            testHttpsMISSCertificateCached();
        } finally {
            PMSUtil.cleanPaths(new String[]{HTTPS_WHITELIST_PATH});
        }
        Log.i(TAG, "Test HTTPS White List finished");
    }

    public static HttpResponse PrepareResourceWithSize(String uri, int size) throws Exception {
        System.out.println("Preparing test resource...");
        String rawHeaders = "Cache-Control: max-age=1000";
        String encodedRawHeaders =  URLEncoder.encode(Base64.encodeToString(rawHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ChangeResponseContentSize", size + ", abcdefghigklmnopqrst")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-AddRawHeadersPermanently", encodedRawHeaders)
                .getRequest();
        HttpResponse response = sendRequest2(request, false, true);
        return response;
    }

    public static void PrepareHttpResources() throws Exception {

        PrepareResourceWithSize(uris1kb.get(0),1024);
        PrepareResourceWithSize(uris10kb.get(0),10240);
        PrepareResourceWithSize(uris50kb.get(0),51200);
        PrepareResourceWithSize(uris100kb.get(0),102400);
        PrepareResourceWithSize(uris500kb.get(0),512000);
        PrepareResourceWithSize(uris1Mb.get(0),1024000);
    }

    public static void PrepareHttpsResources() throws Exception {

        PrepareResourceWithSize(uris1kbhttps.get(0), 1024);
        PrepareResourceWithSize(uris10kbhttps.get(0), 10240);
        PrepareResourceWithSize(uris50kbhttps.get(0), 51200);
        PrepareResourceWithSize(uris100kbhttps.get(0), 102400);
        PrepareResourceWithSize(uris500kbhttps.get(0), 512000);
        PrepareResourceWithSize(uris1Mbhttps.get(0), 1024000);
    }
}
