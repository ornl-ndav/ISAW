What is Hawk?
-------------
    Hawk is a tool for java developers which allows a developer to visualize how a program was created.  Java programs are composed 
of many classes and interfaces.  Each of these classes and interfaces add some integral part to a program.  It may not be very difficult 
to understand what each class does when a program is only made of a few classes.  However, when a program is made of hundreds of classes, 
it is easy to get overwhelmed by the shear size of the source code or forget what each class does.  In such a case, Hawk makes 
representations and summaries of each class to allow a developer to understand the class without having to immediately dive into the 
source code.


Using Hawk
----------
    To start Hawk, issue the command java -jar xx-xx-xx_xx-xxHawk.jar from the command line.  

        Note:  You may need to specify the full path to the java command.  For instance, in UNIX you may need to use the command
       /usr/local/j2sdk<version-number>/bin/java -jar xx-xx-xx_xx-xxHawk.jar

When Hawk starts, the main window should appear centered in the screen.  

        Note:  If you are running Hawk for the first time, a window will show up  describing that the directory ".Hawk" in your home 
	directory could not be found.  Hawk will automatically create this directory for you.  This directory is just used to store 
	preferences and temporary data.

Before you can do any work in Hawk, you need to create a project to work with.  A project is just a collection of classes.  Typically, 
related classes are grouped in their own project.  Then, with Hawk, you can apply actions on each project to obtain information about 
the project.  For instance, you could have all of the classes from a program in its own project.  Then you could have Hawk list all the 
classes in the project alphabetically or by package name.  You could also get a listing of the number of classes, interfaces, inner classes, 
abstract classes, etc. in the project.  You could then print information about the project.  Projects just make it nice to group related 
classes and interfaces.
	
	
Creating A Project
------------------
    To create a project, click on "File->Create New Project".  From the window that appears, click on "Add files" to add .class or .jar 
files to the list of classes to load.  You can also choose directories.  In such a situation, Hawk will scan through the directory and all 
its subdirectory and list all of the .class and .jar files in the list of classes to load.  If there are files that you do not want to add 
to the project, select them and press "Remove selected files."  By holding down Control or Shift while selecting files, you can select 
multiple files at a time.  When you have finished selecting the files, click on "Create project" to create the project.  All of the .class 
file's information will be loaded into the project.  Also, all .jar files will be scanned and all of the .class file's information in the 
.jar file will be added to the project.
    The classes should be found and loaded into the project without having to set the classpath at all.  However, if Hawk encounters any 
problems while loading any of the classes you ask it to load, a window will pop up displaying the cause for the problem, the class that 
caused the problem, and where the class can be found.  You may need to add files or directories to your classpath to allow Hawk to find all 
the classes in a directory.  The newly created project will be added to the "Current Projects" list in the main window.


Adding More Information to the Project
--------------------------------------
    Your newly created project contains a wealth of knowledge about the classes and interfaces it loaded.  However, Hawk doesn't know where 
the source code files and javadocs files are for each class.  To tell Hawk where to find these files, click on 
"Edit->Associate Source Code Files" and "Edit->Associate Javadoc Files" respectively.  From the window that appears, click on "Browse" to 
add a file or directory to the list.  Hawk will search the list for .java files (when looking for java source code) and .html files 
(when looking for javadoc files). When you choose "Ok", Hawk will pair the name of the file with the first class or interface it finds in 
the currently selected project with the same name.  Currently, inner classes are not paired with any source code file or javadoc file.  If 
there is a directory in the list, Hawk will scan through the directory, find the appropriate file, and pair it with the appropriate class.
	
	Pending:  I am going to add buttons to allow the user the remove entries from the list.  Also, I will add a way to manually override 
	which file is paired with which class if some files are paired incorrectly.  Also, I will have a list created that shows which 
	classes could and could not have files associated with it.  
	
    Now you can view the source code and javadoc files for a class in Hawk.  I will explain how momentarily.  The Edit menu also allows you 
to modify the project in other ways.  You can change the project's name, add classes or interfaces to the project, or remove the project if 
you do not need it any more.


Acquiring Information From a Project
------------------------------------
    Once you have a project, select it in the "Current Projects" list to notify Hawk you are working with that project.  Then, the "View" 
menu can be used to view information about the project.  Note:  For ease of use, part of the view menu can be quickly accessed from a pop up 
menu by right clicking in the "Current Projects" list.  
    By choosing "View->Statistics" you can view the project's statistics.  A window will appear showing the total number of classes and 
interfaces, total number of classes, total number of interfaces, number of inner classes, number of abstract classes, etc. in the project.  
    By choosing "View->Search" you can enter conditions a class or interface must meet.  Then the classes and/or interfaces meeting these 
conditions will be listed.  Note:  The search window is HIGHLY in a beta state.  Currently, the list is only printed to the console.  Also, 
I am going to add an option that will allow the user to search for any word in the source code.  The user will also be able to select if 
he/she wants to search javadocs, comments, and/or actual code in the source code.
    The sub-menu "View->Interfaces" contains the most important options in the View menu.  You can select "View->Interfaces->Alphabetically" 
to have window pop up listing all of the classes and/or interfaces of the currently selected project alphabetically.  You can also select 
"View->Interfaces->By Package Name" to have a window pop up listing all of the packages in alphabetical order in a tree.  All of the classes 
and interfaces in each packag will also be listed in alphabetical order.  
    From the "Properties" menu, you can select to shorten Java and/or non-Java names in the list.  For example, a full class name might be 
javax.swing.JTree.  If you choose to shorten Java names, javax.swing.JTree will be displayed as JTree.  Java names are any name that start 
with java or javax.  Non-Java names are names that do not start with java or javax.  Shortening names may make it easier to read the list of 
classes and interfaces.  Both of the guis mentioned, along with the windows that appear by clicking on menus or buttons from the guis will 
appear in the currently selected tab.  More information will be given later on the use of this tab.

Viewing Other Data
------------------
    From the window listing the project's classes alphabetically or from the window listing the package names, after choosing a class or 
interface, select the "View" menu for a list of information you can view about the selected class or interface.  You can also quickly 
access the "View" menu by right clicking on the class list.  The "View" menu's options are described below.  All of the windows that 
appear from the "View" menu appear behind the other windows that already exist in the tab.

Viewing a UML Diagram
---------------------
    If you select "View->Single UML" an ASCII printout for a UML diagram will be displayed in a separate window.  As with the previous 
windows you can choose to shorten Java or non-Java name from the "Properties" menu to make the diagram easier to read.

Viewing Shortened Source Code
-----------------------------
    If you select "View->Shortened Source Code" a window will be displayed that will show the class's or interface's shortened source code.  
The shortened source code is basically displayed how the source code is displayed.  However, the field, constructor, and method declarations 
are given without their corresponding bodies.  As with the previous windows you can choose to shorten Java or non-Java name from the 
"Properties" menu to make the diagram easier to read.  Also, Java keywords are colored blue to make them stand out.

Viewing Source Code
-------------------
    If you select "View->Source Code" a window will be displayed that will show the class's or interface's source code.  If the correct 
source code file could not be found for the class or if you did not associate Java source code with the project, a blank window will appear.  Otherwise, the class's source code will be displayed.  The source code has javadoc comments, comments, keywords, and actual code in separate colors to make it easier to read.

Viewing Javadocs
----------------
    If you select "View->Javadocs" a window will be displayed showing the class's or interface's javadocs.  If the correct javadocs file 
could not be found for the class or interface or if you did not associate javadocs file with the project, a blank window will appear.  
Otherwise, the javadocs for the class or interface will be displayed.

Printing Information
--------------------
    From the main window, if you select "File->Print Project" a window will appear allowing you to configure what classes you want to print 
information about and what information to print.  All of the current projects will appear in the tree on the left side of the window.  
Select the classes you want to print information about and click add to add them to the list to the right of the window.  You can also 
select a project, click add, and all of its classes and interfaces will be added to the list.  Also a "Add All" option is given to 
conveniently add all of the classes and interfaces to the list.
    If you select the "Properties" tab you can choose which sections to have the documentation print.  Also, you can choose the "Options" 
button to customize each selection.  Next, choose browse to choose the file you want to print the documentation to.  The print gui prints 
an ASCII document describing the classes you selected to the file you select.  Then, you can look at the document and print it using another 
program.  Hawk currently doesn't print directly to printers.  Also, Hawk currently only makes ASCII documents.  However, HTML document 
support is nearly completed.

Saving A Project
----------------
    If you want to save a project just choose "File->Save As" from the main window.  Then select the file you want to save to.  If the file 
you chose does not have a jdf extension it will be given one.  Also, if the file already exists, you can select to overwrite the file or 
append the information to the file.  The jdf file that is created is in binary format.

Opening A Project
-----------------
    If you have saved a project and want to open it select "File->Open" from the main window.  A window will appear allowing you to select 
the file you want to open.  Note:  Hawk uses a jdf extension to identify files that it can read.  If you changed the name of the file, you 
need to make sure that it has a jdf extension so that Hawk can find it.  When you select the correct file, Hawk will read its contents and 
reconstruct the project.  Note:  Hawk keeps a record of where to find java source files and javadocs files if you give it this information 
(see Adding More Information to the Project).  It does not keep the entire source code and javadocs files stored.  Therefore, if you modify 
the source code and view it in Hawk, the source code you will see is the modified code.  Also, if you move the move a file you have to tell 
Hawk where to find it (see Adding More Information to the Project).

Using the Desktop
-----------------
    From the main window, the "Window" menu allows you to customize the desktop.  You can add new tabs, remove tabs, and rename tabs.  This 
is useful because if you have multiple windows open in a tab, you can choose the "Window" menu for each window in the tab to move it or copy 
it to another tab.  This allows for tab browsing.  You can have information for each class in its own tab for instance.  Note:  The moving 
and copying of windows from one tab to another is still a work in progress.

Using the Command Line Utility
------------------------------
    When starting Hawk, you can specify command line options to get information about Hawk.   Below is a list of options.  Also, supply 
-help will print out a similar list.

--version or -V  prints the version you are running
--build or -B prints when the version you are running was built
--author or -A prints the author (Dominic Kramer)
--help prints a help message

Contact kramerd@uwstout.edu for more information, to report bugs, or to make suggestions.
