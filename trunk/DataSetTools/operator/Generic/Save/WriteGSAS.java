/*
 * File:  gsas.java 
 *
 * Copyright (C) 2001,/Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2002/05/17 22:20:20  pfpeterson
 * Added checkbox for exporting monitor spectrum. The integrated
 * monitor count is still included in the file.
 *
 * Revision 1.1  2002/02/22 20:58:15  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.5  2002/01/14 20:28:49  pfpeterson
 * Modified to use writer interface for GSAS files
 *
 * Revision 1.4  2001/11/09 19:27:52  dennis
 * Passes in a null monitor DataSet to gsas_filemaker, since the
 * gsas_filemaker was changed to require a monitor DataSet.
 *
 * Revision 1.3  2001/11/09 15:59:01  dennis
 * Fixed minor error in documentation.
 *
 * Revision 1.2  2001/08/14 20:26:48  dennis
 * Changed title and command
 *
 * Revision 1.1  2001/08/14 19:58:52  dennis
 * Renamed from gsas.java
 *
 * Revision 1.1  2001/08/07 15:43:47  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.operator.Generic.Save;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.writer.*;
import DataSetTools.gsastools.*;
import java.util.*;

/** This is an operator shell around the Save gsas File menu option in ISAW.
 *  The Title in Menu's that refers to this is <B>Save As gsas</b>.<BR>
 *  The Command in Scripts used to refer to this operation is <B>gsasOut</b>.
 */
public class WriteGSAS extends GenericSave
{

   public WriteGSAS()
    {
     super( "Save as GSAS " );
     setDefaultParameters();
    }

   /** 
    *@param DS  The data set that is to be saved in gsas format
    *@param filename the name of the file where the data will be saved
    */
   public WriteGSAS( DataSet MS, DataSet DS, String filename, Boolean em )
    {
     super( "Save as GSAS File" );
     parameters = new Vector();
     addParameter( new Parameter("Monitor" , MS ));
     addParameter( new Parameter("Data Set" , DS ));
     addParameter( new Parameter("Output File", filename ));
     addParameter( new Parameter("Export Monitor", em));
    }

   public void setDefaultParameters()
    {
     parameters = new Vector();
     addParameter( new Parameter("Monitor" , new DataSet("","") ));
     addParameter( new Parameter("Data Set" , new DataSet("","") ));
     addParameter( new Parameter("Output File", "filename"));
     addParameter( new Parameter("Export Monitor", Boolean.TRUE));
    }  
   
  /** 
   * Returns <B>SaveGSAS</b>, the command used by scripts to refer to this
   * operation
   */ 
   public String getCommand()
    {
      return "SaveGSAS";
    }

  /** 
   * executes the gsas command, saving the data to the file in gsas form.
   *
   * @return  "Success" only
   */
   public Object getResult()
    { 
      DataSet MS       =(DataSet)( getParameter(0).getValue());
      DataSet DS       =(DataSet)( getParameter(1).getValue());
      String  filename =(String) ( getParameter(2).getValue());
      boolean em       =((Boolean)(getParameter(3).getValue())).booleanValue();

      //System.out.println("(WG)EXPORT MONITOR: "+em);
      GsasWriter gw=new GsasWriter(filename,em);
      gw.writeDataSets(new DataSet[] {MS , DS});

      return "Success";
    }

  /** 
   * Creates a clone of this operator.
   */
   public Object clone()
   {
     WriteGSAS W = new WriteGSAS();
     W.CopyParametersFrom( this );
     return W;
   }

   public static void main( String args[])
   {
     System.out.println("WriteGSAS test... operator compiled and can run");
   }
  
}
