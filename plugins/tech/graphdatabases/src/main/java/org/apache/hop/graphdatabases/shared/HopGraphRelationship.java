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

import org.apache.hop.graphdatabases.core.types.IRelationship;
import org.apache.hop.graphdatabases.core.types.Value;

import java.util.Map;

public class HopGraphRelationship extends HopGraphEntity implements IRelationship {

    private String startElementId;
    private String endElementId;
    private final String type;

    private HopGraphRelationship(String elementId, String startElementId, String endElementId, String type, Map<String, Value> properties){
        super(elementId, properties);
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
