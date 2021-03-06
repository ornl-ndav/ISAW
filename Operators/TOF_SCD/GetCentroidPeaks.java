
/*
 * File:  GetCentroidPeaks.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 *  $Author$
 *  $Date$            
 *  $Revision$
 *
 * $Log$
 * Revision 1.7  2008/01/29 19:43:07  rmikk
 * Used Peak_new methods to set up a Peak_new
 *
 * Revision 1.6  2006/01/18 21:47:44  dennis
 * Minor fix to format of help message.
 * Switched from DOS to UNIX text.
 *
 * Revision 1.5  2006/01/16 04:46:20  rmikk
 * Changed to use the changed constructor for Peak_new
 *
 * Revision 1.4  2005/05/27 03:10:19  dennis
 * Changed to use get attribute method from AttrUtil, rather than
 * the old get attribute method from DataSet and Data
 *
 * Revision 1.3  2004/07/31 23:05:44  rmikk
 * Removed Unused Import
 *
 * Revision 1.2  2004/07/14 17:16:54  rmikk
 * Eliminated the weird import
 *
 * Revision 1.1  2004/07/14 16:20:48  rmikk
 * Initial Checkin
 * This gets the centroided peaks and returns a Vector of Peak_new objects
 *
 */

package Operators.TOF_SCD;

//import java.beans.java_awt_BorderLayout_PersistenceDelegate;

import DataSetTools.operator.*;
import DataSetTools.dataset.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import java.util.*;
//import gov.anl.ipns.Util.Numeric.*;
import DataSetTools.instruments.*;


/*
 * This wrapped operator performs both find peaks and centroid peaks for a 
 * data set.  The peaks object, as Peak_new, are returned.  These Peak objects
 * have ALL the conversion information( except possibly the orientation matrix).
 */
public class GetCentroidPeaks implements Wrappable, HiddenOperator {
  //~ Instance fields **********************************************************

  public DataSet DS; //Calibrated
  public float moncount ;
  public int MaxNumPeaks =30;
  public int MinPeakIntensity =10;
  public int MinTimeChannel =0;
  public int MaxTimeChannel =1000;
  public IntListString PixelRows = new IntListString("0:100");
                              //  GridNums

  //~ Methods ******************************************************************

  /**
   * Uncomment the lines below if you want to use your own command name.
   * Normally the command name is the name of your class file (e.g.
   * ArtsIntegrate) in all capital letters.
   */
  public String getCommand(  ) {
    return "GetCentroidPeaks";
  }

  
  public String getDocumentation(  ) {
   
       StringBuffer s = new StringBuffer(  );
       s.append( "@overview This operator find's and centroids peaks in " );
       s.append( "a DataSet and returns them as a Vector of Peak_new entries" );
       s.append( "@Uses the FindPeaks and CentroidPeaks operators to get " );
       s.append( "a Vector of Peak objects, then converts each to a Peak_new" );
       s.append( "entry ");
       s.append( "@param DS the data set contianing the peak");       
       s.append( "@param moncount  The total counts on the Corresponding ");
       s.append( "monitor for the data set");         
       s.append( "@param MaxNumPeaks The maximum number of peaks to fine");
       s.append( "@param MinPeakIntensity the minimum intensity for a peak");
       s.append( "@param MinTimeChannel The minimum time channel to use");
       s.append( "@param MaxTimeChannel The maximum time channel to use");
       s.append( "@param PixelRows The rows and columns to use");
       
       s.append( "@return A Vector of Peak_new Objects with data entries ");
       s.append( "cleared( no References to the DataSet) " );

       s.append( "@error No Area Grids. The DataSet must have area grids " );
       s.append( "@error index out of bounds if Grid Id's do not match grids" );
       s.append(" @error null pointer exceptions, etc. See stack trace");
       return s.toString(  );
  }

  /**
    *
   */
  public Object calculate(  ) {
   try{
   
     Object O = (new FindPeaks(DS, moncount,MaxNumPeaks, MinPeakIntensity,
                     MinTimeChannel, MaxTimeChannel, PixelRows)).getResult();
    
     if( O instanceof ErrorString)
       return O;
     Vector V = (Vector)O;
     O = (new CentroidPeaks(DS, V)).getResult();
     if( O instanceof ErrorString)
       return  O;
     V = (Vector)O;
     int[] GridIDs =Grid_util.getAreaGridIDs(DS);
     java.util.Arrays.sort(GridIDs);
     if( (GridIDs == null) ||( GridIDs.length < 1))
       return new ErrorString("DataSet has no Area Grids");
     
     IDataGrid[] Grids = new IDataGrid[GridIDs.length];
     for( int i= 0; i< GridIDs.length; i++){
        Grids[i] = Grid_util.getAreaGrid(DS, GridIDs[i]);
        //Grids[i].clearData_entries();
     }
       
     XScale[] xscales = new XScale[ GridIDs.length];
     for( int i=0; i< GridIDs.length; i++){
     
        xscales[i]= Grids[i].getData_entry(1,1).getX_scale();
        
     }
     float[] calibTimeAdjustments = new float[GridIDs.length];
     for( int i=0; i< Grids.length; i++){
     
       calibTimeAdjustments[i]= AttrUtil.getT0Shift(Grids[i].getData_entry(1,1));
       if( Float.isNaN(calibTimeAdjustments[i]))
          calibTimeAdjustments[i]=0f;
     }
       
     /*int[] Rows =IntList.ToArray(PixelRows.toString());
     if( (Rows == null) ||( Rows.length < 1))
       Rows = IntList.ToArray( "1:400");
     else
       java.util.Arrays.sort(Rows);
     */  
    Vector Res= new Vector();
    Data db = DS.getData_entry(0);
    SampleOrientation sampOrient =(SampleOrientation) db.getAttributeValue( 
              Attribute.SAMPLE_ORIENTATION);

    for( int i=0; i < V.size(); i++){
       Peak p = (Peak)(V.elementAt(i));
       {        
         int detnum =p.detnum();
         int indx = java.util.Arrays.binarySearch(GridIDs,detnum);
         float t0_shift = calibTimeAdjustments[indx];
         float initial_path = 
                     AttrUtil.getInitialPath(Grids[indx].getData_entry(1,1));
         Peak_new pk = new Peak_new( p.nrun(),
                                     p.monct(),
                                     p.x(),
                                     p.y(),
                                     p.z(),
                                     Grids[indx],
                                     sampOrient,
                                     xscales[indx].getInterpolatedX(p.z())+t0_shift,
                                     initial_path,
                                     t0_shift );   
         pk.setFacility( AttrUtil.getFacilityName( DS ) );
         pk.setInstrument( AttrUtil.getInstrumentName( DS ) );
         pk.seqnum( p.seqnum() );
         pk.inti(p.inti());
         pk.sigi(p.sigi());
         pk.reflag( p.reflag());
         pk.UB(p.UB());
         pk.ipkobs( p.ipkobs() );
         
         Res.addElement(pk);       
       }
     }
     for( int i=0; i< Grids.length ; i++)
       Grids[i].clearData_entries();
     return Res;
     
   }catch(Throwable ss){
     if( ss != null){
      ss.printStackTrace();
      return new ErrorString( ss.toString() );
     }else{
       return new ErrorString("Weird error");
     }
   }
        
  }
}
