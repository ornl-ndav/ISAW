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
 * Header: /user/rsch/graham/java/packages/compiler/version1.2/RCS/CompilationException.java,v 1.3 1998/11/06 10:37:03 graham Exp
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
 * Revision 1.1  2004/01/28 21:53:46  bouzekc
 * Added to CVS.  Changed package to ExtTools.compiler, moved original log,
 * changed at symbol in email in author tag to "_at_".  Formatted code.
 *
 */
package ExtTools.compiler;

/**
 * Signals an error during dynamic compilation.
 * 
 * <P>
 * The <A HREF="../../compiler/CompilationException.java">source code</A> is
 * available.
 * </p>
 *
 * @author Graham Kirby (<A
 *         HREF="mailto:graham@dcs.st-and.ac.uk">graham_at_dcs.st-and.ac.uk</A>)
 * @version 1.2 2-Nov-98
 */
public class CompilationException extends Exception {
  //~ Constructors *************************************************************

  /**
   * Constructs a CompilationException with the specified detail message. A
   * detail message is a String that describes this particular exception.
   *
   * @param s the detail message
   */
  public CompilationException( String s ) {
    super( s );
  }
}
