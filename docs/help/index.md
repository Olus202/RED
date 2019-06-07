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

<h2>RED Robot Editor User Guide</h2>
<h3>Contents</h3>
<ul>
<li><a href="https://olus202.github.io/RED/help/about.md">About this Guide</a></li>
<li><a href="http://nokia.github.io/RED/help/legal.md">Legal</a></li>
<li><a href="https://olus202.github.io/RED/help/keys.md">RED key shortcuts cheatsheet</a></li>
<li><a href="http://nokia.github.io/RED/help/first_steps/first_steps.md">First steps with RED</a>
<ul>
<li><a href="http://nokia.github.io/RED/help/first_steps/download_install.md">Download and install</a></li>
<li><a href="http://nokia.github.io/RED/help/first_steps/eclipse_principles.md">Eclipse principles</a></li>
<li><a href="http://nokia.github.io/RED/help/first_steps/setting_up_environment.md">Setting up environment</a></li>
<li><a href="http://nokia.github.io/RED/help/first_steps/create_run.md">Create project, add test and run</a></li>
<li><a href="http://nokia.github.io/RED/help/first_steps/preferences_misc.md">Preferences and red.xml</a></li>
</ul>
</li>
<li><a href="http://nokia.github.io/RED/help/user_guide/user_guide.md">User guide</a>
<ul>
<li><a href="http://nokia.github.io/RED/help/user_guide/quick_start.md">Quick start with RED - Robot Editor</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/general.md">General usage hints</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/working_with_RED.md">Working with RED</a>
<ul>
<li><a href="http://nokia.github.io/RED/help/user_guide/working_with_RED/table_general.md">Table Editors - general usage hints</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/working_with_RED/variable_typing.md">Variable typing in editors</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/working_with_RED/libs.md">Recognizing external libraries in RED</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/working_with_RED/variable_mapping.md">Variable mapping - dealing with parameterized paths to libraries and resources</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/working_with_RED/variable_files.md">Variable Files - using files with variable accessible anywhere inside Project</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/working_with_RED/custom_paths_relatve.md">Custom python/class paths and path relativeness</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/working_with_RED/importing.md">Importing files and projects to workspace</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/working_with_RED/remote_library.md">Remote library</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/working_with_RED/red_xml.md">Red.xml description</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/working_with_RED/content_assist.md">Content assistance</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/working_with_RED/dark_theme.md">Dark theme in RED</a></li>
</ul>
</li>
<li><a href="http://nokia.github.io/RED/help/user_guide/validation.md">Validation</a>
<ul>
<li><a href="http://nokia.github.io/RED/help/user_guide/validation/scope.md">Limiting validation scope</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/validation/validation_preferences.md">Configuring problems severity</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/validation/headless.md">Running validation in command line</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/validation/tasks.md">Tasks/TODO</a></li>
</ul>
</li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching.md">Launching Tests</a>
<ul>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/ui_elements.md">User interface</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/local_launch.md">Local launches</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/local_launch_scripting.md">Local launches scripting</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/remote_launch.md">Remote launches</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/string_substitution.md">Parameterizing launches</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/exec_control.md">Controlling execution</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/debug.md">Debugging Robot</a>
<ul>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/debug/ui_elements.md">User interface</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/debug/breakpoints.md">Breakpoints</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/debug/hitting_a_breakpoint.md">Hitting a breakpoint</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/debug/preferences.md">Debugger preferences</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/debug/robot_python_debug.md">Debugging Robot &amp; Python with RED &amp; PyDev</a></li>
</ul>
</li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/launch_prefs.md">Launching preferences</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/launching/red_agent.md">RED Tests Runner Agent</a></li>
</ul>
</li>
<li><a href="http://nokia.github.io/RED/help/user_guide/tools_integration.md">Integration with other tools</a>
<ul>
<li><a href="http://nokia.github.io/RED/help/user_guide/tools_integration/virtual_folders.md">Mounting folders via SSH</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/tools_integration/red_pylint.md">PyDev and PyLint</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/tools_integration/maven.md">Robot Framework Maven plugin</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/tools_integration/gradle.md">Running tests using Gradle</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/tools_integration/rflint.md">Robot Framework Lint</a></li>
</ul>
</li>
<li><a href="http://nokia.github.io/RED/help/user_guide/known_issues.md">Known issues and problems</a></li>
<li><a href="http://nokia.github.io/RED/help/user_guide/diagnostics.md">Diagnostics</a></li>
</ul>
</li>
</ul>
<h3>About</h3>
<p>RED is modern editor based on Eclipse IDE to allow quick and comfortable work
with Robot testware.</p>
<h3>What RED provides:</h3>
<ul>
<li>text editor with validation and code coloring</li>
<li>table editors like in Ride fully synced with source</li>
<li>debug&amp;remote debug with breakpoints, testcase stepping (step into, step over), runtime variable lookup &amp; modification</li>
<li>code assistance &amp; completion for variables, keywords, testcases, resources and libraries</li>
<li>real time testcase validation</li>
<li>execution view</li>
<li>support for plugins via Eclipse mechanisms</li>
<li>support for Robot formats: .txt, .robot, .tsv (HTML format is not supported)</li>
</ul>
<h3>Look &amp; feel</h3>
<p><img src="images/basic_run.gif" alt="" /></p>
