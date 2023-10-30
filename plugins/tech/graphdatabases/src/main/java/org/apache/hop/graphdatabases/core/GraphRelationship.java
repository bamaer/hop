package org.apache.hop.graphdatabases.core;

import org.apache.hop.core.row.IValueMeta;

import java.util.Map;

public class GraphRelationship extends GraphEntity implements IGraphRelationship {

    private String startElementId;
    private String endElementId;
    private final String type;
    public GraphRelationship(
            String elementId,
            String startElementId,
            String endElementId,
            String type,
            Map<String, IValueMeta> properties) {
        super(properties);
        this.startElementId = startElementId;
        this.endElementId = endElementId;
        this.type = type;
    }


    @Override
    public String startNodeElementId() {
        return startElementId;
    }

    @Override
    public String endNodeElementId() {
        return endElementId;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public boolean hasType(String relationshipType) {
        return false;
    }
}
