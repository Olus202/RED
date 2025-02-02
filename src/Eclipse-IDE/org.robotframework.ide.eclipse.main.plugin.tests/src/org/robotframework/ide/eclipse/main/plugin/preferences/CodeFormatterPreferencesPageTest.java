/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.jface.preferences.ComboBoxFieldEditor;
import org.robotframework.red.junit.ShellProvider;

public class CodeFormatterPreferencesPageTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final CodeFormatterPreferencePage page = new CodeFormatterPreferencePage();
        page.init(workbench);

        verifyZeroInteractions(workbench);
    }

    @Test
    public void editorsForCodeFormatterPreferencesAreDefined_byDefault() throws Exception {
        final CodeFormatterPreferencePage page = new CodeFormatterPreferencePage();
        page.createControl(shellProvider.getShell());

        final List<FieldEditor> editors = FieldEditorPreferencePageHelper.getEditors(page);
        assertThat(editors).hasSize(4);

        final Map<Class<?>, List<String>> namesGroupedByType = editors.stream()
                .collect(groupingBy(FieldEditor::getClass, mapping(FieldEditor::getPreferenceName, toList())));
        assertThat(namesGroupedByType).hasEntrySatisfying(BooleanFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED,
                        RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED));
        assertThat(namesGroupedByType).hasEntrySatisfying(ComboBoxFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.FORMATTER_SEPARATOR_TYPE));
        assertThat(namesGroupedByType).hasEntrySatisfying(IntegerFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.FORMATTER_SEPARATOR_LENGTH));
    }
}
