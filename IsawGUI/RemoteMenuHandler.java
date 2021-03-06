/*
 * File:  RemoteMenuHandler.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.8  2004/03/15 03:31:26  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.7  2004/01/24 23:09:39  bouzekc
 * Removed unused imports.
 *
 * Revision 1.6  2003/10/15 03:18:31  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.5  2003/05/28 18:58:20  pfpeterson
 * Changed System.getProperty to SharedData.getProperty
 *
 * Revision 1.4  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/02/22 20:39:14  pfpeterson
 * Operator reorganization.
 *
*/
package IsawGUI;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import java.awt.event.*;
import DataSetTools.util.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Load.*;
import javax.swing.text.*;
/** This class is the ActionListener for the menu item that invokes the
*  LoadRemoteData operator.  This operator gets datasets from remote
*  sources.
*/
public class RemoteMenuHandler implements ActionListener
{ IObserver iobs;
  Document sessionLog;
  /** 
  *@param iobs  the observer who will receive the resultant data sets
  */
  public RemoteMenuHandler( IObserver iobs , Document sessionLog)
    { this.iobs = iobs;
      this.sessionLog = sessionLog;
    }

  /** 
   * This method is invoked when a menu item corresponding to the 
   * LoadRemote operator is invoked.  
   *
   * USES:<ul>
   * <li> System properties IsawFileServer1_Name, IsawGileServer1_Path,
   *         IsawFileServer2_Name, ...  NDSFileServer1_Name, 
   *         NDSFileServer1_Path</li>
   * <li> Several Server types are supported: Live Data Server, Isaw Data
   *       Server and the NDS Server so far</li>
   *  <li> Default port numbers are used corresponding to the server Type
   *  </li>
   *  </ul>
   */
  public void actionPerformed( ActionEvent ev ) 
    {   
        
        if( !Process("Isaw",6088,ServerTypeString.ISAW_FILE_SERVER,
                     iobs, ev.getActionCommand()))
	    Process("NDS", 6008, ServerTypeString.NDS_FILE_SERVER , iobs,
                 ev.getActionCommand());
    }

   private boolean Process( String serverBase, int defaultPort,
            String ServerType, IObserver iobs, String Action )              
	{String ServerName, ServerPath;
         boolean done=false;
         int i=1;
          while( !done)
	    { ServerName = SharedData.getProperty( serverBase+ "FileServer"+
                                  i+"_Name");
              
	      if( ServerName == null )
                 done = true;
              else if( Action.equals( ServerName ))
                { ServerPath = SharedData.getProperty(
                     serverBase + "FileServer"+i+"_Path");
                  if( ServerPath == null)
                     done = true;
                  else
                  {int k = ServerPath.lastIndexOf(";");
                                      int port = defaultPort;
                   if( k >= 0)
                   try{
                      port = (new Integer( ServerPath.substring( k + 1 ))).
                            intValue();
                     }
                   catch( Exception u){port = defaultPort;}
                 
                   if( k >= 0)
                    ServerPath= ServerPath.substring( 0, k );
                  
                   String userName = SharedData.getProperty( "user.name" );
                   Operator op = new LoadRemoteData( ServerPath, port,
                         userName,"IPNS","",
                       new ServerTypeString(ServerType ));
		 /* MnDSOperator op1 = new MnDSOperator( op, 0, ServerPath);
                 MnDSOperator op2 = new MnDSOperator( op1, 0, 
                                              new Integer(port));
                 MnDSOperator op3 = new MnDSOperator( op2, 0, userName);
                 MnDSOperator op4 = new MnDSOperator( op3, 2, 
                       new ServerTypeString(
                           ServerTypeString.ISAW_FILE_SERVER ));
		 */
                 JParametersDialog JP = new JParametersDialog( op,
                      null, sessionLog, iobs);
                  return true;
                }
                }
              else i++;


            }
        return false;
  
    }
/** Test program for methods in this class
*/
public static void main( String args[])
 {System.setProperty("NDSFileServer1_Name","dmikk");
  System.setProperty("NDSFileServer1_Path","dmikk.mscs.uwstout.edu");
  RemoteMenuHandler Rh = new RemoteMenuHandler( null, null );
  Rh.actionPerformed( new ActionEvent( new Integer(5),5,"dmikk"));

 }
}
