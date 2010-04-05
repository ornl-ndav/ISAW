/*
 * File:  Grouping.java 
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
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
 * Revision 1.14  2006/07/10 16:26:11  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.13  2004/03/15 03:37:02  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.12  2004/01/30 02:26:34  bouzekc
 * Removed unused variables and imports.
 *
 * Revision 1.11  2003/12/15 02:06:09  bouzekc
 * Removed unused imports.
 *
 * Revision 1.10  2003/07/07 15:56:09  bouzekc
 * Added missing param tags in constructor and
 * getDocumentation().
 *
 * Revision 1.9  2003/04/17 20:37:47  pfpeterson
 * Added more debug lines and a check that IntList returned a non-empty array.
 *
 * Revision 1.8  2003/03/19 22:36:43  pfpeterson
 * Now carries over Data that was not grouped. Also added a parameter
 * for deciding whether a new DataSet is created or not.
 *
 * Revision 1.7  2003/02/26 16:02:56  dennis
 * Fixed problem with missing group.  (Chris Bouzek)
 *
 * Revision 1.6  2003/02/24 23:32:59  dennis
 * Un-commented the print USAGE statement.
 *
 * Revision 1.5  2003/02/24 21:04:29  pfpeterson
 * Changed style of full constructor and setDefaultParameters.
 *
 * Revision 1.4  2003/02/24 19:02:05  dennis
 * Added getDocumentation() method. (Shannon Hintzman)
 * Switch to use:  new parameter GUIs: DataSetPG, StringPG,
 * FloatPG and BooleanPD. ( Chris Bouzek )
 *
 * Revision 1.3  2002/11/27 23:30:47  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/07/10 15:50:40  pfpeterson
 * Now normalizes y and dy to the number of datablocks that
 * contributed to it.
 *
 * Revision 1.1  2002/07/08 15:44:47  pfpeterson
 * Added to CVS.
 *
 *
 */
package Operators.TOF_Diffractometer;

import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.Parameters.BooleanPG;
import gov.anl.ipns.Parameters.IntegerPG;
import gov.anl.ipns.Parameters.StringPG;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.util.Vector;
import java.util.Arrays;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.XScale;
import DataSetTools.operator.Operator;
import DataSetTools.operator.Parameter;
import DataSetTools.operator.Generic.TOF_Diffractometer.GenericTOF_Diffractometer;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.viewer.IViewManager;
import DataSetTools.viewer.ViewManager;

/** 
 *  This operator will group together the specified groups into a data set. 
 *  The intensity in each bin is added to the total intensity of the 
 *  grouped dataset.
 */
public class Grouping extends GenericTOF_Diffractometer{
    private static final String  TITLE = "Diffractometer Grouping";
    private static final boolean DEBUG = false;

    /** 
     *  Creates operator with title "Time Focus" and a default 
     *  list of parameters.
     */  
    public Grouping(){
        super( TITLE );
    }

    /** 
     * Construct a Grouping operator that sums together a list of
     * DataBlocks.  Note: The DataBlocks need to be at the same
     * position (is checked for).
     *
     *  @param ds        DataSet for which the focusing should be done.
     *  @param group_str String containing list of group ids of the
     *            	 spectra in the DataSet that should be grouped.
     *  @param new_gid   The group id of the new datablock created.
     *  @param newDS     Whether to make a new DataSet.
     */
    public Grouping( DataSet ds, String group_str, int new_gid, boolean newDS){
        this(); 
        getParameter(0).setValue(ds);
        getParameter(1).setValue(group_str);
        getParameter(2).setValue(new Integer(new_gid));
        getParameter(3).setValue(new Boolean(newDS));
    }

  /* ---------------------------- getDocumentation -------------------------- */
 
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator will group together specified groups ");
    Res.append("into a data set.  The intensity in each bin is added to the ");
    Res.append("total intensity of the grouped dataset.");
 
    Res.append("@algorithm If the DataSet is not null and there are items to ");
    Res.append("be grouped, then the id's of the items to be grouped are ");
    Res.append("retrieved and the grouping occurs.  The data is then ");
    Res.append("normalized, packed up, and returned from the method.");
       
    Res.append("@param ds DataSet for which the focusing should be done.");
    Res.append("@param group_str String containing list of group ids of the");
    Res.append(" spectra in the DataSet that should be grouped.");
    Res.append("@param new_gid The new group ID of the new datablock created.");
    Res.append("@param newDS Whether to make a new DataSet.");
    
    Res.append("@return Returns a DataSet where the specified groups have ");
    Res.append("been grouped together or an ErrorString.");
    
    Res.append("@error \"DataSet is null in Grouping\"");
    Res.append("@error \"Invalid Grouping (null or empty string)\"");
    Res.append("@error \"Data to be grouped must be at same effective ");
    Res.append("position\"");
    
    return Res.toString();
  }
  
  /* ------------------------------ getCommand ---------------------------- */
  
    /** 
     * Get the name of this operator to use in scripts. In this case
     * "GroupDiffract".
     */
    public String getCommand(){
        return "GroupDiffract";
    }

  /* -------------------------- setDefaultParameters ----------------------- */

    /** 
     * Sets default values for the parameters.  This must match the data types 
     * of the parameters.
     */
    public void setDefaultParameters(){
        parameters = new Vector();
        addParameter( new Parameter("DataSet parameter",
                                    DataSet.EMPTY_DATA_SET) );
        addParameter( new StringPG("List of Group IDs to focus", "") );
        addParameter( new IntegerPG("New Group ID", 1) );
        addParameter( new BooleanPG("Make New DataSet",false));
    }

  /* ------------------------------ getResult ----------------------------- */

    /** 
     *  Executes this operator using the values of the current parameters.
     *
     *  @return  If successful, this operator produces a DataSet where
     *           the specified groups have been grouped together. The
     *           intensity in each bin is added to the total intensity
     *           of the grouped dataset.
     */
    public Object getResult(){
        DataSet ds        =  (DataSet)(getParameter(0).getValue());
        String  group_str =  (String) (getParameter(1).getValue());
        int new_gid=((Integer)(getParameter(2).getValue())).intValue();
        boolean newDS   = ((Boolean)getParameter(3).getValue()).booleanValue();
        
        // sanity checks
        if ( ds == null )
            return new ErrorString("DataSet is null in Grouping");
        
                                     // find all group IDs if none specified
         if ( group_str == null || group_str.trim().length() <= 0 )
         {
           int   num_groups = ds.getNum_entries();
           int[] id_list = new int[num_groups];
           for ( int i = 0; i < num_groups; i++ )
             id_list[i] = ds.getData_entry(i).getGroup_ID();
           Arrays.sort( id_list );
           group_str = IntList.ToString( id_list );
         }

        // get the list of group ids
        int gid[] = IntList.ToArray( group_str );
        if(gid==null || gid.length==0)
          return new ErrorString("Invalid Grouping specifier:"+group_str);

        if(DEBUG) System.out.println("GRP_STR="+group_str);
        if(DEBUG && gid!=null )System.out.println("Grouping:"+gid[0]+" to "
                                                  +gid[gid.length-1]);

        // initialize the datablocks and positions
        Data d=null;
        DetectorPosition pos=null;
        Data temp_d=null;
        DetectorPosition temp_pos=null;

        System.out.println("BEFORE DOING GROUPING");

                  // Getting a Data block by ID is VERY expensive for a 
                  // large DataSet.  Processing all selected Data blocks is
                  // is much more efficient if a lot of Data blocks are to be
                  // processed.  So... we mark as selected the Data blocks
                  // with the specified group IDs and process the selected
                  // Data blocks.  To avoid side effects, we first save and
                  // then restore the selected flags.

                                 // save selections 
        int[] original_selections = ds.getSelectedIndices();

                                 // select based on ID
        ds.clearSelections();
        ds.setSelectFlagsByID( gid, true );

                                 // get the indices based on selections
        int[] indices_to_group = ds.getSelectedIndices();
        int   num_grouped      = indices_to_group.length;

                                 // restore the original selection flags
        ds.clearSelections();
        ds.setSelectFlagsByIndex( original_selections, true );
 
                                 // do the grouping, using indices
        int index;
        for( int i = 0 ; i < num_grouped ; i++ ){
            index = indices_to_group[ i ];
            temp_d=ds.getData_entry( index );
            if(DEBUG)System.out.println("Data["+index+"]="+temp_d);
            if(temp_d!=null){ // check that there is data with that gid
                temp_pos=(DetectorPosition)
                    temp_d.getAttributeValue(Attribute.DETECTOR_POS);
                if(d==null){
                    d=temp_d;
                    pos=temp_pos;
                }else{
                    if(temp_pos.equals(pos)){       // confirm they are focused
                        d=d.stitch(temp_d,Data.SUM);// to the same position
                    }else{
                        return new ErrorString("Data to be grouped must be at "
                                               +"same effective position");
                    }
                }
            }
        }

        //handle the case where no valid group IDs are sent in
        if( d == null )
        {  
          return new ErrorString(
            "No data entries with group ID(s) " + group_str + "\n");
        }
        
        System.out.println("END OF STITCHING, BEGIN WEIGHTING");
        float result_x[]     = d.getX_scale().getXs();
        float result_y[]     = d.getY_values();
        float result_dy[]    = d.getErrors();
        int   num_bins       = result_y.length;
        boolean is_histogram = d.isHistogram();
                                                  // get all of the XScales
                                                  // then count how many Data
                                                  // Blocks contain each bin
                                                  // center
        XScale[] xscale_list = new XScale[ num_grouped ];
        for ( int j = 0; j < num_grouped; j++ )
        { 
          index = indices_to_group[ j ];
          xscale_list[j] = ds.getData_entry(index).getX_scale();
        }

        int[] num_d = new int[ num_bins ];
        float xval;
        for ( int i = 0; i < num_bins; i++ )
        {
          if ( is_histogram )
            xval = (result_x[i]+result_x[i+1])/2f;
          else
            xval = result_x[i];
          for ( int j = 0; j < num_grouped; j++ )
            if ( xscale_list[j].inRange( xval ) )
              num_d[ i ]++; 
        }

        int num_data = 0;
        for ( int i = 0; i < num_bins; i++ )
        {
          num_data = num_d[i]; 
          if ( num_data > 0 )
          {
            result_y[i] *= (float)num_grouped / (float)num_data;
            if ( result_dy != null )
              result_dy[i] *= (float)num_grouped / (float)num_data;
          }
        }

        System.out.println("DONE WITH WEIGHTING ....");
/*
        // normalize the data by the number of blocks combined to make
        // the new dataset.
        float num_d;
        float x[]  = d.getX_scale().getXs();
        float y[]  = d.getCopyOfY_values();
        float dy[] = d.getCopyOfErrors();
        int max=x.length;
        float xval;
        if(d.isHistogram())max--;
        for( int i=0 ; i<max ; i++ ){
            num_d=0f;
            for( int j=0 ; j<gid.length ; j++ ){
                temp_d=ds.getData_entry_with_id(gid[j]);
                if(temp_d!=null){
                    if(d.isHistogram())
                        xval=(x[i]+x[i+1])/2f;
                    else
                        xval=x[i];
                    if(temp_d.getX_scale().inRange(xval))num_d+=1f;
                }
            }
            if(num_d>0f){
                y[i]=y[i]/num_d;
                if ( dy != null )
                  dy[i]=dy[i]/num_d;
            }
        }
*/
        // pack it all up and return the grouped data
        Data new_d=Data.getInstance( d.getX_scale(),
                                     result_y,
                                     result_dy,
                                     new_gid );
        new_d.setAttributeList(d.getAttributeList());

        DataSet new_ds=null;
        if(newDS)
          new_ds=(DataSet)ds.empty_clone();

        else                     // use the same DataSet, but get rid of 
        {                        // the Data blocks we combined!
          new_ds=ds;
                                 // We must be careful to remove Data blocks
                                 // in reverse order since the meaning of the
                                 // indices change as blocks are removed!!!
          Arrays.sort( indices_to_group );
          for( int i=num_grouped-1 ; i>=0 ; i-- )
            new_ds.removeData_entry(indices_to_group[i]);
        }

        new_ds.addLog_entry("Grouped " + group_str + " to group " + new_gid);
        new_ds.addData_entry(new_d);

        return new_ds;
    }

  /* ------------------------------ clone ------------------------------- */

    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){
        Operator op = new Grouping();
        op.CopyParametersFrom( this );
        return op;
    }

  /* ------------------------------- main -------------------------------- */

    /** 
     * Test program to verify that this will compile and run ok.  
     */
    public static void main(String args[]){
        System.out.println("Test of Grouping starting...");
        if(args.length==1){
            String filename = args[0];
            RunfileRetriever rr = new RunfileRetriever( filename );
            DataSet ds = rr.getDataSet(1);
            // make operator and call it
            String gid="44:73";
            Operator op = new TimeFocusGID(ds, gid, 10.0f, 2.0f, true);
            Object result = op.getResult();
            
            if(result instanceof DataSet ){
              op = new Grouping( (DataSet)result, gid, 44,true );
              result = op.getResult();
              if ( result instanceof DataSet ){      // we got a DataSet back
                                                // so show it and original
                  DataSet new_ds = (DataSet)result;
                  new ViewManager( ds,    IViewManager.IMAGE );
                  new ViewManager( new_ds, IViewManager.IMAGE);
              }else{
                  System.out.println( "Operator returned " + result );
              }
            }
          }else{
              System.out.println("USAGE: Grouping <filename>");
        }
        System.out.println("Test of Grouping done.");
    }
}
