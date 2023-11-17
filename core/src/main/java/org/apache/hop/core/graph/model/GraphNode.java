package org.apache.hop.core.graph.model;

import java.util.Collection;
import java.util.List;

public class GraphNode extends GraphEntity implements IGraphNode{

    private List<String> labels;
//    private List<String> relProps;

    public GraphNode(List<String> labels, List<Object[]> properties) {
        super(properties);
        this.labels = labels;
//        this.relProps = relProps;
    }

    @Override
    public Collection<String> labels() {
        return labels;
    }

    @Override
    public boolean hasLabel(String label) {
        return labels.contains(label);
    }

//    public List<String> getRelationshipProperties(){
//        return relProps;
//    }
//
//    public void setRelationshipProperties(List<String> relProps){
//        this.relProps = relProps;
//    }

}
