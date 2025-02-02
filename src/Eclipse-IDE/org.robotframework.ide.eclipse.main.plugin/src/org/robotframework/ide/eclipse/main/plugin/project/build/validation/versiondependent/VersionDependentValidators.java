/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;

public class VersionDependentValidators {

    private final FileValidationContext validationContext;

    private final ValidationReportingStrategy reporter;

    public VersionDependentValidators(final FileValidationContext validationContext,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.reporter = reporter;
    }

    public Stream<VersionDependentModelUnitValidator> getTestSuiteFileValidators(final RobotSuiteFile fileModel) {
        final IFile file = validationContext.getFile();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream
                .of(new SuiteFileExtensionValidator(file, fileModel, RobotCasesSection.class, reporter));
        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getTaskSuiteFileValidators(final RobotSuiteFile fileModel) {
        final IFile file = validationContext.getFile();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream
                .of(new SuiteFileExtensionValidator(file, fileModel, RobotTasksSection.class, reporter));
        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getGeneralSettingsTableValidators(final SettingTable table) {
        final IFile file = validationContext.getFile();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new DeprecatedGeneralSettingsTableHeaderValidator(file, table, reporter),

                new SettingsDuplicationInOldRfValidator<>(file, table::getTestTemplates, reporter),
                new SettingsDuplicationValidator<>(file, table::getTestTemplates, reporter, ". No template will be used"),

                new SettingsDuplicationInOldRfValidator<>(file, table::getSuiteSetups, reporter),
                new SettingsDuplicationValidator<>(file, table::getSuiteSetups, reporter, ". No Suite Setup will be executed"),

                new SettingsDuplicationInOldRfValidator<>(file, table::getSuiteTeardowns, reporter),
                new SettingsDuplicationValidator<>(file, table::getSuiteTeardowns, reporter, ". No Suite Teardown will be executed"),

                new SettingsDuplicationInOldRfValidator<>(file, table::getTestSetups, reporter),
                new SettingsDuplicationValidator<>(file, table::getTestSetups, reporter, ". No Test Setup will be executed"),

                new SettingsDuplicationInOldRfValidator<>(file, table::getTestTeardowns, reporter),
                new SettingsDuplicationValidator<>(file, table::getTestTeardowns, reporter, ". No Test Teardown will be executed"),

                new SettingsDuplicationInOldRfValidator<>(file, table::getTestTimeouts, reporter),
                new SettingsDuplicationValidator<>(file, table::getTestTimeouts, reporter, ". No timeout will be checked"),

                new SettingsDuplicationInOldRfValidator<>(file, table::getForceTags, reporter),
                new SettingsDuplicationValidator<>(file, table::getForceTags, reporter),

                new SettingsDuplicationInOldRfValidator<>(file, table::getDefaultTags, reporter),
                new SettingsDuplicationValidator<>(file, table::getDefaultTags, reporter),

                new SettingsDuplicationInOldRfValidator<>(file, table::getDocumentation, reporter),
                new SettingsDuplicationValidator<>(file, table::getDocumentation, reporter),

                new DeprecatedGeneralSettingNameValidator(file, table, reporter),
                new MetadataKeyInColumnOfSettingValidatorUntilRF30(file, table, reporter),
                new TimeoutMessageValidator<>(file, table::getTestTimeouts, TestTimeout::getMessageArguments, reporter),
                new LibraryAliasNotInUpperCaseValidator(file, table, reporter),
                new LibraryAliasNotInUpperCaseValidator31(file, table, reporter));

        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getKeywordTableValidators(final KeywordTable table) {
        final IFile file = validationContext.getFile();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new DeprecatedKeywordTableHeaderValidator(file, table, reporter));

        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getTestCaseSettingsValidators(final TestCase testCase) {
        final IFile file = validationContext.getFile();
        final List<RobotLine> fileContent = testCase.getParent().getParent().getFileContent();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new LocalSettingsDuplicationInOldRfValidator(file, fileContent, testCase.getBeginPosition(),
                        testCase.getEndPosition(), RobotTokenType.TEST_CASE_SETTING_NAME_DUPLICATION, reporter),
                new SettingsDuplicationValidator<>(file, testCase::getSetups, reporter, ". No Setup will be executed"),
                new SettingsDuplicationValidator<>(file, testCase::getTeardowns, reporter,
                        ". No Teardown will be executed"),
                new SettingsDuplicationValidator<>(file, testCase::getTemplates, reporter,
                        ". No template will be used"),
                new SettingsDuplicationValidator<>(file, testCase::getTimeouts, reporter,
                        ". No timeout will be checked"),
                new SettingsDuplicationValidator<>(file, testCase::getTags, reporter),
                new SettingsDuplicationValidator<>(file, testCase::getDocumentation, reporter),
                new DeprecatedTestCaseSettingNameValidator(file, testCase, reporter),
                new TimeoutMessageValidator<>(file, testCase::getTimeouts,
                        timeout -> timeout.tokensOf(RobotTokenType.TEST_CASE_SETTING_TIMEOUT_MESSAGE).collect(toList()),
                        reporter));
        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getTaskSettingsValidators(final Task task) {
        final IFile file = validationContext.getFile();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new SettingsDuplicationValidator<>(file, task::getSetups, reporter, ". No Setup will be executed"),
                new SettingsDuplicationValidator<>(file, task::getTeardowns, reporter,
                        ". No Teardown will be executed"),
                new SettingsDuplicationValidator<>(file, task::getTemplates, reporter, ". No template will be used"),
                new SettingsDuplicationValidator<>(file, task::getTimeouts, reporter, ". No timeout will be checked"),
                new SettingsDuplicationValidator<>(file, task::getTags, reporter),
                new SettingsDuplicationValidator<>(file, task::getDocumentation, reporter),
                new TimeoutMessageValidator<>(file, task::getTimeouts,
                        timeout -> timeout.tokensOf(RobotTokenType.TASK_SETTING_TIMEOUT_MESSAGE).collect(toList()),
                        reporter));

        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getKeywordSettingsValidators(final UserKeyword keyword) {
        final IFile file = validationContext.getFile();
        final List<RobotLine> fileContent = keyword.getParent().getParent().getFileContent();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream.of(
                new LocalSettingsDuplicationInOldRfValidator(file, fileContent, keyword.getBeginPosition(),
                        keyword.getEndPosition(), RobotTokenType.KEYWORD_SETTING_NAME_DUPLICATION, reporter),
                new SettingsDuplicationValidator<>(file, keyword::getArguments, reporter),
                new SettingsDuplicationValidator<>(file, keyword::getTeardowns, reporter,
                        ". No Teardown will be executed"),
                new SettingsDuplicationValidator<>(file, keyword::getReturns, reporter),
                new SettingsDuplicationValidator<>(file, keyword::getTimeouts, reporter,
                        ". No timeout will be checked"),
                new SettingsDuplicationValidator<>(file, keyword::getTags, reporter),
                new SettingsDuplicationValidator<>(file, keyword::getDocumentation, reporter),
                new DeprecatedKeywordSettingNameValidator(file, keyword, reporter),
                new TimeoutMessageValidator<>(file, keyword::getTimeouts,
                        timeout -> timeout.tokensOf(RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE).collect(toList()),
                        reporter));

        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }

    public Stream<VersionDependentModelUnitValidator> getForLoopValidators(
            final ForLoopDeclarationRowDescriptor<?> descriptor) {
        final IFile file = validationContext.getFile();
        final Stream<VersionDependentModelUnitValidator> allValidators = Stream
                .<VersionDependentModelUnitValidator> of(new ForLoopInExpressionsValidator(file, descriptor, reporter));

        return allValidators.filter(validator -> validator.isApplicableFor(validationContext.getVersion()));
    }
}
