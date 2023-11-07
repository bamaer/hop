package org.apache.hop.graphdatabases.core;

import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.variables.IVariables;

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
