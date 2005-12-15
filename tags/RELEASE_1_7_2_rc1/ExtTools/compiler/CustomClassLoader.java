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
 * Header: /user/rsch/graham/java/packages/compiler/version1.2/RCS/CustomClassLoader.java,v 1.1 1998/11/06 10:37:03 graham Exp
 *
 * Revision 1.1  1998/11/06 10:37:03  graham
 * Initial revision
 *
 * Revision 1.2  1998/07/17 09:00:00  graham
 * Supports compiling to files, bytes or classes.
 *
 * Revision 1.1  1998/01/07 10:00:00  graham
 * Initial revision
 *
 * $Log$
 * Revision 1.1  2004/01/28 21:53:46  bouzekc
 * Added to CVS.  Changed package to ExtTools.compiler, moved original log,
 * changed at symbol in email in author tag to "_at_".  Formatted code.
 *
 */
package ExtTools.compiler;

import java.util.*;


/**
 * Loads classes using a specified <A
 * HREF="ClassByteLoader.html">ClassByteLoader</A> instance to map class names
 * to the corresponding bytes.
 * 
 * <P>
 * The <A HREF="../../compiler/CustomClassLoader.java">source code</A> is
 * available.
 * </p>
 *
 * @author Graham Kirby (<A
 *         HREF="mailto:graham@dcs.st-and.ac.uk">graham_at_dcs.st-and.ac.uk</A>)
 * @version 1.2 2-Nov-98
 */
public class CustomClassLoader extends ClassLoader {
  //~ Instance fields **********************************************************

  /** Hash table memorising loaded classes. */
  private Hashtable hashtable;

  /** Object used to map class names to byte codes. */
  private ClassByteLoader classByteLoader;

  //~ Constructors *************************************************************

  /**
   * Custom class loader. It attempts to load classes by trying the following
   * steps in turn:
   * 
   * <P>
   * 
   * <OL>
   * <li>
   * looks for a version already loaded by the VM
   * </li>
   * <li>
   * looks for a version already loaded by this class loader
   * </li>
   * <li>
   * delegates to the specified <A
   * HREF="ClassByteLoader.html">ClassByteLoader</A> instance, throwing an
   * exception if that fails
   * </li>
   * </ol>
   * </p>
   *
   * @param classByteLoader used to map class names to byte codes
   */
  public CustomClassLoader( ClassByteLoader classByteLoader ) {
    this.classByteLoader   = classByteLoader;
    hashtable              = new Hashtable(  );
  }

  //~ Methods ******************************************************************

  /**
   * Attempts to load the named class.
   *
   * @param name the name of the class to be loaded
   * @param resolve flag specifying whether referenced classes should be
   *        resolved
   *
   * @return the loaded class
   *
   * @exception ClassNotFoundException if the class cannot be loaded
   */
  protected Class loadClass( String name, boolean resolve )
    throws ClassNotFoundException {
    Class resultClass;

    try {
      // Attempt to access an already loaded version of the class.
      resultClass = findSystemClass( name );
    } catch( Exception e1 ) {
      // Check whether the class has been loaded previously by this class loader.
      Object lookup = hashtable.get( name );

      // If so, return the class from the hash table.
      if( lookup != null ) {
        resultClass = ( Class )lookup;
      } else {
        try {
          // Read the bytes from the appropriate .class file.
          byte[] classBytes = classByteLoader.get( name );

          // Throw exception if the class byte loader failed to load the bytes.
          if( classBytes == null ) {
            throw new ClassNotFoundException( 
              "ClassByteLoader.get returned null for class " + name );
          }

          // Convert the array to a class.
          resultClass = defineClass( name, classBytes, 0, classBytes.length );
        } catch( ClassFormatError e2 ) {
          throw new ClassNotFoundException( 
            "format of class file incorrect for class " + name + ": " +
            e2.getMessage(  ) );
        }
      }
    }

    // Resolve the class if necessary.
    if( resolve ) {
      resolveClass( resultClass );
    }

    // Memorise the class for any further loading attempts.
    hashtable.put( name, resultClass );

    return resultClass;
  }
}
