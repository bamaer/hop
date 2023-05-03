package org.apache.hop.atlas.execution;

import org.apache.hop.atlas.shared.AtlasConnection;
import org.apache.hop.core.gui.plugin.ITypeMetadata;
import org.apache.hop.metadata.api.IHopMetadata;

public class AtlasConnectionTypeMetadata implements ITypeMetadata {

    @Override
    public Class<? extends IHopMetadata> getMetadataClass() {
        return AtlasConnection.class;
    }
}
