/*
 * File:  IPeak.java 
 *             
 * Copyright (C) 2008, Ruth Mikkelson
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
 * * Modified:
 *
 * $Log$
 * Revision 1.2  2008/02/13 20:10:43  dennis
 * Minor fixes to java docs.
 *
 * Revision 1.1  2008/01/29 19:12:36  rmikk
 * Initial Checkin
 * 
 */
package DataSetTools.operator.Generic.TOF_SCD;
import gov.anl.ipns.MathTools.Geometry.*;
import java.io.*;


/**
 * @author Ruth
 *
 */
public interface IPeak {
   /**
    * @author Ruth
    *
    */
   public interface IPeak_IPNSout {
   
   }

   /**
    * Returns the chi value for the sample orientation corresponding to the
    * facility and instrument associated with this peak
    * @return the chi value for the sample orientation
    * 
    * NOTE: Set the facility to IPNS or null and instrument to SCD or null to get correct
    *       calculations with this peak.  To get site/instrument specific output
    *       set the facility and instrument correspondingly
    *       
    * @see  #setFacility(String)
    * @see  #setInstrument(String)
    */
   float    chi() ;
   
   /**
    * Returns the phi value for the sample orientation corresponding to the
    * facility and instrument associated with this peak
    * @return the phi value for the sample orientation
    * 
    * NOTE: Set the facility to IPNS or null and instrument to SCD or null to get correct
    *       calculations with this peak.  To get site/instrument specific output
    *       set the facility and instrument correspondingly
    *       
    * @see  #setFacility(String)
    * @see  #setInstrument(String)
    */
   float    phi();
   
   /**
    * Returns the omega value for the sample orientation corresponding to the
    * facility and instrument associated with this peak
    * @return the omega value for the sample orientation
    * 
    * NOTE: Set the facility to IPNS or null and instrument to SCD or null to get correct
    *       calculations with this peak.  To get site/instrument specific output
    *       set the facility and instrument correspondingly
    *       
    * @see  #setFacility(String)
    * @see  #setInstrument(String)
    */
   float    omega();
   
  
   
   /**
    * Returns the distance in cm of this pixel from the center of the detector
    *   in the detector's "x" direction     * @return the distance in cm of this pixel from the center in the detector
    *         x direction
    */
   float    xcm();
   
   
   /**
    * Returns the distance in cm of this pixel from the center of the detector
    *   in the detector's "y" direction 
    * @return the distance in cm of this pixel from the center in the detector
    *         y direction
    */
   float    ycm();
   
   /**
    * Returns the wavelength corresponding to this peak
    * @return the wavelength corresponding to this peak
    */
   float    wl();
   
   /**
    * Returns the x( column)value in the detector corresponding to this peak
    * @return the x( column)value in the detector corresponding to this peak
    */
   float    x();
   
   /**
    * Returns the y( row) value in the detector corresponding to this peak
    * @return the y( row)value in the detector corresponding to this peak
    */
   float    y();
   
   
   /**
    * Returns the z(channel) value corresponding to this peak. The first
    * channel has a z value of zero at this point. 
    * 
    * @return the z(channel) value corresponding to this peak
    */
   float    z();
   
   /**
    *  Returns the h( Miller index) value corresponding to this peak
    * @return the h( Miller index) value corresponding to this peak or 
    *         zero if cannot be determined
    */
   float    h();
   
   /**
    *  Returns the k( Miller index) value corresponding to this peak
    * @return the k( Miller index) value corresponding to this peak or 
    *         zero if cannot be determined
    */
   float    k();
   
   /**
    *  Returns the l( Miller index) value corresponding to this peak
    * @return the l( Miller index) value corresponding to this peak or 
    *         zero if cannot be determined
    */
   float    l();
   
   /**
    * Returns the sequence number associated with this peak.
    *              
    * @return the sequence number.
    */
   int      seqnum();
   
   /**
    * Returns the time associated with this peak
    * 
    * @return the time associated with this peak
    */
   float    time();
   
   /**
    * Returns the intensity at the given pixel and time associated with this 
    *       peak
    *       
    * @return the intensity at the given pixel and time associated with this 
    *       peak
    */
   int    ipkobs();
   
   /**
    * Returns the integrated peak intensity around the given pixel and time  
    *       associated with this peak
    *       
    * @return the integrated peak intensity around the given pixel and time  
    *       associated with this peak
    *       
    */
   float    inti();
   
   /**
    * Returns the error in the integrated peak intensity around the given  
    *       pixel and time  associated with this peak
    *       
    * @return the error in the integrated peak intensity around the given  
    *       pixel and time  associated with this peak
    *       
    */
   float    sigi();
   
   /**
    * Returns the value of the flag indicating the status of this peak
    * 
    * @return the value of the flag indicating the status of this peak
    */
   int    reflag();
   
   
   /**
    * Returns the ID associated with the detector that this peak is on
    * @return the ID associated with the detector that this peak is on
    */
   int  detnum();
   
   /**
    * Returns the run number associated with this peak
    * @return the run number associated with this peak
    */
   int    nrun();
   
   /**
    *  Returns the distance in cm from scattering center to Sample
    *  @return the distance in cm from scattering center to Sample
    */
   float  L1();
   
   /**
    * Returns the  "Q" vector(really Q/2pi) associated with this peak. This
    *     Q vector is adjusted by the sample orientation 
    * @return the  Q/2pi vector in crystal coordinates associated with this peak
    */
   double[]  getUnrotQ();
   
   /**
    * Returns the  "Q" vector(really Q/2pi) associated with this peak. This
    *     Q vector is NOT adjusted by the sample orientation 
    *     
    * @return the  Q/2pi vector in laboratory coordinates associated with this peak
    */
   float[]  getQ();
   
   /**
    * Returns the distance this pixel is from an edge of the detector
    * 
    * @return the distance this pixel is from an edge of the detector
    */
   float   nearedge();
   
   
   /**
    * Returns the intensity in the monitor associated with this pixel's time
    * 
    * @return the intensity in the monitor associated with this pixel's time
    */
   float     monct();
   
   /**
    * Returns the orientation matrix
    * @return the orientation matrix
    */
   float[][] UB();
   
   //-------- set methods --------------------
   /*
     Note: The peak objects should be immutable.  However, some fields are 
     commonly determined later or changed.  These methods should not change
     any other fields(except seqnum) that have legitimate values, otherwise 
     an exception should be thrown.
    */
   /**
    * Sets the h,k and l values
    * @exception any of this peak's current h,k and l values known
    */
   void sethkl(float h, float k, float l) throws IllegalArgumentException;
   
   /**
    * Sets this peaks UB matrix
    * @param UB  this peaks UB matrix. It then calculates the h,k, and l values
    * @throws IllegalArgumentException if the h,k,and l values are known
    */
   void UB( float[][] UB) throws IllegalArgumentException;
   
   
   /**
    * Sets the intensity at the given pixel and time associated with this 
    *       peak
    *       
    * @param pkObs the intensity at the given pixel and time associated with this 
    *       peak
    */
   int    ipkobs( int pkObs);
   
   /**
    * Sets this peaks Integrated intensity  error
    *
    * @param sig  the error in the integrated intensity error
    * 
    * NOTE: No exceptions have to be thrown here
    */
   float sigi(  float sig);
   
   /**
    * Sets this peaks Integrated intensity  
    *
    * @param inti  the new integrated intensity
    * 
    * NOTE: No exceptions have to be thrown here
    */
   float inti( float inti);
   
   /**
    * Sets the monitor Count
    * @param monitorCount  The new monitorCount
    * 
    *
    */
   float monct( float monitorCount);
   
   
   /**
    * Sets the ref flag
    * @param flag  The new code for this flag
    * 
    * @see #reflag()
    */
   int reflag( int flag);
   
   /**
    * Sets the sequence number for this peak
    * @param seq  the new sequence number
    * 
    * No flag is thrown if this changes
    */
   int seqnum( int seq);
   
   /**
    * Sets the facility name associated with this peak. 
    * @param facilityName  The facility name or null, which sets it to
    *        IPNS and calculation mode
    * @see #chi
    */
   void setFacility( String facilityName);
   
   
   /**
    * Sets the Instrument name associated with this peak. 
    * @param instrumentName  The Instrument name or null, which sets it to
    *       SCD and calculation mode
    * @see #chi
    */
   void setInstrument( String instrumentName);
   
   //------------copy/clone/factory methods---------------------
   
   /**
    *  clones the peak
    *  @return a clone of this peak
    
   Object  clone();
   */
   
   /**
    * Uses the current peak as a peak factory. Changes the x,y and z values and
    * all values derived from it and creates a new Peak
    * 
    * @param x  the column in the detector associated with the new peak
    * @param y  the row in the detector associated with the new peak
    * @param z  the time channel associated with the new peak
    * 
    * @return  a new peak with the x,y,z and all other derived values
    *          changed. The other fields stay the same
    *          
    * NOTE: These values can be Float.NaN, but it is not a good idea.
    *     
    */
   IPeak  createNewPeakxyz( float x, float y, float z, float wl);
    
   
   //---------------I/O---------------------
   
   /**
    * Writes out the information in this peak to a file. If the common fields
    * of this peak differ from the previous peak, a common header may be 
    * written out first.
    * 
    * @param f    The OutputStream to which the writing will go
    * @param prevPeak  The previous peak written out or null
    * @param Facility   The facility conventions to use for displaying things
    *                   like sample orientation data
    * @param Instrument   The Instrument conventions at this facility to use 
    *                   for displaying things like sample orientation data
    */
  // void writePeak( OutputStream f, IPeak prevPeak,String Facility, String Instrument);
   
   /**
    * Reads information on the next peak entries from the given InputStream. 
    * The previous peak should invoke this method to get the new peak. The 
    * common fields of the new peak will be set from the previous peak if
    * there is no information in the file
    *    
    * @param f    The InputStream from which the data for this peak will be
    *             read
    * @param Facility   The facility conventions to use for displaying things
    *                   like sample orientation data
    * @param Instrument   The Instrument conventions at this facility to use 
    *                   for displaying things like sample orientation data
    * @return  A new peak
    */
//   IPeak readPeak( InputStream f, String Facility, String Instrument);
   
   /**
    * Writes out the information in this peak to a file. If the common fields
    * of this peak differ from the previous peak, a common header may be 
    * written out first.
    * 
    * @param f The OutputStream to which the writing will go
    * @param prevPeak The previous peak written out or null
    */
//    void writePeak( OutputStream f, IPeak prevPeak);
    
    /**
      * Reads information on the next peak entries from the given InputStream. 
    * The previous peak should invoke this method to get the new peak. The 
    * common fields of the new peak will be set from the previous peak if
    * there is no information in the file
    *    
    * @param f     The InputStream from which the data for this peak will be
    *             read
    *             
    * @return  A new peak
     */
//    IPeak readPeak( InputStream f);
   
}
