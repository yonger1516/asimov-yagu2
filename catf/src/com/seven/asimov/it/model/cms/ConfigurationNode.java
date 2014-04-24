package com.seven.asimov.it.model.cms;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class ConfigurationNode implements Iterable<ConfigurationNode> {

    @XmlTransient
    private UUID parentId;

    @XmlElement(required = false)
    private String name = "";

    @XmlElement
    private String type;

    @XmlElement
    private String namespace;

    @XmlElement(required = false, nillable = true)
    private List<ConfigurationNode> children = new ArrayList();

    @XmlElement(required = false, nillable = true)
    private List<ConfigurationParameter> parameters = new ArrayList();

    @XmlElement(required = false, nillable = true, name = "key-index")
    @JsonProperty("key-index")
    private List<String> keyIndex = new ArrayList();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ConfigurationNode> getChildren() {
        return children;
    }

    public void setChildren(List<ConfigurationNode> children) {
        this.children = children;
    }

    public List<ConfigurationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ConfigurationParameter> parameters) {
        this.parameters = parameters;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public List<String> getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(List<String> keyIndex) {
        this.keyIndex = keyIndex;
    }

    @Override
    public String toString() {
        return "ConfigurationNode [type=" + type + ", name=" + name
                + ", namespace=" + namespace + ", parentId=" + parentId
                + " key-index=" + keyIndex + " ]";
    }


    @Override
    public Iterator<ConfigurationNode> iterator() {
        return new TreeIterator(this);
    }

    /**
     * Iterate over the tree nodes in breadth first order.
     */
    private final class TreeIterator implements Iterator<ConfigurationNode> {

        private final Queue<ConfigurationNode> queue = new LinkedList();

        private TreeIterator(ConfigurationNode root) {
            queue.add(root);
        }

        @Override
        public boolean hasNext() {
            return queue.size() > 0;
        }

        @Override
        public ConfigurationNode next() {
            ConfigurationNode current = queue.remove();
            queue.addAll(current.getChildren());
            return current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


}
