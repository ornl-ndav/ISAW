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
 * Revision 1.1  2001/08/07 15:43:47  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.operator;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.gsastools.*;
import java.util.*;

/** This is an operator shell around the Save gsas File menu option in ISAW.
*  The Title in Menu's that refers to this is <B>Save As gsas</b>.<BR>
*  The Command in Scripts used to refer to this operation is <B>gsasOut</b>.
*/
public class gsas extends GenericSave
{

   public gsas()
     {super( "Save As gsas " );
      setDefaultParameters();
     }

    /** 
    *@param DS  The data set that is to be saved in gsas format
    *@param filename the name of the file where the data will be saved
    */
   public gsas( DataSet DS, String filename )
     { super( "Save As gsas " );
      parameters = new Vector();
      addParameter( new Parameter("DS=" , DS ));
      addParameter( new Parameter("filename=", filename ));

     }

   public void setDefaultParameters()
    {parameters = new Vector();
     addParameter( new Parameter("Data Set=" , new DataSet("","") ));
     addParameter( new Parameter("Output file name=", "filename"));
    }  
   
   /** Returns <B>gsasOut</b>, the command used by scripts to refer to this
  * operation
  */ 
   public String getCommand()
    {return "gsasOut";
    }

   /** executes the gsas command, saving the data to the file in gsas form.
   *@return  "Success" only
   */
   public Object getResult()
    { DataSet DS = (DataSet)( getParameter(0).getValue());
      String filename = (String) (getParameter(1).getValue());
       gsas_filemaker gsas_output = new gsas_filemaker( DS, filename );
      return "Success";
    }
/** Creates a clone of this operator.
*/
public Object clone()
  {gsas W = new gsas();
   W.CopyParametersFrom( this );
    return W;
  }
public static void main( String args[])
  {System.out.println("yyy");
  }
  
}




