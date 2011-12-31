package DataSetTools.trial;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Command.ScriptUtil;
import DataSetTools.dataset.UniformGrid;
import DataSetTools.instruments.SNS_SampleOrientation;
import DataSetTools.math.tof_calc;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new_IO;
import DataSetTools.operator.Generic.TOF_SCD.Util;


public class MakeScalarTest
{
   float a; float b; float c; float error; char Center; char type; 
   float alpha; float beta; float gamma;
   float[][] conventionalUB;
   float[][] NiggliUB;
   Random rndd= new Random( 1035);
   float[][]U;
   float[][] Rhomb2Hex ={{1f/3,-1f/3,-1f/3},{-2f/3,-1f/3,-1f/3},{1f/3,2f/3,-1f/3}};
   Random nxt;
   /**
    * Creates a Peaks Object and a UB matrix both Niggly and conventional
    * @param a    length of one side in conventional cell
    * @param b    length of 2nd side in conventional cell
    * @param c    length of 3rd side in conventional cell
    * @param error  Error in data
    * @param Center  Centering type, P,F,I,C,R,
    * @param type    Orthorhombic(O),Monoclinic(M),Hexagonal(H)
    *               Note O is tetragonal and cubic. To get centering on different faces interchange a,b,c
    *               Note for centering on M, rearrange a,b, and c
    * @param alpha   alpha  in conventional cell (degrees). 1st angle
    * @param beta  beta  in conventional cell (degrees). 2nd angle if any
    * @param gamma   gamma  in conventional cell (degrees).3rd angle if any
    */
   public MakeScalarTest( float a, float b, float c, float error, char Center, char type, 
             float alpha, float beta, float gamma)
   {
      nxt = new Random(3250);
      this.a = a; this.b = b;
      this.c = c; this.error = error; this.Center = Center;this.type = type; 
      this.alpha = alpha; this.beta = beta; this.gamma =gamma ;
      U=calcU();
      float[][]X= CalcConventionalUB( this.a,this.b,this.c,this.alpha,this.beta,this.gamma, type);
      conventionalUB =LinearAlgebra.mult( U , X );
      NiggliUB = CalcNiggliUB(this.a,this.b,this.c,this.alpha,this.beta,this.gamma, type, Center);
   }
   
   public void setNiggliUB( float[][] UB)
   {
      NiggliUB = UB;
   }

   public void setConventionalUB( float[][] UB)
   {
      conventionalUB = UB;
   }
   private float Perturb( float val, float error)
   {
      return val +nxt.nextFloat( )*error -error/2;
   }
   
   public Vector<Peak_new> getPeaks( String DetCalFileName, int NpeaksPerDetector, float monCount, 
         int runNum)
   {
     if( NiggliUB== null)
        return null;
     
     Scanner sc = null;
     try
     {
        sc = new Scanner( new java.io.File(DetCalFileName) );
     }catch(Exception s)
     {
        return null;
     }
     Hashtable<Integer,UniformGrid> Dets = null;
     float L0 = Float.NaN;
     float T0 = Float.NaN;
     try{
        float[] L0T0= Peak_new_IO.Read_L1_T0( sc );
        L0= L0T0[0];
        T0 = L0T0[1];
        Dets =Peak_new_IO.Read_Grids( sc );
     }catch( Exception ss)
     {
        return null;
     }
     //No Chi,phi,omega  assume =0
     float[] hkl= new float[3];
     Vector<Peak_new> Res = new Vector<Peak_new>();
     float MinAbsQ = Float.MAX_VALUE;
     float UBi;
     for( int ii=0; ii<3;ii++)
        for( int jj=ii; jj<3;jj++)
     { 
         UBi = NiggliUB[ii][jj];
        if( Math.abs( UBi ) < MinAbsQ && UBi !=0)
           MinAbsQ =  Math.abs(UBi );
     
     }
     float Error = error*MinAbsQ;
     for(Enumeration<UniformGrid> Values = Dets.elements( );Values.hasMoreElements( );
                                        )
     {
        UniformGrid grid =  Values.nextElement( );
        
        Arrays.fill( hkl ,0 );
        int npeaks =0;
        while( npeaks<NpeaksPerDetector && (Math.abs( hkl[0] )<20  || Math.abs( hkl[1] )<20 ||
                  Math.abs( hkl[2] )<20))
        {
          
           float[] Qs = LinearAlgebra.mult( NiggliUB , hkl );
           for( int qs=0; qs< 3;qs++)
          {
              Qs[qs] = Perturb(Qs[qs],Error);
              Qs[qs] = 2*(float)Math.PI*Qs[qs];      
          }
          
          
           
           if( Qs != null && Qs[0] < 0)
           {
          
             float Q = (float)Math.sqrt(Qs[0]*Qs[0]+Qs[1]*Qs[1]+Qs[2]*Qs[2]);
             float Angle=(float)Math.acos(  Math.abs( Qs[0]/Q ));
             float a = (float)Math.sqrt( Q*Q/2/(1-Math.cos( Math.PI-2*Angle )) );
             Qs[0] +=a;
             Vector3D V = new Vector3D( Qs);
             V.normalize( );
             float k= grid.position( ).dot( grid.z_vec( ) )/V.dot( grid.z_vec() );
          
             V.multiply( -k );
             V.add( grid.position() );
             V.multiply( -1 );
             float row = grid.row(  V.dot( grid.x_vec() ) , V.dot( grid.y_vec() ));
             float col = grid.col(  V.dot( grid.x_vec() ) , V.dot( grid.y_vec() ));
             if( row >.5 && col >.5 && row <grid.num_rows( )+.5 &&
                   col < grid.num_cols()+.5  && k > 0 )
             {
                Vector3D P = grid.position(row,col);
                //Calculate time
                float time = tof_calc.TOFofDiffractometerQ( (float)Math.acos( P.dot( new Vector3D(1f,0f,0f) )/P.length() ) , 
                                                            L0+P.length() , 
                                                             Q );
                Peak_new PP=  new Peak_new( runNum,monCount,col,row,time/100,grid, new SNS_SampleOrientation(0f,0f,0f),
                      time+T0,L0,T0 );
                PP.reflag(10);
                PP.ipkobs( 12 );
                PP.setFacility( "SNS" );
                PP.setInstrument( "TOPAZ" );
               // PP.UB( NiggliUB);
               // int x=1;
               // if( x==1)
               //    x=1;
                Res.add( PP );
                npeaks++;
             }
           }
           Next( hkl);
        }
     }
     
     return Res;
   }
   
   private float[][] calcU()
   {
      float[][] Res = new float[3][3];
      for( int i=0;i<2;i++)
         for( int j=0;j<3;j++)
            Res[i][j]= 3*rndd.nextFloat();
      Vector3D C0= new Vector3D(Res[0]);
      C0.normalize( );
      Res[0][0]=C0.getX( );
      Res[0][1]=C0.getY( );
      Res[0][2]=C0.getZ( );
      
      Vector3D C2= (new Vector3D(C0));
      C2.cross( new Vector3D(Res[1]) );
      C2.normalize( );
      Res[1][0]=C2.getX( );
      Res[1][1]=C2.getY( );
      Res[1][2]=C2.getZ( );
      
      Vector3D C3=new Vector3D(C0);
      C3.cross( C2 );
      C3.normalize( );
      Res[2][0]= C3.getX( );
      Res[2][1]= C3.getY( );
      Res[2][2]= C3.getZ( );
      
      if( Math.abs( LinearAlgebra.determinant( Res )) <.1)
         return calcU();
      
    //  LinearAlgebra.print( LinearAlgebra.mult( Res , LinearAlgebra.getTranspose( Res ) ) );
      return Res;
      
   }
   public void Next( float[]hkl)
   {
      int S = (int)(Math.abs( hkl[0])+Math.abs( hkl[1])+Math.abs( hkl[2]));
      ///The sum of abs hkl's = S until not possible. Increasing lexicographically 
      if( hkl[2] < 0)
      {
         hkl[2] = -hkl[2];
         return;
      }
      if( Math.abs( hkl[0])+ Math.abs( hkl[1]+1 ) <= S)
      {
         hkl[1] +=1;
         hkl[2] = -(S -Math.abs( hkl[0])- Math.abs( hkl[1] ));
      }else if( Math.abs( hkl[0]+1 ) <= S)
      {
         hkl[0]++;
         hkl[1] = -(S - Math.abs( hkl[0]));
         hkl[2] = 0;
      }else
      {
         Arrays.fill(  hkl,0f );
         hkl[0] = -S-1;
      }
         
      
   }
   
   public float[][] getNigglyUB()
   {
      return NiggliUB;
   }
   
   public  float[][] getConventionalUB()
   {
      return conventionalUB;
   }
   
   private float[][] CalcConventionalUB(float a, float b, float c, 
         float alpha, float beta, float gamma,  char type )
   {
      float[][] Res = new float[3][3];

      Arrays.fill( Res[0] , 0 );
      Arrays.fill( Res[1] , 0 );
      Arrays.fill( Res[2] , 0 );
      if( type=='O')
      {
         Res[0][0] = 1/a;
         Res[1][1] = 1/b;
         Res[2][2] = 1/c;
         
      }else if( type=='H')//a==b
      {
         Res[0][0]= a;
         Res[1][0]= -a/2;
         Res[1][1]= a*.866f;
         Res[2][2]=c;
         Res = LinearAlgebra.getInverse( Res );
      }else //Monoclinic a,b, c at angle on a with angle alpha
      {
         if( alpha <= 90)
            return null;
        Res[0][0] = a;
        Res[1][1] = b;
        float Alpha = (float)(alpha*Math.PI/180f);
        Res[2][0] = c*(float)Math.cos( Alpha);
        Res[2][2] = c*(float) Math.sin(Alpha);
        int n =0;
        float YY=0;
        if( a <=c)
        {   n = (int)(-Res[2][0]/a);
            YY= Res[2][0] +n*a;
        }  
        else
        {
           n= (int)(-a*Res[2][0]/(c*c)-.5);
           YY=n*Res[2][0]+a;
        }
        
        float sgn=1;
        if( a <= c)
         {
            if ( Math.abs( YY + a ) < Math.abs( YY ) && a <= c )
            {
               YY += a;
               sgn = -1;
               n++ ;
            }
         }
        else if( (YY+Res[2][0])*(YY+Res[2][0])+(n+1)*(n+1)*Res[2][2]*Res[2][2] < a*a)
        {
           YY+=Res[2][0];
           n++;
           sgn = -1;
           
        }
        
        if( n>0 )
          if( a <= c)
          {
             Res[2][0]= sgn*YY;
             Res[2][2] *=sgn;
          }else
          {
             if( YY*Res[2][0]+n*Res[2][2]*Res[2][2] > 0)
                sgn =-1;
             else
                sgn = 1;
             Res[0][0]= sgn*YY;
             Res[0][2] =sgn*n*Res[2][2];
          }
        
        Res = LinearAlgebra.getInverse( Res );
        
      }
    
      return Res;
   }
   
   private float[][] CalcNiggliUB(float a, float b, float c, 
         float alpha, float beta, float gamma,  char type, char Center)
   {
      
    
      if( Center=='P')
         return CalcConventionalUB( a,b,c,alpha,beta,gamma,type)    ;
      
      float[][]Res = new float[3][3];
      Arrays.fill( Res[0] , 0f );
      Arrays.fill( Res[1] , 0f );
      Arrays.fill( Res[2] , 0f );
      float[][] ConvUB = getConventionalUB();
      if( ConvUB== null)
         return null;
      
      float[][]ResP = LinearAlgebra.getInverse( ConvUB);
  
      if( type=='H' && Center =='I')
         Center ='R';
      
      if( Center == 'I')
      {
         float s1=1;
         float s2=1;
         for( int r =0; r<3;r++)
            for( int cc=0;cc<3;cc++)
            {
               
               if( cc==0)
                  if( r>0)
                  {
                     s1 =(float) Math.pow( -1 , r );
                     s2 =-s1;
                  }
               Res[r][cc] =ResP[0][cc]/2+s1*ResP[1][cc]/2+s2*ResP[2][cc]/2;
            }
           Res = LinearAlgebra.getInverse( Res );
      }
      else if( Center =='F')
      {
         if( type =='H' || type=='M')
            return null;
         
         float[] ss = new float[3];
        
         for( int r =0; r<3;r++)
            for( int cc=0;cc<3;cc++)
            {
               Arrays.fill( ss,1f);
               ss[r]=0;
               
               Res[r][cc]=ss[0]*ResP[0][cc]/2+ss[1]*ResP[1][cc]/2+ss[2]*ResP[2][cc]/2;
            }
         Res = LinearAlgebra.getInverse( Res );
      }
      else if( Center =='A' || Center=='B'|| Center=='C')
      {
         if( type =='H' )
            return null;
         if( type =='M' && Center== 'B')
            return null;
         
         int r=2;
         if( Center =='A') 
            {
            r=0;
            if( b==c && type=='O')
               return null;
            }
         else if( Center =='B')
         {
            r=1;
            if( a==c&& type=='O')
               return null;
            
         }else if( a==b && type=='O')
            return null;
         
         int k=0;
         Res[r]= ResP[r];
         for( int i=1; i<3; i++)
         {  
            if( k==r) k++;
            for( int cc=0;cc<3;cc++)  
       
            {
               int R = (r+1)%3;
               float s = (float) Math.pow( -1,i);
              
               Res[k][cc]= ResP[(R)%3][cc]/2+s*ResP[(R+1)%3][cc]/2;
             
               
            }
            k++;
         }
         Res = LinearAlgebra.getInverse( Res );
      }
      
      
      else if( Center =='R')
      {
         if( type != 'H' || alpha >120)//alpha =120 planar, >120 no go or c under a-b plane.
         {
            conventionalUB=NiggliUB = null;
            return null;
         }
         
         double Alpha = alpha*Math.PI/180;
         Res[0][0] = a;
         Res[1][0] =(float)(a*Math.cos( Alpha ));
         Res[1][1] = (float)(a*Math.sin( Alpha ));
         Res[2][0] =(float)(a*Math.cos( Alpha ));
         Res[2][1] =(float)(a*Res[1][0] -Res[2][0]*Res[1][0])/Res[1][1];
         Res[2][2] =(float)Math.sqrt( a*a- Res[2][1]*Res[2][1]-Res[2][0]*Res[2][0]);
        
         float[][]X = LinearAlgebra.getTranspose( Res );
         Res = LinearAlgebra.mult( U , X );
         conventionalUB = LinearAlgebra.mult( Res , Rhomb2Hex );
       
      }
    
      if( LinearAlgebra.determinant( Res )< 0)
       for( int cc=0;cc<3;cc++)
          Res[0][cc] *=-1;
     // ScriptUtil.display(  DataSetTools.operator.Generic.TOF_SCD.Util.abc( 
    //        LinearAlgebra.float2double( Res )) );
      Res = DataSetTools.components.ui.Peaks.subs.Nigglify( Res );
      
     // ScriptUtil.display(  DataSetTools.operator.Generic.TOF_SCD.Util.abc( 
     //       LinearAlgebra.float2double( Res )) );
      return Res;
   }
   
   public static void main( String[]args)
   {
      float startA = 2;
      float[] side1Ratios ={1f,1.2f,3f,8f};
      float[] alphas ={20,50,80,110,140};
      char[] Centerings={'P','I','F','C','A','B'};
      float error = .30f;
      String dirName = "C:/Isaw/SampleRuns/SNS/TOPAZ/WSF/ExtGsasRuns/Dennis/tests/Err30/";
      //orthorhombic cases
      float[] sigs = new float[7];
      Arrays.fill( sigs , 0f );
      for( int i=0; i<side1Ratios.length; i++)
         for( int j=i;j<side1Ratios.length;j++)
         {
            float a=startA;
            float b= a*side1Ratios[i];
            float c =a*side1Ratios[j];
            String Type="orth0";
            if( a==b || b==c)
               if( b==c && a==b)
                  Type ="cub";
               else
                  Type ="tetr";
            if( b <700 && a < 700)
            for(int cent=0; cent<Centerings.length;cent++)
            {
               char Center = Centerings[cent];
               MakeScalarTest mt = new MakeScalarTest( a,b,c,error,Center,'O',90f,90f,90f);
               float[][] UBn=mt.getNigglyUB( );
               if( UBn != null)
               {
               float[] abcn = LinearAlgebra.double2float( Util.abc( LinearAlgebra.float2double( UBn) ));
               float[][] UBc = mt.getConventionalUB( );
             
               float[] abcc = LinearAlgebra.double2float( Util.abc( LinearAlgebra.float2double( UBc )));
               Util.writeMatrix( dirName+Type+i+""+j+Center+"nigorient.mat" , UBn , abcn , sigs);
               Util.writeMatrix( dirName+Type+i+""+j+Center+"convorient.mat" , UBc , abcc , sigs );
               String DetCalFileName = "C:/ISAW/InstrumentInfo/SNS/TOPAZ/TOPAZ_2010_09_22.DetCal";
               Vector<Peak_new> Peaks = mt.getPeaks( DetCalFileName , 20, 135222 , 18000 );
               try
               {
                   Peak_new_IO.WritePeaks_new( dirName+Type+i+""+j+Center+".peaks", Peaks ,false);
               }catch(Exception s)
               {
                 System.out.println("Cannot create Peaks file "+dirName+"orth1"+i+""+j+Center+".peaks") ;
                 System.out.println("    Error="+ s);
                 System.out.println("  Stack trace");
                 s.printStackTrace( );
               }
               
            }
            }
         }
   
      
      //Monoclininc cases
      
      for( int i=0; i<side1Ratios.length; i++)
         for( int j=i;j<side1Ratios.length;j++)
         {
            float[] sides = {startA,side1Ratios[i]*startA,side1Ratios[j]*startA};
            if( sides[2] < 700)
            for( int i1=0; i1<3;i1++)
               for( int i2a=1;i2a<3;i2a++)
            {

               int i1q =i1;
               int i2q = (i1+i2a)%3;
               int i3q=(i2q+1)%3;
               if( i1q==i3q) i3q = (i3q+1)%3;
               float a = sides[i1q];
               float b = sides[ i2q];
               float c = sides[i3q];
               int i2,i3,ii1;
               if( i1q==0) ii1=0; else if(i1q==1) ii1=i; else ii1=j;
               if ( i2q==0) i2=0; else if(i2q==1) i2=i; else i2=j;
               if( i3q==0) i3=0; else if(i3q==1) i3=i; else i3=j;
               
               if( (a==b & i1q>i2q) ||(a==c && i1q>i3q) || (b==c && i2q>i3q))
                     {}
               else
               for ( int ang=0; ang <alphas.length; ang++)
               {
                  float angle = alphas[ang];
                  for(int cent=0; cent<Centerings.length;cent++)
                  if( cent <5 )
                  {
                     char Center = Centerings[cent];
                     if( ii1==0 && i2==0 && i3==3)
                     {
                        int x=1;
                     }
                     MakeScalarTest mt = new MakeScalarTest( a,b,c,error,Center,'M',angle,90f,90f);
                     float[][] UBn=mt.getNigglyUB( );
                     if ( UBn != null )
                     {
                        float[] abcn = LinearAlgebra.double2float( Util
                              .abc( LinearAlgebra.float2double( UBn ) ) );
                        float[][] UBc = mt.getConventionalUB( );
                     
                        float[] abcc = LinearAlgebra.double2float( Util
                              .abc( LinearAlgebra.float2double( UBc ) ) );
                        Util.writeMatrix( dirName + "mon"+ii1+""+ i2+"" + i3+""+ang + Center
                              + "nigorient.mat" , UBn , abcn , sigs );
                        Util.writeMatrix( dirName + "mon"+ii1+""+ i2+"" + i3+""+ang + Center
                              + "convorient.mat" , UBc , abcc , sigs );
                        String DetCalFileName = "C:/ISAW/InstrumentInfo/SNS/TOPAZ/TOPAZ_2010_09_22.DetCal";
                        Vector< Peak_new > Peaks = mt.getPeaks( DetCalFileName ,
                              20 , 135222 , 18000 );
                        if( Peaks == null || Peaks.size() < 1)
                        {
                           int xx=1;
                        }else
                        try
                        {
                           Peak_new_IO.WritePeaks_new( dirName + "mon"+ii1+""+ i2+"" + i3+""+ang  +
                                 Center + ".peaks" , Peaks , false );
                        } catch( Exception s )
                        {
                           System.out.println( "Cannot create Peaks file "
                                 + dirName + "orth1" + i + j + Center
                                 + ".peaks" );
                           System.out.println( "    Error=" + s );
                           System.out.println( "  Stack trace" );
                           s.printStackTrace( );
                        }
                     }
                     
                  }
                  
               }
               
            }
           
            
            
            
         } 
  
      //Hexagonal case
      
      for( int i=0; i<side1Ratios.length; i++)
      {
         float a = startA;
         float b = a;
         float c = a*side1Ratios[i];
         MakeScalarTest mt = new MakeScalarTest( a,b,c,error, 'P','H',90,90,90);
         float[][] convCell = mt.getConventionalUB( );
       
         float[][] NigCel = mt.getNigglyUB( );
         if( NigCel != null)
         for(int v=0; v<3;v++)//Vert axis z(2),y(1),x(0)
         {
            float[][] ident={{1f,0f,0f},{0f,1f,0f},{0f,0f,1f}};
            if( v<2)
            {
               ident[v][v] =0;
               ident[2][2] =0;
               int v1= 2;
               ident[v][v1] =1;
               ident[v1][v] =1;
            }
            convCell = LinearAlgebra.mult(ident, convCell   );
            NigCel = LinearAlgebra.mult( ident,NigCel );
            mt.setNiggliUB( NigCel );
            mt.setConventionalUB( convCell );
            float[] abcn = LinearAlgebra.double2float( Util
                  .abc( LinearAlgebra.float2double( NigCel ) ) );
            Util.writeMatrix( dirName + "hex"+i+""+v
                  + "nigorient.mat" , NigCel , abcn , sigs );
            float[] abcc = LinearAlgebra.double2float( Util
                  .abc( LinearAlgebra.float2double(convCell ) ) );
            Util.writeMatrix( dirName +"hex"+i+""+v
                  + "convorient.mat" , convCell , abcc , sigs );
            String DetCalFileName = "C:/ISAW/InstrumentInfo/SNS/TOPAZ/TOPAZ_2010_09_22.DetCal";
            Vector< Peak_new > Peaks = mt.getPeaks( DetCalFileName ,
                  20 , 135222 , 18000 );
            if( Peaks == null || Peaks.size() < 1)
            {
               int xx=1;
            }else
            try
            {
               Peak_new_IO.WritePeaks_new( dirName + "hex"+i+""+v+".peaks" , Peaks , false );
            } catch( Exception s )
            {
               System.out.println( "Cannot create Peaks file "
                     + dirName +"hex"+i+""+v
                     + ".peaks" );
               System.out.println( "    Error=" + s );
               System.out.println( "  Stack trace" );
               s.printStackTrace( );
            }
         }
      }

     //Rhombohedral case
      
      for( int ang=0; ang < alphas.length; ang++)
      {
         float a=startA;
         float angle = alphas[ang];
         MakeScalarTest mt = new MakeScalarTest( a,a,a,error,'R','H',angle,angle,angle);
         float[][] convCell = mt.getConventionalUB( );
       
         float[][] NigCel = mt.getNigglyUB( );
         if ( NigCel != null )
         {
            float[] abcn = LinearAlgebra.double2float( Util.abc( LinearAlgebra
                  .float2double( NigCel ) ) );
            float[] abcC = LinearAlgebra.double2float( Util.abc( LinearAlgebra
                  .float2double( convCell ) ) );
            Util.writeMatrix( dirName + "rho" + ang + "nigorient.mat" , NigCel ,
                  abcn , sigs );
            Util.writeMatrix( dirName + "rho" + ang + "convorient.mat" ,
                  convCell , abcC , sigs );
            String DetCalFileName = "C:/ISAW/InstrumentInfo/SNS/TOPAZ/TOPAZ_2010_09_22.DetCal";
            Vector< Peak_new > Peaks = mt.getPeaks( DetCalFileName , 20 ,
                  135222 , 18000 );
            if ( Peaks == null || Peaks.size( ) < 1 )
            {
               int xx = 1;
            } else
               try
               {
                  Peak_new_IO.WritePeaks_new( dirName + "rho" + ang + ".peaks" ,
                        Peaks , false );
               } catch( Exception s )
               {
                  System.out.println( "Cannot create Peaks file " + dirName
                        + "rho" + ang + ".peaks" );
                  System.out.println( "    Error=" + s );
                  System.out.println( "  Stack trace" );
                  s.printStackTrace( );
               }
         }
      }
      
   }
   /**
    * @param args
    */
   public static void main1(String[] args)
   {
    JTextField A, B, C, Alph, Bet, Gam, Type, Cent;
    A = new JTextField("2.4");
    B = new JTextField("16");
    C = new JTextField("2.0");
    Alph = new JTextField("140");
    Bet = new JTextField("90");
    Gam = new JTextField("90");
    Type = new JTextField("M");
    Cent = new JTextField("I");

    
    JPanel panel = new JPanel();
    panel.setLayout( new GridLayout( 8,2));
    panel.add(  new JLabel("a") );
    panel.add( A );
    panel.add(  new JLabel("b") );
    panel.add( B );
    panel.add(  new JLabel("c") );
    panel.add( C );
    panel.add(  new JLabel("alpha") );
    panel.add( Alph );
    panel.add(  new JLabel("beta") );
    panel.add( Bet );
    panel.add(  new JLabel("gamma") );
    panel.add( Gam );

    panel.add(  new JLabel("Type") );
    panel.add( Type );
    panel.add(  new JLabel("Center") );
    panel.add( Cent         );

    JOptionPane.showMessageDialog(  null , panel );
    float a = Float.parseFloat(  A.getText( ).trim( ) );
    float b = Float.parseFloat(  B.getText( ).trim( ) );
    float c = Float.parseFloat(  C.getText( ).trim( ) );
    float alpha = Float.parseFloat(  Alph.getText( ).trim( ) );
    float beta = Float.parseFloat(  Bet.getText( ).trim( ) );
    float gamma = Float.parseFloat(  Gam.getText( ).trim( ) );
    char type =  Type.getText( ).trim( ).charAt( 0 );
    char Center= Cent.getText( ).trim( ).charAt( 0 );
    MakeScalarTest mt = new MakeScalarTest(a,b,c,0,Center,type,alpha,beta,gamma);
    ScriptUtil.display( mt.getNigglyUB() );
    ScriptUtil.display( mt.getConventionalUB( ) );
    ScriptUtil.display( LinearAlgebra.getInverse( mt.getNigglyUB()) );
    String DetCalFileName = "C:/ISAW/InstrumentInfo/SNS/TOPAZ/TOPAZ_2010_09_22.DetCal";
    Vector<Peak_new>Peaks = mt.getPeaks( DetCalFileName, 10, 250000,1752); 
    try
    {
       Peak_new_IO.WritePeaks_new( "C:/Users/Ruth/x.peaks" , Peaks , false );
    }catch( Exception s)
    {
       
    }
   

   
   }

}
