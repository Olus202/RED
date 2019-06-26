[RED - Robot Editor User Guide](../../index.md) > [User
guide](../user_guide.md) > [Validation](../validation.md) >

## Validation - setting issues severity levels

### General information

Validation findings levels can be customized in Preferences (`[ Window ->
Preferences -> Robot Framework ->
Errors/Warnings](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.validation\)'\))`).  
Each issue can be reported as error/warning/info/ignore. In case of setting
ignore level, such validation issue type will not be visible in Problems view.

Note

    There are RED specific validation types grouped under Project configuration category, which are essential for RED and can have Fatal severity level. Such problems are reported even when validation is turned off, and building as well as validation is cancelled then. 

### Validation related Error/Warnings types with examples

#### Code style

  * **Keyword from nested library** \- occurs when keyword imported by dependency is used in test suite.
  * **Keyword occurrence not consistent with definition** \- occurs when name in keyword call is different than in definition. Use name the same as in definition.
  * **Keyword name with dots** \- occurs when keyword name contains dots. It may be confused with fully qualified name.
  * **Variable given as keyword name** \- occurs when variable is used as keyword call in test suite setup or teardown.
  * **Collection size should be equal to keyword arguments number** \- occurs when collection variable is used in keyword call and collection elements number is different than keyword arguments number.
  * **Invalid time format** \- occurs when time is not formatted correctly. Use number, time string or timer string.
  * **Variable declared without assignment** \- occurs when variable is declared without assignment in Variables section.

#### Name shadowing and conflicts

  * **Duplicated variable name** \- occurs when variable name is duplicated and one variable value overrides another.
  * **Duplicated test case name** \- occurs when test case name is duplicated and both test cases can be run
  * **Masked keyword name** \- occurs when keyword defined in test suite has the same name like keyword from imported library. You can use fully qualified name when calling masked keyword.

#### Unnecessary code

  * **Empty settings definition** \- occurs when suite, test case or keyword setting is defined with empty content.
  * **Unrecognized header type** \- occurs when Robot Framework does not recognize section header. Only ***Settings***, ***Variables***, ***Test Cases*** or ***Keywords*** sections are valid.
  * **Duplicated configuration path** \- occurs when path defined in configuration is subpath of different one. Such path is skipped.
  * **Missing configuration path** \- occurs when missing path is defined in configuration. Such path is skipped.

#### Import

  * **Absolute path is used** \- occurs when absolute path is used. Workspace-relative paths are preferred in RED.
  * **Unsupported resource import used** \- occurs when imported file is in HTML format or is outside of workspace. Red will not parse such files, so keywords and variables defined inside will not be accessible. Use supported formats from workspace only.
  * **Import path relative via modules path** \- occurs when imported path is relative to python path.
  * **Import path outside of workspace** \- occurs when imported path points to location not from workspace.
  * **Import Remote library without arguments** \- occurs when Remote library is imported without arguments.

#### Robot version

  * **Removed Robot Framework API used** \- occurs when syntax from older Robot Framework version is not available in current version.
  * **Unsupported Robot Framework API used** \- occurs when syntax from newer Robot Framework version is not available in older version.
  * **Deprecated Robot Framework API used** \- occurs when deprecated syntax is used. Use current Robot Framework syntax instead.
  * **Duplicated definitions used** \- occurs when testcase or keywords definitions names are not unique. Refers to syntax in pre Robot Framework 2.9.0.
  * **Incorrect variable initialization** \- occurs when there is syntax error in variable initialization. Refers to syntax in pre Robot Framework 2.9.0.

### Runtime and Building related Error/Warnings types with examples

#### Runtime

  * **RED parser warning** \- occurs when for some reason RED parser reports warning.
  * **Robot Framework runtime error** \- occurs when incorrect Robot Framework syntax is issued. Such syntax will fail test in runtime.

#### Project configuration

  * **Project configuration file (red.xml) cannot be read** \- occurs when project has no red.xml configuration file or it cannot be read.
  * **Python Robot Framework environment missing** \- occurs when there is no Robot Environment defined. Python main directory with Robot modules installed should be defined in preferences. Project may override this setting in its configuration file.
  * **Library documentation file cannot be generated** \- occurs when for some reason Robot framework is unable to generate library specification file, probably due to missing library dependencies or errors in library source code.

