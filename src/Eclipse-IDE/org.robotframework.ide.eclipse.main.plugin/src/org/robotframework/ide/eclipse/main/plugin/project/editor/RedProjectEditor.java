/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.navigator.RobotValidationExcludedDecorator;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedXmlFileChangeListener.OnRedConfigFileChange;
import org.robotframework.ide.eclipse.main.plugin.project.editor.general.GeneralProjectConfigurationEditorPart;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.LibrariesProjectConfigurationEditorPart;
import org.robotframework.ide.eclipse.main.plugin.project.editor.validation.ProjectValidationConfigurationEditorPart;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariablesProjectConfigurationEditorPart;
import org.robotframework.red.swt.SwtThread;

public class RedProjectEditor extends MultiPageEditorPart {

    public static final String ID = "org.robotframework.ide.project.editor";

    @Inject
    private IEventBroker eventBroker;

    private final List<IEditorPart> parts = new ArrayList<>();

    private RedProjectEditorInput editorInput;

    private IResourceChangeListener resourceListener;

    public RedProjectEditorInput getRedProjectEditorInput() {
        return editorInput;
    }

    @Override
    protected void setInput(final IEditorInput input) {
        if (input instanceof FileEditorInput) {
            final IFile file = ((FileEditorInput) input).getFile();
            setPartName(file.getProject().getName() + "/" + input.getName());

            editorInput = new RedProjectEditorInput(file, !file.isReadOnly(),
                    new RedEclipseProjectConfigReader().readConfigurationWithLines(file));
            installResourceListener();
        } else {
            final IStorage storage = input.getAdapter(IStorage.class);
            if (storage != null) {
                setPartName(storage.getName() + " [" + storage.getFullPath() + "]");

                try (InputStream stream = storage.getContents()) {
                    editorInput = new RedProjectEditorInput(!storage.isReadOnly(),
                            new RobotProjectConfigReader().readConfigurationWithLines(stream));
                } catch (final CoreException | IOException e) {
                    throw new IllegalProjectConfigurationEditorInputException(
                            "Unable to open editor: unrecognized input of class: " + input.getClass().getName(), e);
                }
            } else {
                throw new IllegalProjectConfigurationEditorInputException(
                        "Unable to open editor: unrecognized input of class: " + input.getClass().getName());
            }
        }
        super.setInput(input);
    }

    @Override
    protected void createPages() {
        try {
            final IEclipseContext context = prepareContext();

            addEditorPart(new GeneralProjectConfigurationEditorPart(), "General", context);
            addEditorPart(new LibrariesProjectConfigurationEditorPart(), "Libraries", context);
            addEditorPart(new VariablesProjectConfigurationEditorPart(), "Variables", context);
            addEditorPart(new ProjectValidationConfigurationEditorPart(), "Validation", context);

            setActivePart(getPageToActivate());

            setupEnvironmentLoadingJob();
        } catch (final PartInitException e) {
            throw new EditorInitializationException("Unable to initialize editor", e);
        }
    }

    private IEclipseContext prepareContext() {
        final IEclipseContext parentContext = getSite().getService(IEclipseContext.class);
        final IEclipseContext context = parentContext.getActiveLeaf();
        context.set(RedProjectEditorInput.class, editorInput);
        context.set(IEditorSite.class, getEditorSite());
        context.set(RedProjectEditor.class, this);
        ContextInjectionFactory.inject(this, context);
        return context;
    }

    private void installResourceListener() {
        final IProject project = editorInput.getRobotProject().getProject();
        resourceListener = new RedXmlFileChangeListener(project, new OnRedConfigFileChange() {

            @Override
            public void whenFileMoved(final IPath movedToPath) {
                final IFile movedFile = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(movedToPath);
                final IEditorInput input = new FileEditorInput(movedFile);

                setPartName(movedFile.getProject().getName() + "/" + input.getName());
                editorInput.setFile(movedFile);
                editorInput.setEditable(!movedFile.isReadOnly());

                RedProjectEditor.super.setInput(input);
            }

            @Override
            public void whenFileRemoved() {
                SwtThread.syncExec(() -> getSite().getPage().closeEditor(RedProjectEditor.this, false));
            }

            @Override
            public void whenFileChanged() {
                editorInput.setConfig(new RedEclipseProjectConfigReader()
                        .readConfigurationWithLines(((FileEditorInput) getEditorInput()).getFile()));
                setupEnvironmentLoadingJob();
            }

            @Override
            public void whenFileMarkerChanged() {
                SwtThread.syncExec(() -> eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_MARKER_CHANGED,
                        new RedProjectConfigEventData<>(editorInput.getFile(), editorInput.getProjectConfiguration())));
            }
        });
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
    }

    private void addEditorPart(final IEditorPart part, final String title, final IEclipseContext context)
            throws PartInitException {
        parts.add(part);
        ContextInjectionFactory.inject(part, context);
        setPageText(addPage(part, getEditorInput()), title);
    }

    private String getPageToActivate() {
        if (getEditorInput() instanceof IFileEditorInput) {
            final IFileEditorInput fileInput = (IFileEditorInput) getEditorInput();
            final String projectName = fileInput.getFile().getProject().getName();

            final String sectionName = ID + ".activePage." + projectName;
            final IDialogSettings dialogSettings = RedPlugin.getDefault().getDialogSettings();
            final IDialogSettings section = dialogSettings.getSection(sectionName);
            if (section == null) {
                return null;
            }
            return section.get("activePage");
        }
        return null;
    }

    private void setActivePart(final String simpleClassNameOfPage) {
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editorPart = getEditor(i);
            if (editorPart.getClass().getSimpleName().equals(simpleClassNameOfPage)) {
                setActivePage(i);
                return;
            }
        }
        setActivePage(0);
    }

    private void setupEnvironmentLoadingJob() {
        final String activeEnv = "activeEnv";
        final String allEnvs = "allEnvs";

        final Job envLoadingJob = new Job("Reading available frameworks") {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                SwtThread.syncExec(() -> eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADING_STARTED,
                        new RedProjectConfigEventData<>(editorInput.getFile(), editorInput.getProjectConfiguration())));

                final RobotProject project = editorInput.getRobotProject();
                final IRuntimeEnvironment activeEnvironment = project == null ? new NullRuntimeEnvironment()
                        : project.getRuntimeEnvironment();
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                final List<IRuntimeEnvironment> allRuntimeEnvironments = RedPlugin.getDefault()
                        .getAllRuntimeEnvironments();
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                setProperty(createKey(activeEnv), activeEnvironment);
                setProperty(createKey(allEnvs), allRuntimeEnvironments);
                return Status.OK_STATUS;
            }
        };
        envLoadingJob.addJobChangeListener(new JobChangeAdapter() {

            @SuppressWarnings("unchecked")
            @Override
            public void done(final IJobChangeEvent event) {
                final IRuntimeEnvironment env = (IRuntimeEnvironment) envLoadingJob
                        .getProperty(createKey(activeEnv));
                final List<IRuntimeEnvironment> allEnvironments = (List<IRuntimeEnvironment>) envLoadingJob
                        .getProperty(createKey(allEnvs));

                SwtThread.syncExec(() -> eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED,
                        new RedProjectConfigEventData<>(editorInput.getFile(),
                                new Environments(allEnvironments, env))));
            }
        });
        envLoadingJob.schedule();
    }

    private QualifiedName createKey(final String localName) {
        return new QualifiedName(RedPlugin.PLUGIN_ID, localName);
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        for (final IEditorPart dirtyEditor : getDirtyEditors()) {
            dirtyEditor.doSave(monitor);
        }
        final RobotProject project = editorInput.getRobotProject();
        project.clearAll();
        new RedEclipseProjectConfigWriter().writeConfiguration(editorInput.getProjectConfiguration(), project);
    }

    private List<? extends IEditorPart> getDirtyEditors() {
        final List<IEditorPart> dirtyEditors = new ArrayList<>();
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editorPart = getEditor(i);
            if (editorPart != null && editorPart.isDirty()) {
                dirtyEditors.add(editorPart);
            }
        }
        return dirtyEditors;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSaveAs() {
        // save as is not allowed
    }

    public void openPage(final Class<? extends IEditorPart> classOfPage) {
        setActivePage(getPageIndexFor(classOfPage));
    }

    private int getPageIndexFor(final Class<? extends IEditorPart> classOfPage) {
        int i = 0;
        for (final IEditorPart part : parts) {
            if (classOfPage.isInstance(part)) {
                return i;
            }
            i++;
        }
        throw new IllegalStateException("Unable to find part of class: " + classOfPage.getName());
    }

    @Override
    protected void pageChange(final int newPageIndex) {
        super.pageChange(newPageIndex);
        final IEditorPart activeEditor = getActiveEditor();
        saveActivePage(activeEditor.getClass().getSimpleName());
    }

    private void saveActivePage(final String activePageClassName) {
        if (getEditorInput() instanceof IFileEditorInput) {
            final IFileEditorInput fileInput = (IFileEditorInput) getEditorInput();
            final String projectName = fileInput.getFile().getProject().getName();

            final String sectionName = ID + ".activePage." + projectName;
            final IDialogSettings dialogSettings = RedPlugin.getDefault().getDialogSettings();
            IDialogSettings section = dialogSettings.getSection(sectionName);
            if (section == null) {
                section = dialogSettings.addNewSection(sectionName);
            }
            section.put("activePage", activePageClassName);
        }
    }

    @Override
    public void dispose() {
        final IEclipseContext parentContext = getEditorSite().getService(IEclipseContext.class);
        final IEclipseContext context = parentContext.getActiveLeaf();
        for (final IEditorPart part : parts) {
            ContextInjectionFactory.uninject(part, context);
        }

        if (resourceListener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
        }
        super.dispose();

        refreshDecorators();
    }

    private void refreshDecorators() {
        SwtThread.asyncExec(() -> {
            final IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
            manager.update(RobotValidationExcludedDecorator.ID);
        });
    }

    private static class IllegalProjectConfigurationEditorInputException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public IllegalProjectConfigurationEditorInputException(final String message) {
            super(message);
        }

        public IllegalProjectConfigurationEditorInputException(final String message, final Exception cause) {
            super(message, cause);
        }
    }

    private static class EditorInitializationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public EditorInitializationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
