<!--
  File:  OperatorResult.jsp
 
  Copyright (C) 2003, Chris M. Bouzek
 
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 
  Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
            Department of Mathematics, Statistics and Computer Science
            University of Wisconsin-Stout
            Menomonie, WI 54751, USA
 
            Chris M. Bouzek <coldfusion78@yahoo.com>
 
  This work was supported by the National Science Foundation under grant
  number DMR-0218882 and by the Intense Pulsed Neutron Source Division
  of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 
  For further information, see <http://www.pns.anl.gov/ISAW/>
 
  Modified:
 
   $Log$
   Revision 1.1  2004/02/13 02:59:58  bouzekc
   Added to CVS.

 -->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<!--    
-->
<HTML>
<HEAD>
  <TITLE>
    
  </TITLE>
</HEAD>
<!-- imports to run Operators -->
<%@ page import="Command.*" %>
<%@ page import="DataSetTools.operator.*" %>
<%@ page import="DataSetTools.parameter.*" %>
<%@ page import="DataSetTools.util.*" %>
<% Operator op = ( Operator )request.getSession(  ).getAttribute( 
                  "OperatorByCommandName" ); %>
<H2 ALIGN="CENTER">Results of&nbsp; <%= op.getCommand(  ) %>
</H2>
<br><br>
<% 
  StringBuffer s = new StringBuffer(  );
  String paramName = "";
  IParameter param = null;
  Object value = null;

  if( op != null ) {
    for( int i = 0; i < op.getNum_parameters(  ); i++ ) {
      paramName = "param" + op.getParameter( i ).getName(  );
      value = request.getParameter( paramName );
      param = op.getParameter( i );

      //directory resolution
      if( param instanceof BrowsePG ) {
        value = SharedData.getProperty( "TOMCAT_ISAW" ) +
                "/SharedRuns/" + value;
      }
        
      param.setValue( value );
    }
    out.print( op.getResult(  ) );
  } else {
    out.println( "ERROR: Operator is null" );
  }
  out.flush(  );
%>
</BODY>
</HTML>
