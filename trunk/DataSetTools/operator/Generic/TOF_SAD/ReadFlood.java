/*
 * File:  ReadFlood.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Revision 1.3  2003/08/21 15:26:44  rmikk
 * Set Detector Positions
 *
 * Revision 1.2  2003/07/22 16:27:01  dennis
 * Fixed formatting.
 *
 * Revision 1.1  2003/07/15 21:36:37  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.operator.Generic.TOF_SAD;

import DataSetTools.dataset.*;
import DataSetTools.operator.DataSet.Attribute.*;
import java.util.*;
import java.io.*;
import DataSetTools.util.*;
import DataSetTools.parameter.*;
import DataSetTools.util.*;
import DataSetTools.math.*;

/**
 *  This operator reads in files written by the WriteFlood operator to produce
 *  two data sets, and Efficiency DataSet and a Mask DataSet.  
 *  The XScale is just 0 to 1.
 */
public class ReadFlood extends GenericTOF_SAD{

   /**
   *    Default constructor
   */
   public ReadFlood(){
      super( "Read Flood Pattern");
   }

   /**
   *    The constructor used by Java and Jython
   *    @param  filename   the name of the file
   *    @param  nrows      the number of rows in the detector
   *    @param  ncols      the number of columns in the detector
   */
   public ReadFlood( String filename, int nrows, int ncols){
      this();
      parameters = new Vector();
      addParameter( new LoadFilePG( "Enter Filename", filename));
      addParameter( new IntegerPG("Enter number of rows", nrows) );
      addParameter( new IntegerPG("Enter numnber of cols", ncols) );
   }


   public void setDefaultParameters(){
      parameters = new Vector();
      addParameter( new LoadFilePG( "Enter Filename", null));
      addParameter( new IntegerPG("Enter number of rows", 128) );
      addParameter( new IntegerPG("Enter numnber of cols", 128) );
   }


   /**
   *     This method executes the code for this operator.  Here it reads 
   *     the data off of the file specified in the first parameter.  It
   *     creates two data sets, an Efficiency and Mask data set.  Both have
   *     the PixelInfoList Attribute set so row and column information is a
   *     preserved.  The Efficiency data set also has the errors recorded.
   *     @ return  a Vector with two elements.  The first is the Efficiency 
   *               data set and the second is the Mask data set or an 
   *               ErrorString if an error ocurred.
   */
   public Object getResult(){
     String filename = ((LoadFilePG)(getParameter(0))).getStringValue();
     int    nrows = ((IntegerPG)(getParameter(1))).getintValue();
     int    ncols = ((IntegerPG)(getParameter(2))).getintValue();

     Vector V =new Vector();
     V.addElement( new float[0]);
      FileInputStream fin = null;
     try{
         fin= new FileInputStream( filename);
        }
     catch( IOException sss){
        return new ErrorString( sss.toString() );
     }
     //-------------- Read in Efficiency y values ----------------------
     String Format = "F10.7,F10.7,F10.7,F10.7,F10.7,F10.7,F10.7,F10.7,/";
     Object S = FileIO.Read(fin, V,Format, nrows*ncols , null);
     //               --- check for errors ---------------
       if( S instanceof ErrorString)
         if( S.toString() != FileIO.NO_MORE_DATA){}
           else return S;
       if( !(S instanceof Integer) )
          return S;
       int nn = ((Integer)S).intValue();
       if( nn != nrows*ncols)
         return new ErrorString( " not enough data");
       float[] yvals = (float[])(V.firstElement());
     

    //-------------- Read in Efficiency error values ----------------------
     V.setElementAt( new float[0], 0);
     S = FileIO.Read(fin, V,Format, nrows*ncols , null);
   //                --- check for errors ---
       if( S instanceof ErrorString)
         if( S.toString() != FileIO.NO_MORE_DATA){}
           else return S;
       if( !(S instanceof Integer) )
         return S;
       nn = ((Integer)S).intValue();
       if( nn != nrows*ncols)
         return new ErrorString( " not enough data");
       float[] errors = (float[])(V.lastElement());

   //-------------- Read in Mask y values ----------------------
     V.setElementAt( new float[0], 0);
     Format ="I10,I10,I10,I10,I10,I10,I10,I10,/";
     S = FileIO.Read(fin, V,Format, nrows*ncols , null);
    //              ------ check for errors --------
        if( S instanceof ErrorString)
          if( S.toString() != FileIO.NO_MORE_DATA){}
            else return S;
        if( !(S instanceof Integer) )
           return S;
        nn = ((Integer)S).intValue();
        if( nn != nrows*ncols)
          return new ErrorString( " not enough data");
        float[] mask= (float[])(V.lastElement());
    
   //---------------------- NOW make the Data Sets------------------------

     DataSet Efficiencies = new DataSet("Efficiencies", new OperationLog(),
          "Time(us)","Time", "Av Counts","Av Counts");
     
     DataSet Mask = new DataSet("Mask", new OperationLog(), "Time(us)", "Time",
                "Valid", "Valid");
     //   Add operators
     DataSetFactory.addOperators( Efficiencies);
     Efficiencies.addOperator(new GetPixelInfo_op() );
     DataSetFactory.addOperators( Mask);
     Mask.addOperator(new GetPixelInfo_op());

     // Set up Shared Grid References
     UniformGrid gridEff = new UniformGrid(37,"cm",
                                           new Vector3D(.5f,0f,0f),
                                           new Vector3D(0f,-1f,0f),
                                           new Vector3D( 0f,0f,1f), 
                                           .21f,.21f,.001f,nrows,ncols);
     UniformGrid gridMask = new UniformGrid(38,"cm",
                                            new Vector3D(.5f,0f,0f),
                                            new Vector3D(0f,-1f,0f),
                                            new Vector3D( 0f,0f,1f), 
                                            .21f,.21f,.0011f,nrows,ncols);

     // Allocate storage for the 1 bin data
     float[] yy = new float[1];
     float[]err = new float[1];

     // Create Each Data block for the Efficiency Data Set and 
     // the Mask Data Set.
     int row = 1;
     int col = 1;
     for( int i =0; i< nrows*ncols; i++){
       // -----------------Now work with  Efficiencies
       
        yy[0]=yvals[i];
        err[0]=errors[i];
        HistogramTable D = new HistogramTable( new UniformXScale( 0f,1f,2), yy,
              err, i);
        Efficiencies.addData_entry( D);

        
        DetectorPixelInfo dpi = new DetectorPixelInfo( i,(short)row,(short)col,
                                  gridEff);
        DetectorPixelInfo[] pinf = new DetectorPixelInfo[1];
        
        pinf[0] = dpi;
        PixelInfoList pilist = new PixelInfoList(pinf);
        PixelInfoListAttribute pilistAt = new PixelInfoListAttribute( Attribute.PIXEL_INFO_LIST,
               pilist);;
        D.setAttribute( pilistAt);
       //------------------ Now work with the Mask DataSet
        yy = new float[1];
        yy[0] = mask[i];
       
        D = new HistogramTable( new UniformXScale( 0f,1f,2), yy, i); 
        Mask.addData_entry( D );
        dpi = new DetectorPixelInfo( i,(short)row,(short)col, gridMask);
        
        DetectorPixelInfo[] pinf1 = new DetectorPixelInfo[1];
        pinf1[0] = dpi; 
        D.setAttribute( new PixelInfoListAttribute(Attribute.PIXEL_INFO_LIST,
                        new PixelInfoList( ( pinf1))   )  );
       
        col++;
        if( col > ncols)
        {
          row++; 
          col=1;
        }
     }
     
     gridEff.setDataEntriesInAllGrids( Efficiencies );
     gridMask.setDataEntriesInAllGrids( Mask );
     Grid_util.setEffectivePositions( Efficiencies, gridEff.ID());
     Grid_util.setEffectivePositions( Mask, gridMask.ID());

     V = new Vector();
     V.addElement( Efficiencies);
     V.addElement( Mask);
     return V;
   }

   public String getDocumentation(){
      StringBuffer Res = new StringBuffer();
      Res.append("@overview This operator reads in files written by the");
      Res.append(" WriteFlood operator to produce two data sets,");
      Res.append(" an Efficiency DataSet and a Mask DataSet. ");
      Res.append(" The XScale is just 0 to 1.");
      Res.append("@param  filename -  the name of the file");
      Res.append("@param  nrows -    the number of rows in the detector");
      Res.append("@param  ncols -    the number of columns in the detector");
      Res.append("@return  A Vector with two elements, the first is the");
      Res.append(" Efficiency data set and the second is the mask data set");
      Res.append("@assumptions The data set was written by the Write Flood");
      Res.append(" operator or in that specific format. The Efficiency y");
      Res.append(" are all written, then the errors, then the mask values");
      Res.append(" (1's or 0's). Only eight numbers are written per line. ");
      Res.append(" The Efficiency values are written F10.7 and the Mask");
      Res.append(" values are written I10");
      return Res.toString();
   }
}//ReadFlood
