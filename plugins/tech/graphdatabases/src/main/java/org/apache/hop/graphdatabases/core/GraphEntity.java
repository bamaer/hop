package org.apache.hop.graphdatabases.core;


import org.apache.hop.core.row.IValueMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GraphEntity implements IGraphEntity{

    private final String elementId;
    private final Map<String, IValueMeta> propertiesMeta;

    public GraphEntity(String elementId){
        this(elementId, Collections.emptyMap());
    }

    public GraphEntity(String elementId, Map<String, IValueMeta> propertiesMeta) {
        this.elementId = elementId;
        this.propertiesMeta = propertiesMeta;
    }

    @Override
    public String elementId() {
        return elementId;
    }

    @Override
    public Map<String, IValueMeta> getPropertiesMeta(){
        return propertiesMeta;
    }

}
