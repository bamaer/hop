package org.apache.hop.graphdatabases.core;

public interface IGraphNode extends IGraphEntity{

    /**
     * Return all labels.
     *
     * @return a label Collection
     */
    Iterable<String> labels();

    /**
     * Test if this node has a given label
     *
     * @param label the label
     * @return {@code true} if this node has the label otherwise {@code false}
     */
    boolean hasLabel(String label);
}
