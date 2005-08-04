<!--
  File:  CreateOperator.jsp
 
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
   Revision 1.1  2004/02/13 02:59:57  bouzekc
   Added to CVS.

 -->
 
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<!--    
-->
<HTML>
<% String commandName = request.getParameter( "commandName" ); %>
<HEAD>
  <TITLE>
    <%= commandName %>
  </TITLE>
</HEAD>
<!-- imports to run Operators -->
<%@ page import="Command.*" %>
<%@ page import="DataSetTools.operator.*" %>
<%@ page import="DataSetTools.util.*" %>
<H2 ALIGN="CENTER">Operator&nbsp;
<%= commandName %>
</H2>
<br>
  <FORM ACTION="OperatorResult.jsp" METHOD="GET"><br>
Enter Parameters:<br><br>
<% 
  StringBuffer s = new StringBuffer(  );
  Script_Class_List_Handler handler = new Script_Class_List_Handler(  );

  Operator op = handler.getOperator( commandName );

  if( op != null ) {
    String name = null;

    for( int i = 0; i < op.getNum_parameters(  ); i++ ) {
      name = op.getParameter( i )
               .getName(  );
      s.append( name );
      s.append( ": <INPUT TYPE=\"TEXT\" NAME=\"param" );
      s.append( name );
      s.append( "\"><br>\n" );
    }
    s.append( "<br>" );
    s.append( "<INPUT TYPE=\"SUBMIT\" NAME=\"getResult\" " );
    s.append( "\"VALUE=\"Get Result\">\n " );
  } else {
    s.append( "<b>Operator " + commandName + " not found.</b><br>" );
  }

  out.print( s.toString(  ) );
  out.flush(  );

  //bind the Operator so we can use it later
  request.getSession(  ).setAttribute( "OperatorByCommandName", op );
%>
</BODY>
</HTML>
