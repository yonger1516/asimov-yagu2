package com.seven.asimov.it.utils.logcat.wrappers;

public enum OperationType {
    //    opTypes.put(1,"proxy_unknown");
//    opTypes.put(2,"proxy_cacheable");
//    opTypes.put(3,"proxy_uncacheable");
//    opTypes.put(4,"proxy_blacklisted");
//    opTypes.put(5,"proxy_dns");
//    opTypes.put(6,"proxy_dns_delayed");
//    opTypes.put(7,"proxy_bypass");
//    opTypes.put(8,"proxy_https_handshake");
//    opTypes.put(9,"deferred_app_close");
//    opTypes.put(10,"deferred_idle_close");
//    opTypes.put(11,"proxy_tcp");
//    opTypes.put(12,"proxy_cacheable_app_compressed");
//    opTypes.put(13,"proxy_uncacheable_app_compressed");
//    opTypes.put(14,"z7_unknown");
//    opTypes.put(15,"z7_ping");
//    opTypes.put(16,"z7_ack");
//    opTypes.put(17,"z7_nack");
//    opTypes.put(18,"z7_data");
//    opTypes.put(19,"z7_pack");
//    opTypes.put(20,"z7_status");
//    opTypes.put(21,"z7_invalidation_data");
//    opTypes.put(22,"z7_start_poll_cmd");
//    opTypes.put(23,"z7_stop_poll_cmd");
//    opTypes.put(24,"z7_heartbeat_data");
//    opTypes.put(25,"z7_mixed_list");
//    opTypes.put(26,"z7_set_clumping");
//    opTypes.put(27,"z7_report");
//    opTypes.put(28,"z7_policy_get");
//    opTypes.put(29,"z7_policy_update");
//    opTypes.put(30,"z7_get_cached_data");
//    opTypes.put(31,"z7_keepalive");
//    opTypes.put(32,"z7_keepalive");
//    opTypes.put(33,"fauxy_dns");
//    opTypes.put(34,"radio_up");
//    opTypes.put(35,"proxy_deffered_close");
//    opTypes.put(36,"unknown");
    unknown,
    proxy_unknown,
    proxy_cacheable,
    proxy_uncacheable,
    proxy_blacklisted,
    proxy_dns,
    proxy_dns_delayed,
    proxy_bypass,
    proxy_https_handshake,
    deferred_app_close,
    deferred_idle_close,
    proxy_tcp,
    proxy_cacheable_app_compressed,
    proxy_uncacheable_app_compressed,
    z7_unknown,
    z7_ping,
    z7_ack,
    z7_nack,
    z7_data,
    z7_pack,
    z7_status,
    z7_invalidation_data,
    z7_start_poll_cmd,
    z7_stop_poll_cmd,
    z7_heartbeat_data,
    z7_mixed_list,
    z7_set_clumping,
    z7_report,
    z7_policy_get,
    z7_policy_update,
    z7_get_cached_data,
    z7_keepalive,
    fauxy_dns,
    radio_up,
    proxy_deffered_close,
    proxy_ssl_local_hs,
    proxy_ssl_remote_hs,
    proxy_stream
}

//class test{
//    public static void main(String [] args){
//        OperationType t = OperationType.z7_report;
//         System.out.println(t.toString());
//        System.out.println(t.ordinal());
//    }
//}