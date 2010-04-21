/* 
 * File: additionalViewControls.java
 *
 * Copyright (C) 2009, Paul Fischer
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
 * number DMR-0800276 and by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.ShowEventsApp.Controls;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

import MessageTools.*;
import EventTools.ShowEventsApp.Command.*;
import gov.anl.ipns.ViewTools.UI.*;

/**
 * Displays information about the point selected on the jogl
 * panel.
 */
public class positionInfoPanel extends JPanel
                               implements IReceiveMessage
{
   public static final long    serialVersionUID = 1L;
// private MessageCenter       messageCenter;
   private JTextField          countsTxt;
   private JTextField          detectorNumTxt;
   private JTextField          ColRowTxt;
   private JTextField          SeqNumTxt;
// private JTextField          histogramPageTxt;
   private JTextField          hklTxt;
   private JTextField          qxyzTxt;
   private JTextField          qTxt;
   private JTextField          dSpacingTxt;
   private JTextField          twoThetaTxt;
   private JTextField          timeTxt;
   private JTextField          eTxt;
   private JTextField          wavelengthTxt;
   private JTextField          projectedHKL_Txt;
   private JTextField          psiTxt;
   private JTextField          tiltTxt;

   private static String       ANG   = FontUtil.ANGSTROM;
   private static String       MU    = FontUtil.MU;
   private static String       DEG   = FontUtil.DEGREE; 
   private static String       THETA = FontUtil.THETA;
   private static String       PSI   = FontUtil.PSI;
   
   /**
    * Builds the panel and adds everything to itself.
    * 
    * @param messageCenter
    */
   public positionInfoPanel(MessageCenter messageCenter)
   {
      //this.messageCenter = messageCenter;
      this.setLayout(new GridLayout(1,1));
      this.add(buildPanel());
      //this.add(buildZoomedPanel());
      messageCenter.addReceiver(this, Commands.SHOW_SELECTED_POINT_INFO);
   }

   /**
    * TO BE IMPLEMENTED LATER.
    * Will build a jogl panel to add to the panel that will show 
    * a zoomed in view of the point selected.
    * 
    * @return JPanel
    */
/* Not used yet
   private JPanel buildZoomedPanel()
   {
      JPanel panel = new JPanel();
      this.setLayout(new GridLayout(1,1));
      
      return panel;
   }
*/
   
   /**
    * Builds the entire panel containing all the information
    * labels about the point selected.
    * 
    * @return JPanel
    */
   private JPanel buildPanel()
   {
      JPanel panel = new JPanel();
      
      panel.setLayout(new GridLayout(15, 2));
      panel.setBorder(new TitledBorder("Position Info."));
      
      JTextField countLbl         = new JTextField("Counts");
      JTextField detectorLbl      = new JTextField("Detector Number");
      JTextField ColRowLbl        = new JTextField("Column,Row");
      JTextField SeqNumLbl           = new JTextField("Peak Sequence Number");
//    JTextField histogramLbl     = new JTextField("Histogram Page");
      JTextField hklLbl           = new JTextField("h,k,l");
      JTextField qxyzLbl          = new JTextField("Qx,Qy,Qz");
      JTextField qLbl             = new JTextField("Q(Inv(" + ANG + "))");
      JTextField dSpacingLbl      = new JTextField("d-Spacing(" + ANG + ")");
      JTextField twoThetaLbl      = new JTextField("2"+THETA+"("+DEG+")");
      JTextField timeLbl          = new JTextField("Time(" + MU + "s)");
      JTextField eLbl             = new JTextField("E(meV)");
      JTextField wavelengthLbl    = new JTextField("Wavelength(" + ANG + ")");
      JTextField projectedHKL_Lbl = new JTextField("Projected HKL");
      JTextField psiLbl           = new JTextField( PSI+"("+DEG+")");
      JTextField tiltLbl          = new JTextField("Tilt(" + DEG + ")");
      
      countsTxt        = new JTextField("0");
      detectorNumTxt   = new JTextField("0");
      ColRowTxt        = new JTextField("(  0,  0)");
      SeqNumTxt           = new JTextField("0");
      detectorNumTxt   = new JTextField("0");
//    histogramPageTxt = new JTextField("0");
      hklTxt           = new JTextField("(  0.00,   0.00,   0.00)");
      qxyzTxt          = new JTextField("(  0.00,   0.00,   0.00)");
      qTxt             = new JTextField(" 0.0000");
      dSpacingTxt      = new JTextField(" 0.0000000");
      twoThetaTxt      = new JTextField(" 0.0000000");
      timeTxt          = new JTextField("   0.0");
      eTxt             = new JTextField(" 0.0000");
      wavelengthTxt    = new JTextField("0.000000");
      projectedHKL_Txt = new JTextField("(  0.000,   0.000,   0.000)");
      psiTxt           = new JTextField("0.000");
      tiltTxt          = new JTextField("0.000");

      panel.add(countLbl);
      panel.add(countsTxt);
      panel.add(detectorLbl);
      panel.add(detectorNumTxt);
      panel.add(ColRowLbl);
      panel.add(ColRowTxt);
      panel.add( SeqNumLbl);
      panel.add( SeqNumTxt);
//    panel.add(histogramLbl);
//    panel.add(histogramPageTxt);
      panel.add(hklLbl);
      panel.add(hklTxt);
      panel.add(qxyzLbl);
      panel.add(qxyzTxt);
      panel.add(qLbl);
      panel.add(qTxt);
      panel.add(dSpacingLbl);
      panel.add(dSpacingTxt);
      panel.add(twoThetaLbl);
      panel.add(twoThetaTxt);
      panel.add(timeLbl);
      panel.add(timeTxt);
      panel.add(eLbl);
      panel.add(eTxt);
      panel.add(wavelengthLbl);
      panel.add(wavelengthTxt);
      panel.add(projectedHKL_Lbl);
      panel.add(projectedHKL_Txt);
      panel.add(psiLbl);
      panel.add(psiTxt);
      panel.add(tiltLbl);
      panel.add(tiltTxt);
      
      Component[] components = panel.getComponents();
      for ( int i = 0; i < components.length; i++ )
      {
        JTextField tf = (JTextField)components[i];
        tf.setEditable(false);
        tf.setBackground(Color.WHITE);
        if ( i % 2 == 1 )
          tf.setHorizontalAlignment(JTextField.RIGHT);
      }

      return panel;
   }
   
   /**
    * Takes in a SelectionInfoCmd and fills out the
    * panel with all the information that was passed to 
    * it.
    * 
    * @param selection A SelectionInfoCmd object containing
    *       all the information about the selected point.
    */
   private void setFields(SelectionInfoCmd selection)
   {
      countsTxt.setText( "" + String.format( "%4.2f" , selection.getCounts() ));
      detectorNumTxt.setText( "" + (int)selection.getDetNum() );
      ColRowTxt.setText( "("+(int)selection.getCol()+
                         ", "+(int)selection.getRow()+")" );
//    histogramPageTxt.setText( "" + (int)selection.getHistPage() );
      SeqNumTxt.setText( ""+ selection.getSeqNum( ) );
      hklTxt.setText( String.format("(%6.2f, %6.2f, %6.2f)",
                                       selection.getHKL().getX(), 
                                       selection.getHKL().getY(),
                                       selection.getHKL().getZ()) );
      qxyzTxt.setText( String.format("(%6.2f, %6.2f, %6.2f)",
                                       selection.getQxyz().getX(), 
                                       selection.getQxyz().getY(),
                                       selection.getQxyz().getZ()) );
      qTxt.setText( String.format("%7.4f", selection.getRaw_Q()) );
      dSpacingTxt.setText( String.format("%8.6f", selection.getD_spacing()) );
      twoThetaTxt.setText( 
               String.format("%8.6f", selection.getTwo_theta( )*180/Math.PI) );
      timeTxt.setText( String.format("%6.1f", selection.getTof()) );
      eTxt.setText( String.format("%7.4f", selection.getE_mev()) );
      wavelengthTxt.setText( String.format("%8.6f", selection.getWavelength()));
      projectedHKL_Txt.setText( String.format("(%6.3f, %6.3f, %6.3f)",
                                       selection.getProjectedHKL().getX(), 
                                       selection.getProjectedHKL().getY(),
                                       selection.getProjectedHKL().getZ()) );
      psiTxt.setText ( String.format("%7.3f", selection.getPSI()) );
      tiltTxt.setText( String.format("%7.3f", selection.getTilt()) );
   }
   
   /**
    * Listen for SHOW_SELECTED_POINT_INFO and call
    * setField(selection) to display all the information.
    */
   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.SHOW_SELECTED_POINT_INFO))
      {
         SelectionInfoCmd selection = (SelectionInfoCmd)message.getValue();
         setFields(selection);
         
         return true;
      }
      
      return false;
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Testing MessageCenter");
      TestReceiver tc = new TestReceiver("Position Info TestingMessages");
      mc.addReceiver(tc, Commands.SHOW_SELECTED_POINT_INFO);
      
      positionInfoPanel pip = new positionInfoPanel(mc);
      
      JFrame View = new JFrame( "Test Position Info Panel" );
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setBounds(10,10, 350, 450);
      View.setVisible(true);
      
      View.add(pip);
      new UpdateManager(mc, null, 100);
   }
}
