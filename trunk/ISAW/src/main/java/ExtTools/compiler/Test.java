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
 *
 * Header: /user/rsch/graham/java/packages/compiler/version1.2/RCS/Test.java,v 1.2 1998/11/06 10:37:03 graham Exp
 *
 * Revision 1.2  1998/11/06 10:37:03  graham
 * Added support for multiple packages and inner classes.
 *
 * Revision 1.1  1998/07/17 09:00:00  graham
 * Initial revision
 *
 * $Log$
 * Revision 1.1  2004/01/28 22:05:44  bouzekc
 * Added to CVS.  Changed package to ExtTools.compiler, moved original log,
 * changed at symbol in email in author tag to "_at_".  Formatted code.
 *
 */
package ExtTools.compiler;

import java.io.*;

import java.lang.reflect.*;


/**
 * Tests the <A HREF="DynamicCompiler.html">DynamicCompiler</A> class.
 * 
 * <P>
 * The <A HREF="../../compiler/Test.java">source code</A> is available.
 * </p>
 *
 * @author Graham Kirby (<A
 *         HREF="mailto:graham@dcs.st-and.ac.uk">graham_at_dcs.st-and.ac.uk</A>)
 * @version 1.2 2-Nov-98
 */
public class Test {
  //~ Methods ******************************************************************

  /**
   * Illustrates various uses of the <A
   * HREF="DynamicCompiler.html">DynamicCompiler</A> class. Prints results of
   * testing some of the following options:
   * 
   * <P>
   * 
   * <UL>
   * <li>
   * single class / multiple classes
   * </li>
   * <li>
   * no package / single package / multiple packages
   * </li>
   * <li>
   * single / multiple compilation by same compiler
   * </li>
   * <li>
   * multiple compilation by different compilers
   * </li>
   * <li>
   * static / non-static method invoked
   * </li>
   * <li>
   * void method / result returned
   * </li>
   * <li>
   * compiled classes stated explicitly / inferred
   * </li>
   * <li>
   * returned classes stated explicitly / inferred
   * </li>
   * <li>
   * single class per definition / multiple classes per definition
   * </li>
   * <li>
   * inner classes / no inner classes
   * </li>
   * <li>
   * direct class / shell invocation
   * </li>
   * <li>
   * compilation errors / no errors
   * </li>
   * </ul>
   * </p>
   *
   * @param args the command line arguments, which are ignored
   */
  public static void main( String[] args ) {
    String packageHeader = "package p.q.r;\n";

    // Single class.
    String defn1 = "public class Test1 {\n" + "  public static void m1() {\n" +
      "    System.out.println (\"Test OK.\");\n" + "  }\n" + "\n" +
      "  public void m2() {\n" + "    System.out.println (\"Test OK.\");\n" +
      "  }\n" + "\n" + "  public String m3 (String s) {\n" +
      "    return \"Test OK.\";\n" + "  }\n" + "}\n";

    // Call to a different class.
    String defn2 = "public class Test2 {\n" + "  public static void m1() {\n" +
      "    Test1 t = new Test1();\n" + "    t.m2 ();\n" + "  }\n" + "}\n";

    // Call to a different package.
    String defn3 = "public class Test3 {\n" + "  public static void m1() {\n" +
      "    p.q.r.Test2 t = new p.q.r.Test2();\n" + "    t.m1 ();\n" + "  }\n" +
      "}\n";

    // Non-public class.
    String defn4 = "public class Test4 {\n" + "  public static void m1() {\n" +
      "    Test5 t = new Test5();\n" + "    t.m1 ();\n" + "  }\n" + "}\n" +
      "\n" + "class Test5 {\n" + "  void m1() {\n" +
      "    System.out.println (\"Test OK.\");\n" + "  }\n" + "}\n";

    // Inner class.
    String defn5 = "public class Test6 {\n" + "  public void m1() {\n" +
      "    Test7 t = new Test7();\n" + "    t.m1 ();\n" + "  }\n" + "\n" +
      "  class Test7 {\n" + "    void m1() {\n" +
      "      System.out.println (\"Test OK.\");\n" + "    }\n" + "  }\n" +
      "}\n";

    // Mutually recursive packages.
    String defn6 = "package a.b.c;\n" + "import d.e.f.*;\n" +
      "public class Test8 {\n" + "  public void m1() {\n" +
      "    Test9 t = new Test9 (this);\n" + "    t.m1 ();\n" + "  }\n" + "\n" +
      "  public void m2() {\n" + "    System.out.println (\"Test OK.\");\n" +
      "  }\n" + "}\n";
    String defn7 = "package d.e.f;\n" + "import a.b.c.*;\n" +
      "public class Test9 {\n" + "  Test8 t8;\n" +
      "  public Test9 (Test8 t8) {\n" + "    this.t8 = t8;\n" + "  }\n" + "\n" +
      "  public void m1() {\n" + "    t8.m2 ();\n" + "  }\n" + "}\n";

    /************************************************************************************************************/

    // Run finalizers on exit to clean up transient files.
    //System.runFinalizersOnExit (true);

    /************************************************************************************************************/

    // Simplest test - single class, no package, calling static void method.
    try {
      System.out.println( "Single class." );

      Class c  = ( new DynamicCompiler(  ) ).compileClass( defn1 );
      Method m = c.getMethod( "m1", new Class[0] );

      m.invoke( null, new Object[0] );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Same within a package.
    try {
      System.out.println( "Single class in package." );

      Class c  = ( new DynamicCompiler(  ) ).compileClass( 
          packageHeader + defn1 );
      Method m = c.getMethod( "m1", new Class[0] );

      m.invoke( null, new Object[0] );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Non static method.
    try {
      System.out.println( "Single class / non-static method." );

      Class c  = ( new DynamicCompiler(  ) ).compileClass( defn1 );
      Method m = c.getMethod( "m2", new Class[0] );
      Object o = c.newInstance(  );

      m.invoke( o, new Object[0] );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Parameter and non-void result.
    try {
      System.out.println( "Single class / parameter and result." );

      Class c            = ( new DynamicCompiler(  ) ).compileClass( defn1 );
      Object[] params    = { "test parameter" };
      Class[] paramTypes = { params[0].getClass(  ) };
      Method m           = c.getMethod( "m3", paramTypes );
      Object o           = c.newInstance(  );
      String s           = ( String )m.invoke( o, params );

      System.out.println( s );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Two class definitions.
    try {
      System.out.println( "Two classes." );

      String[] defns = { defn1, defn2 };
      Class c        = ( new DynamicCompiler(  ) ).compileClass( 
          defns, "Test2" );
      Method m       = c.getMethod( "m1", new Class[0] );
      Object o       = c.newInstance(  );

      m.invoke( o, new Object[0] );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Separate compilation.
    try {
      System.out.println( "Separate compilation." );

      DynamicCompiler dc = new DynamicCompiler(  );
      String[] defns     = { packageHeader + defn1, packageHeader + defn2 };

      dc.compileClass( defns, "p.q.r.Test2" );

      Class c2 = dc.compileClass( defn3 );
      Method m = c2.getMethod( "m1", new Class[0] );
      Object o = c2.newInstance(  );

      m.invoke( o, new Object[0] );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Multiple classes in same definition.
    try {
      System.out.println( "Multiple classes in same definition." );

      DynamicCompiler dc = new DynamicCompiler(  );

      // Main class inferred.
      dc.compileClass( defn4 );

      // Main class explicit.
      Class c2 = dc.compileClass( defn4, "Test4" );
      Method m = c2.getMethod( "m1", new Class[0] );
      Object o = c2.newInstance(  );

      m.invoke( o, new Object[0] );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Multiple classes, multiple definitions.
    try {
      System.out.println( "Multiple classes, multiple definitions." );

      String[] defns       = { defn1, defn4 };
      String[] mainClasses = { "Test1", "Test4" };
      DynamicCompiler dc   = new DynamicCompiler(  );
      Class c              = dc.compileClass( defns, mainClasses, "Test4" );
      Method m             = c.getMethod( "m1", new Class[0] );
      Object o             = c.newInstance(  );

      m.invoke( o, new Object[0] );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Multiple classes returned.
    try {
      System.out.println( "Multiple classes returned." );

      String[] defns           = { defn1, defn4 };
      String[] mainClasses     = { "Test1", "Test4" };
      String[] expectedClasses = { "Test1", "Test4", "Test5" };
      DynamicCompiler dc       = new DynamicCompiler(  );
      Class[] classes1         = dc.compileClasses( defns );

      checkClassNames( classes1, expectedClasses );

      Class[] classes2 = dc.compileClasses( defns, mainClasses );

      checkClassNames( classes2, expectedClasses );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Multiple specified classes returned.
    try {
      System.out.println( "Multiple specified classes returned." );

      String[] defns       = { defn1, defn4 };
      String[] mainClasses = { "Test1", "Test4" };
      DynamicCompiler dc   = new DynamicCompiler(  );
      Class[] classes      = dc.compileSpecificClasses( defns, mainClasses );

      checkClassNames( classes, mainClasses );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Multiple classes specified and returned.
    try {
      System.out.println( "Multiple classes specified and returned." );

      String[] defns       = { defn1, defn4 };
      String[] mainClasses = { "Test1", "Test4" };
      DynamicCompiler dc   = new DynamicCompiler(  );
      Class[] classes      = dc.compileClasses( 
          defns, mainClasses, mainClasses );

      checkClassNames( classes, mainClasses );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Multiple recursive packages.
    try {
      System.out.println( "Multiple recursive packages." );

      String[] defns     = { defn6, defn7 };
      DynamicCompiler dc = new DynamicCompiler(  );
      Class c            = dc.compileClass( defns, "a.b.c.Test8" );
      Method m           = c.getMethod( "m1", new Class[0] );
      Object o           = c.newInstance(  );

      m.invoke( o, new Object[0] );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Multiple independent compilations.
    try {
      System.out.println( "Multiple independent compilations." );

      String[] defns1          = { defn6, defn7 };
      String[] defns2          = { defn1, defn4 };
      String[] expectedClasses = { "Test1", "Test4", "Test5" };
      DynamicCompiler dc       = new DynamicCompiler(  );

      dc.compileClasses( defns1 );

      Class[] classes2 = dc.compileClasses( defns2 );

      checkClassNames( classes2, expectedClasses );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Inner class.
    try {
      System.out.println( "Inner class." );

      String[] defns           = { defn5 };
      String[] expectedClasses = { "Test6$Test7", "Test6" };
      DynamicCompiler dc       = new DynamicCompiler(  );
      Class[] classes          = dc.compileClasses( defns );

      checkClassNames( classes, expectedClasses );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Compile to byte arrays.
    try {
      System.out.println( "Compile to byte arrays." );

      String[] defns                 = { defn1, defn4 };
      int[] expectedByteArrayLengths = { 574, 320, 442 };
      DynamicCompiler dc             = new DynamicCompiler(  );
      byte[][] byteArrays            = dc.compileClassesToBytes( defns );

      checkByteArrayLengths( byteArrays, expectedByteArrayLengths );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Compile to files.
    try {
      System.out.println( "Compile to files." );

      String[] defns            = { defn1, defn4 };
      int[] expectedFileLengths = { 574, 320, 442 };
      DynamicCompiler dc        = new DynamicCompiler(  );
      File[] files              = dc.compileClassesToFiles( defns );

      checkFileLengths( files, expectedFileLengths );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Manually setting class path.
    try {
      System.out.println( "Manually setting class path." );

      DynamicCompiler dc1 = new DynamicCompiler(  );
      String[] defns      = { packageHeader + defn1, packageHeader + defn2 };

      dc1.compileClass( defns, "p.q.r.Test2" );

      DynamicCompiler dc2 = new DynamicCompiler(  );

      dc2.classPathVector.insertElementAt( 
        dc1.classPathVector.elementAt( 0 ), 0 );

      Class c2 = dc2.compileClass( defn3 );
      Method m = c2.getMethod( "m1", new Class[0] );
      Object o = c2.newInstance(  );

      m.invoke( o, new Object[0] );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Compiler class invocation only.
    try {
      System.out.println( "Compiler class invocation only." );

      DynamicCompiler dc = new DynamicCompiler(  );

      if( !dc.isCompilerClassAvailable(  ) ) {
        System.out.println( "Class invocation not available." );
      } else {
        dc.useCompilerClass   = true;
        dc.useShell           = false;

        Class c               = dc.compileClass( defn1 );
        Method m              = c.getMethod( "m1", new Class[0] );

        m.invoke( null, new Object[0] );
      }

      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Shell invocation only.
    try {
      System.out.println( "Shell invocation only." );

      DynamicCompiler dc = new DynamicCompiler(  );

      if( !dc.isShellAvailable(  ) ) {
        System.out.println( "Shell invocation not available." );
      } else {
        dc.useCompilerClass   = false;
        dc.useShell           = true;

        Class c               = dc.compileClass( defn1 );
        Method m              = c.getMethod( "m1", new Class[0] );

        m.invoke( null, new Object[0] );
      }

      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/

    // Invalid class definitions (compilation should fail).
    try {
      System.out.println( 
        "Invalid class definitions (compilation should fail)." );

      String[] defns     = { "xxx " + defn1, "yyy " + defn4 };
      DynamicCompiler dc = new DynamicCompiler(  );

      dc.compileClasses( defns );
      System.out.println(  );
    } catch( Exception e ) {
      System.out.println( "Exception: " + e.getMessage(  ) );
    }

    /************************************************************************************************************/
    System.out.println( "Finished testing." );
  }

  /**
   * DOCUMENT ME!
   *
   * @param byteArrays DOCUMENT ME!
   * @param expectedByteArrayLengths DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  protected static void checkByteArrayLengths( 
    byte[][] byteArrays, int[] expectedByteArrayLengths )
    throws Exception {
    if( byteArrays.length != expectedByteArrayLengths.length ) {
      throw new Exception( "Unexpected number of byte arrays" );
    }

    for( int i = 0; i < byteArrays.length; i++ ) {
      if( byteArrays[i].length != expectedByteArrayLengths[i] ) {
        throw new Exception( "Unexpected byte array length" );
      }
    }

    System.out.println( "Test OK." );
  }

  /**
   * DOCUMENT ME!
   *
   * @param classes DOCUMENT ME!
   * @param expectedClassNames DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  protected static void checkClassNames( 
    Class[] classes, String[] expectedClassNames ) throws Exception {
    if( classes.length != expectedClassNames.length ) {
      throw new Exception( "Unexpected number of classes" );
    }

    String[] generatedClassNames = new String[classes.length];

    for( int i = 0; i < classes.length; i++ ) {
      generatedClassNames[i] = classes[i].getName(  );
    }

    sort( generatedClassNames );
    sort( expectedClassNames );

    for( int i = 0; i < classes.length; i++ ) {
      if( !generatedClassNames[i].equals( expectedClassNames[i] ) ) {
        throw new Exception( "Unexpected class" );
      }
    }

    System.out.println( "Test OK." );
  }

  /**
   * DOCUMENT ME!
   *
   * @param files DOCUMENT ME!
   * @param expectedFileLengths DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  protected static void checkFileLengths( 
    File[] files, int[] expectedFileLengths ) throws Exception {
    if( files.length != expectedFileLengths.length ) {
      throw new Exception( "Unexpected number of files" );
    }

    for( int i = 0; i < files.length; i++ ) {
      if( files[i].length(  ) != expectedFileLengths[i] ) {
        throw new Exception( "Unexpected file length" );
      }
    }

    System.out.println( "Test OK." );
  }

  /**
   * DOCUMENT ME!
   *
   * @param byteArrays DOCUMENT ME!
   */
  protected static void printBytes( byte[][] byteArrays ) {
    System.out.println( "Byte arrays:\n" );

    for( int i = 0; i < byteArrays.length; i++ ) {
      System.out.println( "Array length: " + byteArrays[i].length );

      for( int j = 0; j < byteArrays[i].length; j++ ) {
        System.out.print( byteArrays[i][j] );
        System.out.print( " " );
      }

      System.out.println(  );
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param classes DOCUMENT ME!
   */
  protected static void printClassNames( Class[] classes ) {
    System.out.println( "Classes returned:\n" );

    for( int i = 0; i < classes.length; i++ ) {
      System.out.println( "   " + classes[i].getName(  ) );
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param strings DOCUMENT ME!
   */
  protected static void sort( String[] strings ) {
    int noStrings = strings.length;

    for( int i = 0; i < ( noStrings - 1 ); i++ ) {
      for( int j = 0; j < ( noStrings - i - 1 ); j++ ) {
        if( strings[j].compareTo( strings[j + 1] ) > 0 ) {
          String temp = strings[j + 1];

          strings[j + 1]   = strings[j];
          strings[j]       = temp;
        }
      }
    }
  }
}
