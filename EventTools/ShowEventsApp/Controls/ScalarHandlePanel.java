package EventTools.ShowEventsApp.Controls;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.lattice_calc;
import gov.anl.ipns.Util.File.TextSeparators;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Panels.StringListChoiceViewer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import EventTools.ShowEventsApp.Command.Commands;
import MessageTools.*;
import Operators.TOF_SCD.ReducedCellInfo;
import Operators.TOF_SCD.ReducedFormList;


public class ScalarHandlePanel implements IReceiveMessage
{
   public static String SHOW_CENTERINGS = "Show List";
   public static String APPLY_CENTERINGS ="Apply Selected";
   MessageCenter OrientMatMessageCenter;
   float[][]  UB;
   JPanel  panel;
   JCheckBox[] SymmetryChoices = new JCheckBox[7];
   
   String[]  ChoicesString={ReducedCellInfo.CUBIC,
                 ReducedCellInfo.TETRAGONAL,ReducedCellInfo.ORTHORHOMBIC,
                 ReducedCellInfo.RHOMBOHEDRAL, ReducedCellInfo.HEXAGONAL,
                 ReducedCellInfo.MONOCLINIC,ReducedCellInfo.TRICLINIC};
   
   JCheckBox[] Centerings = new JCheckBox[8];
   
   String[] CentChoiceStrings={ReducedCellInfo.P_CENTERED,
         ReducedCellInfo.F_CENTERED,
         ReducedCellInfo.I_CENTERED,
         ReducedCellInfo.C_CENTERED,"a!=b","a=b","b!=c","b=c"};
  
   JTextField Delta;
   JComboBox SortOn;
   String[] SortChoices ={"Symmetry" ,"Form Number" ,
                          "error", 
                         };
   String[] Choices;
   StringListChoiceViewer viewer;
   Vector<ReducedCellPlus> ScalarOpts ;
   

   static String Symm= ReducedCellInfo.CUBIC+";"+
                ReducedCellInfo.TETRAGONAL+";"+
                ReducedCellInfo.ORTHORHOMBIC+";"+
                ReducedCellInfo.RHOMBOHEDRAL+";"+
                ReducedCellInfo.HEXAGONAL+";"+
                ReducedCellInfo.MONOCLINIC+";"+
                ReducedCellInfo.TRICLINIC+";";
   
   static String Cent =ReducedCellInfo.P_CENTERED+";"+
                       ReducedCellInfo.I_CENTERED+";"+
                       ReducedCellInfo.F_CENTERED+";"+
                       ReducedCellInfo.C_CENTERED+";"+
                       ReducedCellInfo.R_CENTERED+";";
   
   
   public ScalarHandlePanel( MessageCenter OrientMatMessageCenter)
   {
      this.OrientMatMessageCenter = OrientMatMessageCenter;
      UB = null;
      ScalarOpts = null;
      Choices = new String[1];
      Choices[0] = " No Cell Types Available ";
      viewer = new StringListChoiceViewer( Choices, 15,25);
      BuildJPanel();
      OrientMatMessageCenter.addReceiver( this , Commands.SET_ORIENTATION_MATRIX );
      
   }
   
   
   public ScalarHandlePanel( float[][] UB)
   {
      this.UB = UB;
      OrientMatMessageCenter = null;
      Choices = new String[1];
      Choices[0] = " No Cell Types Available ";
      viewer = new StringListChoiceViewer( Choices, 15,25);
      BuildJPanel();
   }
   
   public JPanel getPanel()
   {
      return panel;
   }
 
   
   private void BuildJPanel()
   {
      panel = new JPanel();
      BoxLayout layout= new BoxLayout( panel, BoxLayout.Y_AXIS);
      panel.setLayout( layout );
      
      panel.add( BuildTopPanel());
      panel.add( viewer );
      panel.add( BuildBottomPanel());
      
   }
   
   private JPanel BuildTopPanel()
   {
      JPanel panel = new JPanel( new GridLayout(1,2));
      panel.add( BuildLeftTopPanel());
      panel.add( BuildRightTopPanel());
      
      return panel;
   }
   
   private JPanel BuildLeftTopPanel()
   {
      JPanel panel = new JPanel( new BorderLayout());
      panel.add( new JLabel("Restrict Shown to") ,BorderLayout.NORTH);
      JPanel choices = new JPanel( new GridLayout( 7,1));
      for( int i = 0; i< 7; i++)
      {
         SymmetryChoices[i]= new JCheckBox( ChoicesString[i],true );
         choices.add( SymmetryChoices[i]  );
      }
      panel.add( choices, BorderLayout.CENTER );
      return panel;
   }

   private JPanel BuildRightTopPanel()
   {
      JPanel panel = new JPanel( );
      BoxLayout bl = new BoxLayout( panel, BoxLayout.Y_AXIS);
      panel.setLayout( bl);
      
      JPanel deltaPanel = new JPanel( new GridLayout(1,2));
        deltaPanel.add( new JLabel("Max error"));
        Delta = new JTextField(".2");
        deltaPanel.add(  Delta );
      panel.add(deltaPanel);
      
      
      JPanel CenteringPanel = new JPanel(new GridLayout(4,2));
      CenteringPanel.setBorder( new TitledBorder( 
            new LineBorder( Color.blue,3),"Centering") );
      for( int i=0; i<4;i++)
      {
         Centerings[i] = new JCheckBox(CentChoiceStrings[i], true);
         CenteringPanel.add( Centerings[i] );
      }
      
      panel.add( CenteringPanel);
      
      JPanel SidesPanel = new JPanel(new GridLayout(4,2));
      SidesPanel.setBorder( new TitledBorder( 
            new LineBorder( Color.blue,3),"Sides") );
      for( int i=4; i< 8 ; i++ )
      {
         Centerings[i] = new JCheckBox(CentChoiceStrings[i], true);
         SidesPanel.add( Centerings[i] );
      }
      
      panel.add( SidesPanel);
      
         SortOn = new JComboBox( SortChoices );
         JPanel sortPanel = new JPanel( new BorderLayout());
         sortPanel.add(  new JLabel("Sort On"), BorderLayout.NORTH );
         sortPanel.add(  SortOn,BorderLayout.CENTER );
         
      panel.add( sortPanel);
      return panel;
   }
  
   /**
    * The angle closest to 90 degrees stays the same and the other two
    * angles are replaced by their supplementary angles.
    * 
    * @param latParams  The lattice parameters. These are changed
    * 
    * @return The angle(0,1,2 for alpha, beta and gamma) that stays the same
    *           or a negative number if not possible. Assume no two are equal.
    */
   private int flipLatticeAngle( double[] latParams)
   {
      if( latParams == null || latParams.length < 6)
         return -1;
      int sgn = 1;
      if( latParams[3] >= 90)
         sgn = -1;
      int res =0;
      double min = sgn*(90 -latParams[3]);
      if( sgn*(90-latParams[4]) < min)
      {
         res =1;
         min =sgn*(90-latParams[4]);
      }
      if( sgn*(90-latParams[5]) < min)
      {
         res =2;
         min =sgn*(90-latParams[2]);
      }
      
      for( int i=1;i<3;i++)
         latParams[3+(res+i)%3]= 180-latParams[3+(res+i)%3];
      return res;
   }
   
   private static double[][] NewUB( ReducedCellPlus RedCell, float[][]UB)
   {
      double[][] transf = RedCell.redCell.getTransformation( );
      if( RedCell.flipUBRow >=0)
      {
         double[][] ident = new double[3][3];
        ident[0][0]=1;
        ident[0][1]=0;
        ident[0][2]=0;
        ident[1][0]=0;
        ident[1][1]=1;
        ident[1][2]=0;
        ident[2][0]=0;
        ident[2][1]=0;
        ident[2][2]=1;
        
         int k = RedCell.flipUBRow;
         for( int i=1; i<=2; i++)
            ident[(k+i)%3][(k+i)%3]=-1;
         transf = LinearAlgebra.mult( transf , ident );
      }
      
     return  LinearAlgebra.mult(  LinearAlgebra.float2double( UB ) , 
            LinearAlgebra.getInverse( transf ) );
   }
   
   //make sure current UB is saved
  private void showChoices( )
  {
    double[] latParams1 = lattice_calc.LatticeParamsOfUB(LinearAlgebra.float2double( UB ));
   
    double delta;
    try
    {
       delta = Double.parseDouble( Delta.getText().trim() );
    }catch( Exception s)
    {
       delta = .2;
    }
    double[] latParams2= new double[6];
    System.arraycopy( latParams1 , 0 , latParams2 , 0 , 6);


    int side = flipLatticeAngle( latParams2);
    
    ScalarOpts = new Vector<ReducedCellPlus>();
    ReducedCellInfo SrcRedCell1 = new ReducedCellInfo( 0, latParams1[0],
          latParams1[1],latParams1[2],latParams1[3],latParams1[4],
          latParams1[5]);
    ReducedCellInfo SrcRedCell2 = new ReducedCellInfo( 0, latParams2[0],
          latParams2[1],latParams2[2],latParams2[3],latParams2[4],
          latParams2[5]);
    for( int i=1; i < 45;i++)
    {
       ReducedCellInfo redCell = new ReducedCellInfo( i, latParams1[0],
             latParams1[1],latParams1[2],latParams1[3],latParams1[4],
             latParams1[5]);
       double dist = redCell.distance( SrcRedCell1 );
       if( dist < delta)
          ScalarOpts.add(  new ReducedCellPlus(redCell,-1,dist) );
       
       redCell = new ReducedCellInfo( i, latParams2[0],
             latParams2[1],latParams2[2],latParams2[3],latParams2[4],
             latParams2[5]);
       dist = redCell.distance( SrcRedCell2 );
       if( dist < delta)
          ScalarOpts.add(  new ReducedCellPlus(redCell,side,dist) );
       
    }
  
    for( int i=0; i < SymmetryChoices.length; i++)
       if( !SymmetryChoices[i].isSelected( ))
           FilterOutSymmetry( ScalarOpts, ChoicesString[i]);
    
    for( int i=0; i < Centerings.length; i++)
       if( !Centerings[i].isSelected())
          FilterOutCenterings( ScalarOpts, i);
    
    if( ScalarOpts == null || ScalarOpts.size() <1)
       return;
    
    ReducedCellPlus[] RCells = ScalarOpts.toArray( new ReducedCellPlus[0] );
    Comparator Comp = null;
    int sortChoice = SortOn.getSelectedIndex( );
    if( sortChoice ==0)
       Comp = new SymmetrySort();
    else if( sortChoice == 1)
       Comp = new FormSort();
    else if( sortChoice == 2)
       Comp = new distSort();
    if( Comp != null)
      Arrays.sort( RCells, Comp );
    
    ScalarOpts = new Vector<ReducedCellPlus>(RCells.length);
    for( int i=0; i< RCells.length; i++)
       ScalarOpts.add( RCells[i] );
    
    String[] ScalarOptsStrings = new String[ ScalarOpts.size() ];
    for( int i=0; i< ScalarOpts.size( ); i++)
       ScalarOptsStrings[i] = MakeString( RCells[i]);
    
   viewer.setNewStringList( ScalarOptsStrings );
    
       
  }
   
   private JPanel BuildBottomPanel()
   {
    
      JButton Apply = new JButton( APPLY_CENTERINGS );
      Apply.addActionListener(  new ThisActionListener(0) );
      JButton Show = new JButton( SHOW_CENTERINGS );

      Apply.addActionListener(  new ThisActionListener(0) );
      Show.addActionListener(  new ThisActionListener(0) );
      JPanel panel = new JPanel();
      BoxLayout layout = new BoxLayout( panel, BoxLayout.X_AXIS);
      panel.setLayout(  layout );
      panel.add(  Box.createHorizontalGlue( ) );
      panel.add( Show);
      panel.add( Apply);
      panel.add( Box.createHorizontalGlue( ));
      return panel;
   }

   
   @Override
   public boolean receive(Message message)
   {
      UB = (float[][])message.getValue();
      UB = LinearAlgebra.getTranspose( UB );
      if( UB == null)
      {

         Choices = new String[1];
         Choices[0] = "No Orientation Matrix ";
         viewer.setNewStringList( Choices );
         return false;
      }
     showChoices();
      return false;
   }


   private void FilterOutSymmetry( Vector<ReducedCellPlus>ScalarOpts, String symm)
   {
      for( int i=ScalarOpts.size()-1; i>=0; i--)
      {
         ReducedCellPlus red = ScalarOpts.elementAt( i );
         if( red.redCell.getCellType( ).equals(  symm ))
            ScalarOpts.remove( i );
      }
   }
   

   private void FilterOutCenterings( Vector<ReducedCellPlus>ScalarOpts, int cent)
   {
      for( int i=ScalarOpts.size()-1; i>=0; i--)
      {
         ReducedCellPlus red = ScalarOpts.elementAt( i );
         if( cent < 4)
         { 
            if( red.redCell.getCentering( ).equals( CentChoiceStrings[cent] ))
               ScalarOpts.remove( i );
         }else 
         {
            int lineNum = red.redCell.getFormNum( );
            boolean remove = false;
            if( cent == 4 && lineNum > 17) remove = true;
            else if( cent ==5 && lineNum <=17) remove =true;
            else if( cent == 6 && lineNum >8 && lineNum <=17 )
               remove = true;
            else if( cent ==6 && lineNum>25) remove = true;
            else if( cent ==7 && lineNum <=8) remove =true;
            else if( cent ==7 && lineNum>17 && lineNum <=25) remove =true;
            if( remove)
               ScalarOpts.remove( i );
         }
               
      }
   }
   
   private String MakeString( ReducedCellPlus RedCell)
   {
      TextSeparators ut = new TextSeparators("plain");
      String Res = ut.start( );
      double[][] UB1= NewUB( RedCell , UB);
      Res +=String.format("%-14s",RedCell.redCell.getCellType( ))+"  "+
               RedCell.redCell.getCentering( )+"  ";
      int lineNum = RedCell.redCell.getFormNum( );
      if( lineNum <=25)
         Res +="a=b";
      else
         Res +="a <>b";
      if( lineNum <9 || (lineNum>17 && lineNum <=25))
         Res +="=c";
      else
         Res +="<>c";
      
      Res +="      error:"+String.format( "%6.4f" ,RedCell.distance) +ut.eol( );
      Res +="       Form Num:"+RedCell.redCell.getFormNum( )+ut.eol( );
      Res +=ut.eol( );
      
  
      double[] LatticeParams =  lattice_calc.LatticeParamsOfUB( UB1 );
      Res +=ut.table()+ut.row();
      for( int i=0; i<6;i++)
      {
         Res += String.format("%8.5f",LatticeParams[i]);
         if( i+1 < 6)
            Res +=ut.col( );
         else
            Res +=ut.rowEnd( );
      }

      Res += ut.tableEnd( );
      
      Res +=ut.table( );
      for( int i=0;i<3;i++)
      { Res +=ut.row( );
        for( int j=0; j<3;j++)
        {
           Res += String.format( "%8.5f" , UB1[j][i] );
           if( j+1 <3)
              Res +=ut.col( );
        }
        Res +=ut.rowEnd();
         
      }
      Res +=ut.tableEnd( );
  
      
      Res +=ut.end();
      return Res;
      
      
   }
   /**
    * @param args
    */
   public static void main(String[] args)
   {

      String filename = "C:/ISAW/SampleRuns/SNS/SNAP/WSF/235_46/quartzTest.mat";
      float[][] UB = (float[][])Operators.TOF_SCD.IndexJ.readOrient( filename );
      ScalarHandlePanel panel = new ScalarHandlePanel( UB );
      JFrame jf = new JFrame("Test");
      jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      jf.getContentPane( ).setLayout( new GridLayout(1,1));
      jf.getContentPane( ).add( panel.getPanel());
      jf.setSize( 400,600 );
      WindowShower.show( jf );

   }
 
   class ThisActionListener implements ActionListener
   {
      int category;
      public ThisActionListener( int category )
      {
         this.category = category;
      }
      @Override
      public void actionPerformed(ActionEvent e)
      {

         String command = e.getActionCommand( );
         if( command == SHOW_CENTERINGS)
         {
          if( OrientMatMessageCenter != null)
             OrientMatMessageCenter.send( 
                   new Message(Commands.GET_ORIENTATION_MATRIX, null,false) );
          else if( UB == null)
             return;
          else
          {
             showChoices();
          }
         }else if( command == APPLY_CENTERINGS)
         {
            int k =viewer.getSelectedChoice( );
            if( k < 0)
               k =viewer.getLastViewedChoice( );
            if( k < 0 || k >= ScalarOpts.size())
            {
               JOptionPane.showMessageDialog( null , "Cannot Determine desired Choice" );
               return;
            }
            double[][] UB1= NewUB( ScalarOpts.elementAt( k ), UB);
            UB = LinearAlgebra.double2float( UB1 );
            if( OrientMatMessageCenter != null)
               OrientMatMessageCenter.send(  
                     new Message( Commands.SET_ORIENTATION_MATRIX,
                           LinearAlgebra.getTranspose( UB ),true));
            
         }
         
      }
      
   }
   class ReducedCellPlus 
   {
      int flipUBRow;
      double distance;
      ReducedCellInfo redCell;
      public ReducedCellPlus( ReducedCellInfo redCell, int flipUBRow,double dist)
      {
         this.redCell = redCell;
         this.flipUBRow = flipUBRow;
         this.distance = dist;
      }
      
    
   }
   
   class SymmetrySort implements Comparator<ReducedCellPlus>
   {
      @Override
      public int compare(ReducedCellPlus arg0, ReducedCellPlus arg1)
      {

         if( arg0 == null)
            if( arg1 == null)
               return 0;
            else
               return  -1;
         else if( arg1 == null)
            return 1;
               
         int i1 =Symm.indexOf( arg0.redCell.getCellType( ));       
         int i2 =Symm.indexOf( arg1.redCell.getCellType( ));
         if( i1 < 0)
            if( i2 < 0)
               return 0;
            else
               return -1;
         else if( i2 < 0)
            return 1;
         if( i1 < i2)
            return -1;
         if( i1 > i2)
            return 1;
         i1= Cent.indexOf(  arg0.redCell.getCentering( ) );
         i2= Cent.indexOf(  arg1.redCell.getCentering( ) );
         if( i1 < 0)
            if( i2 < 0)
               return 0;
            else
               return -1;
         else if( i2 < 0)
            return 1;
         if( i1 < i2)
            return -1;
         if( i1 > i2)
            return 1;
         return 0;
      }

      
   }
   
   
   class FormSort implements Comparator<ReducedCellPlus>
   {
      @Override
      public int compare(ReducedCellPlus arg0, ReducedCellPlus arg1)
      {

         if( arg0 == null)
            if( arg1 == null)
               return 0;
            else
               return  -1;
         else if( arg1 == null)
            return 1;
               
         int i1 = arg0.redCell.getFormNum( );       
         int i2  =arg1.redCell.getFormNum( );
         if( i1 < 0)
            if( i2 < 0)
               return 0;
            else
               return -1;
         else if( i2 < 0)
            return 1;
         if( i1 < i2)
            return -1;
         if( i1 > i2)
            return 1;
         return 0;
      }

      
   }
   
   
   class distSort implements Comparator<ReducedCellPlus>
   {
      @Override
      public int compare(ReducedCellPlus arg0, ReducedCellPlus arg1)
      {

         if( arg0 == null)
            if( arg1 == null)
               return 0;
            else
               return  -1;
         else if( arg1 == null)
            return 1;
               
         double i1 = arg0.distance;       
         double i2  =arg1.distance;
         if( i1 < 0)
            if( i2 < 0)
               return 0;
            else
               return -1;
         else if( i2 < 0)
            return 1;
         if( i1 < i2)
            return -1;
         if( i1 > i2)
            return 1;
         return 0;
      }

      
   }
}
