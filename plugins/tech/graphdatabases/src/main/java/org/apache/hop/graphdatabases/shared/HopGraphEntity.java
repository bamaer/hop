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

package org.apache.hop.graphdatabases.shared;

import org.apache.hop.graphdatabases.core.types.IAsValue;
import org.apache.hop.graphdatabases.core.types.IEntity;
import org.apache.hop.graphdatabases.core.types.Value;
import org.apache.hop.graphdatabases.utils.Extract;

import java.util.Map;
import java.util.function.Function;

import static org.apache.hop.graphdatabases.Values.ofObject;

public class HopGraphEntity implements IEntity, IAsValue {

    private final String elementId;
    private final Map<String, Value> properties;

    public HopGraphEntity(String elementId, Map<String, Value> properties){
        this.elementId = elementId;
        this.properties = properties;
    }
    @Override
    public String elementId() {
        return elementId;
    }

    @Override
    public int size() {
        return properties.size();
    }

    @Override
    public Map<String, Object> asMap() {
        return asMap(ofObject());
    }

    @Override
    public <T> Map<String, T> asMap(Function<Value, T> mapFunction) {
        return Extract.map(properties, mapFunction);
    }


    @Override
    public Value asValue() {
        return null;
    }

    @Override
    public Iterable<String> keys() {
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public Value get(String key) {
        return null;
    }

    @Override
    public Iterable<Value> values() {
        return null;
    }

    @Override
    public <T> Iterable<T> values(Function<Value, T> mapFunction) {
        return null;
    }

}
