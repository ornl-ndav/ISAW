
/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2001/07/25 17:39:08  neffk
 * fixed method getParameter() by removing some errors introduced from
 * copying a different operator.
 *
 * Revision 1.1  2001/07/11 16:25:08  neffk
 * encapsulates an Attribute name JComboBox and a JTextField.  this GUI
 * object is provided specifically for input of intervals.
 *
 */

package DataSetTools.components.ParametersGUI;


import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JTextField;
import javax.swing.JPanel;
import DataSetTools.operator.Parameter;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.util.SpecialString;
import javax.swing.JComboBox;

public class JIntervalParameterGUI
  extends JParameterGUI
{
  private JPanel     segment;
  private JTextField text;
  private JComboBox  combobox;
  private static final String SEPERATOR = "";

  public JIntervalParameterGUI( Parameter     parameter, 
                                AttributeList attr_list )
  { 
    super( parameter );

                                       //create a combo box with the
                                       //attributes passed in as parameters
    combobox = new JComboBox();
    combobox.setEditable( false );
    for(int i = 0; i<attr_list.getNum_attributes(); i++)
      combobox.addItem(  attr_list.getAttribute(i).getName()  );

                                      //create and size the text box
                                      //used for entering intervals
    text = new JTextField();
    text.setPreferredSize(  new Dimension( 100, 10 )  ); 

                                      //set up JPanel to be returned
                                      //to the JParametersDialog
    segment = new JPanel();
    segment.setLayout(  new GridLayout( 1, 2 )  ); 
    segment.add( combobox );
    segment.add( text );
  }


  public JPanel getGUISegment()
  {
    return segment;
  }


  /**
   * get the value that the user entered.
   */
  public Parameter getParameter()
  {
    try
    {
      String str = (String)( combobox.getSelectedItem() );
      str += SEPERATOR + text.getText();
      parameter.setValue( str );
    }
    catch( Exception s)
    {
      System.out.println( s );
    }

    return parameter;
  }
}
