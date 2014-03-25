package com.seven.asimov.it.utils.logcat.wrappers;

public class IndexWrapper {

    private static IndexWrapper instance;

    private int idDispatcher = 0;
    private int idCLQ = 0;
    private int idCSQ = 0;
    private int idTrxExec = 0;
    private int idVerdict = 0;
    private int idAppName = 0;
    private int idNetlog = 0;
    private int idErrTrx = 0;
    private int idErrMesTrx = 0;
    private int idFcn = 0;
    private int idFcp = 0;
    private int idGte = 0;
    private int idFcl = 0;
    private int idEas = 0;

    private IndexWrapper(){
        // do nothing
    }

    public static IndexWrapper getInstance(){
        if (instance == null){
            instance = new IndexWrapper();
        }
        return instance;
    }

    public synchronized int getDispatcherId(){
        return ++idDispatcher;
    }

    public synchronized int getCLQId(){
        return ++idCLQ;
    }

    public synchronized  int getCSQId(){
        return ++idCSQ;
    }

    public synchronized int getTrxExecId(){
        return ++idTrxExec;
    }

    public synchronized  int getVerdictId(){
        return ++idVerdict;
    }

    public synchronized int getAppNameId(){
        return ++idAppName;
    }

    public synchronized int getNetlogId(){
        return ++idNetlog;
    }

    public synchronized int getTrxErrId(){
        return ++idErrTrx;
    }

    public synchronized int getTrxErrMesId(){
        return ++idErrMesTrx;
    }

    public synchronized int getFcnId(){
        return ++idFcn;
    }

    public synchronized int getFcpId(){
        return ++idFcp;
    }

    public synchronized int getGteId(){
        return ++idGte;
    }

    public synchronized int getFclId(){
        return ++idFcl;
    }

    public synchronized int getEasId(){
        return  ++idEas;
    }
}
