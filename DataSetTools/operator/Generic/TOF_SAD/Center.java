
/*
 * File:  Center.java
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.5  2004/01/09 00:00:38  rmikk
 * Eliminated debug printing
 * Changed results so they can be used by ISAW scripts
 *
 * Revision 1.4  2003/10/03 16:39:00  rmikk
 * -Fixed it so that now it gets the same numbers as the Vax
 * -Added documentation for the two new arguments to Center
 *
 * Revision 1.3  2003/09/27 13:29:12  rmikk
 * Added 2 new parameters, Xdim and Ydim, and a variable useOldCode
 *
 *
 * Revision 1.2  2003/09/26 15:39:10  rmikk
 * Fixed Negative Xoff set problem
 * Used zero for the weights on row and column centers
 *    that are near the centeer

 */

package DataSetTools.operator.Generic.TOF_SAD;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import java.util.*;
import Command.*;
import DataSetTools.util.*;

/**
*     This Class Finds the position of the Center of the Beam in a sand
*     instrument.
*/
public class Center extends GenericTOF_SAD{

  float BS = 1.5f/100.0f;
  float XMAX = 3.5f/100.0f;//length in cm of square around center to use
  public static boolean useOldCode = true;
 
  public Center(){
    super( "Beam Center");
    setDefaultParameters();
  }

  /**
  *    Constructor for the this operator
  *    @param DS  The DataSet used to determine the center
  *    @param SensDS  The sensitivity Data Set.
  *    @param StartTimeChannel  The starting time channel to use
  *    @param  EndTimeChannel   The ending time channel to use
  *    @param  Xoff    Initial guess for X offset of the beam from Center
  *    @param  Yoff   Initial guess for the Y offset of the beam from Center
  *    @param  Xdim    The width of the area detector
  *    @param  Ydim   The height of the area detector
  */
  public Center( DataSet DS, DataSet SensDS, int StartTimeChannel,
        int EndTimeChannel, float Xoff, float Yoff, float Xdim, float Ydim){
     this();
     parameters = new Vector();
     addParameter( new DataSetPG( "Data Set", DS));
     addParameter( new DataSetPG( "Sensitivity Data Set", SensDS));
     addParameter( new IntegerPG("Start time Channel to use",
             new Integer( StartTimeChannel)));
     addParameter( new IntegerPG("Last time Channel to use",
             new Integer( StartTimeChannel)));
     addParameter( new FloatPG("Guess for X offset(cm) of beam",
             new Float( Xoff)));
     addParameter( new FloatPG("Guess for Y offset(cm) of beam",
             new Float( Yoff)));
     addParameter( new FloatPG("X dimension of detector",
             new Float( Xdim)));
     addParameter( new FloatPG(" dimension of detector",
             new Float( Ydim)));
  }

 public void setDefaultParameters(){
     parameters = new Vector();
     addParameter( new DataSetPG( "Data Set", null));
     addParameter( new DataSetPG( "Sensitivity Data Set", null));
     addParameter( new IntegerPG("Start time Channel to use",
             new Integer( -1)));
     addParameter( new IntegerPG("Last time Channel to use",
             new Integer( -1)));
     addParameter( new FloatPG("Guess for X offset(cm) of beam",
             new Float( 0.0f)));
     addParameter( new FloatPG("Guess for Y offset(cm) of beam",
             new Float( 0.0f)));
     addParameter( new FloatPG("X dimension of detector",
             new Float( -1f)));
     addParameter( new FloatPG(" dimension of detector",
             new Float( -1f)));


 }
  /**
  *    Finds the center of the beam as follows:<BR>
  *    1. For each row, the weighted center is found, eliminating data near
  *       the center of the beamstop. Weights are the counts
  *    2. The weighted average of the row centers is then taken using interpolation
  *       of the counts close to the center for the weight.
  *    3. This yields the X offset. In a similar manner the Y offset is obtained
  *       by using the weighted Centers for each column
  */
  public Object getResult(){
     DataSet DS = ((DataSetPG)getParameter(0)).getDataSetValue();
     DataSet SensDS = ((DataSetPG)getParameter(1)).getDataSetValue();
     int StartTimeChan = ((IntegerPG)getParameter(2)).getintValue();
     int EndTimeChan = ((IntegerPG)getParameter(3)).getintValue();
     float Xoff = ((FloatPG)getParameter(4)).getfloatValue()/100.0f;
     float Yoff = ((FloatPG)getParameter(5)).getfloatValue()/100.0f;
     float Xdim = ((FloatPG)getParameter(6)).getfloatValue()/100.0f;
     float Ydim = ((FloatPG)getParameter(7)).getfloatValue()/100.0f;

     //------------- Set up Grids ---------------------------------
    int[] GridIDs = Grid_util.getAreaGridIDs( DS);
     if( GridIDs == null)
        return new ErrorString("No Data Set Grids set up for "+ DS.toString());
     if( GridIDs.length < 1)
        return new ErrorString("No Data Set Grids set up for "+ DS.toString());
     

     UniformGrid DSGrid =(UniformGrid)Grid_util.getAreaGrid( DS, GridIDs[0]);
     DSGrid.setDataEntriesInAllGrids( DS);

     GridIDs = Grid_util.getAreaGridIDs(SensDS);
     if( GridIDs == null)
        return new ErrorString("No Sensitivity Data Set Grids set up for "+ SensDS.toString());
     if( GridIDs.length < 1)
        return new ErrorString("No Sensitivity Data Set Grids set up for "+ SensDS.toString());
     UniformGrid SensGrid =(UniformGrid)Grid_util.getAreaGrid( SensDS,GridIDs[0]);
     SensGrid.setDataEntriesInAllGrids( SensDS);
     StartTimeChan--; EndTimeChan --;

     if( StartTimeChan < 0){
       StartTimeChan =0;
       EndTimeChan = DS.getData_entry(0).getX_scale().getNum_x()-1;
     }else if( EndTimeChan >DS.getData_entry(0).getX_scale().getNum_x()-1)
       EndTimeChan = DS.getData_entry(0).getX_scale().getNum_x()-1;
      
     float[][] CollapsedData = new float[DSGrid.num_rows()+1][DSGrid.num_cols()+1];
     float TotCount = 0.0f;
     for( int row =1; row <= DSGrid.num_rows(); row++)
      for( int col = 1; col <= DSGrid.num_cols(); col++){
          CollapsedData[row][col] = 0.0f;
          float[] Dyvals = DSGrid.getData_entry(row,col).getY_values();
          float sens =SensGrid.getData_entry(row,col).getY_values()[0];
          for( int chan = StartTimeChan; chan <= EndTimeChan; chan++){
             TotCount += Dyvals[chan];
             if( sens !=0)
              CollapsedData[row][col] += Dyvals[chan]/sens;
           
          }
     }
         

    float SavXoff,SavYoff;
    float[] Rowcm = new float[ DSGrid.num_rows()],
            Colcm = new float[DSGrid.num_cols()];
   
    for( int i = 0; i< DSGrid.num_rows(); i++)
        if( Ydim <=0)
          Rowcm[i] = DSGrid.y( i+1.0f, 2.0f);
        else
          Rowcm[i] = -Ydim/2 + Ydim/DSGrid.num_rows()*( i+.5f);
   
    

    for( int j = 0; j< DSGrid.num_cols(); j++)
        if( Xdim <= 0)
          Colcm[j] = DSGrid.x(  2.0f,j+1.0f);
        else
          Colcm[j] = -Xdim/2 + Xdim/DSGrid.num_cols()*( j+.5f);

    
    float[] RowCenters = new float[ DSGrid.num_rows()];
    float[] ColCenters = new float[ DSGrid.num_cols()];
    float[] RowCentWts = new float[ DSGrid.num_rows()];
    float[] ColCentWts = new float[ DSGrid.num_cols()];
    int row,col;
    int rstart,rend,cstart,cend;
    int count = 0;
    boolean done = false;
    float Cx=0,Cwx=0,Cy=0;
    System.out.println("   Xoff\t\t   Yoff");
    while( !done){
      
       SavXoff=Xoff;
       SavYoff = Yoff;
       rstart = Findind( Rowcm, -XMAX + Yoff, true)+1; 
       rend = Findind( Rowcm, XMAX + Yoff,false)+1; 
       cstart = Findind( Colcm, -XMAX + Xoff,true)+1; 
       cend = Findind( Colcm, XMAX + Xoff,false)+1; 
       Arrays.fill( RowCenters,0.0f);
       Arrays.fill( ColCenters,0.0f);
       Arrays.fill( RowCentWts ,0.0f);
       Arrays.fill( ColCentWts ,0.0f);
       for( row = rstart; row <= rend; row++)
         for( col = cstart; col <= cend; col++)
           if( (Rowcm[row-1]- Yoff)*(Rowcm[row-1]-Yoff)+
                 (Colcm[col-1]-Xoff)*(Colcm[col-1]-Xoff) >= BS*BS){
              ColCenters[col-1] += (Rowcm[row-1]-Yoff)*CollapsedData[row][col];
              RowCenters[row-1] += (Colcm[col-1]-Xoff)*CollapsedData[row][col];
              RowCentWts [row-1] += CollapsedData[row][col];
              ColCentWts[col-1] +=  CollapsedData[row][col];
              Cx+=(Colcm[col-1]-Xoff)*CollapsedData[row][col];
              Cy +=(Rowcm[row-1]-Yoff)*CollapsedData[row][col];
              Cwx +=CollapsedData[row][col];
           }
       
     
       //for(col = cstart;col<=cend; col++)System.out.print( ColCenters[col-1]+" ");

       for( row = rstart; row <=rend; row++)
         if( RowCentWts[row-1] > 1E-5)
           RowCenters[row-1] = RowCenters[row-1]/RowCentWts[row-1];
         else
           RowCenters[row -1] = 0.0f;

       for( col = cstart; col <=cend; col++)
         if( ColCentWts[col-1] > 1E-5)
           ColCenters[col-1] = ColCenters[col-1]/ColCentWts[col-1];
         else
           ColCenters[col-1] = 0.0f;
 
     
     float YAve = CalcXave( CollapsedData,Rowcm, ColCenters,cstart,cend, Yoff,
                   Xoff,  Colcm) +Yoff;
     float XAve = CalcYave (CollapsedData,Colcm, RowCenters,rstart,rend, Xoff,
                   Yoff, Rowcm)+Xoff;
    
     //System.out.println(XAve+"\t"+YAve+"\t"+(Cx/Cwx+Xoff) +"\t"+(Cy/Cwx+Yoff));
     Xoff = XAve;
     Yoff = YAve;
    System.out.println( XAve+"\t\t"+YAve);
    if( Math.abs( SavXoff-Xoff) <.000009)
       if( Math.abs( SavYoff-Yoff) <.000009) 
          done = true;
    count ++;
    if( count >= 60)//60
       done = true;
    }//While not Done

   Vector V = new Vector();
   V.addElement( new Float( Xoff*100f));
   V.addElement( new Float( Yoff*100f));
   V.addElement( CollapsedData);
   Vector V1 = new Vector();
   V1.addElement( new Float(100*Colcm[0]));
   V1.addElement( new Float(100*Colcm[Colcm.length-1]));
   V1.addElement( new Float(100*Rowcm[0]));
   V1.addElement( new Float(100*Rowcm[Rowcm.length-1]));
   V.addElement( V1);
   return V;
  }//getResult
  private float[] mult( float[]x, float v){
    if( x == null)
       return x;
    for( int i=0; i < x.length; i++)
       x[i] *=v;
    return x;
  }
  private int Findind(float[] Rowcm,float val, boolean start){
     int Res = Arrays.binarySearch( Rowcm, val);
     if( Res < 0)
          Res =-Res -1;
     if( Res < 0)
       Res =0;
     if( Res >= Rowcm.length)
       Res = Rowcm.length-1;
     return Res;
  }//Findind
 boolean debug = false;
 // Only fit CollapsedData[j][j] to XBar[j] ????
 float CalcXave( float[][] CollapsedData,float[] Rowcm, float[] ColCenters,
     int minrow, int maxrow, float Yoff, float Xoff, float[] Colcm){
     float SUXB = 0, SUX = 0;
     //System.out.println("CalcxAve--yoffset");
     for( int j = minrow; j<= maxrow; j++){
       int IPrim = Findind( Rowcm, ColCenters[j-1]+Yoff,true) -1 +1;
       if( IPrim < 1)
          IPrim = 1;
       if( debug)
       System.out.print("J"+j+"\t"+
              (ColCenters[j-1]+Yoff));
       float v2 =CollapsedData[ IPrim+1][j];
       float v1 =CollapsedData[ IPrim][j];
       if( (Rowcm[IPrim]-Yoff)*(Rowcm[IPrim]-Yoff)+
                 (Colcm[j-1]-Xoff)*(Colcm[j-1]-Xoff) < BS*BS)
         v2 =0;

       if( (Rowcm[IPrim-1]-Yoff)*(Rowcm[IPrim-1]-Yoff)+
                 (Colcm[j-1]-Xoff)*(Colcm[j-1]-Xoff) < BS*BS)
         v1 =0;
                
       float Delta = (v2-v1)/
                (Rowcm[IPrim] -Rowcm[IPrim-1]);
       float S;
       if(!useOldCode)
           S = v1 +Delta*(ColCenters[j-1]+Yoff -Rowcm[IPrim-1]);
       else
           S = v1+Delta*(ColCenters[j-1] -ColCenters[IPrim-1]) ;
      
       SUXB += S*ColCenters[j-1];
       SUX += S;
       //System.out.println("    "+j+","+S+","+ColCenters[j-1]);

       if( debug)
         System.out.println(CollapsedData[ IPrim][j]+"\t"+S+"\t"+
               CollapsedData[ IPrim+1][j]);
       
     }
    //System.out.println("---------Res is "+(SUXB/SUX)+"----------------");
    debug= false; 
    if( SUX > 1E-5)
      return SUXB/SUX;
    else 
      return 0.0f;

 }

 float CalcYave( float[][] CollapsedData,float[] Colcm, float[] RowCenters,
     int mincol, int maxcol, float Xoff, float Yoff,  float[] Rowcm){
   float  SUYB = 0.0f,SUY = 0.0f;
   //System.out.println("CalcYAve-coloffset");
   for( int j = mincol; j<=maxcol; j++){
       int IPrim = Findind( Colcm, RowCenters[j-1] +Xoff, true) -1+1;
       if(IPrim < 1)
          IPrim =1;
       if( debug)
       System.out.print("J"+j+"\t"+
              (RowCenters[j-1]));
       float v2 =CollapsedData[ j][IPrim+1];
       float v1 =CollapsedData[ j][IPrim];
       if( (Colcm[IPrim]-Xoff)*(Colcm[IPrim+1-1]-Xoff)+
                 (Rowcm[j-1]-Yoff)*(Rowcm[j-1]-Yoff) < BS*BS)
         v2 =0;

       if( (Colcm[IPrim-1]-Xoff)*(Colcm[IPrim-1]-Xoff)+
                 (Rowcm[j-1]-Yoff)*(Rowcm[j-1]-Yoff) < BS*BS)
         v1 =0;
       float Delta = (v2-v1)/
                 (Colcm[IPrim] -Colcm[IPrim -1]);
       float S;
      
       S =v1 +Delta*(RowCenters[j-1]+Xoff -Colcm[IPrim-1]);
      
       //ERROR??                                              RowCenters here  
       SUYB += S*RowCenters[j-1];
       SUY += S;  
       if( debug)
         System.out.println(CollapsedData[ IPrim][j]+"\t"+S+"\t"+
               CollapsedData[ IPrim+1][j]);
   }
   // System.out.println("-----Res = col-XOFF="+(SUYB/SUY)+"--------------");

   if( SUY > 1E-5)
     return SUYB/SUY;
   else
     return 0.0f;

 }

 public String getDocumentation(){
      StringBuffer Res = new StringBuffer();
      Res.append( "@overview - This Class Finds the position of the Center of ");
      Res.append( "the Beam in a sand  instrument.");
      Res.append( "@algorithm -<ol type=\"1\">  <li>For each row, the weighted ");
      Res.append( " center is found, eliminating data near the center of the ");
      Res.append( "beamstop. Weights are the counts.<li> The weighted average ");
      Res.append( "of the row centers is then taken using interpolation of the ");
      Res.append( "counts close to the center for the weight. <li> This yields ");
      Res.append( "the X offset. In a similar manner the Y offset is obtained ");
      Res.append( "by using the weighted Centers for each column");
      Res.append( "@param DS - The DataSet used to determine the center");
      Res.append(  "@param SensDS-  The sensitivity Data Set.");
      Res.append(  "@param StartTimeChannel  The starting time channel to ");
      Res.append( "use");
      Res.append( "@param  EndTimeChannel   The ending time channel to use");
      Res.append( "@param  Xoff    Initial guess for X offset of the beam from ");
      Res.append( "Center");
      Res.append( "@param  Yoff   Initial guess for the Y offset of the beam ");
      Res.append( "from Center");
      Res.append( "@param  Xdim    The width of the area detector");
      Res.append( "@param  Ydim   The height of the area detector");
      Res.append( "@return  A Vector with 5 entries.  The first is the X offset");
      Res.append( "(cm) of the beam.  The second is the Y offset(cm).  The third ");
      Res.append( "is the collapsed Data for use for a visual check using another ");
      Res.append( "operator(ViewArray).  The Final element of the returned");
      Res.append( " Vector is a Vector whose elements are minx,maxx,miny and maxy cm.");


      Res.append( "@assumptions  The pixels within 1.5cm of the Xoffset and ");
      Res.append( "Yoffset are ignored");

      Res.append( "@assumptions  The weighted Average only uses the pixels ");
      Res.append( "whose distance in the x direction from the X offset is less");
      Res.append( " than 3.5cm and likewise for the y direction");
     
     
      return Res.toString();


 }
}//Center
