package org.siouan.frontendgradleplugin.functional;

import static org.siouan.frontendgradleplugin.util.FunctionalTestHelper.assertTaskOutcome;
import static org.siouan.frontendgradleplugin.util.FunctionalTestHelper.runGradle;
import static org.siouan.frontendgradleplugin.util.FunctionalTestHelper.runGradleAndExpectFailure;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.siouan.frontendgradleplugin.AssembleTask;
import org.siouan.frontendgradleplugin.InstallTask;
import org.siouan.frontendgradleplugin.node.NodeInstallTask;
import org.siouan.frontendgradleplugin.util.FunctionalTestHelper;
import org.siouan.frontendgradleplugin.yarn.YarnInstallTask;

/**
 * Functional tests to verify the {@link AssembleTask} integration in a Gradle build.
 */
public class AssembleFuncTest {

    private static final String GRADLE_ASSEMBLE_TASK = "assemble";

    @TempDir
    protected File projectDirectory;

    @Test
    public void shouldFailAssemblingWhenNodeDistributionCannotBeDownloaded() throws IOException {
        FunctionalTestHelper.createBuildFile(projectDirectory, Collections.singletonMap("nodeVersion", "0.76.34"));

        final BuildResult result = runGradleAndExpectFailure(projectDirectory, GRADLE_ASSEMBLE_TASK);

        assertTaskOutcome(result, NodeInstallTask.DEFAULT_NAME, TaskOutcome.FAILED);
    }

    @Test
    public void shouldFailAssemblingWhenYarnDistributionCannotBeDownloaded() throws IOException {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("nodeVersion", "10.15.3");
        properties.put("nodeDistributionUrl", getClass().getClassLoader().getResource("node-v10.15.3.zip").toString());
        properties.put("yarnEnabled", true);
        properties.put("yarnVersion", "0.67.2");
        FunctionalTestHelper.createBuildFile(projectDirectory, properties);

        final BuildResult result = runGradleAndExpectFailure(projectDirectory, GRADLE_ASSEMBLE_TASK);

        assertTaskOutcome(result, NodeInstallTask.DEFAULT_NAME, TaskOutcome.SUCCESS);
        assertTaskOutcome(result, YarnInstallTask.DEFAULT_NAME, TaskOutcome.FAILED);
    }

    @Test
    public void shouldAssembleFrontendWithNpmOrYarn() throws IOException, URISyntaxException {
        Files.copy(new File(getClass().getClassLoader().getResource("package-npm.json").toURI()).toPath(),
            projectDirectory.toPath().resolve("package.json"));
        final Map<String, Object> properties = new HashMap<>();
        properties.put("nodeVersion", "10.15.3");
        properties.put("assembleScript", "run assemble");
        FunctionalTestHelper.createBuildFile(projectDirectory, properties);

        final BuildResult result1 = runGradle(projectDirectory, GRADLE_ASSEMBLE_TASK);

        assertTaskOutcome(result1, NodeInstallTask.DEFAULT_NAME, TaskOutcome.SUCCESS);
        assertTaskOutcome(result1, YarnInstallTask.DEFAULT_NAME, TaskOutcome.SUCCESS);
        assertTaskOutcome(result1, InstallTask.DEFAULT_NAME, TaskOutcome.SUCCESS);
        assertTaskOutcome(result1, GRADLE_ASSEMBLE_TASK, TaskOutcome.SUCCESS);

        final BuildResult result2 = runGradle(projectDirectory, GRADLE_ASSEMBLE_TASK);

        assertTaskOutcome(result2, NodeInstallTask.DEFAULT_NAME, TaskOutcome.UP_TO_DATE);
        assertTaskOutcome(result2, YarnInstallTask.DEFAULT_NAME, TaskOutcome.UP_TO_DATE);
        assertTaskOutcome(result2, InstallTask.DEFAULT_NAME, TaskOutcome.SUCCESS);
        assertTaskOutcome(result2, GRADLE_ASSEMBLE_TASK, TaskOutcome.SUCCESS);

        Files.delete(projectDirectory.toPath().resolve("package-lock.json"));
        Files.copy(new File(getClass().getClassLoader().getResource("package-yarn.json").toURI()).toPath(),
            projectDirectory.toPath().resolve("package.json"), StandardCopyOption.REPLACE_EXISTING);
        properties.put("yarnEnabled", true);
        properties.put("yarnVersion", "1.15.2");
        FunctionalTestHelper.createBuildFile(projectDirectory, properties);

        final BuildResult result3 = runGradle(projectDirectory, GRADLE_ASSEMBLE_TASK);

        assertTaskOutcome(result3, NodeInstallTask.DEFAULT_NAME, TaskOutcome.UP_TO_DATE);
        assertTaskOutcome(result3, YarnInstallTask.DEFAULT_NAME, TaskOutcome.SUCCESS);
        assertTaskOutcome(result3, InstallTask.DEFAULT_NAME, TaskOutcome.SUCCESS);
        assertTaskOutcome(result3, GRADLE_ASSEMBLE_TASK, TaskOutcome.SUCCESS);
    }
}
