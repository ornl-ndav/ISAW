/*
 * File:  LoadSelDataSets.java 
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 *  Revision 1.2  2004/01/24 19:48:52  bouzekc
 *  Removed unused imports.  Removed unused variables in main().
 *
 *  Revision 1.1  2003/12/15 00:50:30  rmikk
 *  Initial Checkin. This operators lets a subset of the data sets stored in
 *    a file be retrieved
 *
 *
 */

package DataSetTools.operator.Generic.Load;

import java.io.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import DataSetTools.parameter.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

/**
 * Operator to load all data sets from one IPNS runfile
 *
 * @see DataSetTools.operator.Operator
 */

public class LoadSelDataSets extends    GenericLoad 
                            implements Serializable, ActionListener
{

   JDialog jf=null;
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this constructor
   * is used, meaningful values for the parameters should be set before 
   * calling getResult().
   */
   public LoadSelDataSets( )
   {
     super( "Load Selected DataSets" );
   }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for with the specified parameter values so 
   *  that the operation can be invoked immediately by calling getResult().
   *
   *  @param  file_name   The fully qualified runfile name
   *  @param  group_mask  A list of group IDs that should be omitted
   *
   */
   public LoadSelDataSets( String   file_name)
   {
      this( );

      IParameter parameter = getParameter(0);
      parameter.setValue( file_name );
    
     
   } 

  /* -------------------------- setDefaultParameters ----------------------- */
  /**
   *  Set the parameters to default values.  
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    
    addParameter( new LoadFilePG("Enter Filename", null));
   
  }

  /*----------------------------getDocumentation-----------------------------*/
  
   public String getDocumentation()
   {
   	StringBuffer Res = new StringBuffer();
	
	Res.append("@overview This operator loads only selecte data sets from a file");
	
	Res.append("@algorithm A file is read in.  If there are no DataSets ");
    	Res.append("found in the file, then an ErrorString is returned.  ");
	Res.append("Otherwise an array is created, the DataSets are read in ");
	Res.append("from the file, omitted of any group IDs that should be, ");
	Res.append("and stored in the array.");
	
	Res.append("@param file_name The fully qualified file name");
	Res.append("  Another dialog box appears after the execution of this operator ");
        Res.append(" starts that allows for the selection of the desired data sets in ");
        Res.append(" this file "); 
	Res.append("@return Returns an array with all the selected DataSets in the ");
        Res.append("file, if the file could be opened.");
	
	Res.append("@error No DataSets in \"filename\".");
	
	return Res.toString();
   
   }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case,
   *           	OneFile
   *
   */
   public String getCommand()
   {
     return "LoadSelDataSets";
   }


  /* ----------------------------- getResult ---------------------------- */
  /**
   * Returns the object that is the result of applying this operation.  This
   * should be called after setting the appropriate parameters.
   *
   * @return  Returns an array with all DataSets in the runfile, if the 
   *          runfile could be opened.
   */
   public Object getResult()
   {
                                          // get the parameters specifying the
                                          // runs         
     String    file_name   = (String)getParameter(0).getValue();


     
     Retriever rr = null;
     try{
         rr= Command.ScriptUtil.getRetriever( file_name );
     }catch( Exception ss){
        return new ErrorString( ss);
     }
     int n_ds      = rr.numDataSets();
     if ( n_ds <= 0 )
     {
       System.out.println("ERROR: no DataSets in " + file_name );
       return new ErrorString("ERROR: no DataSets in " + file_name);
     }
     int[] sel = null;
     try{
       jf = new JDialog((JFrame)null,"DataSets",true);
       jf.setSize( 400, 100*n_ds);
       DefaultListModel listmod = new DefaultListModel();
       JList jlist = new JList(  listmod);
       for( int i=0; i< n_ds; i++){
          String[] info = null;
          if( rr instanceof hasInformation)
             info = ((hasInformation)rr).getDataSetInfo( i);
           String S="Name:";
       if( info != null)
         S += info[0];
       else S += "DataSet "+i;
       S +="("+ NameOF(rr.getType( i))+"):ids(default)-";
       if( info != null)
         S += info[2];
       else S +="???";
       listmod.addElement( S);

     }
     jf.getContentPane().setLayout( new BorderLayout());
     
     JButton jbut = new JButton("OK");
     jf.getContentPane().add(jbut, BorderLayout.SOUTH);
     
     jbut.addActionListener( this);
     jf.getContentPane().add( jlist, BorderLayout.CENTER);
      jf.validate();
     jf.show();
     sel = jlist.getSelectedIndices();

    }catch(Exception ss){
      return new ErrorString("Could not select the DataSets");
    }
     sel = check( sel, n_ds-1);
     if( sel == null)
       return new ErrorString(" no DataSets have been selected");
     if( sel.length < 1)
       return new ErrorString(" no DataSets have been selected");

     DataSet[] DSS = new DataSet[sel.length];
     for( int i=0; i< sel.length; i++)
       DSS[i] = rr.getDataSet(sel[i]);
     return DSS;
   }

  private int[] check( int[] sel, int maxnum){
     int n=0;
     if( sel == null) return null;
     for( int i=0; i< sel.length; i++)
       if( sel[i] < 0) n++;
       else if(  sel[i]> maxnum) n++;
    if( n == sel.length) return null;
    int[] Res = new int[sel.length -n];
    int k=0;
    for( int i=0; i< sel.length; i++)
       if(sel[i] >=0)
          if(sel[i] <=maxnum){
            Res[k] = sel[i];
            k++;
          }
    return Res;

  }

  private String NameOF( int DS_type){

     if( DS_type== Retriever.MONITOR_DATA_SET)
        return "Monitor";
     if( DS_type == Retriever.HISTOGRAM_DATA_SET)
        return "Histogram";
     if( DS_type == Retriever.PULSE_HEIGHT_DATA_SET)
        return "Pulse Height";
     return "Unkown";
    
  }
   /* -------------------------------- main ------------------------------ */
   /* 
    * main program for test purposes only  
    */

   public static void main(String[] args)
   {
      
      LoadSelDataSets loader = new LoadSelDataSets( 
                             "/home/groups/SCD_PROJECT/SampleRuns/hrcs2444.run"
                             );


      Object result = loader.getResult();
      if ( result instanceof DataSet[] )
      {
        DataSet datasets[] = (DataSet[])result;

        for ( int i = 0; i < datasets.length; i++ )
          new ViewManager( datasets[i], IViewManager.IMAGE );
      }
      else
        System.out.println( result.toString() );
	
      System.out.println(loader.getDocumentation());
   } 
   public void actionPerformed( ActionEvent evt){

      jf.dispose();

   }
} 
