package org.apache.hop.core.graph.model;


import java.util.List;

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
