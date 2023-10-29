package org.apache.hop.graphdatabases.core;

import org.apache.hop.core.row.IValueMeta;

import java.util.Collection;
import java.util.Map;

public class GraphNode extends GraphEntity implements IGraphNode{

    private final Collection<String> labels;

    public GraphNode(String elementId, Collection<String> labels, Map<String, IValueMeta> propertiesMeta) {
        super(elementId, propertiesMeta);
        this.labels = labels;
    }

    @Override
    public Collection<String> labels() {
        return labels;
    }

    @Override
    public boolean hasLabel(String label) {
        return labels.contains(label);
    }

}
