/* Copyright (C) 1998 Graham Kirby
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Library General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Library General Public License for more details.
 *
 * To receive a copy of the GNU Library General Public License, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,
 * USA.
 *
 * Original log:
 * Header: /user/rsch/graham/java/packages/compiler/version1.2/RCS/DynamicCompiler.java,v 1.3 1998/11/06 10:37:03 graham Exp
 *
 * Revision 1.3  1998/11/06 10:37:03  graham
 * Added support for multiple packages and inner classes.
 *
 * Revision 1.2  1998/07/17 09:00:00  graham
 * Supports compiling to files, bytes or classes.
 *
 * Revision 1.1  1998/01/07 10:00:00  graham
 * Initial revision
 *
 * $Log$
 * Revision 1.2  2004/02/04 18:58:03  dennis
 * Fixed error in spelling a parameter name in a javadoc comment.
 *
 * Revision 1.1  2004/01/28 21:57:56  bouzekc
 * Added to CVS.  Changed package to ExtTools.compiler, moved original log,
 * changed at symbol in email in author tag to "_at_".  Formatted code.
 * Changed directory for javac to the bin/ directory rather than jre/bin/.
 * Commented out code that created a special compilation directory-code is
 * now compiled directly under the ISAW tree, based on package information.
 *
 */
package ExtTools.compiler;

import java.io.*;

import java.lang.reflect.*;

import java.util.*;


/**
 * Provides support for dynamic access to the Java compiler by a running Java
 * program. The class provides various combinations of input and output to the
 * compiler: the input can be a single class definition or an array of
 * definitions, and the output can be a single class, an array of classes, an
 * array of files, or an array of byte codes.
 * 
 * <P>
 * Optionally the names of the classes being compiled can be extracted
 * automatically from the class definitions: this is done using very
 * simplistic parsing, which will fail if the reserved word 'package' or
 * 'class' occurs in a comment before its first use as a reserved word.
 * </p>
 * 
 * <P>
 * Two alternative mechanisms may be used to access the compiler:
 * 
 * <UL>
 * <li>
 * try to load the package <CODE>sun.tools.javac</CODE> to invoke the compiler
 * class directly
 * </li>
 * <li>
 * try to start a host shell process to call the compiler's command line
 * interface
 * </li>
 * </ul>
 * 
 * By default the first is tried initially, followed by the second if that
 * fails. The mechanisms may be selected independently by setting the flags <A
 * HREF="#useCompilerClass">useCompilerClass</A> and <A
 * HREF="#useShell">useShell</A>. If neither mechanism works, as will be the
 * case on some platforms, the compilation methods throw a <A
 * HREF="CompilationException.html">CompilationException</A>.
 * </p>
 * 
 * <P>
 * The classpath is represented by the vector of file system directories <A
 * HREF="#classPathVector">classPathVector</A>. Successive compilations by the
 * same <A HREF="DynamicCompiler.html">DynamicCompiler</A> instance add the
 * directories containing the generated class files to this vector. Classes
 * being compiled can thus refer to other classes compiled previously by the
 * same instance. It is also possible to add existing classes to the classpath
 * by explicitly adding directories to <A
 * HREF="#classPathVector">classPathVector</A>.
 * </p>
 * 
 * <P>
 * Main changes from version 1.1:
 * 
 * <UL>
 * <li>
 * restriction on multiple packages removed, now supports compilation of
 * multiple mutually recursive packages
 * </li>
 * <li>
 * now thread-safe
 * </li>
 * <li>
 * now correctly returns subsidiary classes and inner classes
 * </li>
 * <li>
 * added option to specify main classes and classes to be returned separately
 * </li>
 * <li>
 * temporary source and class files now deleted on finalization of the compiler
 * object, unless overridden with <A
 * HREF="#deleteFilesOnFinalize">deleteFilesOnFinalize</A>
 * </li>
 * <li>
 * can now read the classpath and add existing classes to it as described above
 * </li>
 * </ul>
 * 
 * The <A HREF="../../compiler/DynamicCompiler.java">source code</A> is
 * available.
 * </p>
 *
 * @author Graham Kirby (<A
 *         HREF="mailto:graham@dcs.st-and.ac.uk">graham_at_dcs.st-and.ac.uk</A>)
 * @version 1.2 2-Nov-98
 */
public class DynamicCompiler {
  //~ Static fields/initializers ***********************************************

  /** Count of compilations performed since class was loaded. */
  private static int count = 0;

  //~ Instance fields **********************************************************

  /**
   * Hashtable storing string properties with keys as specified for <A
   * HREF="#setProperty">setProperty</A>.
   */
  private Hashtable properties = null;

  /** Object that maps full class names to corresponding byte arrays. */
  private ClassByteFileLoader classByteFileLoader = null;

  /** Parameters for the compiler class constructor. */
  private Object[] compilerConstructorParams = null;

  /** The compiler constructor. */
  private Constructor compilerConstructor = null;

  /** Types of parameters to the compile method. */
  private Class[] compileParamTypes = null;

  /** The compile method. */
  private Method compileMethod = null;

  /** Records whether availability of the compiler class has been tested. */
  private boolean testedCompilerClass = false;

  /** Records whether availability of shell commands has been tested. */
  private boolean testedShell = false;

  /** Records availability of the compiler class. */
  private boolean compilerClassAvailable = false;

  /** Records availability of shell commands. */
  private boolean shellAvailable = false;
  /************************************************************************************************************/

  /**
   * Vector storing directories used for successive compilations. Existing
   * class file directories can be added to the classpath by inserting them in
   * the vector. Index 0 in the vector represents the front of the classpath.
   */
  public Vector classPathVector;

  /**
   * Output stream used for compilation error messages. The default is a local
   * output stream that does not write to the standard output.
   */
  public OutputStream outputStream;

  /**
   * Flag specifying whether compilation via direct class invocation should be
   * attempted. The default is <B>true</B>.
   */
  public boolean useCompilerClass = true;

  /**
   * Flag specifying whether compilation via shell invocation should be
   * attempted. The default is <B>true</B>, though direct class invocation is
   * attempted first if enabled.
   */
  public boolean useShell = true;

  /**
   * Flag indicating whether temporary files should be deleted when the
   * compiler is garbage collected. The default is <B>true</B>. If set to
   * <B>true</B>, it is recommended that the calling program also call
   * <CODE><A
   * HREF="http://java.sun.com/products/jdk/1.1/docs/api/java.lang.System.html#runFinalizersOnExit(boolean)">System.runFinalizersOnExit</A>
   * (true)</CODE> to force the system to run all finalizers before exiting.
   * 
   * <P></p>
   */
  public boolean deleteFilesOnFinalize = true;

  //~ Constructors *************************************************************

  /************************************************************************************************************/
  public DynamicCompiler(  ) {
    // Initialise data structures.
    classPathVector       = new Vector(  );
    properties            = new Hashtable(  );
    classByteFileLoader   = new ClassByteFileLoader( 
        classPathVector, properties );
    outputStream          = new ByteArrayOutputStream(  );

    // Read system dependent properties from the environment.
    setProperty( "userDirPath", System.getProperty( "user.dir" ) );
    setProperty( "javaClassPath", System.getProperty( "java.class.path" ) );
    setProperty( "javaHomePath", System.getProperty( "java.home" ) );
    setProperty( "fileSeparator", System.getProperty( "file.separator" ) );
    setProperty( "pathSeparator", System.getProperty( "path.separator" ) );

    // Other default properties.
    setProperty( "tempDirNameRoot", "transient" );
    setProperty( "sourceSuffix", ".java" );
    setProperty( "compiledSuffix", ".class" );
    setProperty( "packageSeparator", "." );
    setProperty( "javacMainClassName", "sun.tools.javac.Main" );
    setProperty( "outputStreamClassName", "java.io.OutputStream" );
    setProperty( "stringClassName", "java.lang.String" );
    setProperty( "compileCommandName", "javac" );
    setProperty( "compileMethodName", "compile" );
  }

  //~ Methods ******************************************************************

  /**
   * Tests whether the system compiler class is available on the current
   * platform.
   *
   * @return true if the compiler class is available
   *
   * @see #isShellAvailable
   * @see #useCompilerClass
   * @see #useShell
   */
  public synchronized boolean isCompilerClassAvailable(  ) {
    if( testedCompilerClass ) {
      return ( compilerClassAvailable );
    } else {
      testedCompilerClass = true;

      try {
        // Try to load the compiler class.
        Class javacMain = Class.forName( getProperty( "javacMainClassName" ) );

        // Construct the parameters and types for the compiler class constructor.
        Object[] objs = { outputStream, getProperty( "compileCommandName" ) };

        compilerConstructorParams = objs;

        Class[] compilerConstructorParamTypes = {
          Class.forName( getProperty( "outputStreamClassName" ) ),
          Class.forName( getProperty( "stringClassName" ) )
        };

        // Get the compiler class constructor.
        compilerConstructor = javacMain.getConstructor( 
            compilerConstructorParamTypes );

        // Construct the parameter types for the compilation method.
        Class[] classes = { ( new String[0] ).getClass(  ) };

        compileParamTypes        = classes;

        // Get the compilation method.
        compileMethod            = javacMain.getMethod( 
            getProperty( "compileMethodName" ), compileParamTypes );
        compilerClassAvailable   = true;

        return compilerClassAvailable;
      } catch( Exception e ) {
        return false;
      }
    }
  }

  /************************************************************************************************************/

  /**
   * Sets a new value for one of the system dependent properties.
   *
   * @param propertyName a string specifying the name of the property to be
   *        set, with the following interpretation: <TABLE>
   * @param propertyValue the string value
   *
   * @see #getProperty
   */
  public synchronized void setProperty( 
    String propertyName, String propertyValue ) {
    properties.put( propertyName, propertyValue );
  }

  /**
   * Returns the current value of one of the system dependent properties.
   *
   * @param propertyName a string specifying the name of the property to be
   *        read, with interpretation as for <A
   *        HREF="#setProperty">setProperty</A>.
   *
   * @return the value corresponding to the named property, or empty string if
   *         there is no match
   *
   * @see #setProperty
   */
  public synchronized String getProperty( String propertyName ) {
    Object val = properties.get( propertyName );

    if( val == null ) {
      // Check for special cases which are generated dynamically.
      if( propertyName.equals( "javaBinPath" ) ) {
        return getProperty( "fileSeparator" ) + "bin";
      }

      if( propertyName.equals( "compilerPath" ) ) {
        String home = getProperty( "javaHomePath" );

        //yank off the jre bit
        home = home.substring( 0, home.indexOf( "/jre" ) );

        return home + getProperty( "javaBinPath" ) +
        getProperty( "fileSeparator" ) + "javac";
      }

      if( propertyName.equals( "interpreterPath" ) ) {
        return getProperty( "javaHomePath" ) + getProperty( "javaBinPath" ) +
        getProperty( "fileSeparator" ) + "java";
      }

      return "";
    } else {
      return ( String )val;
    }
  }

  /**
   * Tests whether the shell commands are available on the current platform.
   *
   * @return true if shell commands are available
   *
   * @see #isCompilerClassAvailable
   * @see #useCompilerClass
   * @see #useShell
   */
  public synchronized boolean isShellAvailable(  ) {
    if( testedShell ) {
      return ( shellAvailable );
    } else {
      testedShell = true;

      try {
        // Construct a shell command to call 'java -version'.
        String command = getProperty( "interpreterPath" ) + " -version";

        // Attempt to execute the command in a shell.
        Exec.setVerbose( false );
        shellAvailable = Exec.execWait( command );

        return shellAvailable;
      } catch( Exception e ) {
        return false;
      }
    }
  }

  /**
   * Compiles the given definition, with the main class inferred. The main
   * class is returned.
   *
   * @param classDefinition a string containing the complete definition of the
   *        class to be compiled
   *
   * @return the compiled class
   *
   * @exception CompilationException if the class definition is invalid or
   *            invocation of the compiler fails
   *
   * @see #compileClass(java.lang.String, java.lang.String)
   * @see #compileClass(java.lang.String[], java.lang.String)
   * @see #compileClass(java.lang.String[], java.lang.String[],
   *      java.lang.String)
   * @see #compileClasses
   * @see #compileSpecificClasses
   * @see #compileClassesToBytes
   * @see #compileClassesToFiles
   */
  public synchronized Class compileClass( String classDefinition )
    throws CompilationException {
    // Wrap the single class definition in an array.
    String[] classDefinitions = { classDefinition };

    // Extract the class names from the class definitions.
    String resultClassName = ( extractFullClassNames( classDefinitions ) )[0];

    try {
      // Compile class definition and return compiled class.
      return compileClass( classDefinitions, resultClassName );
    } catch( ClassNotFoundException e ) {
      throw new CompilationException( "Class name extraction failed" );
    }
  }

  /**
   * Compiles the given definition, with the main class specified explicitly.
   * The main class is returned. This caters for cases where automatic class
   * name extraction does not work.
   *
   * @param classDefinition a string containing the complete definition of the
   *        class to be compiled
   * @param mainClassName a string specifying the fully qualified name of the
   *        class to be compiled
   *
   * @return the compiled class
   *
   * @exception CompilationException if the class definition is invalid or
   *            invocation of the compiler fails
   * @exception ClassNotFoundException if the specified main class cannot be
   *            found
   *
   * @see #compileClass(java.lang.String)
   * @see #compileClass(java.lang.String[], java.lang.String)
   * @see #compileClass(java.lang.String[], java.lang.String[],
   *      java.lang.String)
   * @see #compileClasses
   * @see #compileSpecificClasses
   * @see #compileClassesToBytes
   * @see #compileClassesToFiles
   */
  public synchronized Class compileClass( 
    String classDefinition, String mainClassName )
    throws CompilationException, ClassNotFoundException {
    // Wrap the single class definition and class names in arrays.
    String[] classDefinitions = { classDefinition };
    String[] mainClassNames   = { mainClassName };

    // Compile class definition.
    Class[] compiledClasses = compileClasses( classDefinitions, mainClassNames );

    // Return the specified compiled class.
    return findNamedClass( compiledClasses, mainClassName );
  }

  /**
   * Compiles the given set of definitions, with the main classes inferred. The
   * named class is returned.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param resultClassName a string specifying the fully qualified name of the
   *        class to be returned
   *
   * @return the specified compiled class
   *
   * @exception CompilationException if the class definition is invalid or
   *            invocation of the compiler fails
   * @exception ClassNotFoundException if the specified result class cannot be
   *            found
   *
   * @see #compileClass(java.lang.String)
   * @see #compileClass(java.lang.String, java.lang.String)
   * @see #compileClass(java.lang.String[], java.lang.String[],
   *      java.lang.String)
   * @see #compileClasses
   * @see #compileSpecificClasses
   * @see #compileClassesToBytes
   * @see #compileClassesToFiles
   */
  public synchronized Class compileClass( 
    String[] classDefinitions, String resultClassName )
    throws CompilationException, ClassNotFoundException {
    // Compile the class definitions.
    Class[] compiledClasses = compileClasses( classDefinitions );

    // Return the specified compiled class.
    return findNamedClass( compiledClasses, resultClassName );
  }

  /**
   * Compiles the given set of definitions, with the main classes specified
   * explicitly. The named class is returned. This caters for cases where
   * automatic class name extraction does not work.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param mainClassNames an array of strings specifying the fully qualified
   *        names of the classes to be compiled
   * @param resultClassName a string specifying the fully qualified name of the
   *        class to be returned
   *
   * @return the specified compiled class
   *
   * @exception CompilationException if the class definition is invalid or
   *            invocation of the compiler fails
   * @exception ClassNotFoundException if the specified result class cannot be
   *            found
   *
   * @see #compileClass(java.lang.String)
   * @see #compileClass(java.lang.String, java.lang.String)
   * @see #compileClass(java.lang.String[], java.lang.String[],
   *      java.lang.String)
   * @see #compileClasses
   * @see #compileSpecificClasses
   * @see #compileClassesToBytes
   * @see #compileClassesToFiles
   */
  public synchronized Class compileClass( 
    String[] classDefinitions, String[] mainClassNames, String resultClassName )
    throws CompilationException, ClassNotFoundException {
    // Compile the class definitions.
    Class[] compiledClasses = compileClasses( classDefinitions, mainClassNames );

    // Return the specified compiled class.
    return findNamedClass( compiledClasses, resultClassName );
  }

  /**
   * Compiles the given set of definitions, with the main classes inferred. All
   * resulting classes, including any subsidiary or inner classes, are
   * returned.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   *
   * @return an array containing all compiled classes
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   *
   * @see #compileClasses(java.lang.String[], java.lang.String[])
   * @see #compileClasses(java.lang.String[], java.lang.String[],
   *      java.lang.String[])
   * @see #compileClass
   * @see #compileSpecificClasses
   * @see #compileClassesToBytes
   * @see #compileClassesToFiles
   */
  public synchronized Class[] compileClasses( String[] classDefinitions )
    throws CompilationException {
    // Extract the class names from the class definitions.
    String[] mainClassNames = extractFullClassNames( classDefinitions );

    // Return the compiled classes.
    return compileClasses( classDefinitions, mainClassNames );
  }

  /**
   * Compiles the given set of definitions, with the main classes specified
   * explicitly. All resulting classes, including any subsidiary or inner
   * classes, are returned.      This caters for cases where automatic class
   * name extraction does not work.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param mainClassNames an array of strings specifying the fully qualified
   *        names of the classes to be compiled
   *
   * @return an array containing all compiled classes
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   *
   * @see #compileClasses(java.lang.String[])
   * @see #compileClasses(java.lang.String[], java.lang.String[],
   *      java.lang.String[])
   * @see #compileClass
   * @see #compileSpecificClasses
   * @see #compileClassesToBytes
   * @see #compileClassesToFiles
   */
  public synchronized Class[] compileClasses( 
    String[] classDefinitions, String[] mainClassNames )
    throws CompilationException {
    // Get the root directory.
    createNewCompilationDir(  );

    // Compile the class definitions and generate .class files.
    compile( classDefinitions, mainClassNames );

    // Extract the class names from the class definitions.
    String[] resultClassNames = fileToClassNames( scanFileNames(  ) );

    try {
      // Return the compiled classes.
      return loadClasses( resultClassNames );
    } catch( ClassNotFoundException e ) {
      throw new CompilationException( "Loading all classes failed" );
    }
  }

  /**
   * Compiles the given set of definitions, with the main classes specified
   * explicitly. The named classes are returned. This caters for cases where
   * automatic class name extraction does not work.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param mainClassNames an array of strings specifying the fully qualified
   *        names of the classes to be compiled
   * @param resultClassNames an array of strings specifying the fully qualified
   *        names of the classes to be returned
   *
   * @return an array containing the specified compiled classes
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   * @exception ClassNotFoundException if any of the specified result classes
   *            cannot be found
   *
   * @see #compileClasses(java.lang.String[])
   * @see #compileClasses(java.lang.String[], java.lang.String[])
   * @see #compileClass
   * @see #compileSpecificClasses
   * @see #compileClassesToBytes
   * @see #compileClassesToFiles
   */
  public synchronized Class[] compileClasses( 
    String[] classDefinitions, String[] mainClassNames,
    String[] resultClassNames )
    throws CompilationException, ClassNotFoundException {
    // Get the root directory.
    createNewCompilationDir(  );

    // Compile the class definitions and generate .class files.
    compile( classDefinitions, mainClassNames );

    // Return the compiled classes.
    return loadClasses( resultClassNames );
  }

  /**
   * Compiles the given set of class definitions to byte arrays, with the main
   * classes inferred. All resulting classes, including any subsidiary or
   * inner classes, are returned.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   *
   * @return an array of arrays containing the byte codes of the compiled
   *         classes
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   *
   * @see #compileClassesToBytes(java.lang.String[], java.lang.String[])
   * @see #compileClassesToBytes(java.lang.String[], java.lang.String[],
   *      java.lang.String[])
   * @see #compileSpecificClassesToBytes
   * @see #compileClass
   * @see #compileClasses
   * @see #compileClassesToFiles
   */
  public synchronized byte[][] compileClassesToBytes( 
    String[] classDefinitions ) throws CompilationException {
    // Extract the class names from the class definitions.
    String[] mainClassNames = extractFullClassNames( classDefinitions );

    // Return the compiled classes as byte arrays.
    return compileClassesToBytes( classDefinitions, mainClassNames );
  }

  /**
   * Compiles the given set of class definitions to byte arrays, with the main
   * classes specified explicitly. All resulting classes, including any
   * subsidiary or inner classes, are returned.      This caters for cases
   * where automatic class name extraction does not work.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param mainClassNames an array of strings specifying the fully qualified
   *        names of the classes to be compiled
   *
   * @return an array of arrays containing the byte codes of the compiled
   *         classes
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   *
   * @see #compileClassesToBytes(java.lang.String[])
   * @see #compileClassesToBytes(java.lang.String[], java.lang.String[],
   *      java.lang.String[])
   * @see #compileSpecificClassesToBytes
   * @see #compileClass
   * @see #compileClasses
   * @see #compileClassesToFiles
   */
  public synchronized byte[][] compileClassesToBytes( 
    String[] classDefinitions, String[] mainClassNames )
    throws CompilationException {
    // Get the root directory.
    createNewCompilationDir(  );

    // Compile the class definitions and generate .class files.
    compile( classDefinitions, mainClassNames );

    // Extract the class names from the class definitions.
    String[] resultClassNames = fileToClassNames( scanFileNames(  ) );

    try {
      // Return the compiled byte codes.
      return loadBytes( resultClassNames );
    } catch( ClassNotFoundException e ) {
      throw new CompilationException( "Loading all classes failed" );
    }
  }

  /**
   * Compiles the given set of class definitions to byte arrays, with the main
   * classes specified explicitly. The named classes are returned. This caters
   * for cases where automatic class name extraction does not work.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param mainClassNames an array of strings specifying the fully qualified
   *        names of the classes to be compiled
   * @param resultClassNames an array of strings specifying the fully qualified
   *        names of the classes to be returned
   *
   * @return an array containing all compiled classes
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   * @exception ClassNotFoundException if any of the specified result classes
   *            cannot be found
   *
   * @see #compileClassesToBytes(java.lang.String[])
   * @see #compileClassesToBytes(java.lang.String[], java.lang.String[])
   * @see #compileSpecificClassesToBytes
   * @see #compileClass
   * @see #compileClasses
   * @see #compileClassesToFiles
   */
  public synchronized byte[][] compileClassesToBytes( 
    String[] classDefinitions, String[] mainClassNames,
    String[] resultClassNames )
    throws CompilationException, ClassNotFoundException {
    // Get the root directory.
    createNewCompilationDir(  );

    // Compile the class definitions and generate .class files.
    compile( classDefinitions, mainClassNames );

    // Return the compiled classes.
    return loadBytes( resultClassNames );
  }

  /**
   * Compiles the given set of class definitions to <CODE>.class</CODE> files,
   * with the main classes inferred. All resulting classes, including any
   * subsidiary or inner classes, are returned.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   *
   * @return an array of files containing the byte codes of the compiled
   *         classes
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   *
   * @see #compileClassesToFiles(java.lang.String[], java.lang.String[])
   * @see #compileClassesToFiles(java.lang.String[], java.lang.String[],
   *      java.lang.String[])
   * @see #compileSpecificClassesToFiles
   * @see #compileClass
   * @see #compileClasses
   * @see #compileClassesToBytes
   */
  public synchronized File[] compileClassesToFiles( String[] classDefinitions )
    throws CompilationException {
    // Extract the class names from the class definitions.
    String[] mainClassNames = extractFullClassNames( classDefinitions );

    // Return the compiled classes as .class files.
    return compileClassesToFiles( classDefinitions, mainClassNames );
  }

  /**
   * Compiles the given set of class definitions to <CODE>.class</CODE> files,
   * with the main classes specified explicitly. All resulting classes,
   * including any subsidiary or inner classes, are returned.      This caters
   * for cases where automatic class name extraction does not work.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param mainClassNames an array of strings specifying the fully qualified
   *        names of the classes to be compiled
   *
   * @return an array of files containing the byte codes of the compiled
   *         classes
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   *
   * @see #compileClassesToFiles(java.lang.String[])
   * @see #compileClassesToFiles(java.lang.String[], java.lang.String[],
   *      java.lang.String[])
   * @see #compileSpecificClassesToFiles
   * @see #compileClass
   * @see #compileClasses
   * @see #compileClassesToBytes
   */
  public synchronized File[] compileClassesToFiles( 
    String[] classDefinitions, String[] mainClassNames )
    throws CompilationException {
    // Get the root directory.
    createNewCompilationDir(  );

    // Compile the class definitions and generate .class files.
    compile( classDefinitions, mainClassNames );

    // Extract the class names from the class definitions.
    String[] resultClassNames = fileToClassNames( scanFileNames(  ) );

    try {
      // Return the compiled files.
      return loadFiles( resultClassNames );
    } catch( ClassNotFoundException e ) {
      throw new CompilationException( "Loading all classes failed" );
    }
  }

  /**
   * Compiles the given set of class definitions to <CODE>.class</CODE> files,
   * with the main classes specified explicitly. The named classes are
   * returned. This caters for cases where automatic class name extraction
   * does not work.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param mainClassNames an array of strings specifying the fully qualified
   *        names of the classes to be compiled
   * @param resultClassNames an array of strings specifying the fully qualified
   *        names of the classes to be returned
   *
   * @return an array containing all compiled classes
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   * @exception ClassNotFoundException if any of the specified result classes
   *            cannot be found
   *
   * @see #compileClassesToFiles(java.lang.String[])
   * @see #compileClassesToFiles(java.lang.String[], java.lang.String[])
   * @see #compileSpecificClassesToFiles
   * @see #compileClass
   * @see #compileClasses
   * @see #compileClassesToBytes
   */
  public synchronized File[] compileClassesToFiles( 
    String[] classDefinitions, String[] mainClassNames,
    String[] resultClassNames )
    throws CompilationException, ClassNotFoundException {
    // Get the root directory.
    createNewCompilationDir(  );

    // Compile the class definitions and generate .class files.
    compile( classDefinitions, mainClassNames );

    // Return the compiled classes.
    return loadFiles( resultClassNames );
  }

  /**
   * Compiles the given set of definitions, with the main classes inferred. The
   * named classes are returned.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param resultClassNames an array of strings specifying the fully qualified
   *        names of the classes to be returned
   *
   * @return an array containing the specified compiled classes
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   * @exception ClassNotFoundException if any of the specified result classes
   *            cannot be found
   *
   * @see #compileClasses(java.lang.String[])
   * @see #compileClasses(java.lang.String[], java.lang.String[])
   * @see #compileClasses(java.lang.String[], java.lang.String[],
   *      java.lang.String[])
   * @see #compileClass
   * @see #compileSpecificClasses
   * @see #compileClassesToBytes
   * @see #compileClassesToFiles
   */
  public synchronized Class[] compileSpecificClasses( 
    String[] classDefinitions, String[] resultClassNames )
    throws CompilationException, ClassNotFoundException {
    // Extract the class names from the class definitions.
    String[] mainClassNames = extractFullClassNames( classDefinitions );

    // Return the compiled classes.
    return compileClasses( classDefinitions, mainClassNames, resultClassNames );
  }

  /**
   * Compiles the given set of class definitions to byte arrays, with the main
   * classes inferred. The named classes are returned.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param resultClassNames an array of strings specifying the fully qualified
   *        names of the classes to be returned
   *
   * @return an array containing the specified compiled classes
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   * @exception ClassNotFoundException if any of the specified result classes
   *            cannot be found
   *
   * @see #compileClassesToBytes
   * @see #compileClass
   * @see #compileClasses
   * @see #compileClassesToFiles
   */
  public synchronized byte[][] compileSpecificClassesToBytes( 
    String[] classDefinitions, String[] resultClassNames )
    throws CompilationException, ClassNotFoundException {
    // Extract the class names from the class definitions.
    String[] mainClassNames = extractFullClassNames( classDefinitions );

    // Return the compiled classes.
    return compileClassesToBytes( 
      classDefinitions, mainClassNames, resultClassNames );
  }

  /**
   * Compiles the given set of class definitions to <CODE>.class</CODE> files,
   * with the main classes inferred. The named classes are returned.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param resultClassNames an array of strings specifying the fully qualified
   *        names of the classes to be returned
   *
   * @return an array containing the specified compiled classes
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   * @exception ClassNotFoundException if any of the specified result classes
   *            cannot be found
   *
   * @see #compileClassesToFiles
   * @see #compileClass
   * @see #compileClasses
   * @see #compileClassesToBytes
   */
  public synchronized File[] compileSpecificClassesToFiles( 
    String[] classDefinitions, String[] resultClassNames )
    throws CompilationException, ClassNotFoundException {
    String[] mainClassNames = extractFullClassNames( classDefinitions );

    // Return the compiled files.
    return compileClassesToFiles( 
      classDefinitions, mainClassNames, resultClassNames );
  }

  /**
   * Creates a nested directory structure corresponding to the packages in the
   * given full class name, rooted at the directory most recently added to the
   * classpath.
   *
   * @param fullClassName a string specifying a fully qualified class name
   *
   * @return the directory corresponding to the most deeply nested package
   *
   * @exception CompilationException if the attempt to create the directories
   *            fails
   */
  protected File getPackageDirectory( String fullClassName )
    throws CompilationException {
    // Create tokenizer to extract the package name components.
    StringTokenizer st = new StringTokenizer( 
        fullClassName, getProperty( "packageSeparator" ) );

    // Start at the directory most recently added to the classpath.
    File packageDir = ( File )classPathVector.firstElement(  );

    // Get the first token.
    String t = st.nextToken(  );

    // Add the sub-directories for the package structure in turn, if any.
    // The last token is ignored since it is the local class name.
    while( st.hasMoreTokens(  ) ) {
      // Generate the name of the next directory.
      packageDir = new File( packageDir, t );

      // Attempt to create the package directory if it does not already exist.
      if( !packageDir.exists(  ) ) {
        if( !packageDir.mkdir(  ) ) {
          throw new CompilationException( "creation of root directory failed" );
        }
      }

      // Get the next token.
      t = st.nextToken(  );
    }

    // Return the deepest level directory.
    return packageDir;
  }

  /**
   * Recursively deletes everything from the specified directory.
   *
   * @param dir the directory to be cleared
   * @param deleteDir true if the directory is to be deleted too
   */
  protected void clearDir( File dir, boolean deleteDir ) {
    clearDir( dir );

    if( deleteDir ) {
      dir.delete(  );
    }
  }

  /**
   * Recursively deletes everything from the specified directory.
   *
   * @param dir the directory to be cleared
   */
  protected void clearDir( File dir ) {
    // Get list of files and sub-directories.
    String[] existingFileNames = dir.list(  );

    for( int i = 0; i < existingFileNames.length; i++ ) {
      File f = new File( dir, existingFileNames[i] );

      // Delete the file or recursively clear the sub-directory.
      if( f.isDirectory(  ) ) {
        clearDir( f );
      }

      f.delete(  );
    }
  }

  /************************************************************************************************************/

  /**
   * Compiles the given set of class definitions to <CODE>.class</CODE> files.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param mainClassNames an array of strings specifying the fully qualified
   *        names of the classes to be compiled
   *
   * @exception CompilationException if any of the class definitions are
   *            invalid or invocation of the compiler fails
   */
  protected void compile( String[] classDefinitions, String[] mainClassNames )
    throws CompilationException {
    // Check number of class definitions and class names match and are non-zero.
    if( 
      ( classDefinitions == null ) || ( mainClassNames == null ) ||
        ( classDefinitions.length == 0 ) ||
        ( classDefinitions.length != mainClassNames.length ) ) {
      throw new CompilationException( "incorrect number of class names" );
    }

    try {
      // Construct the arguments to the compilation command.
      // The first four arguments specify the class path and the output directory; the rest are the source file names.
      String[] argv               = new String[mainClassNames.length + 4];
      String compilationClassPath = "";
      Enumeration compilationDirs = classPathVector.elements(  );

      while( compilationDirs.hasMoreElements(  ) ) {
        compilationClassPath += ( getProperty( "pathSeparator" ) +
        ( ( File )( compilationDirs.nextElement(  ) ) ).getAbsolutePath(  ) );
      }

      String outputDirPath = ( ( File )classPathVector.firstElement(  ) ).getAbsolutePath(  );

      // The compilation class path includes the standard class path and the successive output directories.
      argv[0]   = "-classpath";
      argv[1]   = getProperty( "javaClassPath" ) + compilationClassPath;

      // Output to the root directory.
      argv[2]   = "-d";
      argv[3]   = outputDirPath;

      // Write the class definitions into corresponding source files. 
      generateSourceFiles( classDefinitions, mainClassNames, argv );

      // Try to invoke compiler class directly, if enabled.
      if( useCompilerClass && isCompilerClassAvailable(  ) ) {
        // Create a new instance of the compiler class.
        Object compiler = compilerConstructor.newInstance( 
            compilerConstructorParams );

        // Construct the parameters for the compilation method.
        Object[] compileParams = { argv };

        // Invoke the compilation method of the compiler object.
        if( 
          !( ( Boolean )compileMethod.invoke( compiler, compileParams ) ).booleanValue(  ) ) {
          throwCompilationErrors(  );
        }

        return;
      }

      // Try to invoke the compiler via a shell command, if enabled.
      if( useShell && isShellAvailable(  ) ) {
        // Construct a shell command to compile the source files.
        String compileCommand = getProperty( "compilerPath" ) + " -classpath " +
          getProperty( "javaClassPath" ) + compilationClassPath + " -d " +
          outputDirPath;

        // Add the full paths of the source files.
        for( int i = 4; i < argv.length; i++ ) {
          compileCommand += ( " " + argv[i] );
        }

        // Attempt to execute the compile command in a shell.
        Exec.setVerbose( false );

        if( !Exec.execWait( compileCommand ) ) {
          throwCompilationErrors(  );
        }

        return;
      }

      throw new CompilationException( "no compilation mechanism successful" );
    } catch( IOException e ) {
      throw new CompilationException( 
        "could not create source files: " + e.getMessage(  ) );
    } catch( InstantiationException e ) {
      throw new CompilationException( 
        "could not create new instance of " +
        getProperty( "javacMainClassName" ) + ": " + e.getMessage(  ) );
    } catch( IllegalAccessException e ) {
      throw new CompilationException( 
        "could not access constructor for " +
        getProperty( "javacMainClassName" ) + ": " + e.getMessage(  ) );
    } catch( InvocationTargetException e ) {
      throw new CompilationException( 
        "constructor for " + getProperty( "javacMainClassName" ) +
        " threw exception: " + e.getMessage(  ) );
    }
  }

  /**
   * Creates a new compilation directory. To avoid race conditions where two
   * threads try to use the same directory number, calls the static
   * synchronized method  <A
   * HREF="#createNewCompilationDirSynchronized">createNewCompilationDirSynchronized</A>
   * to prevent multiple threads executing the <A
   * HREF="#createNewCompilationDirActual">createNewCompilationDirActual</A>
   * method concurrently (even of separate <A
   * HREF="DynamicCompiler.html#_top_">DynamicCompiler</A> instances).
   *
   * @exception CompilationException if the directory could not be created
   */
  protected void createNewCompilationDir(  ) throws CompilationException {
    createNewCompilationDirSynchronized( this );
  }

  /**
   * Calls <A
   * HREF="#createNewCompilationDirActual">createNewCompilationDirActual</A>
   * to create a new compilation directory. Synchronized to avoid multiple
   * concurrent executions.
   *
   * @exception CompilationException if the directory could not be created
   *
   * @see #createNewCompilationDirActual
   */
  protected static synchronized void createNewCompilationDirSynchronized( 
    DynamicCompiler dc ) throws CompilationException {
    dc.createNewCompilationDirActual(  );
  }

  /**
   * Creates a new compilation directory, numbered using the static variable
   * <A HREF="#count">count</A>.
   *
   * @exception CompilationException if the directory could not be created
   */
  protected void createNewCompilationDirActual(  ) throws CompilationException {
    // Increment the compilation count
    count++;

    // Generate a new directory name.
    File newDir = new File( getProperty( "userDirPath" ) );  //, getProperty ("tempDirNameRoot") + count);

    classPathVector.insertElementAt( newDir, 0 );
  }

  /**
   * Extracts the fully qualified class names corresponding to the given class
   * definitions. It will fail if the word 'package' or 'class' occurs in a
   * comment before its first occurrence as a reserved word.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes
   *
   * @return an array of strings containing the corresponding full class names
   */
  protected String[] extractFullClassNames( String[] classDefinitions ) {
    // Initialise array to hold full class names.
    String[] fullClassNames = new String[classDefinitions.length];

    for( int i = 0; i < fullClassNames.length; i++ ) {
      // Get the class definition.
      String classDefinition = classDefinitions[i];

      // Create a tokenizer that splits on spaces, tabs and newlines.
      StringTokenizer st    = new StringTokenizer( classDefinition, " \t\n\r;" );
      String localClassName = "";
      String fullClassName  = "";
      String packageName    = "";

      // Get the first token.
      String token = st.nextToken(  );

      // Iterate through the class definition looking for package and class declarations.
      while( st.hasMoreTokens(  ) ) {
        // Check for 'package' reserved word and record the package name if found.
        if( token.equals( "package" ) ) {
          packageName = st.nextToken(  );
        }

        // Check for 'class' reserved word and record the class name if found.
        if( token.equals( "class" ) ) {
          localClassName = st.nextToken(  );

          // Construct the full class name from the package name, if any, and the local class name.
          if( packageName.equals( "" ) ) {
            fullClassName = localClassName;
          } else {
            fullClassName = packageName + "." + localClassName;
          }

          // Can now stop reading the class definition.
          break;
        }

        // Get the next token.
        token = st.nextToken(  );
      }

      // Record the full class name.
      fullClassNames[i] = fullClassName;
    }

    return fullClassNames;
  }

  /**
   * Extracts a local class name.
   *
   * @param mainClassName a string specifying the fully qualified names of the
   *        class to be compiled
   *
   * @return a string containing the local class name
   *
   * @exception CompilationException if not all the full class names are in the
   *            same package
   */
  protected String extractLocalClassName( String mainClassName )
    throws CompilationException {
    String localClassName = mainClassName;

    // Look for the last occurrence of a dot in the full class name.
    int lastDotIndex = mainClassName.lastIndexOf( 
        getProperty( "packageSeparator" ) );

    // Get the full package name, allowing for there being no package.
    // For example, if the full class name is "x.y.myClass", the full package name is "x.y".
    String fullPackageName = "";

    if( lastDotIndex > -1 ) {
      fullPackageName = mainClassName.substring( 0, lastDotIndex );
    }

    int fullPackageNameLength = fullPackageName.length(  );

    // Extract the local class name.
    if( fullPackageNameLength > 0 ) {
      localClassName = mainClassName.substring( fullPackageNameLength + 1 );
    }

    return localClassName;
  }

  /**
   * Converts the given array of fully qualified .class file names to fully
   * qualified class names, by removing the .class suffixes.
   *
   * @param fileNames an array of strings containing fully qualified .class
   *        file names
   *
   * @return an array containing fully qualified class names
   */
  protected String[] fileToClassNames( String[] fileNames ) {
    String[] classNames = new String[fileNames.length];

    // Get the length of the suffix in characters.
    int suffixLength = getProperty( "compiledSuffix" ).length(  );

    for( int i = 0; i < classNames.length; i++ ) {
      String fileName = fileNames[i];

      classNames[i] = fileName.substring( 
          0, fileName.length(  ) - suffixLength );
    }

    return classNames;
  }

  /**
   * Cleans up transient source and class files when the compiler object is
   * collected. This can be disabled by setting the <A
   * HREF="#deleteFilesOnFinalize">deleteFilesOnFinalize</A> flag to false.
   *
   * @exception Throwable if anything goes wrong
   */
  protected void finalize(  ) throws Throwable {
    super.finalize(  );

    if( deleteFilesOnFinalize ) {
      // Get the transient directories used by this compiler.
      Enumeration compilationDirs = classPathVector.elements(  );

      // Delete each directory.
      while( compilationDirs.hasMoreElements(  ) ) {
        File transientDir = ( File )compilationDirs.nextElement(  );

        clearDir( transientDir, true );
      }
    }
  }

  /**
   * Finds the named class in the given array.
   *
   * @param classes an array of classes
   * @param fullClassName a fully qualified class name
   *
   * @return the class with the specified name
   *
   * @exception ClassNotFoundException if the named class is not found
   */
  protected Class findNamedClass( Class[] classes, String fullClassName )
    throws ClassNotFoundException {
    for( int i = 0; i < classes.length; i++ ) {
      if( classes[i].getName(  ).equals( fullClassName ) ) {
        return classes[i];
      }
    }

    throw new ClassNotFoundException(  );
  }

  /**
   * Creates a nested directory structure corresponding to the packages in the
   * given class names, rooted at the given directory.
   *
   * @param classDefinitions an array of strings containing the complete
   *        definitions of the classes to be compiled
   * @param mainClassNames an array of strings specifying the fully qualified
   *        names of the classes to be compiled
   * @param argv an array of strings that will contain the arguments to the
   *        compilation command
   *
   * @exception CompilationException if an existing source file cannot be
   *            deleted
   * @exception IOException if any of the source files cannot be created
   */
  protected void generateSourceFiles( 
    String[] classDefinitions, String[] mainClassNames, String[] argv )
    throws CompilationException, IOException {
    for( int i = 0; i < mainClassNames.length; i++ ) {
      // Create the directory structure corresponding to the package.
      File packageDir = getPackageDirectory( mainClassNames[i] );

      // Extract the local class name.
      // For example, if the full class name is "x.y.myClass", the local class name is "myClass".
      String localClassName = extractLocalClassName( mainClassNames[i] );

      // Construct name for the source file.
      String sourceFileName = localClassName + getProperty( "sourceSuffix" );

      // Delete the source file if it already exists.
      File sourceFile = new File( packageDir, sourceFileName );

      if( sourceFile.exists(  ) ) {
        if( !sourceFile.delete(  ) ) {
          throw new CompilationException( 
            "deletion of existing source class file failed" );
        }
      }

      // Write the source code to the file.
      PrintWriter printWriter = new PrintWriter( 
          new FileOutputStream( sourceFile ) );

      printWriter.println( classDefinitions[i] );
      printWriter.flush(  );
      printWriter.close(  );

      // Record the full path of the source file in the list of arguments to the compilation command.
      argv[4 + i] = sourceFile.getAbsolutePath(  );
    }
  }

  /**
   * Loads the specified classes as byte arrays, using the class mapper object.
   *
   * @param classNames an array of strings containing the fully qualified names
   *        of the classes to be loaded
   *
   * @return an array containing the loaded classes as byte arrays
   *
   * @exception ClassNotFoundException if any of the specified classes cannot
   *            be loaded
   *
   * @see #loadClasses
   * @see #loadFiles
   */
  protected byte[][] loadBytes( String[] classNames )
    throws ClassNotFoundException {
    // Create array to hold the byte arrays.
    byte[][] compiledBytes = new byte[classNames.length][];

    // Load each compiled class into the array.
    for( int i = 0; i < classNames.length; i++ ) {
      compiledBytes[i] = classByteFileLoader.get( classNames[i] );

      if( compiledBytes[i] == null ) {
        throw new ClassNotFoundException( "byte code not found" );
      }
    }

    return compiledBytes;
  }

  /**
   * Loads the specified classes using a custom class loader and the class
   * mapper object.
   *
   * @param classNames an array of strings containing the fully qualified names
   *        of the classes to be loaded
   *
   * @return an array containing the loaded classes
   *
   * @exception ClassNotFoundException if any of the specified classes cannot
   *            be loaded
   *
   * @see #loadBytes
   * @see #loadFiles
   */
  protected Class[] loadClasses( String[] classNames )
    throws ClassNotFoundException {
    // Create a class loader that loads from the root directory.
    CustomClassLoader rootDirLoader = new CustomClassLoader( 
        ( ClassByteLoader )classByteFileLoader );

    // Create array to hold the compiled classes.
    Class[] compiledClasses = new Class[classNames.length];

    // Load each compiled class into the array.
    for( int i = 0; i < classNames.length; i++ ) {
      // Load the class and resolve external references.
      compiledClasses[i] = rootDirLoader.loadClass( classNames[i], true );
    }

    return compiledClasses;
  }

  /**
   * Finds the .class files containing the specified classes, using the class
   * mapper object.
   *
   * @param classNames an array of strings containing the fully qualified names
   *        of the classes to be loaded
   *
   * @return an array containing the .class files
   *
   * @exception ClassNotFoundException if any of the specified classes cannot
   *            be loaded
   *
   * @see #loadClasses
   * @see #loadBytes
   */
  protected File[] loadFiles( String[] classNames )
    throws ClassNotFoundException {
    // Create array to hold the files.
    File[] compiledFiles = new File[classNames.length];

    // Load each compiled file into the array.
    for( int i = 0; i < classNames.length; i++ ) {
      compiledFiles[i] = classByteFileLoader.findClassFile( classNames[i] );

      if( compiledFiles[i] == null ) {
        throw new ClassNotFoundException( "file not found" );
      }
    }

    return compiledFiles;
  }

  /**
   * Performs a recursive scan of the most recent compilation directory, and
   * returns an array of strings containing the fully qualified names of all
   * .class files found, including the .class suffix. For example: {
   * "a.b.c.class", "a.b.d.class" }
   *
   * @return an array containing fully qualified class file names
   */
  protected String[] scanFileNames(  ) {
    // Create a vector to accumulate the class file names.
    Vector fullClassNameVector = new Vector(  );

    // Do a recursive scan of the most recently added compilation directory.
    scanFileNames( 
      ( File )classPathVector.firstElement(  ), "", fullClassNameVector );

    // Convert the vector to an array.
    int noClasses           = fullClassNameVector.size(  );
    String[] fullClassNames = new String[noClasses];

    for( int i = 0; i < noClasses; i++ ) {
      fullClassNames[i] = ( String )fullClassNameVector.elementAt( i );
    }

    return fullClassNames;
  }

  /**
   * Performs a recursive scan of the specified directory, and adds to the
   * given vector strings containing the fully qualified names of all .class
   * files found, including the .class suffix. For example: { "a.b.c.class",
   * "a.b.d.class" }
   *
   * @param parentDir the directory to scan
   * @param pathName a string containing the accumulated path
   * @param fullClassNameVector a vector used to accumulate the class file
   *        names
   */
  protected void scanFileNames( 
    File parentDir, String pathName, Vector fullClassNameVector ) {
    // Get the names of files and directories in the given directory.
    String[] fileList = parentDir.list(  );

    // Add a separator to the path if it is not empty.
    if( !pathName.equals( "" ) ) {
      pathName += ".";
    }

    // Scan each file or directory.
    for( int i = 0; i < fileList.length; i++ ) {
      String localFileName = fileList[i];
      String fullFileName  = pathName + localFileName;

      // Test for a .class file.
      if( localFileName.endsWith( getProperty( "compiledSuffix" ) ) ) {
        fullClassNameVector.addElement( fullFileName );
      } else {
        // Recursively scan if it is a sub-directory.
        File subDir = new File( parentDir, localFileName );

        if( subDir.isDirectory(  ) ) {
          scanFileNames( subDir, fullFileName, fullClassNameVector );
        }
      }
    }
  }

  /**
   * Throws an exception with a message containing the compilation error
   * messages.
   *
   * @exception CompilationException always thrown
   */
  protected void throwCompilationErrors(  ) throws CompilationException {
    String compilerMessages = outputStream.toString(  );
    String errorMessage     = "compilation failed";

    if( compilerMessages.length(  ) > 0 ) {
      errorMessage += ( "\n\nError messages:" + "\n\n========================" +
      "\n\n" + compilerMessages + "\n\n========================\n" );
    }

    throw new CompilationException( errorMessage );
  }
}
