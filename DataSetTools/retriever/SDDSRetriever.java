/*
 * File:  SDDSRetriever.java 
 *             
 * Copyright (C) 2001, Alok Chatterjee
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.8  2006/06/08 22:21:04  dennis
 * Now sets the Data block labels from the SDDS column names for
 * each Data block constructed.
 * Now sets the DataSet title as the name of the first Data block in
 * the DataSet, followed by the units used when forming that DataSet.
 * Added javadocs and did some code cleanup and reformatting.
 *
 *
 * Revision 1.7  2004/01/24 20:36:22  bouzekc
 * Removed unused variables and imports.
 *
 * Revision 1.6  2002/11/27 23:23:16  pfpeterson
 * standardized header
 *
 * Revision 1.5  2002/05/30 23:01:17  chatterjee
 * Corrected bug that would only put consecutive columns
 * having the same units in a dataset and not if they were
 * separated by a column with different units.
 *
 * Revision 1.4  2002/04/08 15:46:40  dennis
 * Adds XDateTime operator to the DataSets so that the Date and Time
 * corresponding to a particular elapsed time in seconds can be
 * displayed.
 *
 * Revision 1.3  2002/04/05 16:31:00  chatterjee
 * Fixed the label/units error appearing in the SelectedGraph view
 *
 * Revision 1.2  2002/03/13 16:14:46  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.1  2002/03/12 20:41:38  chatterjee
 * Retriever for SDDS files. Need to instantiate this class with the sdds 
 * file name to load it into ISAW.
 *
 */

package DataSetTools.retriever ; 
import DataSetTools.dataset.* ;
import SDDS.java.SDDS.*;
import java.lang.reflect.*;
import javax.swing.filechooser.FileFilter;
import DataSetTools.operator.DataSet.Information.XAxis.*;
import java.util.*;

/**
 *  The SDDS Retriever will load an SDDS Log file into a collection of
 *  DataSets, with each DataSet containing SDDS Columns with the same 
 *  units. 
 */
public class SDDSRetriever extends Retriever
{
   int i , m;
   SDDSFile sdds;
   int num_data_sets;    
   DataSet[] DS;
   DataSet ds;
   String fileName;
   int page =1;
   int rows = 0;
   int ycount = 0;
   int x_col=0;
   FileFilter FF;

  /* --------------------------- constructor ---------------------------- */
  /**
   *  Construct an SDDSRetriever object, by opening the specified SDDS log 
   *  file and reading the columns of SDDS data into DataSets, organized by
   *  the units of the SDDS data. 
   *
   *  @param fileName  The name of the SDDS log file to load.
   */
   public SDDSRetriever( String fileName )
   {
     super( fileName ) ;
     num_data_sets = 0;
     sdds = new SDDSFile( fileName );
     if ( !sdds.readFile() )
       return;

     String[] columnNames    = null, 
              parameterNames = null;
     Object[][] columnValues = null;
     Object[][] parameterValues = null;
     int i, 
         j, 
         numberOfColumns = 0, 
         numberOfParameters = 0;
     int rows;
     int type =0;

     String[] col_units = sdds.getAllColumnUnits();
     rows = sdds.getRowCount(page);
     columnNames = sdds.getColumnNames();
     int cols = Array.getLength(columnNames);

     double xData[] = new double[rows];
     double xx[] = new double[rows];
     float x[] = new float[rows];

     float y[][]= new float[cols][rows];
     parameterNames = sdds.getParameterNames();

     DS = new DataSet[cols];

     if (parameterNames != null) 
     {
       numberOfParameters = Array.getLength(parameterNames);
       parameterValues = new Object[numberOfParameters][1];
       for (i=0;i<numberOfParameters;i++) 
         parameterValues[i][0] = sdds.getParameterValue(i,page,false);         
     }
   
     if (columnNames != null) 
     {
       numberOfColumns = Array.getLength(columnNames);         
       columnValues = new Object[numberOfColumns][rows];
       for (i=0;i<numberOfColumns;i++)
         columnValues[i] = sdds.getColumnValues(i,page,false);
     }

     for (j=0;j<numberOfColumns;j++) 
     {
       if (columnNames[j].equals("Time"))
         for (m=0;m < rows ;m++) 
         {
           x_col=j;
           xData[m] = ((Double)columnValues[x_col][m]).doubleValue();
           xx[m] = xData[m]-xData[0];
           x[m] =   (float)xx[m];       
       //  System.out.println("Column values are :" +m +" , " +x[m]);
         }
     }

     for (j=0;j<numberOfColumns;j++) 
     {
       type = sdds.getColumnType(j);
       y[j] = (float[])( 
           SDDSUtil.castArrayAsFloat(sdds.getColumnValues(j,page,false), type));
     }

     int n_ds = 0;     // n_ds counts the distinct datasets being built
         j    = 0;     // j steps across the available data blocks

     boolean done = false;
     Hashtable ht = new Hashtable();
  
     while ( j < cols && !done )     // While there are more DataSets to build,
     {                               // store index of DataSets with same
                                     // units in hashtable, with units as key.
       if (col_units[j] == null)
         col_units[j] = new String("");

       String  y_units = col_units[j]; 
       String  x_units = col_units[x_col];    

       DataSet DSS = null;
       Object  O   = ht.get(col_units[j]);     // Look for DataSet with same
                                               // units as the current column

       if( O == null )                        // if no such DataSet, make a
       {                                      // a new DataSet with right units
         String ds_title = columnNames[j] + " " + y_units;
         /*
         if ( ds_title.length() == 0 )
           ds_title = "UN-LABELED";
         */
         DataSetFactory ds_factory = 
                        new DataSetFactory( ds_title, 
                                            x_units,"Time",
                                            y_units, columnNames[j] );

         DS[n_ds] = ds_factory.getDataSet();
         DS[n_ds].addOperator( new XDateTime() ); 
         ht.put( col_units[j], new Integer(n_ds) );
         DSS = DS[n_ds];
         n_ds++;
       }
       else                                   // we have an existing DataSet
       {                                      // with the right units so use it
         int kk = ((Integer)O).intValue();
         DSS = DS[kk];
       }  
                                            // now that we found the DataSet
                                            // for this SDDS column, make
                                            // a new Data block for the 
                                            // column and add it to the DataSet
       Data d  = Data.getInstance( new VariableXScale(x), y[j], j );
       d.setAttribute( new StringAttribute(columnNames[j], col_units[j]));
       d.setLabel( columnNames[j] );

       for ( i=0; i < numberOfParameters; i++ )
         d.setAttribute( new StringAttribute(parameterNames[i],
                                             parameterValues[i][0].toString()));
       DSS.addData_entry( d );
       j++;

       if ( j >= cols )                      // all Data blocks used so done
         done = true;

     } //end of outer while

     num_data_sets= ht.size();
  } 


  /* -------------------------- getDataSet ---------------------------- */
  /**
   * Get the specified DataSet that was loaded from the SDDS log file.
   *
   * @param data_set_num  The number of the DataSet from the SDDS file. 
   *
   * @return The specified DataSet, if it exists, or null if no such
   *         DataSet exists.
   */
  public DataSet getDataSet( int data_set_num ) 
  { 
    ds = DS[data_set_num];
    if ( ds == null )
      System.out.println("getDS res=null for"+data_set_num);
    return ds ;
  }               
  

  /* ---------------------------- getType ------------------------------ */
  /**
   *  Get the type code of a particular DataSet in this SDDS file. 
   *  Currently, this just returns type code Retriever.HISTOGRAM_DATA_SET.
   *  It may be necessary to add new type codes to support SDDS properly.
   *
   *  @param  data_set_num  The number of the DataSet in this runfile whose
   *                        type code is needed.  data_set_num must be between
   *                        0 and numDataSets()-1
   *
   *  @return the type code for the specified DataSet.
   */
  public int getType( int data_set_num ) 
  { 
    if ( data_set_num < num_data_sets && data_set_num >= 0 )
      return HISTOGRAM_DATA_SET;
    else
      return INVALID_DATA_SET;
  }           

  
  /* ------------------------ numDataSets -------------------------- */
  /**
   * Get the number of distinct DataSets that were loaded from the SDDS
   * file.
   *
   *  @return The number of distinct DataSets from the SDDS file.  
   */
  public int numDataSets() 
  {
    if ( sdds == null )
      return 0;
 
    return num_data_sets;
  }         


}    

