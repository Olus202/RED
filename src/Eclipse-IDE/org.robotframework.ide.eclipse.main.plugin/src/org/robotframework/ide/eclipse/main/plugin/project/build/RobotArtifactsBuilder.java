/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.PythonVersion;
import org.rf.ide.core.project.NullRobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy.ReportingInterruptedException;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProjectConfigurationProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.libs.LibrariesBuilder;

import com.google.common.annotations.VisibleForTesting;

class RobotArtifactsBuilder {

    private final IProject project;

    private final BuildLogger logger;

    RobotArtifactsBuilder(final IProject project, final BuildLogger logger) {
        this.project = project;
        this.logger = logger;
    }

    Job createBuildJob(final boolean rebuildNeeded, final ValidationReportingStrategy reporter,
            final ValidationReportingStrategy fatalReporter) {
        if (rebuildNeeded) {
            logger.log("BUILDING: refreshing project");

            return new Job("Building") {

                @Override
                protected IStatus run(final IProgressMonitor monitor) {
                    try {
                        try {
                            project.getFile(".project")
                                    .deleteMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_INFINITE);
                            project.getFile(RobotProjectConfig.FILENAME)
                                    .deleteMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_INFINITE);
                        } catch (final CoreException e) {
                            // that's fine, lets try to build project
                        }
                        buildArtifacts(project, monitor, reporter, fatalReporter);

                        return Status.OK_STATUS;
                    } catch (final ReportingInterruptedException e) {
                        return new Status(IStatus.CANCEL, RedPlugin.PLUGIN_ID, "Unable to build project", e);
                    } finally {
                        monitor.done();
                    }
                }
            };
        } else {
            return new Job("Skipping build") {

                @Override
                protected IStatus run(final IProgressMonitor monitor) {
                    logger.log("BUILDING: skipped");
                    monitor.done();
                    return Status.OK_STATUS;
                }
            };
        }
    }

    private void buildArtifacts(final IProject project, final IProgressMonitor monitor,
            final ValidationReportingStrategy reporter, final ValidationReportingStrategy fatalReporter) {
        if (monitor.isCanceled()) {
            return;
        }
        logger.log("BUILDING: project '" + project.getName() + "' build started");
        final SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
        subMonitor.beginTask("Building", 100);
        subMonitor.subTask("checking Robot execution environment");

        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
        final SubMonitor configCreationMonitor = subMonitor.newChild(15);
        final RobotProjectConfig configuration = provideConfiguration(robotProject, reporter);
        configCreationMonitor.done();
        if (subMonitor.isCanceled()) {
            return;
        }

        final SubMonitor runtimeEnvCreationMonitor = subMonitor.newChild(15);
        final IRuntimeEnvironment runtimeEnvironment = provideRuntimeEnvironment(robotProject, configuration,
                fatalReporter);
        checkRuntimeEnvironment(runtimeEnvironment, robotProject, reporter);
        runtimeEnvCreationMonitor.done();
        if (subMonitor.isCanceled()) {
            return;
        }

        new LibrariesBuilder(logger).buildLibraries(robotProject, runtimeEnvironment, configuration,
                subMonitor.newChild(70));
        logger.log("BUILDING: project '" + project.getName() + "' build finished");
    }

    @VisibleForTesting
    RobotProjectConfig provideConfiguration(final RobotProject robotProject,
            final ValidationReportingStrategy reporter) {
        try {
            if (!robotProject.getConfigurationFile().exists()) {
                final RobotProblem problem = RobotProblem.causedBy(ProjectConfigurationProblem.CONFIG_FILE_MISSING);
                reporter.handleProblem(problem, robotProject.getFile(".project"), 1);
                return new NullRobotProjectConfig();
            }
            return robotProject.readRobotProjectConfig();
        } catch (final CannotReadProjectConfigurationException e) {
            final RobotProblem problem = RobotProblem.causedBy(ProjectConfigurationProblem.CONFIG_FILE_READING_PROBLEM)
                    .formatMessageWith(e.getMessage());
            reporter.handleProblem(problem, robotProject.getConfigurationFile(), e.getLineNumber());
            return new NullRobotProjectConfig();
        }
    }

    @VisibleForTesting
    IRuntimeEnvironment provideRuntimeEnvironment(final RobotProject robotProject,
            final RobotProjectConfig configuration, final ValidationReportingStrategy reporter) {

        final IRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
        if (runtimeEnvironment.isNullEnvironment()) {
            final String location = configuration.providePythonLocation();
            final RobotProblem problem = RobotProblem.causedBy(ProjectConfigurationProblem.ENVIRONMENT_MISSING)
                    .formatMessageWith(location == null ? "" : location);
            reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);

        } else if (!runtimeEnvironment.isValidPythonInstallation()) {
            final RobotProblem problem = RobotProblem.causedBy(ProjectConfigurationProblem.ENVIRONMENT_NOT_A_PYTHON)
                    .formatMessageWith(runtimeEnvironment.getFile());
            reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);

        } else if (!runtimeEnvironment.hasRobotInstalled()) {
            final RobotProblem problem = RobotProblem.causedBy(ProjectConfigurationProblem.ENVIRONMENT_HAS_NO_ROBOT)
                    .formatMessageWith(runtimeEnvironment.getFile());
            reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);
        }
        return runtimeEnvironment;
    }

    void checkRuntimeEnvironment(final IRuntimeEnvironment runtimeEnvironment, final RobotProject robotProject,
            final ValidationReportingStrategy reporter) {

        if (runtimeEnvironment.getRobotVersion().isDeprecated()) {
            final RobotProblem problem = RobotProblem.causedBy(ProjectConfigurationProblem.ENVIRONMENT_DEPRECATED_ROBOT)
                    .formatMessageWith(runtimeEnvironment.getRobotVersion().asString());
            reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);

        } else if (PythonVersion.from(runtimeEnvironment.getVersion()).isDeprecated()) {
            final RobotProblem problem = RobotProblem
                    .causedBy(ProjectConfigurationProblem.ENVIRONMENT_DEPRECATED_PYTHON)
                    .formatMessageWith(runtimeEnvironment.getFile(),
                            PythonVersion.from(runtimeEnvironment.getVersion()).asString());
            reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);
        }
    }
}
