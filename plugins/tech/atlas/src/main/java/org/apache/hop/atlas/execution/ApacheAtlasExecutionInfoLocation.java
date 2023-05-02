package org.apache.hop.atlas.execution;

import org.apache.atlas.AtlasClientV2;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.model.SearchFilter;
import org.apache.atlas.model.typedef.AtlasTypesDef;
import org.apache.hop.atlas.shared.AtlasConnection;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.execution.Execution;
import org.apache.hop.execution.ExecutionData;
import org.apache.hop.execution.ExecutionState;
import org.apache.hop.execution.ExecutionType;
import org.apache.hop.execution.IExecutionInfoLocation;
import org.apache.hop.execution.IExecutionMatcher;
import org.apache.hop.execution.plugin.ExecutionInfoLocationPlugin;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

@GuiPlugin(description="Apache Atlas Execution Information GUI Elements")
@ExecutionInfoLocationPlugin(
        id="apache-atlas-location",
        name="Apache Atlas location",
        description="Logs metadata and execution information to Apache Atlas"
)
public class ApacheAtlasExecutionInfoLocation implements IExecutionInfoLocation {

    @HopMetadataProperty protected String pluginId;
    @HopMetadataProperty protected String pluginName;
    @HopMetadataProperty protected String connectionName;

    private IVariables variables;
    private IHopMetadataProvider metadataProvider;
    private AtlasClientV2 atlasClient;

    private static final String PIPELINE_TYPE = "hop_pipeline";
    private static final String TRANSFORM_TYPE = "hop_tansform";
    private static final String PIPELINE_TRANSFORM_TYPE = "transform_pipeline";
    private static final String PIPELINE_HOP_TYPE = "pipeline_hop";
    private static final String VERSION = "1.0";
    private static final String[] TYPES = {PIPELINE_TYPE, TRANSFORM_TYPE, PIPELINE_TRANSFORM_TYPE, PIPELINE_HOP_TYPE};

    public ApacheAtlasExecutionInfoLocation(){
        this.connectionName = "atlas";
        this.pluginId = "apache-atlas-execinfo";
        this.pluginName = "Apache Atlas Execution Information";
    }

    public ApacheAtlasExecutionInfoLocation(ApacheAtlasExecutionInfoLocation location){
        this.pluginId = location.pluginId;
        this.pluginName = location.pluginName;
        this.connectionName = location.connectionName;
    }

    @Override
    public String getPluginId() {
        return pluginId;
    }

    @Override
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    @Override
    public IExecutionInfoLocation clone() {
        return new ApacheAtlasExecutionInfoLocation(this);
    }

    @Override
    public void initialize(IVariables variables, IHopMetadataProvider metadataProvider) throws HopException {
        this.variables = variables;
        this.metadataProvider = metadataProvider;

        // Initialize Apache Atlas client
        try {
            AtlasConnection connection =
                    metadataProvider
                            .getSerializer(AtlasConnection.class)
                            .load(variables.resolve(connectionName));
            AtlasClientV2 atlasClient = connection.getClient();
            if (atlasClient == null) {
                throw new HopException("Unable to find Apache Atlas connection " + connectionName);
            }
        }catch (Exception e){
            throw new HopException("Error initializing Apache Atlas execution information: " + e.getMessage(), e);
        }
        try{
            verifyTypes();
        }catch(AtlasServiceException e){
            throw new HopException("Error verifying Apache Hop types in Apache Atlas: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws HopException {
        // close Apache Atlas client.
    }

    @Override
    public void registerExecution(Execution execution) throws HopException {

    }

    @Override
    public void updateExecutionState(ExecutionState executionState) throws HopException {

    }

    @Override
    public boolean deleteExecution(String s) throws HopException {
        return false;
    }

    @Override
    public ExecutionState getExecutionState(String s) throws HopException {
        return null;
    }

    @Override
    public void registerData(ExecutionData executionData) throws HopException {

    }

    @Override
    public List<String> getExecutionIds(boolean b, int i) throws HopException {
        return null;
    }

    @Override
    public Execution getExecution(String s) throws HopException {
        return null;
    }

    @Override
    public List<Execution> findExecutions(String s) throws HopException {
        return null;
    }

    @Override
    public Execution findPreviousSuccessfulExecution(ExecutionType executionType, String s) throws HopException {
        return null;
    }

    @Override
    public List<Execution> findExecutions(IExecutionMatcher iExecutionMatcher) throws HopException {
        return null;
    }

    @Override
    public ExecutionData getExecutionData(String s, String s1) throws HopException {
        return null;
    }

    @Override
    public Execution findLastExecution(ExecutionType executionType, String s) throws HopException {
        return null;
    }

    @Override
    public List<String> findChildIds(ExecutionType executionType, String s) throws HopException {
        return null;
    }

    @Override
    public String findParentId(String s) throws HopException {
        return null;
    }

    private void verifyTypes() throws AtlasServiceException {
        MultivaluedMap<String, String> searchParams = new MultivaluedMapImpl();

        for(String typeName : TYPES){
            SearchFilter searchFilter = new SearchFilter();
            AtlasTypesDef registeredTypes = atlasClient.getAllTypeDefs(searchFilter);
            if(registeredTypes.isEmpty()){
                System.out.println("Need to register type " + typeName);
            }
        }
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }
}
