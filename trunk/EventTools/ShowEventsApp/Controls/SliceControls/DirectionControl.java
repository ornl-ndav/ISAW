
package EventTools.ShowEventsApp.Controls.SliceControls;

import java.awt.*;
import javax.swing.*;
import gov.anl.ipns.ViewTools.UI.Vector3D_UI;


import gov.anl.ipns.MathTools.Geometry.Vector3D;


public class DirectionControl extends JPanel
{
  private String       title;
  private Vector3D_UI  VectorUI;
  private JTextField   StepSizeTF;
  private JTextField   NumStepsTF;


  public DirectionControl( String title, Vector3D initial_vec )
  {
    this.title = title;

    VectorUI   = new Vector3D_UI( "", initial_vec );
    StepSizeTF = new JTextField("0.04");
    NumStepsTF = new JTextField("250");

    VectorUI.setHorizontalAlignment( JTextField.RIGHT );
    StepSizeTF.setHorizontalAlignment( JTextField.RIGHT );
    NumStepsTF.setHorizontalAlignment( JTextField.RIGHT );

    setLayout( new GridLayout(3,2) );

    add( new JLabel(title) );
    add( VectorUI );

    add( new JLabel("Step Size") );
    add( StepSizeTF );

    add( new JLabel("Number of Steps") );    
    add( NumStepsTF );
  }


  public String getTitle()
  {
    return title;
  }


  public Vector3D getDirection()
  {
    return VectorUI.getVector();
  }


  /**
   *  Get the value entered for the step size.  If a valid value is not
   *  present, return 0;
   */
  public double getStepSize()
  {
    double value = 0.0;
    try
    {
      value = Double.parseDouble( StepSizeTF.getText().trim() ); 
    }
    catch ( Exception ex )
    {
      System.out.println("ERROR parsing step size in " + title );
    }
    return value;
  }


  /**
   *  Get the value entered for the number of steps.  If a valid value is not
   *  present, return 0;
   */
  public int getNumSteps()
  {
    int value = 0;
    try
    {
      value = Integer.parseInt( NumStepsTF.getText().trim() );
    }
    catch ( Exception ex )
    {
      System.out.println("ERROR parsing num steps " + title );
    }

    if ( value < 0 )     // negative number of steps not allowed
      value = 0;

    return value;
  }


  public static void main( String args[] )
  {
    JFrame test_frame = new JFrame("Direction Control");
    test_frame.setBounds( 0, 0, 300, 200 );
    test_frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
    
    Vector3D initial_vec = new Vector3D( 1, 0, 0 );
    DirectionControl control = new DirectionControl( "Direction 1( 'slice' )",
                                                      initial_vec );
    test_frame.add( control );
    test_frame.setVisible( true );

    System.out.println( control.getDirection() );
    System.out.println( control.getStepSize() );
    System.out.println( control.getNumSteps() );
  }


}
