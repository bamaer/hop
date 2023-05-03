package org.apache.hop.atlas.shared;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasClientV2;
import org.apache.atlas.AtlasException;
import org.apache.commons.compress.compressors.lz77support.Parameters;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BasicBuilderParameters;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.DefaultParametersHandler;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.variables.IVariables;
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

    @HopMetadataProperty private String protocol;
    @HopMetadataProperty private String hostname;
    @HopMetadataProperty private String port;
    @HopMetadataProperty private String username;
    @HopMetadataProperty(password = true) private String password;

    private Configuration config;

    public AtlasConnection(){
        protocol = "http";
        hostname = "localhost";
        port = "21000";
        username = "admin";
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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public AtlasClientV2 getClient() throws HopException, AtlasException {
        String[] urls = { protocol + "://" + hostname + ":" + port };
        String[] userPass = {username, password};
        config = ApplicationProperties.get();
        config.setProperty("atlas.rest.address", urls[0]);
        AtlasClientV2 atlasClient = new AtlasClientV2(urls, userPass);
        return atlasClient;
    }
    public void test(IVariables variables) throws HopException{
        try{
            getClient();
        }catch(Exception e){
            throw new HopException("Unable to connect to Apache Atlas: " + e.getMessage(), e);
        }
    }
}
