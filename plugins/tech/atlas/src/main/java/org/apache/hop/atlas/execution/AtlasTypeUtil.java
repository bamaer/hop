package org.apache.hop.atlas.execution;

import org.apache.atlas.AtlasClientV2;
import org.apache.atlas.model.typedef.AtlasBaseTypeDef;
import org.apache.atlas.model.typedef.AtlasEntityDef;
import org.apache.atlas.model.typedef.AtlasRelationshipDef;

import java.util.Collections;

import static org.apache.atlas.model.typedef.AtlasStructDef.AtlasAttributeDef.Cardinality.SET;
import static org.apache.atlas.model.typedef.AtlasStructDef.AtlasAttributeDef.Cardinality.SINGLE;
import static org.apache.atlas.type.AtlasTypeUtil.createClassTypeDef;
import static org.apache.atlas.type.AtlasTypeUtil.createRelationshipEndDef;
import static org.apache.atlas.type.AtlasTypeUtil.createRelationshipTypeDef;
import static org.apache.atlas.type.AtlasTypeUtil.createRequiredAttrDef;
import static org.apache.hop.atlas.execution.ApacheAtlasExecutionInfoLocation.ACTION_TYPE;
import static org.apache.hop.atlas.execution.ApacheAtlasExecutionInfoLocation.PIPELINE_EXECUTION_REL_TYPE;
import static org.apache.hop.atlas.execution.ApacheAtlasExecutionInfoLocation.PIPELINE_HOP_REL_TYPE;
import static org.apache.hop.atlas.execution.ApacheAtlasExecutionInfoLocation.PIPELINE_TRANSFORM_REL_TYPE;
import static org.apache.hop.atlas.execution.ApacheAtlasExecutionInfoLocation.PIPELINE_TYPE;
import static org.apache.hop.atlas.execution.ApacheAtlasExecutionInfoLocation.TRANSFORM_TYPE;
import static org.apache.hop.atlas.execution.ApacheAtlasExecutionInfoLocation.WORKFLOW_ACTION_REL_TYPE;
import static org.apache.hop.atlas.execution.ApacheAtlasExecutionInfoLocation.WORKFLOW_EXECUTION_REL_TYPE;
import static org.apache.hop.atlas.execution.ApacheAtlasExecutionInfoLocation.WORKFLOW_HOP_REL_TYPE;
import static org.apache.hop.atlas.execution.ApacheAtlasExecutionInfoLocation.WORKFLOW_TYPE;

public class AtlasTypeUtil {

    private AtlasClientV2 atlasClient;
    private static final String VERSION = "1.0";

    private static final String PIPELINE_TRANSFORM_TYPE = "transform_pipeline";

    public AtlasTypeUtil(AtlasClientV2 atlasClient){
        this.atlasClient = atlasClient;
    }

    public AtlasEntityDef createType(String entityTypeName){
        AtlasEntityDef atlasEntityDef = null;
        switch(entityTypeName){
            // base pipeline and workflow types
            case PIPELINE_TYPE:
                atlasEntityDef = createClassTypeDef(PIPELINE_TYPE, PIPELINE_TYPE, VERSION,
                        Collections.singleton("Process"),
                        createRequiredAttrDef("filename", AtlasBaseTypeDef.ATLAS_TYPE_STRING)
                );
                break;
            case TRANSFORM_TYPE:
                atlasEntityDef = createClassTypeDef(TRANSFORM_TYPE, TRANSFORM_TYPE, VERSION,
                        Collections.singleton("Process"),
                        createRequiredAttrDef("type", AtlasBaseTypeDef.ATLAS_TYPE_STRING)
                );
                break;
            case WORKFLOW_TYPE:
                atlasEntityDef = createClassTypeDef(WORKFLOW_TYPE, WORKFLOW_TYPE, VERSION,
                        Collections.singleton("Process"),
                        createRequiredAttrDef("filename", AtlasBaseTypeDef.ATLAS_TYPE_STRING)
                );
                break;
            case ACTION_TYPE:
                atlasEntityDef = createClassTypeDef(ACTION_TYPE, ACTION_TYPE, VERSION,
                        Collections.singleton("Process"),
                        createRequiredAttrDef("type", AtlasBaseTypeDef.ATLAS_TYPE_STRING)
                );
                break;
            default:
                break;
        }
        return atlasEntityDef;
    }

    public AtlasRelationshipDef createRelationship(String relationshipName){
        AtlasRelationshipDef relationshipDef = null;
        switch(relationshipName){
            // transforms in a pipeline
            case PIPELINE_TRANSFORM_REL_TYPE:
                relationshipDef = createRelationshipTypeDef(PIPELINE_TRANSFORM_REL_TYPE, PIPELINE_TRANSFORM_REL_TYPE,
                        VERSION,
                        AtlasRelationshipDef.RelationshipCategory.ASSOCIATION,
                        AtlasRelationshipDef.PropagateTags.NONE,
                        createRelationshipEndDef(PIPELINE_TYPE, "pipelineTransforms",SINGLE, false),
                        createRelationshipEndDef(TRANSFORM_TYPE, "hop_pipeline", SET, true)
                        );
                break;
            // pipeline hops
            case PIPELINE_HOP_REL_TYPE:
                relationshipDef = createRelationshipTypeDef(PIPELINE_HOP_REL_TYPE, PIPELINE_HOP_REL_TYPE,
                        VERSION,
                        AtlasRelationshipDef.RelationshipCategory.ASSOCIATION,
                        AtlasRelationshipDef.PropagateTags.NONE,
                        createRelationshipEndDef(TRANSFORM_TYPE, "hop_pipeline_hop", SINGLE, false ),
                        createRelationshipEndDef(TRANSFORM_TYPE, "hop_pipeline_hop", SINGLE, false)
                );
                break;
            // actions in a workflow
            case WORKFLOW_ACTION_REL_TYPE:
                relationshipDef = createRelationshipTypeDef(WORKFLOW_ACTION_REL_TYPE, WORKFLOW_ACTION_REL_TYPE,
                        VERSION,
                        AtlasRelationshipDef.RelationshipCategory.ASSOCIATION,
                        AtlasRelationshipDef.PropagateTags.NONE,
                        createRelationshipEndDef(WORKFLOW_TYPE, "workflowExecutions", SINGLE, false),
                        createRelationshipEndDef(ACTION_TYPE, "hop_workflow", SET, true)
                );
                break;
            case WORKFLOW_HOP_REL_TYPE:
                relationshipDef = createRelationshipTypeDef(WORKFLOW_HOP_REL_TYPE, WORKFLOW_HOP_REL_TYPE,
                        VERSION,
                        AtlasRelationshipDef.RelationshipCategory.ASSOCIATION,
                        AtlasRelationshipDef.PropagateTags.NONE,
                        createRelationshipEndDef(ACTION_TYPE, "hop_workflow_hop", SINGLE, false),
                        createRelationshipEndDef(ACTION_TYPE, "hop_workflow_hop", SINGLE, false)
                );
                break;
            case PIPELINE_EXECUTION_REL_TYPE:
                relationshipDef = createRelationshipTypeDef(PIPELINE_EXECUTION_REL_TYPE, PIPELINE_EXECUTION_REL_TYPE,
                        VERSION,
                        AtlasRelationshipDef.RelationshipCategory.AGGREGATION,
                        AtlasRelationshipDef.PropagateTags.NONE,
                        createRelationshipEndDef(PIPELINE_TYPE, "pipelineExecutions", SET, true),
                        createRelationshipEndDef(PIPELINE_EXECUTION_REL_TYPE, "pipeline", SINGLE, false)
                );
                break;
            case WORKFLOW_EXECUTION_REL_TYPE:
                relationshipDef = createRelationshipTypeDef(WORKFLOW_EXECUTION_REL_TYPE, WORKFLOW_EXECUTION_REL_TYPE,
                        VERSION,
                        AtlasRelationshipDef.RelationshipCategory.AGGREGATION,
                        AtlasRelationshipDef.PropagateTags.NONE,
                        createRelationshipEndDef(WORKFLOW_TYPE, "workflowExecution", SET, true),
                        createRelationshipEndDef(WORKFLOW_EXECUTION_REL_TYPE, "workflow", SINGLE, false)
                        );
                break;

            default:
                break;

        }
        return relationshipDef;
    }
}
