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
 *  Revision 1.7  2002/08/01 22:10:54  rmikk
 *  Fixed some errors with the Arbitrary axis handler system
 *
 *  Revision 1.6  2002/08/01 13:51:45  rmikk
 *  Implemented code to deal with arbitrary axis handlers
 *
 *  Revision 1.5  2002/07/30 14:35:59  rmikk
 *  Improved handling of differing XScales.
 *
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
   DataSet ds,
           dsSave;
   double maxvalue = -1,
          minvalue = -1;
   int maxrows, 
       maxcols;
       
   float lastTime = -1;
   int[][] groups = null;
   double[] axis1, 
            axis2;
   XScale x_scale ;
   IAxisHandler Axis1,Axis2,Axis3;
   int mode =0;
   //*****************************************************************************************
   //						Constructors
   //*****************************************************************************************
  public ContourData( DataSet data_set, IAxisHandler Axis1, IAxisHandler Axis2, IAxisHandler Axis3)
    {mode =1;
     ds = data_set;
     dsSave = data_set; 
     this.Axis1= Axis1;
     this.Axis2= Axis2;
     this.Axis3= Axis3;
     SetUpInfo( ds, Axis1, Axis2, Axis3);
    }
  public ContourData( DataSet data_set )
   {
      ds = data_set;
      dsSave = data_set;
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

     x_scale = data_set.getData_entry(0).getX_scale();
     ds = (DataSet)(dsSave.clone());
     for( int j=0; j< ds.getNum_entries(); j++)
       ds.getData_entry(j).resample( x_scale,0);
   }


   //*****************************************************************************************
   //						   Methods
   //*****************************************************************************************
   public SGTData getSGTData( float X )
   {
      if( mode !=0)
         return getSGTDataSpecial( X);
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
               values[w] = db.getY_value( X,0);
               //if( x_scale != null)
               //   db.resample( x_scale , 0);
              /* float[] yvalues = db.getY_values( x_scale, 0);
               int ii = x_scale.getI( X );
               if( db.isHistogram()) ii--;
               if( ii < 0) 
                  values[w] = 0;
               else if( ii > yvalues.length)
                  values[w] = 0;
               else
                 values[w] = yvalues[ii];
               if( values[w] > maxvalue )
                  maxvalue = values[w];
               if( values[w] < minvalue )
                  minvalue = values[w];
              */
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

  int[] Groupss;
  double[] values;
  public SGTData getSGTDataSpecial( float X )
   { SimpleGrid sg;
      SGTMetaData xMeta;
      SGTMetaData yMeta;
      SGTMetaData zMeta;
     lastTime = X;
    values = new double[ nrowws*ncolls+1];
    Arrays.fill( values, 0.0f);
    Groupss = new int[nrowws*ncolls+1];
    Arrays.fill( Groupss, -1);
    for( int i=0;i< ds.getNum_entries(); i++)
      {int indx = Axis3.getXindex( i, X);
       if( indx >= 0)
         {float ax1= Axis1.getValue( i,indx);
          float ax2 = Axis2.getValue( i,indx);
          int row = (int)( nrowws*(ax2-minAx2)/(maxAx2-minAx2));
          int col =(int)( ncolls*(ax1-minAx1)/(maxAx1-minAx1));
          double y=0;
          Data D =ds.getData_entry(i);
          if( indx >=0)
           if( indx < D.getX_scale().getNum_x())
            {//if( D.isHistogram() && indx >0) indx--;
            // y = D.getX_scale().getX(indx);
               y = D.getY_values()[indx];
            }
         if( y > 0)
           { //if(values[col*nrowws+row]>0)
              // System.out.println("Dup "+row+","+col);
            values[ col*nrowws+row ]+=y;
            Groupss[ col*nrowws+row ] = i;
           // if( (i==257)||(i==345))
            //  System.out.println("XX i,y,indx="+i+","+y+","+indx);
           // System.out.println("("+ax1+","+ax2+","+i+","+y);
            }
      }//if indx >=0
      }
     /* System.out.println("Time ="+X);
      for( int k=0; k<values.length;k++)
        {
         if( (values[k] >0)||(Groupss[k]>=0))
          System.out.print("("+k+":"+values[k]+","+Groupss[k]+")  ");
         }
      System.out.println("___________________");
      */
      SimpleGrid sl;
      xMeta = new SGTMetaData( Axis1.getAxisName(), "" );
      yMeta = new SGTMetaData( Axis2.getAxisName(), "" );
      zMeta = new SGTMetaData( Axis3.getAxisName(), "" );
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
     //XScale x_scale;
      //Read in the first data entry and get the range of time values. While
      //there could be missing data in the returned array, we will take a
      //chance and say that the first detector will give us good data.
      if( mode !=0)
        { //System.out.println("in getTimeRange "+minAx3+","+maxAx3+","+ntimes);
          return (new UniformXScale(minAx3,maxAx3, ntimes)).getXs();
        }
      Data db = ds.getData_entry( 0 );

      if( x_scale == null)
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


   public float getTime(double row, double col)
   { if( mode == 0)
        return lastTime;
      //Needs to be the time in the data set.
     int Group = getGroupIndex( row, col);
     if( Group < 0)
       return Float.NaN;
     int time = Axis3.getXindex( Group, lastTime);
     Data D = ds.getData_entry(Group);
     return D.getX_scale().getX(time);
      
   }

   public void setXScale( XScale xscale)
    { if( xscale == null)
        x_scale = ds.getData_entry(0).getX_scale();
      else
        {x_scale = xscale;
        
         }
      ds =(DataSet)( dsSave.clone());
      for( int j=0; j< ds.getNum_entries(); j++)
        ds.getData_entry(j).resample( x_scale,0);
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
     
      
     
      if( mode == 0)
        {
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
      else //returns user coordinates
         { float R = (int)((row-minAx2)/(maxAx2-minAx2)*nrowws);
           float C =(int) ((col -minAx1)/(maxAx1-minAx1)*ncolls);
          // System.out.println("mode 1 "+r+","+c+","+nrowws+","+ncolls);
          // System.out.println("Range row(Ax2)="+minAx2+","+row+","+maxAx2+","+
           //      (row-minAx2)+","+(maxAx2-minAx2)+","+((row-minAx2)/(maxAx2-minAx2)));
          // System.out.println( ((row-minAx2)/(maxAx2-minAx2)*nrowws)+","+r);
           if( R<0) return -1;
           if( C<0) return -1;
           if( R >nrowws) return -1;
           if( C >ncolls) return -1;
           r = (int) R;
           c = (int) C;
           //System.out.println( "RCrc="+R+","+C+","+r+","+c +","+values[c*nrowws+r]);
           int G =Groupss[ c*nrowws+r];
           int xIndex = Axis3.getXindex( G, lastTime);
          // System.out .println("Gr, A1,A2,time, timeIndex="+G+","+Axis1.getValue(G,xIndex)+","+
            //  Axis2.getValue( G, xIndex)+ ","+lastTime+","+Axis3.getValue(G,xIndex)+","+xIndex);
           return Groupss[ c*nrowws+r];
         }

   }
 float minAx1,minAx2, minAx3, maxAx1, maxAx2, maxAx3;
 int nrowws, ncolls, ntimes;
 
 public void SetUpInfo( DataSet ds, IAxisHandler Axis1,IAxisHandler Axis2,IAxisHandler Axis3)
    { 
      
      for( int i = 0; i < ds.getNum_entries(); i++)
        { if( i==0)
            { minAx1= Axis1.getMinAxisValue( i);
              maxAx1= Axis1.getMaxAxisValue( i);
              minAx2= Axis2.getMinAxisValue( i);
              maxAx2= Axis2.getMaxAxisValue( i);
              minAx3= Axis3.getMinAxisValue( i);
              maxAx3= Axis3.getMaxAxisValue( i);
            }
         else if( Axis1.getMinAxisValue(i) < minAx1)
            minAx1=Axis1.getMinAxisValue(i);
         else if( Axis1.getMaxAxisValue(i) > maxAx1)
            maxAx1=Axis1.getMaxAxisValue(i);
         else if( Axis2.getMinAxisValue(i) < minAx2)
            minAx2=Axis2.getMinAxisValue(i);
         else if( Axis2.getMaxAxisValue(i) > maxAx2)
            maxAx2=Axis2.getMaxAxisValue(i);
         else if( Axis3.getMinAxisValue(i) < minAx3)
            minAx3=Axis3.getMinAxisValue(i);
         else if( Axis3.getMaxAxisValue(i) > maxAx3)
            maxAx3=Axis3.getMaxAxisValue(i);
         
         //System.out.println("Ax1 min,max="+ minAx1+","+maxAx1);
         }
      ncolls =(int)(.5+java.lang.Math.sqrt( 8*ds.getNum_entries()*(maxAx2-minAx2)/(maxAx1-minAx1)));
       
      nrowws = (int)(.5+ncolls*(maxAx1-minAx1)/(maxAx2-minAx2));
      ntimes = 20;
      axis2 = new double[nrowws];
      axis1 = new double[ncolls];
      for(int i = 0; i < ncolls; i++)
        axis1[i] = minAx1 + i*(maxAx1-minAx1)/ncolls;
      
      for(int i = 0; i < nrowws; i++)
        axis2[i] = minAx2 + i*(maxAx2-minAx2)/nrowws;
     //System.out.println("end Setup min-max are"+minAx1+","+maxAx1+","+minAx2+","+maxAx2+","
     //            +minAx3+","+maxAx3);
     //System.out.println("nrows, ncols="+nrowws+","+ncolls);
    }//SetUp
}
