[RED - Robot Editor User Guide](..\\..\\index.md) > [User
guide](..\\user_guide.md) > [Working with RED](..\\working_with_red.md) >

## Table Editors - general usage hints

### Jump to Source, View in Table Editor

Clicking on element in Source and pressing F4 key will open respectful Table
editor.  
The same work other way around - element from Table editor is shown in Source.

### Undo - CTRL+Z actions in Table Editors

Those table actions groups are independent from each other which means that
CTRL+Z reverts only in active table editor even when there were other actions
performed in another table editor in between.  
Each table editor stores theirs list of performed actions which can be
reverted by CTRL+Z.  
Note that due to table to source synchronization, revertible actions are
discarded when switching to Source editor.

### Changing default type in Add new Variable

Type of new variable in Variable Editor can be controlled by small green arrow
next to "...add new xxx":  
  
![](images/add_new_var.png)  
  
Scalar type is displayed as default on add action element. Other types are:
list and dictionary.  

### Table preferences

All table related preferences (cell text folding, number of columns, default
behaviors) can be configured at `[ Window -> Preferences -> Robot Framework ->
Editor](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.editor\)'\))`
in **Tables** section.  
  
![](images/table_preferences.png)  
  

### Default number of columns in Test Cases/Keywords editors

To make Table editors tidy, RED creates predefined numbers of columns.  

### Enter key - what to do after Enter key press during cell edit

Enter key type can be behave in two ways in RED while editing cell in any of
Table editors.  
By default, hitting Enter will end cell edit and move cursor to next cell to
the right. If the cell is the last in row (for instance in comment cell),
Enter will move cursor to new row.  
Additionally it can be configured that Enter will finish cell edit and cursor
will stay on current cell.  
**Hint:** by pressing Shift+Enter, cursor will move backwards.

