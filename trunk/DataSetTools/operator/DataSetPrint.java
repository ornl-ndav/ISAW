/*
 * @(#)DataSetPrint.java   0.1  00/08/02   Dongfeng Chen 
 *                                         Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.6  2000/08/17 19:19:07  dennis
 *  Moved OutputTable from DataSetTools/operator to DataSetTools/components/ui
 *
 *  Revision 1.5  2000/08/17 19:08:28  dennis
 *  Modified 08/13 by Dongfeng.  Now prints output into JTable for viewing, copying
 *  and pasting.  Changed the output format. All of the output types can be pasted
 *  neatly.
 *
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

public class DataSetPrint extends    Operator 
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
   * Returns the abbreviated command string for this operator.
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
                    new DataSet("Run for DataSetPrinting", "Empty DataSet"));
     addParameter( parameter );

     parameter = new Parameter("Data block index", new Integer( 0) );
     addParameter( parameter );
     
     parameter = new Parameter("Output Type (0=Print, 1=write 2= textfield or 3=table)", 
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

                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSetFactory factory = new DataSetFactory( 
                                     ds.getTitle(),
                                     "x_value",
                                     "x_axis",
                                     "y_value",
                                     "y_axis" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "DataSetPrint Data" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );
   
    Data             data,
                     new_data;
    float            y_vals[];              
    float            x_vals[];              
    float            err_vals[];              
    
    int              num_data = ds.getNum_entries();

     data = ds.getData_entry( index );        
  
     if ( data == null )
       return new ErrorString("ERROR: In PrintDS, No Data block # " + index  );

     x_vals           = data.getX_scale().getXs();
     y_vals  = data.getCopyOfY_values();
     
     err_vals  = data.getErrors();
     
     int numofy= y_vals.length;
     DecimalFormat df=new DecimalFormat("####0.000");
     
     float [][] tableArray = new float [numofy][4];
        
     String [] columnName =     {"Number", 
                                "x_values",
                                "y_values",
                                "Err_values"
                                          };
     
     
     
     for ( int i = 0; i < numofy; i++ )
     {
       result.append( i+"\t "+df.format(x_vals[i])+"\t "+df.format(y_vals[i])+"\t "+df.format(err_vals[i])+"\r\n");
       tableArray[i][0]=i;
       tableArray[i][1]=x_vals[i];
       tableArray[i][2]=y_vals[i];
       tableArray[i][3]=err_vals[i];       
     }
    
    String output = result.toString();

    //0.Print to screen
   if(OPtype==0)
   {
    System.out.print(output);
    System.out.print("... Pop up "+ ds.toString()+"_INDX_"+index+" on screen \n");
   }

    //1.write to a file 
    if(OPtype==1)
    try{
        String filename = ds.toString()+"_"+index+".prt";
        filename = StringUtil.fixSeparator( filename );
        File f = new File(filename);
        FileOutputStream op = new FileOutputStream(f);
        OutputStreamWriter opw =new OutputStreamWriter(op);
        opw.write(output);
        opw.flush();
        opw.close();
    System.out.print("... Save  "+ ds.toString()+"_INDX_"+index+" to the file "+filename+"\n");
    }catch(Exception e){}
    
 
   //2.Jtextfield
   if(OPtype==2)
		try
		{
    		JFrameMessageCHOP JFMC=(new JFrameMessageCHOP("Window Output for "+ds.toString()+"_INDX_"+index, ds.toString()+" for index "+index , output));
    		JFMC.setVisible(true);
    		JFMC.setBounds(60, 60, 680, 680);
       System.out.print("... Pop up  "+ ds.toString()+"_INDX_"+index+" in text field \n");

		}
		catch (Throwable tt)
		{
			System.err.println(tt);
			tt.printStackTrace();
			System.exit(1);
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
        System.out.print("... Pop up "+ ds.toString()+"_INDX_"+index+" in Jtable \n");
        
   } 

    return new_ds;
  }  


public static void pause(int time)
{ 
 System.out.print("Pause for "+time/1000 +" second! ");
  try{Thread.sleep(time);}catch(Exception e){}
    
}


}


