/*
 * File:  HttpServletUtilities.java
 *
 * Copyright (C) 2003, Chris M. Bouzek
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
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 *           Chris M. Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882 and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2004/02/13 02:34:50  bouzekc
 *  Added to CVS.
 *
 */
package DataSetTools.servlets;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * This is a class designed to ease the use of servlet and HTML technology by
 * providing several utility methods for page redirection and formatting.
 *
 * @author Chris M. Bouzek
 */
public class HttpServletUtilities {
  //~ Constructors *************************************************************

  /**
   * Static method only class.  Do not instantiate.
   */
  private HttpServletUtilities(  ) {}

  //~ Methods ******************************************************************

  /**
   * Builds the standard HTML head for a page.
   *
   * @param title The page title.
   */
  public static String buildHead( String title ) {
    StringBuffer s = new StringBuffer(  );
    s.append( "<!doctype html public \"-//w3c//dtd html 4.0 " );
    s.append( "transitional//en\" \"http://www.w3.org/TR/REC-html40/" );
    s.append( "strict.dtd\">\n" );
    s.append( "<!-- Copyright 2003 Chris M. Bouzek -->\n\n" );
    s.append( "<html>\n  <head>\n" );
    s.append( "  <meta http-equiv=\"Content-Type\" content=\"text/html;" );
    s.append( "charset=iso-8859-1\">\n" );
    s.append( "    <title>" );
    s.append( title );
    s.append( "</title>\n  </head>\n  <body>\n " );

    return s.toString(  );
  }

  /**
   * Uses the servlet, address, request, and response to redirect to a web
   * page.  Uses the HttpServletContext() to retain relative paths.  This will
   * only work with servers supporting JSP 2.2 or higher.
   *
   * @param address The address to redirect to.
   * @param request The original request.
   * @param response The original response.
   */
  public static void gotoPage( 
    String address, HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
    RequestDispatcher dispatcher = request.getRequestDispatcher( address );
    dispatcher.forward( request, response );
  }
}
