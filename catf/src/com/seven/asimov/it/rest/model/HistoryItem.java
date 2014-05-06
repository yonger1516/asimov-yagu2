package com.seven.asimov.it.rest.model;

import org.jtransfo.DomainClass;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@DomainClass("com.seven.oc.sa.db.model.HistoryItem")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HistoryItem {

    @XmlElement
    private long timestamp;
    @XmlElement
    private String description;
    @XmlElement
    private List<String> details;
    // TODO later this should reflect to the user ID.
    // This requires refactoring in the authentication module
    @XmlElement
    private String userName;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
