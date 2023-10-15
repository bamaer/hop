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

package org.apache.hop.graphdatabases.core.types;

import java.util.List;
import java.util.function.Function;

public interface Value extends IMapAccessor, IMapAccessorWithDefaultValue {

    int size();

    boolean isEmpty();

    @Override
    Iterable<String> keys();

    Value get(int index);

    Type type();
    boolean hasType(Type type);
    boolean isTrue();
    boolean isFalse();
    boolean isNull();
    Object asObject();
    <T> T computeOrDefault(Function<Value, T> mapper, T defaultValue);

    boolean asBoolean();
    boolean asBoolean(boolean defaultValue);
    byte[] asByteArray();
    byte[] asByteArray(byte[] defaultValue);
    String asString();
    String asString(String defaultValue);
    Number asNumber();
    long asLong();
    long asLong(long defaultValue);
    int asInt();
    int asInt(int defaultValue);
    double asDouble();
    double asDouble(double defaultValue);
    float asFloat();
    float asFloat(float defaultValue);
    List<Object> asList();
    List<Object> asList(List<Object> defaultValue);
    INode asNode();
    IRelationship asRelationship();
    Path asPath();

}
