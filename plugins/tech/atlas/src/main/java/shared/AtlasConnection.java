package shared;

import org.apache.atlas.AtlasClientV2;
import org.apache.hop.metadata.api.HopMetadata;
import org.apache.hop.metadata.api.HopMetadataBase;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadata;

@HopMetadata(
        key = "atlas-connection",
        name = "Apache Atlas Connection",
        description = "A shared connection to an Apache Atlas instance",
        image = "atlas_logo.svg",
        documentationUrl = "/metadata-types/atlas/atlas-connection.html"
)
public class AtlasConnection extends HopMetadataBase implements IHopMetadata {

    @HopMetadataProperty private String baseUrl;
    @HopMetadataProperty private String userName;
    @HopMetadataProperty(password = true) private String password;

    public AtlasConnection(){
        baseUrl = "http://localhost:21000";
        userName = "admin";
        password = "admin";
    }

    public AtlasConnection(AtlasClientV2 atlasClient){
    }

    @Override
    public String toString(){
        return name == null ? super.toString() : name;
    }

    @Override
    public int hashCode(){
        return name == null ? super.hashCode() : name.hashCode();
    }

    @Override
    public boolean equals(Object object){
        return false; 
    }
}
