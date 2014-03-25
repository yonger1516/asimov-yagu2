package com.seven.asimov.it.utils.pcf;

import android.util.Log;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.it.utils.pms.z7.IntArrayMap;
import com.seven.asimov.it.utils.pms.z7.Marshaller;
import com.seven.asimov.it.utils.pms.z7.Z7TransportAddress;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.seven.asimov.it.base.constants.TFConstantsIF.*;

public class PcfHelper {
    public static enum Method {
        POST, GET, DELETE, PUT
    }

    public static enum Version {
        SAVED("SAVED"), COMMITTED("COMMITTED"), ROLLBACK1("ROLLBACK1"), ROLLBACK2("ROLLBACK2");

        Version(String value) {
            this.value = value;
        }

        private String value;

        @Override
        public String toString() {
            return value;
        }

        public static Version getEnum(String value) {
            if (value == null)
                throw new IllegalArgumentException();
            for (Version v : values())
                if (value.equalsIgnoreCase(v.toString())) return v;
            throw new IllegalArgumentException();
        }
    }

    public static enum Status {
        UPTODATE("UPTODATE"), ADDED("ADDED"), REMOVED("REMOVED"), CHANGED("CHANGED");

        Status(String value) {
            this.value = value;
        }

        private String value;

        @Override
        public String toString() {
            return value;
        }

        public static Status getEnum(String value) {
            if (value == null)
                throw new IllegalArgumentException();
            for (Status v : values())
                if (value.equalsIgnoreCase(v.toString())) return v;
            throw new IllegalArgumentException();
        }
    }

    public static enum InterfaceType {
        ALL("all"), WIFI("wifi"), WIMAX("wimax"), MOBILE_3GPP("mobile_3gpp"), MOBILE_3GPP2("mobile_3gpp2"), MOBILE_LTE("mobile_lte"), MOBILE_IDEN("mobile_iden"), MOBILE("mobile");

        InterfaceType(String value) {
            this.value = value;
        }

        private String value;

        @Override
        public String toString() {
            return value;
        }

        public static InterfaceType getEnum(String value) {
            if (value == null)
                throw new IllegalArgumentException();
            for (InterfaceType v : values())
                if (value.equalsIgnoreCase(v.toString())) return v;
            throw new IllegalArgumentException();
        }
    }

    public static enum Type {
        APPLICATION("application"), SERVICE("service"), POLICY("policy"), USERGROUP("usergroup");

        Type(String value) {
            this.value = value;
        }

        private String value;

        @Override
        public String toString() {
            return value;
        }

        public static Type getEnum(String value) {
            if (value == null)
                throw new IllegalArgumentException();
            for (Type v : values())
                if (value.equalsIgnoreCase(v.toString())) return v;
            throw new IllegalArgumentException();
        }
    }

    public static enum Priority {
        NORMAL("normal"), IMPORTANT("important"),URGENT("urgent");

        Priority(String value) {
            this.value = value;
        }

        private String value;

        @Override
        public String toString() {
            return value;
        }

        public static Priority getEnum(String value) {
            if (value == null)
                throw new IllegalArgumentException();
            for (Priority v : values())
                if (value.equalsIgnoreCase(v.toString())) return v;
            throw new IllegalArgumentException();
        }
    }



    public static enum ChangeType {
        ADDED, REMOVED, CHANGED
    }

    private static final String TAG = PcfHelper.class.getSimpleName();
    private static final String URI_TEMPLATE = "http://%s:%d%s";
    private static final String JSESSIONID_PATTERN = ".*JSESSIONID=(.*); Path=(.*);.*";
    private static final String GROUP_MEMBERSHIP_COMMON = "/policy-management/rest/groupmembership";
    private static final String POLICY_COMMON = "/policy-management/rest/policy";
    private static final String LOGIN_COMMON = "/policy-management/rest/login";
    private static final String SERVICE_COMMON = "/policy-management/rest/service";
    private static final String TAG_COMMON = "/policy-management/rest/tag";
    private static final String CHANGES_COMMON = "/policy-management/rest/changes";
    private static final String CHANGES_UNPROVISIONED = CHANGES_COMMON+"/unprovisioned";
    private static final String USER_COMMON = "/policy-management/rest/user";
    private static final String USERGROUP_COMMON = "/policy-management/rest/usergroup";
    private static final String ALL_USERS_COMMON = "/policy-management/rest/groupmembership/users";
    private static final String POLICY_PMS_COMMON = "/policy-management/rest/policy/pms";
    private static final String UNPROVISIONED_COMMON = "/policy-management/rest/changes/unprovisioned";
    private static final String VERSION = "/policy-management/rest/sysinfo/version";
    private static final String USER_INFO = "/policy-management/rest/userinfo/username";
    private static final String BODY_TO_LOGIN = "{\"username\": \"admin\", \"password\": \"admin\"}";
    private static final String COOKIE_REGEX = "(.*?)\\=(.*?);.*";
    private static String z7TpAddress;
    private static final int PORT = 8080;
    //private static String jsessionid;
    private static Map<String,String> cookieStorage=new HashMap<String,String>();

    static {
        Log.i(TAG, "Initializing... PcfHelper ver 1.0. Trying to obtain Jsessionid");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, LOGIN_COMMON), null, BODY_TO_LOGIN, false));
            //jsessionid = getValueByRegexp(response.getHeaderField("Set-Cookie"), JSESSIONID_PATTERN, 1);
            //Log.i(TAG, String.format("Jsessionid: %s", jsessionid));
            z7TpAddress = getDeviceZ7TpAddress();
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }


    private static void parseCookies(HttpResponse response){
        for(HttpHeaderField field:response.getHeaderFields()){
            if((field.getName()!=null)&&field.getName().equals("Set-Cookie")){
                String key=getValueByRegexp(field.getValue(), COOKIE_REGEX, 1);
                String value=getValueByRegexp(field.getValue(), COOKIE_REGEX, 2);
                if((key!=null)&&(value!=null))
                    cookieStorage.put(key,value);
            }
        }

    }
    private static void addCookies(HttpRequest request){
        if(cookieStorage.keySet().isEmpty())
            return;
        StringBuilder cookie=new StringBuilder("");
        for(String key:cookieStorage.keySet()){
            cookie.append(" ");
            cookie.append(key);
            cookie.append("=");
            cookie.append(cookieStorage.get(key));
            cookie.append(";");
        }
        cookie.deleteCharAt(cookie.length()-1);
        request.addHeaderField(new HttpHeaderField("Cookie",cookie.toString()));
    }

    /**
     * Creates a new, empty, <tt>UserGroup</tt>. The group will be created with status <tt>ADDED</tt> and version <tt>COMMITTED</tt>
     *
     * @param name - name of the corresponding user group.
     * @return UserGroup - created User Group.
     */
    public static UserGroup createNewUserGroup(String name) {
        if (name == null)
            throw new IllegalArgumentException("Incorrect parameters while creating a new default user group");
        Log.i(TAG, "Creating a new default User Group");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, GROUP_MEMBERSHIP_COMMON), null, new UserGroup(name).toString(), true));
            String id = getValueByRegexp(response.getBody(), "\"(.*)\"", 1);
            return retrieveUserGroupById(id);
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create a new default User Group");
        //TODO must be custom exception
        return null;
    }

    /**
     * Creates a new custom <tt>UserGroup</tt>.</tt>
     *
     * @param userGroup - user group to create.
     * @return UserGroup - created User Group.
     */
    public static UserGroup createNewUserGroup(UserGroup userGroup) {
        if (userGroup == null)
            throw new IllegalArgumentException("Incorrect parameters while creating a custom default user group");
        Log.i(TAG, "Creating a new custom User Group");
        if (!userGroup.getStatus().equals(Status.UPTODATE) || !userGroup.getVersion().equals(Version.SAVED))
            Log.w(TAG, "To be able to see and use this user group its status should be UPTODATE and version SAVED. However, you would like to create a new group that does not maintain these conditions.");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, GROUP_MEMBERSHIP_COMMON), null, userGroup.toString(), true));
            String id = getValueByRegexp(response.getBody(), "\"(.*)\"", 1);
            return retrieveUserGroupById(id);
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create a new custom User Group");
        //TODO must be custom exception
        return null;
    }

    /**
     * Create a new default <tt>Policy</tt>.
     *
     * @param name - name of the corresponding pcf policy to create.
     * @return PcfPolicy - created policy.
     */
    public static PcfPolicy createNewPolicy(String name) {
        if (name == null)
            throw new IllegalArgumentException("Incorrect parameters while creating a new default pcf policy");
        Log.i(TAG, "Creating a new default Policy");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_COMMON), null, new PcfPolicy(name).toString(), true));
            String id = getValueByRegexp(response.getBody(), "\"(.*)\"", 1);
            return retrievePolicyById(id);
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create a new default Policy");
        //TODO must be custom exception
        return null;
    }

    /**
     * Create a new custom <tt>Policy</tt>.
     *
     * @param policy - custom policy to create.
     * @return PcfPolicy - created policy.
     */
    public static PcfPolicy createNewPolicy(PcfPolicy policy) {
        if (policy == null)
            throw new IllegalArgumentException("Incorrect parameters while creating a new custom policy");
        Log.i(TAG, "Creating a new custom Policy");
        if (!policy.getStatus().equals(Status.UPTODATE) || !policy.getVersion().equals(Version.SAVED))
            Log.w(TAG, "To be able to see and use this policy its status should be UPTODATE and version SAVED. However, you would like to create a new policy that does not maintain these conditions.");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_COMMON), null, policy.toString(), true));
            String id = getValueByRegexp(response.getBody(), "\"(.*)\"", 1);
            return retrievePolicyById(id);
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create a new custom Policy");
        //TODO must be custom exception
        return null;
    }

    /**
     * List all available policies.
     *
     * @return List<PcfPolicy> - list of all pcf policies.
     */
    public static List<PcfPolicy> retrieveAllPolicies() {
        Log.i(TAG, "Retrieving all pcf policies");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_COMMON), null, null, true));
            return PcfPolicy.toListOfPcfPolicies(response.getBody());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve all PcfPolicies");
        //TODO must be custom exception
        return null;
    }

    /**
     * Adds a new <tt>Service</tt>.
     *
     * @param service - service to create.
     * @return Service - created service.
     */
    public static Service createNewService(Service service) {
        if (service == null)
            throw new IllegalArgumentException("Incorrect parameters while creating a new custom service");
        Log.i(TAG, "Creating a new custom Service");
        if (!service.getVersion().equals(Version.SAVED))
            Log.w(TAG, "To be able to see and use this service its version should be SAVED. However, you would like to create a new service that does not maintain this condition.");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, SERVICE_COMMON), null, service.toString(), true));
            String id = getValueByRegexp(response.getBody(), "\"(.*)\"", 1);
            return retrieveServiceById(id);
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create a new custom Service");
        //TODO must be custom exception
        return null;
    }

    /**
     * List all available <tt>Service</tt>s.
     *
     * @return List<Service> - list of all services.
     */
    public static List<Service> retrieveAllServices() {
        Log.i(TAG, "Retrieving all services");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, SERVICE_COMMON), null, null, true));
            return Service.toListOfServices(response.getBody());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve a list of Services");
        //TODO must be custom exception
        return null;
    }

    /**
     * Get all existing tags
     *
     * @return List<String> - list of all tags.
     */
    public static List<String> retrieveAllTags() {
        Log.i(TAG, "Retrieving all tags");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, TAG_COMMON), null, null, true));
            JSONArray tags = new JSONArray(response.getBody());
            List<String> listOfTags = new ArrayList<String>();
            for (int i = 0; i < tags.length(); i++) {
                listOfTags.add(tags.getString(i));
            }
            return listOfTags;
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve a list of Tags");
        //TODO must be custom exception
        return null;
    }

    /**
     * Get all existing Changes
     *
     * @return List<Change> - list of all Changes.
     */
    public static List<Change> retrieveAllChanges() {
        Log.i(TAG, "Retrieving all Changes");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, CHANGES_UNPROVISIONED), null, null, true));

            List<Change> listOfChanges = Change.toListOfChanges(response.getBody());

            return listOfChanges;
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve a list of Changes");
        //TODO must be custom exception
        return null;
    }


    /**
     * Set Change priority
     *
     .
     */
    public static void setChangePriority(Change change,Priority priority) {
        if (change == null || priority == null)
            throw new IllegalArgumentException("Incorrect parameters while setting Change priority");
        Log.i(TAG, "Trying to set Change priority");
        try {

            sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, CHANGES_COMMON + String.format("/%s/%s?priority=%s", change.getTargetType(),change.getId(),priority)), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Show user that matches the provided query parameters. Note that even if multiple parameters are provided, only one of them will be used for search. The precedence is as follows: <ol> <li>7tp</li> <li>imsi</li> <li>msisdn</li> </ol>
     *
     * @param z7tp   - z7tp to search.
     * @param imsi   - imsi to search.
     * @param msisdn - msisdn to search.
     * @return User  - User that matched all parameters.
     */
    public static User retrieveUserByParameters(String z7tp, String imsi, String msisdn) {
        if (z7tp == null && imsi == null && msisdn == null)
            throw new IllegalArgumentException("Incorrect parameters while retrieving user");
        StringBuilder query = new StringBuilder();
        query.append("?");
        if (z7tp != null) {
            query.append("7tp=").append(z7tp);
        } else if (imsi != null) {
            query.append("imsi=").append(imsi);
        } else query.append("&msisdn=").append(msisdn);

        Log.i(TAG, "Retrieving user by parameters");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE + query.toString(), mPmsServerIp, PORT, USER_COMMON), null, null, true));
            return User.toUserFromJson(response.getBody());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to search for the user");
        //TODO must be custom exception
        return null;
    }

    /**
     * Find corresponding user with z7TpAddress from transport settings.
     *
     * @return User - user with z7TpAddress from transport settings.
     */
    public static User retrieveUserBy7tp() {
        return retrieveUserByParameters(z7TpAddress, null, null);
    }

    /**
     * List all stored user groups.
     *
     * @return List<UserGroup> - list of all user groups.
     */
    public static List<UserGroup> retrieveAllUsergroups() {
        Log.i(TAG, "Retrieving all user groups");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, USERGROUP_COMMON), null, null, true));
            return UserGroup.toListOfUserGroups(response.getBody());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve all user groups");
        //TODO must be custom exception
        return null;
    }

    /**
     * Retrieve all users in pcf database.
     *
     * @return List<User> - list of all users.
     */
    public static List<User> retrieveAllUsersInPcfDatabase() {
        Log.i(TAG, "Retrieving all users in pcf database");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, ALL_USERS_COMMON), null, null, true));
            return User.toListOfUsers(response.getBody());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve all users");
        //TODO must be custom exception
        return null;
    }

    /**
     * Assigns an existing user to the given group.
     *
     * @param groupId - id of group.
     * @param userId  - id of user.
     */
    public static void assignUsersToGroup(String groupId, String... userId) {
        if (groupId == null || userId == null) throw new IllegalArgumentException();
        try {
            StringBuilder body = new StringBuilder();
            body.append("[");
            for (int i = 0; i < userId.length; i++) {
                body.append(String.format("\"%s\"", userId[i]));
            }
            body.append("]");
            sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, GROUP_MEMBERSHIP_COMMON + String.format("/%s/batch", groupId)), null, body.toString(), true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Assigns set of policies to the appropriate group.
     *
     * @param groupId    - id of the give group.
     * @param genericIds - generic IDs of the policies.
     */
    public static void assignPoliciesToGroup(String groupId, GenericId... genericIds) {
        if (groupId == null || genericIds == null)
            throw new IllegalArgumentException("Incorrect parameters while assigning policies to the user group");
        Log.i(TAG, "Trying to assign policies to the group");
        try {
            JSONArray body = new JSONArray();
            for (GenericId id : genericIds) {
                body.put(id.toJson());
            }
            sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, USERGROUP_COMMON + String.format("/%s/policies", groupId)), null, body.toString(), true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Add services to a policy.
     *
     * @param policyId   - id of the pcf policy.
     * @param genericIds - array of the services that are represented by generics.
     */
    public static void assignServicesToPolicy(String policyId, GenericId... genericIds) {
        if (policyId == null || genericIds == null)
            throw new IllegalArgumentException("Incorrect parameters while assigning services to the policy");
        Log.i(TAG, "Trying to assign services to the policy");
        try {
            JSONArray body = new JSONArray();
            for (GenericId id : genericIds) {
                body.put(id.toJson());
            }
            sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_COMMON + String.format("/%s/services", policyId)), null, body.toString(), true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Remove services from a <tt>Policy</tt>.
     *
     * @param policyId - id of the policy.
     * @param services - services to remove from.
     */
    public static void removeServicesFromPolicy(String policyId, Service... services) {
        if (policyId == null || services == null||services.length==0)
            throw new IllegalArgumentException("Incorrect parameters while deleting services from the policy");
        Log.i(TAG, "Trying to delete services from the policy");
        try {
            StringBuilder strListServices=new StringBuilder("");
            for (Service s : services) {
                strListServices.append(s.getId()).append(",");
            }
            strListServices.deleteCharAt(strListServices.length()-1);
            sendRequestWithBlockingPort(prepareRequest(Method.DELETE, String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_COMMON + String.format("/%s/services?services=%s", policyId,strListServices.toString())), null,null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Removes service by id.
     *
     * @param serviceId - id of the service to delete.
     */
    public static void removeServiceById(String serviceId) {
        if (serviceId == null)
            throw new IllegalArgumentException("Incorrect parameters while deleting service by id");
        Log.i(TAG, "Trying to delete service by id");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.DELETE, String.format(URI_TEMPLATE, mPmsServerIp, PORT, SERVICE_COMMON + String.format("/%s", serviceId)), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Hack for the generic application framework. Returns all PMS related policies
     */
    public static void retrieveAllPmsRelatedPolicy() {
        Log.i(TAG, "Trying to retrieve all pms related policy");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_PMS_COMMON), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Retrieve a single <tt>Policy</tt> identified by <tt>id</tt>.
     *
     * @param id - id of the policy.
     * @return PcfPolicy - policy with corresponding id.
     */
    public static PcfPolicy retrievePolicyById(String id) {
        if (id == null) throw new IllegalArgumentException("Incorrect parameters while retrieving policy by id");
        Log.i(TAG, "Trying to retrieve policy by id");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_COMMON + String.format("/%s", id)), null, null, true));
            return PcfPolicy.toPcfPolicyFromJson(response.getBody());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve policy by id");
        //TODO must be custom exception
        return null;
    }

    /**
     * Removes a <tt>Policy</tt>.
     *
     * @param id - id of policy.
     * @return boolean - true if policy is successfully removed, else - false.
     */
    public static boolean removePolicyById(String id) {
        if (id == null) throw new IllegalArgumentException("Incorrect parameters while removing policy by id");
        boolean isSuccess = false;
        Log.i(TAG, "Trying to remove policy by id");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.DELETE, String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_COMMON + String.format("/%s", id)), null, null, true));
            PcfPolicy policy = retrievePolicyById(id);
            if (policy.isRemoved()) isSuccess = true;
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        return isSuccess;
    }

    /**
     * Update a <tt>Policy</tt>.
     *
     * @param pcfPolicy - pcf policy which you would like to update.
     * @return PcfPolicy - policy with updated values.
     */
    public static PcfPolicy updatePolicy(PcfPolicy pcfPolicy) {
        if (pcfPolicy == null)
            throw new IllegalArgumentException("Incorrect parameters while updating the policy");
        Log.i(TAG, "Updating the Policy");
        if (!pcfPolicy.getStatus().equals(Status.UPTODATE) || !pcfPolicy.getVersion().equals(Version.SAVED))
            Log.w(TAG, "To be able to see and use this policy its status should be UPTODATE and version SAVED. However, you would like to update the policy that does not maintain these conditions.");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.PUT, String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_COMMON + String.format("/%s", pcfPolicy.getId())), null, pcfPolicy.toString(), true));
            return retrievePolicyById(pcfPolicy.getId());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to update a new custom Policy");
        //TODO must be custom exception
        return null;
    }

    /**
     * Updates active state of corresponding policy.
     *
     * @param policyName - name of the policy which we are going to update.
     * @param active     - expected active state.
     * @return PcfPolicy - updated policy with corresponding name.
     */
    public static PcfPolicy updateActiveState(String policyName, Boolean active) {
        PcfPolicy policy = retrievePolicyByName(policyName);
        if (!policy.isActive().equals(active)) {
            policy.setActive(active);
            return updatePolicy(policy);
        } else {
            Log.i(TAG, "Policy already has correct active value");
            return policy;
        }
    }

    /**
     * Retrieve a single <tt>Service</tt>.
     *
     * @param id - id of the service.
     * @return Service - created Service.
     */
    public static Service retrieveServiceById(String id) {
        if (id == null) throw new IllegalArgumentException("Incorrect parameters while retrieving policy by id");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, SERVICE_COMMON + String.format("/%s", id)), null, null, true));
            return Service.toServiceFromJson(response.getBody());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve service by id");
        //TODO must be custom exception
        return null;
    }

    /**
     * Get all available information about the current build.
     */
    public static void retrieveVersion() {
        Log.i(TAG, "Trying to retrieve version of the pcf");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, VERSION), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Add a tag. The tag will not initially be associated with any policy or application.
     *
     * @param name - name of the tag.
     */
    public static void createNewTag(String name) {
        if (name == null) throw new IllegalArgumentException("Incorrect parameters while creating a new tag");
        Log.i(TAG, "Trying to create a new tag");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, TAG_COMMON + String.format("/%s", name)), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Delete a tag. Removes the tag from the list of tags, and from all the related policies and applications.
     *
     * @param name - name of the tag
     */
    public static void removeTag(String name) {
        if (name == null) throw new IllegalArgumentException("Incorrect parameters while removing a tag");
        Log.i(TAG, "Trying to remove a tag");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.DELETE, String.format(URI_TEMPLATE, mPmsServerIp, PORT, TAG_COMMON + String.format("/%s", name)), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Deletes a single user. The associated restrictions and user group memberships will be removed.
     *
     * @param userId - the ID of the user to delete.
     */
    public static void removeUserById(String userId) {
        if (userId == null) throw new IllegalArgumentException("Incorrect parameters while removing user by id");
        Log.i(TAG, "Trying to remove user by id");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.DELETE, String.format(URI_TEMPLATE, mPmsServerIp, PORT, USER_COMMON + String.format("/%s", userId)), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Shows a single user.
     *
     * @param userId - the ID of the user.
     * @return User - user with corresponding id.
     */
    public static User retrieveUserById(String userId) {
        if (userId == null) throw new IllegalArgumentException("Incorrect parameters while retrieving user by id");
        Log.i(TAG, "Trying to retrieve user by id");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, USER_COMMON + String.format("/%s", userId)), null, null, true));
            return User.toUserFromJson(response.getBody());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve User by id");
        //TODO must be custom exception
        return null;
    }

    /**
     * Shows a usergroup based on its ID.
     *
     * @param id - id of the user group.
     * @return UserGroup - user group with corresponding id.
     */
    public static UserGroup retrieveUserGroupById(String id) {
        if (id == null) throw new IllegalArgumentException("Incorrect parameters while retrieving a user group by id");
        Log.i(TAG, "Trying to retrieve user group by id");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, USERGROUP_COMMON + String.format("/%s", id)), null, null, true));
            return UserGroup.toUserGroupFromJson(response.getBody());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve User Group by id.");
        //TODO must be custom exception
        return null;
    }

    /**
     * Delete a usergroup based on its id.
     *
     * @param id - id of the user group.
     */
    public static void removeUserGroupById(String id) {
        if (id == null) throw new IllegalArgumentException("Incorrect parameters while removing user group by id");
        Log.i(TAG, "Trying to remove user group by id");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.DELETE, String.format(URI_TEMPLATE, mPmsServerIp, PORT, USERGROUP_COMMON + String.format("/%s", id)), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    //TODO CHECK THIS POINT

    /**
     * Username of the currently authenticated user.
     *
     * @return String - name of current user.
     */
    public static String retrieveCurrentUser() {
        Log.i(TAG, "Trying  to retrieve current user");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, USER_INFO), null, null, true));
            return getValueByRegexp(response.getBody(), "\"(.*)\")", 1);
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve Current User");
        //TODO must be custom exception
        return null;
    }

    //TODO MULTIPLY CREATING OF THE USERS

    /**
     * Removes an user from the given group. The user won't be removed from the data store.
     *
     * @param groupId - id of the group.
     * @param userId  - id of the user.
     */
    public static void removeUserFromGroup(String groupId, String userId) {
        if (groupId == null || userId == null)
            throw new IllegalArgumentException("Incorrect parameters while removing user from group");
        Log.i(TAG, "Trying to remove user from group");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.DELETE, String.format(URI_TEMPLATE, mPmsServerIp, PORT, GROUP_MEMBERSHIP_COMMON + String.format("/%s/%s", groupId, userId)), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Shows policy by name.
     *
     * @param name - name of the policy.
     * @return PcfPolicy - appropriate policy.
     */
    public static PcfPolicy retrievePolicyByName(String name) {
        if (name == null) throw new IllegalArgumentException("Incorrect parameters while retrieving policy by name");
        Log.i(TAG, "Trying to retrieve policy by name");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_COMMON + String.format("/name/%s", name)), null, null, true));
            return PcfPolicy.toPcfPolicyFromJson(response.getBody());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve policy by name");
        //TODO must be custom exception
        return null;
    }

    //TODO

    /**
     * List services for the given <tt>Policy</tt>.
     *
     * @param policyId - id of the policy.
     * @return List<Service> - list of the services for given policy.
     */
    public static List<Service> retrieveListOfServicesForPolicy(String policyId) {
        if (policyId == null)
            throw new IllegalArgumentException("Incorrect parameters while retrieving list of services for policy by id");
        Log.i(TAG, "Retrieving list of services for policy by id");
        try {
            List<Service> services = new ArrayList<Service>();
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_COMMON + String.format("/%s/services", policyId)), null, null, true));
            JSONArray responseBody = new JSONArray(response.getBody());
            for (int i = 0; i < responseBody.length(); i++) {
                services.add(retrieveServiceById(responseBody.getJSONObject(i).getString("id")));
            }
            return services;
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve list of services for policy by id");
        //TODO must be custom exception
        return null;
    }

    /**
     * Remove services from a <tt>Policy</tt>
     *
     * @param policyId - id of the policy
     */
    /*public static void removeServicesFromPolicy(String policyId) {
        HttpRequest request = HttpRequest.Builder.create().getRequest();
        request.setUri(String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_COMMON + String.format("/%s/services", policyId)));
        request.setMethod("DELETE");
        request.addHeaderField(new HttpHeaderField("Content-type", "application/json"));
        request.addHeaderField(new HttpHeaderField("Connection", "close"));
        request.addHeaderField(new HttpHeaderField("Cookie", "JSESSIONID=" + jsessionid + "; lb_id=10020"));
        try {
            HttpResponse response = AsimovTestCase.sendRequest(request);
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }*/

    //TODO ADDING services to policy


    //TODO

    /**
     * List <tt>UserGroup</tt>s that the specified <tt>Policy</tt> applies to.
     *
     * @param policyId - id of the policy
     */
    public static void retrieveListOfUsergroupsForPolicy(String policyId, UserGroup... userGroups) {
        if (policyId == null || userGroups == null)
            throw new IllegalArgumentException("Incorrect parameters while retrieving list of the user groups for policy");
        Log.i(TAG, "Retrieving list of the user groups for policy");
        try {
            JSONArray body = new JSONArray();
            for (UserGroup u : userGroups) {
                body.put(u.toJson());
            }
            sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, POLICY_COMMON + String.format("/%s/usergroups", policyId)), null, body.toString(), true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * List all policies this service is included in.
     *
     * @param serviceId - the UUID of the <tt>Service</tt>.
     */
    public static List<PcfPolicy> retrieveListOfPoliciesForService(String serviceId) {
        if (serviceId == null)
            throw new IllegalArgumentException("Incorrect parameters while retrieving list of the policies for the service");
        Log.i(TAG, "Retrieving list of the policies for the service");
        try {
            List<PcfPolicy> policies = new ArrayList<PcfPolicy>();
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, SERVICE_COMMON + String.format("/%s/policies", serviceId)), null, null, true));
            JSONArray responseBody = new JSONArray(response.getBody());
            for (int i = 0; i < responseBody.length(); i++) {
                policies.add(retrievePolicyById(responseBody.getJSONObject(i).getString("id")));
            }
            return policies;
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve list of the policies for the service");
        //TODO must be custom exception
        return null;
    }

    /**
     * List applications tagged with a specific tag.
     *
     * @param tag - The tag that is matched in applications.
     */
    public static void retrieveAllApplicationsByTag(String tag) {
        Log.i(TAG, "Trying to retrieve all applications by tag");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, TAG_COMMON + String.format("/%s/numofapps", tag)), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * List policies including a specified tag.
     *
     * @param tag - The tag that is matched in policies.
     */
    public static void retrieveAllPoliciesByTag(String tag) {
        Log.i(TAG, "Trying to retrieve all policies by tag");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, TAG_COMMON + String.format("/%s/policies", tag)), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * List the <tt>Usergroup</tt>s the user belongs to.
     *
     * @param userId - UUID of the user.
     * @return List<UserGroup> - list of all user group for user.
     */
    public static List<UserGroup> retrieveAllUsergroupsForUserById(String userId) {
        if (userId == null)
            throw new IllegalArgumentException("Incorrect parameters while retrieving user groups for user by id");
        Log.i(TAG, "Retrieving user groups for user by id");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, USER_COMMON + String.format("/%s/groups", userId)), null, null, true));
            return UserGroup.toListOfUserGroups(response.getBody());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve user groups for user by id");
        //TODO must be custom exception
        return null;
    }

    /**
     * Show a user group based on its name.
     *
     * @param name - The name of the user group.
     * @return - user group by name.
     */
    public static UserGroup retrieveUserGroupByName(String name) {
        if (name == null)
            throw new IllegalArgumentException("Incorrect parameters while retrieving user group by name");
        Log.i(TAG, "Retrieving user group by name");
        try {
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, USERGROUP_COMMON + String.format("/name/%s", name)), null, null, true));
            return UserGroup.toUserGroupFromJson(response.getBody());
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve user group by name");
        //TODO must be custom exception
        return null;
    }

    /**
     * Delete a usergroup based on its name.
     *
     * @param name - The name of the user group.
     */
    public static void removeUserGroupByName(String name) {
        if (name == null)
            throw new IllegalArgumentException("Incorrect parameters while removing user group by name");
        Log.i(TAG, "Removing user group by name");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.DELETE, String.format(URI_TEMPLATE, mPmsServerIp, PORT, USERGROUP_COMMON + String.format("/name/%s", name)), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    //TODO attach/remove set of policies from usergroup

    /**
     * List the policies attached to a usergroup.
     *
     * @param groupId - Id of the Usergroup.
     * @return - list of policies that are attached to a user group.
     */
    public static List<PcfPolicy> retrieveListOfPoliciesForUserGroup(String groupId) {
        if (groupId == null)
            throw new IllegalArgumentException("Incorrect parameters while retrieving list of the policies for the user group");
        Log.i(TAG, "Retrieving list of the policies for the user group");
        try {
            List<PcfPolicy> policies = new ArrayList<PcfPolicy>();
            HttpResponse response = sendRequestWithBlockingPort(prepareRequest(Method.GET, String.format(URI_TEMPLATE, mPmsServerIp, PORT, USERGROUP_COMMON + String.format("/%s/policies", groupId)), null, null, true));
            JSONArray responseBody = new JSONArray(response.getBody());
            for (int i = 0; i < responseBody.length(); i++) {
                policies.add(retrievePolicyById(responseBody.getJSONObject(i).getString("id")));
            }
            return policies;
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to retrieve list of the policies for the user group");
        //TODO must be custom exception
        return null;
    }

    /**
     * Provision all pending changes.
     */
    public static void provisionAllPendingChanges() {
        Log.i(TAG, "Trying to provision all changes");
        try {
            sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, UNPROVISIONED_COMMON), null, null, true));
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
    }

    private static HttpRequest prepareRequest(Method method, String uri, HttpHeaderField[] additionalHeaders, String body, boolean includeCookie) {
        HttpRequest request = HttpRequest.Builder.create().getRequest();
        request.setUri(uri);
        request.setMethod(method.toString());
        request.addHeaderField(new HttpHeaderField("Content-type", "application/json"));
        request.addHeaderField(new HttpHeaderField("Connection", "close"));
//        if (includeCookie)
//            request.addHeaderField(new HttpHeaderField("Cookie", "JSESSIONID=" + jsessionid + "; lb_id=10020"));
        if (additionalHeaders != null) {
            for (HttpHeaderField headerField : additionalHeaders) {
                request.addHeaderField(headerField);
            }
        }
        if (body != null) {
            request.setBody(body);
            request.addHeaderField(new HttpHeaderField("Content-length", Integer.toString(request.getBody().length())));
        }
        return request;
    }

    private static String getValueByRegexp(String source, String regexp, int group) {
        Log.i(TAG, String.format("Trying to get value from: %s, regexp: %s, group: %d", source, regexp, group));
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            return matcher.group(group);
        }
        Log.e(TAG, "Can not find appropriate records! Returning null");
        //TODO must be custom exception
        return null;
    }

    private static HttpResponse sendRequestWithBlockingPort(HttpRequest request, int portToBlock) throws IOException,
            URISyntaxException, InterruptedException {
//        String path = "/data/misc/openchannel";
//        String[] addPortToIgnor = {"su", "-c", TFConstants.IPTABLES_PATH + " -t nat -I OUTPUT -m conntrack --ctorigdstport " + portToBlock + " -j ACCEPT"};
//        String[] deletePortFromIgnore = {"su", "-c", TFConstants.IPTABLES_PATH + " -t nat -D OUTPUT -m conntrack --ctorigdstport " + portToBlock + " -j ACCEPT"};
//        Runtime.getRuntime().exec(addPortToIgnor).waitFor();
        addCookies(request);
        HttpResponse response = AsimovTestCase.sendRequest(request);
        if(response.getStatusCode()== HttpStatus.SC_UNAUTHORIZED){
            response=AsimovTestCase.sendRequest(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, LOGIN_COMMON), null, BODY_TO_LOGIN, false));
            Log.e("###DEBUG", "PcfHelper reauthorization result="+response.getStatusCode());

            response = AsimovTestCase.sendRequest(request);
        }
        parseCookies(response);
//        Runtime.getRuntime().exec(deletePortFromIgnore).waitFor();
        return response;
    }

    private static HttpResponse sendRequestWithBlockingPort(HttpRequest request) throws IOException,
            URISyntaxException, InterruptedException {
        HttpResponse response=sendRequestWithBlockingPort(request, PORT);
        if(response.getStatusCode()== HttpStatus.SC_UNAUTHORIZED){
            response=sendRequestWithBlockingPort(prepareRequest(Method.POST, String.format(URI_TEMPLATE, mPmsServerIp, PORT, LOGIN_COMMON), null, BODY_TO_LOGIN, false), PORT);
            Log.e("###DEBUG", "PcfHelper reauthorization result="+response.getStatusCode());
            response=sendRequestWithBlockingPort(request, PORT);
            if(response.getStatusCode()== HttpStatus.SC_UNAUTHORIZED)
                throw new AssertionFailedError("Reauthorization at PCF rest failed");
        }
        return response;
    }

    private static String getDeviceZ7TpAddress() throws IOException {
        final String transportSettingsFilePath = "/mnt/sdcard/z7tpts";
        if (z7TpAddress != null) return z7TpAddress;
        try {
            String[] cmd = new String[]{
                    "cat", "/data/data/com.seven.asimov/transport_settings/*", ">", transportSettingsFilePath
            };
            ShellUtil.execWithCompleteResult(Arrays.asList(cmd), true);
            InputStream inStream = new ObjectInputStream(new FileInputStream(transportSettingsFilePath));
            IntArrayMap policySource = (IntArrayMap) Marshaller.decode(inStream);
            for (int i = 0; i < policySource.size(); i++) {
                Object obj = policySource.get(i);
                if (obj instanceof Z7TransportAddress) {
                    Z7TransportAddress address = (Z7TransportAddress) obj;
                    Log.i(TAG, "7TP address found: " + address.toString());
                    z7TpAddress = address.toString();
                }
            }
            return z7TpAddress;
        } catch (FileNotFoundException e) {
            throw new AssertionFailedError("File transport_settings not found. Please verify that OC installed and worked.");
        } catch (EOFException e) {
            throw new AssertionFailedError("File transport_settings not found. Please verify that OC installed and worked.");
        } finally {
            String[] cmd = new String[]{
                    "rm", transportSettingsFilePath
            };
            ShellUtil.execWithCompleteResult(Arrays.asList(cmd), true);
        }
    }

}
