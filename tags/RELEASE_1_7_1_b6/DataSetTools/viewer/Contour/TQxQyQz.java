/*
 * File: TQxQyQz.java
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 * $Log$
 * Revision 1.12  2004/06/18 18:39:55  rmikk
 * Eliminated unused variables
 *
 * Revision 1.11  2004/03/15 19:33:59  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.10  2004/03/15 03:29:00  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.9  2004/03/06 21:56:04  dennis
 * Fixed format of log message.
 *
 * Revision 1.8  2004/01/24 22:22:25  bouzekc
 * Removed unused imports and local variables.
 *
 * Revision 1.7  2003/05/19 15:20:42  rmikk
 * -Now uses the DataSetTools.components.ui.PlaneSelector
 *    control.
 * -Tested and fixed code so this is now a DataSet viewer( possibly
 *    not interactive)
 *
 * Revision 1.6  2003/01/09 21:10:03  rmikk
 * Fixed the matrix corresponding to the three directions.
 *
 * Revision 1.5  2003/01/07 23:04:08  rmikk
 * Fixed an error with the orientation file name
 *
 * Revision 1.4  2003/01/07 16:08:47  rmikk
 * The main program now has file browse buttons.  If there is an orientation 
 * file for the given data set, it can now be loaded.  This means that the 
 * hkl values will show for the selected points.
 *
 * Revision 1.3  2002/11/27 23:24:30  pfpeterson
 * standardized header
 *
 */
package DataSetTools.viewer.Contour;
import DataSetTools.viewer.*;
import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.ViewTools.UI.*;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import Operators.Generic.*;
import DataSetTools.operator.DataSet.*; 
import DataSetTools.operator.*;

public class TQxQyQz  extends ContourView
  {
    PlaneSelector PlaneControl;
    float[][]  Or,IOr, T, IT , Ident;
    DataSet ds;
    String[] AxisName;
    String[] AxisUnits;
    ViewerState state;
    JPanel PlaneSelHolder;
    //  Or*(hkl)=Qxyz;   T*Qxyz = (hkl)
    public TQxQyQz( DataSet ds, ViewerState state)
      {super( ds, state, new QxQyQzAxesHandler( ds,null,null,null));
       
       //rpl_.addKeyListener( new MyKeyListener());
       //Experiment E =new Experiment(getDataSet(), Transf );
       //E.addOKListener( new ExperimentListener());
        AxisName = new String[3];
        AxisUnits= new String[3];
        for( int i=0; i<3; i++)
          {
            IAxisHandler Iax = Transf.getAxis(i);
            AxisName[i] = Iax.getAxisName();
            AxisUnits[i] = Iax.getAxisUnits();
           }
        this.ds = ds;
        PlaneControl = new PlaneSelector();
        PlaneControl.add( PlaneSelector.CARD_3POINT_PLANE);
        PlaneControl.add( PlaneSelector.CARD_DIRECTION);
        for( int i=0; i< 3;i++)
          for( int card = 0;card <2; card++)
            PlaneControl.addPointListener( new PointListener( card,i), card,i);
        PlaneControl.addPlaneChangeListener( new PlaneChangeListener());
        PlaneSelHolder.add( PlaneControl );
        Ident = new float[3][3];
        for( int i=0; i<3;i++)
          for( int j=0;j<3;j++)
            if( i == j)
              Ident[i][j]= 1.0f;
            else
              Ident[i][j] = 0.0f;
       
        Attribute A = ds.getAttribute( Attribute.ORIENT_MATRIX);
        if( A == null)
         {
            Or = IOr = Ident;
          }
        else
          {
             Or = ((Float2DAttribute)A).getFloatValue();
             if( Or == null) 
               Or = Ident;
             else
               for( int i=0; i<2;i++)
                   for( int j=0;j<2;j++) 
                     Or[i][j] = (float)(Or[i][j]*2*java.lang.Math.PI);           
             IOr = LinearAlgebra.getInverse(Or);
             if( IOr == null)
               { IOr= Or = Ident;
                }

             if( IOr != Ident)
               { String[] Label = {"h", "k", "l"};
                 for( int card = 0; card < 2; card++)
                    PlaneControl.setLabel( card, 0, Label );
               }
           }
          T = IOr;
          IT = Or;
       }

    public void setDataSet( DataSet ds)
      {
       Transf = new QxQyQzAxesHandler( ds, null,null,null);
       super.setDataSet( ds );
       }

   
   /**
   *   Adds the PlaneControl to jpanel and sets it up
   *
   */
   public void addControl( JPanel jpanel)
     {
       PlaneSelHolder = new JPanel();
       jpanel.add( PlaneSelHolder);

     }

   class PointListener implements ActionListener
     {
      int card,point;
      public PointListener( int card, int point)
        {
         this.card = card;
         this.point = point;
        }
  
       public void actionPerformed( ActionEvent evt)
         {
           int Group = ds.getPointedAtIndex();
           float time = ds.getPointedAtX();
           if( Group< 0)
             return;
           if( Group >= ds.getNum_entries())
             return;
           if( card ==1)
             if( point ==3)
                return;
           String S = "Find Qx, Qy, Qz";
           if( Or != Ident)
              S = "Find h, k, l";
           DataSetOperator op = ds.getOperator(S);
           if( op == null)
             {
               SharedData.addmsg( "No data set operator "+S);
               return;
             } 
           op.setParameter(new Parameter( "", new Integer(Group)), 0) ;
           op.setParameter( new Parameter("", new Float( time)), 1);
           Object O = op.getResult();
           if( O instanceof Position3D)
             PlaneControl.setPoint( card, point, ((Position3D)O).getCartesianCoords());
           else
              SharedData.addmsg("error in getting coords:"+O);

         }

      }//PointChangeListener

   class PlaneChangeListener implements ActionListener
     {

       public void actionPerformed( ActionEvent evt)
        {float[] Opoint ;
         Opoint = PlaneControl.getPoint( 0,0);
         
         T = new float[3][3];
         T[0] = PlaneControl.getPoint( 0,1);
         T[1]= PlaneControl.getPoint( 0,2);
         T[2] = PlaneControl.getPoint(1,2);
         
         if( (Opoint == null) ||(T[0]==null) ||(T[1]==null) ||(T[2]==null))
           { SharedData.addmsg("Not all points are set");
             return;
            }
         for(int j=0; j<3; j++)
           {
            T[0][j] -=Opoint[j];
            T[1][j] -=Opoint[j];
            }
         
         T = LinearAlgebra.mult( Or,T);
         
         normalize( T[0] );  
         float dotProd = T[0][0]*T[1][0]+T[0][1]*T[1][1]+T[0][2]*T[1][2];
         for( int j=0; j< 3; j++)
            T[1][j] -= dotProd*T[0][j];
         normalize(T[1]);
         normalize( T[2]);
         float[][] TT = new float[3][3];
         for( int i=0;i<3;i++)
          for( int j=0; j<3; j++)
            TT[i][j]= T[i][j];

        
         T =  LinearAlgebra.getInverse( TT );
         if( T == null)
           T = Ident;
         Transf.setTransformation(T, null, null);

         
         setData( getDataSet(), getState().get_int( ViewerState.CONTOUR_STYLE) );
         rpl_.draw();
        }

      }
   public void normalize( float[] v)
     {
      float L =(float) java.lang.Math.sqrt( v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
      if( L == 0)
        return;
      v[0] = v[0]/L;
      v[1]=v[1]/L;
      v[2]=v[2]/L;
      }
   public static void main( String args[])
   {JFrame jf = new JFrame("QxQyQz transform");
   
    jf.setSize(600,600);
    Vector Prompts, InitValues;
    Prompts = new Vector();
    InitValues = new Vector();
    Prompts.addElement( "DataSet Filename?");
    InitValues.addElement( new LoadFileString() );
    Prompts.addElement( "Histogram number ");
    InitValues.addElement( new Integer(1));
    Prompts.addElement( "Orientation file Exists?");
    InitValues.addElement( new Boolean( false));
    Prompts.addElement("Orientation Filename");
    InitValues.addElement( new LoadFileString() );
    Operator op =( new InputBox( "TQxQyQz", Prompts, InitValues, new Vector()));
    Object Result = op.getResult();
    if(Result instanceof ErrorString)
      {
        System.out.println("Error="+Result);
        System.exit( 0 );
      }

    String DSfilename =  InitValues.elementAt(0).toString();
    int k = ((Integer)(InitValues.elementAt(1))).intValue();
    String ORfilename = InitValues.elementAt(3).toString();
    boolean orient_file = ((Boolean)(InitValues.elementAt(2))).booleanValue();
  
    DataSet[] DS = ( new IsawGUI.Util()).loadRunfile( DSfilename);

    if( DS == null)
       System.exit(0);
    if( DS.length <=0)
       System.exit(0);
   
    if( k < 0)
       k=DS.length-1;
    if( k >= DS.length)
        k = DS.length-1;
    
    DataSet ds = DS[k];
   
    if( orient_file)
      {
       DataSetOperator opp = ds.getOperator("Load Orientation Matrix");
       if( opp != null)
         {
          //LoadFileString lfs = new LoadFileString( ORfilename);
     
          opp.setParameter(new Parameter("filename", ORfilename ),0);
         System.out.println( opp.getResult() );
         }
       else
         System.out.println("No Load Orientation operator");
 
    }
  
    TQxQyQz TQ = new TQxQyQz(ds, null);

    jf.getContentPane().add( TQ);
    jf.show();
    jf.validate();


    }
    class MyKeyListener extends KeyAdapter//did not work
      {public MyKeyListener()
         {
         }
       public void keyTyped(KeyEvent e)
         {System.out.println("in keyTyped");
          if( e.getKeyChar() !='E')
             return;
          Experiment E =new Experiment(getDataSet(), Transf );
          E.addOKListener( new ExperimentListener());
          }
       }
      class ExperimentListener  implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
       {
         axis1 = Transf.getAxis(0);
         axis2= Transf.getAxis(1);
         axis3= Transf.getAxis(2);
         setData( getDataSet(), state.get_int( ViewerState.CONTOUR_STYLE) );
        rpl_.draw();
        }
     }
  
    class Experiment extends JFrame
      { IAxesHandler Transf;
        DataSet ds;
        JTextField Vec1x,Vec1y,Vec1z,Vec2x,Vec2y,Vec2z,Bas1x,Bas1y,Bas1z,
                   Bas2x,Bas2y,Bas2z,Bas3x,Bas3y,Bas3z;
        JLabel  Distx,Disty,Distz;

        JButton Vec1,Vec2,Dist,Bas1,Bas2,Bas3,OK,Clear;
     
        public Experiment( DataSet ds,IAxesHandler Transf )
          { super("Experiment");
            this.Transf = Transf;
            this.ds = ds;
            getContentPane().setLayout( new GridLayout( 8,4));
            Vec1 = new JButton("Vector 1");
             Vec2 = new JButton("Vector 2");
             Dist = new JButton("Distance");
              Bas1 = new JButton("Dir 1");
              Bas2 = new JButton("Dir 2");
              Bas3= new JButton("Dir 3");
             Vec1x= new JTextField("0.0",10);
             Vec1y= new JTextField("0.0",10);
             Vec1z= new JTextField("0.0",10);
             Vec2x= new JTextField("0.0",10);
             Vec2y= new JTextField("0.0",10);
             Vec2z= new JTextField("0.0",10);
             Distx= new JLabel("          ");
             Disty= new JLabel("          ");
             Distz= new JLabel("          ");
             Bas1x= new JTextField("1.0",10);
             Bas1y= new JTextField("0.0",10);
             Bas1z= new JTextField("0.0",10);
             Bas2x= new JTextField("0.0",10);
             Bas2y= new JTextField("1.0",10);
             Bas2z= new JTextField("0.0",10);
             Bas3x= new JTextField("0.0",10);
             Bas3y= new JTextField("0.0",10);
             Bas3z= new JTextField("1.0",10);
             OK = new JButton("OK");
          getContentPane().add(Vec1);
            getContentPane().add(Vec1x);
            getContentPane().add(Vec1y);
            getContentPane().add(Vec1z);
            getContentPane().add(Vec2);
            getContentPane().add(Vec2x);
            getContentPane().add(Vec2y);
            getContentPane().add(Vec2z);
            getContentPane().add(Dist);
            getContentPane().add(Distx);
            getContentPane().add(Disty);
            getContentPane().add(Distz);
            getContentPane().add(Bas1);
            getContentPane().add(Bas1x);
            getContentPane().add(Bas1y);
            getContentPane().add(Bas1z);
            getContentPane().add(Bas2);
            getContentPane().add(Bas2x);
            getContentPane().add(Bas2y);
            getContentPane().add(Bas2z);
            getContentPane().add(Bas3);
            getContentPane().add(Bas3x);
            getContentPane().add(Bas3y);
            getContentPane().add(Bas3z);
            getContentPane().add(OK);
            getContentPane().add(new JLabel());
            getContentPane().add(new JLabel());


            MyActionListener ml = new MyActionListener();
            Vec1.addActionListener( ml);
            Vec2.addActionListener( ml);
            Bas1.addActionListener( ml);
            Bas2.addActionListener( ml);
            Bas3.addActionListener( ml);
            Dist.addActionListener( ml);
            OK.addActionListener( ml);
            this.setSize( 400,500);
            this.show();
            this.validate();
            
          }
       //Gives inverse of A transpose, sorry
       public float[][] inv( float[][] A)
          {float[][] Res = new float[3][3];
           Res[0][0]= A[1][1]*A[2][2]-A[2][1]*A[1][2];
           Res[0][1]= -A[1][0]*A[2][2]+A[2][0]*A[1][2];
           Res[0][2]= A[1][0]*A[2][1]-A[2][0]*A[1][1];
           float D = Res[0][0]*A[0][0]+Res[0][1]*A[0][1]+Res[0][2]*A[0][2];
           if( D==0) return null;
           Res[1][0]= -A[0][1]*A[2][2]+A[2][1]*A[0][2];
           Res[1][1]= A[0][0]*A[2][2]-A[2][0]*A[0][2];
           Res[1][2]= -A[0][0]*A[2][1]+A[2][0]*A[0][1];
           Res[2][0]= A[0][1]*A[1][2]-A[1][1]*A[0][2];
           Res[2][1]= -A[0][0]*A[1][2]+A[1][0]*A[0][2];
           Res[2][2]= A[0][0]*A[1][1]-A[1][0]*A[0][1];
           for( int i=0;i<3;i++)
            for( int j=0;j<3;j++)
              Res[i][j]=Res[i][j]/D;
           return Res;
          }

       Vector V = new Vector();
       public void addOKListener( ActionListener al)
         {V.addElement(al);
         }
       class MyActionListener implements ActionListener
         {
          float[] getQVec()
             {int Group = ds.getPointedAtIndex();
              float Time = ds.getPointedAtX();
              return QxQyQzAxesHandler.getQxQyQz( ds, Group, Time);
             }
          float getFloat( JTextField Tfield) throws NumberFormatException 
            {String S = Tfield.getText();
             return (new Float( S)).floatValue();
             }
          float[] getVect() throws NumberFormatException
             {float[] Res = new float[3];
              Res[0] = getFloat(Vec2x)-getFloat(Vec1x);
              Res[1] = getFloat(Vec2y)-getFloat(Vec1y);
              Res[2] = getFloat(Vec2z)-getFloat(Vec1z);

              return Res;
              }
          public void Unitize( float[] V)
            {float D = (float) java.lang.Math.sqrt( V[0]*V[0]+V[1]*V[1]+V[2]*V[2]);
             if( D == 0) return;
             if( D < .02) if( D > -.02) return;
             V[0]=V[0]/D;
             V[1]=V[1]/D;
             V[2]=V[2]/D;

             }
          public void Grammify( float[]V, float[]B1, float[] B2)
           {if( B1== null)
              return;
            float a1 = -(V[0]*B1[0]+V[1]*B1[1]+V[2]*B1[2]);
            //float a2=0;
            //if( B2 != null)
            //  a2 =-(V[0]*B2[0]+V[1]*B2[1]+V[2]*B2[2]);
            for( int i=0;i<3;i++)
              {V[i]+=a1*B1[i];
               //if( (a2 !=0)&&(B2!=null));
                // V[i]+=a2*B2[i];
               }
            }
          public void actionPerformed( ActionEvent evt)
           {//int Group=-1;
            //float time=Float.NaN;
            float[] Q = null;
            try{
            if( evt.getSource().equals( Vec1))
               {Q = getQVec();
                if( Q==null) return;
                Vec1x.setText(new Float(Q[0]).toString());
                Vec1y.setText(new Float(Q[1]).toString());
                Vec1z.setText(new Float(Q[2]).toString());
               }
            else if( evt.getSource().equals(Vec2))
              {Q = getQVec();
                if( Q==null) return;
                Vec2x.setText(new Float(Q[0]).toString());
                Vec2y.setText(new Float(Q[1]).toString());
                Vec2z.setText(new Float(Q[2]).toString());
               }
            else if( evt.getSource().equals( Dist))
               {try
                  { float x1= getFloat(Vec1x);
                    float y1= getFloat( Vec1y);
                    float z1= getFloat( Vec1z);
                    float x2= getFloat(Vec2x);
                    float y2= getFloat( Vec2y);
                    float z2= getFloat( Vec2z);
                    float D = (float)java.lang.Math.sqrt((x1-x2)*(x1-x2)+
                                (y1-y2)*(y1-y2)+(z1-z2)*(z1-z2));
                    Distx.setText( (new Float(D)).toString()); 
                  }
                catch( Exception u){Distx.setText("");}
           
                }
            else if( evt.getSource().equals(Bas1))
              {float[] S = getVect();
               Bas1x.setText(""+S[0]);
               Bas1y.setText(""+S[1]);
               Bas1z.setText(""+S[2]);
              }
            else if( evt.getSource().equals(Bas2))
              {float[] S = getVect();
               Bas2x.setText(""+S[0]);
               Bas2y.setText(""+S[1]);
               Bas2z.setText(""+S[2]);
              }
            else if( evt.getSource().equals(Bas3))
              {float[] S = getVect();
               Bas3x.setText(""+S[0]);
               Bas3y.setText(""+S[1]);
               Bas3z.setText(""+S[2]);
              }
            else if( evt.getSource().equals(OK))
              {float[][] A,B;
               A = new float[3][3];
               A[0][0]=getFloat(Bas1x);
               A[0][1]=getFloat(Bas1y);
               A[0][2]=getFloat(Bas1z);
               A[1][0]=getFloat(Bas2x);
               A[1][1]=getFloat(Bas2y);
               A[1][2]=getFloat(Bas2z);
               A[2][0]=getFloat(Bas3x);
               A[2][1]=getFloat(Bas3y);
               A[2][2]=getFloat(Bas3z);
               Unitize(A[0]);
               Grammify(A[1], A[0], null);
               Unitize(A[1]);
               Unitize(A[2]);
               B = inv(A);
               if( B== null)  return;
               Transf.setTransformation( B, null, null);
               for( int j=0;j< V.size();j++)
                 {((ActionListener)(V.elementAt(j))).actionPerformed(
                        new ActionEvent(this, ActionEvent.ACTION_PERFORMED,"OK"));
                  }
              }//OK
            
           }//try
         catch( NumberFormatException u){}
           
         }// actionPerformed
       }//My ActionListener
 
  }//Experiment
}//TQxQyQz class
