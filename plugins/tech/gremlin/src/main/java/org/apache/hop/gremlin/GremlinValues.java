/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.gremlin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

/** Converts between Gremlin values and the plain Java values of graph connections. */
public final class GremlinValues {

  private GremlinValues() {}

  /**
   * A result as a row: a map result, as from project(), elementMap() or valueMap(), is the row
   * itself. Anything else, vertices and edges included, is a row with one value called "result".
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> toRow(Object result) {
    Object value = toValue(result);
    if (result instanceof Map<?, ?> && value instanceof Map<?, ?> map) {
      return (Map<String, Object>) map;
    }
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("result", value);
    return row;
  }

  /**
   * A Gremlin result value as maps, lists and plain values. Vertices and edges become maps with
   * their id, label and properties.
   */
  public static Object toValue(Object value) {
    if (value instanceof Vertex vertex) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("id", toValue(vertex.id()));
      map.put("label", vertex.label());
      Map<String, Object> properties = new LinkedHashMap<>();
      Iterator<VertexProperty<Object>> iterator = vertex.properties();
      while (iterator.hasNext()) {
        VertexProperty<Object> property = iterator.next();
        properties.put(property.key(), toValue(property.value()));
      }
      map.put("properties", properties);
      return map;
    }
    if (value instanceof Edge edge) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("id", toValue(edge.id()));
      map.put("label", edge.label());
      map.put("outV", toValue(edge.outVertex().id()));
      map.put("inV", toValue(edge.inVertex().id()));
      Map<String, Object> properties = new LinkedHashMap<>();
      Iterator<Property<Object>> iterator = edge.properties();
      while (iterator.hasNext()) {
        Property<Object> property = iterator.next();
        properties.put(property.key(), toValue(property.value()));
      }
      map.put("properties", properties);
      return map;
    }
    if (value instanceof Path path) {
      List<Object> list = new ArrayList<>();
      for (Object object : path.objects()) {
        list.add(toValue(object));
      }
      return list;
    }
    if (value instanceof Map<?, ?> map) {
      // The element id and label (T.id, T.label) become "id" and "label", unless the element has
      // properties with those names.
      Map<String, Object> converted = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (entry.getKey() instanceof T t) {
          converted.putIfAbsent(t.name(), toValue(entry.getValue()));
        }
      }
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (!(entry.getKey() instanceof T)) {
          converted.put(String.valueOf(entry.getKey()), toValue(entry.getValue()));
        }
      }
      return converted;
    }
    if (value instanceof Collection<?> collection) {
      List<Object> list = new ArrayList<>();
      for (Object element : collection) {
        list.add(toValue(element));
      }
      return list;
    }
    if (value instanceof Integer integer) {
      return integer.longValue();
    }
    if (value instanceof Float f) {
      return f.doubleValue();
    }
    return value;
  }

  /**
   * A property value to write. Dates and times become java.util.Date in UTC, the date type every
   * Gremlin server supports, among them Neptune and JanusGraph.
   */
  public static Object toPropertyValue(Object value) {
    if (value instanceof LocalDate localDate) {
      return Date.from(localDate.atStartOfDay(ZoneOffset.UTC).toInstant());
    }
    if (value instanceof LocalDateTime localDateTime) {
      return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }
    if (value instanceof ZonedDateTime zonedDateTime) {
      return Date.from(zonedDateTime.toInstant());
    }
    if (value instanceof OffsetDateTime offsetDateTime) {
      return Date.from(offsetDateTime.toInstant());
    }
    return value;
  }
}
