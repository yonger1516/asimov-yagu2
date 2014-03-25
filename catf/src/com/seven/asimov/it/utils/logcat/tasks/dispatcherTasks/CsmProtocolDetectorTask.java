package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CsmPortocolDetecorWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsmProtocolDetectorTask extends Task<CsmPortocolDetecorWrapper> {

    private static final String csmConnectorCreatedRegexp =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.+Connector.cpp:[0-9]+.\\s\\([0-9]+\\) - CSM \\[([0-9A-Z]+)\\] Сonnector created \\((.+)\\), socket: ([^,]+), addr: (.+)";

    private static final String csmHandleWriteRegexp =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.+Connector.cpp:[0-9]+.\\s\\([0-9]+\\) - CSM \\[([0-9A-Z]+)\\] handle_write_event\\(\\) trying to connect";

    private static final String csmWriteStartingRegexp =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.+AbstractSocketWorker.cpp:[0-9]+.\\s\\([0-9]+\\) - CSM \\[([0-9A-Z]+)\\] AbstractSocketWorker::write_to_socket\\(\\) write starting...";

    private static final String csmWriteRegexp =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.+AbstractSocketWorker.cpp:[0-9]+.\\s\\([0-9]+\\) - CSM \\[([0-9A-Z]+)\\] AbstractSocketWorker::write_to_socket\\(\\) trn_id\\(([0-9]+)\\) bucket_id\\([0-9]+\\) size [0-9]+ msg_type 5";

    private static final String csmSessionGetOutRegexp = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.+Session.cpp:[0-9]+.\\s\\([0-9]+\\) - CSM \\[([0-9A-Z]+)\\] jni//TCPDispatcher/Session.cpp::get_out\\(\\). remote_addr: .+, proxy_addr: .+, protocols stack: .+";

    private static final String csmOutManager = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.+Manager.cpp:[0-9]+.\\s\\([0-9]+\\) - CSM \\[([0-9A-Z]+)\\] OUT::Manager::connect_out\\(.+\\) for TRX \\[.+\\]";

    private static final String csmOutError = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.+http_task.hpp:[0-9]+.\\s\\([0-9]+\\) - Constructed TDR1 from ochttpd, CSM \\[([0-9A-Z]+)\\] TS \\[[0-9A-Z]+\\] HTRX \\[[0-9A-Z]+\\] \\(bfc [0-9]+, btc [0-9]+, bts [0-9]+, bfs [0-9]+\\) Type 1:proxy_stream,  UID [0-9]+, state 1:fg, proto -\\/-\\/unknown\\/tcp4\\:\\-\\/\\-\\/unknown\\/tcp4, TS .+, MonoTS .+, MonoEndTS .+, transparent 1, errcode -32012, DST: orig .+, proxied_to .+ resolution TS .+ radio_aligned [0-9]+";

    private Pattern ccrPattern;

    public enum CSM_TASK {
       CSM_CONNECTOR, CSM_HANDLE, CSM_WRITE_START, CSM_WRITE, CSM_SESSION_GETOUT, CSM_OUT_MANAGER, CSM_OUT_ERROR
    }

    public CsmProtocolDetectorTask(CSM_TASK csmTaskType) {
         switch (csmTaskType) {
             case CSM_CONNECTOR:
                 ccrPattern = Pattern.compile(csmConnectorCreatedRegexp);
                 break;
             case CSM_HANDLE:
                 ccrPattern = Pattern.compile(csmHandleWriteRegexp);
                 break;
             case CSM_WRITE_START:
                 ccrPattern = Pattern.compile(csmWriteStartingRegexp);
                 break;
             case CSM_WRITE:
                 ccrPattern = Pattern.compile(csmWriteRegexp);
                 break;
             case CSM_SESSION_GETOUT:
                 ccrPattern = Pattern.compile(csmSessionGetOutRegexp);
                 break;
             case CSM_OUT_MANAGER:
                 ccrPattern = Pattern.compile(csmOutManager);
                 break;
             case CSM_OUT_ERROR:
                 ccrPattern = Pattern.compile(csmOutError);
                 break;
         }
    }

    protected CsmPortocolDetecorWrapper parseLine(String line) {
        Matcher matcher = ccrPattern.matcher(line);
        if (matcher.find()) {
            CsmPortocolDetecorWrapper wrapper = new CsmPortocolDetecorWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setCsmId(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
