/*
 * File:  XYDataTable.java 
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
 * Revision 1.2  2002/11/27 23:21:28  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/02/22 20:58:17  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.3  2002/02/11 21:44:24  rmikk
 * Altered to reflect changes in table_view
 *
 */
package DataSetTools.operator.Generic.Save;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;
import DataSetTools.viewer.Table.*;
import DataSetTools.util.*;
import java.util.*;
import DataSetTools.util.*;
import  javax.swing.*;

/** This class creates a operator the produces a table of x vs y vs errors.
 *  The table can be sent to the console, table, or file<P>
 * The Title is <B>Table x, y, error</b>. This represents this operator on
 *     menu items.<BR>
 * The Command name is <B>Table</b>. This represents this operator in the
 *    Command Pane.
*/
public class XYDataTable  extends GenericSave
{  
    public XYDataTable( )
     { super( "Table x, y, error");
       setDefaultParameters();
     }
    
    /**
    *@param DS The data Set that is to be viewed as a table
    *@param showErrors  A third column of errors will be viewed
    *@param outputMedia  The output can be Console, File, or Table
    *@param filename   The file where the File View sends the "view"
    *@param SelectedGroups The INDECIES of the groups to be viewed
    */    
    public XYDataTable( DataSet DS, boolean showErrors , MediaList outputMedia,
                DataDirectoryString filename, IntListString SelectedGroups )
      { super( "Table x,y, error");
        parameters = new Vector();
        addParameter( new Parameter( "Data Set", DS ));
        addParameter( new Parameter("Show Errors ", new Boolean(showErrors) ));
        addParameter( new Parameter( "Output:", 
                      new MediaList( "Console" )));
        addParameter( new Parameter("filename ", filename));
        addParameter( new Parameter("Selected Group indices", 
                                    SelectedGroups));
                                 
      }


    public void setDefaultParameters()
     {parameters = new Vector();
        addParameter( new Parameter( "Data Set", new DataSet("","") ));
        addParameter( new Parameter("Show Errors ", new Boolean( true ) ));
        addParameter( new Parameter( "Output", new MediaList("Console")));
        addParameter( new Parameter("filename ",new String()));
        addParameter( new Parameter("Selected Group indices", 
                                    new IntListString("1,3:8")));
    }

   /** Returns <B>Table</b>, the name used to refer to this operator in Scripts
   */ 
   public String getCommand()
     {return "Table";
     }

   /** The "Result" here is a table view. 
   * @return "Finished" if there were no errors, otherwise an error message 
   * NOTE: Some error messages may appear in the command window if no view
   *  appears.
   */ 
   public Object getResult()
    { DataSet DS = (DataSet)(getParameter( 0 ).getValue());
      boolean showerrors = ((Boolean)(getParameter(1).getValue())).
                        booleanValue();
     String output = ((MediaList)(getParameter(2).getValue())).toString();
     String filename = getParameter(3).getValue().toString();
     IntListString SelGroups = (IntListString)(getParameter(4).getValue());
     int mode = 0;
     //System.out.println("output="+output);
     if( output .equals("Console"))
         mode = 0;
     else if( output.equals("File"))
         mode = 1;
     else if( output.equals("Table"))
         mode = 2;
   
     if( (mode < 0) || (mode >2))
       mode = 0;
     table_view TB = new table_view( mode );
     TB.setFileName( filename );
     //DataSetOperator op = new SetField( DS,
     //			new DSSettableFieldString( 
     //                   DSFieldString.SELECTED_GROUPS), SelGroups);
     //op.getResult();
     //String Used[];
     DefaultListModel sel = new DefaultListModel();
     if( TB.getFieldInfo(DS,"X values") == null)
       { return new ErrorString("No such field x");
        }
     sel.addElement( TB.getFieldInfo(DS,"X values"));
     if(TB.getFieldInfo(DS,"Y values")==null)
       return new ErrorString("No such Field y");
     sel.addElement(TB.getFieldInfo(DS,"Y values"));
     if( showerrors )
        {if(TB.getFieldInfo(DS, "Error values")==null)
           return new ErrorString(" No such fieldinfo e");
         sel.addElement( TB.getFieldInfo(DS, "Error values"));
        }
     
     
     
     
     DataSet DSS[];
     DSS = new DataSet[1];
     DSS[0] = DS;
     TB.Showw( DSS , sel , "HGT,F" , false, IntList.ToArray(SelGroups.toString()) );
     return "Finished";
    }

  /** clones this operator
  */
  public Object clone()
   {XYDataTable Res = new XYDataTable();
    Res.CopyParametersFrom( this );
   return Res;
   }

/** Test program to determine if Classpaths, etc. are set up correctly
*/
public static void main( String args[])
  {System.out.println("XYDataTable");
   String Used[];
   int used[];
   Used = new String[3];
   Used[2] = "error";
   Used[0] = "xval";
   Used[1] = "yval";
   
  }
}















