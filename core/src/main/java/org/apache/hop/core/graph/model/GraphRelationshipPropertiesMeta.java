package org.apache.hop.core.graph.model;

import org.apache.hop.core.row.IValueMeta;

import java.util.List;

public class GraphRelationshipPropertiesMeta extends GraphPropertiesMeta implements IGraphProperties{

    private List<String> fromPrimaryPropNames, toPrimaryPropNames;

    public GraphRelationshipPropertiesMeta(String[] propertyNames,
                                           IValueMeta[] propertyTypes,
                                           List<String> fromPrimaryPropNames,
                                           List<String> toPrimaryPropNames){
        super(propertyNames, propertyTypes);
        this.fromPrimaryPropNames = fromPrimaryPropNames;
        this.toPrimaryPropNames = toPrimaryPropNames;
    }

    public List<String> getFromPrimaryPropNames() {
        return fromPrimaryPropNames;
    }

    public void setFromPrimaryPropNames(List<String> fromPrimaryPropNames) {
        this.fromPrimaryPropNames = fromPrimaryPropNames;
    }

    public List<String> getToPrimaryPropNames() {
        return toPrimaryPropNames;
    }

    public void setToPrimaryPropNames(List<String> toPrimaryPropNames) {
        this.toPrimaryPropNames = toPrimaryPropNames;
    }
}
