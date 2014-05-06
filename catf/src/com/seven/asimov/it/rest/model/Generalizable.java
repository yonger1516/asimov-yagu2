package com.seven.asimov.it.rest.model;

import java.util.UUID;

public interface Generalizable {

    UUID getId();
    String getName();
    Status getStatus();
    
}
