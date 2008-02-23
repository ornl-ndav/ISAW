/*
 * File: IntegrateUtils.java 
 *
 * Copyright (C) 2002-2006, Peter Peterson, Ruth Mikkelson
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
 * Some of this work was supported by the National Science Foundation under
 * grant number DMR-0218882
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log: IntegrateUtils.java,v $
 * Revision 1.4  2008/02/13 20:10:43  dennis
 * Minor fixes to java docs.
 *
 * Revision 1.3  2008/01/29 20:30:41  rmikk
 * Replaced Peak by IPeak
 * Added Integrate methods that replaced ids[][] by a data grid or data set and
 *     grid ID.
 *
 * Revision 1.2  2006/12/19 16:55:28  dennis
 * Added methods checkReal() and minmaxhkl() from Integrate1 class.
 *
 * Revision 1.1  2006/12/19 05:16:28  dennis
 * Collection of static methods used by the various versions of the
 * SCD integrate operators.  These particular methods were factored
 * out of the Integrate_new class.
 *
 *
 */
package Operators.TOF_SCD;

import Command.ScriptUtil;
import DataSetTools.dataset.*;
import DataSetTools.operator.DataSet.Attribute.LoadSCDCalib;
import DataSetTools.operator.DataSet.Information.XAxis.SCDQxyz;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.trial.VecQToTOF;
import DataSetTools.util.*;
import DataSetTools.math.*;
import gov.anl.ipns.MathTools.Geometry.*;
import java.util.Vector;
import DataSetTools.instruments.*;
import Operators.TOF_SCD.SCD_LogUtils;

/**
 *  This class contains common static methods shared by the various
 *  integrate operators.
 */
public class IntegrateUtils 
{
  public static boolean DEBUG = false; 

  /**
   * This method integrates the peak by looking at a rectangular "shoebox"
   * around the specified peak.  The volume specified by the shoebox is assumed 
   * to be the peak.  The border voxels (just outside the shoebox in 3D) are 
   * taken as the background.  If the peak position is too close to the edge
   * of the volume of data, it cannot be integrated and this method just
   * returns.
   */
   public static void integrateShoebox( IPeak         peak,
                                        DataSet      ds,
                                        int          ids[][],
                                        int          colXrange[],
                                        int          rowYrange[],
                                        int          timeZrange[],
                                        StringBuffer log )
   {
     // set up where the peak is located
     int cenX=(int)Math.round(peak.x());
     int cenY=(int)Math.round(peak.y());
     int cenZ=(int)Math.round(peak.z());

     // we will consider the specified shoe box to the the part of the peak
     // that is included and will consider the background to be the border
     // "voxels" in 3D.  So we need to have some extra space around the peak.
     // If we don't, we'll just return without integrating the peak.

     int minZ = 0;
     int maxZ = ds.getData_entry(ids[1][1]).getY_values().length - 1;
     if ( cenZ + timeZrange[0] < minZ )  // too close to time channel 0
     {
       peak.reflag(0);
       return;
     }

     if ( cenZ + timeZrange[1] > maxZ )  // too close to max time channel
     {
       peak.reflag(0);
       return;
     }

     int minX = 1;                           // in ids[][] the first index
     int maxX = ids.length-1;                // is the column (i.e. X) index

     if ( cenX + colXrange[0] < minX + 1 )
     {
       peak.reflag(0);
       return;
     }

     if ( cenX + colXrange[1] > maxX - 1 )
     {
       peak.reflag(0);
       return;
     }

     int minY = 1;                           // in ids[][] the second index
     int maxY = ids[1].length-1;             // is the row (i.e. Y) index

     if ( cenY + rowYrange[0] < minY + 1 )
     {
       peak.reflag(0);
       return;
     }

     if ( cenY + rowYrange[1] > maxY - 1 )
     {
       peak.reflag(0);
       return;
     }

     SCD_LogUtils.addLogHeader( log, peak );
                                             // size of peak "shoebox"
     int nX = colXrange[1]  - colXrange[0]  + 1;
     int nY = rowYrange[1]  - rowYrange[0]  + 1;

     float n_signal =  nX    *  nY;
     float n_total  = (nX+2) * (nY+2);
     float n_border = n_total - n_signal;
     float ratio    = n_signal/n_border;

     float slice_total = 0;      // slice total on peak + background region
     float p_sig_plus_back = 0;  // signal + background total for peak region
     float intensity;            // intensity at one voxel
     float border;               // total on border region only 
     float slice_I;              // signal - background on one slice
     float slice_sigI;           // sigI on one slice
     float totI = 0;             // cumulative I for all slices
     float totSigI = 0;          // cumulative sigI for all slices
     float slice_peak;           // largest intensity in slice
     int   slice_peak_x;         // x value of largest intensity in slice
     int   slice_peak_y;         // y value of largest intensity in slice
     boolean border_peak;        // set true if the largest value occurs on 
                                 // the border of this slice

                                          // range of x,y,z enclosed in shoebox
     int first_x = cenX + colXrange[0];
     int last_x  = cenX + colXrange[1];
     int first_y = cenY + rowYrange[0];
     int last_y  = cenY + rowYrange[1];
     int first_z = cenZ + timeZrange[0];
     int last_z  = cenZ + timeZrange[1];
     for(int k = first_z; k <= last_z;  k++)
     {
       slice_peak   = -1;
       slice_peak_x = -1;
       slice_peak_y = -1;
       slice_total  =  0;
       p_sig_plus_back = 0;
       for(int i = first_x - 1; i <= last_x + 1;   i++)
         for(int j = first_y - 1; j <= last_y + 1; j++)
       {
         intensity = getObs( ds, ids[i][j], k );
         slice_total += intensity;
         if ( i >= first_x  &&  i <= last_x &&    // check if pixel in peak 
              j >= first_y  &&  j <= last_y )     // region of this slice
           p_sig_plus_back += intensity;

         if ( intensity > slice_peak )
         {
           slice_peak = intensity;
           slice_peak_x = i;
           slice_peak_y = j;
         }
       }

       if ( slice_peak_x == first_x - 1 ||
            slice_peak_x == last_x  + 1 ||
            slice_peak_y == first_y - 1 ||
            slice_peak_y == last_y  + 1  )
         border_peak = true;
       else
         border_peak = false;

       border = slice_total - p_sig_plus_back;    // total on border region only
       slice_I    = p_sig_plus_back - ratio * border;
       slice_sigI = (float)Math.sqrt(p_sig_plus_back + ratio * ratio * border);

       totI += slice_I;
       totSigI = (float)Math.sqrt( slice_sigI * slice_sigI + totSigI * totSigI);

       SCD_LogUtils.addLogSlice( log,
                                 k-cenZ, k,
                                 slice_peak_x, slice_peak_y, (int)slice_peak,
                                 first_x, last_x, first_y, last_y,
                                 slice_I, slice_sigI, "Yes", border_peak );
     }

     peak.inti( totI );
     peak.sigi( totSigI );
     int ipkobs = (int)getObs( ds, ids[cenX][cenY], cenZ );
     peak.ipkobs( ipkobs );

     SCD_LogUtils.addLogPeakSummary( log, totI, totSigI );
   }

   /**
    * This method integrates the peak by looking at a rectangular "shoebox"
    * around the specified peak.  The volume specified by the shoebox is assumed 
    * to be the peak.  The border voxels (just outside the shoebox in 3D) are 
    * taken as the background.  If the peak position is too close to the edge
    * of the volume of data, it cannot be integrated and this method just
    * returns.
    * 
    * @param  peak     - A peakd that is to be integrated
    * @param  ds       - The data set with the peak
    * @param  minDX    - min delta col from peak to use
    * @param  maxDX    - max delta  col from peak to use
    * @param  minDY    - min delta row from peak to use
    * @param  maxDY    - max delta row  from peak to use
    * @param  minChan  - min delta timeChan  from peak to use
    * @param  maxChan  - max delta timeChan  from peak to use 
    * @param  log      - A buffer to save log information
    * 
    * NOTE: Step sizes from mins to maxs are 1.
    * 
    */
    public static void integrateShoebox( IPeak        peak,
                                         DataSet      ds,
                                         int          minDX, 
                                         int          maxDX,
                                         int          minDY,
                                         int          maxDY,
                                         int          minChan,
                                         int          maxChan,
                                         StringBuffer log      )
                                                throws IllegalArgumentException
    {
      // set up where the peak is located
      int cenX=(int)Math.round(peak.x());
      int cenY=(int)Math.round(peak.y());
      int cenZ=(int)Math.round(peak.z());
      IDataGrid ids = Grid_util.getAreaGrid( ds, peak.detnum());
      if( ids == null)
         throw new IllegalArgumentException("No detector "+peak.detnum()+
                                      " in this data set");
      // we will consider the specified shoe box to the the part of the peak
      // that is included and will consider the background to be the border
      // "voxels" in 3D.  So we need to have some extra space around the peak.
      // If we don't, we'll just return without integrating the peak.

      int minZ = 0;
      int maxZ = ids.getData_entry(1 ,1).getY_values().length - 1;
      if ( cenZ + minChan < minZ )  // too close to time channel 0
      {
        peak.reflag(0);
        return;
      }

      if ( cenZ + maxChan > maxZ )  // too close to max time channel
      {
        peak.reflag(0);
        return;
      }

      int minX = 1;                           // in ids[][] the first index
      int maxX = ids.num_cols();                // is the column (i.e. X) index

      if ( cenX + minDX < minX + 1 )
      {
        peak.reflag(0);
        return;
      }

      if ( cenX + maxDX > maxX - 1 )
      {
        peak.reflag(0);
        return;
      }

      int minY = 1;                           // in ids[][] the second index
      int maxY = ids.num_rows();             // is the row (i.e. Y) index

      if ( cenY + minDY < minY + 1 )
      {
        peak.reflag(0);
        return;
      }

      if ( cenY + maxDY > maxY - 1 )
      {
        peak.reflag(0);
        return;
      }

      SCD_LogUtils.addLogHeader( log, peak );
                                              // size of peak "shoebox"
      int nX = 2;//assume step size 1 maxDX  - minDX  + 1;
      int nY = 2;// assume step size 1 maxDY  - minDY  + 1;

      float n_signal =  nX    *  nY;
      float n_total  = (nX+2) * (nY+2);
      float n_border = n_total - n_signal;
      float ratio    = n_signal/n_border;

      float slice_total = 0;      // slice total on peak + background region
      float p_sig_plus_back = 0;  // signal + background total for peak region
      float intensity;            // intensity at one voxel
      float border;               // total on border region only 
      float slice_I;              // signal - background on one slice
      float slice_sigI;           // sigI on one slice
      float totI = 0;             // cumulative I for all slices
      float totSigI = 0;          // cumulative sigI for all slices
      float slice_peak;           // largest intensity in slice
      int   slice_peak_x;         // x value of largest intensity in slice
      int   slice_peak_y;         // y value of largest intensity in slice
      boolean border_peak;        // set true if the largest value occurs on 
                                  // the border of this slice

                                           // range of x,y,z enclosed in shoebox
      int first_x = cenX + minDX;
      int last_x  = first_x+1;//cenX + maxDX;
      int first_y = cenY + minDY;
      int last_y  = first_y+1;//cenY + maxDY;
      int first_z = cenZ + minChan;
      int last_z  = first_z+1;//cenZ + maxChan;
      for(int k = first_z; k <= last_z;  k++)
      {
        slice_peak   = -1;
        slice_peak_x = -1;
        slice_peak_y = -1;
        slice_total  =  0;
        p_sig_plus_back = 0;
        for(int i = first_x - 1; i <= last_x + 1;   i++)
          for(int j = first_y - 1; j <= last_y + 1; j++)
        {
          intensity = getObs( ds, ids.getData_entry(i,j), k );
          slice_total += intensity;
          if ( i >= first_x  &&  i <= last_x &&    // check if pixel in peak 
               j >= first_y  &&  j <= last_y )     // region of this slice
            p_sig_plus_back += intensity;

          if ( intensity > slice_peak )
          {
            slice_peak = intensity;
            slice_peak_x = i;
            slice_peak_y = j;
          }
        }

        if ( slice_peak_x == first_x - 1 ||
             slice_peak_x == last_x  + 1 ||
             slice_peak_y == first_y - 1 ||
             slice_peak_y == last_y  + 1  )
          border_peak = true;
        else
          border_peak = false;

        border = slice_total - p_sig_plus_back;    // total on border region only
        slice_I    = p_sig_plus_back - ratio * border;
        slice_sigI = (float)Math.sqrt(p_sig_plus_back + ratio * ratio * border);

        totI += slice_I;
        totSigI = (float)Math.sqrt( slice_sigI * slice_sigI + totSigI * totSigI);

        SCD_LogUtils.addLogSlice( log,
                                  k-cenZ, k,
                                  slice_peak_x, slice_peak_y, (int)slice_peak,
                                  first_x, last_x, first_y, last_y,
                                  slice_I, slice_sigI, "Yes", border_peak );
      }

      peak.inti( totI );
      peak.sigi( totSigI );
      int ipkobs = (int)getObs( ds, ids.getData_entry(  cenX, cenY), cenZ );
      peak.ipkobs( ipkobs );

      SCD_LogUtils.addLogPeakSummary( log, totI, totSigI );
    }

  /**
   * This method integrates the peak by looking at five time slices
   * centered at the one the peak exists on. It grows a rectangle on
   * each time slice to get the maximum I/dI for each time slice then
   * adds the results from each time slice to maximize the total I/dI.
   */
  public static void integratePeak( IPeak         peak, 
                                    DataSet      ds, 
                                    int[][]      ids,
                                    int[]        timeZrange, 
                                    int          increaseSlice, 
                                    StringBuffer log) {

    // For debugging purposes, it' helpful to track what's going on in some 
    // cases.  To track what is done with a particular peak, specify the hkl
    // values and add println() statements to dump out needed values 
    // if trace is true.
    boolean trace = false;
    int trace_h = -1;
    int trace_k =  3;
    int trace_l =  3;
    if ( Math.round(peak.h()) == trace_h &&
         Math.round(peak.k()) == trace_k &&
         Math.round(peak.l()) == trace_l  )
      trace = true;
    trace = false;   // disable trace for now.

    // set up where the peak is located
    float[] tempIsigI=null;
    int cenX=(int)Math.round(peak.x());
    int cenY=(int)Math.round(peak.y());
    int cenZ=(int)Math.round(peak.z());

    // set up the time slices to integrate
    int minZrange=timeZrange[0];
    int maxZrange=timeZrange[1];
    int[] zrange=new int[maxZrange-minZrange+1];
    for( int i=0 ; i<zrange.length ; i++ )
      zrange[i]=cenZ+i+minZrange;
    minZrange=cenZ;
    maxZrange=cenZ;
    for( int i=0 ; i<zrange.length ; i++ ){
      if(zrange[i]<minZrange) minZrange=zrange[i];
      if(zrange[i]>maxZrange) maxZrange=zrange[i];
    }
    int minZ=0;
    int maxZ=ds.getData_entry(ids[1][1]).getX_scale().getNum_x();
    for( int i=0 ; i<zrange.length ; i++ ){           // can't integrate past
      if( zrange[i]<minZ || zrange[i]>=maxZ ) return; // ends of time axis
    }

    // determine the range in index
    int indexZmin=0;
    int indexZcen=0;
    int indexZmax=0;
    for( int i=0 ; i<zrange.length ; i++ ){
      if(zrange[i]==minZrange) indexZmin=i;
      if(zrange[i]==cenZ)      indexZcen=i;
      if(zrange[i]==maxZrange) indexZmax=i;
    }

    SCD_LogUtils.addLogHeader( log, peak );

    // initialize variables for the slice integration
    float[][] IsigI=new float[zrange.length][2]; // 2nd index is I,dI
    float Itot=0f;
    float dItot=0f;
    StringBuffer innerLog=new StringBuffer(40);
    String[] integSliceLogs=new String[zrange.length];

    // integrate the cenZ time slice
    innerLog.delete(0,innerLog.length());
    innerLog.append(SCD_LogUtils.formatInt(cenX)+"  "+
                    SCD_LogUtils.formatInt(cenY) +
                    SCD_LogUtils.formatInt(getObs(ds,ids[cenX][cenY],cenZ),6));
    tempIsigI=integratePeakSlice(ds,ids,cenX,cenY,cenZ,increaseSlice,innerLog);
    integSliceLogs[indexZcen]=innerLog.toString();
    // update the list of integrals if intensity is positive
    if(tempIsigI[0]!=0f){
      IsigI[indexZcen][0]=tempIsigI[0];
      IsigI[indexZcen][1]=tempIsigI[1];
      if(tempIsigI[0]>0f){
        Itot=tempIsigI[0];
        dItot=tempIsigI[1];
      }
    }
    if( tempIsigI[0]<=0f ){ // shrink what is calculated
      minZrange=cenZ+1;
      maxZrange=cenZ-1;
    }

    float maxP;
    // integrate the time slices before the peak
    for( int k=indexZcen-1 ; k>=0 ; k-- ){
      maxP=getObs(ds,ids[cenX][cenY],zrange[k]);
      if(zrange[k]>=minZrange){
        // determine the local maximum
        for( int i=cenX-1 ; i<=cenX+1 ; i++ ){
          for( int j=cenY-1 ; j<=cenY+1 ; j++ ){
            if( i==(int)Math.round(peak.x()) && j==(int)Math.round(peak.y()))
              continue;
            if(getObs(ds,ids[i][j],zrange[k])>maxP){
              maxP=getObs(ds,ids[i][j],zrange[k]);
              cenX=i;
              cenY=j;
            }
          }
        }
      }
      // clear the log
      innerLog.delete(0,innerLog.length());
      innerLog.append(SCD_LogUtils.formatInt(cenX)+"  "+
                      SCD_LogUtils.formatInt(cenY)+
                      SCD_LogUtils.formatInt(maxP,6));
      if(zrange[k]<minZrange){
        integSliceLogs[k]=innerLog.toString();
        continue;
      }
      tempIsigI
        =integratePeakSlice(ds,ids,cenX,cenY,zrange[k],increaseSlice,innerLog);
      integSliceLogs[k]=innerLog.toString();
      // update the list of integrals if intensity is positive
      if(tempIsigI[0]!=0f){
        IsigI[k][0]=tempIsigI[0];
        IsigI[k][1]=tempIsigI[1];
      }
      if( tempIsigI[0]<=0f ){ // shrink what is calculated
        minZrange=zrange[k]+1;
        continue;
      }

      // shrink what is calculated if the slice would not be added
                // this is not fully correct since Itot and dItot aren't
                // changing when a slice should be added
      if(! increasingIsigI(Itot,dItot,IsigI[k][0],IsigI[k][1]) &&
         ! is_significant (Itot,dItot,IsigI[k][0],IsigI[k][1]) ) {
        minZrange=zrange[k]+1;
        continue;
      }
    }

    // reset the location of the peak in x and y
    cenX=(int)Math.round(peak.x());
    cenY=(int)Math.round(peak.y());

    // integrate the time slices after the peak
    try{
      for( int k=indexZcen+1 ; k<zrange.length ; k++ ){
        maxP=getObs(ds,ids[cenX][cenY],zrange[k]);
        if(zrange[k]<=maxZrange){
          // determine the local maximum
          for( int i=cenX-1 ; i<=cenX+1 ; i++ ){
            for( int j=cenY-1 ; j<=cenY+1 ; j++ ){
              if( i==(int)Math.round(peak.x()) && j==(int)Math.round(peak.y()))
                continue;
              if(getObs(ds,ids[i][j],zrange[k])>maxP){
                maxP=getObs(ds,ids[i][j],zrange[k]);
                cenX=i;
                cenY=j;
              }
            }
          }
        }
        // clear the log
        innerLog.delete(0,innerLog.length());
        innerLog.append( SCD_LogUtils.formatInt(cenX)+"  "+
                         SCD_LogUtils.formatInt(cenY)+
                         SCD_LogUtils.formatInt(maxP,6));
        if(zrange[k]>maxZrange){
          integSliceLogs[k]=innerLog.toString();
          continue;
        }
        tempIsigI
          =integratePeakSlice(ds,ids,cenX,cenY,zrange[k],increaseSlice,innerLog);
        integSliceLogs[k]=innerLog.toString();
        // update the list of integrals if intensity is positive
        if(tempIsigI[0]!=0f){
          IsigI[k][0]=tempIsigI[0];
          IsigI[k][1]=tempIsigI[1];
        }
        if( tempIsigI[0]<=0f ){ // shrink what is calculated
          maxZrange=zrange[k]-1;
          continue;
        }

        // shrink what is calculated if the slice would not be added
        // this is not fully correct since Itot and dItot aren't
        // changing when a slice should be added
        if(! increasingIsigI(Itot,dItot,IsigI[k][0],IsigI[k][1]) &&
           ! is_significant (Itot,dItot,IsigI[k][0],IsigI[k][1]) ) {
          maxZrange=zrange[k]-1;
          continue;
        }
      }
    }catch(ArrayIndexOutOfBoundsException e){
      // let it drop on the floor
    }

    // determine the range to bother trying to sum over
    for( int i=0 ; i<zrange.length ; i++ ){
      if(zrange[i]==minZrange) indexZmin=i;
      if(zrange[i]==cenZ)      indexZcen=i;
      if(zrange[i]==maxZrange) indexZmax=i;
    }

    // figure out what to add to the total

    // cenZ has already been added (but do a quick check anyhow)
    if( Itot==0f && dItot==0f){
      indexZmin=indexZcen+1;
      indexZmax=indexZcen-1;
    }

    // now the previous slices
    for( int k=indexZcen-1 ; k>=indexZmin ; k-- ){
      if( increasingIsigI(Itot,dItot,IsigI[k][0],IsigI[k][1]) ||
          is_significant (Itot,dItot,IsigI[k][0],IsigI[k][1]) ) {
        Itot=Itot+IsigI[k][0];
        dItot=(float)Math.sqrt(dItot*dItot+IsigI[k][1]*IsigI[k][1]);
      }else{
        minZrange=zrange[k]+1;
        indexZmin=k+1;
      }
    }

    // now the following slices
    for( int k=indexZcen+1 ; k<=indexZmax ; k++ ){
      if( increasingIsigI(Itot,dItot,IsigI[k][0],IsigI[k][1])  ||
          is_significant (Itot,dItot,IsigI[k][0],IsigI[k][1])  ) {
        Itot=Itot+IsigI[k][0];
        dItot=(float)Math.sqrt(dItot*dItot+IsigI[k][1]*IsigI[k][1]);
      }else{
        maxZrange=zrange[k]-1;
        indexZmax=k-1;
      }
    }

   if ( trace )
     for ( int k = 0; k < IsigI.length; k++ )
       System.out.println("slice, I, sigI = " + k +
                          ", " + IsigI[k][0] + ", " + IsigI[k][1] );

    // then add information to the log file
    if(log!=null){
      // each time slice
      for( int k=0 ; k<zrange.length ; k++ ){
        log.append( SCD_LogUtils.formatInt(zrange[k]-cenZ)+"   "+
                    SCD_LogUtils.formatInt(zrange[k])+"  ");
        if(integSliceLogs[k]!=null && integSliceLogs[k].length()>0){
          log.append(integSliceLogs[k]);
          if(integSliceLogs[k].length()<20)
            log.append("    NOT INTEGRATED");
        }else{
          log.append("-------- NOT INTEGRATED --------");
        }
        log.append(" "+SCD_LogUtils.formatFloat(IsigI[k][0]));
        log.append("  "+SCD_LogUtils.formatFloat(IsigI[k][1]));
        if(IsigI[k][0]>0f && IsigI[k][1]>0f)
          log.append(" "+SCD_LogUtils.formatFloat(IsigI[k][0]/IsigI[k][1])+
                     "      ");
        else
          log.append(" "+SCD_LogUtils.formatFloat(0f)+"      ");
        if( k>=indexZmin && k<=indexZmax )
          log.append("Yes\n");
        else
          log.append("No\n");
      }

      SCD_LogUtils.addLogPeakSummary( log, Itot, dItot );
    }

    // change the peak to reflect what we just did
    peak.inti(Itot);
    peak.sigi(dItot);
  }

  /**
   * This method integrates the peak by looking at five time slices
   * centered at the one the peak exsists on. It grows a rectangle on
   * each time slice to get the maximum I/dI for each time slice then
   * adds the results from each time slice to maximize the total I/dI.
   * 
   * @param   peak           The peak to integrate
   * @param   ds             The data set with the peak 
   * @param   minChan        min delta timeChan  from peak to use
   * @param   maxChan        max delta timeChan  from peak to use 
   * @param   increaseSlice  The amount to increase slicesize by
   * @param   log            A buffer for log information
   *
   */
  public static void integratePeak( IPeak        peak, 
                                    DataSet      ds, 
                                    int          minChan,
                                    int          maxChan,
                                    int          increaseSlice, 
                                    StringBuffer log) 
                                               throws IllegalArgumentException{

    // For debugging purposes, it' helpful to track what's going on in some 
    // cases.  To track what is done with a particular peak, specify the hkl
    // values and add println() statements to dump out needed values 
    // if trace is true.
    boolean trace = false;
    int trace_h = -1;
    int trace_k =  3;
    int trace_l =  3;
    if ( Math.round(peak.h()) == trace_h &&
         Math.round(peak.k()) == trace_k &&
         Math.round(peak.l()) == trace_l  )
      trace = true;
    trace = false;   // disable trace for now.

    // set up where the peak is located
    float[] tempIsigI=null;
    int cenX=(int)Math.round(peak.x());
    int cenY=(int)Math.round(peak.y());
    int cenZ=(int)Math.round(peak.z());

    // set up the time slices to integrate
    int minZrange=minChan;
    int maxZrange=maxChan;
    int[] zrange=new int[maxZrange-minZrange+1];
    for( int i=0 ; i<zrange.length ; i++ )
      zrange[i]=cenZ+i+minZrange;
    minZrange=cenZ;
    maxZrange=cenZ;
    for( int i=0 ; i<zrange.length ; i++ ){
      if(zrange[i]<minZrange) minZrange=zrange[i];
      if(zrange[i]>maxZrange) maxZrange=zrange[i];
    }
    IDataGrid grid = Grid_util.getAreaGrid( ds, peak.detnum());
    if( grid == null)
       throw new IllegalArgumentException( 
                              "DataSet does not contain given grid");
    int minZ=0;
    int maxZ=grid.getData_entry(1,1).getX_scale().getNum_x();
    for( int i=0 ; i<zrange.length ; i++ ){           // can't integrate past
      if( zrange[i]<minZ || zrange[i]>=maxZ ) return; // ends of time axis
    }

    // determine the range in index
    int indexZmin=0;
    int indexZcen=0;
    int indexZmax=0;
    for( int i=0 ; i<zrange.length ; i++ ){
      if(zrange[i]==minZrange) indexZmin=i;
      if(zrange[i]==cenZ)      indexZcen=i;
      if(zrange[i]==maxZrange) indexZmax=i;
    }

    SCD_LogUtils.addLogHeader( log, peak );

    // initialize variables for the slice integration
    float[][] IsigI=new float[zrange.length][2]; // 2nd index is I,dI
    float Itot=0f;
    float dItot=0f;
    StringBuffer innerLog=new StringBuffer(40);
    String[] integSliceLogs=new String[zrange.length];

    // integrate the cenZ time slice
    innerLog.delete(0,innerLog.length());
    innerLog.append(SCD_LogUtils.formatInt(cenX)+"  "+
                    SCD_LogUtils.formatInt(cenY) +
                    SCD_LogUtils.formatInt(getObs(ds,grid.getData_entry(cenX, cenY),cenZ),6));
    tempIsigI=integratePeakSlice(ds,grid,cenX,cenY,cenZ,increaseSlice,innerLog);
    integSliceLogs[indexZcen]=innerLog.toString();
    // update the list of integrals if intensity is positive
    if(tempIsigI[0]!=0f){
      IsigI[indexZcen][0]=tempIsigI[0];
      IsigI[indexZcen][1]=tempIsigI[1];
      if(tempIsigI[0]>0f){
        Itot=tempIsigI[0];
        dItot=tempIsigI[1];
      }
    }
    if( tempIsigI[0]<=0f ){ // shrink what is calculated
      minZrange=cenZ+1;
      maxZrange=cenZ-1;
    }

    float maxP;
    // integrate the time slices before the peak
    for( int k=indexZcen-1 ; k>=0 ; k-- ){
      maxP=getObs(ds,grid.getData_entry( cenX, cenY),zrange[k]);
      if(zrange[k]>=minZrange){
        // determine the local maximum
        for( int i=cenX-1 ; i<=cenX+1 ; i++ ){
          for( int j=cenY-1 ; j<=cenY+1 ; j++ ){
            if( i==(int)Math.round(peak.x()) && j==(int)Math.round(peak.y()))
              continue;
            if(getObs(ds,grid.getData_entry( i, j),zrange[k])>maxP){
              maxP=getObs(ds,grid.getData_entry( i, j),zrange[k]);
              cenX=i;
              cenY=j;
            }
          }
        }
      }
      // clear the log
      innerLog.delete(0,innerLog.length());
      innerLog.append(SCD_LogUtils.formatInt(cenX)+"  "+
                      SCD_LogUtils.formatInt(cenY)+
                      SCD_LogUtils.formatInt(maxP,6));
      if(zrange[k]<minZrange){
        integSliceLogs[k]=innerLog.toString();
        continue;
      }
      tempIsigI
        =integratePeakSlice(ds,grid,cenX,cenY,zrange[k],increaseSlice,innerLog);
      integSliceLogs[k]=innerLog.toString();
      // update the list of integrals if intensity is positive
      if(tempIsigI[0]!=0f){
        IsigI[k][0]=tempIsigI[0];
        IsigI[k][1]=tempIsigI[1];
      }
      if( tempIsigI[0]<=0f ){ // shrink what is calculated
        minZrange=zrange[k]+1;
        continue;
      }

      // shrink what is calculated if the slice would not be added
                // this is not fully correct since Itot and dItot aren't
                // changing when a slice should be added
      if(! increasingIsigI(Itot,dItot,IsigI[k][0],IsigI[k][1]) &&
         ! is_significant (Itot,dItot,IsigI[k][0],IsigI[k][1]) ) {
        minZrange=zrange[k]+1;
        continue;
      }
    }

    // reset the location of the peak in x and y
    cenX=(int)Math.round(peak.x());
    cenY=(int)Math.round(peak.y());

    // integrate the time slices after the peak
    try{
      for( int k=indexZcen+1 ; k<zrange.length ; k++ ){
        maxP=getObs(ds,grid.getData_entry( cenX , cenY),zrange[k]);
        if(zrange[k]<=maxZrange){
          // determine the local maximum
          for( int i=cenX-1 ; i<=cenX+1 ; i++ ){
            for( int j=cenY-1 ; j<=cenY+1 ; j++ ){
              if( i==(int)Math.round(peak.x()) && j==(int)Math.round(peak.y()))
                continue;
              if(getObs(ds,grid.getData_entry( i,j),zrange[k])>maxP){
                maxP=getObs(ds,grid.getData_entry( i,j),zrange[k]);
                cenX=i;
                cenY=j;
              }
            }
          }
        }
        // clear the log
        innerLog.delete(0,innerLog.length());
        innerLog.append( SCD_LogUtils.formatInt(cenX)+"  "+
                         SCD_LogUtils.formatInt(cenY)+
                         SCD_LogUtils.formatInt(maxP,6));
        if(zrange[k]>maxZrange){
          integSliceLogs[k]=innerLog.toString();
          continue;
        }
        tempIsigI
          =integratePeakSlice(ds,grid,cenX,cenY,zrange[k],increaseSlice,innerLog);
        integSliceLogs[k]=innerLog.toString();
        // update the list of integrals if intensity is positive
        if(tempIsigI[0]!=0f){
          IsigI[k][0]=tempIsigI[0];
          IsigI[k][1]=tempIsigI[1];
        }
        if( tempIsigI[0]<=0f ){ // shrink what is calculated
          maxZrange=zrange[k]-1;
          continue;
        }

        // shrink what is calculated if the slice would not be added
        // this is not fully correct since Itot and dItot aren't
        // changing when a slice should be added
        if(! increasingIsigI(Itot,dItot,IsigI[k][0],IsigI[k][1]) &&
           ! is_significant (Itot,dItot,IsigI[k][0],IsigI[k][1]) ) {
          maxZrange=zrange[k]-1;
          continue;
        }
      }
    }catch(ArrayIndexOutOfBoundsException e){
      // let it drop on the floor
    }

    // determine the range to bother trying to sum over
    for( int i=0 ; i<zrange.length ; i++ ){
      if(zrange[i]==minZrange) indexZmin=i;
      if(zrange[i]==cenZ)      indexZcen=i;
      if(zrange[i]==maxZrange) indexZmax=i;
    }

    // figure out what to add to the total

    // cenZ has already been added (but do a quick check anyhow)
    if( Itot==0f && dItot==0f){
      indexZmin=indexZcen+1;
      indexZmax=indexZcen-1;
    }

    // now the previous slices
    for( int k=indexZcen-1 ; k>=indexZmin ; k-- ){
      if( increasingIsigI(Itot,dItot,IsigI[k][0],IsigI[k][1]) ||
          is_significant (Itot,dItot,IsigI[k][0],IsigI[k][1]) ) {
        Itot=Itot+IsigI[k][0];
        dItot=(float)Math.sqrt(dItot*dItot+IsigI[k][1]*IsigI[k][1]);
      }else{
        minZrange=zrange[k]+1;
        indexZmin=k+1;
      }
    }

    // now the following slices
    for( int k=indexZcen+1 ; k<=indexZmax ; k++ ){
      if( increasingIsigI(Itot,dItot,IsigI[k][0],IsigI[k][1])  ||
          is_significant (Itot,dItot,IsigI[k][0],IsigI[k][1])  ) {
        Itot=Itot+IsigI[k][0];
        dItot=(float)Math.sqrt(dItot*dItot+IsigI[k][1]*IsigI[k][1]);
      }else{
        maxZrange=zrange[k]-1;
        indexZmax=k-1;
      }
    }

   if ( trace )
     for ( int k = 0; k < IsigI.length; k++ )
       System.out.println("slice, I, sigI = " + k +
                          ", " + IsigI[k][0] + ", " + IsigI[k][1] );

    // then add information to the log file
    if(log!=null){
      // each time slice
      for( int k=0 ; k<zrange.length ; k++ ){
        log.append( SCD_LogUtils.formatInt(zrange[k]-cenZ)+"   "+
                    SCD_LogUtils.formatInt(zrange[k])+"  ");
        if(integSliceLogs[k]!=null && integSliceLogs[k].length()>0){
          log.append(integSliceLogs[k]);
          if(integSliceLogs[k].length()<20)
            log.append("    NOT INTEGRATED");
        }else{
          log.append("-------- NOT INTEGRATED --------");
        }
        log.append(" "+SCD_LogUtils.formatFloat(IsigI[k][0]));
        log.append("  "+SCD_LogUtils.formatFloat(IsigI[k][1]));
        if(IsigI[k][0]>0f && IsigI[k][1]>0f)
          log.append(" "+SCD_LogUtils.formatFloat(IsigI[k][0]/IsigI[k][1])+
                     "      ");
        else
          log.append(" "+SCD_LogUtils.formatFloat(0f)+"      ");
        if( k>=indexZmin && k<=indexZmax )
          log.append("Yes\n");
        else
          log.append("No\n");
      }

      SCD_LogUtils.addLogPeakSummary( log, Itot, dItot );
    }

    // change the peak to reflect what we just did
    peak.inti(Itot);
    peak.sigi(dItot);
  }

  /**
   * Integrate a peak while varying the range in x and y. This does
   * the hard work of growing the rectangle on a time slice to
   * maximize I/dI.
   */
  public static float[] integratePeakSlice(DataSet ds, IDataGrid ids, int Xcen,
                        int Ycen, int z, int increaseSlice, StringBuffer log){
    float[] IsigI=new float[2];
    float[] tempIsigI=new float[2];

    int[] init_rng= {Xcen-2,Ycen-2,Xcen+2,Ycen+2};
    int[] rng     = {Xcen-2,Ycen-2,Xcen+2,Ycen+2};
    int[] step    = {-1,-1,1,1};

    int itteration=0;
    final int MAX_ITTER=10;

    // initial run with default size for integration
    if( checkRange(ids,rng) ){
      tempIsigI=integrateSlice(ds,ids,rng,z);
      if(tempIsigI[0]==0f || tempIsigI[1]==0f){ // something wrong
        SCD_LogUtils.formatRange(rng,log);
        itteration=MAX_ITTER;
      }

      if( tempIsigI[0]>0f && tempIsigI[1]>0f ){
        IsigI[0]=tempIsigI[0];
        IsigI[1]=tempIsigI[1];
      }
    }
    int direction=0;
    for( int i=0 ; i<4 ; i++ ){
      itteration=0;
      direction=0;
      while(direction<2 && itteration<MAX_ITTER){ // only change direction once
        itteration++;                      // and allow a max num of itteration
        rng[i]=rng[i]+step[i];
        if( checkRange(ids,rng) ){
          tempIsigI=integrateSlice(ds,ids,rng,z);
          if(tempIsigI[0]==0f||tempIsigI[1]==0f){ // something wrong
            SCD_LogUtils.formatRange(rng,log);
            itteration=MAX_ITTER;
          }
          if( tempIsigI[0]>0f && tempIsigI[1]>0f ){
            if( IsigI[0]/IsigI[1]<tempIsigI[0]/tempIsigI[1]){
              IsigI[0]=tempIsigI[0];
              IsigI[1]=tempIsigI[1];
            }else{ // change direction
              step[i]=-1*step[i];
              rng[i]=rng[i]+step[i];
              direction++;
            }
          }
        }else{ // change direction
          step[i]=-1*step[i];
          rng[i]=rng[i]+step[i];
          direction++;
        }
      }
    }

    // use a fixed box
    if(IsigI[0]<=IsigI[1]*5f){
      if(checkRange(ids,init_rng) )
        IsigI=integrateSlice(ds,ids,init_rng,z);
      SCD_LogUtils.formatRange(init_rng,log);
      return IsigI;
    }

    // increase the size of the slice's integration (if requested)
    if(increaseSlice>0){
      rng[0]=rng[0]-increaseSlice;
      rng[1]=rng[1]-increaseSlice;
      rng[2]=rng[2]+increaseSlice;
      rng[3]=rng[3]+increaseSlice;
      if( checkRange(ids,rng) ){
        IsigI=integrateSlice(ds,ids,rng,z);
      }else{ // goes out of range
        rng[0]=rng[0]+increaseSlice;
        rng[1]=rng[1]+increaseSlice;
        rng[2]=rng[2]-increaseSlice;
        rng[3]=rng[3]-increaseSlice;
      }
    }

    // add information to the log and return the integral
    SCD_LogUtils.formatRange(rng,log);
    return IsigI;
  }
  /**
   * Integrate a peak while varying the range in x and y. This does
   * the hard work of growing the rectangle on a time slice to
   * maximize I/dI.
   */
  public static float[] integratePeakSlice(DataSet ds, int[][] ids, int Xcen,
                        int Ycen, int z, int increaseSlice, StringBuffer log){
    float[] IsigI=new float[2];
    float[] tempIsigI=new float[2];

    int[] init_rng= {Xcen-2,Ycen-2,Xcen+2,Ycen+2};
    int[] rng     = {Xcen-2,Ycen-2,Xcen+2,Ycen+2};
    int[] step    = {-1,-1,1,1};

    int itteration=0;
    final int MAX_ITTER=10;

    // initial run with default size for integration
    if( checkRange(ids,rng) ){
      tempIsigI=integrateSlice(ds,ids,rng,z);
      if(tempIsigI[0]==0f || tempIsigI[1]==0f){ // something wrong
        SCD_LogUtils.formatRange(rng,log);
        itteration=MAX_ITTER;
      }

      if( tempIsigI[0]>0f && tempIsigI[1]>0f ){
        IsigI[0]=tempIsigI[0];
        IsigI[1]=tempIsigI[1];
      }
    }
    int direction=0;
    for( int i=0 ; i<4 ; i++ ){
      itteration=0;
      direction=0;
      while(direction<2 && itteration<MAX_ITTER){ // only change direction once
        itteration++;                      // and allow a max num of itteration
        rng[i]=rng[i]+step[i];
        if( checkRange(ids,rng) ){
          tempIsigI=integrateSlice(ds,ids,rng,z);
          if(tempIsigI[0]==0f||tempIsigI[1]==0f){ // something wrong
            SCD_LogUtils.formatRange(rng,log);
            itteration=MAX_ITTER;
          }
          if( tempIsigI[0]>0f && tempIsigI[1]>0f ){
            if( IsigI[0]/IsigI[1]<tempIsigI[0]/tempIsigI[1]){
              IsigI[0]=tempIsigI[0];
              IsigI[1]=tempIsigI[1];
            }else{ // change direction
              step[i]=-1*step[i];
              rng[i]=rng[i]+step[i];
              direction++;
            }
          }
        }else{ // change direction
          step[i]=-1*step[i];
          rng[i]=rng[i]+step[i];
          direction++;
        }
      }
    }

    // use a fixed box
    if(IsigI[0]<=IsigI[1]*5f){
      if(checkRange(ids,init_rng) )
        IsigI=integrateSlice(ds,ids,init_rng,z);
      SCD_LogUtils.formatRange(init_rng,log);
      return IsigI;
    }

    // increase the size of the slice's integration (if requested)
    if(increaseSlice>0){
      rng[0]=rng[0]-increaseSlice;
      rng[1]=rng[1]-increaseSlice;
      rng[2]=rng[2]+increaseSlice;
      rng[3]=rng[3]+increaseSlice;
      if( checkRange(ids,rng) ){
        IsigI=integrateSlice(ds,ids,rng,z);
      }else{ // goes out of range
        rng[0]=rng[0]+increaseSlice;
        rng[1]=rng[1]+increaseSlice;
        rng[2]=rng[2]-increaseSlice;
        rng[3]=rng[3]-increaseSlice;
      }
    }

    // add information to the log and return the integral
    SCD_LogUtils.formatRange(rng,log);
    return IsigI;
  }


  /**
   * Integrate around the peak in the given time slice. This
   * integrates the region passed to it.
   */
  private static float[] integrateSlice(DataSet ds, int[][] ids,
                                                           int[] range, int z){
    float[] IsigI=new float[2];

    int minX=range[0];
    int minY=range[1];
    int maxX=range[2];
    int maxY=range[3];

    int ibxmin=minX-1;
    int ibxmax=maxX+1;
    int ibymin=minY-1;
    int ibymax=maxY+1;

    float isigtot = (float)((maxX-minX+1)*(maxY-minY+1));
    float ibktot  = (float)((ibxmax-ibxmin+1)*(ibymax-ibymin+1)-isigtot);
    float stob    = isigtot/ibktot;

    float ibtot=0f;
    float istot=0f;

    float intensity;

    for( int i=minX-1 ; i<=maxX+1 ; i++ ){
      for( int j=minY-1 ; j<=maxY+1 ; j++ ){
        intensity=getObs(ds,ids[i][j],z);
        ibtot=ibtot+intensity;
        if( i>=minX && i<=maxX && j>=minY && j<=maxY )
          istot=istot+intensity;
      }
    }

    ibtot=ibtot-istot;
    IsigI[0]=istot-stob*ibtot;
    IsigI[1]=(float)Math.sqrt(istot+stob*stob*ibtot);

    return IsigI;
  }

  /**
   * Integrate around the peak in the given time slice. This
   * integrates the region passed to it.
   */
  private static float[] integrateSlice(DataSet ds, IDataGrid ids,
                                                           int[] range, int z){
    float[] IsigI=new float[2];

    int minX=range[0];
    int minY=range[1];
    int maxX=range[2];
    int maxY=range[3];

    int ibxmin=minX-1;
    int ibxmax=maxX+1;
    int ibymin=minY-1;
    int ibymax=maxY+1;

    float isigtot = (float)((maxX-minX+1)*(maxY-minY+1));
    float ibktot  = (float)((ibxmax-ibxmin+1)*(ibymax-ibymin+1)-isigtot);
    float stob    = isigtot/ibktot;

    float ibtot=0f;
    float istot=0f;

    float intensity;

    for( int i=minX-1 ; i<=maxX+1 ; i++ ){
      for( int j=minY-1 ; j<=maxY+1 ; j++ ){
        intensity=getObs(ds,ids.getData_entry(i,j),z);
        ibtot=ibtot+intensity;
        if( i>=minX && i<=maxX && j>=minY && j<=maxY )
          istot=istot+intensity;
      }
    }

    ibtot=ibtot-istot;
    IsigI[0]=istot-stob*ibtot;
    IsigI[1]=(float)Math.sqrt(istot+stob*stob*ibtot);

    return IsigI;
  }

  /**
   * Utility method to determine whether adding I,dI to Itot,dItot
   * will increase the overall I/sigI ratio or not
   */
  public static boolean increasingIsigI( float Itot, float dItot,
                                         float I,    float dI)
  {
    if(I<=0f || dI==0f) return false;
    if(Itot==0f && dItot==0f) return true;

    float myItot=Itot+I;
    float myDItot=(float)Math.sqrt(dItot*dItot+dI*dI);

    return ( (Itot/dItot)<(myItot/myDItot) );
  }


  /**
   * Check whether or not the additional intensity is at least 1% of the
   * total AND the I/sigI ratio is at least 2.
   */
  public static boolean is_significant( float Itot,
                                        float dItot,
                                        float I,
                                        float dI)
  {
    // System.out.print("is_significant: " + Itot + ", " + dItot + 
    //                                ", " + I    + ", " + dI );
    if(I<=0f || dI==0f)
    {
      // System.out.println(" false, since I <=, dI = 0 ");
      return false;
    }

    if(Itot==0f && dItot==0f)
    {
      // System.out.println(" true, since Itot, dItot = 0 ");
      return true;
    }

    if ( (I > 0.01 * Itot) && I > 2*dI )
    {
      // System.out.println(" true, since I > 0.01 Itot && I > 2*dI ");
      return true;
    }
    else
    {
      // System.out.println(" false ");
      return false;
    }
  }


  /**
   * Determines whether the integration range lies on the detector.
   */
  public static boolean checkRange(int[][] ids, int[] range){
    int minX=range[0];
    int minY=range[1];
    int maxX=range[2];
    int maxY=range[3];

    if(minX<2) return false;

    if(minY<2) return false;

    if(maxX>ids.length-2) return false;

    if(maxY>ids[0].length-2) return false;

    return true;
  }

  /**
   * Determines whether the integration range lies on the detector.
   * @param ids  the grid for the detector
   * @param range  an array of ints with minx,miny,maxx,maxy
   * @return true if the range values represent pixels of the detector
   */
  public static boolean checkRange(IDataGrid ids, int[] range){
    int minX=range[0];
    int minY=range[1];
    int maxX=range[2];
    int maxY=range[3];

    if(minX<2) return false;

    if(minY<2) return false;

    if(maxX>ids.num_cols()-2) return false;

    if(maxY>ids.num_rows()-2) return false;

    return true;
  }

  /**
   * Put the peak at the nearest maximum within the given deltas
   */
  public static void movePeak( Peak peak, DataSet ds, int[][] ids,
                                int dx, int dy, int dz){
    int x=(int)Math.round(peak.x());
    int y=(int)Math.round(peak.y());
    int z=(int)Math.round(peak.z());

    int maxP=(int)Math.round(getObs(ds,ids[x][y],z));
    peak.ipkobs(maxP);
    int maxX=x;
    int maxY=y;
    int maxZ=z;

    float point=0f;
    for( int i=x-dx ; i<=x+dx ; i++ ){
      for( int j=y-dy ; j<=y+dy ; j++ ){
        for( int k=z-dz ; k<=z+dz ; k++ ){
          point=getObs(ds,ids[i][j],k);
          if(point>maxP){
            maxP=(int)Math.round(point);
            maxX=i;
            maxY=j;
            maxZ=k;
          }
          point=0f;
        }
      }
    }
    if(maxX!=x || maxY!=y || maxZ!=z){ // move to nearby maximum
      peak.pixel(maxX,maxY,maxZ);
      peak.ipkobs(maxP);
    }else{
      peak.pixel(x,y,z); // move it onto integer pixel postion
    }
  }
  
  /**
   * Put the peak at the nearest maximum within the given deltas
   */
  public static IPeak maxClosePeak( IPeak peak, DataSet ds, int[][] ids,
                                int dx, int dy, int dz){
    int x=(int)Math.round(peak.x());
    int y=(int)Math.round(peak.y());
    int z=(int)Math.round(peak.z());

    int maxP=(int)Math.round(getObs(ds,ids[x][y],z));
    peak.ipkobs(maxP);
    int maxX=x;
    int maxY=y;
    int maxZ=z;

    float point=0f;
    for( int i=x-dx ; i<=x+dx ; i++ ){
      for( int j=y-dy ; j<=y+dy ; j++ ){
        for( int k=z-dz ; k<=z+dz ; k++ ){
          point=getObs(ds,ids[i][j],k);
          if(point>maxP){
            maxP=(int)Math.round(point);
            maxX=i;
            maxY=j;
            maxZ=k;
          }
          point=0f;
        }
      }
    }
    if(maxX!=x || maxY!=y || maxZ!=z){ // move to nearby maximum
      //peak.pixel(maxX,maxY,maxZ);
      //peak.ipkobs(maxP);
       IPeak Res = peak.createNewPeakxyz(maxX , maxY , maxZ );
       Res.ipkobs( maxP );
       Res.reflag( peak.reflag());
       Res.seqnum( peak.seqnum() );
       return Res;
    }else{
       IPeak Res = peak.createNewPeakxyz(x ,y , z );
       Res.ipkobs( peak.ipkobs());
       Res.reflag( peak.reflag());
       Res.UB(peak.UB());
       if( peak.UB()==null)
          Res.sethkl(  peak.h() , peak.k() , peak.l() );
       Res.inti(peak.inti());
       Res.sigi( peak.sigi());
       Res.seqnum( peak.seqnum() );
       
       return Res;
      //peak.pixel(x,y,z); // move it onto integer pixel postion
    }
  }



  /**
   * Determines whether the peak can be within the realspace limits specified
   */
  public static boolean checkReal(IPeak peak, float[][] lim){
    float wl=peak.wl();
    if(wl==0f) return false;

    float xcm=peak.xcm();
    float ycm=peak.ycm();
    if( xcm>=lim[0][0] && xcm<=lim[0][1] ){
      if( ycm>=lim[1][0] && ycm<=lim[1][1] ){
        if( wl>=lim[2][0] && wl<=lim[2][1] ){
          return true;
        }
      }
    }

    return false;
  }


  /**
   * Determine the edges of the detector in xcm, ycm, and wl. This
   * assumes that ids[0][0] and ids[maxrow][maxcol] are at
   * opposite corners of the detector
   *
   * @param pkfac A fully configure peak factory. It should contain
   * all of the information necessary to go from hkl to pixel.
   * @param ids The 2D matrix which maps row and column to the
   * linear index of datas in DataSet
   * @param times The detector is assumed to have the same x-axis for
   * all pixels. This should be a safe assumption.
   *
   * @return A 3x2 matrix of the limits in real space. 
   */
  public static float[][] minmaxreal( PeakFactory pkfac, 
                                      int[][] ids, 
                                      XScale times ){
    // Determine the limits in pixel and time. This is set up as
    // arrays to shorten later code in the method.
    int[]     x_lim ={1,ids.length-1};
    int[]     y_lim ={1,ids[0].length-1};
    int[]     z_lim ={0,times.getNum_x()-1};
    float[][] time  = {{times.getX(z_lim[0]),times.getX(z_lim[0]+1)},
                       {times.getX(z_lim[1]-1),times.getX(z_lim[1])}};

    // define a temporary peak that will be each of the corners
    IPeak peak=null;

    // The real-space representation of the peaks will be stored in
    // this matrix. The first index is xcm=0, ycm=1, and wl=2. The
    // second index is just the number of the peak, it just needs to
    // be unique
    float[][] real=new float[3][8];

    // This looks scarier than it is. The three indices are x (i), y
    // (j), and z (k). It is set up to reduce the amount of typing
    // that needs to be done if there is an error in the code.
    for( int i=0 ; i<2 ; i++ ){
      for( int j=0 ; j<2 ; j++ ){
        for( int k=0 ; k<2 ; k++ ){
          peak=pkfac.getPixelInstance(x_lim[i],y_lim[j],z_lim[k],
                                      time[k][0],time[k][1]);
          real[0][i+2*j+4*k]=peak.xcm();
          real[1][i+2*j+4*k]=peak.ycm();
          real[2][i+2*j+4*k]=peak.wl();
        }
      }
    }

    // set the peak to null so the garbage collector can reclaim it
    peak=null;


    // the first index is h,k,l and the second index is min,max
    float[][] real_lim={{real[0][0],real[0][0]},
                        {real[1][0],real[1][0]},
                        {real[2][0],real[2][0]}};

    // sort out the min and max values
    for( int i=1 ; i<8 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        real_lim[j][0]=Math.min(real_lim[j][0],real[j][i]);
        real_lim[j][1]=Math.max(real_lim[j][1],real[j][i]);
      }
    }

    return real_lim;
  }

  
  /**
   * Determine the edges of the detector in xcm, ycm, and wl. This
   * assumes that ids[0][0] and ids[maxrow][maxcol] are at
   * opposite corners of the detector
   *
   * @param pkfac A fully configure peak factory. It should contain
   * all of the information necessary to go from hkl to pixel.
   * @param ids The 2D matrix which maps row and column to the
   * linear index of datas in DataSet
   * @param times The detector is assumed to have the same x-axis for
   * all pixels. This should be a safe assumption.
   *
   * @return A 3x2 matrix of the limits in hkl. 
   */
  public static int[][] minmaxhkl(PeakFactory pkfac, int[][] ids, XScale times){
    // Determine the limits in pixel and time. This is set up as
    // arrays to shorten later code in the method.
    int[]     x_lim ={1,ids.length-1};
    int[]     y_lim ={1,ids[0].length-1};
    int[]     z_lim ={0,times.getNum_x()-1};
    float[][] time  = {{times.getX(z_lim[0]),times.getX(z_lim[0]+1)},
                       {times.getX(z_lim[1]-1),times.getX(z_lim[1])}};

    // define a temporary peak that will be each of the corners
    Peak peak=null;

    // The hkls of the peaks will be stored in this matrix. The first
    // index is h=0, k=1, and l=2. The second index is just the number
    // of the peak, it just needs to be unique
    int[][] hkl=new int[3][8];

    // This looks scarier than it is. The three indices are x (i), y
    // (j), and z (k). It is set up to reduce the amount of typing
    // that needs to be done if there is an error in the code.
    for( int i=0 ; i<2 ; i++ ){
      for( int j=0 ; j<2 ; j++ ){
        for( int k=0 ; k<2 ; k++ ){
          peak=pkfac.getPixelInstance(x_lim[i],y_lim[j],z_lim[k],
                                      time[k][0],time[k][1]);
          hkl[0][i+2*j+4*k]=Math.round(peak.h());
          hkl[1][i+2*j+4*k]=Math.round(peak.k());
          hkl[2][i+2*j+4*k]=Math.round(peak.l());
        }
      }
    }

    // set the peak to null so the garbage collector can reclaim it
    peak=null;

    // the first index is h,k,l and the second index is min,max
    int[][] hkl_lim={{hkl[0][0],hkl[0][0]},
                     {hkl[1][0],hkl[1][0]},
                     {hkl[2][0],hkl[2][0]}};

    // sort out the min and max values
    for( int i=1 ; i<8 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        hkl_lim[j][0]=Math.min(hkl_lim[j][0],hkl[j][i]);
        hkl_lim[j][1]=Math.max(hkl_lim[j][1],hkl[j][i]);
      }
    }

    return hkl_lim;
  }
  
  /**
   * Determine the edges of the detector in xcm, ycm, and wl. This
   * assumes that ids[0][0] and ids[maxrow][maxcol] are at
   * opposite corners of the detector
   *
   * @param ds   The DataSet to process. It must have loaded the orientation
   *               matrix
   * @param grid  The grid number in question.
   * 
   * @return 3x2 matrix  of floats of the limits in hkl.
   *
   *@see DataSetTools.operator.DataSet.Attribute.LoadOrientation
   *
   
  public static int[][] minmaxhkl(DataSet ds, int gridID) throws 
                                               IllegalArgumentException{
     if( ds == null)
        throw new IllegalArgumentException("Null dataset in minmaxhkl");
     IDataGrid  grid = Grid_util.getAreaGrid(ds, gridID);
     if( grid == null )
        throw new IllegalArgumentException("No gridID "+gridID+
                                             " in the dataset, minmaxhkl");
     Float2DAttribute att = (Float2DAttribute)ds.getAttribute(
                                                   Attribute.ORIENT_MATRIX);
     if( att == null )
        throw new IllegalArgumentException(
             "Dataset does not have a loaded orientation matrix,in minmaxhkl");
     
     float[][]UB = att.getFloatValue();
     
     FloatAttribute attF = (FloatAttribute)grid.getData_entry(1,1).getAttribute(  Attribute.INITIAL_PATH);
     if( attF == null)
        throw new IllegalArgumentException(
                 "dataset has  negative initial path length in minmaxhkl");
     float initialPathLength = attF.getFloatValue();
     XScale times = grid.getData_entry( 1 , 1 ).getX_scale();
     float Time_shift=AttrUtil.getT0Shift( ds );
     if( Float.isNaN(  Time_shift ))
          Time_shift = 0f;
     return minmaxhkl( grid, times, UB, initialPathLength ,
              (SampleOrientation)ds.getAttributeValue( Attribute.SAMPLE_ORIENTATION),
              Time_shift
              );
     

  }
  */
  /**
   * Determine the edges of the detector in xcm, ycm, and wl. This
   * assumes that ids[0][0] and ids[maxrow][maxcol] are at
   * opposite corners of the detector
   *
   * @param grid   The grid containing position information for the detector
   * @param times  An Xscale giving the range of times included
   * @param  initialPathLength  The distance from the moderator to the sample in meters
   * 
   * @return 3x2 matrix  of floats of the limits in hkl.
   *
   *
   */
  /*public static int[][] minmaxhkl(IDataGrid grid,  XScale times, float[][]UB,
                     float initialPathLength, SampleOrientation sampOr, 
                     float Time_shift ){
    // Determine the limits in pixel and time. This is set up as
    // arrays to shorten later code in the method.
    int[]     x_lim ={1,grid.num_cols()};
    int[]     y_lim ={1,grid.num_rows()};
    int[]     z_lim ={0,times.getNum_x()-1};
    float[][] time  = {{times.getX(z_lim[0]),times.getX(z_lim[0]+1)},
                       {times.getX(z_lim[1]-1),times.getX(z_lim[1])}};


    // The hkls of the peaks will be stored in this matrix. The first
    // index is h=0, k=1, and l=2. The second index is just the number
    // of the peak, it just needs to be unique
    int[][] hkl=new int[3][8];

    
    // This looks scarier than it is. The three indices are x (i), y
    // (j), and z (k). It is set up to reduce the amount of typing
    // that needs to be done if there is an error in the code.
    float[][] invUB = gov.anl.ipns.MathTools.LinearAlgebra.getInverse( UB );
    
    for( int i=0 ; i<2 ; i++ ){
      for( int j=0 ; j<2 ; j++ ){
        for( int k=0 ; k<2 ; k++ ){
//          peak=pkfac.getPixelInstance(x_lim[i],y_lim[j],z_lim[k],
//                                      time[k][0],time[k][1]);
          Vector3D pos = grid.position(y_lim[i],x_lim[j]);
          float Time;
          if( k==0)
             Time = times.getStart_x();
          else
             Time = times.getEnd_x();
          Time += Time_shift;//T0 offset
          float[] Qvec = tof_calc.DiffractometerVecQ( pos, 
                  initialPathLength, Time ).get();
          if( sampOr != null ){
             Vector3D Res = new Vector3D();
             sampOr.getGoniometerRotationInverse().apply_to( new Vector3D( Qvec), Res);
             Qvec = Res.get();
          }
          
          hkl[0][i+2*j+4*k]=(int)Math.round((invUB[0][0]*Qvec[0]+ 
                                       invUB[0][1]*Qvec[1]+invUB[0][2]*Qvec[2])*.5/Math.PI);
          hkl[1][i+2*j+4*k]=(int)Math.round((invUB[1][0]*Qvec[0]+ 
                   invUB[1][1]*Qvec[1]+invUB[1][2]*Qvec[2])*.5/Math.PI);
          hkl[2][i+2*j+4*k]=(int)Math.round((invUB[2][0]*Qvec[0]+ 
                   invUB[2][1]*Qvec[1]+invUB[2][2]*Qvec[2])*.5/Math.PI);
        }
      }
    }
    // the first index is h,k,l and the second index is min,max
    int[][] hkl_lim={{hkl[0][0],hkl[0][0]},
                     {hkl[1][0],hkl[1][0]},
                     {hkl[2][0],hkl[2][0]}};

    // sort out the min and max values
    for( int i=1 ; i<8 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        hkl_lim[j][0]=Math.min(hkl_lim[j][0],hkl[j][i]);
        hkl_lim[j][1]=Math.max(hkl_lim[j][1],hkl[j][i]);
      }
    }

    return hkl_lim;
  }
*/

  /**
   * Checks the allowed indices of hkl given the centering type.
   *
   * @param type the type of centering operation. Acceptable values
   * are primitive (1), a-centered (2), b-centered (3), c-centered
   * (4), [f]ace-centered (5), [i] body-centered (6), or
   * [r]hombohedral-centered (7)
   *
   * @return true if the hkl is allowed false otherwise
   */
  public static boolean checkCenter(int h, int k, int l, int type){
    if(type==0){       // primitive
      return true;
    }else if(type==1){ // a-centered
      int kl=(int)Math.abs(k+l);
      return ( (kl%2)==0 );
    }else if(type==2){ // b-centered
      int hl=(int)Math.abs(h+l);
      return ( (hl%2)==0 );
    }else if(type==3){ // c-centered
      int hk=(int)Math.abs(h+k);
      return ( (hk%2)==0 );
    }else if(type==4){ // [f]ace-centered
      int hk=(int)Math.abs(h+k);
      int hl=(int)Math.abs(h+l);
      int kl=(int)Math.abs(k+l);
      return ( (hk%2)==0 && (hl%2)==0 && (kl%2)==0 );
    }else if(type==5){ // [i] body-centered
      int hkl=(int)Math.abs(h+k+l);
      return ( (hkl%2)==0 );
    }else if(type==6){ // [r]hombohedral-centered
      int hkl=Math.abs(-h+k+l);
      return ( (hkl%3)==0 );
    }

    return false;
  }


  /*
   *  Go through the vector of peaks and remove any peak for which 
   *  d < d_min.
   */
  public static void RemovePeaksWithSmall_d( Vector  peaks,
                                             float   d_min,
                                             DataSet ds,
                                             int     detnum )
  {
    if ( DEBUG )
    {
      System.out.println("Processing DataSet    " + ds );
      System.out.println("Processing detector # " + detnum );
    }
    UniformGrid grid = (UniformGrid)Grid_util.getAreaGrid( ds, detnum );
    if ( grid != null )
    {
      if ( DEBUG )
        System.out.println("Found grid for detector # " + detnum );
      boolean keep_peak[] = new boolean[ peaks.size() ];
      int     num_kept = 0;
      for( int i = 0; i < peaks.size(); i++ )    // first find out which to 
      {                                          // keep
        IPeak my_peak = (IPeak)peaks.elementAt(i);
        if( peak_d_OK( my_peak, d_min, grid ) )
        {
          keep_peak[i] = true;
          num_kept++;
        }
        else
          keep_peak[i] = false;
      }

      if ( d_min > 0 )
        SharedData.addmsg("Keeping " + num_kept + " of " + peaks.size() +
                          " for detector " + detnum + " DataSet " + ds );

      if ( num_kept < peaks.size() )             // copy over the peaks we kept
      {
        IPeak kept_peaks[] = new IPeak[ num_kept ];
        int  index = 0;
        for ( int i = 0; i < keep_peak.length; i++ )
          if ( keep_peak[i] )
          {
            kept_peaks[index] = (IPeak)peaks.elementAt(i);
            index++;
          }
        peaks.clear();
        peaks.ensureCapacity( num_kept );
        for ( int i = 0; i < kept_peaks.length; i++ )
          peaks.addElement( kept_peaks[i] );
      }

    }
    else
      SharedData.addmsg("WARNING: DataGrid not found in Integrate " +
                        "ds = " + ds + "Detector = " + detnum );
   }


  /**
   *  Check whether or not the "d" for this peak is less than a 
   *  specified d_min.  
   */
  public static boolean peak_d_OK( IPeak        peak,
                                   float       d_min,
                                   UniformGrid grid  )
  {
    int row     = Math.round( peak.y() );
    int col     = Math.round( peak.x() );
    int channel = Math.round( peak.z() );

    Data d = grid.getData_entry(row,col);
    if ( d == null )                        // can't check d_min, so keep peak
      return true;

    float initial_path;
    Attribute attr = d.getAttribute(Attribute.INITIAL_PATH);
    if ( attr != null )
      initial_path = (float)attr.getNumericValue();
    else
      return true;

    Vector3D  position_vec = grid.position( row, col );
    float total_path = initial_path + position_vec.length();

    float t0 = 0;
    attr = d.getAttribute(Attribute.T0_SHIFT);
    if ( attr != null )
      t0 = (float)attr.getNumericValue();

    XScale xscale = d.getX_scale();
    float tof = xscale.getX( channel ) + t0;

    DetectorPosition position = new DetectorPosition( position_vec );
    float angle = position.getScatteringAngle();

    float d_spacing = tof_calc.DSpacing( angle, total_path, tof );
    if ( DEBUG )
      System.out.println("seqnum " + peak.seqnum() +
                         "row  = " + row +
                         "col  = " + col +
                         "chan = " + channel +
                         "d    = " + d_spacing );

    if ( d_spacing < d_min )
      return false;
    else
      return true;
  }


  /**
   * Determine the observed intensity of the peak at its (rounded)
   * pixel position.
   */
  public static void getObs(IPeak peak, DataSet ds, int[][] ids)
                                         throws ArrayIndexOutOfBoundsException{
    if( ds==null || peak==null ) return;
    int id=ids[(int)Math.round(peak.x())][(int)Math.round(peak.y())];
    int z=(int)Math.round(peak.z());

    peak.ipkobs((int)getObs(ds,id,z));
  }


  /**
   * Determine the observed intensity of the given id and time slice
   * number.
   */
  public static float getObs(DataSet ds, int id, int z){
    if( ds==null ) return 0f;

    Data d=ds.getData_entry(id);
    if(d==null) return 0f;
    float[] yValues = d.getY_values();
    if( yValues == null || z < 0 || z >=yValues.length)
       return 0f;
    return yValues[z];
  }
  /**
   * Determine the observed intensity of the given id and time slice
   * number.
   */
  public static float getObs(DataSet ds, Data id, int z){
    if( ds==null ) return 0f;

    
    if(id==null) return 0f;
    float[] yValues = id.getY_values();
    if( yValues == null || z < 0 || z >=yValues.length)
       return 0f;
    return yValues[z];

    
  }


  /**
   * @return a 1D array of the form Xmin, Ymin,Xmax, Ymax
   */
  public static int[] getBounds(int[][] ids){
    int[] bounds={-1,-1,-1,-1}; // see javadocs for meaning

    // search for min
    outer: for( int i=0 ; i<ids.length ; i++ ){
      for( int j=0 ; j<ids[0].length ; j++ ){
        if(ids[i][j]==-1) continue; // there is nothing here
        bounds[0]=i;
        bounds[1]=j;
        break outer;
      }
    }

    // search for max
    outer: for( int i=ids.length-1 ; i>bounds[0] ; i-- ){
      for( int j=ids[0].length-1 ; j>bounds[1] ; j-- ){
        if(ids[i][j]==-1) continue; // there is nothing here
        bounds[2]=i;
        bounds[3]=j;
        break outer;
      }
    }

    if( (bounds[0]==-1 && bounds[2]==-1) || (bounds[0]==-1 && bounds[2]==-1) ){
      for( int i=0 ; i<4 ; i++ )
        bounds[i]=-1;
    }else{
      if(bounds[0]==-1)
        bounds[0]=bounds[2];
      else if(bounds[2]==-1)
        bounds[2]=bounds[0];
      if(bounds[1]==-1)
        bounds[1]=bounds[3];
      else if(bounds[3]==-1)
        bounds[3]=bounds[1];
    }

    return bounds;
  }
  public static void showUsage(){
     System.out.println("This is a test program for several methods");
     System.out.println(" Argument 1 is the filename for the data set");
     System.out.println(" Argument 2 is the grid ID");
     System.out.println(" Argument 3  is the file with the UB matrix");
     System.out.println(" Arguemnt4,5,6 are the xychan values");
     System.exit(0);
  }
  public static void main( String args[]){
     
     if( args== null || args.length != 6)
        IntegrateUtils.showUsage();
    DataSet ds = null;
    int gridID = -1;
    IDataGrid grid = null;
    int[][] ids = null;
    
     try{
        DataSet[] DS = ScriptUtil.load( args[0]);
        ds = DS[ DS.length -1];

        System.out.println((new DataSetTools.operator.DataSet.Attribute.LoadOrientation( ds, args[2])).getResult());
        gridID = (new Integer( args[1])).intValue();
        grid = Grid_util.getAreaGrid( ds, gridID );

        float[] xyz = grid.position().get();
        float cosDetA = (float)(xyz[0]/Math.sqrt(xyz[0]*xyz[0]+xyz[1]*xyz[1]));
        float cosDetA2 =(float)( Math.sqrt( (xyz[0]*xyz[0]+ xyz[1]*xyz[1])/(xyz[0]*xyz[0]+ xyz[1]*xyz[1]+xyz[2]*xyz[2])));
        double DD = Math.sqrt(xyz[0]*xyz[0]+ xyz[1]*xyz[1]+xyz[2]*xyz[2]);

        System.out.println("detD, detA,DetA2="+grid.position().length()+","+(Math.atan2( xyz[1],xyz[0])*180/Math.PI)+
                ","+ Math.atan2(xyz[2],DD)*180/Math.PI);

        

         float[] calib = new float[5];
        /* //default with no calibrations
        calib[0]=0f;
        calib[3]= -100*grid.width()/2f;
        calib[4] = -100*grid.height()/2f;
        calib[1]= 100*grid.width()/(float)grid.num_cols();
        calib[2]= 100*grid.height()/(float)grid.num_rows();
        */
         
         // Using calibrations
        System.out.println( (new LoadSCDCalib( ds, "C:/IsawTests/instprm.dat", -1,"")).getResult()); 
        calib =  (float[])grid.getData_entry(1,1).getAttributeValue( Attribute.SCD_CALIB);
        ids = new int[ 1+grid.num_rows()][1+grid.num_cols()];
        for( int i=0; i< ds.getNum_entries(); i++){
           Data D = ds.getData_entry( i );
           PixelInfoList plist =(PixelInfoList) D.getAttribute( Attribute.PIXEL_INFO_LIST).getValue();
           IPixelInfo plist0=plist.pixel(0);
           if( plist0.gridID() == gridID)
              ids[(int)(plist0.row()+.5)][(int)(plist0.col()+.5)] =i;      
           
        }
        
        float initialPathLength = ((Float)grid.getData_entry(1,1).getAttributeValue( Attribute.INITIAL_PATH)).floatValue();
        float[] XYC = new float[3];
        for( int i=0; i<3; i++)
           XYC[i]= (new Float( args[i+3])).floatValue();
        xyz = grid.position().get();
       
      // Vector3D pos2plane = new Vector3D( xyz[0], xyz[1], 0f);
       cosDetA = (float)(xyz[0]/Math.sqrt(xyz[0]*xyz[0]+xyz[1]*xyz[1]));
       DD =Math.sqrt( xyz[0]*xyz[0]+ xyz[1]*xyz[1]);
       cosDetA2 =(float)( Math.sqrt( (xyz[0]*xyz[0]+ xyz[1]*xyz[1])/(xyz[0]*xyz[0]+ xyz[1]*xyz[1]+xyz[2]*xyz[2])));
       PeakFactory pfac = new PeakFactory(((int[])grid.getData_entry(1,1).getAttributeValue( Attribute.RUN_NUM))[0],
                       gridID, initialPathLength,
                       100*grid.position().length(),(float)(Math.atan2( xyz[1],xyz[0])*180/Math.PI),
                       (float)(Math.atan2( xyz[2], DD)*180/Math.PI)
                       );
       System.out.println("detD, detA,DetA2="+grid.position().length()+","+(Math.atan2( xyz[1],xyz[0])*180/Math.PI)+
                ","+ Math.atan2(xyz[2],DD)*180/Math.PI);
       pfac.UB( (float[][])(ds.getAttributeValue( Attribute.ORIENT_MATRIX)));
       SampleOrientation sampOr = (SampleOrientation)ds.getAttributeValue( Attribute.SAMPLE_ORIENTATION);
       if( sampOr != null)
          pfac.sample_orient( sampOr.getChi(), sampOr.getPhi(), sampOr.getOmega());
       FloatAttribute FA =(FloatAttribute)ds.getAttribute( Attribute.T0_SHIFT);
       float T0 = 0f;
       if( FA != null)
          T0 = FA.getFloatValue();
       
       pfac.calib(calib );
       XScale xscl = grid.getData_entry( 1, 1).getX_scale();
       System.out.println("Testing integrate Peaks");
       System.out.println("Old----------------------");
          int[] TimeRange = {-1,0,1,2,3};
           Peak peak = pfac.getPixelInstance( XYC[0], XYC[1], XYC[2], xscl.getX((int)XYC[2]), xscl.getX((int)XYC[2]+1));
          // peak.calib( calib );
          StringBuffer log = new StringBuffer();
          IntegrateUtils.integratePeak( peak, ds, ids, TimeRange, 1,log);
          ScriptUtil.display( peak );
     System.out.println("New ----------------------");
     peak = pfac.getPixelInstance( XYC[0], XYC[1], XYC[2], xscl.getX((int)XYC[2]), xscl.getX((int)XYC[2]+1));
     peak.calib( calib );
     log = new StringBuffer();
     IntegrateUtils.integratePeak( peak, ds, -1, 3, 1, log);
     ScriptUtil.display( peak );
     System.out.println("============================================================");
     System.out.println();
     System.out.println(" Testing integrate peaks with boxes");
     System.out.println("Old------ ");
     int[] rowColRange ={-2,-1,0,1,2};
       peak = pfac.getPixelInstance( XYC[0], XYC[1], XYC[2], xscl.getX((int)XYC[2]), xscl.getX((int)XYC[2]+1));
       peak.calib( calib );
     log = new StringBuffer();
     IntegrateUtils.integrateShoebox( peak, ds, ids, rowColRange, rowColRange,TimeRange,log);
     ScriptUtil.display( peak );
    System.out.println("New ----------------------");
    peak = pfac.getPixelInstance( XYC[0], XYC[1], XYC[2], xscl.getX((int)XYC[2]), xscl.getX((int)XYC[2]+1));
    peak.calib( calib );
    log = new StringBuffer();
     IntegrateUtils.integrateShoebox( peak, ds, -2,2,-2,2,-1,3,log);
     ScriptUtil.display( peak );
     System.out.println("============================================================");
       
     }catch( Exception s){
        s.printStackTrace();
        System.exit(0);
     }
  
   /*
    old-

  r=25.431959143928285
  unrot Qvec=[-2.5119078, 2.2406757, -0.79813975]






  new--

    Qvec*2pi =[-7.907138, -12.395857, -5.149313]
     length =15.578688
     time 1022.4
     Q =15.1782 

     { 0.12310845, -0.20512672, -0.08521087 : 1.0 } = pos(row,col)  len= .2589558
  wl = .32
  pos.length( row,col)=.2539558
  pos(row,col) =[.123108,-.205127,-.08521]=[x,y,z]
  xcm(row,col) = -.075346
  ycm(row,col) = -.075504825
  pos center =.23040162
    */  
  }
     
} 
