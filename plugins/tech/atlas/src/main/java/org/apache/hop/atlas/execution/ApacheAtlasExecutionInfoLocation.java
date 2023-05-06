package org.apache.hop.atlas.execution;

import org.apache.atlas.AtlasClientV2;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.model.SearchFilter;
import org.apache.atlas.model.typedef.AtlasEntityDef;
import org.apache.atlas.model.typedef.AtlasRelationshipDef;
import org.apache.atlas.model.typedef.AtlasTypesDef;
import org.apache.hop.atlas.shared.AtlasConnection;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.gui.plugin.GuiElementType;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.gui.plugin.GuiWidgetElement;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.execution.Execution;
import org.apache.hop.execution.ExecutionData;
import org.apache.hop.execution.ExecutionInfoLocation;
import org.apache.hop.execution.ExecutionState;
import org.apache.hop.execution.ExecutionType;
import org.apache.hop.execution.IExecutionInfoLocation;
import org.apache.hop.execution.IExecutionMatcher;
import org.apache.hop.execution.plugin.ExecutionInfoLocationPlugin;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Collections;
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

    private IVariables variables;
    private IHopMetadataProvider metadataProvider;
    private AtlasClientV2 atlasClient;
    private AtlasTypeUtil typeUtil;

    public static final String PIPELINE_TYPE = "hop_pipeline";
    public static final String TRANSFORM_TYPE = "hop_transform";
    public static final String PIPELINE_TRANSFORM_REL_TYPE = "hop_transform_pipeline";
    public static final String PIPELINE_HOP_REL_TYPE = "hop_pipeline_hop";
    public static final String PIPELINE_EXECUTION_REL_TYPE = "hop_pipeline_execution";
    public static final String WORKFLOW_TYPE = "hop_workflow";
    public static final String ACTION_TYPE = "hop_action";
    public static final String WORKFLOW_ACTION_REL_TYPE = "hop_action_workflow";
    public static final String WORKFLOW_HOP_REL_TYPE = "hop_workflow_hop";
    public static final String WORKFLOW_EXECUTION_REL_TYPE = "hop_workflow_execution";
    public static final String[] TYPES = {
            PIPELINE_TYPE,
            TRANSFORM_TYPE,
            WORKFLOW_TYPE,
            ACTION_TYPE
    };
    private static final String[] REL_TYPES = {
            PIPELINE_TRANSFORM_REL_TYPE,
            PIPELINE_HOP_REL_TYPE,
            WORKFLOW_ACTION_REL_TYPE,
            WORKFLOW_HOP_REL_TYPE,
            PIPELINE_EXECUTION_REL_TYPE,
            WORKFLOW_EXECUTION_REL_TYPE
    };

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

    @GuiWidgetElement(
            id = "connectionName",
            order = "010",
            parentId = ExecutionInfoLocation.GUI_PLUGIN_ELEMENT_PARENT_ID,
            type = GuiElementType.METADATA,
            typeMetadata = AtlasConnectionTypeMetadata.class,
            toolTip = "i18n::AtlasExecutionInfoLocation.Connection.Tooltip",
            label = "i18n::AtlasExecutionInfoLocation.Connection.Label")
    @HopMetadataProperty(key = "connection")
    protected String connectionName;

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
            atlasClient = connection.getClient();
            typeUtil = new AtlasTypeUtil(atlasClient);

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
        String dummy = "";

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

    @Override
    public String getExecutionStateLoggingText(String executionId, int sizeLimit)
            throws HopException {
        synchronized (this) {
            try {
                return "";
//                return session.readTransaction(
//                        transaction -> getNeo4jExecutionStateLoggingText(transaction, executionId, sizeLimit));
            } catch (Exception e) {
                throw new HopException("Error getting execution from Neo4j", e);
            }
        }
    }

    @Override
    public ExecutionState getExecutionState(String executionId, boolean includeLogging)
            throws HopException {
        synchronized (this) {
            try {
                return null;
//                return session.readTransaction(
//                        transaction -> getNeo4jExecutionState(transaction, executionId, includeLogging));
            } catch (Exception e) {
                throw new HopException("Error getting execution from Neo4j", e);
            }
        }
    }

    private void verifyTypes() throws AtlasServiceException {
        MultivaluedMap<String, String> searchParams = new MultivaluedMapImpl();

        SearchFilter searchFilter = new SearchFilter();
        searchFilter.setParam("type", "entity");
        AtlasTypesDef registeredTypes = atlasClient.getAllTypeDefs(searchFilter);
        List<AtlasEntityDef> entityDefs = registeredTypes.getEntityDefs();
        List<String> entityDefNames = new ArrayList<>();
        for(AtlasEntityDef entityDef : entityDefs){
            entityDefNames.add(entityDef.getName());
        }

        List<AtlasEntityDef> typeDefinitions = new ArrayList<>();
        List<AtlasRelationshipDef> relDefinitions = new ArrayList<>();

        // check entity types
        for(String typeName : TYPES){
            if(!entityDefNames.contains(typeName)){
                System.out.println("Need to register type " + typeName);
                typeDefinitions.add(typeUtil.createType(typeName));
//                atlasClient.createAtlasTypeDefs()
//                atlasClient.createAtlasTypeDefs(typeUtil.createType(typeName));
            }
        }
        for(String relName : REL_TYPES){
            if(!entityDefNames.contains(relName)){
                System.out.println("Need to register relationship type " + relName);
                relDefinitions.add(typeUtil.createRelationship(relName));
            }
        }

        AtlasTypesDef typesDefs = new AtlasTypesDef(Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), typeDefinitions, relDefinitions, Collections.emptyList());
        try{
            atlasClient.createAtlasTypeDefs(typesDefs);
//            atlasClient.createre
////            atlasClient.createAtlasTypeDefs(typesDefs);
            System.out.println("types and relationships created");
        }catch(AtlasServiceException e){
            e.printStackTrace();
        }

    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }
}
