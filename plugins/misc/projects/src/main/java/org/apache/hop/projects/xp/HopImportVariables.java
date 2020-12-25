package org.apache.hop.projects.xp;

import org.apache.hop.core.config.DescribedVariable;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.extension.ExtensionPoint;
import org.apache.hop.core.extension.IExtensionPoint;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.projects.config.ProjectsConfig;
import org.apache.hop.projects.config.ProjectsConfigSingleton;
import org.apache.hop.projects.project.Project;
import org.apache.hop.projects.project.ProjectConfig;
import org.apache.hop.projects.util.ProjectsUtil;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.hopgui.HopGui;

import java.util.Collections;

@ExtensionPoint(
        id = "HopImportVariables",
        description = "Imports variables into a Hop project",
        extensionPointId = "HopImportVariables"
)
public class HopImportVariables  implements IExtensionPoint<String>{

    @Override
    public void callExtensionPoint(ILogChannel iLogChannel, IVariables variables, String projectName) throws HopException {

        String envName = "Hop Import Environment";

        HopGui hopGui = HopGui.getInstance();
        ProjectsConfig config = ProjectsConfigSingleton.getConfig();

        // open existing project
        if(!StringUtil.isEmpty(projectName)){
            ProjectConfig projectConfig = config.findProjectConfig(projectName);

            try {
                Project project = projectConfig.loadProject( hopGui.getVariables() );
                project.getDescribedVariables().clear();
                for (int i = 0; i < variables.getVariableNames().length; i++) {
                    DescribedVariable variable =
                            new DescribedVariable(
                                    variables.getVariableNames()[i], // name
                                    variables.getVariable(variables.getVariableNames()[i]), // value
                                    "" // description
                            );
                    project.getDescribedVariables().add(variable);
                }
                project.modifyVariables(variables, projectConfig, Collections.emptyList(), null);
                project.saveToFile();
                ProjectsUtil.enableProject(hopGui.getLog(), projectName, project, variables, Collections.emptyList(), null, hopGui);

            } catch ( Exception e ) {
                new ErrorDialog( hopGui.getShell(), "Error", "Error importing variables to project '" + projectName, e );
            }
        }
    }
}
