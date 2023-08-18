package org.apache.hop.core.graph;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.plugins.IPlugin;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.metadata.api.IHopMetadataObjectFactory;

public class GraphDatabaseMetaObjectFactory implements IHopMetadataObjectFactory {

    @Override
    public Object createObject(String id, Object parentObject) throws HopException {
        PluginRegistry registry = PluginRegistry.getInstance();
        IPlugin plugin = registry.findPluginWithId(GraphDatabasePluginType.class, id);
        IGraphDatabase iGraphDatabase = (IGraphDatabase)registry.loadClass(plugin);
        return iGraphDatabase;
    }

    @Override
    public String getObjectId(Object object) throws HopException {
        if(!(object instanceof IGraphDatabase)) {
            throw new HopException(
                    "Object is not of class IGraphDatabase but of '" + object.getClass().getName() + "'");
        }
        return ((IGraphDatabase) object).getPluginId();
    }

}
