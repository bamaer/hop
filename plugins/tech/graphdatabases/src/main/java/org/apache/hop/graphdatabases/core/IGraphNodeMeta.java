package org.apache.hop.graphdatabases.core;

import java.util.List;

public interface IGraphNodeMeta extends IGraphProperties{

    List<String> getLabels();

    void setLabels(String[] labels);
}
