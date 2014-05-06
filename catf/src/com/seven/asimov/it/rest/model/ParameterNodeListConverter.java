package com.seven.asimov.it.rest.model;

import org.jtransfo.JTransfoImpl;
import org.jtransfo.ListTypeConverter;

public class ParameterNodeListConverter extends ListTypeConverter {

    public ParameterNodeListConverter() {
        super("parameterNodeListConverter", ParameterNode.class);
        setJTransfo(new JTransfoImpl());
    }

}
