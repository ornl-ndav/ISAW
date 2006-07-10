/*
 * File:  DataSetPrint.java  
 * 
 * Copyright (C) 2000, Dongfeng Chen, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.3  2006/07/10 21:28:25  dennis
 *  Removed unused imports, after refactoring the PG concept.
 *
 *  Revision 1.2  2006/07/10 16:25:59  dennis
 *  Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 *  Revision 1.1  2005/01/10 15:25:00  dennis
 *  Moved into "Save" directory.
 *
 *  Revision 1.8  2004/03/19 17:20:50  dennis
 *  Removed unused variable(s)
 *
 *  Revision 1.7  2004/03/15 06:10:50  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.6  2004/03/15 03:28:32  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.5  2003/02/28 14:53:47  pfpeterson
 *  Changed call of deprecated fixSeparator to setFileSeparator.
 *
 *  Revision 1.4  2003/02/28 14:50:10  dennis
 *  Added getDocumentation() method. (Tyler Stelzer)
 *
 *  Revision 1.3  2002/11/27 23:20:43  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/09/19 16:05:07  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 20:56:51  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.Generic.Save;

import gov.anl.ipns.Parameters.IParameter;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.ViewTools.UI.*;

import  java.io.*;
import  java.text.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.components.ui.*;
import  DataSetTools.operator.*;
import  DataSetTools.operator.Generic.*;

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


  /* ------------------------ getCategoryList ------------------------------ */
  /**
   * Get an array of strings listing the operator category names  for 
   * this operator. The first entry in the array is the 
   * string: Operator.OPERATOR. Subsequent elements of the array determine
   * which submenu this operator will reside in.
   * 
   * @return  A list of Strings specifying the category names for the
   *          menu system 
   *        
   */
  public String[] getCategoryList()
  {
    return Operator.FILE_PRINT;
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

    IParameter parameter = getParameter(0);
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
        filename = StringUtil.setFileSeparator( filename );
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

  
  public String getDocumentation()
  {
    StringBuffer res = new StringBuffer("");
    
    res.append("@overview This operator converts Print data information.");
    
    res.append("@algorithm Check to make sure there is valid data.  Output");
     res.append(" information to the designated output location.  Return");
     res.append(" a string stating that it printed to a location.");
    
    res.append("@param  ds  The DataSet to which the operation is applied.");
    res.append("@param  index  The data block number.");
    res.append("@param  outputtype  Where to write the data to.");
    
    res.append("@return This returns a string \"Printed < dataSet >, index");
     res.append(" < index > to < location >\". Otherwise it returns an");
     res.append(" error string if it is not successful.");
    
    res.append("@error ERROR: In PrintDS, No Data block # "); 
    
    return res.toString();
  }


}
