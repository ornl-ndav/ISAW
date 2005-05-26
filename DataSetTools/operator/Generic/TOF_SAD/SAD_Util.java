/*
 * File:  SAS_Util.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson, Alok Chatterjee and Dennis Mikkelson
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
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.11  2005/05/26 15:51:17  dennis
 * Fixed one javadoc comment and changed Data.SMOOTH_LINEAR to
 * IData.SOOTH_LINEAR to access the field directly.
 *
 * Revision 1.10  2005/05/13 00:43:04  dennis
 * Added new versions of methods SumQs_1D() and SumQs_2D() that accept
 * a list of boolean flags indicating whether or not a particular channel
 * should be used.  The new version of SumQs_2D() is essentially the same as
 * the previous version, except for the extra parameter.  The new version
 * of SumQs_1D() is quite different from the previous version.  The previous
 * version converted the spectra to Q before summing.  This version processes
 * each bin individually, like the 2D version, to allow omitting individual
 * bins in a logical way.
 *
 * Revision 1.9  2005/05/11 22:54:39  dennis
 * Completed javadocs.
 * Added some additional internal documentation.
 *
 * Revision 1.8  2005/03/30 01:46:54  dennis
 * Removed unneeded semicolon.
 *
 * Revision 1.7  2004/08/02 21:13:42  rmikk
 * Removed unused imports
 *
 * Revision 1.6  2004/07/26 14:58:06  rmikk
 * Fixed the ConvertToWL to fix the data set operators to correspond
 *   to those from wave length
 *
 * Revision 1.5  2004/04/28 18:58:21  dennis
 * Now only print debug information in CalcRatios() method for the
 * first group, and only if debugging is turned on.
 * Turned off debugging.
 *
 * Revision 1.4  2004/04/27 21:50:13  dennis
 *   Added InterpolateDataSet() to interpolate and extend
 * a DataSet over a new xscale.  If the new xscale covers a
 * larger range than the current xscale, the first (or last)
 * y-value will be used for x's beyond the current xscale.
 *   Added some debug prints.
 *
 * Revision 1.3  2004/04/27 15:23:23  dennis
 * Added RemoveAreaDetectorData() method for use by Reduce_LPSD.
 *
 * Revision 1.2  2004/04/26 18:54:16  dennis
 *   Added method Build2D_Difference_DS() that subtracts the sample
 * and background S(Qx,Qy) faster than the general DataSet subtract
 * operator.  This works, assuming each operand has exactly the same
 * detector grid.  (This cut execution time for batch_reduce from
 * 37 seconds to 15 seconds in the 2D case, on a 2.8 Ghz P4).
 *   Removed two unused parameters from Build2D_DS().
 *
 * Revision 1.1  2004/04/26 13:56:54  dennis
 *   Extracted and restructured calculations from Reduce_KCL.
 *   Changed SumQs_1D() and SumQs_2D() methods to step through all
 * groups in a DataSet and coordinate groups with other DataSets
 * based on Group_ID, so these no longer require an area detector.
 *   Extracted calculation of 2D Q region to cover from SumQs_2D()
 * and put it in GetQRegion().
 * Improved efficiency of ConvertXsToWL().
 *   Added method, FixGroupIDs(), to get groupIDs for sensitivity
 * data set from the area detector grid, if using area detector.
 * (The legacy sensitivity file format did not include enough
 * information.)
 *   Added method, BuildIndexOFID_Table(), to allow efficient access
 * to Data blocks based on group ID.  (Needed, since SumQs*() now
 * are based on group ID, not area detector row, col.)
 *
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.ViewTools.Panels.Transforms.*;
//import DataSetTools.util.*;

import java.util.Vector;
import java.util.*;

import DataSetTools.dataset.*;
import DataSetTools.math.*;

import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Math.Analyze.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import DataSetTools.operator.DataSet.Conversion.XYAxis.*;


/**
 *  This class contains static methods used for the small angle diffractometer
 *  Reduce codes.
 */
public class SAD_Util
{ 
   public static boolean debug = false;
  
   /**
    *  Don't instantiate this class
    */
   private SAD_Util() 
   {}


  /* ----------------------------- SumQs_2D -------------------------------- */
  /**
   *  Form a DataSet containing S(Qx,Qy) together with error estimates.
   *
   *  @param  RUNSds       Array of DataSets containing the monitor DataSet
   *                       and a DataSet with the ratios R = (S/Ms-C/Mc)/Ts, 
   *                       as a function of wavelength.
   *  @param  Eff          DataSet containing the detector efficiency ratios as
   *                       a function of wavelength.
   *  @param  Sens         DataSet containing the sensitivity of each pixel
   *  @param  sensIndex    Array containing indices into the sensitivity
   *                       DataSet, to speed up access to the sensitivity value
   *  @param  MonitorInd   Contains the index of the upstream monitor
   *  @param  bounds       Bounds for the region in (Qx,Qy) that is to be
   *                       calculated.
   *  @param  NQxBins      Number of binx in the Qx direction
   *  @param  NQyBins      Number of binx in the Qy direction
   *
   *  @return A DataSet containing the 2D array of calculated S(Qx,Qy) values
   *          together with error estimates.
   */
   public static DataSet SumQs_2D( DataSet     RUNSds[], 
                                   DataSet     Eff,
                                   DataSet     Sens,
                                   int         sensIndex[],
                                   int         MonitorInd[],
                                   CoordBounds bounds,
                                   int         NQxBins,
                                   int         NQyBins  )
   {
     float Qxmin = bounds.getX1(); 
     float Qxmax = bounds.getX2(); 
     float Qymin = bounds.getY1(); 
     float Qymax = bounds.getY2(); 

     float xDELTAQ = ((Qxmax - Qxmin)/NQxBins);
     float yDELTAQ = ((Qymax - Qymin)/NQyBins);

     float WTQXQY[][] = Init2DArray( NQxBins, NQyBins );
     float SQXQY [][] = Init2DArray( NQxBins, NQyBins );
     float SERRXY[][] = Init2DArray( NQxBins, NQyBins );
        
     float[] eff = Eff.getData_entry(0).getY_values();
     float[] Mon = RUNSds[0].getData_entry(MonitorInd[0]).getY_values();
     Data Dsamp;
     DetectorPosition detPos;
     float[] Qxy,
             SampYvals,
             SampErrs,
             wlvals;
     float scatAngle,
           sens,
           lambdaAv,
           Q,
           Qx,
           Qy,
           DNx,
           DNy;
     int Nx,
         Ny;

     FloatAttribute At = (FloatAttribute)
             (RUNSds[1].getData_entry(0).getAttribute(Attribute.INITIAL_PATH));
     float L1 = At.getFloatValue();

     int sensIDoffset = 1 - sensIndex[0];
     int id;

     for( int i = 0; i< RUNSds[1].getNum_entries(); i++)
     {
       Dsamp = RUNSds[1].getData_entry(i);
       id    = Dsamp.getGroup_ID();
                          
       detPos = ((DetectorPosition)
                 Dsamp.getAttributeValue( Attribute.DETECTOR_POS ));
       Vector3D q_vec = 
                new Vector3D( tof_calc.DiffractometerVecQ( detPos, L1, 1000f ));
       q_vec.normalize();
       Qxy = q_vec.get();
       scatAngle = detPos.getScatteringAngle();

       sens = Sens.getData_entry(sensIndex[id + sensIDoffset]).getY_values()[0];
       SampYvals = Dsamp.getY_values();
       SampErrs  = Dsamp.getErrors();
       wlvals    = Dsamp.getX_scale().getXs();
            
       for( int wk = 0; wk+1 < wlvals.length; wk++ )
       {
         lambdaAv = .5f * ( wlvals[ wk ] + wlvals[ wk+1 ] );
         Q  = tof_calc.DiffractometerQofWavelength( scatAngle, lambdaAv ); 
         Qx = -Q * Qxy[1]; 
         Qy =  Q * Qxy[2];
             
         DNx = ((Qx -Qxmin)/xDELTAQ);
         DNy = ((Qy -Qymin)/xDELTAQ);
         Nx  = (int)java.lang.Math.floor( DNx );
         Ny  = (int)java.lang.Math.floor( DNy );
	 if( Nx >=0       && Ny >= 0    && 
             Qx < Qxmax   && Qy < Qymax &&
             Nx < NQxBins && Ny < NQyBins )
         {
            float W =sens*eff[ wk ] * Mon[ wk ];

            WTQXQY[Nx][Ny] = WTQXQY[Nx][Ny] + W; //weightYvals[ wk ];

            SQXQY[Nx][Ny] = SQXQY[Nx][Ny] + SampYvals[ wk ] * Mon[ wk ];

            float U = SampErrs[ wk  ] * W;
            SERRXY[Nx][Ny] = SERRXY[Nx][Ny] + U * U;
          }
          else
          {
            // System.out.println("out of bounds"+Qxmin+","+Q+","+Qxmax+"::"+
            //            Qymin+","+Qy+","+Qymax);
          }
       }
     }//for( int i = 0; i< RUNSds[1].getNum_entries(); i++)
        
	
     for( int i = 0; i < NQxBins; i++ )
       for( int j = 0; j < NQyBins; j++ )
       {
         if(WTQXQY[i][j] == 0)
         {
           SQXQY[i][j]  = 0f;
           SERRXY[i][j] = 0f;
         }
         else
         {
           SQXQY[i][j]  = SQXQY[i][j]/WTQXQY[i][j];
           SERRXY[i][j] = (float)java.lang.Math.sqrt(SERRXY[i][j])/WTQXQY[i][j];
         }
       }

      DataSet ds = Build2D_DS( xDELTAQ, yDELTAQ,
                               NQxBins, NQyBins, SQXQY, SERRXY, "s2d" );
      return ds;
   }


  /* ----------------------------- SumQs_2D -------------------------------- */
  /**
   *  Form a DataSet containing S(Qx,Qy) together with error estimates,
   *  using only channels listed in the "use_chan" array.
   *
   *  @param  RUNSds       Array of DataSets containing the monitor DataSet
   *                       and a DataSet with the ratios R = (S/Ms-C/Mc)/Ts, 
   *                       as a function of wavelength.
   *  @param  Eff          DataSet containing the detector efficiency ratios as
   *                       a function of wavelength.
   *  @param  Sens         DataSet containing the sensitivity of each pixel
   *  @param  sensIndex    Array containing indices into the sensitivity
   *                       DataSet, to speed up access to the sensitivity value
   *  @param  MonitorInd   Contains the index of the upstream monitor
   *  @param  bounds       Bounds for the region in (Qx,Qy) that is to be
   *                       calculated.
   *  @param  NQxBins      Number of binx in the Qx direction
   *  @param  NQyBins      Number of binx in the Qy direction
   *  @param  use_chan     array of boolean flags indicating which channel
   *                       numbers should be used.
   *
   *  @return A DataSet containing the 2D array of calculated S(Qx,Qy) values
   *          together with error estimates.
   */
   public static DataSet SumQs_2D( DataSet     RUNSds[],
                                   DataSet     Eff,
                                   DataSet     Sens,
                                   int         sensIndex[],
                                   int         MonitorInd[],
                                   CoordBounds bounds,
                                   int         NQxBins,
                                   int         NQyBins,
                                   boolean     use_chan[]  )
   {
     float Qxmin = bounds.getX1();
     float Qxmax = bounds.getX2();
     float Qymin = bounds.getY1();
     float Qymax = bounds.getY2();

     float xDELTAQ = ((Qxmax - Qxmin)/NQxBins);
     float yDELTAQ = ((Qymax - Qymin)/NQyBins);

     float WTQXQY[][] = Init2DArray( NQxBins, NQyBins );
     float SQXQY [][] = Init2DArray( NQxBins, NQyBins );
     float SERRXY[][] = Init2DArray( NQxBins, NQyBins );

     float[] eff = Eff.getData_entry(0).getY_values();
     float[] Mon = RUNSds[0].getData_entry(MonitorInd[0]).getY_values();
     Data Dsamp;
     DetectorPosition detPos;
     float[] Qxy,
             SampYvals,
             SampErrs,
             wlvals;
     float scatAngle,
           sens,
           lambdaAv,
           Q,
           Qx,
           Qy,
           DNx,
           DNy;
     int Nx,
         Ny;

     FloatAttribute At = (FloatAttribute)
             (RUNSds[1].getData_entry(0).getAttribute(Attribute.INITIAL_PATH));
     float L1 = At.getFloatValue();

     int sensIDoffset = 1 - sensIndex[0];
     int id;

     for( int i = 0; i< RUNSds[1].getNum_entries(); i++)
     {
       Dsamp = RUNSds[1].getData_entry(i);
       id    = Dsamp.getGroup_ID();

       detPos = ((DetectorPosition)
                 Dsamp.getAttributeValue( Attribute.DETECTOR_POS ));
       Vector3D q_vec =
                new Vector3D( tof_calc.DiffractometerVecQ( detPos, L1, 1000f ));
       q_vec.normalize();
       Qxy = q_vec.get();
       scatAngle = detPos.getScatteringAngle();

       sens = Sens.getData_entry(sensIndex[id + sensIDoffset]).getY_values()[0];
       SampYvals = Dsamp.getY_values();
       SampErrs  = Dsamp.getErrors();
       wlvals    = Dsamp.getX_scale().getXs();

       for( int wk = 0; wk+1 < wlvals.length; wk++ )
       {
         if ( use_chan[ wk ] )
         {
           lambdaAv = .5f * ( wlvals[ wk ] + wlvals[ wk+1 ] );
           Q  = tof_calc.DiffractometerQofWavelength( scatAngle, lambdaAv );
           Qx = -Q * Qxy[1];
           Qy =  Q * Qxy[2];

           DNx = ((Qx -Qxmin)/xDELTAQ);
           DNy = ((Qy -Qymin)/xDELTAQ);
           Nx  = (int)java.lang.Math.floor( DNx );
           Ny  = (int)java.lang.Math.floor( DNy );
           if( Nx >=0       && Ny >= 0    &&
               Qx < Qxmax   && Qy < Qymax &&
               Nx < NQxBins && Ny < NQyBins )
           {
              float W =sens*eff[ wk ] * Mon[ wk ];

              WTQXQY[Nx][Ny] = WTQXQY[Nx][Ny] + W; //weightYvals[ wk ];
  
              SQXQY[Nx][Ny] = SQXQY[Nx][Ny] + SampYvals[ wk ] * Mon[ wk ];

              float U = SampErrs[ wk  ] * W;
              SERRXY[Nx][Ny] = SERRXY[Nx][Ny] + U * U;
            }
            else
            {
              // System.out.println("out of bounds"+Qxmin+","+Q+","+Qxmax+"::"+
              //            Qymin+","+Qy+","+Qymax);
            }
          }
       }
     }//for( int i = 0; i< RUNSds[1].getNum_entries(); i++)


     for( int i = 0; i < NQxBins; i++ )
       for( int j = 0; j < NQyBins; j++ )
       {
         if(WTQXQY[i][j] == 0)
         {
           SQXQY[i][j]  = 0f;
           SERRXY[i][j] = 0f;
         }
         else
         {
           SQXQY[i][j]  = SQXQY[i][j]/WTQXQY[i][j];
           SERRXY[i][j] = (float)java.lang.Math.sqrt(SERRXY[i][j])/WTQXQY[i][j];
         }
       }
      DataSet ds = Build2D_DS( xDELTAQ, yDELTAQ,
                               NQxBins, NQyBins, SQXQY, SERRXY, "s2d" );
      return ds;
   }


   /* --------------------------- GetQRegion ------------------------ */
   /**
    *  Determine the 2D region to be covered when calculating S(Qx,Qy)
    *  as the intersection of the region covered by the detector and the
    *  region specified by the values in qu[].
    *
    *  @param  SampGrid   DataGrid for the area detector 
    *  @param  lambda     array of wavelengths to use
    *  @param  qu         Array containing Qxmin, Qxmax, Qymin, Qymax givng
    *                     the range of Qx,Qy to be covered
    *
    *  @return a CoordBounds object containing the region of (Qx,Qy) covered.
    */
    public static CoordBounds GetQRegion( UniformGrid SampGrid, 
                                          float       lambda[], 
                                          float       qu[] )
    { 
      float Qxmin,
            Qxmax,
            Qymin,
            Qymax;
                                                  //Change to subrange of times
      double LLOW = .5 * ( lambda[0] + lambda[1] );

      float[] mins = SampGrid.position(1f,1f).get();
      float[] maxs = SampGrid.position( SampGrid.num_rows(), 
                                        SampGrid.num_cols() ).get();

      float L2 = SampGrid.position().length();
      double sinxMax = java.lang.Math.sin(.5*java.lang.Math.atan(-maxs[1]/L2));
      double sinyMax = java.lang.Math.sin(.5*java.lang.Math.atan( maxs[2]/L2));
      double sinxMin = java.lang.Math.sin(.5*java.lang.Math.atan(-mins[1]/L2));
      double sinyMin = java.lang.Math.sin(.5*java.lang.Math.atan( mins[2]/L2));

      Qxmin = (float)(4*java.lang.Math.PI*sinxMin/LLOW);
      Qymin = (float)(4*java.lang.Math.PI*sinyMin/LLOW);
      Qxmax = (float)(4*java.lang.Math.PI*sinxMax/LLOW);
      Qymax = (float)(4*java.lang.Math.PI*sinyMax/LLOW);

      if( qu != null )
        if( qu.length >= 4 )
        {
           Qxmin = (float)java.lang.Math.max( Qxmin, qu[0] );
           Qxmax = (float)java.lang.Math.min( Qxmax, qu[1] );
           Qymin = (float)java.lang.Math.max( Qymin, qu[2] );
           Qymax = (float)java.lang.Math.min( Qymax, qu[3] );
        }

      CoordBounds bounds = new CoordBounds( Qxmin, Qymin, Qxmax, Qymax );
      return bounds;
    }


   /* ------------------------- BuildRunNumList ----------------------- */
   /**
    *  Make an array containing the list of run numbers for the runs used
    *
    *  @param  ds   Array of DataSets for the runs being used
    *
    *  @return an integer array containing the list of run numbers
    *          corresponding to the specified DataSets.
    */
   public static int[] BuildRunNumList( DataSet ds[] )
   { 
     Vector v = new Vector();
     int    run_num;

     for ( int i = 0; i < ds.length; i++ )
       if ( ds[i] != null )
       {
         run_num = (((IntListAttribute) 
                (ds[i].getAttribute(Attribute.RUN_NUM))).getIntegerValue())[0]; 
         v.add( new Integer(run_num) );
       }

     int[] RunNums = new int[ v.size() ];
     for ( int i = 0; i < v.size(); i++ )
       RunNums[i] = ((Integer)v.elementAt(i)).intValue();

     return RunNums;
   }


   /* --------------------------- AdjustGrid ---------------------------- */ 
   /**
    *  Shift the data grid for an area detector to compensate for the 
    *  beam center offset
    *
    *  @param  ds    The DataSet whose data grid is to be moved.
    *  @param  xoff  the x offset of the beam center on the detector face
    *  @param  yoff  the y offset of the beam center on the detector face
    */
   public static void AdjustGrid(DataSet ds, float xoff, float yoff) 
   { 
      int ids[] = Grid_util.getAreaGridIDs(ds);

      if (ids.length != 1)
        System.out.println("ERROR: wrong number of data grids " + ids.length);
      IDataGrid grid = Grid_util.getAreaGrid(ds, ids[0]);

      UniformGrid.setDataEntriesInAllGrids(ds);
      Vector3D pos = grid.position();

      // NOTE: xoff and yoff are specified in a local coordinate system on 
      //       the face of the detector.  In lab coordinates this means that
      //       the detector should be moved by specified xoff in the LAB Y
      //       direction and by minus the specified yoffset in the LAB Z 
      //       direction.
      pos.add(new Vector3D(0, xoff, -yoff));
      ((UniformGrid) grid).setCenter(pos);
    
      Grid_util.setEffectivePositions(ds, grid.ID());
   }


  /* --------------------------- Build2D_DS ----------------------------- */ 
  /**
   *  Build a DataSet, complete with attributes, pixel info lists, etc.
   *  from the specified 2D arrays of values and errors.
   *  
   *  @param  Dx        The width of the 2D area covered by the data
   *  @param  Dy        The height of the 2D area covered by the data
   *  @param  Nx        The number of columns
   *  @param  Ny        The number of rows 
   *  @param  list      Array containing the values to store in the DataBlock
   *                    at position (row,col)
   *  @param  err       Array containing error estimates for the values in list
   *  @param  DataSetName  Name to use for this DataSet
   *  
   *  @return  A new DataSet with a DataGrid and Nx*Ny DataBlocks each
   *           containing on of the values from list[][].
   */
   public static DataSet Build2D_DS( float     Dx,         
                                     float     Dy, 
                                     int       Nx,         
                                     int       Ny,
                                     float[][] list,       
                                     float[][] err, 
                                     String    DataSetName )
   {
    
     DataSet DS = new DataSet( DataSetName,
                               new OperationLog(), 
                               "per Angstrom",
                               "", 
                               "Rel Counts", 
                               "Rel Counts" );
     UniformGrid grid = new UniformGrid( 47,
                                        "per Angstrom",
                                         new Vector3D(0f,0f,0f),
                                         new Vector3D(0f,Dy,0f), 
                                         new Vector3D(0f,0f,Dx), 
                                         Dx*Nx,
                                         Dy*Ny, 
                                         0.0001f,
                                         Ny,
                                         Nx );

     UniformXScale xscl = new UniformXScale( 0, 1, 2 );

     float[] yvals, errs;
     for(int row = 1; row <= Ny; row++)
       for( int col = 1; col <= Nx; col++ )
       {
         yvals = new float[1];
         errs  = new float[1];
         yvals[0] = list[row-1][col-1];
         errs[0]  = err [row-1][col-1];
         if( col == Nx )                     // TODO why is the last col null?
           list[row-1] = null;
         HistogramTable Dat = 
                        new HistogramTable( xscl, yvals, errs, (row-1)*Nx+col );

         DetectorPixelInfo dpi = 
             new DetectorPixelInfo((row-1)*Nx+col,(short)row, (short)col,grid);

         Dat.setAttribute( new PixelInfoListAttribute(Attribute.PIXEL_INFO_LIST,
                                                     new PixelInfoList( dpi )));

         Dat.setAttribute( new FloatAttribute(Attribute.INITIAL_PATH, 3));
         Dat.setAttribute( new FloatAttribute(Attribute.TOTAL_COUNT, yvals[0]));
         DS.addData_entry(Dat);
      }

    DS.setAttribute( new StringAttribute(Attribute.INST_NAME,"SAND"));
    UniformGrid.setDataEntriesInAllGrids( DS );
   
    Grid_util.setEffectivePositions( DS, 47 );
    return DS;
  }


  /* ---------------------- Build2D_Difference_DS ------------------------- */
  /**
   *  Build a DataSet, that is the difference of the specified DataSets
   *  containing S(Qx,Qy).  This is a specialized version that does NOT
   *  deal with most attributes.  It allows forming the difference of the
   *  sample minus background DataSets faster.  The DataSets are assumed
   *  to have identical area detector grids.
   *
   *  @param ds_1   The DataSet from which ds_2 is subtracted.
   *  @param ds_2   The DataSet which is subtracted from ds_1.
   *
   *  @return  A new DataSet containing the values (and errors) for ds_1-ds_2
   */
   public static DataSet Build2D_Difference_DS( DataSet ds_1, DataSet ds_2 )
   {
     int[] Ids= Grid_util.getAreaGridIDs( ds_1 );
     UniformGrid grid_1 = (UniformGrid)Grid_util.getAreaGrid( ds_1, Ids[0] );
     UniformGrid grid_2 = (UniformGrid)Grid_util.getAreaGrid( ds_2, Ids[0] );

     int n_rows = grid_1.num_rows();
     int n_cols = grid_1.num_cols();
     float list[][] = new float[n_rows][n_cols];
     float errs[][] = new float[n_rows][n_cols];
     Data d1,
          d2;
     for ( int row = 1; row <= n_rows; row++ )
       for ( int col = 1; col <= n_cols; col++ )
       {
         d1 = grid_1.getData_entry( row, col );
         d2 = grid_2.getData_entry( row, col );
         list[row-1][col-1] = d1.getY_values()[0] - d2.getY_values()[0];
         errs[row-1][col-1] = SumDiffErr(d1.getErrors()[0], d2.getErrors()[0]);
       }
 
     float Dx = grid_1.width() / n_cols;
     float Dy = grid_1.height() / n_rows;

     return Build2D_DS( Dx, Dy, n_cols, n_rows, list, errs, "Difference DS" );
   }


  /* ----------------------------- SumQs_1D ------------------------------ */
  /**
   *  This does most of the calculations for the 1D S(Q) case.  It builds
   *  the 1D S(Q) DataSet by mapping each spectrum to Q and summing. 
   *
   *  @param  RUNSds       Array of DataSets containing the monitor DataSet
   *                       and a DataSet with the ratios R = (S/Ms-C/Mc)/Ts, 
   *                       as a function of wavelength.
   *  @param  EffDS        DataSet containing the detector efficiency ratios as
   *                       a function of wavelength.
   *  @param  SensDs       DataSet containing the sensitivity of each pixel
   *  @param  sensIndex    Array containing indices into the sensitivity
   *                       DataSet, to speed up access to the sensitivity value
   *  @param  MonitorInd   Contains the index of the upstream monitor
   *  @param  xscl         XScale giving the list of bins to use when
   *                       calculating S(Q).
   *
   *  @return A DataSet containing the 1D array of calculated S(Q) values
   *          together with error estimates.
   */
  public static DataSet SumQs_1D( DataSet     RUNSds[], 
                                  DataSet     EffDS,
                                  DataSet     SensDs, 
                                  int         sensIndex[],
                                  int         MonitorInd[],
                                  XScale      xscl      )
  {
    DataSet SampMonDs = RUNSds[0];
    DataSet SampDs    = RUNSds[1];

    float[] Resy   = new float[ xscl.getNum_x()-1 ];
    float[] ErrSq  = new float[ Resy.length ];
    float[] weight = new float[ Resy.length ];
    float  sens;
    float[] yvals, 
            errs, 
            xvals, 
            eff,
            eff1,
            eff2;
    Data D;
    Arrays.fill( Resy, 0.0f );
    Arrays.fill( ErrSq, 0.0f );
    Arrays.fill( weight, 0.0f );
    float[] monit = SampMonDs.getData_entry( MonitorInd[0] ).getY_values();
      
    eff  = EffDS.getData_entry(0).getY_values();
    eff2 = new float[ eff.length ];
    for( int i = 0; i< eff.length; i++)
      eff2[i]= eff[i]*monit[i];
      
    int sensIDoffset = 1 - sensIndex[0];
    int id;
    for ( int index = 0; index < SampDs.getNum_entries(); index++ )
    {
      D = SampDs.getData_entry( index );
      id = D.getGroup_ID();
      sens = SensDs.getData_entry(sensIndex[id+sensIDoffset]).getY_values()[0];
      if( sens != 0.0f )
      {
        yvals = D.getCopyOfY_values();
        for( int i = 0; i< yvals.length; i++)
          yvals[i] = yvals[i] * monit[i];

        errs = D.getCopyOfErrors();
        for( int i=0; i< errs.length; i++)
           errs[i] = errs[i] * monit[i] * eff[i] * sens;

        xvals = getQ_Values( D );
        yvals = Rebin( yvals, xvals, xscl );
        errs  = Rebin( errs, xvals, xscl );
        eff1  = Rebin( eff2, xvals, xscl );
            
        SqErrors( errs);
        for( int chan = 0; chan < yvals.length; chan++ )
          if( eff1[chan] !=0 )
          {
            Resy[chan]   += yvals[chan];
            ErrSq[chan]  += errs[chan];
            weight[chan] += sens*eff1[chan];
          }
      }//sens !=0
    }//for each Data block 

    for( int i = 0; i< Resy.length;i++ )
      if( weight[i] > 0 )
      {
        Resy[i]  = Resy[i]/weight[i];
        ErrSq[i] = (float)java.lang.Math.sqrt( ErrSq[i] )/weight[i];
      }
      else
        Resy[i] = ErrSq[i] = 0.0f;

    DataSet Result = new DataSet( "s",
                                   new OperationLog(),
                                  "per Angstrom",
                                  "Q","Rel Intensity", 
                                  "Intensity");  
    D = new HistogramTable( xscl, (Resy), (ErrSq), 0 );
    Result.addData_entry( D ); 

    return Result; 
  }//SumQs_1D 


  /* ----------------------------- SumQs_1D -------------------------------- */
  /**
   *  Form a DataSet containing S(Qx,Qy) together with error estimates,
   *  using only channels listed in the "use_chan" array, in a manner
   *  analogous to what was done for the 2D sum.
   *
   *  @param  RUNSds       Array of DataSets containing the monitor DataSet
   *                       and a DataSet with the ratios R = (S/Ms-C/Mc)/Ts, 
   *                       as a function of wavelength.
   *  @param  Eff          DataSet containing the detector efficiency ratios as
   *                       a function of wavelength.
   *  @param  Sens         DataSet containing the sensitivity of each pixel
   *  @param  sensIndex    Array containing indices into the sensitivity
   *                       DataSet, to speed up access to the sensitivity value
   *  @param  MonitorInd   Contains the index of the upstream monitor
   *  @param  xscl         XScale giving the list of bins to use when
   *                       calculating S(Q).
   *  @param  use_chan     array of boolean flags indicating which channel
   *                       numbers should be used.
   *
   *  @return A DataSet containing the 2D array of calculated S(Qx,Qy) values
   *          together with error estimates.
   */
   public static DataSet SumQs_1D( DataSet     RUNSds[],
                                   DataSet     Eff,
                                   DataSet     Sens,
                                   int         sensIndex[],
                                   int         MonitorInd[],
                                   XScale      xscl,
                                   boolean     use_chan[]  )
   {
     float[] Resy   = new float[ xscl.getNum_x()-1 ];
     float[] ErrSq  = new float[ Resy.length ];
     float[] Weight = new float[ Resy.length ];
     Arrays.fill( Resy, 0.0f );
     Arrays.fill( ErrSq, 0.0f );
     Arrays.fill( Weight, 0.0f );

     float[] eff = Eff.getData_entry(0).getY_values();
     float[] Mon = RUNSds[0].getData_entry(MonitorInd[0]).getY_values();

     Data Dsamp;
     DetectorPosition detPos;
     float[] SampYvals,
             SampErrs,
             wlvals;
     float scatAngle,
           sens,
           lambdaAv,
           Q;

     int sensIDoffset = 1 - sensIndex[0];
     int id;

     for( int i = 0; i< RUNSds[1].getNum_entries(); i++)
     {
       Dsamp = RUNSds[1].getData_entry(i);
       id    = Dsamp.getGroup_ID();

       detPos = ((DetectorPosition)
                 Dsamp.getAttributeValue( Attribute.DETECTOR_POS ));
       scatAngle = detPos.getScatteringAngle();

       sens = Sens.getData_entry(sensIndex[id + sensIDoffset]).getY_values()[0];
       SampYvals = Dsamp.getY_values();
       SampErrs  = Dsamp.getErrors();
       wlvals    = Dsamp.getX_scale().getXs();

       for( int wk = 0; wk+1 < wlvals.length; wk++ )
       {
         if ( use_chan[ wk ] )
         {
           lambdaAv = .5f * ( wlvals[ wk ] + wlvals[ wk+1 ] );
           Q  = tof_calc.DiffractometerQofWavelength( scatAngle, lambdaAv );
          
           int q_index = xscl.getI_GLB( Q );
           if ( q_index >= 0 && q_index < Resy.length )
           {
              float W = sens*eff[ wk ] * Mon[ wk ];
              float U = SampErrs[ wk ] * W;

              Resy[ q_index ]   += SampYvals[ wk ] * Mon[ wk ]; 
              ErrSq[ q_index ]  += U * U; 
              Weight[ q_index ] += W;
           } 
         }
       }
     }//for( int i = 0; i< RUNSds[1].getNum_entries(); i++)

     for( int i = 0; i< Resy.length;i++ )
       if( Weight[i] > 0 )
       {
         Resy[i]  = Resy[i]/Weight[i];
         ErrSq[i] = (float)java.lang.Math.sqrt( ErrSq[i] )/Weight[i];
       }
       else
       {
         Resy[i]  = 0.0f;
         ErrSq[i] = 0.0f;
       }

     DataSet Result = new DataSet( "s",
                                    new OperationLog(),
                                   "per Angstrom",
                                   "Q","Rel Intensity",
                                   "Intensity");
     Data D = new HistogramTable( xscl, (Resy), (ErrSq), 0 );
     Result.addData_entry( D );

     return Result;
   }


  /* ---------------------------- CalcRatios ----------------------------- */
  /**
   * Calculate the ratio R = (S/Ms-C/Mc)/Ts  with errs for R/sens/eff.
   * The calculated ratios are returned in the y-value arrays of the
   * RUNSds[] DataSets.
   *
   *  @param  RUNSds       Array of DataSets containing the monitor DataSet
   *                       and Sample DataSet.
   *  @param  RUNCds       Array of DataSets containing the cadmium run
   *                       monitor DataSet and cadmium run sample DataSet.
   *  @param  cadIndex     Array containing indices into the cadmium DataSet 
   *                       DataSet, to speed up access.
   *  @param  Transm           The DataSet giving the transmission as a 
   *                           function of wavelength.
   *  @param  useTransmission  Boolean flag indicating whether or not to use
   *                           divide by the transmission.
   *  @param  Eff              DataSet containing the detector efficiency 
   *                           ratios as a function of wavelength.
   *  @param  SensDs       DataSet containing the sensitivity of each pixel
   *  @param  sensIndex    Array containing indices into the sensitivity
   *                       DataSet, to speed up access to the sensitivity value
   *  @param  MonitorInd   Contains the index of the upstream monitor
   *  @param  scale        The scale factor that is multiplied times the 
   *                       calculated ratios and errors.
   *
   */
  public static void CalcRatios( DataSet    RUNSds[], 
                                 DataSet    RUNCds[], 
                                 int        cadIndex[],
                                 DataSet    Transm, 
                                 boolean    useTransmission, 
                                 DataSet    Eff, 
                                 DataSet    SensDs,
                                 int        sensIndex[],
                                 int        MonitorInd[],
                                 float      scale )
  {
     DataSet SampMon = RUNSds[0];
     DataSet SampDs  = RUNSds[1];
     DataSet CadMon  = RUNCds[0];
     DataSet CadDs   = RUNCds[1];

     float[] sampy, 
             samperr, 
             Cadmy, 
             Cadmerr,
             Transmy;

     if( useTransmission )
       Transmy = Transm.getData_entry(0).getY_values();
     else
       Transmy = null;

     float[] SampMony = SampMon.getData_entry(MonitorInd[0]).getY_values();
     float[] CadmMony = CadMon.getData_entry(MonitorInd[0]).getY_values();
     float[] Effy = Eff.getData_entry(0).getY_values();

     float[] Transmerr = null;
     if( useTransmission )
       Transmerr = Transm.getData_entry(0).getErrors();

     float[] SampMonerr = SampMon.getData_entry(MonitorInd[0]).getErrors();
     float[] CadmMonerr = CadMon.getData_entry(MonitorInd[0]).getErrors();
     float[] Efferr     = Eff.getData_entry(0).getErrors();

     Data D;
     float err1,
           err2,
           err3, 
           Num, 
           sens,
           senserr;

     int sensOffset = 1 - sensIndex[0];
     int cadOffset  = 1 - cadIndex[0];
     int id;

     for ( int index = 0; index < SampDs.getNum_entries(); index++ )
       {
         D       = SampDs.getData_entry( index );
         sampy   = D.getY_values();
         samperr = D.getErrors();
         id      = D.getGroup_ID();

         D       = SensDs.getData_entry( sensIndex[ id + sensOffset ] );
         sens    = D.getY_values()[0];
         senserr = D.getErrors()[0];

         if( sens == 0)
         {
            Arrays.fill( sampy, 0.0f );
            Arrays.fill( samperr, 0.0f );
         }
         else
         {
            D = CadDs.getData_entry( cadIndex[ id + cadOffset ] );
            Cadmy   = D.getY_values();
            Cadmerr = D.getErrors();
            D = null;

            if ( debug && index == 0 )
            {
            System.out.println("sampy.length      = " + sampy.length );
            System.out.println("samperr.length    = " + samperr.length );
            System.out.println("SampMony.length   = " + SampMony.length );
            System.out.println("SampMonerr.length = " + SampMonerr.length );
            System.out.println("Cadmy.length      = " + Cadmy.length );
            System.out.println("Cadmerr.length    = " + Cadmerr.length );
            System.out.println("CadmMony.length   = " + CadmMony.length );
            System.out.println("CadmMonerr.length = " + CadmMonerr.length );
            System.out.println("Transmy.length    = " + Transmy.length );
            System.out.println("Transmerr.length  = " + Transmerr.length );
            }

            for( int i = 0; i < sampy.length; i++ )
            {
               err1 = quoErr(sampy[i],samperr[i],SampMony[i],SampMonerr[i]);
               err2 = quoErr(Cadmy[i],Cadmerr[i],CadmMony[i],CadmMonerr[i]);
               err3 = SumDiffErr( err1,  err2);
           
               Num = sampy[i]/SampMony[i] -Cadmy[i]/CadmMony[i]; 
               sampy[i] = Num;
 
               if( useTransmission)
               {
                 samperr[i] = quoErr( sampy[i], err3, Transmy[i], Transmerr[i]);
                 sampy[i]   = sampy[i]/Transmy[i];
               }
               else
                 samperr[i] = err3;
            
               samperr[i] = quoErr( sampy[i], 
                                    samperr[i], 
                                    sens * Effy[i],
                                    prodErr(sens, senserr, Effy[i], Efferr[i]));
               sampy[i]   = scale * sampy[i];
               samperr[i] = scale * samperr[i];
            }
       }//else sens ==0
     }
   }


  /* ---------------------------- prodErr -------------------------------- */
  /**
   *  Calculate the error in a produce, based on the error in the two factors 
   *
   *  @param Fac1     The first factor
   *  @param Fac1Err  Error estimate for the first factor
   *  @param Fac2     The second factor
   *  @param Fac2Err  Error estimate for the second factor
   *
   *  @return the estimate of the error in Fac1*Fac2.
   */
  public static float prodErr( float Fac1, 
                               float Fac1Err, 
                               float Fac2, 
                               float Fac2Err)
  {
    return (float)Math.sqrt( Fac1 * Fac2Err * Fac1 * Fac2Err +
                             Fac2 * Fac1Err * Fac2 * Fac1Err );
  }


  /* ---------------------------- quoErr -------------------------------- */
  /**
   *  Calculate the error in a quotient, based on the error in the numerator
   *  and denomoinator.
   *  @param Num      The numerator
   *  @param NumErr   Error estimate for the numerator
   *  @param Den      The denominator 
   *  @param DenErr   Error estimate for the denominator
   *
   *  @return the estimate of the error in Num/Den.
   */
  public static float quoErr( float Num, float NumErr, float Den, float DenErr)
  {
    if( Den ==0)
    {
      DenErr = 0;
      Den    = 1;
    }
   
    float V = Num/Den/Den;
    return (float)Math.sqrt( NumErr * NumErr/Den/Den + V * V * DenErr * DenErr);
  }

 
  /* --------------------------- SumDiffErr ----------------------------- */
  /**
   *  Calculate the error in a sum or difference, based on the 
   *  error in the two terms
   *  @param Term1Err   Error estimate for the Term1 
   *  @param Term2Err   Error estimate for the Term2 
   *
   *  @return the square root of the sum of the squares of the errors in
   *          term1 and term2.
   */
  public static float SumDiffErr( float Term1Err, float Term2Err )
  {
    return (float)Math.sqrt( Term1Err * Term1Err + Term2Err * Term2Err );
  }


  /* ---------------------------- SqErrors ------------------------------ */
  /**
   *  Replace list of errors with list of errors squared
   *
   *  @param  errs   List of error estimates which are squared in this method.
   *
   */
  public static void SqErrors( float[] errs )
  {
    if( errs == null) return;
    for( int i=0; i< errs.length; i++)
      errs[i] = errs[i] * errs[i];
   }


  /* --------------------------- getQ_Values ---------------------------- */
  /**
   *  Get the list of Q values corresponding to the list of wavelengths 
   *  for a Data block whose x values are in wavelength.
   *
   *  @param  D     A Data block giving a spectrum as a function of wavelength
   *
   *  @return  An array of 'Q' values corresponding the wavelengths in D
   *           
   */
  public static float[] getQ_Values( Data D )
  {
    float[] Res = D.getX_scale().getXs();

    float scatAngle = ((DetectorPosition) D.getAttributeValue( 
                                Attribute.DETECTOR_POS)).getScatteringAngle();

    for( int i = 0; i< Res.length; i++)
      Res[i] = tof_calc.DiffractometerQofWavelength( scatAngle, Res[i] );
    
    return Res;
  }

  
  /* ------------------------------ Rebin ------------------------------- */
  /**
  *   Return a new set of yvalues that correspond to the old set of yvalues
  *   rebinned to the new XScale
  *
  *   @param  yvals     the old set of y values
  *   @param  xvals     the x values corresponding to the old y values
  *                     ( assumes histogram )
  *   @param  qu_scale  the new XScale to be rebinned to
  *
  *   @return  An array of y-values obtained by rebinning the specified
  *            y values to the new qu_scale.
  */ 
  public static float[] Rebin( float[] yvals, float[] xvals, XScale qu_scale )
  {
    float[] xx  = qu_scale.getXs();
    float[] Res = new float[ xx.length-1];
    Arrays.fill( Res, 0.0f);
    int i, j;
    i = xvals.length - 1;

    //xvals are in reverse order
    for( j=0; j + 1< xx.length; j++)
    {
       while( (i-1 >= 0) && ( xvals[i-1] < xx[j]) )
         i--;
       if( i < 1)
          return Res;
       if( xvals[i] < xx[j+1] )
       Res[j] += yvals[i-1] * (java.lang.Math.min( xvals[i-1], xx[j+1] )-
                 java.lang.Math.max( xx[j], xvals[i] )) / (xvals[i-1]-xvals[i]);
       i--;
       if( i > 0 )
       while( (i > 0 ) && ( xvals[i] < xx[j+1] ) )
       {
         Res[j]+= yvals[i-1]*( java.lang.Math.min( xvals[i-1],xx[j+1])-
                 java.lang.Math.max( xx[j],xvals[i]))/(xvals[i-1]-xvals[i]);
         i--;
       }     
       if( i < 0) return Res;
       if( xvals[i] >= xx[j+1] ) 
         i++;
       if( i >= xvals.length) 
         i = xvals.length - 1;        
    }
  
   return Res;
  }
 

  /* ---------------------- RemoveAreaDetectorData ------------------------ */
  /**
   *  If the given DataSet has groups from an area detector, construct a
   *  new DataSet that has those groups removed.  
   *  NOTE: All groups between the min and max ID for the area detector 
   *        will be removed.
   *
   *  @param  ds  The DataSet from which the area detector data (if any) 
   *              will be removed.
   *
   *  @return The original DataSet, if there was not an a area detector, or
   *          a new DataSet with the area detector data removed.
   */
  public static DataSet RemoveAreaDetectorData( DataSet ds )
  {
    int[] ids = Grid_util.getAreaGridIDs( ds );

    if ( ids == null || ids.length == 0 )
      return ds;

    if ( ids.length > 1 )
      System.out.println("WARNING...RemoveAreaDetectorData only removes ONE");

    UniformGrid grid = (UniformGrid)Grid_util.getAreaGrid( ds, ids[0] );
   
    int minID = Integer.MAX_VALUE;                    // find min and max ID
    int maxID = Integer.MIN_VALUE;
    int  id;
    Data d;
    for ( int row = 1; row < grid.num_rows(); row++ )
      for ( int col = 1; col < grid.num_cols(); col++ )
      {
         d  = grid.getData_entry( row, col );
         id = d.getGroup_ID();
         if ( id < minID )
           minID = id;
         else if ( id > maxID )
           maxID = id;
      }
                                                     // copy groups with ids 
                                                     // outside [minID, maxID]
    DataSet new_ds = ds.empty_clone();
    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      d  = ds.getData_entry(i);
      id = d.getGroup_ID();
      if ( id < minID || id > maxID )
        new_ds.addData_entry( (Data)(d.clone()) );
    }

    return new_ds;
  }


  /* --------------------------- SetUpGrid ------------------------------ */
  /**
   *  Get the data grid for an area detector and reset the Data entries
   *  to the correct Data blocks
   *  
   *  @param  DS   The DataSet from an area detector for which the Data block
   *               references are to be reset.
   *
   *  @return The DataGrid for the area detector, after re-setting the 
   *          Data block references.
   */
  public static UniformGrid SetUpGrid( DataSet DS )
  {
    int[] Ids = Grid_util.getAreaGridIDs( DS );
    UniformGrid SampGrid = (UniformGrid)Grid_util.getAreaGrid( DS, Ids[0] );
    SampGrid.clearData_entries();
    SampGrid.setData_entries( DS );
    return SampGrid; 
  }


  /* ---------------------- ZeroAreaDetSens ----------------------------- */
  /**
   *  Set the sensitivity to zero for all pixels that are too near
   *  the edge, or whose radius is outside of [Radmin,Radmax] for
   *  the sensitivity grid for an area detector.
   *
   *  @param SensGrid    The DataGrid containing the sensitivity information.
   *  @param SampGrid    The DataGrid for the sample run
   *  @param Radmin      The minimum radius to keep
   *  @param Radmax      The maximum radius to keep 
   *  @param nedge       The number of edge pixels to discard
   */
  public static void ZeroAreaDetSens( UniformGrid SensGrid, 
                                      UniformGrid SampGrid,
                                      float       Radmin,
                                      float       Radmax,
                                      int         nedge  )
  {
    for( int row = 1; row <= SensGrid.num_rows(); row++)
      for( int col = 1; col <= SensGrid.num_cols(); col++)
      {
        boolean Z = false;

        // TODO  check this calculation.  It seems that this is correct if 
        //       numbered from 0...N-1,  but the grid is numbered 1...N
        if ( (row < nedge) || (row > SensGrid.num_rows() - nedge) || 
             (col < nedge) || (col > SensGrid.num_cols() - nedge) )
          Z = true;    

        Data DD = SampGrid.getData_entry(row, col);
        DetectorPosition dp = (DetectorPosition)
                               (DD.getAttributeValue(Attribute.DETECTOR_POS));
             
        float[] pos = dp.getCartesianCoords();
    
        float rad = pos[1] * pos[1] + pos[2] * pos[2];
        if ((rad < Radmin * Radmin) || (rad > Radmax * Radmax))
          Z = true;

        if( Z )
          SensGrid.getData_entry(row,col).getY_values()[0]= 0.0f;
     }
  }


  /* -------------------------- Init2DArray ----------------------------- */
  /**
   *  Construct a 2D array and set its values to 0.
   *  
   *  @param nrows  The number of rows to make
   *  @param ncols  The number of cols to make
   *  @return  a new float array res[][] with the specified number of rows and
   *           columns that is filled with 0's.
   */
  public static float[][] Init2DArray( int nrows, int ncols )
  {
    float[][] Res = new float[nrows][ncols];

    for( int i=0; i< nrows; i++ )
      Arrays.fill(Res[i],0.0f);

    return Res;
  }


  /* ------------------------- ConvertXsToWL ----------------------------- */
  /**
   *  Convert the given list of x values, initially representing 
   *  times-of-flight, to wavelength, based on the position of
   *  a particular detector element.
   *
   *  @param  x           list of x values, initially representing tof, that
   *                      are converted to wavelength values.
   *  @param  ds          The DataSet from which the detector element position
   *                      is found.
   *  @param  index       index in DataSet ds, identifying the particular
   *                      spectrum (and hence detector element). 
   *  @param  is_monitor  Flag indicating whether or not the specified index
   *                      corresponds to a monitor.
   *  @return  a reference to the list of x-values (array x) is also returned,
   *           in addition to modifying the values in place in array x.
   */
  public static float[] ConvertXsToWL( float[] x, 
                                       DataSet ds, 
                                       int     index, 
                                       boolean is_monitor )
  {
    int num_data = ds.getNum_entries();      // make sure we have a valid 
    if ( index < 0 || index >= num_data )    // Data index
    {
      System.out.println("ERROR: Invalid index in ConvertXsToWL " + index );
      return x;
    }

    Data data               = ds.getData_entry( index );
    AttributeList attr_list = data.getAttributeList();

                                             // get the detector position and
                                             // initial path length
    DetectorPosition position=(DetectorPosition)
                       attr_list.getAttributeValue( Attribute.DETECTOR_POS );

    Float initial_path_obj=(Float)
                        attr_list.getAttributeValue(Attribute.INITIAL_PATH);

    if( position == null || initial_path_obj == null)  // make sure it has the
    {
      System.out.println("ERROR: Missing attribute in ConvertXsToWL " + index );
      return x;
    }
      
    float initial_path = initial_path_obj.floatValue();
    float total_length;
    if ( is_monitor )
    {
      float cartesian_coords[] = position.getCartesianCoords();
      total_length = initial_path + cartesian_coords[0];
    }
    else
    {
      float spherical_coords[] = position.getSphericalCoords();
      total_length = initial_path + spherical_coords[0];
    }

    for(int i= 0; i< x.length; i++)
      x[i] = tof_calc.Wavelength( total_length, x[i] );
    return x;
  }


  /* --------------------------- ConvertToWL ------------------------------ */
  /**
   *   Convert the given DataSet from time-of-flight to Wavelength and
   *   resample the spectrum at the specified set of wavelength values. 
   *   This method is "memory efficient".  Since all have a common XScale there
   *   is little extra space.  
   *
   *   @param ds          The DataSet to be converted to WL
   *   @param wlScale     The XScale for resultant wave lengths
   *   @param is_monitor  True if the data set is a monitor data set
   *
   *   @return   returns the data set converted to wave length with 
   *             appropriate operators 
   */
   public static DataSet ConvertToWL( DataSet ds, 
                                      XScale  wlScale,
                                      boolean is_monitor )
   {
     Data D, D1;
     
     for( int i = 0; i< ds.getNum_entries(); i++)
     {
       D = ds.getData_entry(i);
       float[] xvals = D.getX_scale().getXs();
       float[] yvals = D.getY_values();
       float[] errs  = D.getErrors();
       AttributeList alist = D.getAttributeList();
       xvals = ConvertXsToWL( xvals, ds, i, is_monitor );
       if( xvals[0] > xvals[1] )
       {
         arrayUtil.Reverse(xvals);
         arrayUtil.Reverse(yvals);
         arrayUtil.Reverse(errs);
       }
       D1= Data.getInstance( new VariableXScale( xvals ), 
                             yvals, errs, D.getGroup_ID() );
       D1.setAttributeList( alist );
       D1.resample( wlScale, IData.SMOOTH_NONE );
       ds.replaceData_entry( D1, i );
     }

     ds.setX_units("Angstrom");
     ds.setX_label("WaveLength");
     ds.addLog_entry( "Converted to Wavelength" );
     String pre ="";
     if(ds.getDSType().indexOf("Monitor")>=0)
        pre="Monitor";
     
     for( int i=ds.getNum_operators()-1; i >=0; i--){
        DataSetOperator op = ds.getOperator(i);
        if( op.getClass().toString().indexOf("Diffractometer")>=0)
           pre ="Diffractometer";
        else if( op.getClass().toString().indexOf("Spectrometer")>=0)
           pre ="Spectrometer";
        if( op instanceof XAxisConversionOp)
           ds.removeOperator(op);
        else if( op instanceof XYAxisConversionOp)
           ds.removeOperator( op );
     }
     
     DataSetOperator op = getDSOp( pre+"WavelengthTo"+"Tof");
     if( op != null)
       ds.addOperator(op);
       
     op = getDSOp( pre+"WavelengthTo"+"D");
     if( op != null)
       ds.addOperator(op);
       
     op = getDSOp( pre+"WavelengthTo"+"Q");
     if( op != null)
       ds.addOperator(op);
     
     op = getDSOp( pre+"WavelengthTo"+"Energy");
     if( op != null)
       ds.addOperator(op);
     
     return ds;
   }

 
 /* ----------------------------- getDSOp ------------------------------- */
 /**
  *  Get a new instance of a specified XAxis Conversion operator.
  *
  *  @param opTitle  The title of the XAxis conversion operator to get.
  *
  *  @return  A new instance of the specified operator, or null if it can't
  *           be constructed.
  */
 private static DataSetOperator getDSOp( String opTitle )
 {
   try
   {
      Class C = Class.forName("DataSetTools.operator.DataSet.Conversion.XAxis."
                               + opTitle);
      if( C.getSuperclass() != 
         DataSetTools.operator.DataSet.Conversion.XAxis.XAxisConversionOp.class)
         return null;
      return (DataSetOperator)(C.newInstance());
   }
   catch( Exception ss)
   {
     return null;
   }
 }


  /* ------------------------ InterpolateDataSet ------------------------ */
  /**
   *  Create a new DataSet by interpolating between values in an
   *  existing DataSet.  
   *  Replace leading & trailing zeros with the first and last values
   *  respectively.  This is needed since the input data may not cover
   *  the full range of values needed for the new xscale.
   *
   *  @param ds      The DataSet whose values are to be interpolated
   *  @param xscale  The xscale at which the DataSet's values are to be
   *                 interpolated.
   *  @return        A new DataSet containing the interpolated Data blocks.
   */
  public static DataSet InterpolateDataSet( DataSet ds, XScale xscale )
  {

    float x[]       = xscale.getXs();
    float width1    = x[1] - x[0];
    float mid_points[] = new float[ x.length - 1 ];
    for ( int i = 0; i < mid_points.length; i++ )
      mid_points[i] = (x[i] + x[i+1]) / 2;

    XScale function_scale = new VariableXScale( mid_points );

    Operator to_hist = new ConvertHistogramToFunction( ds, false, false );
    to_hist.getResult();

    ds.getData_entry(0).resample( function_scale, IData.SMOOTH_LINEAR );

    Operator to_func = new ConvertFunctionToHistogram(ds, width1, false, false);
    to_func.getResult();

    XScale new_scale = ds.getData_entry(0).getX_scale();
    System.out.println( "new_hist_scale = " + new_scale );

    //
    // Replace leading & trailing zeros with the first and last values
    // respectively.  This is needed since the input data may not cover
    // the full range of values needed for the new xscale.
    // 
    float y[] = ds.getData_entry(0).getY_values();
    int first_nz = 0;
    while ( first_nz < y.length && y[first_nz] <= 0 )
      first_nz++;

    if ( first_nz < y.length )
      for ( int i = 0; i < first_nz; i++ )
        y[i] = y[ first_nz ];

    int last_nz = y.length - 1;
    while ( last_nz > 0 && y[last_nz] <= 0 )
      last_nz--;

    if ( last_nz >= 0 )
      for ( int i = last_nz + 1; i < y.length; i++ )
        y[i] = y[ last_nz ];

    if ( debug )
    {
      System.out.println("Resampled values ============================ " );
      x = new_scale.getXs();
      for ( int i = 0; i < y.length; i++ )
        System.out.println( "x,y = " + x[i] + ", " + y[i] );
      System.out.println("x,y = " + x[x.length-1] );
    }
    return ds;
  }


   /* -------------------------- FixGroupIDs ------------------------- */
   /**
    *  Set up the group IDs for the DataBlocks in some DataSet to 
    *  match the group IDS in the sample DataSet, assuming that the
    *  DataGrids are both the same size and  have their Data 
    *  references set.
    *
    *  @param  someGrid    A DataGrid containing references to its corresponding
    *                      Data blocks, for which the IDs are to be set to
    *                      match the IDs of the specified sampleGrid.
    *  @param  sampleGrid  A DataGrid containing references to its corresponding
    *                      Data blocks, whose IDs are to be used to set
    *                      the IDs of the some specified grid.
    */
   public static void FixGroupIDs( UniformGrid someGrid, 
                                   UniformGrid sampleGrid ) 
   {
      int id;
      int n_rows = someGrid.num_rows();
      int n_cols = someGrid.num_cols();
      for ( int row = 1; row <= n_rows; row++ )
        for ( int col = 1; col <= n_cols; col++ )
        {
          id = sampleGrid.getData_entry( row, col ).getGroup_ID();
          someGrid.getData_entry( row, col ).setGroup_ID(id);         
        }
   }


  /* ----------------------- BuildIndexOfID_Table ----------------------- */
  /**
   *  Build a list giving the index in the DataSet of each group ID
   *  present int the DataSet.  The table size is MaxID - MinID + 2.
   *  Position 0 stores the MinID that was present in  the DataSet.  
   *  The index of the Data block with the MinID is stored in position 1
   *  and position  MaxID + MinID + 1 stores the index of the Data block
   *  with the MaxID.  To find the Data block with ID "id", use
   *  table[ id - minID + 1 ] as the index into the DataSet.
   *
   *  @param  ds  DataSet whose DataEntries are to be indexed via the ID table.
   *
   *  @return  integer array 'table' set up so that table[ id - minID + 1 ] 
   *           contains the index in DataSet ds of the group with ID 'id'. 
   */
  public static int[]  BuildIndexOfID_Table( DataSet ds )
  {
    int n = ds.getNum_entries();
    if ( n <= 0 ) 
    {
       System.out.println( "ERROR empty DataSet in BuildIndexOfID_Table()");
       return null;
    }

    int minID = ds.getData_entry(0).getGroup_ID();
    int maxID = minID;
    int id;
    for ( int i = 1; i < n; i++ )
    {
      id = ds.getData_entry(i).getGroup_ID();
      if ( id > maxID )
        maxID = id;
      else if ( id < minID )
        minID = id;
    }

    int table[] = new int [ maxID - minID + 2 ];
    for ( int i = 1; i < table.length; i++ )
      table[i] = 0;

    table[0] = minID;
    int offset = 1 - minID;
    for ( int i = 1; i < n; i++ )
    {
      id = ds.getData_entry(i).getGroup_ID();
      table[ id + offset ] = i;
    }

    return table;
  }


  /* ------------------------------- toVec ------------------------------ */
  /**
   *  Utility for main program that just puts an array of floats into a Vector
   *
   *  @param  list   list of floats to be placed in a Vector
   *
   *  @return A new vector containing the elements of list[] in the order
   *          they are listed.
   */
  public static Vector toVec( float[] list )
  {
     if( list == null)
       return new Vector();

     Vector Res = new Vector();

     for( int i = 0; i< list.length; i++)
       Res.addElement( new Float( list[i]));

     return Res;
  }

}
