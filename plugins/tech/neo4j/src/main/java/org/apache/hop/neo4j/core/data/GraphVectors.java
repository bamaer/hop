/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.neo4j.core.data;

import java.util.ArrayList;
import java.util.List;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.IValueMeta;

/**
 * Converts embedding vectors between Hop and graph databases.
 *
 * <p>In a Hop row a vector is the {@code float[]} of the Vector value type. Graph databases take
 * and return it as a list of numbers, which every graph connection already passes on: Bolt, AGE,
 * FalkorDB and Gremlin. This class doesn't reference the Vector value type itself, so the graph
 * plugins don't depend on it being installed.
 */
public final class GraphVectors {

  private GraphVectors() {}

  /**
   * The vector in a Hop value as a list of numbers for a graph database.
   *
   * <p>Accepts a Vector field ({@code float[]}), a {@code double[]} or list of numbers, and text in
   * the form Embed text writes ({@code [0.1,0.2,0.3]}) or without brackets ({@code 0.1,0.2}).
   */
  public static List<Double> toList(IValueMeta valueMeta, Object valueData)
      throws HopValueException {
    if (valueMeta.isNull(valueData)) {
      return null;
    }
    Object value =
        valueMeta.isStorageBinaryString()
            ? valueMeta.convertToNormalStorageType(valueData)
            : valueData;
    if (value instanceof String || value == null) {
      value = valueMeta.getString(valueData);
    }
    return toList(value);
  }

  /**
   * A vector as a list of numbers, from any of the forms {@link #toList(IValueMeta, Object)} takes.
   */
  public static List<Double> toList(Object value) throws HopValueException {
    if (value == null) {
      return null;
    }
    if (value instanceof float[] floats) {
      List<Double> list = new ArrayList<>(floats.length);
      for (float f : floats) {
        list.add((double) f);
      }
      return list;
    }
    if (value instanceof double[] doubles) {
      List<Double> list = new ArrayList<>(doubles.length);
      for (double d : doubles) {
        list.add(d);
      }
      return list;
    }
    if (value instanceof List<?> elements) {
      List<Double> list = new ArrayList<>(elements.size());
      for (Object element : elements) {
        list.add(toDouble(element));
      }
      return list;
    }
    return parse(value.toString());
  }

  /** A vector returned by a graph database as the {@code float[]} of the Hop Vector value type. */
  public static float[] toFloatArray(Object value) throws HopValueException {
    if (value == null) {
      return null;
    }
    if (value instanceof float[] floats) {
      return floats;
    }
    List<Double> list = toList(value);
    float[] floats = new float[list.size()];
    for (int i = 0; i < floats.length; i++) {
      floats[i] = list.get(i).floatValue();
    }
    return floats;
  }

  private static double toDouble(Object element) throws HopValueException {
    if (element instanceof Number number) {
      return number.doubleValue();
    }
    if (element == null) {
      throw new HopValueException("A vector can't contain empty values");
    }
    try {
      return Double.parseDouble(element.toString().trim());
    } catch (NumberFormatException e) {
      throw new HopValueException("'" + element + "' in a vector is not a number", e);
    }
  }

  private static List<Double> parse(String text) throws HopValueException {
    String body = text.trim();
    if (body.startsWith("[") && body.endsWith("]")) {
      body = body.substring(1, body.length() - 1).trim();
    }
    List<Double> list = new ArrayList<>();
    if (body.isEmpty()) {
      return list;
    }
    for (String part : body.split(",")) {
      list.add(toDouble(part));
    }
    return list;
  }
}
