## RED Robot Editor User Guide

### Contents

  * [About this Guide](https://olus202.github.io/RED/help/about.md)
  * [Legal](http://nokia.github.io/RED/help/legal.md)
  * [RED key shortcuts cheatsheet](https://olus202.github.io/RED/help/keys.md)
  * [First steps with RED](http://nokia.github.io/RED/help/first_steps/first_steps.md)
    * [Download and install](http://nokia.github.io/RED/help/first_steps/download_install.md)
    * [Eclipse principles](http://nokia.github.io/RED/help/first_steps/eclipse_principles.md)
    * [Setting up environment](http://nokia.github.io/RED/help/first_steps/setting_up_environment.md)
    * [Create project, add test and run](http://nokia.github.io/RED/help/first_steps/create_run.md)
    * [Preferences and red.xml](http://nokia.github.io/RED/help/first_steps/preferences_misc.md)
  * [User guide](http://nokia.github.io/RED/help/user_guide/user_guide.md)
    * [Quick start with RED - Robot Editor](http://nokia.github.io/RED/help/user_guide/quick_start.md)
    * [General usage hints](http://nokia.github.io/RED/help/user_guide/general.md)
    * [Working with RED](http://nokia.github.io/RED/help/user_guide/working_with_RED.md)
      * [Table Editors - general usage hints](http://nokia.github.io/RED/help/user_guide/working_with_RED/table_general.md)
      * [Variable typing in editors](http://nokia.github.io/RED/help/user_guide/working_with_RED/variable_typing.md)
      * [Recognizing external libraries in RED](http://nokia.github.io/RED/help/user_guide/working_with_RED/libs.md)
      * [Variable mapping - dealing with parameterized paths to libraries and resources](http://nokia.github.io/RED/help/user_guide/working_with_RED/variable_mapping.md)
      * [Variable Files - using files with variable accessible anywhere inside Project](http://nokia.github.io/RED/help/user_guide/working_with_RED/variable_files.md)
      * [Custom python/class paths and path relativeness](http://nokia.github.io/RED/help/user_guide/working_with_RED/custom_paths_relatve.md)
      * [Importing files and projects to workspace](http://nokia.github.io/RED/help/user_guide/working_with_RED/importing.md)
      * [Remote library](http://nokia.github.io/RED/help/user_guide/working_with_RED/remote_library.md)
      * [Red.xml description](http://nokia.github.io/RED/help/user_guide/working_with_RED/red_xml.md)
      * [Content assistance](http://nokia.github.io/RED/help/user_guide/working_with_RED/content_assist.md)
      * [Dark theme in RED](http://nokia.github.io/RED/help/user_guide/working_with_RED/dark_theme.md)
    * [Validation](http://nokia.github.io/RED/help/user_guide/validation.md)
      * [Limiting validation scope](http://nokia.github.io/RED/help/user_guide/validation/scope.md)
      * [Configuring problems severity](http://nokia.github.io/RED/help/user_guide/validation/validation_preferences.md)
      * [Running validation in command line](http://nokia.github.io/RED/help/user_guide/validation/headless.md)
      * [Tasks/TODO](http://nokia.github.io/RED/help/user_guide/validation/tasks.md)
    * [Launching Tests](http://nokia.github.io/RED/help/user_guide/launching.md)
      * [User interface](http://nokia.github.io/RED/help/user_guide/launching/ui_elements.md)
      * [Local launches](http://nokia.github.io/RED/help/user_guide/launching/local_launch.md)
      * [Local launches scripting](http://nokia.github.io/RED/help/user_guide/launching/local_launch_scripting.md)
      * [Remote launches](http://nokia.github.io/RED/help/user_guide/launching/remote_launch.md)
      * [Parameterizing launches](http://nokia.github.io/RED/help/user_guide/launching/string_substitution.md)
      * [Controlling execution](http://nokia.github.io/RED/help/user_guide/launching/exec_control.md)
      * [Debugging Robot](http://nokia.github.io/RED/help/user_guide/launching/debug.md)
        * [User interface](http://nokia.github.io/RED/help/user_guide/launching/debug/ui_elements.md)
        * [Breakpoints](http://nokia.github.io/RED/help/user_guide/launching/debug/breakpoints.md)
        * [Hitting a breakpoint](http://nokia.github.io/RED/help/user_guide/launching/debug/hitting_a_breakpoint.md)
        * [Debugger preferences](http://nokia.github.io/RED/help/user_guide/launching/debug/preferences.md)
        * [Debugging Robot & Python with RED & PyDev](http://nokia.github.io/RED/help/user_guide/launching/debug/robot_python_debug.md)
      * [Launching preferences](http://nokia.github.io/RED/help/user_guide/launching/launch_prefs.md)
      * [RED Tests Runner Agent](http://nokia.github.io/RED/help/user_guide/launching/red_agent.md)
    * [Integration with other tools](http://nokia.github.io/RED/help/user_guide/tools_integration.md)
      * [Mounting folders via SSH](http://nokia.github.io/RED/help/user_guide/tools_integration/virtual_folders.md)
      * [PyDev and PyLint](http://nokia.github.io/RED/help/user_guide/tools_integration/red_pylint.md)
      * [Robot Framework Maven plugin](http://nokia.github.io/RED/help/user_guide/tools_integration/maven.md)
      * [Running tests using Gradle](http://nokia.github.io/RED/help/user_guide/tools_integration/gradle.md)
      * [Robot Framework Lint](http://nokia.github.io/RED/help/user_guide/tools_integration/rflint.md)
    * [Known issues and problems](http://nokia.github.io/RED/help/user_guide/known_issues.md)
    * [Diagnostics](http://nokia.github.io/RED/help/user_guide/diagnostics.md)

### About

RED is modern editor based on Eclipse IDE to allow quick and comfortable work
with Robot testware.

### What RED provides:

  * text editor with validation and code coloring
  * table editors like in Ride fully synced with source
  * debug&remote debug with breakpoints, testcase stepping (step into, step over), runtime variable lookup & modification
  * code assistance & completion for variables, keywords, testcases, resources and libraries
  * real time testcase validation
  * execution view
  * support for plugins via Eclipse mechanisms
  * support for Robot formats: .txt, .robot, .tsv (HTML format is not supported)

### Look & feel

![](images/basic_run.gif)

