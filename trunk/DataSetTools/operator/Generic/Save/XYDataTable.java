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
 * Revision 1.5  2003/09/18 19:45:57  rmikk
 * -added parameters to give channel number instead of the
 *   xvalues
 *
 * Revision 1.4  2003/09/08 17:15:08  rmikk
 * Changed to use the new ParameterGUI's where return values
 *   must be of the proper data type.
 *
 * Revision 1.3  2003/02/03 18:29:38  dennis
 * Added getDocumentation() method. (Joshua Olson)
 *
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
import DataSetTools.parameter.*;
/** This class creates a operator that produces a table of x vs y vs errors.
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
    *@param SelectedGroups The INDICIES of the groups to be viewed
    *@param order- code for dimension.  Use default use HGT,F 
    *@param  useChanvsX- to give channel numbers in place of x values
    */    
    public XYDataTable( DataSet DS, boolean showErrors , MediaList outputMedia,
                DataDirectoryString filename, IntListString SelectedGroups,
                String order , boolean useChanvsX)
      { super( "Table x,y, error");
        parameters = new Vector();
        addParameter( new Parameter( "Data Set", DS ));
        addParameter( new Parameter("Show Errors ", new Boolean(showErrors) ));
        addParameter( new Parameter( "Output:", 
                      new MediaList( "Console" )));
        addParameter( new Parameter("filename ", filename));
        addParameter( new IntArrayPG("Selected Group indices", 
                                    SelectedGroups));
        addParameter( new Parameter("order ", order));
        addParameter( new Parameter("Use Chan not X", new Boolean( useChanvsX)));
                                 
      }


    public void setDefaultParameters()
     {parameters = new Vector();
        addParameter( new Parameter( "Data Set", new DataSet("","") ));
        addParameter( new Parameter("Show Errors ", new Boolean( true ) ));
        addParameter( new Parameter( "Output", new MediaList("Console")));
        addParameter( new Parameter("filename ",new String()));
        addParameter( new IntArrayPG("Selected Group indices", 
                                    ("1,3:8")));
        addParameter( new Parameter("order ", "HGT,F"));
        addParameter( new Parameter("Use Chan not X", new Boolean( false)));
                                 
    }
    
 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This class creates an operator that produces a ");
    s.append("table of x vs y vs errors.");
    
    s.append("@assumptions There exist both x and y values.");
    s.append("\n The specified filename either does not exist, or it is ");
    s.append("acceptable to overwrite it.\n");
                                                                              //
    s.append("@algorithm The MediaList variable called 'outputMedia' ");
    s.append("determines what type the output will be (either Console, ");
    s.append("File, or Table).  Then the program makes sure that both x ");
    s.append("and y values exist.  If either does not, then errors are ");
    s.append("generated, and information is given about these errors.  ");
    s.append("Otherwise the program then creates the table. ");
    
           
    
    s.append("@param DS The data set that is to be viewed as a table");  
    s.append("@param showErrors Determines whether or not a third column ");
    s.append("of errors will be viewed");  
    s.append("@param outputMedia The output can be Console, File, or Table");  
    s.append("@param filename The file where the File View sends the 'view'");  
    s.append("@param SelectedGroups The INDICIES of the groups to be viewed"); 

    s.append("@param order- code for dimension.  Use default use HGT,F to get only one ");
    s.append("column of yvalues and errors. HT,GF gives one column of yvalues per group");
    s.append("@param  useChanvsX- to give channel numbers in place of x values"); 
    
    s.append("@return The string 'Finished' is returned when there are ");
    s.append("no errors.  Otherwise error message(s) will be returned.  ");
    s.append("(These error messages are discussed in the 'Errors' section.)");
    
    s.append("@error Returns an error if there are no x values and/or ");
    s.append("y values.");
    s.append("@error Also returns an error if the boolean showErrors is ");
    s.append("true, but there are no errors to show.");

    return s.toString();
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
     String output = ((getParameter(2).getValue())).toString();
     String filename = getParameter(3).getValue().toString();
     
     int[] SelGroups =( (IntArrayPG)getParameter(4)).getArrayValue();
     String order = getParameter(5).getValue().toString();
     boolean useChan = ((Boolean)(getParameter(6).getValue())).booleanValue();
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
     String ChanX = "X values";
     if( useChan)
         ChanX = "XY index";
     if( TB.getFieldInfo(DS,ChanX) == null)
       { return new ErrorString("No such field "+ChanX);
        }
     sel.addElement( TB.getFieldInfo(DS,ChanX));
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
     TB.Showw( DSS , sel , order , false, (SelGroups) );
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















