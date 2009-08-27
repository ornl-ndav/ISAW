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

/**
 * Displays information about the point selected on the jogl
 * panel.
 */
public class positionInfoPanel extends JPanel
                               implements IReceiveMessage
{
   public static final long    serialVersionUID = 1L;
   //private MessageCenter       messageCenter;
   private JTextField          countsTxt;
   private JTextField          detectorNumTxt;
   private JTextField          ColRowTxt;
   private JTextField          rowTxt;
   private JTextField          histogramPageTxt;
   private JTextField          hklTxt;
   private JTextField          qxyzTxt;
   private JTextField          qTxt;
   private JTextField          dSpacingTxt;
   private JTextField          timeTxt;
   private JTextField          eTxt;
   private JTextField          wavelengthTxt;
   private char                ANG = '\u00c5';
   private char                MU = '\u03bc';
   
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
   private JPanel buildZoomedPanel()
   {
      JPanel panel = new JPanel();
      this.setLayout(new GridLayout(1,1));
      
      return panel;
   }
   
   /**
    * Builds the entire panel containing all the information
    * labels about the point selected.
    * 
    * @return JPanel
    */
   private JPanel buildPanel()
   {
      JPanel panel = new JPanel();
      
      panel.setLayout(new GridLayout(11, 2));
      panel.setBorder(new TitledBorder("Position Info."));
      
      JTextField countLbl = new JTextField("Counts");
      countLbl.setEditable(false);
      countLbl.setBackground(Color.WHITE);
      
      JTextField detectorLbl = new JTextField("Detector Number");
      detectorLbl.setEditable(false);
      detectorLbl.setBackground(Color.WHITE);
      
      JTextField ColRowLbl = new JTextField("Column,Row");
      ColRowLbl.setEditable(false);
      ColRowLbl.setBackground(Color.WHITE);
      
      JTextField histogramLbl = new JTextField("Histogram Page");
      histogramLbl.setEditable(false);
      histogramLbl.setBackground(Color.WHITE);
      
      JTextField hklLbl = new JTextField("h,k,l");
      hklLbl.setEditable(false);
      hklLbl.setBackground(Color.WHITE);
      
      JTextField qxyzLbl = new JTextField("Qx,Qy,Qz");
      qxyzLbl.setEditable(false);
      qxyzLbl.setBackground(Color.WHITE);
      
      JTextField qLbl = new JTextField("Q(Inv(" + ANG + "))");
      qLbl.setEditable(false);
      qLbl.setBackground(Color.WHITE);
      
      JTextField dSpacingLbl = new JTextField("d-Spacing(" + ANG + ")");
      dSpacingLbl.setEditable(false);
      dSpacingLbl.setBackground(Color.WHITE);
      
      JTextField timeLbl = new JTextField("Time(" + MU + "s)");
      timeLbl.setEditable(false);
      timeLbl.setBackground(Color.WHITE);
      
      JTextField eLbl = new JTextField("E(meV)");
      eLbl.setEditable(false);
      eLbl.setBackground(Color.WHITE);
      
      JTextField wavelengthLbl = new JTextField("Wavelength(" + ANG + ")");
      wavelengthLbl.setEditable(false);
      wavelengthLbl.setBackground(Color.WHITE);
      
      countsTxt = new JTextField("0");
      countsTxt.setEditable(false);
      countsTxt.setBackground(Color.WHITE);
      countsTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      detectorNumTxt = new JTextField("0");
      detectorNumTxt.setEditable(false);
      detectorNumTxt.setBackground(Color.WHITE);
      detectorNumTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      ColRowTxt = new JTextField("(  0,  0)");
      ColRowTxt.setEditable(false);
      ColRowTxt.setBackground(Color.WHITE);
      ColRowTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      detectorNumTxt = new JTextField("0");
      detectorNumTxt.setEditable(false);
      detectorNumTxt.setBackground(Color.WHITE);
      detectorNumTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      histogramPageTxt = new JTextField("0");
      histogramPageTxt.setEditable(false);
      histogramPageTxt.setBackground(Color.WHITE);
      histogramPageTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      hklTxt = new JTextField("(  0.00,   0.00,   0.00)");
      hklTxt.setEditable(false);
      hklTxt.setBackground(Color.WHITE);
      hklTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      qxyzTxt = new JTextField("(  0.00,   0.00,   0.00)");
      qxyzTxt.setEditable(false);
      qxyzTxt.setBackground(Color.WHITE);
      qxyzTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      qTxt = new JTextField(" 0.0000");
      qTxt.setEditable(false);
      qTxt.setBackground(Color.WHITE);
      qTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      dSpacingTxt = new JTextField(" 0.0000000");
      dSpacingTxt.setEditable(false);
      dSpacingTxt.setBackground(Color.WHITE);
      dSpacingTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      timeTxt = new JTextField("   0.0");
      timeTxt.setEditable(false);
      timeTxt.setBackground(Color.WHITE);
      timeTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      eTxt = new JTextField(" 0.0000");
      eTxt.setEditable(false);
      eTxt.setBackground(Color.WHITE);
      eTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      wavelengthTxt = new JTextField("0.000000");
      wavelengthTxt.setEditable(false);
      wavelengthTxt.setBackground(Color.WHITE);
      wavelengthTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      panel.add(countLbl);
      panel.add(countsTxt);
      panel.add(detectorLbl);
      panel.add(detectorNumTxt);
      panel.add(ColRowLbl);
      panel.add(ColRowTxt);
      panel.add(histogramLbl);
      panel.add(histogramPageTxt);
      panel.add(hklLbl);
      panel.add(hklTxt);
      panel.add(qxyzLbl);
      panel.add(qxyzTxt);
      panel.add(qLbl);
      panel.add(qTxt);
      panel.add(dSpacingLbl);
      panel.add(dSpacingTxt);
      panel.add(timeLbl);
      panel.add(timeTxt);
      panel.add(eLbl);
      panel.add(eTxt);
      panel.add(wavelengthLbl);
      panel.add(wavelengthTxt);
      
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
      detectorNumTxt.setText( "" + selection.getDetNum() );
      ColRowTxt.setText( "("+selection.getCol()+", "+selection.getRow()+")" );
      histogramPageTxt.setText( "" + selection.getHistPage() );
      hklTxt.setText( String.format("(%6.2f, %6.2f, %6.2f)",
            selection.getHKL().getX(), selection.getHKL().getY(),
            selection.getHKL().getZ()) );
      qxyzTxt.setText( String.format("(%6.2f, %6.2f, %6.2f)",
            selection.getQxyz().getX(), selection.getQxyz().getY(),
            selection.getQxyz().getZ()) );
      qTxt.setText( String.format("%7.4f", selection.getRaw_Q()) );
      dSpacingTxt.setText( String.format("%8.6f", selection.getD_spacing()) );
      timeTxt.setText( String.format("%6.1f", selection.getTof()) );
      eTxt.setText( String.format("%7.4f", selection.getE_mev()) );
      wavelengthTxt.setText( String.format("%8.6f", selection.getWavelength()) );
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
