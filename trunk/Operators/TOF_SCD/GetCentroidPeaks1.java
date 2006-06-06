
/*
 * File:  GetCentroidPeaks1.java 
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
 * $Log$
 * Revision 1.1  2006/06/06 19:32:47  rmikk
 * Initial Checkin of new Centroid Peak operator. Command name is the same
 * as the old one except a 1 is added
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
public class GetCentroidPeaks1 implements Wrappable, HiddenOperator {
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
    return "GetCentroidPeaks1";
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
	public Object calculate() {
		try {

			Object O = (new FindPeaks(DS, moncount, MaxNumPeaks,
					MinPeakIntensity, MinTimeChannel, MaxTimeChannel, PixelRows))
					.getResult();

			if (O instanceof ErrorString)
				return O;
			Vector V = (Vector) O;
			for(int i=0;i<V.size();i++){
				Peak P=(Peak) (V.elementAt(i));
				V.setElementAt( GetPeakR( DS,P),i);
				PeakInfo p = (PeakInfo)V.elementAt(i);
				System.out.println("xx"+i+","+p.getWeightedAverageRow()+","+p.getWeightedAverageCol()+","+
						    p.getWeightedAverageChan()+","+p.getCalcBackgroundLevel()+","+p.backgroundIntensity+","+
						    p.minY+"-"+p.maxY+","+p.minX+"-"+p.maxX+","+p.minZ+"-"+p.maxZ+","+p.getNCells()+","+p.getTotIntensity());
			}
			
			for(int i=0; i< V.size(); i++){
				PeakInfo P =(PeakInfo)V.elementAt(i);
				if( P != null)
					
				
					for( int j=V.size()-1; j>i; j--){
						PeakInfo P1=(PeakInfo)V.elementAt(j);
						if(P1 != null)
						if( P.hits(P1) )
							V.remove(j);
					}
			}
			//set up sequence numbers
			for(int i=0; i < V.size(); i++){
				Peak_new pk_n=((PeakInfo)V.elementAt(i)).makePeak();
				
				V.setElementAt( pk_n, i);
			}
			
			boolean done =false;
			for( int i= V.size()-1; i >=0 ; i--){
				if( V.elementAt(i) == null)
					V.remove(i);
			}
			
			return V;
		} catch (Exception ss) {
			return new ErrorString(ss.toString());
		}
		
		
		
	}
	
	private PeakInfo GetPeakR( DataSet DS, Peak P ){
		int x= (int)P.x();
		int y= (int) P.y();
		int z =(int)P.z();
		boolean done  =false;
		int gridID=P.detnum();
		IDataGrid grid = Grid_util.getAreaGrid( DS, gridID); 
		int nrows= grid.num_rows();
	    int ncols=grid.num_cols();

		float cutoff = grid.getData_entry(y,x).getY_values()[z];;
	    int nchan = DS.getData_entry(0).getY_values().length;
	    PeakInfo PP = GetPeak.getPeakInfo( y, x, z, gridID, DS, cutoff);
	    done = (PP==null);
	    PeakInfo PP2 = null;
		while(!done){
			if(PP==null)
				done = false;
		    else if(Float.isNaN(PP.getCalcBackgroundLevel() ) )
				done = false;
			else if( PP.getNCells()<5)
				done = false;
			else if( PP.getNCells() > .8*PP.getNCellsExtent())
				done =false;
			else if( (PP.maxX-PP.minX >.2*ncols)||(PP.maxY-PP.minY >.2*nrows)||(PP.maxZ-PP.minZ >.1*nchan)){
				done = true;
				PP=PP2;
			}
				
		    else if( cutoff<P.ipkobs()/200) done = true;
			else if( cutoff - PP.getCalcBackgroundLevel() <.3)
		   		done =true;
			if(!done){
				PP2=PP;
				
				float cutoff1 =(cutoff+PP.getCalcBackgroundLevel())/2.0f;
				
				if( cutoff1 >= .95*cutoff) 
					cutoff=cutoff/2f;
				else if( Float.isNaN(cutoff1))
					cutoff = cutoff/2.0f;
				else
					cutoff=cutoff1;
					
				PP = GetPeak.getPeakInfo( y, x, z, gridID, DS, cutoff);
			}
		}
		return PP;
		
	}
}
