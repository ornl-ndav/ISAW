/**
 * 
 */

package DataSetTools.trial;

import java.awt.event.*;
import java.util.ArrayList;

import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Panels.Graph.GraphJPanel;

import gov.anl.ipns.ViewTools.Panels.ThreeD.*;
import gov.anl.ipns.ViewTools.UI.VectorReadout;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.awt.*;
import javax.swing.plaf.basic.BasicArrowButton;



/**
 * This class produces a JFrame with controls to tweak the direction in a
 * VectorReadout reflecting the change in the VectorReadout and giving a quick
 * display
 * 
 * @author Ruth
 * 
 */
public class TweakerFrame extends JFrame implements ActionListener {



   ThreeD_JPanel Space;

   VectorReadout Direction1,
                 Direction2,
                 Direction3,
                 SelDirection;
   

   JPanel        panel;

   JPanel        ProjPanel;

   ArrayList     StepSizes;

   GraphJPanel   Data , FFT;

   JTextField    Percent;

   JSpinner      Perc;

   float[]       PercentWithin = {
                                 0f , 0f , 0f , 0f , 0f
                               };


   public TweakerFrame( ThreeD_JPanel vec_Q_space, VectorReadout aStar,
            VectorReadout bStar,VectorReadout cStar ) {

      super( "Tweaker" );
      Space = vec_Q_space;
      Direction1 = aStar;
      Direction2 = bStar;
      Direction3 = cStar;
      SelDirection = Direction1;
      if( aStar != null)
         aStar.addActionListener( this );
      if( bStar != null)
         bStar.addActionListener( this );

      if( cStar != null)
         cStar.addActionListener( this );


      Dimension D = getToolkit().getScreenSize();
      D.setSize( D.getWidth() / 4 , D.getHeight() / 3 );
      Dimension D2 = new Dimension((int) D.getWidth()-4, (int)D.getHeight()/7);
      
      panel = new JPanel();
      //BoxLayout b = new BoxLayout( panel , BoxLayout.Y_AXIS );
      //panel.setLayout( b );
      panel.setLayout( new GridLayout(7,1));
      getContentPane().setLayout( new GridLayout( 1 , 1 ) );      
      setSize( D );
      getContentPane().add( panel );
      
      ButtonGroup BG = new ButtonGroup();
      JPanel DirChoose = new JPanel( new GridLayout( 1,4));
      DirChoose.add( new JLabel("Select Plane "));
      JCheckBox BB = new JCheckBox("a*b*");
      BB.addActionListener( new DirChoiceListener( 1));
      BG.add( BB );
      BB.setSelected( true );
      DirChoose.add(BB); 
      BB = new JCheckBox("b*c*");
      BB.addActionListener( new DirChoiceListener( 2));
      BG.add( BB );
      DirChoose.add(BB); 
      BB = new JCheckBox("a*c*");
      BB.addActionListener( new DirChoiceListener( 3));
      BG.add( BB );
      DirChoose.add(BB);
     
      panel.add( DirChoose);
       StepSizes = new ArrayList();
       StepSizes.add( 0 , .5f );
       StepSizes.add( 0 , .1f );
       StepSizes.add( 0 , .05f );
       StepSizes.add( 0 , .02f );
       StepSizes.add( 0 , .01f );
       StepSizes.add( 0 , .005f );
       StepSizes.add( 0 , .002f );
       StepSizes.add( 0 , .001f );
      panel.add( SetUpLine( "Normal.x" ) );
      panel.add( SetUpLine( "Normal.y" ) );
      panel.add( SetUpLine( "Normal.len" ) );


      Data = new GraphJPanel();
      //Data.setLayout( new java.awt.GridLayout(1,1) );
      //Data.autoX_bounds();
      //Data.autoY_bounds();
      Data.setBorder( new TitledBorder( new LineBorder( java.awt.Color.black ) ,
               "Data Graph" ) );
      Data.setBounds( new Rectangle(D2) );
      panel.add( Data );
 
      FFT = new GraphJPanel();
     // FFT.autoX_bounds();
      //FFT.autoY_bounds();
      FFT.setBorder( new TitledBorder( new LineBorder( java.awt.Color.black ) ,
               "FFT Graph" ) );
      FFT.setLayout( new java.awt.GridLayout(1,1) );
      FFT.setBounds(  new Rectangle( D2 ) );
      panel.add( FFT );

      Perc = new JSpinner( new SpinnerNumberModel( 20. , 10. , 40. , 10. ) );
      Perc.addChangeListener( new PercentChangeListener() );
      JPanel Fit = new JPanel();
      BoxLayout bFit = new BoxLayout( Fit , BoxLayout.X_AXIS );
      Fit.setLayout( bFit );
      Fit.add( new JLabel( "Fraction with index within " ) );
      Fit.add( Perc );
      Fit.add( new JLabel( "% of an integer is " ) );
      Percent = new JTextField( 6 );
      Fit.add( Percent );
      panel.add( Fit );
      SetUpData();

     

      getContentPane().setLayout( new GridLayout( 1 , 1 ) );      
      setSize( D );
      invalidate();
      doLayout();
      WindowShower.show( this );


   }

  
   private JPanel SetUpLine( String label ) {

      JPanel Res = new JPanel();
      JSpinner Step = new JSpinner( new SpinnerListModel( StepSizes ) );
      Step.setValue( .005f);
      BoxLayout BL = new BoxLayout( Res , BoxLayout.X_AXIS );
      Res.setLayout( BL );
      Res.add( new JLabel( label ) );
      
      BasicArrowButton left = new BasicArrowButton( SwingConstants.WEST );
      Res.add( left );
      left.addActionListener( new dirListener( label , - 1 , Step ) );
      
      left = new BasicArrowButton( SwingConstants.EAST );
      Res.add( left );
      left.addActionListener( new dirListener( label , 1 , Step ) );
      
      Res.add( left );
      Res.add( Step );
      
      return Res;


   }

   public void actionPerformed( ActionEvent evt){
      SetUpData();
   }
   
   public static Vector3D NormalUnitDir( Vector3D selDir, Vector3D dir1,  
            Vector3D dir2, Vector3D dir3){
      
      if( selDir == null || dir1 == null || dir2 == null
                  || dir2 == null)
         return null;
      
      Vector3D[] dirs = new Vector3D[2];
      int j=0;
      
      if( !selDir .equals( dir1)){
         dirs[j] = dir1;
         j++;
      }
      
      if( !selDir .equals(dir2)){
         dirs[j] = dir2;
         j++;
      }
      
      if( !selDir.equals(dir3)){
         if( j > 1)
            return null;
         dirs[j] = dir3;
         j++;
      }
      
      if( j < 2 || dirs[0] == null || dirs[1] == null)
         return null;
      
      Vector3D Res = dirs[0];
      Res.cross( dirs[1] );
      float L = Res.length();
      if( L <= 0 )
         return null;
      Res.multiply( 1/L );
      return Res;
   }
   
   public static Vector3D getVec( VectorReadout VRead){
      if( VRead == null)
         return null;
      return VRead.getVector();
   }
   
   
   private  Vector3D getNormalUnitDir( VectorReadout SelDir){
      
      return NormalUnitDir( getVec(SelDir), getVec(Direction1),
               getVec( Direction2), getVec(Direction3));

   }
   
   /**
    * Call this method if any changes should alter what is showing on this
    * Tweaker Frame. Changes- Select a new a*,b etc., or remove some Peaks
    * 
    */
   private void SetUpData( ) {

      if( SelDirection == null || Space == null || Direction1 == null
               || Direction2 == null || Direction3 == null)
         return;
      
      Vector3D dir = getNormalUnitDir( SelDirection);
      if( dir == null )
         return;

      float L = dir.dot( SelDirection.getVector() );
      dir.multiply( L );
      float dirLength = dir.length();
      if( dirLength <= 0 )
         return;
      Vector3D unitDir = new Vector3D( dir );
      unitDir.multiply( 1 / ( dirLength * dirLength ) );
      float[] U = unitDir.get();
      Object[] QVec = Space.getObjects( "Peaks" );
      if( QVec == null )
         return;
      
      float[] proj = new float[ QVec.length ];
      java.util.Arrays.fill( PercentWithin , 0f );
      
      int j = 0;
      float startf = Float.NaN;
      float endf = Float.NaN;
      for( int i = 0 ; i < QVec.length ; i++ ) {

         float[] Vec1 = ( (Polymarker) QVec[ i ] ).position().get();
         if( Vec1 == null || Vec1.length < 3 ) {


         }
         else {
            
            float P = Vec1[ 0 ] * U[ 0 ] + Vec1[ 1 ] * U[ 1 ] + Vec1[ 2 ]
                     * U[ 2 ];
            float X = (float) Math.min( Math.abs( Math.floor( P ) - P ) , Math
                     .abs( Math.floor( P ) + 1 - P ) ) * 10;

            PercentWithin[ (int) X ]++ ;
            proj[ j ] = P;
            if( !Float.isNaN( P ))
            if( j==0 )
               startf = endf = P;
            else if( P < startf)
               startf = P;
            else if ( P > endf)
               endf = P;
            j++ ;
            
         }
      }

      for( int i = 1 ; i < PercentWithin.length ; i++ ){
         PercentWithin[ i ] += PercentWithin[ i - 1 ];
         PercentWithin[i-1]=PercentWithin[i-1]/j*100;
      }
      
      PercentWithin[PercentWithin.length-1]=
                      PercentWithin[PercentWithin.length-1]/j*100;
      
      SetPercents();

      float[] proj1 = proj;
      //System.arraycopy( proj , 0 , proj1 , 0 , j );
      //java.util.Arrays.sort( proj1 );
      int start = (int)Math.floor(  startf );// Check for NaN
      int last = (int)Math.floor(  endf);
      if( last < endf )
         last++ ;
      
      float[] dat = new float[ ( last - start + 1 ) * 20 ];
      java.util.Arrays.fill( dat , 0f );
      
      for( int i = 0 ; i < proj1.length ; i++ )
         if( ! Float.isNaN( proj1[ i ] ) )
            dat[ (int) ( ( proj1[ i ] - start ) * 20 ) ] += 1;

      float[] xvals = new float[ dat.length ];
      
      for( int i = 0 ; i < dat.length ; i++ )
         xvals[ i ] = start + i / 20f;
      Data.autoX_bounds();
      Data.setY_bounds( 0f , proj1.length*20f/dat.length );
      Data.setData( xvals , dat );

      Data.repaint();


      // ------------------------ FFT --------.------------
      float[] fftdat = new float[ 2 * dat.length ];
      java.util.Arrays.fill( fftdat , 0f );
      
      for( int i = 0 ; i < dat.length ; i++ )
         fftdat[ 2 * i ] = dat[ i ];
      
      jnt.FFT.ComplexFloatFFT_Mixed fft = new jnt.FFT.ComplexFloatFFT_Mixed(
               dat.length );
      
      fft.transform( fftdat );
      
      float[] fftdat2 = new float[ dat.length ];
      for( int i = 0 ; i < fftdat2.length ; i++ )
         fftdat2[ i ] = (float) Math.sqrt( fftdat[ 2 * i ] * fftdat[ 2 * i ]
                  + fftdat[ 2 * i + 1 ] * fftdat[ 2 * i + 1 ] );

      float[] FFTx = new float[ fftdat2.length ];
      float delta = xvals[ 1 ] - xvals[ 0 ];
      for( int i = 0 ; i < FFTx.length ; i++ )
         FFTx[ i ] = i * delta;
      FFT.autoX_bounds();
      FFT.autoY_bounds();
      FFT.setData( FFTx , fftdat2 );

      FFT.repaint();

   }


   protected void SetPercents() {

      float F = ( (SpinnerNumberModel) ( Perc.getModel() ) ).getNumber()
               .floatValue();
      
      Percent.setText( PercentWithin[ (int) ( F / 10 ) - 1 ] + "%" );
      
   }

   
   
   class dirListener implements ActionListener {



      String   lab;

      int      sgn;

      int      index;

      JSpinner Step;

     

      public dirListener( String label, int dir, JSpinner Step ) {

         lab = label;
         this.Step = Step;
         if( dir < 0 )
            sgn = - 1;
         else
            sgn = 1;
         
        
         if( lab.indexOf( ".x" ) >= 0 )
            index = 0;
         else if( lab.indexOf( ".y" ) >= 0 )
            index = 1;
         else
            index = 2;


      }


      public void actionPerformed( ActionEvent evt ) {

         
         Vector3D  unitDir = TweakerFrame.NormalUnitDir( 
                  TweakerFrame.getVec( SelDirection ), 
                  TweakerFrame.getVec( Direction1 ), 
                  TweakerFrame.getVec(Direction2 ), 
                  TweakerFrame.getVec( Direction3 ) 
                  );
         if( unitDir == null)
            return;
         float NormalLength = SelDirection.getVector().dot( unitDir );
         Vector3D SelOffset = new Vector3D( unitDir);
         SelOffset.multiply( NormalLength ); 
                                 //unitDir*NormalLength +SelOffSet = SelDirection(orig)
         SelOffset.subtract( SelDirection.getVector() );
         SelOffset.multiply( -1f );
         
         float delt = ( (Float) Step.getValue() ).floatValue();
         if( index < 2 ) {
            float[] g = unitDir.get();

            g[ index ] += delt * sgn;
            if( 1 - g[ 0 ] * g[ 0 ] - g[ 1 ] * g[ 1 ] < 0 )
               return;
            g[ 2 ] = (float) Math.sqrt( 1 - g[ 0 ] * g[ 0 ] - g[ 1 ] * g[ 1 ] );
            unitDir = new Vector3D( g );

         }
         else {
            
            NormalLength += delt * sgn;
            if( NormalLength == 0 )
               return;
         }

         unitDir.multiply( NormalLength );
         //Now change project non-Selected to new plane( adjust length) 
         //              and adjust Selected Direction
         
         if( Direction1 != SelDirection)
            AdjustDirection( Direction1, unitDir);        
         if( Direction2 != SelDirection)
            AdjustDirection( Direction2, unitDir);        
         if( Direction3 != SelDirection)
            AdjustDirection( Direction3, unitDir);
         AdjustNormal( SelDirection, SelOffset, unitDir);
         
         SetUpData();      
         

      }
      
      private void AdjustDirection( VectorReadout Dir, Vector3D NormalDir){
         Vector3D V = Dir.getVector();
         if( V == null)
            return;
         float L_orig = V.length();
         float K = V.dot( NormalDir )/NormalDir.length();
         Vector3D Res = new Vector3D(NormalDir);
         Res.multiply(  -K );
         Res.add( V);
         if( Res.length() <=0)
            return;
         Res.multiply( L_orig/Res.length());
         Dir.setVector( Res );
      }
      
      //Adjust Seldirection so in same plane as orig and dot product with
      // unit dir matches
      private void AdjustNormal( VectorReadout SelDir, Vector3D offset, 
                                            Vector3D NormalDir){
         
         if( SelDir == null || offset == null || NormalDir == null)
            return;
         
         Vector3D Res = new Vector3D( NormalDir);
         Res.add(  offset );
         float L =NormalDir.length();
         if( L <= 0)
            return;
         float dot = Res.dot( NormalDir )/L;
         if( dot !=0)
              Res.multiply(  L/dot );
         else
            return;
         SelDir.setVector( Res );
         
         
         
         
      }
   }


   class PercentChangeListener implements ChangeListener {



      public void stateChanged( ChangeEvent evt ) {

         if( ! ( evt.getSource() instanceof JSpinner ) )
            return;

         SetPercents();


      }

   }
   class DirChoiceListener implements ActionListener{
      
      VectorReadout Direction;
      public DirChoiceListener( int i){
         if( i == 1)
            Direction = Direction1;
         else if( i ==2)
            Direction = Direction2;
         else
            Direction = Direction3;
         
      }
     public void actionPerformed( ActionEvent evt){
        SelDirection = Direction;
        SetUpData();
     }
   }
}
