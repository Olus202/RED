<html>
<head>
<link href="PLUGINS_ROOT/org.robotframework.ide.eclipse.main.plugin.doc.user/help/style.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<a href="index.html">RED - Robot Editor User Guide</a> &gt; <a href="user_guide.html">User guide</a> &gt; <a href="..\tools_integration.html">Integration with other tools</a> &gt; 
	<h2>Robot Framework Lint analysis</h2>
<p>Starting with RED 0.8.1 it is possible to run <a class="external" href="http://github.com/boakley/robotframework-lint/" target="_blank">Robot Framework Lint</a> 
	analysis tool. Of course one have to have it installed in the python installation
	used by the project.
	</p>
<h3>Running RfLint</h3>
<p>RfLint can be run on selected file or folder (or whole project) in <b>Project Explorer</b> view. In order
	to start analysis open context menu for selected resource and choose <b><code>Robot Framework -&gt; Run RfLint analysis</code></b>.
	</p>
<img src="images/rflint_run.png"/>
<p>The analysis should start and its progress is visible in <b>Progress</b> view. At any time you can abort running
	validation:
	</p>
<img src="images/rflint_progress.png"/>
<p>Analysis performed from RED is reporting all the findings as <b>Problem markers</b> of a separate type called
	<b>RfLint Problem</b>. This is a different type than those reported by standard RED <a href="../validation.html">
	validation</a> mechanism (they have <b>Robot Problem</b> type). Overall this means that the findings are visible
	in <b>Problems</b> view and are also visible in editors. 
	</p>
<img src="images/rflint_problems.png"/>
<p>In order to remove problems simply choose <b><code>Robot Framework -&gt; Clean RfLint problems</code></b> from context menu
	of selected resource.
	</p>
<dl class="note">
<dt>Note</dt>
<dd>Robot Framework Lint analysis is not run on excluded project parts (see more under topic <a href="../validation/scope.html">Limiting validation scope</a>.
	   </dd>
</dl>
<h3>Configuration</h3>
<p>It is possible to configure RfLint behavior in Preferences (
		<code><a class="command" href="javascript:executeCommand('org.eclipse.ui.window.preferences(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.rflint)')">
		Window -&gt; Preferences -&gt; Robot Framework -&gt; Errors/Warnings -&gt; RfLint validation</a></code>)
	</p>
<img src="images/rflint_prefs.png"/>
<ul>
<li>rule <b>severity</b> - rule of a given name can have severity specified: <b>default</b> means severity is 
		unchanged, <b>Error</b> and <b>Warning</b> changes severity to one of those levels, while <b>Ignore</b> will
		silence the rule,
		<p></p>
</li>
<li>rule <b>configuration</b> - as described in <a class="external" href="http://github.com/boakley/robotframework-lint/wiki/How-to-write-custom-rules" target="_blank">RfLint Wiki</a>
		some rules can be configured. This can be done on preference page. Simply write arguments for given rule. 
		If there are multiple arguments then they should be separated with colon (:) character,
		<p></p>
</li>
<li><b>additional rules files</b> - add python files containing custom RfLint rules so that they will 
		be also used for analysis.
		<p></p>
</li>
<li><b>additional arguments</b> - custom RfLint arguments that will be used for analysis
		<p></p>
</li>
</ul>
<dl class="note">
<dt>Note</dt>
<dd>Additional arguments field accepts Eclipse <a href="../launching/string_substitution.html">string variables</a>.</dd>
</dl>
</body>
</html>