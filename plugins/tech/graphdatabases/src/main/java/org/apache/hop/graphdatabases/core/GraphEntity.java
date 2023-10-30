package org.apache.hop.graphdatabases.core;


import org.apache.hop.core.row.IValueMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GraphEntity implements IGraphEntity{

    private final Map<String, IValueMeta> propertiesMeta;

    public GraphEntity(String elementId){
        this(Collections.emptyMap());
    }

    public GraphEntity(Map<String, IValueMeta> propertiesMeta) {
        this.propertiesMeta = propertiesMeta;
    }

    @Override
    public Map<String, IValueMeta> getPropertiesMeta(){
        return propertiesMeta;
    }

}
