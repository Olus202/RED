/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.RedURI;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ImportSearchPaths.MarkedUri;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProjectPathsProvider;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

import com.google.common.collect.ImmutableMap;

/**
 * @author Michal Anglart
 */
abstract class GeneralSettingsImportsValidator implements ModelUnitValidator {

    protected final FileValidationContext validationContext;

    protected final RobotSuiteFile suiteFile;

    private final List<? extends AImported> imports;

    protected final ValidationReportingStrategy reporter;

    public GeneralSettingsImportsValidator(final FileValidationContext validationContext,
            final RobotSuiteFile suiteFile, final List<? extends AImported> imports,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.suiteFile = suiteFile;
        this.imports = imports;
        this.reporter = reporter;
    }

    protected abstract IProblemCause getCauseForNonExistingImport();

    protected abstract IProblemCause getCauseForMissingImportArguments();

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        for (final AImported imported : imports) {
            validateImport(imported);
        }
    }

    private void validateImport(final AImported imported) {
        final RobotToken pathOrNameToken = imported.getPathOrName();
        if (pathOrNameToken == null) {
            reportMissingImportArgument(imported.getDeclaration());
        } else {
            final String pathOrName = RobotExpressions.unescapeSpaces(pathOrNameToken.getText());
            if (RobotExpressions.isParameterized(pathOrName)) {
                final Map<String, String> variablesMapping = suiteFile.getRobotProject()
                        .getRobotProjectHolder()
                        .getVariableMappings();
                final String resolved = RobotExpressions.resolve(variablesMapping, pathOrName);
                if (RobotExpressions.isParameterized(resolved)) {
                    reportUnresolvedParameterizedImport(resolved, pathOrNameToken);
                } else {
                    validateSpecifiedImport(imported, resolved, pathOrNameToken, true);
                }
            } else {
                validateSpecifiedImport(imported, pathOrName, pathOrNameToken, false);
            }
        }
    }

    private void reportMissingImportArgument(final RobotToken declarationToken) {
        reporter.handleProblem(RobotProblem.causedBy(getCauseForMissingImportArguments())
                .formatMessageWith(declarationToken.getText()), validationContext.getFile(), declarationToken);
    }

    private void reportUnresolvedParameterizedImport(final String resolved, final RobotToken pathOrNameToken) {
        final Map<String, Object> additional = ImmutableMap.of(AdditionalMarkerAttributes.NAME, resolved);
        reporter.handleProblem(
                RobotProblem.causedBy(GeneralSettingsProblem.IMPORT_PATH_PARAMETERIZED)
                        .formatMessageWith(pathOrNameToken.getText()),
                validationContext.getFile(), pathOrNameToken, additional);
    }

    private void validateSpecifiedImport(final AImported imported, final String pathOrName,
            final RobotToken pathOrNameToken, final boolean isParameterized) {
        if (isPathImport(pathOrName)) {
            validatePathImport(pathOrName, pathOrNameToken, isParameterized, imported.getArguments());
        } else {
            validateNameImport(pathOrName, pathOrNameToken, imported.getArguments());
        }
    }

    protected abstract boolean isPathImport(String pathOrName);

    protected void validatePathImport(final String path, final RobotToken pathToken, final boolean isParameterized,
            final List<RobotToken> arguments) {

        if (ImportPath.hasNotEscapedWindowsPathSeparator(pathToken.getText())) {
            reportWindowsPathImport(pathToken);
            return;
        }

        final IPath importPath = new Path(path);
        if (!isParameterized && importPath.isAbsolute()) {
            reportAbsolutePathImport(pathToken, path);
        }

        final Optional<MarkedUri> absoluteMarkedPath = new RobotProjectPathsProvider(suiteFile.getRobotProject())
                .tryToFindAbsoluteMarkedUri(suiteFile.getFile(), ImportPath.from(path));
        if (!absoluteMarkedPath.isPresent()) {
            reportMissingImportPath(path, pathToken, importPath);
            return;
        }

        final URI absoluteImportUri = absoluteMarkedPath.get().getPath();
        if (isRelativeToModuleSearchPath(absoluteMarkedPath)) {
            reportModuleSearchPathRelativeness(path, pathToken, importPath, absoluteImportUri);
        }

        final IWorkspaceRoot wsRoot = suiteFile.getFile().getWorkspace().getRoot();
        final RedWorkspace redWorkspace = new RedWorkspace(wsRoot);

        final IResource resource = redWorkspace.forUri(absoluteImportUri);
        if (resource != null) {

            if (!resource.exists()) {
                reportNonExistingResource(path, pathToken, resource.getFullPath().toPortableString());
            } else {
                validateResource(resource, path, pathToken, arguments);
            }

        } else {
            reportFileOutsideOfWorkspace(path, pathToken);

            final File importAsFile = new File(absoluteImportUri);
            if (!importAsFile.exists()) {
                reportNonExistingResource(path, pathToken, null);
            } else {
                validateFile(importAsFile, path, pathToken, arguments);
            }
        }
    }

    private void reportFileOutsideOfWorkspace(final String path, final RobotToken pathToken) {
        reporter.handleProblem(
                RobotProblem.causedBy(GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE).formatMessageWith(path),
                validationContext.getFile(), pathToken);
    }

    private void reportNonExistingResource(final String path, final RobotToken pathToken,
            final String workspaceRelativePath) {
        final Map<String, Object> attributes = workspaceRelativePath != null
                ? ImmutableMap.of(AdditionalMarkerAttributes.PATH, workspaceRelativePath)
                : ImmutableMap.of();
        reporter.handleProblem(RobotProblem.causedBy(getCauseForNonExistingImport()).formatMessageWith(path),
                validationContext.getFile(), pathToken, attributes);
    }

    private boolean isRelativeToModuleSearchPath(final Optional<MarkedUri> markedUri) {
        return markedUri.isPresent() && markedUri.get().isRelativeGlobally();
    }

    private void reportModuleSearchPathRelativeness(final String path, final RobotToken pathToken,
            final IPath importPath, final URI absoluteUri) {
        final Map<String, Object> attributes = ImmutableMap.of(AdditionalMarkerAttributes.PATH,
                importPath.toPortableString());
        final String absolutePath = RedURI
                .reverseUriSpecialCharsEscapes(new File(absoluteUri).getAbsolutePath().replaceAll("\\\\", "/"));
        reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH)
                .formatMessageWith(path, absolutePath), validationContext.getFile(), pathToken, attributes);
    }

    private void reportMissingImportPath(final String path, final RobotToken pathToken, final IPath importPath) {
        final Map<String, Object> attributes = ImmutableMap.of(AdditionalMarkerAttributes.PATH,
                importPath.toPortableString());
        reporter.handleProblem(RobotProblem.causedBy(getCauseForNonExistingImport()).formatMessageWith(path),
                validationContext.getFile(), pathToken, attributes);
    }

    private void reportAbsolutePathImport(final RobotToken pathToken, final String path) {
        reporter.handleProblem(
                RobotProblem.causedBy(GeneralSettingsProblem.IMPORT_PATH_ABSOLUTE).formatMessageWith(path),
                validationContext.getFile(), pathToken);
    }

    private void reportWindowsPathImport(final RobotToken pathToken) {
        final Map<String, Object> attributes = ImmutableMap.of(AdditionalMarkerAttributes.PATH, pathToken.getText());
        final RobotProblem problem = RobotProblem
                .causedBy(GeneralSettingsProblem.IMPORT_PATH_USES_SINGLE_WINDOWS_SEPARATORS);
        reporter.handleProblem(problem, validationContext.getFile(), pathToken, attributes);
    }

    @SuppressWarnings("unused")
    protected void validateNameImport(final String name, final RobotToken nameToken, final List<RobotToken> arguments) {
        // nothing to do; override if needed
    }

    @SuppressWarnings("unused")
    protected void validateResource(final IResource resource, final String path, final RobotToken pathToken,
            final List<RobotToken> arguments) {
        // nothing to do; override if needed
    }

    @SuppressWarnings("unused")
    protected void validateFile(final File file, final String path, final RobotToken pathToken,
            final List<RobotToken> arguments) {
        // nothing to do; override if needed
    }
}
