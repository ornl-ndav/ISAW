/**
 * File:  ContourView.java
 *
 * Copyright (C) 2001, Rion Dooley & Ruth Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 * Modified:
 *
 *  $Log$
 *  Revision 1.4  2002/07/25 20:55:22  rmikk
 *  The times now reflect if the Data is Histogram or Function
 *     Data.
 *  Eliminated excess returns and fixed indentations
 *
 *  Revision 1.3  2002/07/19 22:16:45  rmikk
 *  Fixed a minor error.  The last row and column now
 *     reports conversion and info information
 *
 *  Revision 1.2  2002/07/12 21:12:54  rmikk
 *  Included documentation to automatically record log messages
 *
 */
package DataSetTools.viewer.Contour;


import gov.noaa.pmel.sgt.demo.*;
import gov.noaa.pmel.sgt.dm.*;
import IsawGUI.Util;
import java.awt.*;
import java.lang.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.instruments.*;
import DataSetTools.math.*;


/**
 * Provides a mechanism for selecting and viewing portions of an Area Detector
 * Data Set using SGT's contour plot
 *
 * @see DataSetTools.dataset.DataSet
 * @see DataSetTools.viewer.DataSetViewer
 *
 */
public class ContourData
{

   SGTData data_;
   DataSet ds;
   double maxvalue = -1,
          minvalue = -1;
   int maxrows, 
       maxcols;
   float lastTime = -1;
   int[][] groups = null;
   double[] axis1, 
            axis2;
   //*****************************************************************************************
   //						Constructors
   //*****************************************************************************************
   public ContourData( DataSet data_set )
   {
      ds = data_set;
      maxvalue = 0;
      minvalue = 9999999;
      //Load area detector data
      //Build a vector holding the row and column entries
      maxrows = -1;
      maxcols = -1;
      int[][] k = new int[ds.getNum_entries() + 1][3];
      int w = 0;

      for( int i = 0; i < ds.getNum_entries(); i++ )
      {
         Data db = ds.getData_entry( i );
         DetInfoListAttribute dl = ( DetInfoListAttribute )db.getAttribute( Attribute.DETECTOR_INFO_LIST );

         if( dl != null )
         {
            DetectorInfo[] dll = ( DetectorInfo[] )dl.getValue();

            if( dll.length >= 1 )
            {
               int row = ( int )dll[0].getRow();
               int col = ( int )dll[0].getColumn();

               if( row > maxrows )
                  maxrows = row;
               if( col > maxcols )
                  maxcols = col;

               k[w][0] = row;
               k[w][1] = col;
               k[w][2] = i;
               w++;
            }

         }
      }

      //Now that the vector holds row, column, and group index data in that order, we
      //can build a 2d array containing the group index and using row and column values
      //as indecies.
      //note: the zeroth row and column will be empty
      groups = new int[maxrows + 1][maxcols + 1];
      for( int i = 0; i < maxrows + 1; i++ )
         Arrays.fill( groups[i], -1 );
      for( int i = 0; i < ( w ); i++ )
      {
         groups[ k[i][0] ][ k[i][1] ] = k[i][2];

      }
      axis1 = new double[ ( maxcols + 1 ) ];
      axis2 = new double[ ( maxrows + 1 ) ];
      for( int row = 0; row < maxrows + 1; row++ )
         axis2[row] = row;
      for( int col = 0; col < maxcols + 1; col++ )
         axis1[col] = col;
   }


   //*****************************************************************************************
   //						   Methods
   //*****************************************************************************************
   public SGTData getSGTData( float X )
   {

      // X = (float)1261.596;		//Set time slice parameter, t=X=0
      int i, 
          j,
          G,
          row,
          col,
          w = 0;
      SimpleGrid sg;
      SGTMetaData xMeta;
      SGTMetaData yMeta;
      SGTMetaData zMeta;

      lastTime = X;

      //Given the group indecies and time slice, we look up the row, column, and y value and store
      //them in separate 1d arrays.  Axis data itself will be integer values.  The axis arrays
      //will hold no reapeated values. (ie values.size = axis1.size * axis2.size)
      double[] values = new double[ ( maxrows + 1 ) * ( maxcols + 1 ) ];

      maxvalue = -1;
      minvalue = -1;
      for( col = 0; col < maxcols + 1; col++ )
      {
         for( row = 0; row < maxrows + 1; row++ )
         {
            G = groups[row][col];
            if( G < 0 )
               values[w] = 0.0f;
            else
            {
               Data db = ds.getData_entry( G );

               values[w] = db.getY_value( X, 0 );
               if( values[w] > maxvalue )
                  maxvalue = values[w];
               if( values[w] < minvalue )
                  minvalue = values[w];
            }

            w = w + 1;
         }
      }

      // Create SGTGrid from axis and data
      SimpleGrid sl;

      xMeta = new SGTMetaData( "Column", "" );
      yMeta = new SGTMetaData( "Row", "" );
      zMeta = new SGTMetaData( "Count", "" );
      sl = new SimpleGrid( values, axis1, axis2, "Area Detector Data" );
      sl.setXMetaData( xMeta );
      sl.setYMetaData( yMeta );
      sl.setZMetaData( zMeta );

      data_ = sl;
      return data_;

   }


   /*
    * Get the range of time vales for the area detector data
    */
   public float[] getTimeRange()
   {
      XScale x_scale;
      //Read in the first data entry and get the range of time values. While
      //there could be missing data in the returned array, we will take a
      //chance and say that the first detector will give us good data.
      Data db = ds.getData_entry( 0 );

      x_scale = db.getX_scale();
      float[] times = new float[x_scale.getNum_x()];

      times = x_scale.getXs();
      if( db instanceof FunctionTable )
         return times;
      if( db instanceof FunctionModel )
         return times;

      float[] Hxvals = new float[ times.length - 1];

      for( int i = 0; i < Hxvals.length; i++ )
         Hxvals[i] = ( times[i] + times[i + 1] ) / 2.0f;
      return Hxvals;

   }


   public float getTime()
   {
      return lastTime;
   }


   public int getGroupIndex( double row, double col )
   {
      int r, c;

      r = ( int )( row );
      c = ( int )( col );
      if( row - r > .5 )
         r++;
      else if( r - row > .5 )
         r--;
      if( col - c > .5 )
         c++;
      else if( c - col > .5 )
         c--;
      if( r <= 0 )
         return -1;
      if( c <= 0 )
         return -1;
      if( r > maxrows )
         return -1;
      if( c > maxcols )
         return -1;
      return groups[r][c];

   }

}
