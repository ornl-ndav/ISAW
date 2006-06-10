
/*
 * File:  GetCentroidPeaks1.java 
 *             
 * Copyright (C) 2006, Ruth Mikkelson
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
 
 * $Log$
 * Revision 1.4  2006/06/10 22:10:26  rmikk
 * Improved the documentation
 *
 * Revision 1.3  2006/06/08 22:04:15  rmikk
 * Eliminated debug printing to a file.
 * Eliminated an unused variable
 *
 * Revision 1.2  2006/06/08 15:50:59  rmikk
 * Fixed copyright.
 * Started first cutoff at 80% of the intensity at the startinf point
 *
 * Revision 1.1  2006/06/06 19:32:47  rmikk
 * Initial Checkin of new Centroid Peak operator. Command name is the same
 * as the old one except a 1 is added
 *
 */
package Operators.TOF_SCD;



import DataSetTools.operator.*;
import DataSetTools.dataset.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import java.util.*;



/*
 * This wrapped operator performs both find peaks and centroid peaks for a 
 * data set.  The peaks object, as Peak_new, are returned.  These Peak objects
 * have ALL the conversion information( except possibly the orientation matrix).
 * The algorithm for centroiding one peak uses 
 * DataSetTools.operator.Generic.TOF_SCD.GetPeak with varying cutoffs until
 * certain conditions are obtained.
 */
public class GetCentroidPeaks1 implements Wrappable, HiddenOperator {
  //~ Instance fields **********************************************************

  public DataSet DS; //Calibrated
  public float moncount ;
  public int MaxNumPeaks = 30;
  public int MinPeakIntensity = 10;
  public int MinTimeChannel = 0;
  public int MaxTimeChannel = 1000;
  public IntListString PixelRows = new IntListString( "0:100" );
                              //  GridNums

  //~ Methods ******************************************************************

  /**
   * Returns GetCentroidPeaks1, the command name used by the Scripting 
   * language to invoke this operator
   * 
   * @return  GetCentroidPeaks1
   */
  public String getCommand(  ) {
    return "GetCentroidPeaks1";
  }

  
  public String getDocumentation(  ) {
   
       StringBuffer s = new StringBuffer(  );
       s.append( "@overview This operator find's and centroids peaks in " );
       s.append( "a DataSet and returns them as a Vector of Peak_new entries" );
       s.append( "@Uses the FindPeaks and an internal method to get " );
       s.append( "a Vector of Peak objects, then converts each to a Peak_new" );
       s.append( "entry ");
       s.append( "@param DS the data set contianing the peak" );       
       s.append( "@param moncount  The total counts on the Corresponding " );
       s.append( "monitor for the data set" );         
       s.append( "@param MaxNumPeaks The maximum number of peaks to fine" );
       s.append( "@param MinPeakIntensity the minimum intensity for a peak" );
       s.append( "@param MinTimeChannel The minimum time channel to use" );
       s.append( "@param MaxTimeChannel The maximum time channel to use" );
       s.append( "@param PixelRows The rows and columns to use" );
       
       s.append( "@return A Vector of Peak_new Objects with data entries " );
       s.append( "cleared( no References to the DataSet) "  );

       s.append( "@error No Area Grids. The DataSet must have area grids "  );
       s.append( "@error index out of bounds if Grid Id's do not match grids" );
       s.append( " @error null pointer exceptions, etc. See stack trace" );
       return s.toString(  );
  }

  /**
	 *  Finds the centroid peaks for this data set as follows:
	 *  First it finds all peaks using the FindPeaks method. Next it 
	 *  Centroids each of these peaks using GetPeaks with cutoffs changing
	 *  by 50% between current cutoff and the background, until finished.
	 *  Finished means that the extents are too large, the cutoff is too close
	 *  to the background, etc. Next it eliminates peaks whose extents hit each other
	 *  or that are null. Finally it converts the form of the peaks from
	 *   GetPeaks to a Peak_new object and  returns it.
	 */
	public Object calculate() {
		Object Res = null;
		try {

			Object O = ( new FindPeaks( DS, moncount, MaxNumPeaks,
					MinPeakIntensity, MinTimeChannel, MaxTimeChannel, PixelRows ) )
					.getResult();

			if ( O instanceof ErrorString )
				return O;
			Vector V = (Vector) O;
			for( int i = 0;i < V.size();i++ ){
				Peak P =(Peak) ( V.elementAt( i ) );
				V.setElementAt( GetPeakR( DS,P ),i );
			}
		
			
			for( int i = 0; i < V.size(); i++ ){
				PeakInfo P =(PeakInfo)V.elementAt( i );
				if( P != null )
				
					for( int j = V.size() - 1; j > i; j-- ){
						PeakInfo P1 = (PeakInfo)V.elementAt( j );
						if( P1 != null )
						if( P.hits( P1 ) )
							V.remove( j );
					}
			}
			//set up sequence numbers
			for( int i = 0; i  < V.size(); i++ ){
				Peak_new pk_n =((PeakInfo)V.elementAt( i ) ).makePeak();
				
				V.setElementAt( pk_n, i );
			}
			
			
			for( int i = V.size() - 1; i >= 0 ; i-- ){
				if( V.elementAt( i ) == null )
					V.remove( i );
			}
			
			return V;
		} catch ( Exception ss ) {
			
			String[] S = Command.ScriptUtil.GetExceptionStackInfo( ss, true, 3 );
			String SS = ss.toString() + "\n";
			if( S !=  null )
				for( int i = 0; i < S.length; i++ )
					SS += S[ i ] + "\n";
			Res = new ErrorString( SS );
			
		}
		
		return Res;
		
	}
	
	
	//Repeatedly runs DataSetTools.operator.Generic.TOF_SCD.GetPeak with 
	//different cutoffs until done.
	private PeakInfo GetPeakR( DataSet DS, Peak P ){
		
		int x = (int)P.x();
		int y = (int) P.y();
		int z  = (int)P.z();
		boolean done  = false;
		int gridID = P.detnum();
		IDataGrid grid = Grid_util.getAreaGrid( DS, gridID ); 
		int nrows = grid.num_rows();
	    int ncols = grid.num_cols();

		float cutoff = .8f*grid.getData_entry( y,x ).getY_values()[ z ];;
	    int nchan = DS.getData_entry( 0 ).getY_values().length;
	    
	    PeakInfo PP = GetPeak.getPeakInfo( y, x, z, gridID, DS, cutoff );
	    done = ( PP ==  null );
	    PeakInfo PP2 = null;
		while( !done ){
			if( PP == null )
				done = false;
		    else if( Float.isNaN( PP.getCalcBackgroundLevel() ) )
				done = false;
			else if( PP.getNCells() < 5 )
				done = false;
			else if( PP.getNCells() > .8*PP.getNCellsExtent() )
				done = false;
			else if( ( PP.maxX - PP.minX > .2*ncols )||( PP.maxY - PP.minY > .2*nrows )||( PP.maxZ - PP.minZ > .1*nchan ) ){
				done = true;
				PP = PP2;
			}
				
		    else if( cutoff < P.ipkobs()/200 )
		    	done = true;
			else if( cutoff - PP.getCalcBackgroundLevel() < .3 )
		   		done = true;
			if( !done ){
				PP2 = PP;
				
				float cutoff1 = ( cutoff + PP.getCalcBackgroundLevel() )/2.0f;
				
				if( cutoff1 >= .95*cutoff ) 
					cutoff = cutoff/2f;
				else if( Float.isNaN( cutoff1 ) )
					cutoff = cutoff/2.0f;
				else
					cutoff = cutoff1;
					
				PP = GetPeak.getPeakInfo( y, x, z, gridID, DS, cutoff );
			}
		}
		
		return PP;
		
	}
}
