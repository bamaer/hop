package org.apache.hop.core.graph.model;

import org.apache.hop.core.row.IValueMeta;

public abstract class GraphPropertiesMeta implements IGraphProperties{

    public String[] propertyNames;
    public IValueMeta[] propertyTypes;
    public GraphPropertiesMeta(String[] propertyNames, IValueMeta[] propertyTypes){
        this.propertyNames = propertyNames;
        this.propertyTypes = propertyTypes;
    }

    @Override
    public String[] getPropertyNames() {
        return propertyNames;
    }

    @Override
    public void setPropertyNames(String[] propertyNames) {
        this.propertyNames = propertyNames;
    }

    @Override
    public IValueMeta[] getPropertyTypes() {
        return propertyTypes;
    }

    @Override
    public void setPropertyTypes(IValueMeta[] propertyTypes) {
        this.propertyTypes = propertyTypes;
    }
}
