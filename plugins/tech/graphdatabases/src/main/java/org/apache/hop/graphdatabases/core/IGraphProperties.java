package org.apache.hop.graphdatabases.core;

import org.apache.hop.core.row.IValueMeta;

public interface IGraphProperties {

    String[] getPropertyNames();

    void setPropertyNames(String[] propertyNames);

    IValueMeta[] getPropertyTypes();

    void setPropertyTypes(IValueMeta[] propertyTypes);

}
