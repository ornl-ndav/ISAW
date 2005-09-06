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
 * Revision 1.1  1998/11/06 10:37:03  graham
 * Initial revision
 * 
 * $Log$
 * Revision 1.2  2004/01/28 21:43:26  bouzekc
 * Moved original log messages.
 *
 * Revision 1.1  2004/01/28 21:42:32  bouzekc
 * Added to CVS.
 *
 *
 */
package ExtTools.compiler;

import java.io.*;

import java.util.*;


/**
 * Supports loading of class byte code from the file system.
 * 
 * <P>
 * The <A HREF="../../compiler/ClassByteFileLoader.java">source code</A> is
 * available.
 * </p>
 *
 * @author Graham Kirby (<A
 *         HREF="mailto:graham@dcs.st-and.ac.uk">graham_at_dcs.st-and.ac.uk</A>)
 * @version 1.2 2-Nov-98
 */
public class ClassByteFileLoader implements ClassByteLoader {
  //~ Instance fields **********************************************************

  private Vector classPathVector;
  private Hashtable properties;

  //~ Constructors *************************************************************

  /**
   * @param classPathVector vector of directories forming class path with index
   *        0 being searched first
   * @param properties hash table containing entries for various constants
   *        including "packageSeparator" and "compiledSuffix"
   */
  public ClassByteFileLoader( Vector classPathVector, Hashtable properties ) {
    this.classPathVector   = classPathVector;
    this.properties        = properties;
  }

  //~ Methods ******************************************************************

  /**
   * Finds the .class file corresponding to the given class name.
   *
   * @param fullClassName a full class name
   *
   * @return the file containing the byte code for the given class
   */
  public File findClassFile( String fullClassName ) {
    File classFile          = null;
    String packageSeparator = ( String )properties.get( "packageSeparator" );
    String compiledSuffix   = ( String )properties.get( "compiledSuffix" );
    Enumeration classDirs   = classPathVector.elements(  );

    while( classDirs.hasMoreElements(  ) ) {
      // Create tokenizer to extract the package name components.
      StringTokenizer st = new StringTokenizer( 
          fullClassName, packageSeparator );

      // Start at the root directory.
      File packageDir = ( File )classDirs.nextElement(  );

      // Get the first token.
      String s = st.nextToken(  );

      // Add the sub-directories for the package structure in turn, if any.
      while( st.hasMoreTokens(  ) ) {
        // Generate the name of the next directory.
        packageDir   = new File( packageDir, s );

        // Get the next token.
        s            = st.nextToken(  );
      }

      // Get the class file.
      classFile = new File( packageDir, s + compiledSuffix );

      // Return the class file if found.
      if( classFile.exists(  ) ) {
        break;
      }
    }

    return classFile;
  }

  /**
   * Finds the byte array corresponding to the given class name.
   *
   * @param name a fully qualified class name
   *
   * @return an array containing the byte code for the given class, or null if
   *         it is not found
   */
  public byte[] get( String name ) {
    byte[] classBytes = null;

    // Get the file containing the byte code.
    File classFile = findClassFile( name );

    try {
      // Create an input stream from the file.
      FileInputStream classFileInputStream = new FileInputStream( classFile );

      // Get the file length.
      int fileLength = classFileInputStream.available(  );

      // Create an array the same length as the file to hold the compiled class.
      classBytes = new byte[fileLength];

      // Read class definition into the array.
      classFileInputStream.read( classBytes );

      // Close the file.
      classFileInputStream.close(  );
    } catch( Exception e ) {}

    return classBytes;
  }
}
