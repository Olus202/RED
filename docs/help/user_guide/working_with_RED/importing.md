<html>
<head>
<link href="PLUGINS_ROOT/org.robotframework.ide.eclipse.main.plugin.doc.user/help/style.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<a href="RED/../../../../help/index.html">RED - Robot Editor User Guide</a> &gt; <a href="RED/../../../../help/user_guide/user_guide.html">User guide</a> &gt; <a href="RED/../../../../help/user_guide/working_with_RED.html">Working with RED</a> &gt; 
<h2>Different ways of importing files to project and workspace</h2>
Eclipse provides different ways of importing files to user workspace, some of the possibilities can lead to problems with RED.<br/>
Here are the possibilities how to include files to workspace from most recommended to least:<br/>
<h3>1. Create project and import existing files</h3>
<img src="images/import_1.png"/><br/><br/>
Such import action will <b>copy selected files to project folder under workspace path. Note that</b> under Advanced options are unchecked.
<br/><br/><img src="images/import_2.png"/>
<h3>2. Import existing project into workspace</h3>
<img src="images/import_4.png"/><br/><br/>
Selected project will be <b>copied to workspace.</b>
<br/><br/><img src="images/import_3.png"/><br/>
<h3>3. Link project to workspace</h3>
<img src="images/import_4.png"/><br/><br/>
Unchecked option will link project to workspace thus project will be stored in existing place. <b>Note that</b> having project outside of workspace might cause issues with paths to libraries and resources. Use this option with caution.
<br/><br/><img src="images/import_5.png"/><br/>
<h3>4. Link files to project </h3>
<img src="images/import_1.png"/><br/><br/>
Linking files to project is the least recommended option.
<br/><br/><img src="images/import_6.png"/><br/>
</body>
</html>