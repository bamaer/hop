package org.apache.hop.graphdatabases.core;

import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaNumber;
import org.apache.hop.core.row.value.ValueMetaString;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GraphNodesTest {

    @Test
    public void createGraphNodes(){
        List<String> labels = new ArrayList<>();
        Map<String, IValueMeta> propertiesMeta = new HashMap<>();
        List<Map<String, Object>> propertiesData = new ArrayList<>();
        List<String> relProps = new ArrayList<>();

        labels.add("LABEL_ONE");
        labels.add("LABEL_TWO");

        propertiesMeta.put("prop_one", new ValueMetaInteger());
        propertiesMeta.put("prop_two", new ValueMetaString());
        propertiesMeta.put("prop_three", new ValueMetaNumber());

        relProps.add("prop_one");
        GraphNode node = new GraphNode(labels, propertiesMeta, relProps);

        assertEquals(node.getPropertiesMeta().size(), 3);
        assertEquals(node.getPropertiesMeta().get("prop_one").getType(), IValueMeta.TYPE_INTEGER);
        assertEquals(node.getPropertiesMeta().get("prop_two").getType(), IValueMeta.TYPE_STRING);
        assertEquals(node.getPropertiesMeta().get("prop_three").getType(), IValueMeta.TYPE_NUMBER);

        assertEquals(node.getRelationshipProperties().size(), 1);
    }
}
