/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.StructuredContentProvider;

class ReferencedLibrariesContentProvider extends StructuredContentProvider {

    @Override
    public Object[] getElements(final Object inputElement) {
        final List<Object> elements = new ArrayList<>();
        elements.addAll((List<?>) inputElement);
        elements.add(new ElementAddingToken("library file", true));

        return elements.toArray();
    }

}
