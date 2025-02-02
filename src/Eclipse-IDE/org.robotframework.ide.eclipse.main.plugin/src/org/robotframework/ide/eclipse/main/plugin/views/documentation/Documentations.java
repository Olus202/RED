/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteStreamFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordDefinitionInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordDefinitionInput.KeywordDefinitionOnSettingInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordProposalInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordSpecificationInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.LibraryImportSettingInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.LibrarySpecificationInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SuiteFileInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SuiteFileInput.SuiteFileOnSettingInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TaskInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TaskInput.TaskOnSettingInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TestCaseInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TestCaseInput.TestCaseOnSettingInput;

import com.google.common.annotations.VisibleForTesting;

public class Documentations {

    public static void showDocForKeywordSpecification(final IWorkbenchPage page, final RobotProject project,
            final LibrarySpecification librarySpecification, final KeywordSpecification keywordSpecification) {
        final DocumentationViewWrapper view = display(page,
                new KeywordSpecificationInput(project, librarySpecification, keywordSpecification));
        if (view != null) {
            page.activate(view);
        }
    }

    public static void showDocForLibrarySpecification(final IWorkbenchPage page, final RobotProject project,
            final LibrarySpecification librarySpecification) {
        final DocumentationViewWrapper view = display(page,
                new LibrarySpecificationInput(project, librarySpecification));
        if (view != null) {
            page.activate(view);
        }
    }

    public static void showDocForRobotElement(final IWorkbenchPage page, final RobotFileInternalElement element) {
        findInput(element).ifPresent(input -> {
            final DocumentationViewWrapper view = display(page, input);
            if (view != null) {
                page.activate(view);
            }
        });
    }

    public static DocumentationViewWrapper showDocForEditorSourceSelection(final IWorkbenchPage page,
            final RobotSuiteFile suiteModel, final IDocument document, final int offset) {

        final Optional<? extends RobotElement> element = suiteModel.findElement(offset);
        if (element.isPresent()) {
            try {
                final Optional<IRegion> activeCellRegion = DocumentUtilities.findCellRegion(document,
                        suiteModel.isTsvFile(), offset);
                if (activeCellRegion.isPresent()) {
                    final String cellContent = document.get(activeCellRegion.get().getOffset(),
                            activeCellRegion.get().getLength());
                    return showDoc(page, (RobotFileInternalElement) element.get(), cellContent);
                }
            } catch (final BadLocationException e) {
                RedPlugin.logError("Could not find selected position in document", e);
            }
        }
        return null;
    }

    public static Optional<DocumentationViewInput> findDocumentationForEditorSourceSelection(
            final RobotSuiteFile suiteModel, final int offset, final String cellContent) {

        final Optional<? extends RobotElement> element = suiteModel.findElement(offset);
        if (element.isPresent()) {
            final RobotFileInternalElement internalElement = (RobotFileInternalElement) element.get();
            return requiresPerLabelSearching(internalElement)
                    ? Optional.of(new KeywordProposalInput(internalElement, cellContent))
                    : findInput(internalElement);
        }
        return Optional.empty();
    }

    public static DocumentationViewWrapper showDocForEditorTablesSelection(final IWorkbenchPage page,
            final SelectionLayerAccessor selectionLayerAccessor) {
        final PositionCoordinate[] coordinates = selectionLayerAccessor.getSelectedPositions();
        return Stream.of(coordinates)
                .findFirst()
                .map(selectionLayerAccessor::getLabelFromCell)
                .flatMap(label -> selectionLayerAccessor
                        .getSelectedElements()
                        .filter(RobotFileInternalElement.class::isInstance)
                        .map(RobotFileInternalElement.class::cast)
                        .findFirst()
                        .map(elem -> showDoc(page, elem, label)))
                .orElse(null);
    }

    private static DocumentationViewWrapper showDoc(final IWorkbenchPage page, final RobotFileInternalElement element,
            final String cellContent) {

        if (element.getSuiteFile() instanceof RobotSuiteStreamFile) {
            return null;
        }

        final Optional<DocumentationViewInput> input = requiresPerLabelSearching(element)
                ? Optional.of(new KeywordProposalInput(element, cellContent))
                : findInput(element);
        if (input.isPresent()) {
            return display(page, input.get());
        }
        return null;
    }

    private static boolean requiresPerLabelSearching(final RobotFileInternalElement element) {
        if (element instanceof RobotSetting) {
            final RobotSetting setting = (RobotSetting) element;
            return !(setting.isDocumentation() || setting.isLibraryImport() || setting.isResourceImport());
        } else if (element instanceof RobotKeywordCall && ((RobotKeywordCall) element).isLocalSetting()) {
            return !((RobotKeywordCall) element).isDocumentationSetting();
        }
        return element instanceof RobotKeywordCall;
    }

    @VisibleForTesting
    static Optional<DocumentationViewInput> findInput(final RobotFileInternalElement element) {
        if (element instanceof RobotSuiteFile) {
            return Optional.of(new SuiteFileInput((RobotSuiteFile) element));

        } else if (element instanceof RobotKeywordDefinition) {
            return Optional.of(new KeywordDefinitionInput((RobotKeywordDefinition) element));

        } else if (element instanceof RobotCase) {
            return Optional.of(new TestCaseInput((RobotCase) element));

        } else if (element instanceof RobotTask) {
            return Optional.of(new TaskInput((RobotTask) element));

        } else if (element instanceof RobotKeywordCall && ((RobotKeywordCall) element).isLocalSetting()) {
            final RobotKeywordCall setting = (RobotKeywordCall) element;
            if (setting.isDocumentationSetting() && setting.getParent() instanceof RobotCase) {
                return Optional.of(new TestCaseOnSettingInput(setting));

            } else if (setting.isDocumentationSetting() && setting.getParent() instanceof RobotTask) {
                return Optional.of(new TaskOnSettingInput(setting));

            } else if (setting.isDocumentationSetting() && setting.getParent() instanceof RobotKeywordDefinition) {
                return Optional.of(new KeywordDefinitionOnSettingInput(setting));
            }

        } else if (element instanceof RobotSetting) {
            final RobotSetting setting = (RobotSetting) element;
            if (setting.isDocumentation()) {
                return Optional.of(new SuiteFileOnSettingInput(setting));

            } else if (setting.isResourceImport()) {
                final Optional<IResource> resource = setting.getImportedResource();
                if (resource.isPresent() && resource.get().exists() && resource.get().getType() == IResource.FILE) {
                    final RobotSuiteFile resourceSuite = RedPlugin.getModelManager()
                            .createSuiteFile((IFile) resource.get());

                    return Optional.of(new SuiteFileInput(resourceSuite));
                }

            } else if (setting.isLibraryImport()) {
                return Optional.of(new LibraryImportSettingInput(setting));
            }
        }
        return Optional.empty();
    }

    public static void markViewSyncBroken(final IWorkbenchPage page) {
        final IViewPart docViewPart = page.findView(DocumentationView.ID);
        if (docViewPart == null) {
            return;
        }
        final DocumentationView docView = ((DocumentationViewWrapper) docViewPart).getComponent();
        docView.markSyncBroken();
    }

    private static DocumentationViewWrapper display(final IWorkbenchPage page, final DocumentationViewInput input) {
        final DocumentationViewWrapper docView = openDocumentationViewIfNeeded(page);
        if (docView != null) {
            docView.getComponent().displayDocumentation(input);
        }
        return docView;
    }

    private static DocumentationViewWrapper openDocumentationViewIfNeeded(final IWorkbenchPage page) {
        final IViewPart docViewPart = page.findView(DocumentationView.ID);
        if (docViewPart == null) {
            try {
                return ((DocumentationViewWrapper) page.showView(DocumentationView.ID));
            } catch (final PartInitException e) {
                RedPlugin.logError("Unable to show Documentation View.", e);
                return null;
            }
        } else {
            return (DocumentationViewWrapper) docViewPart;
        }
    }
}
