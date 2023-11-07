package org.apache.hop.graphdatabases.core;

import org.apache.hop.core.row.IValueMeta;

import java.util.List;
import java.util.Map;

public interface IGraphEntity {

    List<Object[]> getProperties();
}
