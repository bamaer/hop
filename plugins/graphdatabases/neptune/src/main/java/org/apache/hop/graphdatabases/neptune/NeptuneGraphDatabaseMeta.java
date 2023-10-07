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

package org.apache.hop.graphdatabases.neptune;


import org.apache.hop.core.graph.BaseBoltGraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMetaPlugin;
import org.apache.hop.core.graph.IBoltGraphDatabase;
import org.apache.hop.core.gui.plugin.GuiPlugin;

@GraphDatabaseMetaPlugin(
        type = "NEPTUNE",
        typeDescription = "AWS Neptune",
        documentationUrl = ""
)
@GuiPlugin(id = "GUI-NeptuneGraphDatabaseMeta")
public class NeptuneGraphDatabaseMeta extends BaseBoltGraphDatabaseMeta implements IBoltGraphDatabase {

/*
    @Override
    public String getBoltPort(){
        return "8183";
    }
*/
}
