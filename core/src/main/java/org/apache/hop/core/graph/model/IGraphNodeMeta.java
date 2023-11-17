package org.apache.hop.core.graph.model;

import java.util.List;

public interface IGraphNodeMeta extends IGraphProperties{

    List<String> getLabels();

    void setLabels(String[] labels);
}
