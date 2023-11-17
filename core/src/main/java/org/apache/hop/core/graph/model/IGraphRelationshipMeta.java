package org.apache.hop.core.graph.model;

public interface IGraphRelationshipMeta extends IGraphProperties{

    String getType();

    void setType(String relationshipType);
}
