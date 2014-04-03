package com.seven.asimov.it.utils.pms;

import android.os.Build;
import android.util.Log;
import com.seven.asimov.it.asserts.CATFAssert;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.BaseConstantsIF;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAddedTask;
import com.seven.asimov.it.utils.logcat.wrappers.PolicyWrapper;
import com.seven.asimov.it.utils.pms.z7.IntArrayMap;
import com.seven.asimov.it.utils.pms.z7.Marshaller;
import com.seven.asimov.it.utils.pms.z7.Z7TransportAddress;
import com.seven.asimov.it.utils.sms.SmsUtil;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.seven.asimov.it.base.constants.TFConstantsIF.*;

public class PMSUtil {
    private static String URI_TEMPLATE = "http://%s:%d%s";
    private static String REST_BATCH = "/rest/batch";
    private static String REST_PROPERTY = "/rest/property";
    private static String REST_MANUAL_PUSH = "/rest/policy/pushChanges";
    private static String FIND_ENDPOINT = "/rest/endpoint/find/%s?field=id";
    private static String ENDPOINT_PROFILES = "/rest/endpoint/%s/profiles";
    private static String REST_SCOPE_TYPE_GLOBAL = "/rest/scope/type/GLOBAL";
    private static String REST_NAMESPACE_PROPERTIES = "/rest/namespace/%s/properties";

    private static String ALL_MODELS = "/rest/model/all";
    private static String NAMESPACES = "/rest/namespace/children/";

    private static String ROOT = "root";
    private static String ID = "id";
    private static String NAME = "name";
    private static String TYPE = "type";
    private static String PARENT_ID = "parentId";
    private static String VERSION = "version";
    private static String DELETED = "deleted";
    private static String NAMESPASE_ID = "namespaceId";
    private static String VALUE = "value";
    private static String VENDOR = "vendor";
    private static String SCOPE_ID = "scopeId";
    private static String SCOPE = "scope";
    private static String ENDPOINT = "endpoint";
    private static String PROFILE = "profile";
    private static List<Property> personalPolicies = new ArrayList<Property>();

    private static List<Property> globalPolicies = new ArrayList<Property>();
    private static List<String> initializedPaths = new ArrayList<String>();
    private static final HashSet<String> setOfGlobalScopes = new HashSet<String>();
    private static final HttpHeaderField mAuthorizedHeader = new HttpHeaderField("Authorization", "Basic YWRtaW46YWRtaW4=");

    private static final String mCreateNamespace = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<Mutations xmlns=\"http://www.seven.com/xsd/mc.xsd\"><mutation><namespace><type>INSERT</type>"
            + "<important>false</important><path>%s</path><target><name>%s</name>"
            + "<type>UNDEFINED</type><version>0</version><deleted>false</deleted></target></namespace></mutation></Mutations>";

    private static final String mCreateProperty = "<Mutations xmlns=\"http://www.seven.com/xsd/mc.xsd\">"
            + "<mutation><property><type>INSERT</type><important>%s</important><path>%s</path>%s<target hierarchy=\"overwrite\">"
            + "<name>%s</name><type>STRING</type><value>%s</value><version>0</version>"
            + "<deleted>false</deleted></target></property></mutation></Mutations>";

    private static final String mDeleteNamespace = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<Mutations xmlns=\"http://www.seven.com/xsd/mc.xsd\"><mutation><namespace>"
            + "<type>DELETE</type><important>false</important><target id=\"%s\"><name>%s</name><type>UNDEFINED</type>"
            + "<parentId>%s</parentId><version>%s</version><deleted>false</deleted></target></namespace></mutation></Mutations>";

    private static final String mDeleteProperty = "<Mutations xmlns=\"http://www.seven.com/xsd/mc.xsd\">"
            + "<mutation><property><type>DELETE</type><important>%s</important><target hierarchy=\"overwrite\" id=\"%s\">"
            + "<name>%s</name><type>STRING</type><value>%s</value><version>%s</version>"
            + "<deleted>false</deleted></target></property></mutation></Mutations>";

    private static final String mUpdateProperty = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<Mutations xmlns=\"http://www.seven.com/xsd/mc.xsd\">"
            + "<mutation><property><type>UPDATE</type><important>true</important>"
            + "<target hierarchy=\"overwrite\" id=\"%s\">"
            + "<name>%s</name><type>STRING</type><value>%s</value><scopeId>%s</scopeId>"
            + "<namespaceId>%s</namespaceId><version>0</version><deleted>false</deleted>"
            + "</target></property></mutation></Mutations>";

    private static int tryId = 0;
    private static boolean restServerDetected = false;
    private static boolean modelInfoInitialized = false;
    private static String z7TpAddress;
    private static String referencedObjectId;

    private static Model model;
    private static final String TAG = PMSUtil.class.getSimpleName();

    private static final Logger logger = LoggerFactory.getLogger(PMSUtil.class.getSimpleName());

    public static void setRestServerDetected(boolean isDetected) {
        restServerDetected = isDetected;
    }

    public static void initModelInfo() throws Exception {
        if (!modelInfoInitialized) {
            boolean isIpTablesUpdated = false;
            model = new Model();

            if (!restServerDetected) {
                try {
                    detectRelayServer(++tryId);
                } catch (AssertionFailedError error) {
                    logger.error("Error while trying to detect REST server. Going to try once more"
                            + ExceptionUtils.getStackTrace(error));
                    try {
                        detectRelayServer(++tryId);
                    } catch (Exception e) {
                        logger.error("Failed to detect REST server. Using defaults temporary "
                                + ExceptionUtils.getStackTrace(e));
                    }
                }
                restServerDetected = true;
            }

            Document doc = getXmlDocumentFromResponse(sendHttpRequestToPms(ALL_MODELS, null));
            NodeList nl = doc.getElementsByTagName("model");
            if (isEmulator()) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Node nNode = nl.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        if (EMULATOR_NAME.equalsIgnoreCase(getTagValue(NAME, eElement))
                                && mEmulatorVendor.equalsIgnoreCase(getTagValue(VENDOR, eElement))) {
                            model.name = EMULATOR_NAME;
                            model.id = eElement.getAttribute(ID);
                            model.vendor = getTagValue(VENDOR, eElement);
                            model.scopeId = ((Element) eElement.getElementsByTagName(SCOPE).item(0)).getAttribute(ID);
                            logger.info("model detected: " + model);
                            break;
                        }
                    }
                }
            } else {
                for (int i = 0; i < nl.getLength(); i++) {
                    Node nNode = nl.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        if (getTagValue(NAME, eElement).equalsIgnoreCase(Build.MODEL)) {
                            model.name = Build.MODEL;
                            model.id = eElement.getAttribute(ID);

                            model.vendor = getTagValue(VENDOR, eElement);
                            logger.info("model detected: " + model);
                            logger.info("Z7TP address: " + getDeviceZ7TpAddress());
                            Document endpointResponseBody = getXmlDocumentFromResponse(
                                    sendHttpRequestToPms(String.format(FIND_ENDPOINT, z7TpAddress), null));
                            String endpointId = endpointResponseBody.getElementsByTagName(ENDPOINT).item(0)
                                    .getAttributes().getNamedItem(ID).getNodeValue();
                            Document profilesResponseBody = getXmlDocumentFromResponse(
                                    sendHttpRequestToPms(String.format(ENDPOINT_PROFILES, endpointId), null));
                            referencedObjectId = profilesResponseBody.getElementsByTagName(PROFILE).item(0)
                                    .getAttributes().getNamedItem(ID).getNodeValue();
                            NodeList nodeList = ((Element) profilesResponseBody.getElementsByTagName(PROFILE).item(0)).
                                    getElementsByTagName(SCOPE);
                            if (nodeList.getLength() != 0) {
                                model.scopeId = nodeList.item(0).getAttributes().getNamedItem(ID).getNodeValue();
                            }
                            logger.info("scopeId: " + model.scopeId);
                            logger.info("Referenced Object ID: " + referencedObjectId);
                            break;
                        }
                    }
                }
            }
            modelInfoInitialized = true;
        }
    }

    public static String getPmsServerIp() {
        return EXTERNAL_IP;
    }

    public static String createNameSpace(String path, String name) {
        String id = "";
        try {
            logger.debug("Creating a namespace: " + String.format("%s@%s", path, name));
            Namespace ns = getNameSpaceInfo(String.format("%s@%s", path, name), false, false);
            if (ns.id == null) {
                String str = String.format(mCreateNamespace, path, name);
                HttpResponse resp = sendHttpRequestToPms(REST_BATCH, str);
                if (resp.getStatusCode() == 400)
                    resp = sendHttpRequestToPms(REST_BATCH, str);
                String respBode = resp.getBody();
                id = respBode.substring(respBode.indexOf("objectId") + 10, respBode.indexOf("objectName") - 2);
                logger.info("created namespace: " + id);
                pushToClient();
            } else {
                logger.error("We already have this namespace on the server side");
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return id;
    }

    public static String createProperty(String name, String path, String value, boolean important, boolean isModel) {
        String id = "";
        try {
            initModelInfo();
            String scType = "<scopeType>GLOBAL</scopeType>";
            if (isModel) {
                scType = String.format("<scopeType>MODEL</scopeType><referencedObjectId>%s</referencedObjectId>",
                        model.id);
            }
            String str = String.format(mCreateProperty, important, path, scType, name, value);
            String respBode = sendHttpRequestToPms(REST_BATCH, str).getBody();
            id = respBode.substring(respBode.indexOf("objectId") + 10, respBode.indexOf("objectName") - 2);
            logger.info("created property: " + id);
            pushToClient();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return id;
    }

    /**
     * Creates personal scope property. <b>Warning! Usable with real device only!</b>
     *
     * @param name      property name
     * @param path      property path
     * @param value     property value
     * @param important if true server will receive a hint for ASAP policies delivery
     * @param push      signal to server immediately send policy
     * @return property ID. Can be used for property removing
     */
    public static String createPersonalScopeProperty(String name, String path, String value, boolean important, boolean push) {
        String id = "";
        try {
            Thread initModel = new Thread() {
                @Override
                public void run() {
                    try {
                        initModelInfo();
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        logger.error(ExceptionUtils.getStackTrace(e));
                    }
                }
            };
            initModel.run();
            initModel.join();
            String scType = String.format("<scopeType>PERSONAL</scopeType><referencedObjectId>%s</referencedObjectId>",
                    referencedObjectId);
            String str = String.format(mCreateProperty, important, path, scType, name, value);
            String respBode = sendHttpRequestToPms(REST_BATCH, str).getBody();
            id = respBode.substring(respBode.indexOf("objectId") + 10, respBode.indexOf("objectName") - 2);
            logger.info("created property: " + id);
            if (push) pushToClient();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return id;
    }

    public static String createPersonalScopeProperty(String name, String path, String value, boolean important) {
        return createPersonalScopeProperty(name, path, value, important, true);
    }

    public static void deleteProperty(String propertyId) {
        deleteProperty(propertyId, true);
    }

    public static void deleteProperty(String propertyId, boolean forceUpdate) {
        try {
            Property prop = getPropertyInfo(propertyId);
            if (CURRENT_SERVER_VERSION.compareTo("2.2.") == -1) {
                logger.debug("Sending a request for server with version smaller than 2.2");
                sendHttpRequestToPms(REST_PROPERTY + "/" + prop.id + "/" + prop.version, null, true);
            } else {
                logger.debug("Sending a request for server with version larger or equal to 2.2");
                String str = String.format(mDeleteProperty, "false", prop.id, prop.name, prop.value, prop.version);
                sendHttpRequestToPms(REST_BATCH, str);
            }
            pushToClient();
            getPropertyInfo(propertyId);

            if (forceUpdate) {
                int randomValue = (int) (Math.random() * 1000);
                String id = createPersonalScopeProperty(String.format("forUpdate%d", randomValue), "@asimov", "value", true);
                Thread.sleep(10 * 1000);
                Property propForUpdate = getPropertyInfo(id);
                String str = String.format(mDeleteProperty, "true", propForUpdate.id, propForUpdate.name, propForUpdate.value, propForUpdate.version);
                sendHttpRequestToPms(REST_BATCH, str);
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public static void deleteNamespace(String path) {
        try {
            Namespace ns = getNameSpaceInfo(path, false, true);
            if (ns.id != null) {
                String str = String.format(mDeleteNamespace, ns.id, ns.name, ns.parentId, ns.version);
                sendHttpRequestToPms(REST_BATCH, str, false);
                pushToClient();
            } else {
                logger.error("There is no corresponding namespace on the server!");
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * This method is aimed to prepare appropriate PMS policies.
     * You should note that it uses class Policy and 2 policies will be equal if their names and paths are equal. Values don't matter.
     * <p>There are some potential results of this method:</p>
     * <ol>
     * <li>If needed policy is in personal list and value is not equal to expected and policy is should be we will update it.</li>
     * <li>If needed policy is in personal list and value is equal to expected and policy is should not be we will delete it.</li>
     * <li>If needed policy is in global list and value is not equal to expected and policy is should be we will insert personal policy to replace global one.</li>
     * <li>If needed policy is in global list and value is equal to expected and policy is should not be we will delete it.</li>
     * <li>If both lists don't contain needed policy and it should be we will insert a personal policy.</li>
     * </ol>
     * <p>This method includes next steps:</p>
     * <ol>
     * <li>To block PMS port.</li>
     * <li>To form set of unique paths due to policies which are needed.</li>
     * <li>To find out our global and personal scopes.</li>
     * <li>To fill lists of personal and global policies.</li>
     * <li>Update, delete, or create a corresponding policy. It depends on some conditions that were described earlier. </li>
     * <li>To unblock PMS port</li>
     * </ol>
     *
     * @param policies Set of policies
     * @throws Exception
     */
    public static void preparePmsServer(final HashSet<Policy> policies) throws Exception {
        if (policies == null) return;
        //first step
        final HashSet<String> setOfPaths = new HashSet<String>();
        for (Policy policy1 : policies) {
            String path = policy1.getPath();
            logger.debug("Detected path: " + path);
            logger.debug(String.format("Corresponding path was added to the list: %b", setOfPaths.add(path)));
        }
        //second step
        initModelInfo();
        Document doc = getXmlDocumentFromResponse(sendGetHttpRequestToPms(REST_SCOPE_TYPE_GLOBAL));
        NodeList scopes = doc.getElementsByTagName(SCOPE);
        for (int i = 0; i < scopes.getLength(); i++) {
            Node node = scopes.item(i);
            Element element = (Element) node;
            logger.debug("Detected id: " + element.getAttribute(ID));
            setOfGlobalScopes.add(element.getAttribute(ID));
        }
        //third step
        fillListOfPolicies(setOfPaths);
        logger.debug("Number of global policies for the corresponding namespace: " + globalPolicies.size());
        logger.debug("Number of personal policies for the corresponding namespace: " + personalPolicies.size());

        //fourth step
        for (Policy policy : policies) {
            logger.debug("New policy name: " + policy.getName() + " PolicyPath:" + policy.getPath());
            int personalIndex = personalPolicies.indexOf(new Property(policy.getName(), policy.getPath()));
            int globalIndex = globalPolicies.indexOf(new Property(policy.getName(), policy.getPath()));
            if (personalIndex != -1) {
                logger.info("Match in personal policies");
                processPersonalPolicy(policy, personalIndex);
            } else if (globalIndex != -1) {
                logger.error("Match in global policies");
                processGlobalPolicy(policy, globalIndex);
            } else {
                if (policy.isShouldBe()) {
                    addPersonalPolicy(policy);
                }
            }
        }
    }

    private static void addPersonalPolicy(Policy policy) {
        String personalId = createPersonalScopeProperty(policy.getName(), policy.getPath(), policy.getValue(), true);
        Property personalToAdd = getPropertyInfo(personalId);
        personalToAdd.setPath(policy.getPath());
        personalPolicies.add(personalToAdd);
        logger.debug("2: " + personalPolicies.size());
    }

    private static void fillListOfPolicies(HashSet<String> setOfPaths) {
        for (String path : setOfPaths) {
            if (!initializedPaths.contains(path)) {
                getPropertiesForNamespace(path);
                initializedPaths.add(path);
            }
        }
    }

    private static String replaceMetaCharacters(String source) {
        return source.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&quot;");
    }

    private static void getPropertiesForNamespace(String path) {
        Namespace ns = getNameSpaceInfo(path, false, true);
        if (ns.id != null) {
            Document doc = getXmlDocumentFromResponse(sendGetHttpRequestToPms(String.format(REST_NAMESPACE_PROPERTIES, ns.id)));
            NodeList properties = doc.getElementsByTagName("property");
            for (int i = 0; i < properties.getLength(); i++) {
                Node node = properties.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String propertyId = element.getAttribute(ID);
                    String scopeId = getTagValue(SCOPE_ID, element);
                    if (model.scopeId != null && model.scopeId.equals(scopeId)) {
                        Property property = getPropertyInfo(propertyId);
                        property.value = replaceMetaCharacters(property.value);
                        property.setPath(path);
                        personalPolicies.add(property);
                        logger.debug("Adding a personal policy: " + personalPolicies.size());
                    } else if (setOfGlobalScopes.contains(scopeId)) {
                        Property property = getPropertyInfo(propertyId);
                        property.value = replaceMetaCharacters(property.value);
                        property.setPath(path);
                        globalPolicies.add(property);
                    }
                }
            }
        } else {
            logger.warn(String.format("There is no namespace: %s", path));
        }
    }

    private static String getAttributeFromTag(Document xml, String tag, String attribute) {
        try {
            NodeList nl = xml.getElementsByTagName("results");
            if (nl.getLength() != 0) {
                Element element = (Element) nl.item(0);
                return element.getAttribute("objectId");
            } else {
                logger.error(String.format("There are no records in the appropriate xml about: tag = %s, attribute = %s", tag, attribute));
            }
        } catch (Exception e) {
            logger.error(String.format("Exception while retrieving attribute for: tag = %s, attribute = %s", tag, attribute));
            ExceptionUtils.getStackTrace(e);
        }
        return "";
    }

    private static void processPersonalPolicy(Policy policy, int personalIndex) {
        Property property = personalPolicies.get(personalIndex);
        if (!policy.getValue().equals(property.value) && policy.isShouldBe()) {
            logger.debug("Updating personal policy: " + property.toString());
            HttpResponse response = sendHttpRequestToPms(REST_BATCH, String.format(mUpdateProperty, property.id, property.name, policy.getValue(), property.scopeId, property.namespaceId));
            pushToClient();
            personalPolicies.remove(property);
            String idOfUpdatedPolicy = getAttributeFromTag(getXmlDocumentFromResponse(response), "results", "objectId");
            Property updatedProperty = getPropertyInfo(idOfUpdatedPolicy);
            updatedProperty.value = replaceMetaCharacters(updatedProperty.value);
            updatedProperty.setPath(policy.getPath());
            personalPolicies.add(updatedProperty);
        } else if (policy.getValue().equals(property.value) && !policy.isShouldBe()) {
            logger.debug("Deleting personal policy: " + property.toString());
            deleteProperty(property.id);
            personalPolicies.remove(property);
        }
    }

    private static void processGlobalPolicy(Policy policy, int globalIndex) {
        Property property = globalPolicies.get(globalIndex);
        if (!policy.getValue().equals(property.value) && policy.isShouldBe()) {
            logger.debug("Inserting personal scope property to overwrite global policy: " + property.toString());
            addPersonalPolicy(policy);
        } else if (policy.getValue().equals(property.value) && !policy.isShouldBe()) {
            logger.debug("Deleting global policy: " + property.toString());
            deleteProperty(property.id);
            globalPolicies.remove(property);
        }
    }

    public static void cleanPersonalProperties(HashSet<String> paths) throws Exception {
        initModelInfo();
        fillListOfPolicies(paths);
        if (personalPolicies == null) {
            logger.error("Can not delete all personal policies! List of personal policies is null");
        } else {
            for (int i = 0; i < personalPolicies.size(); i++) {
                if (paths.contains(personalPolicies.get(i).path)) {
                    deleteProperty(personalPolicies.get(i).id);
                    personalPolicies.remove(i);
                    i--;
                }
            }
        }
    }

    public static String setLogLevel(int level) {
        Assert.assertTrue((level >= 1 && level <= 7));
        return createProperty("level", "@asimov@logging", Integer.toString(level), true, true);
    }

    private static HttpResponse sendHttpRequestToPms(String path, String body) {
        return sendHttpRequestToPms(path, body, false);
    }

    private static HttpResponse sendHttpRequestToPms(String path, String body, boolean isDelMethod) {
        HttpResponse response = null;
        try {
            HttpRequest request = HttpRequest.Builder.create().getRequest();
            request.addHeaderField(mAuthorizedHeader);
            request.setUri(String.format(URI_TEMPLATE, mPmsServerIp, mPmsServerPort, path));
            if (body != null) {
                request.setMethod("POST");
                request.addHeaderField(new HttpHeaderField("Content-type", "application/xml"));
                request.setBody(body);
            }
            if (isDelMethod)
                request.setMethod("DELETE");
            response = AsimovTestCase.sendRequest(request);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return response;
    }

    private static HttpResponse sendGetHttpRequestToPms(String path) {
        HttpResponse response = null;


        while (true) {

            int i = 0;
            try {
                HttpRequest request = HttpRequest.Builder.create().getRequest();
                request.addHeaderField(mAuthorizedHeader);
                request.setUri(String.format(URI_TEMPLATE, mPmsServerIp, mPmsServerPort, path));
                request.setMethod("GET");
                request.addHeaderField(new HttpHeaderField("Content-type", "application/xml"));
                response = AsimovTestCase.sendRequest(request, null, false, false, AsimovTestCase.Body.BODY, 30 * 1000, null);    //workaround for ticket ASMV-22199

                //response = AsimovTestCase.sendRequest(request);
            } catch (Exception e) {
                logger.error(ExceptionUtils.getStackTrace(e));
                logger.debug("Try again");

                if (i >= 3) {
                    break;
                }
                i++;
            }

            break;
        }
        return response;
    }

    private static Property getPropertyInfo(String propertyId) {
        Property result = new Property();
        Document doc = getXmlDocumentFromResponse(sendHttpRequestToPms(REST_PROPERTY + "/" + propertyId, null));
        NodeList nl = doc.getElementsByTagName("Property");
        for (int i = 0; i < nl.getLength(); i++) {
            Node nNode = nl.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                result.id = eElement.getAttribute(ID);
                result.name = getTagValue(NAME, eElement);
                result.version = getTagValue(VERSION, eElement);
                result.type = getTagValue(TYPE, eElement);
                result.deleted = getTagValue(DELETED, eElement);
                result.namespaceId = getTagValue(NAMESPASE_ID, eElement);
                result.value = getTagValue(VALUE, eElement);
                result.scopeId = getTagValue(SCOPE_ID, eElement);
            }
        }
        logger.info("property info" + result);
        return result;
    }

    private static Document getXmlDocumentFromResponse(HttpResponse response) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(response.getBody()));
            return db.parse(is);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    private static Namespace findNamespaceByName(String pathHttp, String namespaceToFind) {
        return findNamespaceByName(pathHttp, namespaceToFind, true);
    }

    private static Namespace findNamespaceByName(String pathHttp, String namespaceToFind, boolean includeDeleted) {
        Namespace result = new Namespace();

        Document doc = getXmlDocumentFromResponse(sendHttpRequestToPms(NAMESPACES + pathHttp, null));
        NodeList nl = doc.getElementsByTagName("namespace");
        logger.debug(String.format("Number of nodes: %s", nl.getLength()));
        for (int i = 0; i < nl.getLength(); i++) {
            Node nNode = nl.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if (getTagValue(NAME, eElement).equalsIgnoreCase(namespaceToFind)
                        && (includeDeleted || !includeDeleted && "false".equals(getTagValue(DELETED, eElement)))) {
                    result.id = eElement.getAttribute(ID);
                    result.name = getTagValue(NAME, eElement);
                    result.version = getTagValue(VERSION, eElement);
                    result.type = getTagValue(TYPE, eElement);
                    result.deleted = getTagValue(DELETED, eElement);
                    result.parentId = pathHttp.equalsIgnoreCase(ROOT) ? ROOT : getTagValue(PARENT_ID, eElement);
                    break;
                }
            }
        }
        logger.info("namespace info" + result);
        return result;
    }

    private static String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = nlList.item(0);
        return nValue != null ? nValue.getNodeValue() : "";
    }

    private static Namespace getNameSpaceInfo(String namespace) {
        return getNameSpaceInfo(namespace, true);
    }

    private static Namespace getNameSpaceInfo(String namespace, boolean includeDeleted) {
        return getNameSpaceInfo(namespace, includeDeleted, false);
    }

    private static Namespace getNameSpaceInfo(String namespace, boolean includeDeleted, boolean createIfNotExists) {
        Namespace ns = new Namespace();
        StringBuilder currentDirectory = new StringBuilder();
        String[] folders = namespace.substring(1).split("@");
        String tempId = "root";
        for (String folder : folders) {
            ns = findNamespaceByName(tempId, folder, includeDeleted);
            if (ns.id == null && !createIfNotExists) return ns;
            if (ns.id == null) {
                createNameSpace(currentDirectory.toString(), folder);
                ns = findNamespaceByName(tempId, folder, includeDeleted);
            }
            currentDirectory.append("@").append(folder);
            tempId = ns.id;
            logger.debug(String.format("Current directory: %s", currentDirectory));
        }
        logger.info(ns.toString());
        return ns;
    }

    private static class Namespace {
        String id;
        String name;
        String type;
        String parentId;
        String version;
        String deleted;

        @Override
        public String toString() {
            return "id= " + id + " name= " + name + " type= " + type + " parentId= " + parentId + " version= "
                    + version + " deleted= " + deleted;
        }
    }

    private static class Model {
        String id;
        String name;
        String scopeId;
        String vendor;

        @Override
        public String toString() {
            return " name= " + name + " id= " + id + " scopeId= " + scopeId + " vendor= " + vendor;
        }
    }

    public static class Property {
        private String id;
        private String name;
        private String type;
        private String value;
        private String scopeId;
        private String namespaceId;
        private String version;
        private String deleted;
        private String path;

        public void setPath(String pathToSet) {
            path = pathToSet;
        }

        public String getPath() {
            return path;
        }

        Property() {

        }

        Property(String name, String path) {
            this.name = name;
            this.path = path;
        }

        @Override
        public String toString() {
            return "id= " + id + " name= " + name + " type= " + type + "path= " + path + " value= " + value + " scopeId= "
                    + scopeId + " namespaceId= " + namespaceId + " version= " + version + " deleted= " + deleted;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (this == obj) return true;
            if (getClass() != obj.getClass()) return false;

            Property other = (Property) obj;
            return name.equals(other.name) && path.equals(other.path);
        }
    }

    public static void detectRelayServer(int tryId) throws Exception {
        Socket socket = null;
        try {

            socket = new Socket(EXTERNAL_IP, DEFAULT_RELAY_PORT);

            Assert.assertEquals("Failed to detect relay! Host: " + EXTERNAL_IP + " port: " + DEFAULT_RELAY_PORT, true, socket.isConnected());
            String relayIp = resolveRelayIp(EXTERNAL_IP);

            String resolvedIp = socket.getInetAddress().getHostAddress();

            if (!resolvedIp.equals(relayIp)) {
                throw new AssertionFailedError("resolvedIp doesn't match branding system.relay_host. Expected : " +
                        relayIp + " but was : " + resolvedIp);
            }
            logger.info("PMSUtil " + resolvedIp);
            logger.info("PMSUtil IP DETECTED=" + mPmsServerIp);
            Log.d("PMSUtil", "resolvedIp=" + resolvedIp);

            restServerDetected = true;
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    // Read Transport settings file and search for Z7TP address
    public static String getDeviceZ7TpAddress() throws IOException {
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
                    logger.info("7TP address found: " + address.toString());
                    String addressString = address.toString();
                    z7TpAddress = "0x" + addressString.substring(addressString.indexOf("-") + 1, addressString.lastIndexOf("-"));
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

    public static void pushToClient() {
        HttpResponse resp = sendHttpRequestToPms(REST_MANUAL_PUSH, "");
        if (resp != null) {
            if (resp.getStatusCode() == HttpStatus.SC_OK) {
                logger.info("Property added");
            }
        } else logger.info("Property NOT added");
    }

    private static boolean isEmulator() {
        return Build.MODEL.equals(EMULATOR_NAME);
    }

    private static String resolveRelayIp(String hostName) throws UnknownHostException {
        String regExp = "([0-9]*\\.[0-9]*\\.[0-9]*\\.[0-9]*)";
        Pattern p = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(hostName);
        String ip;
        if (!m.find()) {
            InetAddress inetAddress;
            inetAddress = InetAddress.getByName(hostName);
            ip = inetAddress.getHostAddress();
        } else {
            ip = hostName;
        }
        return ip;
    }

    public static String personalScopePropertyRequestBody(String name, String path, String value, boolean important) {
        String scType = String.format("<scopeType>PERSONAL</scopeType><referencedObjectId>%s</referencedObjectId>", referencedObjectId);
        return String.format(mCreateProperty, important, path, scType, name, value);
    }

    public static HttpRequest getHttpRequestToPms(String path, String body, boolean isDelMethod) {
        try {
            HttpRequest request = HttpRequest.Builder.create().getRequest();
            request.addHeaderField(mAuthorizedHeader);
            request.setUri(String.format(URI_TEMPLATE, mPmsServerIp, mPmsServerPort, path));
            if (body != null) {
                body = body + BaseConstantsIF.CRLF + BaseConstantsIF.CRLF;
                request.setMethod("POST");
                request.addHeaderField(new HttpHeaderField("Content-type", "application/xml"));
                request.addHeaderField(new HttpHeaderField("Connection", "close"));
                request.setBody(body);
                request.addHeaderField(new HttpHeaderField("Content-length", Integer.toString(request.getBody().length())));
                return request;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void cleanPaths(String[] paths) throws Exception {
        final HashSet<String> pathsToClear = new HashSet<String>();
        for (int i = 0; i < paths.length; i++) {
            pathsToClear.add(paths[i]);
        }
        PMSUtil.cleanPersonalProperties(pathsToClear);
    }

    public static void addPolicies(Policy[] policiesToAdd) throws Exception {
        final HashSet<Policy> policies = new HashSet<Policy>();
        for (int i = 0; i < policiesToAdd.length; i++) {
            policies.add(policiesToAdd[i]);
        }
        PMSUtil.preparePmsServer(policies);
    }

    public static void addPoliciesWithCheck(Policy[] policies) throws Exception {
        final int LOGCAT_START_DELAY = 10 * 1000;
        final PolicyAddedTask paddTask = new PolicyAddedTask();
        LogcatUtil logcat = new LogcatUtil(AsimovTestCase.getStaticContext(), paddTask);
        TestUtil.sleep(LOGCAT_START_DELAY);
        try {
            logcat.start();
            PMSUtil.addPolicies(policies);
            TestUtil.sleep(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
            logcat.stop();

            if (paddTask.getLogEntries().isEmpty()) {
                Log.e(TAG, "Policy update notification not received from server. Emulating SMS notification.");
                logcat = new LogcatUtil(AsimovTestCase.getStaticContext(), paddTask);
                logcat.start();
                (new SmsUtil(AsimovTestCase.getStaticContext())).sendPolicyUpdate((byte) 1);
                TestUtil.sleep(TFConstantsIF.WAIT_FOR_POLICY_UPDATE);
                logcat.stop();
            }

            for (Policy policy : policies) {
                if (policy.isShouldBe())
                    checkPolicyReceived(paddTask, policy.getName(), policy.getValue());
            }
        } finally {
            logcat.stop();
        }
    }

    public static void checkPolicyReceived(PolicyAddedTask policyAddedTask, String propertyName, String value) {
        boolean propertyFound = false;
        for (PolicyWrapper entry : policyAddedTask.getLogEntries()) {
            if (entry.getName().equals(propertyName) && entry.getValue().equals(value)) {
                propertyFound = true;
                break;
            }
        }
        CATFAssert.assertTrue("Policy \"" + propertyName + "\" with value \"" + value + "\" does not received or applied", propertyFound);
    }

    private static void cleanAlreadyDetectedPolicies(String regexp, String path) {
        try {
            List<String> personalIdToDelete = findIdInList(regexp, path, personalPolicies);
            logger.debug(String.format("Id of the personal policies that matched the corresponding regexp: %s", personalIdToDelete));
            List<String> globalIdToDelete = findIdInList(regexp, path, globalPolicies);
            logger.debug(String.format("Id of the global policies that matched the corresponding regexp: %s", globalIdToDelete));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private static List<String> findIdInList(String regexp, String path, List<Property> list) {
        List<String> ids = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);

        for (int i = 0; i < list.size(); i++) {
            Property current = list.get(i);
            logger.debug(String.format("Processing the following property: %s", current));
            Matcher matcher = pattern.matcher(current.name);
            if (matcher.find() && path.equals(current.getPath())) {
                logger.debug("Going to delete");
                ids.add(current.id);
                deleteProperty(current.id, false);
                list.remove(i);
                i--;
            }
        }
        return ids;
    }

    public static void clearPoliciesByRegexp(String regexp, String path) {
        try {
            PMSUtil.initModelInfo();
            PMSUtil.getPropertiesForNamespace(path);
            cleanAlreadyDetectedPolicies(regexp, path);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public static void addConfigurationBypassPorts(boolean add) {
        String name = "interception_ports";
        String value = "1:24,26:109,111:219,221:464,466:586,588:992,994,996:7734,7736:8086,8088:8098,8100:8110,8112:65535";
        String path = "@asimov@interception@octcpd";
        try {
            if (add) {
                PMSUtil.addPolicies(new Policy[]{new Policy(name, value, path, true)});
            } else {
                PMSUtil.cleanPaths(new String[]{path});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
