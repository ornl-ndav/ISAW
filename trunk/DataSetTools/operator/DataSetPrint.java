/*
 * File:  DataSetPrint.java  
 * 
 * Copyright (C) 2000, Dongfeng Chen,
 *                     Dennis Mikkelson
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
 *  $Log$
 *  Revision 1.16  2001/08/10 18:40:53  rmikk
 *  Removed the System.exit commands and returned
 *  errormessages.
 *
 *  Revision 1.15  2001/07/23 15:42:39  dennis
 *  Now uses DataSet tile ( no tag ) for the filename to print to.
 *
 *  Revision 1.14  2001/07/23 15:34:46  dennis
 *  Added infomational messages to catch block for printing
 *  to file.
 *
 *  Revision 1.13  2001/07/20 18:17:00  dennis
 *  Made prompt for print destination more compact.
 *
 *  Revision 1.12  2001/07/16 22:49:50  dennis
 *  Modified it to work even if there are no error values present
 *  in the Data block.  Did some general "clean up".
 *
 *  Revision 1.11  2001/07/16 14:25:24  dennis
 *  Now extends GenericOperator, so it appears in the GUI
 *
 *  Revision 1.10  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.9  2001/04/26 19:06:42  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.8  2000/11/07 15:56:13  dennis
 *
 *  Revision 1.7  2000/10/03 22:13:10  dennis
 *
 *  Now uses the constant empty DataSet, DataSet.EMPTY_DATA_SET,
 *   as a place holder for the DataSet parameter.
 *
 *  Revision 1.6  2000/08/17 19:19:07  dennis
 *  Moved OutputTable from DataSetTools/operator to DataSetTools/components/ui
 *
 *  Revision 1.5  2000/08/17 19:08:28  dennis
 *  Modified 08/13 by Dongfeng.  Now prints output into JTable for viewing, 
 *  copying and pasting.  Changed the output format. All of the output types 
 *  can be pasted neatly.
 *
 *  Revision 1.4  2000/08/08 21:12:52  dennis
 *  Now prints error values and uses tabs & number format to make the
 *  dislay neater.
 *
 *  Revision 1.3  2000/08/03 22:10:23  dennis
 *  Now uses tabs as separators
 *
 *  Revision 1.2  2000/08/03 21:49:13  dennis
 *  Moved JFrameMessageCHOP to DataSetTools/components/ui
 *
 *  Revision 1.1  2000/08/03 21:43:40  dennis
 *  Dongfeng's utility for quick printing.
 *   
 */

package DataSetTools.operator;

import  java.io.*;
import  java.text.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.components.ui.*;

/**
 * This operator converts Print data information.
 * 
 */

public class DataSetPrint extends    GenericOperator 
                                     implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public DataSetPrint( )
  {
    super( "Print Data blocks" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   */

  public DataSetPrint( DataSet     ds,
                       int         index,
                       int         outputtype )
  {
    this();

    Parameter parameter = getParameter(0);
    parameter.setValue( ds );
    
    parameter = getParameter( 1 );
    parameter.setValue( new Integer( index ) );
    
    parameter = getParameter( 2 );
    parameter.setValue( new Integer( outputtype ) );
    
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return  the command name to be used with script processor: in this case, 
   *          PrintDS
   */
   public String getCommand()
   {
     return "PrintDS";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
     parameters = new Vector();  // must do this to create empty list of 
                                 // parameters

     Parameter parameter = new Parameter( "Run for DataSetPrinting",
                                           DataSet.EMPTY_DATA_SET );
     addParameter( parameter );

     parameter = new Parameter("Data block index", new Integer( 0) );
     addParameter( parameter );
     
     parameter = new Parameter("0:Console,1:file,2:textfield,3:table)", 
                                new Integer( 0) );
     addParameter( parameter );
     
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
    StringBuffer result = new StringBuffer("");
    
                                     
                                     // get the current data set
    DataSet ds     = (DataSet)(getParameter(0).getValue());
    int     index  = ((Integer)(getParameter(1).getValue()) ).intValue() ;
    int     OPtype = ((Integer)(getParameter(2).getValue()) ).intValue() ;

    Data    data;
    float   y_vals[];              
    float   x_vals[];              
    float   err_vals[] = null;              
    
    int     num_data = ds.getNum_entries();

    data = ds.getData_entry( index );        
  
    if ( data == null )
      return new ErrorString("ERROR: In PrintDS, No Data block # " + index  );

     x_vals   = data.getX_scale().getXs();
     y_vals   = data.getCopyOfY_values();
     err_vals = data.getErrors();
     
     int numofy= y_vals.length;
     DecimalFormat df=new DecimalFormat("####0.000");
     
     String columnName[];
     float tableArray[][];

     if (err_vals != null )
     {
       tableArray = new float [numofy][4];
       columnName = new String[] {"Number","x_values","y_values","Err_values"};
     }
     else
     {
       tableArray = new float [numofy][3];
       columnName = new String[] { "Number", "x_values", "y_values" };
     }
     
     for ( int i = 0; i < numofy; i++ )
     {

       if ( err_vals != null )
         result.append( i+"\t " + 
                        df.format(x_vals[i]) + "\t " + 
                        df.format(y_vals[i]) + "\t " +
                        df.format(err_vals[i])+"\r\n");
       else
         result.append( i+"\t " +
                        df.format(x_vals[i]) + "\t " +
                        df.format(y_vals[i]) + "\r\n");

       tableArray[i][0]=i;
       tableArray[i][1]=x_vals[i];
       tableArray[i][2]=y_vals[i];
       if ( err_vals != null )
         tableArray[i][3]=err_vals[i];       
     }
    
   String output = result.toString();
   String return_str = "Printed " + ds.toString() + ", index " + index;

    //0.Print to screen
   if(OPtype==0)
   {
    System.out.print(output);
    return_str += " on console";
   }

    //1.write to a file 
    String filename = "";
    if(OPtype==1)
    try
    {
        filename = ds.getTitle()+"_"+index+".prt";
        filename = StringUtil.fixSeparator( filename );
        File f = new File(filename);
        FileOutputStream op = new FileOutputStream(f);
        OutputStreamWriter opw =new OutputStreamWriter(op);
        opw.write(output);
        opw.flush();
        opw.close();
        return_str += " to file " +filename;
    }
    catch(Exception e)
    { 
      System.out.println("Exception while writing file "+filename );
      System.out.println("Exception is " + e );
      e.printStackTrace();
      return new ErrorString( e.toString());
    }
 
   //2.Jtextfield
   if(OPtype==2)
   try
   {
    JFrameMessageCHOP JFMC = (
        new JFrameMessageCHOP( "Window Output for " + ds.toString()+
                               "_INDX_" + index, 
                               ds.toString() + " for index " + index,
                               output));
    JFMC.setVisible(true);
    JFMC.setBounds(60, 60, 680, 680);
    return_str += " in text field";
   }
   catch (Throwable tt)
   {
     System.err.println(tt);
     tt.printStackTrace();
     return new ErrorString( tt.toString());
     //System.exit(1);
   }
    
   //3.Jtable
   if(OPtype==3)
   {
     OutputTable frame = new OutputTable( 
                              tableArray,
                              columnName,
                              ds.toString()+"_INDX_"+index );
     frame.pack();
     frame.setVisible(true);
     return_str += " in Jtable";
   } 

    System.out.println( return_str );
    return return_str;
  }  


public static void pause(int time)
{ 
  System.out.print("Pause for "+time/1000 +" second! ");
  try{Thread.sleep(time);}catch(Exception e){}
}


}
