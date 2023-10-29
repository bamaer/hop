package org.apache.hop.graphdatabases.core;

public interface IGraphRelationship extends IGraphEntity{

    /**
     * The id of the node where this relationship starts.
     *
     * @return the node id
     */
    String startNodeElementId();

    /**
     * The id of the node where this relationship ends.
     *
     * @return the node id
     */
    String endNodeElementId();

    /**
     * Return the <em>type</em> of this relationship.
     *
     * @return the type name
     */
    String type();

    /**
     * Test if this relationship has the given type
     *
     * @param relationshipType the give relationship type
     * @return {@code true} if this relationship has the given relationship type otherwise {@code false}
     */
    boolean hasType(String relationshipType);

}
