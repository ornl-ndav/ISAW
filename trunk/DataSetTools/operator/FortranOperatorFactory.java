/*
 * File:  FortranOperatorFactory.java
 *
 * Copyright (C) 2004 Chris Bouzek
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.3  2004/01/28 22:19:53  bouzekc
 * Removed code to set the temporary compile directory, as it was no longer
 * needed.  Added to the main() test method.
 *
 * Revision 1.2  2004/01/28 22:12:26  bouzekc
 * Reformatted code.
 *
 * Revision 1.1  2004/01/28 22:11:43  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.operator;

import Command.JavaCC.Fortran.*;

import DataSetTools.util.*;

import ExtTools.compiler.*;

import java.io.*;


/**
 * This class represents a Factory that can be used to create ISAW operators
 * from a subset of the Fortran language.  This is a singleton class, and thus
 * cannot be instantiated.  There is one extremely important rule:  If you
 * want the  Operators to show up in the menu correctly, your ISAW
 * installation directory should be similar to the stock install: i.e a
 * directory starting with "ISAW"  and the requisite directories of classes
 * immediately below these.   Like the JavaWrapperOperators, these Fortran
 * code sets must follow these rules:<br><br>
 * 
 * <ul>
 * <li>
 * Only one function is allowed.  It must be called calculate, and it may not
 * have any arguments.
 * </li>
 * <li>
 * For functions that need arguments, global variables should be used.
 * </li>
 * </ul>
 * 
 * No complex variables no  operator no single implicit variable initialization
 * (e.g. creating an implicit integer and initializing it like: imyint = 1
 * )...this is not a compiler, and I do not keep track of  variables.  If I
 * allow implicit variable initialization, then there is no way to do
 * assignments.  It is allowed if there is more than one variable  (e.g.
 * myvar1 = 1, myvar2 = 2). no implicit type conversion yet no
 * multidimensional character arrays -------------------- IF statements
 * -------------------- single line if statements must truly be on a single
 * line, like: if (this is true) do this NOT if(this is true) do this 'else'
 * statements are their own line, however: if (...) then ... elseif( ... )
 * then ... else ... if creating a multiple line if, there can be no single
 * line if statements.  For example this is invalid: if( x .ge. 5 ) x = 5 + 7
 * elseif( tr .ge. 7 ) d = 9 else d = 5 + 7 -------------------- LOOPS
 * -------------------- All labels for loops must be numbers The step, if
 * given, for a loop must be an actual number, not a variable
 */
public class FortranOperatorFactory {
  //~ Constructors *************************************************************

  /**
   * DO NOT make this constructor public.  This is a factory, and should not be
   * instantiated.
   */
  private FortranOperatorFactory(  ) {
    super(  );
  }

  //~ Methods ******************************************************************

  /**
   * Returns a JavaWrapperOperator that was created by converting Fortran code
   * to a Wrappable class.  If there are errors in parsing or reading the
   * file, this will return null.
   *
   * @param source The filename of the file holding the Fortran code.
   */
  public static Operator getInstance( String source ) {
    Wrappable wrapped = createFortranWrapped( source );

    if( wrapped == null ) {
      return null;
    }

    return new JavaWrapperOperator( wrapped );
  }

  /**
   * Creates the package name for a given File by chopping off everything
   * after the ISAW.../ things.  This assumes that ISAW resides in a directory
   * starting with "ISAW" and that the requisite class file directories exist
   * underneath it without an extra layers (similar to the stock
   * distribution).
   *
   * @param file The File that contains the absolute fully qualified path of
   *        the file on the filesystem.
   *
   * @return A String representing the File's parent path, delimited by
   *         periods, without the ISAW and previous part of the File's path.
   */
  public static String createPackageName( File file ) {
    String folderPath = FilenameUtil.setForwardSlash( file.getParent(  ) );

    //chop off the ISAW.... stuff
    int ISAWIndex  = folderPath.indexOf( "ISAW" );
    int slashIndex = folderPath.indexOf( "/", ISAWIndex );

    //we are going to create the package name here
    folderPath     = ( folderPath.substring( 
        slashIndex + 1, folderPath.length(  ) ) ).replaceAll( "/", "." );

    return folderPath;
  }

  /**
   * Testbed.
   *
   * @param args unused.
   */
  public static void main( String[] args ) {
    Operator myop = FortranOperatorFactory.getInstance( 
      "/home/students/bouzekc/ISAW/Operators/MyFortran.f" );
    System.out.println( myop.getResult(  ) );
  }

  /**
   * Creates a Wrappable Object from the Fortran code in the given file. If
   * there are errors in parsing or reading, this will return null.
   *
   * @param fileName The name of the file holding the Fortran code.
   *
   * @return The Wrappable Object that was created.
   */
  private static Wrappable createFortranWrapped( String fileName ) {
    String code         = createWrappedCodeFromFortran( fileName );
    Wrappable myWrapped = null;

    try {
      DynamicCompiler compiler = new DynamicCompiler(  );

      //doesn't matter where we run from, always use the ISAW_HOME as the root 
      //for the compiled classes
      compiler.setProperty( 
        "userDirPath", SharedData.getProperty( "ISAW_HOME" ) );
      //compiler.setProperty( "tempDirNameRoot", "" );
      myWrapped = ( Wrappable )( compiler.compileClass( code ).newInstance(  ) );
    } catch( Exception iae ) {
      SharedData.addmsg( "Unable to compile converted Java class." );
    }

    return myWrapped;
  }

  /**
   * Takes the Fortran code in the file referenced by fileName and generates
   * Wrappable Java code based on the Fortran code.
   *
   * @param fileName The name of the file holding the Fortran code.
   *
   * @return The converted Fortran code.
   */
  private static String createWrappedCodeFromFortran( String fileName ) {
    //we should probably have Script_Class_List_Handler look for the 
    //Fortran file so a fully qualified name doesn't always have to be
    //given
    File fortranFile = new File( fileName );
    String shortName = fortranFile.getName(  ).split( "\\." )[0];

    //the trick here is that we can use the built-in reflection of 
    //JavaWrapperOperator to create a category list by putting in a 
    //package name
    StringBuffer jCode = new StringBuffer( "package " );

    jCode.append( createPackageName( fortranFile ) );
    jCode.append( ";\n" );
    jCode.append( "import DataSetTools.operator.Wrappable;\n" );
    jCode.append( "public class " );

    //this is just the short name of the file
    jCode.append( shortName );
    jCode.append( " implements Wrappable {\n" );
    jCode.append( "  public String getCommand(  ) {\n" );
    jCode.append( "    return \"" );

    //this gives us the name of the file in upper case without the extension
    jCode.append( shortName.toUpperCase(  ) );
    jCode.append( "\";\n  }\n" );
    jCode.append( "  public String getDocumentation(  ) {\n" );
    jCode.append( "    return null;\n  }\n" );

    TextFileReader tfr = null;

    try {
      tfr = new TextFileReader( fortranFile.toString(  ) );

      //read each line, converting it to Java.  The parser automatically
      //puts in newline characters for us.
      while( !tfr.eof(  ) ) {
        jCode.append( FortranParser.parseText( tfr.read_line(  ) ) );
      }
    } catch( Exception e ) {
      SharedData.addmsg( "Unable to parse file " + fortranFile.toString(  ) );

      return null;
    } finally {
      //clean up if we hit an exception
      if( tfr != null ) {
        try {
          tfr.close(  );
        } catch( IOException ioe2 ) {
          //drop it on the floor
        }
      }
    }

    //put the closing brace for the class on
    jCode.append( "}" );

    return jCode.toString(  );
  }
}
