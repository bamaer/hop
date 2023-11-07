package org.apache.hop.graphdatabases.core;

public interface IGraphRelationshipMeta extends IGraphProperties{

    String getType();

    void setType(String relationshipType);
}
