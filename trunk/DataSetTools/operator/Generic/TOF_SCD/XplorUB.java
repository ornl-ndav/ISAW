/* 
 * File: XplorUB.java 
 *  
 * Copyright (C) 2007     Ruth Mikkelson
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
 * Contact :  Dennis Mikkelson<mikkelsond@uwstout.edu>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 * $Log$
 * Revision 1.1  2008/01/30 16:31:39  rmikk
 * Changed package for this class
 *
 * Revision 1.2  2008/01/30 16:03:44  rmikk
 * Removed second GPL and added documentation
 *
 * Revision 1.1  2008/01/30 15:18:04  rmikk
 * Initial checkin
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;
import java.util.Vector;

import javax.swing.JFileChooser;

import Command.Script_Class_List_Handler;
import java.text.*;


import DataSetTools.operator.Generic.TOF_SCD.*;
/**
 * 
 * This class is just a front-end testing program for the class
 * DataSetTools.operator.Generic.TOF_SCD.GetUB. The testing breaks down
 * the steps and allows for examining intermediate results.
 * 
 * @author Ruth
 *
 */
public class XplorUB {

   /**
    * Constructor
    */
   public XplorUB() {

      super();
     
   }
   
   /**
    * Displays the format for using this program
    */
   public static void showUsage(){
      System.out.println(" To invoke this application either");
      System.out.println("   1. Java XplorUB  PeaksFilename");
      System.out.println("           or");
      System.out.println("   2. Select the Peaks File from the Open Dialog");
      
      System.out.println("");System.out.println("");
      System.out.println(" This application parallels  "+
               "DataSetTools.operator.Generic.TOF_SCD.GetUB");
      System.out.println(" Intermediate steps can be viewed and altered.");
      
   
  }
   
  /**
   * Displays the elements in a List assuming List elements are candidates for 
   * plane normals
   * @param List  The list of plane normals along with other statistics
   * @param Nelements The number of relevent elements in the lis
   */
  public static void showList( float[][] List, int Nelements){
     System.out.println("------------Candidates for Plane Normals"+"" +
                                                  "---------------------");
     System.out.println("Column Headings ");
     System.out.println("Ux,Uy,Uz    x,y,and z coord of unit direction");
     System.out.println("d, nbins    represent the length between adjacent" +
              "planes  in q(d) and in bins on a line(nbins)");
     System.out.println("r            The highest correlaed value after 1st"+
                                       " negative correlation");
     System.out.println("%            The percent of peaks whose miller index"+
                       " for this direction is within .2 of an integer");
     String Header ="        Ux       Uy      Uz       d    nbins    r       %  ";
     //System.out.println( Header );
     java.text.DecimalFormat dform1 =new java.text.DecimalFormat(" 0.0000 ;-0.0000 ");
     java.text.DecimalFormat dform2 =new java.text.DecimalFormat(" 000.000 ;-000.000 ");
     java.text.DecimalFormat dform3 =new java.text.DecimalFormat(" 000 ;-000 ");
     //("#.#### #.#### #.#### ###.###  ###   #.#### ###.### ");
     StringBuffer sb = new StringBuffer(50);
     for( int i=0; i < GetUB.Nelements; i++){
        float[] L1= GetUB.List[i];
        sb = dform3.format((double)i,sb, new FieldPosition(NumberFormat.INTEGER_FIELD));
        sb.append(":");
        sb = dform1.format((double)L1[GetUB.X], sb, new FieldPosition( NumberFormat.FRACTION_FIELD));
        sb = dform1.format((double)L1[GetUB.Y], sb, new FieldPosition( NumberFormat.FRACTION_FIELD));
        double z = Math.sqrt( 1- L1[GetUB.X]* L1[GetUB.X]- L1[GetUB.Y]* L1[GetUB.Y]);
        sb = dform1.format(z, sb, new FieldPosition( NumberFormat.FRACTION_FIELD));
        sb = dform2.format((double)L1[GetUB.LEN],sb,new FieldPosition(NumberFormat.FRACTION_FIELD));
        sb= dform3.format((double)L1[GetUB.NBINS_PER_LINE],sb, new FieldPosition(NumberFormat.INTEGER_FIELD));
        sb = dform1.format((double)L1[GetUB.CORR], sb, new FieldPosition( NumberFormat.FRACTION_FIELD));
        sb = dform1.format(50*(double)L1[GetUB.FIT1], sb, new FieldPosition( NumberFormat.FRACTION_FIELD));
        if( i %20 ==0 )
           System.out.println( Header);
        System.out.println( sb);
        
        sb.setLength(0);
        
     }
  }
  
  /**
   * Displays the correlations for the projections of the peaks to the last set line
   * @param correlations  A list of auto correlations starting with a lag of 2
   * @param start   The lag
   */
  public static void showCorrs( float[] correlations, int start){
     java.text.DecimalFormat dform3 =new java.text.DecimalFormat(" 000 ;-000 ");
     java.text.DecimalFormat dform1 =new java.text.DecimalFormat(" 0.0000 ;-0.0000 ");
     StringBuffer sb = new StringBuffer(86);
     sb.append(" 0   :");
     if( start > 0) sb.append("------- ------- ");
     for( int i=0; i< GetUB.xixj.length;i++){
        sb =dform1.format((double)correlations[i],sb, new FieldPosition( NumberFormat.FRACTION_FIELD));
        if( (i+1+start)%10 ==0){
           System.out.println( sb);
           sb.setLength(0);
           sb=dform3.format((double)i+1+start,sb, new FieldPosition(NumberFormat.INTEGER_FIELD));
           sb.append(":");
        }
     }
     System.out.println(sb);
     
     
  }
  
  
  /**
   * Displays the result of projecting the peaks onto a line in the last set direction
   * @param line  The list of total intensities in the various bins. 
   */
  public static void showLine( float[] line){
     java.text.DecimalFormat dform3 =new java.text.DecimalFormat(" 00000 ;-00000 ");
     java.text.DecimalFormat dform1 =new java.text.DecimalFormat(" 0000000.00 ;-0000000.00 ");
     StringBuffer sb = new StringBuffer(86);
     sb.append(" 0   :");
     
     for( int i=0; i<line.length;i++){
        sb =dform1.format((double)line[i],sb, new FieldPosition( NumberFormat.FRACTION_FIELD));
        if( (i+1)%10 ==0){
           System.out.println( sb);
           sb.setLength(0);
           sb=dform3.format((double)i+1,sb, new FieldPosition(NumberFormat.INTEGER_FIELD));
           sb.append(":");
        }
     }
     System.out.println(sb);
     
     
  }
  

   /**
    * This program is a front-end testing program for the class
    * DataSetTools.operator.Generic.TOF_SCD.GetUB. The testing breaks down
    * the steps and allows for examining intermediate results.
    * @param args  the filename containing the information about the peaks
    */
   public static void main( String[] args ) {
      String PeaksFileName = null;
      
      
      float gridWidth = .1f;
      float MaxXtalLength =15;
      float x = 0;
      float y , z, d;
      x = 0.037499327f;
      y = -0.89929664f;
      d= Float.NaN;
      
      float near = .5f;
      float range = .3f;
      float[] line = null;
      boolean[] omit = null;
      float[][] Dirs = null;
      float[][] UB = null;
      float[][] List;
      float[] corrs1Line = null;
      int stop =0; //stop code for calc UB matrix
      boolean CandidatesNeedCalc = true;
      
      if( args==null || args.length < 1){
        JFileChooser jf = new JFileChooser();
        if( jf.showOpenDialog( null) == JFileChooser.APPROVE_OPTION)
           PeaksFileName = jf.getSelectedFile().getAbsolutePath();
        else{
           XplorUB.showUsage();
           System.exit( 0 );
        }
           
      }else
         PeaksFileName = args[0];
      
      Object Res = ( new DataSetTools.operator.Generic.TOF_SCD.ReadPeaks(
               PeaksFileName ) ).getResult();
      if( Res instanceof gov.anl.ipns.Util.SpecialStrings.ErrorString){
         System.out.println("Could not bring in peaks file becuse "+ Res);
         System.out.println( PeaksFileName);
         System.exit(0);
      }
      Vector Peaks =(Vector) Res;
      String S = "";
      while( !S.equals("q")){
      System.out.println( "Enter Option desired");
      System.out.println("    gw- Set Grid Width                                        st- stop code");
      System.out.println("    mx- Set maximum length of Xtal cell in real space         N  -Fraction indexed by current UB");
      System.out.println("    f- Find All direction candidates. Use L to view list      ab-run abc on UB");
      System.out.println("         Set gw and mx to desired specs(option v below)       n0-#of omitted peaks");
      System.out.println("    L- Show list of all directions candidates");
      System.out.println("    Lx- Load xth entry of L for testing");
      System.out.println("        Their x,y and d values will be loaded");
      System.out.println("    x- set x value for unit nnormal direction(z>=0)");
      System.out.println("    y- set y value for unit normal direction(z>=0)");
      System.out.println("    d- set distance in q between peaks in dir x,y");
      System.out.println("    l- project peaks to line in direction x, y ,"+
                                        "(z>0 and unit vector)");
      System.out.println("    c-Calculate autocorrelations, best lag, etc. ");
      System.out.println("       Use options vc, vr, and vp for details");
      System.out.println("    vc-View the autocorrelations    ");
      System.out.println("    vr- View the best autocorrelated values");
      System.out.println("    vp- View percentage of points within .2 of plane");
      System.out.println("");
      System.out.println("    t- set closeness of peak to plane to not "+
                                      "be omitted. Planes are in the current"+
                                      " direction(x,y) with the distance from vr");
      System.out.println("        This calculates the miller index for the"+ 
                                 "plane the peak lies on. Only the fractional"+
                                 "part of this index is used");
      System.out.println("        enter a negative number to reset"+
                                        "omitted peaks to none");
      System.out.println("    o- omit peaks too far from planes");
      System.out.println("        Note this is a union. The previously "+
                                  "omitted peaks are still omitted");
      System.out.println("    n-calculate the number of peaks that are too "+
                                    "far from a plane");
      
      System.out.println("");
      System.out.println("    D- show the current of plane normals directions");
      System.out.println("    Dx-SET xth plane normal to x,y,d");
      System.out.println("    v-View gw,mx,x,y,d,t,#peaks, and UB");
      System.out.println("    UB-Calculate the best UB matrix with plane "+
                                "Normal directions D");
      System.out.println("        The UB matrix is run through Blind to"+
                                  "standardize");
      System.out.println("    Sx - Saves the UB matrix to UBx.mat in the "+
                                     "user's home directory");
      System.out.println("     w- Does the whole GetUBMatrix operator w. debug");
      System.out.println("          This changes UB so this result can be "+
                                    "saved wih option Sx above");
      
      System.out.println("     q- to exit");
      
      
      S = Script_Class_List_Handler.getString();
      String S1 = null;
      S = S.trim();
      if( S.equals( "gw" ) ) {
         System.out.println("");
         try{
          gridWidth = (new Float(
                         Script_Class_List_Handler.getString())).floatValue();
          if( (gridWidth <.000001) || (gridWidth >1)){
             System.out.println("Improper gridWidth");
             gridWidth =.1f;
          }
          CandidatesNeedCalc=true;
         }catch(Exception s1){
            System.out.println("Could not change Grid width");
         }
         System.out.println("");
         }
         else if( S.equals( "mx" ) ) {
            System.out.println("");

            
            try{
               MaxXtalLength = (new Float( Script_Class_List_Handler.getString()))
                                                                  .floatValue();
               if( (MaxXtalLength < 1) || ( MaxXtalLength > 30 )){
                  System.out.println("Improper unit cell length");
                  MaxXtalLength = 12f;
               }
               CandidatesNeedCalc=true; 
               line = null;
               
              }catch(Exception s1){
                 System.out.println("Could not change max length unit cell");
              }

              System.out.println(""); 
         }
         else if( S.equals( "f" ) ) {
            System.out.println("");
            float[] L = new float[200];
            if(CandidatesNeedCalc){
               GetUB.getCandidateDirections( Peaks, omit,gridWidth,
                                                         MaxXtalLength, L);
               CandidatesNeedCalc =false;
            }
            System.out.println("");
         }
         else if( S.equals( "L" ) ) {
            if( !CandidatesNeedCalc)
                 XplorUB.showList( GetUB.List, GetUB.Nelements );
            else
               System.out.println("Use f option first");
            System.out.println("");
            
         }
         else if( S.startsWith( "L" ) ){
            System.out.println("");
            if( CandidatesNeedCalc)
               System.out.println("Use f option first");
            else{
              try{
              int n = (new Integer(S.substring(1).trim())).intValue(); 
              if( n>=0  && n < GetUB.Nelements){
                 float[] dat = GetUB.List[n];
                 x= dat[ GetUB.X];
                 y= dat[ GetUB.Y];
                 d= dat[ GetUB.LEN];
                 line = null;
              }
              }catch( Exception s2){
                 System.out.println("Cannot load given candidate");
              }
            }
            System.out.println("");
         }
         else if( S.equals( "x" ) ) {
            System.out.println("\n Enter x coord of unit direction");
            
            try{
               x= (new Float( Script_Class_List_Handler.getString())).floatValue();
               line = null;                
              }catch(Exception s1){
                 System.out.println("Could not change x coord of unit direction");
              }
              System.out.println("");
             
         }
         else if( S.equals( "y" ) ) {
            System.out.println("\n Enter y coordinate of unit direction");
            
            try{
               y = (new Float( Script_Class_List_Handler.getString()))
                                                                 .floatValue();
               line = null;                 
              }catch(Exception s1){
                 System.out.println("Could not change y coord of unit"+
                                                                    " direction");
              }
             
         }
         else if( S.equals( "d" ) ) {
            System.out.println("\n Enter distance(q) between crystal planes"+
                                    " in (x,y,z') dir  ");
            try{
               d = (new Float( Script_Class_List_Handler.getString())).floatValue();
                                
              }catch(Exception s1){
                 System.out.println("Could not change d");
              }
              System.out.println("");
         }
         else if( S.equals( "l" ) ) {
            System.out.println("");
            corrs1Line = null;
            float[] L = new float[200];
            if( 1 - x*x-y*y >=0){
               System.out.println("NOTE: Center is middle entry and each bin "+
                        " spans a distance in q of "+
                                                 1 /MaxXtalLength / 4f / 12f );
               line = GetUB.ProjectPeakToDir( x, y, Peaks, omit, 
                                          MaxXtalLength, L);
               XplorUB.showLine( line);
            } else{
               line = null;
               
               System.out.println("(x,y) are not in the unit circle");
            }
            System.out.println("");
         }
         else if( S.equals( "c" ) ) {
            if( line != null && corrs1Line == null){
               System.out.println("");
               corrs1Line = GetUB.CalcStats( line, GetUB.findMinNonZero( line),
                        GetUB.findMaxNonZero( line ));
              
            }else if( line == null)
               System.out.println(" Must use option l first");
            System.out.println("");
         }
         else if( S.equals( "vc" ) ) {
            System.out.println("");
            if(corrs1Line!= null &&  line != null)
               XplorUB.showCorrs( GetUB.xixj,2);
            else
               System.out.println("Option c is not done.");
               System.out.println("");
         }
         else if( S.equals( "vr" ) ) {
            System.out.println("");
            if(corrs1Line!= null &&  line != null){
               System.out.println("'Best' correlation is  "+ 
                                                    corrs1Line[ GetUB.CORR]);

               System.out.println("  occurs at a lag of  "+ 
                       corrs1Line[ GetUB.LEN]/MaxXtalLength / 4f / 12f +"in q");
               System.out.println("  occurs at a lag of "+  
                          corrs1Line[ GetUB.NBINS_PER_LINE]+" in bins on line");
            }else
               System.out.println("Option c is not done.");
            System.out.println("");
               
         }
         else if( S.equals( "vp" ) ) {
            if(corrs1Line!= null &&  line != null)
               System.out.println( corrs1Line[ GetUB.FIT1]/2f);
            else
               System.out.println("Option c is not done.");
           System.out.println("");
         }
         else if( S.equals( "t" ) ) {
            System.out.println("Enter tolerance in miller indices for a peak "+
                      "to be on a crystal plane perpendicular to (x,y,z'>=0)");
            S1= Script_Class_List_Handler.getString();
            try{
               range = (new Float(S1.trim())).floatValue();
               if( range < 0)
                  omit = null;
               else if( range < .01 || range >.5){
                  System.out.println("closeness is set to .3");
                  range = .3f;
               }
            }catch( Exception s3){
               System.out.println("closeness is set to .3");
                range = .3f;
            }
            CandidatesNeedCalc =true;
            System.out.println("");
         }
         else if( S.equals( "o" ) && range > 0 ) {
            System.out.println("");
            if( omit == null){
               omit = new boolean[ Peaks.size()];
               java.util.Arrays.fill(omit, false);
            }
            float[] Q = new float[3];
            Q[0]=d*x;
            Q[1]=d*y;
            Q[2] =d*(float)Math.sqrt(1-x*x-y*y);
            int n= GetUB.OmitPeaks( Peaks,Q, omit, range,false);
            System.out.println(n+" new peaks were omitted");
            System.out.println("");
            CandidatesNeedCalc = true;
            
         }
         else if( S.equals( "n" ) ) {
            System.out.println("");
            if( range < 0)
               System.out.println(" No new peaks will be omitted");
            else if( 1-x*x-y*y>=0){
            if( omit == null){
               omit = new boolean[ Peaks.size()];
               java.util.Arrays.fill(omit, false);
            }
            float[] Q = new float[3];
            Q[0]=d*x;
            Q[1]=d*y;
            Q[2] =d*(float)Math.sqrt(1-x*x-y*y);
            int n= GetUB.OmitPeaks( Peaks,Q, omit, range,true);
            System.out.println(n+" new peaks will be omitted");
            }else
               System.out.println("Current (x,y) are not inside unit circle");
            System.out.println("");
         }
         else if( S.equals( "D" ) ) {
            System.out.println("");
            GetUB.showDirs( Dirs );
            System.out.println("");
         }
         else if( S.startsWith( "D" ) ) {
            System.out.println("");
            if( 1-x*x-y*y >= 0)
            try{
               int n=( new Integer( S.substring(1).trim())).intValue();
               float[] Q = new float[3];
               Q[0]=d*x;
               Q[1]=d*y;
               Q[2] =d*(float)Math.sqrt(1-x*x-y*y);
               if( Dirs == null){
                  Dirs = new float[1][3];
                  Dirs[0] = Q;
               }else {
                  if( n>= Dirs.length){
                     float[][] Dirs1= new float[Dirs.length+1][3];
                     System.arraycopy( Dirs,0,Dirs1,0,Dirs.length);
                     Dirs= Dirs1;
                  }
                  Dirs[Math.min(n,Dirs.length-1)] = Q;
                     
               }
               
                     
            }catch( Exception s4){
               System.out.println("Cannot set this direction as one of the 3");
            }
            else
               System.out.println("Current (x,y) are not inside unit circle");
            System.out.println("");
         }
         else if( S.equals( "v" ) ) {
            System.out.println("-------------------------------");
            System.out.println("    Grid Width ="+gridWidth);
            System.out.println(" Set maximum unit cell length ="+MaxXtalLength);
            System.out.println(" Current x coord of unit direction ="+x);
            System.out.println(" Current y coord of unit direction ="+y);
            System.out.println(" Current distance in q between adjacent planes "+
                     "in unit direction (x,y,(z>0))=" +d);
            System.out.println("Closeness of peak to plane to not "+
                                      "be omitted = " +range);
            System.out.println(" # of peaks ="+ Peaks.size());
            System.out.println("-------------------------------");
            
            
         }else if( S.equals( "st" )){
            System.out.println("Enter Code for stopping UB calculation. 1 is no process");
            System.out.println(" 2 is after optimization. All other codes go thru blind");
            S1= Script_Class_List_Handler.getString();
            try{
               stop = ( new Integer( S1.trim())).intValue();
            }catch( Exception ss){
               stop =0;
            }
      
         }else if( S.equals( "UB" ) ) {
            System.out.println("");
            if( Dirs == null || Dirs.length < 3)
               System.out.println(" Use D0,D1,D2 to set current plane normals");
            else{
               float[] Stats = new float[5];
               UB= GetUB.UBMatrixFrPlanes( Dirs, Peaks, omit, Stats, stop);   
               System.out.println("Stats"+Command.ScriptUtil.display(Stats));
               }
            System.out.println("");
         }else if( S.equals("N")){
            boolean[] omt = null;
            if( stop ==7) omt=new boolean[Peaks.size()];
            System.out.println( GetUB.IndexStat( UB , Peaks , range, omt )+" of the "+
                     Peaks.size()+" are indexed at level "+range);
            if( stop==7){
               omit=omt;
               System.out.println("   The omitted peaks are set");
            }
         }else if( S.equals("w")){
            System.out.println("");
            GetUB.debug = true;
            float[] Stats = new float[5];
            UB = GetUB.GetUBMatrix( Peaks, MaxXtalLength,Stats);
            GetUB.debug = false; 
            System.out.println("Stats"+Command.ScriptUtil.display(Stats));
         }else if( S.equals( "no" )){//# omitted
            if( omit == null)System.out.println("0 peaks are omitted");
            else{
               int ct =0;
               for( int i=0; i< omit.length; i++)
                  if( omit[i])ct++;
               System.out.println( ct +" peaks omitted out of "+
                        Peaks.size()+" peaks");
            }
         }else if( S.equals("ab")){
            if(UB != null)
             System.out.println( Command.ScriptUtil.display(
                      DataSetTools.operator.Generic.TOF_SCD.Util.abc(
                               gov.anl.ipns.MathTools.LinearAlgebra.float2double(UB))));
         }else if( S.startsWith( "S" ) ) {
            System.out.println("");
            String filename = System.getProperty( "user.home");
            filename = filename.replace('\\','/');
            if( !filename.endsWith("/"))if( filename.length() >0)
               filename = filename+'/';
            filename += "UB"+S.substring(1).trim()+".mat";
            if( UB != null ) {
               double[][] UBd = new double[ 3 ][ 3 ];
               for( int i = 0 ; i < 3 ; i++ )
                  for( int j = 0 ; j < 3 ; j++ )
                     UBd[ i ][ j ] = UB[ i ][ j ];
               double[] abcd = DataSetTools.operator.Generic.TOF_SCD.Util
                        .abc( UBd );
               float[] abc = new float[ 7 ];
               for( int i = 0 ; i < 7 ; i++ )
                  abc[ i ] = (float) abcd[ i ];
               
              
               DataSetTools.operator.Generic.TOF_SCD.Util.writeMatrix(
                       filename, UB , abc , new float[ 8 ] );
               System.out.println( "File written to UB" + filename );
         }
            System.out.println("");
      }

   }//while not 'q'
   }

}
