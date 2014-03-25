package com.seven.asimov.it.exception.z7;

public class Z7Error extends Exception {

    private Z7ErrorCode m_errorCode;
    private Z7Result m_resultCode;
    private String m_description = null;
    private Z7Error m_nestedError = null;
    private Throwable m_cause;

    //https://jira.seven.com/requests/browse/ZSEVEN-14077
    //This one is used for displaying dynamic parameters in error text on client side.
    //It is a transient field to hold dynamic content, be fore it is serialzied, the m_description
    //field should be replace by contnet from parameters by updateDescriptionWithParameters(), in format of "{parameters[0]},{paramters[1]}...".
    private transient String[] m_parameters = null;

    public Z7Error(Z7ErrorCode errorCode, Z7Result resultCode, String description, Z7Error nestedError) {
        init(errorCode, resultCode, description, nestedError);
    }
    public Z7Error(Z7ErrorCode errorCode) {
        init(errorCode, errorCode == Z7ErrorCode.Z7_ERR_NOERROR ? Z7Result.Z7_OK : Z7Result.Z7_E_FAIL, null, null);
    }
    public Z7Error(Z7ErrorCode errorCode, Z7Result resultCode) {
        init(errorCode, resultCode, null, null);
    }
    public Z7Error(Z7ErrorCode errorCode, String description) {
        init(errorCode, errorCode == Z7ErrorCode.Z7_ERR_NOERROR ? Z7Result.Z7_OK : Z7Result.Z7_E_FAIL, description, null);
    }
    public Z7Error(Z7ErrorCode errorCode, Z7Error nestedError) {
        init(errorCode, errorCode == Z7ErrorCode.Z7_ERR_NOERROR ? Z7Result.Z7_OK : (nestedError != null ? nestedError.m_resultCode : Z7Result.Z7_E_FAIL), null, nestedError);
    }
    public Z7Error(Z7ErrorCode errorCode, Z7Result resultCode, String description) {
        init(errorCode, resultCode, description, null);
    }
    public Z7Error(Z7ErrorCode errorCode, Z7Result resultCode, Z7Error nestedError) {
        init(errorCode, resultCode, null, nestedError);
    }
    public Z7Error(Z7ErrorCode errorCode, String description, Z7Error nestedError) {
        init(errorCode, errorCode == Z7ErrorCode.Z7_ERR_NOERROR ? Z7Result.Z7_OK : (nestedError != null ? nestedError.m_resultCode : Z7Result.Z7_E_FAIL), null, nestedError);
    }
    public Z7Error(Z7ErrorCode errorCode, Z7Result resultCode, String description, Throwable thr) {
        init(errorCode, resultCode, description, null);
        m_cause = thr;
    }
    public Z7Error(Throwable thr) {
        this(Z7ErrorCode.Z7_ERR_INTERNAL_ERROR, thr);
    }
    public Z7Error(Z7ErrorCode errorCode, Throwable thr) {
        // TODO JAVAMAIL: remove this workaround once the bug causing circular reference in javax.mail.MessagingException has a been fixed
        this(errorCode, errorCode == Z7ErrorCode.Z7_ERR_NOERROR ? Z7Result.Z7_OK : Z7Result.Z7_E_FAIL, (thr.getMessage() != null ? (thr.getClass().getName() + ": " + thr.getMessage()) : thr.getClass().getName()), thr);
    }
    public Z7Error(Z7ErrorCode errorCode, Z7Result resultCode, Throwable thr) {
        // TODO JAVAMAIL: remove this workaround once the bug causing circular reference in javax.mail.MessagingException has a been fixed
        this(errorCode, resultCode, (thr.getMessage() != null ? (thr.getClass().getName() + ": " + thr.getMessage()) : thr.getClass().getName()), thr);
    }
    public static Z7Error asZ7Error(Throwable thr) {
        if (thr instanceof Z7Error) {
            return (Z7Error)thr;
        }
        return new Z7Error(thr);
    }

    private void init(Z7ErrorCode errorCode, Z7Result resultCode, String description, Z7Error nestedError) {
        m_errorCode = errorCode;
        m_resultCode = resultCode;
        m_description = description;
        m_nestedError = nestedError;
    }

    public Z7ErrorCode getErrorCode() {
        return m_errorCode;
    }

    public void setErrorCode(Z7ErrorCode errorCode) {
        m_errorCode = errorCode;
    }

    public Z7Result getResultCode() {
        return m_resultCode;
    }

    public void setResultCode(Z7Result resultCode) {
        m_resultCode = resultCode;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public Throwable getCause() {
        return m_cause;
    }

    public Z7Error getNestedError() {
        return m_nestedError;
    }

    public void setNestedError(Z7Error nestedError) {
        m_nestedError = nestedError;
    }

    public void setParameters(String[] parameters) {
        this.m_parameters = parameters;
    }

    /**
     * https://jira.seven.com/requests/browse/ZSEVEN-14077
     * If there is parameters, then return the formated parameters in String "{parameters[0]},{parameters[1]}..."
     * If m_parameters is null, return description. This is originally only used by Marshaller.writeError().
     */
    public String getDescriptionOrParameters() {
        String result = this.m_description;
        if (this.m_parameters != null && this.m_parameters.length > 0) {
            result = "{" + this.m_parameters[0] + "}";
            for (int i=1; i<this.m_parameters.length; i++) {
                result += ",{" + this.m_parameters[i] + "}";
            }
        }

        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        toString(sb);
        return sb.toString();
    }

    public void toString(StringBuffer sb) {
        sb.append("Error(code=");
        sb.append(m_errorCode);
        sb.append(", result=");
        sb.append(m_resultCode);
        if (m_description != null) {
            sb.append(", description='");
            sb.append(m_description);
            sb.append('\'');
        }
        if (m_nestedError != null) {
            sb.append(", nested=");
            m_nestedError.toString(sb);
        }
        sb.append(')');
    }

    public void printStackTrace() {
        super.printStackTrace();
        if(m_cause!=null) {
            // TODO JAVAMAIL: remove this workaround once the bug causing circular reference in javax.mail.MessagingException has a been fixed
            if (!"javax.mail.MessagingException".equals(m_cause.getClass().getName())){
                m_cause.printStackTrace();
            }
        }
        else {
            if (m_nestedError != null) {
                m_nestedError.printStackTrace();
            }
        }
    }
}

