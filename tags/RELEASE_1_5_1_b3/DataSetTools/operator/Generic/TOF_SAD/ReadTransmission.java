
/*
 * File:  ReadTransmission.java 
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
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2003/08/21 15:16:53  rmikk
 * Fixed error
 *
 * Revision 1.1  2003/07/25 16:37:15  rmikk
 * Initial checkin
 *
 * Revision 1.1  2003/07/15 21:36:37  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.operator.Generic.TOF_SAD;
import DataSetTools.dataset.*;
import java.util.*;
import java.io.*;
import DataSetTools.util.*;
import DataSetTools.parameter.*;
import DataSetTools.util.*;
import DataSetTools.math.*;

/**
*    This operator reads in files written by the WriteFlood operator to produce
*    two data sets, and Efficiency DataSet and a Mask DataSet.  The XScale is just 0 to 1.
*/
public class ReadTransmission extends GenericTOF_SAD{

   /**
   *    Default constructor
   */
   public ReadTransmission(){
      super( "Read Transmission File");
   }

   /**
   *    The constructor used by Java and Jython
   *    @param  filename   the name of the file
   *    @param  ntimes      the number of rows in the detector
   */
   public ReadTransmission( String filename, int ntimes){
      this();
      parameters = new Vector();
      addParameter( new LoadFilePG( "Enter Filename", filename));
      addParameter( new IntegerPG("Enter number of time channels", ntimes) );
   }


   public void setDefaultParameters(){
      parameters = new Vector();
      addParameter( new LoadFilePG( "Enter Filename", null));
      addParameter( new IntegerPG("Enter number of time channels", 68) );
   }


   /**
   *     This method executes the code for this operator.  Here it reads 
   *     the data off of the file specified in the first parameter.  It
   *     creates a Transmission data set.    The Tranmission data set also has the errors 
   *     recorded.
   *     @ return  the Transmission data set
   *     
   */
   public Object getResult(){
     String filename = ((LoadFilePG)(getParameter(0))).getStringValue();
     int    ntimes = ((IntegerPG)(getParameter(1))).getintValue();
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
     String Format = "E15.5,E15.5,E15.5,E15.5,E15.5,/";
     Object S = FileIO.Read(fin, V,Format, ntimes , null);
     //               --- check for errors ---------------
       if( S instanceof ErrorString)
         if( S.toString() != FileIO.NO_MORE_DATA){}
           else return S;
       if( !(S instanceof Integer) )
          return S;
       int nn = ((Integer)S).intValue();
       if( nn != ntimes)
         return new ErrorString( " not enough data");
       float[] yvals = (float[])(V.firstElement());
     
    //-------------- Read in Efficiency error values ----------------------
     V.setElementAt( new float[0], 0);
     S = FileIO.Read(fin, V,Format,ntimes , null);
   //                --- check for errors ---
       if( S instanceof ErrorString)
         if( S.toString() != FileIO.NO_MORE_DATA){}
           else return S;
       if( !(S instanceof Integer) )
         return S;
       nn = ((Integer)S).intValue();
       if( nn !=ntimes)
         return new ErrorString( " not enough data");
       float[] errors = (float[])(V.lastElement());
   //--------------- Read Last line with Run Numbers --------------
      V .setElementAt( new String[0],0);
     
       S = FileIO.Read(fin, V,"S30",1 , null);
   //                --- check for errors ---
       if( S instanceof ErrorString)
         if( S.toString() != FileIO.NO_MORE_DATA){}
           else return S;
       if( !(S instanceof Integer) )
         return S;
       nn = ((Integer)S).intValue();
       if( nn !=1)
         return new ErrorString( " not enough data");

     String S8 = ((String[])V.elementAt(0))[0];
     String[] runNums = StringUtil.extract_tokens( S8.trim(), "- ");
   //---------------------- NOW make the Data Sets------------------------

     DataSet Transmission = new DataSet("Tr-"+runNums[0], new OperationLog(),
          "Channel","Channel", "Rel Counts","Rel Counts");
     
     //   Add operators
     DataSetFactory.addOperators( Transmission);
     String S5 ="";
     for( int i = 0; i< runNums.length -1;i++){
       S5 =S5+runNums[i];
       if( i+1 < runNums.length-1)
         S5 = S5+","; 
     }
     IntListAttribute run= new IntListAttribute(Attribute.RUN_NUM,
                  IntList.ToArray(S5));
     Transmission.setAttribute( run);
   
     HistogramTable d = new  HistogramTable( new UniformXScale(0.0f,ntimes+0f, ntimes+1),
                  yvals,errors, 0);
     d.setAttribute( run);
     Transmission.addData_entry(d);
     return Transmission;

   }

   public String getDocumentation(){
      StringBuffer Res = new StringBuffer();
      Res.append( "@overview This operator reads in files written by the ");
      Res.append("WriteFlood operator to produce a Transmission");
      Res.append(" DataSet.  The XScale is just 0 to number of channels-1.");
      Res.append("@param  filename -  the name of the file");
      Res.append("@param  times -    the number of time channels");
      Res.append(" @return  Transmission data set");
      Res.append("@assumptions The data set was written by the Write Flood ");
      Res.append("  operator or in that specific format. The Transmission y ");
      Res.append("  are all written, then the errors,");
      Res.append(" The Efficiency values are written E15.5 ");
      return Res.toString();
   }
}//ReadTransmission
