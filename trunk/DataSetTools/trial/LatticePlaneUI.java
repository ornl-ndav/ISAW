/*
 * File:  LatticePlaneUI.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.6  2004/08/11 05:18:20  dennis
 * Added control to turn filter on and off.
 * Now normalizes the plane normal when a new normal is set.
 * Replaced the d_spacing label with a text entry widget, so the
 * user can specify a different value for the d_spacing corresponding
 * to a family of planes in reciprocal space.
 *
 * Revision 1.5  2004/07/28 15:44:39  dennis
 * Made default value (0,0,0).
 *
 * Revision 1.4  2004/03/15 06:10:53  dennis
 * Removed unused import statements.
 *
 * Revision 1.3  2004/03/15 03:28:43  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.2  2003/06/05 14:36:27  dennis
 * Now shows more significant digits in the normal, sigma and
 * d_spacing readouts.
 *
 * Revision 1.1  2003/06/03 17:02:19  dennis
 * UI for displaying information about planes in the reciprocal lattice.
 *
 */
package DataSetTools.trial;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.ViewTools.UI.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class LatticePlaneUI extends ActiveJPanel
{
  public static final String USER_SET = "User->";
  public static final String FFT_SET  = "FFT->";
  public static final String FILTER_OFF = "Filter OFF";
  public static final String FILTER_ON  = "Filter ON";

  private JLabel       normal_label;
  private JLabel       sigma_label;
  private JButton      user_set;
  private JButton      fft_set;
  private JButton      filter;
  private TextValueUI  miller_value;  
  private TextValueUI  d_ui;
  private String       title;

  private float        normal[]  = { 0, 0, 0, 1 };
  private float        sigma     = 0;
  private float        d_spacing = 0;
  private boolean      filter_flag = false;

  public LatticePlaneUI( String title ) 
  {
     this.title = title;

     String border_title = "Constant " + title + " Planes";

     TitledBorder border =
                  new TitledBorder(LineBorder.createBlackLineBorder(), 
                                   border_title );
     border.setTitleFont( FontUtil.BORDER_FONT );
     setBorder( border );
                                                         // make the controls 
     normal_label = new JLabel( "Normal:" );
     d_ui         = new TextValueUI( " d ", 0 );
     sigma_label  = new JLabel( " " + FontUtil.SIGMA + ":   " );
     user_set     = new JButton(USER_SET);
     fft_set      = new JButton(FFT_SET);
     filter       = new JButton(FILTER_OFF);
     miller_value = new TextValueUI( title, 0 );
                                                         // make the panels
     Box    container     = new Box( BoxLayout.Y_AXIS );
     JPanel panel1        = new JPanel();
     JPanel panel2        = new JPanel();
     JPanel control_panel = new JPanel();
     panel1.setLayout( new GridLayout(1,1) );
                                                     // set the fonts and colors
     setBackground( Color.white );
     panel1.setBackground( Color.white );
     panel2.setBackground( Color.white );
     control_panel.setBackground( Color.white );

     normal_label.setFont( FontUtil.MONO_FONT0 );
     sigma_label.setFont( FontUtil.MONO_FONT0 );

     Insets margins = new Insets( 0, 0, 0, 0 );
     user_set.setMargin( margins );
     fft_set.setMargin( margins );
     filter.setMargin( margins );
     user_set.setFont( FontUtil.LABEL_FONT );
     fft_set.setFont( FontUtil.LABEL_FONT );
     filter.setFont( FontUtil.LABEL_FONT );
     miller_value.setFont( FontUtil.LABEL_FONT );
                                                    // add controls and panels
     panel1.setLayout( new GridLayout(1,1) );
     control_panel.setLayout( new GridLayout(1,4) );

     panel1.add( normal_label );
     panel2.setLayout( new GridLayout(1,2) );
     panel2.add( d_ui );
     panel2.add( sigma_label );

     control_panel.add( user_set );
     control_panel.add( fft_set );
     control_panel.add( filter );
     control_panel.add( miller_value );

     container.add( panel1 );
     container.add( panel2 );
     container.add( control_panel );

     setLayout( new GridLayout( 1, 1 ) );
     add( container );

     ActionListener listener = new ButtonListener();
     user_set.addActionListener( listener );
     fft_set.addActionListener( listener );
     filter.addActionListener( listener );
     miller_value.addActionListener( listener );
  }


/* --------------------------- set_normal --------------------------- */
/**
 *  Set the values to be displayed as normal vector and offset.  The values
 *  will be normalized to be of unit length, if the vector is not the zero
 *  vector.
 */
  public void set_normal( float new_normal[] )
  {
    if ( new_normal == null || new_normal.length < 3 )
    {
      System.out.println(
               "ERROR: Invalid normal array in LatticePlaneUI.set_normal()" );
      return;
    }

    Vector3D normal_vec = new Vector3D( new_normal );
    if ( normal_vec.length() > 0 )
      normal_vec.normalize();

    this.normal = normal_vec.get();
 
    String text = new String(" ");
    for ( int i = 0; i < 3; i++ )
      text += Format.real( normal[i], 5, 5 ) + "  ";

    normal_label.setText( text );
  }

/* -------------------------- get_normal ----------------------------- */
/**
 *  Get the current values from the normal array.
 */
  public float[] get_normal()
  {
    float vals[] = new float[ normal.length ];
    for ( int i = 0; i < vals.length; i++ )
      vals[i] = normal[i];
    return vals;
  } 


/* --------------------------- set_d_sigma --------------------------- */
/**
 *  Set the values to be displayed as the d_spacing and sigma values.
 */
  public void set_d_sigma( float d_spacing, float sigma )
  {
    if ( d_spacing <= 0 || sigma <= 0 )
    {
      System.out.println(
         "ERROR: invalid parameter in LatticePlaneUI.set_d_sigma");
      return;
    }

    this.d_spacing = d_spacing;
    this.sigma     = sigma;

    d_ui.setValue( d_spacing );
    String s = Format.real( sigma, 5, 5 );
    sigma_label.setText( " " + FontUtil.SIGMA + ": " + s );
  }


/* --------------------------- get_d_spacing --------------------------- */
/**
 *  Get the current d_spacing value 
 */
  public float get_d_spacing()
  {
    return d_ui.getValue();
  }


/* --------------------------- get_sigma --------------------------- */
/**
 *  Get the current sigma value
 */
  public float get_sigma()
  {
    return this.sigma;
  }


/* -------------------------- set_miller_index ------------------------- */
/**
 *  Set the current miller index value
 */
  public void set_miller_index( float value )
  {
    miller_value.setValue( value );
  }


/* -------------------------- get_miller_index ------------------------- */
/**
 *  Get the current miller index value
 */
  public float get_miller_index()
  {
    return miller_value.getValue();
  }


/* ------------------------------ filter_on ---------------------------- */
/**
 *  Get the state of the filter on/off toggle switch.
 *
 *  @return true if the filter state should be on and false otherwize.
 */
  public boolean filter_on()
  {
    return filter_flag;
  }

/* -------------------------------------------------------------------------
 *
 *  INTERNAL CLASSES
 *
 */

/* ------------------------------ ButtonListener ------------------------- */
/*
 */

private class ButtonListener implements ActionListener
{
  public void actionPerformed( ActionEvent e )
  {
    String command = e.getActionCommand();

    if ( command.equals( FILTER_OFF ) )    // toggle the label on filter button
    {
      filter.setText( FILTER_ON );
      filter_flag = true;
      command = FILTER_ON;
    }
    else if ( command.equals( FILTER_ON ) )
    {
      filter.setText( FILTER_OFF );
      filter_flag = false;
      command = FILTER_OFF;
    }

    send_message( command );
  }
}


/* -------------------------------------------------------------------------
 *
 * MAIN  ( Basic main program for testing purposes only. )
 *
 */
    public static void main(String[] args)
    {
      float n_arr[] = { 1,2,3,4 };
      float d     = 4.89317f;
      float sigma = 0.00345f;

      JFrame f = new JFrame("Test for VectorReadout");
      f.setBounds(0,0,200,150);
      LatticePlaneUI control  = new LatticePlaneUI( "h" );

      control.set_normal( n_arr );
      control.set_d_sigma( d, sigma );
      control.set_miller_index( 1.5f );

      f.getContentPane().setLayout( new GridLayout(1,1) );
      f.getContentPane().add(control);

      control.addActionListener( new ActionListener()
       {
         public void actionPerformed(ActionEvent e)
         {
           String action = e.getActionCommand();
           System.out.println("In Main, command = " + action );
         }
       });

      f.setVisible(true);
    }

}
