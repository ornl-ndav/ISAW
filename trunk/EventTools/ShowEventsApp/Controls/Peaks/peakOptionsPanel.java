/* 
 * File: peakOptionsPanel.java
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

package EventTools.ShowEventsApp.Controls.Peaks;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.GridLayout;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

/**
 * Creates the options panel for peak options which include
 * findPeaksPanel and writePeaks panel.
 */
public class peakOptionsPanel extends JPanel
{
   private static final long serialVersionUID = 1L;
   
   /**
    * Constructor for peakOptionsPanel that builds
    * the panel as well as sets the messagecenter.
    * 
    * @param message_center
    */
   public peakOptionsPanel(MessageCenter message_center)
   {
      findPeaksPanel findPeaks = new findPeaksPanel(message_center);
      //markPeaks markpeaks = new markPeaks(message_center);
      //writePeaks writepeaks = new writePeaks(message_center);
      
      this.setBorder(new TitledBorder("Peak Options"));
      
      this.setLayout(new GridLayout(5,1));
      //this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      this.add(findPeaks.buildCheckInfo());
      this.add(findPeaks.buildMaxPeaks());
      this.add(findPeaks.buildMinPeaks());
      this.add(findPeaks.buildLogPanel());
      this.add(findPeaks.buildButtonPanel());
      //this.add(writepeaks);
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Test Peak Options");
      TestReceiver tr = new TestReceiver("Testing Peak Options");
      
      mc.addReceiver(tr, Commands.WRITE_PEAK_FILE);
      mc.addReceiver(tr, Commands.FIND_PEAKS);
      mc.addReceiver(tr, Commands.MARK_PEAKS);
      
      peakOptionsPanel peakOptions = new peakOptionsPanel(mc);
      
      JFrame View = new JFrame("Test Peak Options");
      View.setBounds(10, 10, 300, 300);
      View.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      View.setVisible(true);
      
      View.add(peakOptions);
      
      new UpdateManager(mc, null, 100);
   }
}
