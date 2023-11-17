package org.apache.hop.core.graph.model;

import org.apache.hop.core.row.IValueMeta;

public interface IGraphProperties {

    String[] getPropertyNames();

    void setPropertyNames(String[] propertyNames);

    IValueMeta[] getPropertyTypes();

    void setPropertyTypes(IValueMeta[] propertyTypes);

}
