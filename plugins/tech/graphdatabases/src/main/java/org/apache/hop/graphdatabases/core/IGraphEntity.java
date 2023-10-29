package org.apache.hop.graphdatabases.core;

import org.apache.hop.core.row.IValueMeta;

import java.util.Map;

public interface IGraphEntity {

    String elementId();

    Map<String, IValueMeta> getPropertiesMeta();
}
