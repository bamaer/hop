package org.apache.hop.graphdatabases.core;


import org.apache.hop.core.row.IValueMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GraphEntity implements IGraphEntity{

    private List<Object[]> properties;

    public GraphEntity(String elementId){
    }

    public GraphEntity(List<Object[]> properties) {
        this.properties = properties;
    }

    @Override
    public List<Object[]> getProperties(){
        return properties;
    }

}
