/*
 * File:  CalibSCD.java
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2003/07/22 15:21:59  dennis
 * Replaced local value for h/mn as about (.395..) with "standard"
 * value from tof_calc.
 *
 * Revision 1.1  2003/07/21 22:09:57  rmikk
 * Initial checkin
 *
 *
 */

import DataSetTools.functions.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.util.*;
import DataSetTools.math.*;
import java.lang.Math.*;
import java.util.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.dataset.*;
import java.io.*;

public class CalibSCD extends OneVarParameterizedFunction
  {
                                             // more accurate than .3955974
   public static final double H_OVER_MN = tof_calc.ANGST_PER_US_PER_MM / 10;  
   
   String PeaksFileName , CalibFileName;
   int[] Seq;
   XScale xscale = null;
   int detNum;
   double[] calib , calib1;
   //float[][]xyt , chiphiom;
   double [][] hkl , Qxyz , M , InvM , U1 , InvShkl , SQxyz , MU;
   double[][] InvMU; 
   double[] zero ={0.0 , 0.0 , 0.0};
   int[] runNum;
   int npeaks;
   Vector Peaks;
   double[] times;
   double[] paramValues;
   String[] ParamNames = {"ax" , "bx" , "ay" , "by" , "Tzero" , "L1"} ;
   int[] peakNums;
   public CalibSCD( String PeaksFileName ,  String SeqNums ,
               String CalibFileName , int linenum , int detNum )
      { 
       super( "Peak errors" , new double[6] , new String[6] );
       this.PeaksFileName = PeaksFileName;
       this.CalibFileName = CalibFileName;
       this.detNum = detNum;
       //this.xscale = xscale;
       parameters = new double[6];;
       if( SeqNums == null )
         Seq = null;
       else
         Seq = IntList.ToArray( SeqNums.toString() );

       Object X;
       try{
         X = LoadSCDCalib.readCalib( new TextFileReader( CalibFileName ) ,
                                           false , linenum );
           }
       catch( Exception s3 )
          {
           PeaksFileName = null;
           SharedData.addmsg( "Improper Calibration File:" + s3 );
           return;
 
           }
 
       if( X instanceof ErrorString )
          {PeaksFileName = null;
           SharedData.addmsg( "Improper Calibration File:" + X.toString() );
           return;
           }
       else if( !( X instanceof Object[] ) )
         {PeaksFileName = null;
           SharedData.addmsg( "Improper Reading of Calibration File:" + X.toString() );
           return;
           }     
       Object[] Xo = ( Object[] ) X;
       calib = LinearAlgebra.float2double(( float[] )Xo[0]);

       calib1 = new double[5];
       java.lang.System.arraycopy( calib , 4 , calib1 , 0 , 5 );
       paramValues = new double[6];
       paramValues[0] = ( double ) calib[5];    
       paramValues[1] = ( double ) calib[7];
       paramValues[2] = ( double ) calib[6];
       paramValues[3] = ( double )calib[8];
 
       paramValues[4] = ( double ) calib[4];//Tzero
  
       paramValues[5] = ( double ) calib[3]/100 ; //L1
  
       ReadPeaks readPksOp = new ReadPeaks( PeaksFileName );
       Object X1 = readPksOp.getResult();
       if( ( X1 == null ) || ( X1 instanceof ErrorString ) || !( X1 instanceof Vector ) )
         {PeaksFileName = null;
           String S = "";
           if( X1 != null ) S = ( (ErrorString)X1 ).toString();
           SharedData.addmsg( "Improper PeaksFile:" + X1 );
           return;
             }              
       npeaks = 0;
       System.out.println( "calib=" );
       LinearAlgebra.print( calib1 );

       Peaks = ( Vector ) X1;
       for( int i = 0 ; i < Peaks.size(); i++ )
         {
            Peak pk = (Peak)( Peaks.elementAt( i ) );
            pk.L1( (float)(calib[3]/100.0) );
           
            //pk.calib( calib1 );
            //pk.time(xscale );
            
            //if ( !isSeqNum(pk.seqnum() , Seq ) )
            // {}
            //else if( (detNum >= 0 ) && (pk.detnum() != detNum))
             // {}
            //else 
            if( (isSeqNum( pk.seqnum() , Seq ) ) && ( ( detNum <0)  || (pk.detnum() == detNum ) )  )
            if( pk.h() != 0 )
               npeaks++ ;
            else if( pk.k() != 0 )
               npeaks ++ ;
            else if( pk.l() != 0 )
               npeaks++ ;
  
         }
       hkl = new double[npeaks][3];
       peakNums = new int[ npeaks];
       double[][] Shkl = new double[3][3];
       SQxyz = new double[3][3];
       Zero( Shkl );
       times = new double[npeaks];
       int k = 0;
       Qxyz = new double[npeaks][3];
       for( int i = 0 ; i < Peaks.size() ; i++ )
          {
           Peak pk = (Peak)( Peaks.elementAt( i ) );
            
           if( (isSeqNum( pk.seqnum() , Seq ) ) && ( ( detNum <0)  || (pk.detnum() == detNum ) )  )
               if( (pk.h() != 0) ||(pk.l() != 0) ||(pk.k() != 0) )
                  {
                   hkl[k][0] = (double) pk.h();
                   hkl[k][1] = (double)  pk.k();
                   hkl[k][2] = (double) pk.l();
                   peakNums[k] = i;
                   double L = getL1()* 100.0 +  Math.sqrt( get_xcm( pk )* get_xcm( pk )+
                               get_ycm( pk )* get_ycm( pk ) + pk.detD()* pk.detD() );
                 
                   times[k] = ( L* pk.wl( )/H_OVER_MN - getTzero( ));
                   k++;
                  }
          }
       for( int i = 0 ; i < 3 ; i++ )
           for( int j= 0 ; j < 3 ; j++ )
               for( int l = 0 ; l <  npeaks ; l++ )
                   Shkl[i][j] += hkl[l][i]* hkl[l][j];
    
       InvShkl = LinearAlgebra.getInverse( Shkl );
       System.out.println( "#peaks=" + npeaks );
       parameters = paramValues;
       parameter_names = ParamNames;
       if( InvShkl == null)
          return;
       calcQs();
 
      }//Constructor
 
   private void Zero( double[][] U )
      {
       for( int i = 0 ; i < U.length ;  i++ )
         Arrays.fill( U[i] , 0.0 );

      } 

   private void calcQs1()
      {
       paramValues = parameters;
       calib1[0] = getTzero();
       calib1[1] = getax();
       calib1[2] = getay();
       calib1[3] = getbx();
       calib1[4] = getby();
       System.out.println( "calibin calcQs=" );
       LinearAlgebra.print( calib1 );
       int k = 0;
       for( int i= 0 ; i < Peaks.size() ; i++ )
         {  
            Peak pk = ( Peak )( Peaks.elementAt( i ) );
            pk.L1( (float)getL1() );
            //pk.calib( calib1 );
            //if( isSeqNum( pk.seqnum() , Seq ) )
            if( (isSeqNum( pk.seqnum() , Seq ) ) && ( ( detNum <0)  || (pk.detnum() == detNum ) )  )
            if( (pk.h() != 0) ||(pk.l() != 0 ) ||(pk.k() != 0 ))
               { //pk.calib( calib1);
                if( get_wl ( pk , k ) == 0 )
                  System.out.println( "wl2=0 for " + pk.h() + "," + pk.k() + "," + pk.l());
                Qxyz[k] = pk.getUnrotQ();
                k++ ;
               }
          }
       FinishCalcQs();
      }
  

   private void FinishCalcQs(){
       
       Zero( SQxyz );
      
       for( int i = 0 ; i < 3 ; i++ )
         for( int k = 0 ; k < 3 ; k++ )
           for( int l = 0 ; l < npeaks ; l++ )
             SQxyz[i][k] += Qxyz[l][i]* hkl[l][k];

       M = new double[3][3];
    

       M = LinearAlgebra.mult( SQxyz , InvShkl );//this is transpose of reg UB
       if( M == null)
          return;
       MU = M;  //in case cannot determine theoretical matrix.  return here

       InvM = LinearAlgebra.getInverse( M );
       double [][] G = LinearAlgebra.mult( Transpose( M ) , M );
       double a = Math.sqrt( G[0][0] ) , b = Math.sqrt( G[1][1] ) , c = Math.sqrt( G[2][2] );
       double[][] T1 = getBaseMatrix( a , b , c , G[1][2]/b/c ,  G[0][2]/a/c , G[0][1]/a/b );
  
       double[][]T2A = getBaseMatrix( 4.9138 , 4.9138 , 5.4051 , 0.0 , 0.0 , -.5 );
       double[][] T2 = LinearAlgebra.mult( T2A , Transpose( T2A ) );

       T2 = LinearAlgebra.getInverse( T2 );
       a = Math.sqrt( T2[0][0] ); b = Math.sqrt( T2[1][1] ); c = Math.sqrt( T2[2][2] );
       T2 = getBaseMatrix( a , b , c , T2[1][2]/b/c , T2[0][2]/a/c , T2[0][1]/a/b );
       //System.out.println("T2=");
       //LinearAlgebra.print( T2);
       // Now get U so Uh=q
       //    System.out.println( "M= at end" );
       //   LinearAlgebra.print( M );
       double[][] F = LinearAlgebra.mult( LinearAlgebra.getInverse( T1 ),Transpose( M ) );
       MU = LinearAlgebra.mult(T2,F );
       MU = Transpose( MU);
       //System.out.println( "M,MU=");
       //LinearAlgebra.print( Transpose(M));
       //LinearAlgebra.print( MU);
      // MU = Transpose( MU );
         InvMU = LinearAlgebra.getInverse( MU );
      }


   double[][] Transpose( double[][] X )
      {
       double[][] Res = new double[3][3];
       for( int i = 0 ;i  < 3 ; i++ )
           for( int j = 0 ; j < 3 ; j++ )
               Res[i][j] = X[j][i];
          
       return Res;
      }

   private void calcQs()
      {
       if( InvShkl == null)
          return;
       paramValues = parameters;
       if( Qxyz == null )
          Qxyz = new double[npeaks][3];
       int k = 0;
       for( int i= 0 ; i < Peaks.size() ; i++ )
          {  
           Peak pk = ( Peak )( Peaks.elementAt( i ) );
         
           //pk.calib( calib1 );
           if( (isSeqNum( pk.seqnum() , Seq ) ) && ( ( detNum <0)  || (pk.detnum() == detNum ) )  )
           //if( isSeqNum( pk.seqnum() , Seq ) )
               if( (pk.h() != 0) ||(pk.l() != 0) ||(pk.k() != 0) )
                  {
                   double wl = get_wl ( pk , k );
  
                   double R1 =(double)( get_xcm( pk )* get_xcm( pk ) + get_ycm( pk )* get_ycm( pk )+
                                     pk.detD()* pk.detD() );
                   R1 = java.lang.Math.sqrt( R1 );
                   double L = ( getL1()  +  java.lang.Math.sqrt( R1 ) );
              
                   double mult = java.lang.Math.PI/180;
            

                   double zd = get_ycm( pk );
                   double angle = pk.detA()* mult;
                   double  xd = pk.detD()* Math.cos( angle ) +
                                                       get_xcm( pk )* Math.sin( angle );
                   double yd = pk.detD()* Math.sin( angle ) -
                                                get_xcm( pk )* Math.cos( angle );
                   R1 = R1* wl;
                   xd = xd/R1 - 1.0/wl;
                   yd = yd/R1;
                   zd = zd/R1;
             
                   double[] DD = new double[2];                   
                   angle = -pk.omega()/180.0* Math.PI;
             
                   double CA = Math.cos( angle );
                   double SA = Math.sin( angle );
                   double xd1 =  xd* CA  + yd* SA;
                   double yd1 = -xd* SA  + yd* CA;
  
                   angle = pk.chi()/180.0* Math.PI;
              
                   CA = Math.cos( angle );
                   SA = Math.sin( angle );
                   double yd2 = yd1* CA  + zd* SA;
                   double zd2 = -yd1* SA + zd* CA;

                   angle = pk.phi()/180.0* Math.PI;
                
                   CA = Math.cos( angle );
                   SA = Math.sin( angle );
                   xd = xd1* CA  + yd2* SA;
                   yd = -xd1* SA + yd2* CA;
               
                   Qxyz[k][0] = xd ;
                   Qxyz[k][1] = yd;
                   Qxyz[k][2] = zd2;
             
  
                   k++ ;
                  }
          }

       FinishCalcQs();
      }

   private boolean isSeqNum( int num , int[] list )
      {
       if( list == null )
           return true;
       int i = Arrays.binarySearch( list , num );
       if( i < 0 )
           return false;
       else
           return true;

      }


   public double[] getParameters()
     {//parameters = new double[0];;
      
       return paramValues;
      }

   public int numParameters()
      {
       return ParamNames.length;
      }

   public void setParameters( double[] params )
      {
       super.setParameters( params );
       paramValues = params;
       //parameters = new double[0];;
       calcQs();
      
      }

   public void showOrientation()
      {
       System.out.println( "Orientation" );
       LinearAlgebra.print( M );
       double[][]G = LinearAlgebra.getInverse( LinearAlgebra.mult(Transpose( M ) , M ) );
       System.out.println( "\n Tensor Matrix in real space" );
       LinearAlgebra.print( G );
       double a = Math.sqrt( G[0][0] ) ,
              b = Math.sqrt( G[1][1] ) ,
              c = Math.sqrt( G[2][2] );
       System.out.println( "\nLattice Parameters:" + a + "    " + b + "     " + c + "    " +  Math.acos( G[1][2]/b/c )* 180/Math.PI + "   "
                     + Math.acos( G[0][2]/a/c )* 180/Math.PI + "   "+ Math.acos( G[1][0]/b/a )* 180/Math.PI );


       

   }
   public void show1( String S) throws IOException{
     //if( 3==3)
     //   throw new IOException();
    
     int[] list = IntList.ToArray( S);
 
     if( list == null)
        throw new IOException();
     if( list.length < 1)
        throw  new IOException();
     char c=0;
     while( c != 'z'){
       System.out.println("now enter letters xywqQrhklpe or z to quit");
       try{
          c = (char)System.in.read();
          while( c < ' ')
               c = (char)System.in.read();
          if( c =='z')
             return;
          if( "xywqQrhklpe".indexOf(c) < 0)
             return;
           }
       catch( Exception ss){ 
           throw new IOException();}

       if( "xywqQrhklp".indexOf(c) >=0)
          System.out.println("Theoretical"+"\t"+"Observed");
       if( c=='q')
          System.out.println("seq"+"\t"+"qx"+"\t"+"qx");
       else if( c == 'Q')
          System.out.println("seq"+"\t"+"qy"+"\t"+"qy");
       else if( c == 'r')
          System.out.println("seq"+"\t"+"qz"+"\t"+"qz");

       else if( c == 'h')
          System.out.println("seq"+"\t"+"h"+"\t"+"h");
      else if( c == 'k')
          System.out.println("seq"+"\t"+"k"+"\t"+"k");
      else if( c == 'l')
          System.out.println("seq"+"\t"+"l"+"\t"+"l");

       else if( c == 'x')
          System.out.println("seq"+" "+"xcm"+" "+"x"+" "+"xcm-obs  pk-xcm");
       else if( c == 'y')
          System.out.println("seq"+"\t"+"\t"+"ycm"+"y"+"\t"+"\t"+"ycm       ");
       else if( c == 'w')
          System.out.println("seq"+"\t"+"wl"+"\t"+"t"+"\t"+"t_chan"+"\t"+"wl              ");
       else if( c == 'e')
          System.out.println("seq"+"\t"+"ax"+"\t"+"bx"+"\t"+"ay"+"\t"+"by"+"\t"+"t0"+"\t"+"L");
       else if( c == 'p')
          System.out.println("ax  bx  ay  by  tzero   L1");
    
       for( int i=0; i < list.length; i++){
        int k = list[i];
        if(( k < 0) || ( k >= npeaks) )
           continue;
        
        Peak pk = (Peak)(Peaks.elementAt( peakNums[ k] ));
       
       if( c == 'q'){
          double[]q = new double[3];
          int cc=0;
             q[cc] = MU[cc][0]*hkl[k][0]+MU[cc][1]*hkl[k][1]+MU[cc][2]*hkl[k][2];
          System.out.println(  pk.seqnum() +"\t"+q[cc]+ "\t"+ Qxyz[k][cc]);
       }
      else if( c == 'Q'){
          double[]q = new double[3];
          int cc=1;
             q[cc] = MU[cc][0]*hkl[k][0]+MU[cc][1]*hkl[k][1]+MU[cc][2]*hkl[k][2];
           System.out.println(  pk.seqnum() +"\t"+q[cc]+ "\t"+ Qxyz[k][cc]);
       }
      else if( c == 'r'){
          double[]q = new double[3];
          int cc=2;
             q[cc] = MU[cc][0]*hkl[k][0]+MU[cc][1]*hkl[k][1]+MU[cc][2]*hkl[k][2];
          System.out.println(  pk.seqnum() +"\t"+q[cc]+ "\t"+ Qxyz[k][cc]);
       }
       else if( c == 'h'){
         
          double[] q = new double[3];
          int cc=0;
             q[cc] = InvMU[cc][0]*Qxyz[k][0]+InvMU[cc][1]*Qxyz[k][1]+InvMU[cc][2]*Qxyz[k][2];
          System.out.println( pk.seqnum()+"\t"+hkl[k][cc]+ "\t"+q[cc]);

       }
       else if( c == 'k'){
         
          double[] q = new double[3];
          int cc=1;
             q[cc] = InvMU[cc][0]*Qxyz[k][0]+InvMU[cc][1]*Qxyz[k][1]+InvMU[cc][2]*Qxyz[k][2];
           System.out.println( pk.seqnum()+"\t"+hkl[k][cc]+ "\t"+q[cc]);

       }
       else if( c == 'l'){
         
          double[] q = new double[3];
          int cc=0;
             q[cc] = InvMU[cc][0]*Qxyz[k][0]+InvMU[cc][1]*Qxyz[k][1]+InvMU[cc][2]*Qxyz[k][2];
         System.out.println( pk.seqnum()+"\t"+hkl[k][cc]+ "\t"+q[cc]);
       }else if( c == 'p'){
           double[] Params = getParameters();
           for( int cc = 0; cc<6; cc++)
             System.out.print( Params[ cc]+"  ");
           System.out.println("");
  

       }
      else if( "xyw".indexOf(c) >=0){
       //get xcm,ycm,wl from theor q's
       double[]q = new double[3];
       for( int cc=0; cc<3;cc++)
          q[cc] = MU[cc][0]*hkl[k][0]+MU[cc][1]*hkl[k][1]+MU[cc][2]*hkl[k][2];
       double[]xxx = getxcm( q, k, pk);
       if( c == 'x')
         System.out.println( pk.seqnum()+" "+xxx[0]+" "+pk.x()+" "+ get_xcm(pk)+" "+pk.xcm() );
      
       else if( c== 'y')
         System.out.println( pk.seqnum()+"\t"+xxx[1]+"\t"+pk.y()+"\t"+ get_ycm(pk) );
       else if( c=='w')
         System.out.println( pk.seqnum()+"\t"+xxx[2]+"\t"+times[k]+"\t"+ pk.z()+"\t"+
                                     pk.wl()  );
      }
     else if( c=='e'){
         for(  int kk=0;kk< numParameters(); kk++)
            System.out.print( DataSetTools.util.Format.real(get_dFdai( (double)k, kk),11)+"  ");
         System.out.println("");
     }
    }//end for
    }//while c !='z'

    throw new IOException();
   }

   private double[] getxcm( double[] q, int peak, Peak pk){
      double angle = -pk.phi()/180.0* Math.PI;
                
      double CA = Math.cos( angle );
      double SA = Math.sin( angle );
      double xd1 = q[0]* CA  + q[1]* SA;
      double yd2 = -q[0]* SA + q[1]* CA;

      angle = -pk.chi()/180.0* Math.PI;
              
      CA = Math.cos( angle );
      SA = Math.sin( angle );
      double yd1 = yd2* CA  + q[2]* SA;
      double zd = -yd2* SA + q[2]* CA;

     angle = pk.omega()/180.0* Math.PI;
            
     CA = Math.cos( angle );
     SA = Math.sin( angle );
     double xd =  xd1* CA  + yd1* SA;
     double yd = -xd1* SA  + yd1* CA;

     
     //System.out.println("Q rot ="+ xd+","+yd+","+zd+","+q[0]+","+q[1]+","+q[2]);
    

     //xd,yd,zd
     double wl = 2*xd/(xd*xd+yd*yd+zd*zd);
     if(wl < 0) wl = -wl;

    // System.out.println("    lengths&wl="+Math.sqrt(xd*xd+yd*yd+zd*zd)  +
     //                              ","+Math.sqrt( q[0]*q[0]+q[1]*q[1]+q[2]*q[2])+","+ wl+";"+
     //                        (1/wl));
     xd = xd +1/wl;

     // Now (xd,yd,zd) points in direction of beam
     //  Project on unit Vector in direction of DetD;
    float DetD = pk.detD();
    double DetA = pk.detA() *Math.PI/180f;
    double xdp = xd*Math.cos(DetA) + yd*Math.sin(DetA);
    //System.out.println("xdp, DetD="+xdp+","+DetD);
    //scale (xd,yd,zd) so projection length is DetD, so scattering vector has correct length
    xd = xd/xdp*DetD;
    yd = yd/xdp*DetD;
    zd = zd/xdp*DetD;
    //System.out.println("   new scatt vect="+xd+","+yd+","+zd);
    //xcm is now the projection of this vector on 
    //       line perpendicular to DetD(DetA -angle 90deg )
    double xcm= xd*Math.sin(DetA) -yd*Math.cos(DetA);//=xd*cos(A-90)+yd*sin(A-90)
    double ycm = zd;
    double[] Res = new double[3];
    Res[0] = xcm;
    Res[1] = ycm;
    Res[2] = wl;
     
    return Res;
   }
   public double getValue( double x )
      {
       if( InvShkl == null)
           return 0.0;
       double error = 0;
       calcQs();
       int  peak = (int) x;
       if( peak < 0 )
           return 0.0;
       if( peak >= npeaks )
           return 0.0;
       double [] q_est = new double[3];
       String S = "";
       for( int i = 0 ; i < 3 ; i++ )
          {
           double R =0;
           R = MU[i][0]* hkl[peak][0];
           R += MU[i][1]* hkl[peak][1];
           R +=MU[i][2]* hkl[peak][2];
           S +=R+ ",";
           R = R - Qxyz[peak][i];
           R = R* R;
           error = error + R;
          // error = error + (M[i][0]* hkl[peak][0] + M[i][1]* hkl[peak][1] + M[i][2]* hkl[peak][2] -
                   //  Qxyz[peak][i])^2.0;
         }
       return java.lang.Math.sqrt( error );
      }

   private double getax(){
       return paramValues[0];
   }

   private double getbx(){
      return paramValues[1];
   }

   private double getay(){
      return paramValues[2];
   }

   private double getby(){
      return paramValues[3];
   }

   private double getTzero(){
      return paramValues[4];//calib[4];  
   }

   private double getL1(){
      double x = paramValues[5];
      return paramValues[5];//calib[3]/100;
   }

   private double getDetD(){
      return calib[2];
   }

   private double getDetA(){
      return calib[1];
   }

   private double getDet(){
      return calib[0];
   }

   private double get_xcm( Peak pk ){
       return getax()* ( pk.x() - .5 ) + getbx();
   }

   private double get_ycm( Peak pk ){
       return  getay()* ( pk.y()-.5 ) + getby();
   }  
  
   private double get_wl( Peak pk , int k ){

       double L = getL1()* 100.0 +  Math.sqrt( get_xcm( pk )* get_xcm( pk ) + get_ycm( pk )* get_ycm( pk ) + 
                              pk.detD()* pk.detD());
     
       return  (times[k] + getTzero() )* H_OVER_MN/L;
   }     
 

   double[][] getBaseMatrix( double a , double b , double c , double cosa , 
               double cosb , double cosc )
      {
       cosa = Math.min( 1.0 , Math.max( -1.0 , cosa ) );
       cosb = Math.min( 1.0 , Math.max( -1.0 , cosb ) );
       cosc = Math.min( 1.0 , Math.max( -1.0 , cosc ) );
     
       double[][] Res = new double[3][3];
       Res[0][2] = Res[1][0] = Res[1][2] = 0;
       Res[1][1] = b;
       Res[0][1] = a* cosc;
       Res[0][0] = a *  Math.sin( Math.acos( cosc ) );
       Res[2][1] = c* cosa;
       Res[2][0] =( a* c* cosb - Res[0][1]* Res[2][1] )/Res[0][0];
       Res[2][2] = Math.sqrt( c* c - Res[2][0]* Res[2][0] - Res[2][1]* Res[2][1] );
       return Res;
      } 

   public static void showUsage()
      {
       System.out.println("------------------ USAGE -------------------------");
       System.out.println( "The arguments to this program are" );
       System.out.println("     1. The peaks filename" );
       System.out.println("     2. The calibration filename" );
       System.out.println("     3. The line number to use from the calibration file" );
       System.out.println("     4.(optional) The detector number. Omitted or -1 uses all detectors" );
       System.out.println("     5. (optional)Sequence numbers in standard format(no quotes) or \"\" for all" );
       System.out.println("--------------------------------------------------");
      }

   public static void main( String args[] )
      {
       //DataSet[] DS = ( new IsawGUI.Util( ) ).
       //       loadRunfile( "C:/Ruth/ISAW/SampleRuns/scd07906.run" );
       //int k = DS.length - 1;
       //XScale xscl = DS[k].getData_entry( 0 ).getX_scale();
       if( args == null)
         { 
           CalibSCD.showUsage();
           System.exit( -1 );
          }
       if( args.length < 3 )
         { 
           CalibSCD.showUsage();
           System.exit( -1 );
          }
       String peaksFilename = args[0];//"quartz.peaks";
       String instParamFileName =args[1];// "instprmN.dat";
       int lineNum = -1;
       try{
           lineNum = ( new Integer( args[2] ) ).intValue();//5
          }
       catch( Exception sss)
          {
           System.out.println(" Incorrect line number for calibration file");
           System.exit( -1 );
          }
      
       int detNum = -1;
       if( args.length >3)
           try{
               detNum = (new Integer( args[3] ) ).intValue();
           }
           catch( Exception ssss)
           {
             detNum = -1;
           }
       String SeqNums = null;
       if( args.length > 4)
           SeqNums = args[4];
       if( SeqNums != null)
       if( SeqNums.trim().length() <1)
           SeqNums = null;
       else if( SeqNums.trim().equals( "\"\"") )
           SeqNums = null;

       CalibSCD F = new CalibSCD( peaksFilename ,  SeqNums , instParamFileName , lineNum , detNum );
       double E = 0;
      
       while( true )
          {
           
           char c = 0;
           try{

              while( 4 == 4){
               System.out.println(" Enter \"YES\" to continue or subrange"+c);
               String S ="";
               c=0;
               while( c <= 32 )
                   c = (char)System.in.read();
     
               while( c > 32 )
                  {
                   S +=c;
                   c = (char)System.in.read();     
                  }
    
               F.show1(S);
               }
              }
           catch( Exception ss )
              {System.out.println("Starting Marquardt");
               int nn = F.numParameters();
               
               double[] x = new double[F.npeaks];
               double[] y = new double[F.npeaks];
               Arrays.fill( y ,  0.0 );
               for( int i =0 ; i < F.npeaks ; i++ ) 
                   x[i] =( double) (i );
               double[] sigma = new double[F.npeaks];
               Arrays.fill( sigma ,  1.0 );
               //sigma[5] =10.0;
               for( int ii =0 ; ii < 1 ; ii++ )
                  {
                   MarquardtArrayFitter  Fit = 
                     new MarquardtArrayFitter( F, x, y, sigma, 1E-9, 30 );
                   double[] params = F.getParameters();
                   String[] ParamNames = F.getParameterNames();
                   double[] sigmas  = Fit.getParameterSigmas();
                   double[] sigmas2 = Fit.getParameterSigmas_2();
                   System.out.println("------------------- Parameters -------------------------");
                   System.out.println("  Name      Value +/-  Error ");
                   for( int m = 0 ; m < F.numParameters() ; m++ )
                       System.out.println("  "+ParamNames[m]+"    "+params[m]+ " +/- " + sigmas[m] +
                                          " +/- " + sigmas2[m]   );
             
                   //System.out.println( "Parameters" + LinearAlgebra.print(F.getParameters()) );
                   System.out.println( "ChiSqr=" + Fit.getChiSqr() );
                   System.out.println("\n        --------  Orientation --------------");
                   F.showOrientation();
                   E = 0;
                   for( int i =0 ; i < F.npeaks ;  i++ )
                       E += F.getValue( (double)i )* F.getValue( (double)i );
                   System.out.println( "Errors=" + E );
                   
                   //System.exit( 0 );
                  }
              }
          }//while true 
       }//main
  }
