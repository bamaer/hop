package org.apache.hop.graphdatabases.core;

import org.apache.hop.core.row.IValueMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GraphNode extends GraphEntity implements IGraphNode{

    private final Collection<String> labels;
    private List<String> relProps;

    public GraphNode(Collection<String> labels, Map<String, IValueMeta> propertiesMeta, List<String> relProps) {
        super(propertiesMeta);
        this.labels = labels;
        this.relProps = relProps;
    }

    @Override
    public Collection<String> labels() {
        return labels;
    }

    @Override
    public boolean hasLabel(String label) {
        return labels.contains(label);
    }

    public List<String> getRelationshipProperties(){
        return relProps;
    }

    public void setRelationshipProperties(List<String> relProps){
        this.relProps = relProps;
    }

}
