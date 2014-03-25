package com.seven.asimov.it.tests.generic.application.handler.networktype;


import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.NetworktypeConditionTestsCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;

/**
 * NetworkType condition tests
 * The tests are deprecated and will be rewritten according
 * ASMV-22583 Make NetworktypeCondition automated test cases more suitable
 */
@Ignore
public class NetworktypeConditionTests extends NetworktypeConditionTestsCase {
    public void test_000_AddDropSessionProperty() throws Throwable {
        MobileNetworkUtil mobileNetworkUtil = MobileNetworkUtil.init(getContext());
        mobileNetworkUtil.on3gOnly();
        operateDropSessionProperty(true);
    }

    /**
     * Verify detection of wi-fi networktype condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: wifi
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * <p/>
     * 1. Connect to wi-fi
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * <p/>
     * Wi-fi networktype condition should be activated
     */
    public void test_001_TC() throws Throwable {
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "wifi", true, WIFI_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of wimax networktype condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: wimax
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * 2. Ensure that device support wimax technology
     * <p/>
     * 1. Connect to wimax
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * <p/>
     * Wimax networktype condition should be activated
     */
    public void test_002_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "wimax", true, WIMAX_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of mobile_3gpp networktype condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: mobile_3gpp
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * 2. Ensure that device support mobile_3gpp technology
     * <p/>
     * 1. Connect to mobile_3gpp
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * <p/>
     * Mobile_3gpp networktype condition should be activated
     */
    public void test_003_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "mobile_3gpp", true, MOBILE_3GPP_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of mobile_3gpp2 networktype condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: mobile_3gpp2
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * 2. Ensure that device support mobile_3gpp2 technology
     * <p/>
     * 1. Connect to mobile_3gpp2
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * <p/>
     * *Mobile_3gpp2 networktype condition should be activated
     */
    public void test_004_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "mobile_3gpp2", true, MOBILE_3GPP_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of mobile_lte networktype condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype:  mobile_lte
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * 2. Ensure that device support mobile_lte technology
     * <p/>
     * 1. Connect to mobile_lte
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * <p/>
     * Mobile_lte networktype condition should be activated
     */
    public void test_005_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "mobile_lte", true, MOBILE_LTE_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of mobile_iden networktype condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype:  mobile_iden
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * 2. Ensure that device support mobile_iden technology
     * <p/>
     * 1. Connect to mobile_iden
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * <p/>
     * Mobile_iden networktype condition should be activated
     */
    public void test_006_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "mobile_iden", true, MOBILE_IDEN_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of mobile networktype condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype:  mobile
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * 2. Ensure that device support mobile technology
     * <p/>
     * 1. Connect to 3G
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * <p/>
     * Mobile networktype condition should be activated
     */
    public void test_007_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "mobile", true, MOBILE_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify that non-correct netwotktype condition is not applied
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: network
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * <p/>
     * 1. Observe logcat
     * <p/>
     * *Policy should be received but not applied
     */
    public void test_009_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "network", false, 9));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify that empty netwotktype condition is not applied
     * <p/>
     * 1. Configure policy rule:
     * Dont add value to the scripts@<script_name>@conditions@networktype
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * <p/>
     * 1. Observe logcat
     * <p/>
     * Policy should be received but not applied
     */
    public void test_010_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "", false, 9));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of wi-fi networktype exit condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: mobile
     * Add value to the scripts@<script_name>@exit_conditions@networktype: wifi
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * <p/>
     * 1. Connect to 3G
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * 4. Connect to wifi
     * 5. Load http://www.amazon.com/
     * <p/>
     * Wi-fi networktype exit condition should be activated on step 4
     */
    public void test_011_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "mobile", true, MOBILE_NETWORK));
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@exit_conditions", "wifi", true, WIFI_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of wimax networktype exit condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: mobile
     * Add value to the scripts@<script_name>@exit_conditions@networktype: wimax
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * 2. Ensure that device support wimax technology"
     * <p/>
     * 1. Connect to 3G
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * 4. Connect to wimax
     * 5. Load http://www.amazon.com/
     * <p/>
     * Wimax networktype exit condition should be activated on step 4
     */
    public void test_012_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "mobile", true, MOBILE_NETWORK));
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@exit_conditions", "wimax", true, WIMAX_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of mobile_3gpp networktype exit condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: wifi
     * Add value to the scripts@<script_name>@exit_conditions@networktype: mobile_3gpp
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * 2. Ensure that device support mobile_3gpp technology
     * <p/>
     * 1. Connect to wi-fi
     * 2. Ensure that policy was received and applied
     * 3. Connect to 3G
     * 4. Load http://www.amazon.com/
     * <p/>
     * Mobile_3gpp networktype exit condition should be activated on step 4
     */
    public void test_013_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "wifi", true, WIFI_NETWORK));
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@exit_conditions", "mobile_3gpp", true, MOBILE_3GPP_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of mobile_3gpp2 networktype exit condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: wifi
     * Add value to the scripts@<script_name>@exit_conditions@networktype: mobile_3gpp2
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * 2. Ensure that device support mobile_3gpp2 technology
     * <p/>
     * 1. Connect to wi-fi
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * 4. Connect to mobile_3gpp2
     * 5. Load http://www.amazon.com/
     * <p/>
     * Mobile_3gpp2 networktype exit condition should be activated on step 4
     */
    public void test_014_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "wifi", true, WIFI_NETWORK));
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@exit_conditions", "mobile_3gpp2", true, MOBILE_3GPP2_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of mobile_lte networktype exit condition
     * <p/>
     * 1. Configure policy rule
     * Add value to the scripts@<script_name>@conditions@networktype: wifi
     * Add value to the scripts@<script_name>@exit_conditions@networktype: mobile_lte
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * 2. Ensure that device support mobile_lte technology
     * <p/>
     * <p/>
     * 1. Connect to wi-fi
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * 4. Connect to mobile_lte
     * 5. Load http://www.amazon.com/
     * <p/>
     * Mobile_lte networktype exit condition should be activated on step 4
     */
    public void test_015_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "wifi", true, WIFI_NETWORK));
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@exit_conditions", "mobile_lte", true, MOBILE_LTE_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of mobile_lte networktype exit condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: wifi
     * Add value to the scripts@<script_name>@exit_conditions@networktype: mobile_iden
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * 2. Ensure that device support mobile_iden technology
     * <p/>
     * <p/>
     * 1. Connect to wi-fi
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * 4. Connect to mobile_iden
     * 5. Load http://www.amazon.com/
     * <p/>
     * Mobile_iden networktype exit condition should be activated on step 4
     */
    public void test_016_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "wifi", true, WIFI_NETWORK));
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@exit_conditions", "mobile_iden", true, MOBILE_IDEN_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify detection of mobile networktype exit condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: wifi
     * Add value to the scripts@<script_name>@exit_conditions@networktype: mobile
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * 2. Ensure that device support mobile technology
     * <p/>
     * 1. Connect to wi-fi
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * 4. Connect to 3G
     * 5. Load http://www.amazon.com/
     * <p/>
     * Mobile networktype exit condition should be activated on step 4
     */
    public void test_017_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "wifi", true, WIFI_NETWORK));
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@exit_conditions", "mobile", true, MOBILE_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify that non-correct netwotktype exit condition is not applied
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: wifi  Add value to the scripts@<script_name>@exit_conditions@networktype: network
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * <p/>
     * 1. Observe logcat
     * <p/>
     * Policy should be received but not applied
     */
    public void test_018_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "wifi", true, WIFI_NETWORK));
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@exit_conditions", "network", true, MOBILE_NETWORK));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify that empty netwotktype exit condition is not applied
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: wifi
     * Don't add value to the scripts@<script_name>@exit_conditions@networktype
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * <p/>
     * 1. Observe logcat
     * <p/>
     * Policy should be received but not applied
     */
    public void test_019_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "wifi", true, WIFI_NETWORK));
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@exit_conditions", "", true, 9));
        networktypeConditionTest(managedProperties);
    }

    /**
     * Verify that netwotktype exit condition is not applied without enter condition
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@exit_conditions@networktype: mobile
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * <p/>
     * 1. Observe logcat
     * <p/>
     * Policy should be received but not applied
     */
    public void test_020_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@exit_conditions", "mobile", false, MOBILE_NETWORK));
        networktypeConditionTest(managedProperties);

    }

    /**
     * Verify detection of networktype conditions when enter end exit conditions are the same
     * <p/>
     * 1. Configure policy rule:
     * Add value to the scripts@<script_name>@conditions@networktype: wifi
     * Add value to the scripts@<script_name>@exit_conditions@networktype: wifi
     * Add value to the scripts@<script_name>@actions@drop_sessions: port_range=80:100 package=com\.android.*
     * <p/>
     * 1. Connect to wi-fi
     * 2. Ensure that policy was received and applied
     * 3. Load http://www.amazon.com/
     * <p/>
     * Wi-fi networktype condition should be activated. Wi-fi networktype exit condition should be activated.
     */
    public void test_021_TC() throws Throwable {
        managedProperties.clear();
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@conditions", "wifi", true, WIFI_NETWORK));
        managedProperties.add(new Property("networktype", "@asimov@application@com.seven.asimov.it@scripts@script_networktype@exit_conditions", "wifi", true, WIFI_NETWORK));
        networktypeConditionTest(managedProperties);
        operateDropSessionProperty(false);
    }
}
