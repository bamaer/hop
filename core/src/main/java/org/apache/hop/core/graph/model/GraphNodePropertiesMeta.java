package org.apache.hop.core.graph.model;

import org.apache.hop.core.row.IValueMeta;

import java.util.List;

public class GraphNodePropertiesMeta extends GraphPropertiesMeta implements IGraphProperties{

    private List<String> primaryPropertyNames;

    public GraphNodePropertiesMeta(String[] propertyNames,
                                   IValueMeta[] propertyTypes,
                                   List<String> primaryPropertyNames ){
        super(propertyNames, propertyTypes);
        this.primaryPropertyNames = primaryPropertyNames;
    }

    public List<String> getPrimaryPropertyNames() {
        return primaryPropertyNames;
    }

    public void setPrimaryPropertyNames(List<String> primaryPropertyNames) {
        this.primaryPropertyNames = primaryPropertyNames;
    }
}
