package org.apache.hop.core.graph;


@GraphDatabaseMetaPlugin(
        type = "NONE",
        typeDescription = "No graph connection type",
        documentationUrl = ""
)
public class NoneGraphDatabaseMeta extends BaseGraphDatabaseMeta implements IGraphDatabase{

    @Override
    public boolean isSupportsTimestampDataType() {
        return false;
    }

    @Override
    public void setSupportsTimestampDataType(boolean b) {

    }
}
