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
 *  Revision 1.10  2002/10/07 15:00:37  rmikk
 *  Will now keep better information on a cell.  The last group
 *    and its corresponding x value that added non zero intensities
 *    to a cell are saved.
 *  Fixed the case for one column so that the Contour View can
 *    be used to display 1 row "Arrays".
 *  Fixed an error that occurs in the altermative( not row/
 *     col) display.  The row value and column value are now
 *    calculated and recorded for each of the x values in a
 *     range.
 *
 *  Revision 1.9  2002/08/30 15:29:46  rmikk
 *    -If there is no  DetInfoListAttribute or if two groups have the same row or column,
 *      a phi,theta vs time countour plot is shown
 *    -added units to the data
 *    -fixed errors in calculating intensities
 *    -Added code to take care of one row or one column cases
 *    -Gave a better estimate of the range of intensities caused by several groups or
 *     several channels being mapped to the same Contour "pixel".
 *
 *  Revision 1.8  2002/08/02 19:34:13  rmikk
 *  Semi Fix to replace XScale features
 *
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
import DataSetTools.util.*;

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
  public ContourData( DataSet data_set, IAxisHandler Axis1, IAxisHandler Axis2, 
                     IAxisHandler Axis3)
    {ThetPhiData( data_set, Axis1, Axis2, Axis3);
    }
  private void ThetPhiData( DataSet data_set, IAxisHandler Axis1, IAxisHandler Axis2, 
                     IAxisHandler Axis3)
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
         
         if( dl == null )
           {ThetPhiData( ds, new thetaAxisHandler( ds), new phiAxisHandler( ds), 
                          new TimeAxisHandler( ds ));
            return;
           }
         else
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
         {Arrays.fill( groups[i], -1 );
        
         }
      for( int i = 0; i < ( w ); i++ )
      { if( groups[ k[i][0] ][ k[i][1] ] >0)
           {ThetPhiData( ds,  new thetaAxisHandler( ds),new phiAxisHandler( ds),
                          new TimeAxisHandler( ds ));
            return;
           }
        else
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
            }

            w = w + 1;
         }
      }
     
      // Create SGTGrid from axis and data
      SimpleGrid sl;

      xMeta = new SGTMetaData( "Column", "");
      yMeta = new SGTMetaData( "Row", "" );
      zMeta = new SGTMetaData( ds.getY_label(), ds.getY_units() );
   
      sl = new SimpleGrid( values, axis1, axis2, "Area Detector Data" );
      sl.setXMetaData( xMeta );
      sl.setYMetaData( yMeta );
      sl.setZMetaData( zMeta );

      data_ = sl;
      return data_;

   }

  private float CalcY( float[] yy, float Index1, float Index2)
    {float Res =0;
     float index1,index2;
     if( Index1 < Index2)
       {index1 = Index1;
        index2 = Index2;
        }
     else
       {index1 = Index2;
        index2 = Index1;
       }
     for( int i = (int)index1 + 1; i <=(int)index2 -1 ; i++)
        {Res += yy[i];
         if(bg)System.out.print("("+i+","+yy[i]+")");
         }
     
     if( (int)index1 < yy.length)
        if((int)index1 < (int)index2)
          {Res += yy[(int)index1]*(index1 +1 -(int)index1);
           if(bg)System.out.print("(A"+index1+","+yy[(int)index1]+")"+Res);
          }
        else
          Res += yy[(int)index1]*(index2-index1);
     
     if( (int)index2  < yy.length)
          if((int)index1 < (int)index2)
           {Res += yy[(int)index2  ]*(index2-(int)index2);
            if(bg)System.out.print("(B"+index2+","+yy[(int)index2]+")"+Res);
           }
    if(bg)System.out.println("y="+Res);
    bg = false;
     return Res;
      
    }
  // fixes out of range indecies(index1 only) to be start of a valid range
  // of indecies or -1 if not possible.
  private float fixIndex( float index1, float index2,int groupIndx)
    { if( groupIndx < 0)
        return -1;
      if( groupIndx >= ds.getNum_entries())
        return -1;
      int numX = ds.getData_entry( groupIndx).getX_scale().getNum_x();
      if((index1 >=0)&& (index1 < numX))
        return index1;
      if( (index2 <0) ||(index2 >= numX))
         return -1;
      if( index1 >= numX)
         return numX -1;
      if( index2 < numX/2)
         return 0;
      return numX-1;
     }
  int[] Groupss, Inds;
  double[] values;
  boolean bg=false;
  public SGTData getSGTDataSpecial( float X )
   {SimpleGrid sg;
    SGTMetaData xMeta;
    SGTMetaData yMeta;
    SGTMetaData zMeta;
    lastTime = X;
    //System.out.println("in SGTData"+minAx1+","+minAx2+","+minAx3
     //     +","+maxAx1+","+ maxAx2+","+maxAx3);
    values = new double[ nrowws*ncolls];
    Arrays.fill( values, 0.0f);
    Groupss = new int[nrowws*ncolls];
    Inds = new int[nrowws*ncolls];
    Arrays.fill( Groupss, -1);
    Arrays.fill( Inds, -1);
    int Trow=(int)( nrowws*(-4-minAx2)/(maxAx2-minAx2) );
    int Tcol =(int)( ncolls*(-4-minAx1)/(maxAx1-minAx1) );
    //System.out.println("-------------------------------");
    float SS=0;
    for( int i=0;i< ds.getNum_entries(); i++)
      {float index1 = Axis3.getXindex( i, X);
       float index2 =  Axis3.getXindex( i, X - (maxAx3 - minAx3)/ntimes);
      
       //if(i==0)System.out.println("gr,indx1,indx2="+i+","+index1+","+index2+","+
       //          maxAx3+","+minAx3);

       index1 = fixIndex( index1,index2, i);
       index2 = fixIndex( index2, index1, i);


       if( index2 <0) 
            index2 = 0;  
       if( index1 > index2)
         { float x = index1;
           index2 = index1;
           index1 = x;
          } 
       Data D =ds.getData_entry(i); 
       float[] yy =D.getY_values();
       
       if( (index1 >= 0) && (index2 >=0))
         for( int indx =(int)index1; indx <=(int)index2;indx++)
         {
          //int indx = (int)(( index1 + index2)/2);
          float ax1= Axis1.getValue( i,indx);
          float ax2 = Axis2.getValue( i,indx);
          int row ;
          if( maxAx2==minAx2) 
            row=0;
          else 
            row= (int)( nrowws*(ax2-minAx2)/(maxAx2-minAx2) );
          int col;
          if( maxAx1 == minAx1) 
              col = 0;
          else 
              col =(int)( ncolls*(ax1-minAx1)/(maxAx1-minAx1) );
          if(3==2)
           System.out.println("i,ax1,ax2,row,col,index1,index2="+i+","+
              ax1+","+ax2+","+row+","+col+","+index1+","+index2);
         
          if( row >= nrowws) row = nrowws-1;
          if( col >= ncolls) col = ncolls -1;
          if( row < 0) row =0;
          if( col < 0) col = 0;
          //System.out.println("  ax1,ax2,row,col="+ax1+","+ax2+","+row+","+col);
          
          //if( ax1 >=-7.1) if( ax1<-7) 
            //if(ax2 >-8.1) if( ax2<-8)
            if( 2==3)
             if( indx >=0)if( indx < D.getX_scale().getNum_x())
             {bg = true;
              System.out.print(i+","+X+","+Axis3.getValue(i,indx)+","
                   +","+ax1+","+ax2+","+row+","+col
                );
              } 
          double y = 0,y1 = 0, y2 = 0;
         
          if( indx >=0)
           if( indx < yy.length)// D.getX_scale().getNum_x())
            {
              y = yy[indx];//CalcY(yy, index1,index2);
            }
         
         bg=false;
         if( (y != 0) &&(index1 !=index2))
           { //if(values[col*nrowws+row]>0)
              // System.out.println("Dup "+row+","+col);
            double yx=y;
            if( indx ==(int)index1)
              y =y - ( index1-(int)index1)*yx;
            if( indx == (int)index2)
              y= y - (1-index2+(int)index2)*yx;
            values[ col*nrowws+row ]+=y;
            Groupss[ col*nrowws+row ] = i;
            Inds[ col*nrowws+row ] = indx;
            if(3==2)if( Trow==row)if(Tcol==col)
              { SS+=y;
               System.out.println("Gr,indx,y="+i+","+indx+","+index1+","+","+index2+","+y+","+SS);
              }
               
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
      xMeta = new SGTMetaData( Axis1.getAxisName(), Axis1.getAxisUnits() );
      yMeta = new SGTMetaData( Axis2.getAxisName(), Axis2.getAxisUnits() );
      zMeta = new SGTMetaData( ds.getY_label(), ds.getY_units() );
      sl = new SimpleGrid( values, axis1, axis2, "Area Detector Data" );
      sl.setXMetaData( xMeta );
      sl.setYMetaData( yMeta );
      sl.setZMetaData( zMeta );
     
      data_ = sl;
      //System.out.println("v of special="+values[Tcol*nrowws+Trow]);
      return data_;
    }
 
   public ClosedInterval getYRange()
     {ClosedInterval Yrange = ds.getYRange();
      if( mode <1)
        return Yrange;
      float mult = 1.0f;
      mult =(float)java.lang.Math.max(1.0,ds.getXRange().getNum_x()/(float)ntimes);
      mult *= (float)java.lang.Math.max(1.0, ds.getNum_entries()/(float)(nrowws*ncolls));
      return new ClosedInterval( Yrange.getStart_x()*mult,
                                 Yrange.getEnd_x()*mult);

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
     /*int Group = getGroupIndex( row, col);
     if( Group < 0)
       return Float.NaN;
     float time1 = Axis3.getXindex( Group, lastTime);
     float time2 = Axis3.getXindex( Group, lastTime - (maxAx3 - minAx3)/ntimes);
     float time =( fixIndex( time1,time2,Group)+fixIndex( time2,time1,Group))/2;

     Data D = ds.getData_entry(Group);
     float Ttime = D.getX_scale().getX((int)time);
     //System.out.println( "Gr,timeind,Time,lasttime="+Group+","+time1+","+
     //         Ttime+","+lastTime +","+time2);
            
     return Ttime;
     */
    float R;
    if( maxAx2 ==minAx2) R = 0;
    else R = (int)((row-minAx2)/(maxAx2-minAx2)*nrowws);
    float C ;
    if( maxAx1 == minAx1)C = 0;
    else C=(int) ((col -minAx1)/(maxAx1-minAx1)*ncolls);
 

   if( R<-.5) return Float.NaN;
   if( C<-.5) return Float.NaN;
   if( R >nrowws+.5) return Float.NaN;
   if( C >ncolls+.5) return Float.NaN;
   int r = (int) (R +.5 );
   int c = (int) (C + .5);
   
   int G =Groupss[ c*nrowws+r];
   int indx = Inds[c*nrowws+r]; 
   if( G == -1)
     return Float.NaN;
   Data D = ds.getData_entry( G);  
   return D.getX_scale().getX(indx);
   }

   public void setXScale( XScale xscale)
     {if( mode ==0)
        {if( xscale == null)
            x_scale = ds.getData_entry(0).getX_scale();
         else
            x_scale = xscale;
        
         ds =(DataSet)( dsSave.clone());
         for( int j=0; j< ds.getNum_entries(); j++)
            ds.getData_entry(j).resample( x_scale,0);
        }
      else if( xscale != null)
       { ntimes = xscale.getNum_x();
         if( ntimes <10)
             ntimes = 10;
       }
      else 
         ntimes = 20;  
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
         { float R;
           if( maxAx2 ==minAx2) R = 0;
           else R = (int)((row-minAx2)/(maxAx2-minAx2)*nrowws);
           float C ;
           if( maxAx1 == minAx1)C = 0;
           else C=(int) ((col -minAx1)/(maxAx1-minAx1)*ncolls);
 

           if( R<-.5) return -1;
           if( C<-.5) return -1;
           if( R >nrowws+.5) return -1;
           if( C >ncolls+.5) return -1;
           r = (int) (R +.5 );
           c = (int) (C + .5);
           
           //System.out.println( "RCrc="+R+","+C+","+row+","+col +","+values[c*nrowws+r]);
           int G =Groupss[ c*nrowws+r];
          // float xIndex = Axis3.getXindex( G, lastTime);
          // System.out .println("Gr, A1,A2,time, timeIndex="+G+","+Axis1.getValue(G,xIndex)+","+
            //  Axis2.getValue( G, xIndex)+ ","+lastTime+","+Axis3.getValue(G,xIndex)+","+xIndex);
           return G;
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
      int X = ds.getNum_entries();
      if( maxAx2 == minAx2) nrowws=1;
      else nrowws =(int)(.5+java.lang.Math.sqrt( 8*ds.getNum_entries()*(maxAx2-minAx2)/(maxAx1-minAx1)));
       
      if(maxAx1 == minAx1) ncolls =1;
      else if( nrowws == 1) ncolls = X;
      else ncolls = (int)(.5+nrowws*(maxAx1-minAx1)/(maxAx2-minAx2));
      ntimes = 20;
     
      //System.out.println("orig nrowws,ncolss="+nrowws+","+ncolls+","+X);
 
      
      
      if( nrowws*ncolls > 5*X)
        if( ncolls > nrowws)
           ncolls = java.lang.Math.min(200, 3*X/nrowws);
        else
           nrowws = java.lang.Math.min(200, 3*X/ncolls);
      ncolls =java.lang.Math.min(200, ncolls);
      nrowws =java.lang.Math.min(200, nrowws);
      if( nrowws <= 2) nrowws =3;
      if( ncolls <= 2) ncolls =3;
      //System.out.println("nrowws,ncols="+ nrowws+","+ncolls);
      axis2 = new double[nrowws];
      axis1 = new double[ncolls];
      double D = (maxAx1-minAx1)/ncolls;
      if(java.lang.Math.abs(maxAx1-minAx1) <=.000001*java.lang.Math.max(
           java.lang.Math.abs(maxAx1),java.lang.Math.abs(minAx1)))
          D =1;
      for(int i = 0; i < ncolls; i++)
        axis1[i] = minAx1 + (i+.5)*D;
       D = (maxAx2-minAx2)/nrowws;
      if(java.lang.Math.abs(maxAx2-minAx2) <=.000001*java.lang.Math.max(
           java.lang.Math.abs(maxAx2),java.lang.Math.abs(minAx2)))
          D =1;
      for(int i = 0; i < nrowws; i++)
        axis2[i] = minAx2 + (i+.5)*D;
     //System.out.println("end Setup min-max are"+minAx1+","+maxAx1+","+minAx2+","+maxAx2+","
     //            +minAx3+","+maxAx3);
     //System.out.println("nrows, ncols="+nrowws+","+ncolls);
    }//SetUp
}
