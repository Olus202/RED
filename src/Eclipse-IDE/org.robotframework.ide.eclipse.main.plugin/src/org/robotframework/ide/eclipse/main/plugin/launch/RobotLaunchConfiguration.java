/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

public class RobotLaunchConfiguration {

    static final String TYPE_ID = "org.robotframework.ide.robotLaunchConfiguration";

    private static final String USE_PROJECT_EXECUTOR = "Project executor";
    private static final String EXECUTOR_NAME = "Executor";
    private static final String EXECUTOR_ARGUMENTS_ATTRIBUTE = "Executor arguments";
    private static final String INTERPRETER_ARGUMENTS_ATTRIBUTE = "Interpreter arguments";
    private static final String INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE = "Include option enabled";
    private static final String INCLUDED_TAGS_ATTRIBUTE = "Included tags";
    private static final String EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE = "Exclude option enabled";
    private static final String EXCLUDED_TAGS_ATTRIBUTE = "Excluded tags";
    private static final String PROJECT_NAME_ATTRIBUTE = "Project name";
    private static final String TEST_SUITES_ATTRIBUTE = "Test suites";
    private static final String REMOTE_DEBUG_HOST_ATTRIBUTE = "Remote debug host";
    private static final String REMOTE_DEBUG_PORT_ATTRIBUTE = "Remote debug port";
    private static final String REMOTE_DEBUG_TIMEOUT_ATTRIBUTE = "Remote debug timeout";

    private static final String GENERAL_PURPOSE_OPTION_ENABLED_ATTRIBUTE = "General purpose option enabled";


    private final ILaunchConfiguration configuration;
    
    static ILaunchConfigurationWorkingCopy createDefault(final ILaunchConfigurationType launchConfigurationType,
            final List<IResource> resources) throws CoreException {

        final ILaunchConfigurationWorkingCopy configuration = prepareDefault(launchConfigurationType, resources);
        configuration.doSave();
        return configuration;
    }

    public static ILaunchConfigurationWorkingCopy prepareDefault(final ILaunchConfigurationType launchConfigurationType,
            final List<IResource> resources) throws CoreException {
        final String name = resources.size() == 1 ? resources.get(0).getName() : resources.get(0).getProject()
                .getName();
        final String configurationName = DebugPlugin.getDefault().getLaunchManager()
                .generateLaunchConfigurationName(name);
        final ILaunchConfigurationWorkingCopy configuration = launchConfigurationType.newInstance(null,
                configurationName);
        final Map<IResource, List<String>> resourcesMapping = new HashMap<>();
        for (final IResource resource : resources) {
            resourcesMapping.put(resource, new ArrayList<String>());
        }
        fillDefaults(configuration, resourcesMapping);
        return configuration;
    }

    public static void fillDefaults(final ILaunchConfigurationWorkingCopy launchConfig) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchConfig);
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
        try {
            robotConfig.setExecutor(SuiteExecutor.Python);
            robotConfig.setExecutorArguments(preferences.getAdditionalRobotArguments());
            robotConfig.setInterpreterArguments(preferences.getAdditionalInterpreterArguments());
            robotConfig.setProjectName("");
            robotConfig.setSuitePaths(new HashMap<String, List<String>>());
            robotConfig.setIsIncludeTagsEnabled(false);
            robotConfig.setIsExcludeTagsEnabled(false);
            robotConfig.setIncludedTags(new ArrayList<String>());
            robotConfig.setExcludedTags(new ArrayList<String>());
            robotConfig.setRemoteDebugHost("");
            robotConfig.setIsGeneralPurposeEnabled(true);
        } catch (final CoreException e) {
            DetailedErrorDialog.openErrorDialog("Problem with Launch Configuration",
                    "RED was unable to load the working copy of Launch Configuration.");
        }
    }

    public static void fillDefaults(final ILaunchConfigurationWorkingCopy launchConfig,
            final Map<IResource, List<String>> suitesMapping) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchConfig);
        final IProject project = getFirst(suitesMapping.keySet(), null).getProject();
        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
        try {
            if (robotProject.getRuntimeEnvironment() != null) {
                final SuiteExecutor interpreter = robotProject.getRuntimeEnvironment().getInterpreter();
                robotConfig.setExecutor(interpreter);
            }
            final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
            robotConfig.setExecutorArguments(preferences.getAdditionalRobotArguments());
            robotConfig.setInterpreterArguments(preferences.getAdditionalInterpreterArguments());
            robotConfig.setProjectName(project.getName());
        
            robotConfig.updateTestCases(suitesMapping);

            robotConfig.setIsIncludeTagsEnabled(false);
            robotConfig.setIsExcludeTagsEnabled(false);
            robotConfig.setIncludedTags(new ArrayList<String>());
            robotConfig.setExcludedTags(new ArrayList<String>());
            robotConfig.setRemoteDebugHost("");
            robotConfig.setIsGeneralPurposeEnabled(true);
        } catch (final CoreException e) {
            DetailedErrorDialog.openErrorDialog("Problem with Launch Configuration",
                    "RED was unable to load the working copy of Launch Configuration.");
        }
    }

    public void updateTestCases(final Map<IResource, List<String>> suitesMapping) throws CoreException {

        final Map<String, List<String>> suitesNamesMapping = new HashMap<>();
        for (final IResource resource : suitesMapping.keySet()) {
            if (!(resource instanceof IProject)) {
                suitesNamesMapping.put(resource.getProjectRelativePath().toPortableString(),
                        suitesMapping.get(resource));
            }
        }
        setSuitePaths(suitesNamesMapping);
    }
    
    public static void prepareRerunFailedTestsConfiguration(final ILaunchConfigurationWorkingCopy launchCopy,
            final String outputFilePath) throws CoreException {
        final RobotLaunchConfiguration robotLaunchConfig = new RobotLaunchConfiguration(launchCopy);
        robotLaunchConfig.setExecutorArguments("-R " + outputFilePath);
        robotLaunchConfig.setSuitePaths(new HashMap<String, List<String>>());
    }

    public static ILaunchConfigurationWorkingCopy prepareLaunchConfigurationForSelectedTestCases(
            final Map<IResource, List<String>> resourcesToTestCases) throws CoreException {

        final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        final String name = getNameForSelectedTestCasesConfiguration(resourcesToTestCases.keySet());
        final String configurationName = manager.generateLaunchConfigurationName(name);
        final ILaunchConfigurationWorkingCopy configuration = manager.getLaunchConfigurationType(TYPE_ID)
                .newInstance(null, configurationName);

        fillDefaults(configuration, resourcesToTestCases);

        final RobotLaunchConfiguration robotConfiguration = new RobotLaunchConfiguration(configuration);
        robotConfiguration.setIsGeneralPurposeEnabled(false);

        return configuration;
    }

    public static String getNameForSelectedTestCasesConfiguration(final Collection<IResource> resources) {
        if (resources.size() == 1) {
            return getFirst(resources, null).getName() + RobotLaunchConfigurationFinder.SELECTED_TESTS_CONFIG_SUFFIX;
        }
        final Set<IProject> projects = new HashSet<>();
        for (final IResource res : resources) {
            if (projects.add(res.getProject())) {
                if (projects.size() > 1) {
                    break;
                }
            }
        }
        if (projects.size() == 1) {
            return getFirst(projects, null).getName() + RobotLaunchConfigurationFinder.SELECTED_TESTS_CONFIG_SUFFIX;
        }
        return "New Configuration";
    }

    public RobotLaunchConfiguration(final ILaunchConfiguration config) {
        this.configuration = config;
    }

    public String getName() {
        return configuration.getName();
    }

    ILaunchConfigurationWorkingCopy asWorkingCopy() throws CoreException {
        return configuration instanceof ILaunchConfigurationWorkingCopy
                ? (ILaunchConfigurationWorkingCopy) configuration : configuration.getWorkingCopy();
    }

    public void setUsingInterpreterFromProject(final boolean usesProjectExecutor) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(USE_PROJECT_EXECUTOR, usesProjectExecutor);
    }
    
    public void setExecutor(final SuiteExecutor executor) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(EXECUTOR_NAME, executor == null ? "" : executor.name());
    }

    public void setExecutorArguments(final String arguments) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(EXECUTOR_ARGUMENTS_ATTRIBUTE, arguments);
    }

    public void setInterpreterArguments(final String arguments) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(INTERPRETER_ARGUMENTS_ATTRIBUTE, arguments);
    }

    public void setProjectName(final String projectName) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(PROJECT_NAME_ATTRIBUTE, projectName);
    }

    public void setSuitePaths(final Map<String, List<String>> suitesToCases) throws CoreException {
        // test case names should be always in lower case
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        final Map<String, String> suites = Maps.asMap(suitesToCases.keySet(), new Function<String, String>() {

            @Override
            public String apply(final String path) {
                final List<String> testSuites = new ArrayList<>();
                final Iterable<String> temp = filter(suitesToCases.get(path), Predicates.notNull());
                for (final String s : temp) {
                    testSuites.add(s.toLowerCase());
                }
                return Joiner.on("::").join(testSuites);
            }
        });
        launchCopy.setAttribute(TEST_SUITES_ATTRIBUTE, suites);
    }
    
    public void setIsIncludeTagsEnabled(final boolean isIncludeTagsEnabled) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, isIncludeTagsEnabled);
    }
    
    public void setIsGeneralPurposeEnabled(final boolean isGeneralPurposeEnabled) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(GENERAL_PURPOSE_OPTION_ENABLED_ATTRIBUTE, isGeneralPurposeEnabled);
    }

    public void setIncludedTags(final List<String> tags) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(INCLUDED_TAGS_ATTRIBUTE, tags);
    }
    
    public void setIsExcludeTagsEnabled(final boolean isExcludeTagsEnabled) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, isExcludeTagsEnabled);
    }
    
    public void setExcludedTags(final List<String> tags) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(EXCLUDED_TAGS_ATTRIBUTE, tags);
    }
    
    public void setRemoteDebugHost(final String host) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_DEBUG_HOST_ATTRIBUTE, host);
    }
    
    public void setRemoteDebugPort(final String port) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_DEBUG_PORT_ATTRIBUTE, port);
    }
    
    public void setRemoteDebugTimeout(final String timeout) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_DEBUG_TIMEOUT_ATTRIBUTE, timeout);
    }

    public boolean isGeneralPurposeConfiguration() throws CoreException {
        return configuration.getAttribute(GENERAL_PURPOSE_OPTION_ENABLED_ATTRIBUTE, false);
    }

    public boolean isUsingInterpreterFromProject() throws CoreException {
        return configuration.getAttribute(USE_PROJECT_EXECUTOR, true);
    }

    public SuiteExecutor getExecutor() throws CoreException {
        return SuiteExecutor.fromName(configuration.getAttribute(EXECUTOR_NAME, SuiteExecutor.Python.name()));
    }

    public String getExecutorArguments() throws CoreException {
        return configuration.getAttribute(EXECUTOR_ARGUMENTS_ATTRIBUTE, "");
    }

    public String getInterpreterArguments() throws CoreException {
        return configuration.getAttribute(INTERPRETER_ARGUMENTS_ATTRIBUTE, "");
    }

    public String getProjectName() throws CoreException {
        return configuration.getAttribute(PROJECT_NAME_ATTRIBUTE, "");
    }

    public String[] getEnvironmentVariables() throws CoreException {
        final Map<String, String> vars = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
                (Map<String, String>) null);
        if (vars == null) {
            return null;
        }
        final boolean shouldAppendVars = configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES,
                true);
        final List<String> varMappings = new ArrayList<>();
        if (shouldAppendVars) {
            append(varMappings, System.getenv());
        }
        append(varMappings, vars);
        return varMappings.toArray(new String[0]);
    }

    private void append(final List<String> varMappings, final Map<String, String> vars) {
        for (final Entry<String, String> entry : vars.entrySet()) {
            varMappings.add(entry.getKey() + "=" + entry.getValue());
        }
    }

    public Map<String, List<String>> getSuitePaths() throws CoreException {
        final Map<String, String> mapping = configuration.getAttribute(TEST_SUITES_ATTRIBUTE,
                new HashMap<String, String>());
        final Map<String, List<String>> suitesToTestsMapping = new HashMap<>();
        for (final Entry<String, String> entry : mapping.entrySet()) {
            final List<String> splittedTestNames = Splitter.on("::").omitEmptyStrings().splitToList(entry.getValue());
            suitesToTestsMapping.put(entry.getKey(), splittedTestNames);
        }
        return suitesToTestsMapping;
    }

    public Map<IResource, List<String>> collectSuitesToRun() throws CoreException {
        IProject project;
        final String projectName = getProjectName();
        if (projectName.isEmpty()) {
            project = null;
        } else {
            project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (!project.exists()) {
                project = null;
            }
        }
        final Map<IResource, List<String>> suitesToRun = new HashMap<>();
        if (project == null) {
            return suitesToRun;
        }

        final Map<String, List<String>> suitePaths = getSuitePaths();
        for (final Entry<String, List<String>> entry : suitePaths.entrySet()) {
            final IPath path = Path.fromPortableString(entry.getKey());
            final IResource resource = path.getFileExtension() == null ? project.getFolder(path)
                    : project.getFile(path);
            suitesToRun.put(resource, entry.getValue());
        }
        return suitesToRun;
    }

    public boolean isIncludeTagsEnabled() throws CoreException {
        return configuration.getAttribute(INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, false);
    }
    
    public List<String> getIncludedTags() throws CoreException {
        return configuration.getAttribute(INCLUDED_TAGS_ATTRIBUTE, new ArrayList<String>());
    }
    
    public boolean isExcludeTagsEnabled() throws CoreException {
        return configuration.getAttribute(EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, false);
    }
    
    public List<String> getExcludedTags() throws CoreException {
        return configuration.getAttribute(EXCLUDED_TAGS_ATTRIBUTE, new ArrayList<String>());
    }
    
    public String getRemoteDebugHost() throws CoreException {
        return configuration.getAttribute(REMOTE_DEBUG_HOST_ATTRIBUTE, "");
    }
    
    public Optional<Integer> getRemoteDebugPort() throws CoreException {
        if (configuration.hasAttribute(REMOTE_DEBUG_PORT_ATTRIBUTE)) {
            final String port = configuration.getAttribute(REMOTE_DEBUG_PORT_ATTRIBUTE, "");
            if (!port.isEmpty()) {
                return Optional.of(Integer.parseInt(port));
            }
        }
        return Optional.absent();
    }
    
    public Optional<Integer> getRemoteDebugTimeout() throws CoreException {
        if (configuration.hasAttribute(REMOTE_DEBUG_TIMEOUT_ATTRIBUTE)) {
            final String timeout = configuration.getAttribute(REMOTE_DEBUG_TIMEOUT_ATTRIBUTE, "");
            if (!timeout.isEmpty()) {
                return Optional.of(Integer.parseInt(timeout));
            }
        }
        return Optional.absent();
    }

    public RobotProject getRobotProject() throws CoreException {
        final IProject project = getProject();
        return RedPlugin.getModelManager().getModel().createRobotProject(project);
    }

    private IProject getProject() throws CoreException {
        final String projectName = getProjectName();
        if (projectName.isEmpty()) {
            return null;
        }
        final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (!project.exists()) {
            throw newCoreException("Project '" + projectName + "' cannot be found in workspace", null);
        }
        return project;
    }

    public boolean isSuitableFor(final List<IResource> resources) {
        try {
            for (final IResource resource : resources) {
                final IProject project = resource.getProject();
                if (!getProjectName().equals(project.getName())) {
                    return false;
                }
                boolean exists = false;
                for (final String path : getSuitePaths().keySet()) {
                    final IResource res = project.findMember(Path.fromPortableString(path));
                    if (res != null && res.equals(resource)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    return false;
                }
            }
            return true;
        } catch (final CoreException e) {
            return false;
        }
    }

    public boolean isSuitableForOnly(final List<IResource> resources) {
        try {
            final List<IResource> toCall = newArrayList();
            toCall.addAll(resources);
            final Set<String> canCall = getSuitePaths().keySet();
            if (toCall.size() != canCall.size()) {
                return false;
            }
            for (final IResource resource : resources) {
                final IProject project = resource.getProject();
                if (!getProjectName().equals(project.getName())) {
                    return false;
                }
                boolean exists = false;
                for (final String path : getSuitePaths().keySet()) {
                    final IResource res = project.findMember(Path.fromPortableString(path));
                    if (res != null && res.equals(resource)) {
                        exists = true;
                        toCall.remove(res);
                        canCall.remove(path);
                        break;
                    }
                }
                if (!exists) {
                    return false;
                }
            }
            if (toCall.size() == 0 && canCall.size() == 0) {
                return true;
            } else {
                return false;
            }
        } catch (final CoreException e) {
            return false;
        }
    }

    public String createConsoleDescription(final RobotRuntimeEnvironment env) throws CoreException {
        return isUsingInterpreterFromProject() ? env.getPythonExecutablePath() : getExecutor().executableName();
    }

    public String createExecutorVersion(final RobotRuntimeEnvironment env)
            throws RobotEnvironmentException, CoreException {
        return isUsingInterpreterFromProject() ? env.getVersion() : RobotRuntimeEnvironment.getVersion(getExecutor());
    }

    public List<String> getSuitesToRun() throws CoreException {
        final Collection<IResource> suites = getSuiteResources();

        final List<String> suiteNames = new ArrayList<>();
        for (final IResource suite : suites) {
            suiteNames.add(RobotSuitesNaming.createSuiteName(suite));
        }
        return suiteNames;
    }

    Collection<IResource> getSuiteResources() throws CoreException {
        final Collection<String> suitePaths = getSuitePaths().keySet();

        final Map<String, IResource> resources = Maps.asMap(newHashSet(suitePaths), new Function<String, IResource>() {

            @Override
            public IResource apply(final String suitePath) {
                try {
                    return getProject().findMember(Path.fromPortableString(suitePath));
                } catch (final CoreException e) {
                    return null;
                }
            }
        });

        final List<String> problems = new ArrayList<>();
        for (final Entry<String, IResource> entry : resources.entrySet()) {
            if (entry.getValue() == null || !entry.getValue().exists()) {
                problems.add(
                        "Suite '" + entry.getKey() + "' does not exist in project '" + getProject().getName() + "'");
            }
        }
        if (!problems.isEmpty()) {
            throw newCoreException(Joiner.on('\n').join(problems));
        }
        return resources.values();
    }

    public List<IResource> getResourcesUnderDebug() throws CoreException {
        final List<IResource> suiteResources = newArrayList(getSuiteResources());
        if (suiteResources.isEmpty()) {
            suiteResources.add(getProject());
        }
        return suiteResources;
    }

    public Collection<String> getTestsToRun() throws CoreException {
        final List<String> tests = new ArrayList<>();
        for (final Entry<String, List<String>> entries : getSuitePaths().entrySet()) {
            for (final String testName : entries.getValue()) {
                tests.add(RobotSuitesNaming.createSuiteName(getProject(), Path.fromPortableString(entries.getKey())) + "."
                        + testName);
            }
        }
        return tests;
    }

    public boolean isRemoteDefined() throws CoreException {
        return getRemoteDebugPort().isPresent() && !getRemoteDebugHost().isEmpty();
    }

    static boolean contentEquals(final ILaunchConfiguration config1, final ILaunchConfiguration config2)
            throws CoreException {
        final RobotLaunchConfiguration rConfig1 = new RobotLaunchConfiguration(config1);
        final RobotLaunchConfiguration rConfig2 = new RobotLaunchConfiguration(config2);
        return rConfig1.getExecutor().equals(rConfig2.getExecutor())
                && rConfig1.getExecutorArguments().equals(rConfig2.getExecutorArguments())
                && rConfig1.getProjectName().equals(rConfig2.getProjectName())
                && rConfig1.isUsingInterpreterFromProject() == rConfig2.isUsingInterpreterFromProject()
                && rConfig1.getInterpreterArguments().equals(rConfig2.getInterpreterArguments())
                && rConfig1.isExcludeTagsEnabled() == rConfig2.isExcludeTagsEnabled()
                && rConfig1.isIncludeTagsEnabled() == rConfig2.isIncludeTagsEnabled()
                && rConfig1.isGeneralPurposeConfiguration() == rConfig2.isGeneralPurposeConfiguration()
                && rConfig1.getExcludedTags().equals(rConfig2.getExcludedTags())
                && rConfig1.getIncludedTags().equals(rConfig2.getIncludedTags())
                && rConfig1.getSuitePaths().equals(rConfig2.getSuitePaths());
    }

    private static CoreException newCoreException(final String message) {
        return newCoreException(message, null);
    }

    private static CoreException newCoreException(final String message, final Throwable cause) {
        return new CoreException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, cause));
    }

}
