/*
 * File:  QbinsPG.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 *
 * Modified:
 *
 * $Log$
 * Revision 1.16  2003/12/16 00:06:00  bouzekc
 * Removed unused imports.
 *
 * Revision 1.15  2003/11/19 04:13:23  bouzekc
 * Is now a JavaBean.
 *
 * Revision 1.14  2003/10/11 20:29:32  bouzekc
 * Removed import of java.lang.  This is not needed and can
 * sometimes cause problems.
 *
 * Revision 1.13  2003/10/11 19:30:56  bouzekc
 * Removed definition of clone() as the superclass implements it using
 * reflection.  Moved Qbins1PG out to a package-level access class
 * due to reflection's inability to easily determine information about an
 * inner class.
 *
 * Revision 1.12  2003/09/27 13:22:49  rmikk
 * Made dQ/Q the default
 *
 * Revision 1.11  2003/09/15 20:48:05  bouzekc
 * Fixed bug where getValue() would crash if no GUI existed.  getValue() now
 * returns an empty Vector if the GUI does not exist.
 *
 * Revision 1.10  2003/09/15 18:05:03  dennis
 * Changed defaults for End Q, Num Steps, and state of dq/q radio
 * button.  (For Alok)
 *
 * Revision 1.9  2003/09/15 17:28:45  bouzekc
 * Fixed bug in validateSelf().  Clarified ambiguous references in Qbins1PG.
 *
 * Revision 1.8  2003/09/13 22:18:22  bouzekc
 * Moved class Comb and class Qbins1PG inside of QbinsPG.  This was to correct
 * an earlier move outside of the class.
 *
 * Revision 1.7  2003/09/09 23:06:29  bouzekc
 * Implemented validateSelf().
 *
 * Revision 1.6  2003/08/30 19:49:17  bouzekc
 * Now extends VectorPG.  Qbins1PG now implements Concatenator.  Moved Help
 * button functionality into ArrayEntryJFrame for a cleaner look.  Removed
 * inner actionListeners, as ArrayEntryJFrame now handles that functionality.
 *
 * Revision 1.5  2003/08/28 20:06:56  rmikk
 * QbinPG's setValue no longer appends the new Value to
 *   the old value
 *
 * Revision 1.4  2003/08/28 03:10:04  bouzekc
 * Modified to work with the new ParameterGUI.
 *
 * Revision 1.3  2003/08/25 14:57:58  rmikk
 * -Updated to work better with the Anisotropic case
 * -Removed JScrollPane and put it into ArrayEntryJPanel
 *
 * Revision 1.2  2003/08/22 20:13:47  bouzekc
 * Modified to work with EntryWidget.  Added call to setValue( Object) in
 * initGUI(Vector).  Added testbed main().
 *
 * Revision 1.1  2003/08/21 15:29:18  rmikk
 * Initial Checkin
 *
 */


package DataSetTools.parameter;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;

import DataSetTools.util.PGActionListener;

/**
*    This ParameterGUI was designed specifically for entering a large
*    list of Q bin boundaries with either constant Q widths or constant
*    ratios. This ParameterGUI allows for concatenating several of these
*    lists
*/
public class QbinsPG  extends VectorPG{


   public QbinsPG( String Prompt, Object val){ 
     super( Prompt, val );
     setParam( new Qbins1PG( "Set Q bins or Qx,Qy min/max", null ) );
     this.setType( "Qbins" );
   }

   public QbinsPG( String Prompt, Object val, boolean valid ) {
     super( Prompt, val, valid );
     setParam( new Qbins1PG( "Set Q bins or Qx,Qy min/max", null ) );
     this.setType( "Qbins" );
   }

   public void initGUI( Vector vals ) {
     super.initGUI( vals );
     StringBuffer ttext = new StringBuffer(  );;
     ttext.append( "  Enter ,startQ ,end Q, Nsteps, and dQ or dQ/Q spacings\n" );
     ttext.append( "If Nsteps <=0, startQ will just be added. Use this to enter\n" );
     ttext.append( "  Qx min, Qx max, Qy min, then Qy max for Anisotropic analysis\n" );
     ttext.append( "\n  Then press Add to add to the lower list box\n" );
     ttext.append( "      This can be repeated to concatenate lists\n" );
     ttext.append( "\n Press DONE in lower list box to record the list showing\n\n" );
     ttext.append( "   The other buttons in the bottom can be used for editting" );
     getEntryFrame(  ).setHelpMessage( ttext.toString(  ) );
   }

   /**
    * Validates this QbinsPG.  A QbinsPG is considered valid if its internal
    * Qbins1PG parameter is valid.
    */
   public void validateSelf(  ) {
     Qbins1PG qb1pg = ( Qbins1PG )getParam(  );
     if( qb1pg != null ) {
       qb1pg.validateSelf(  );

       setValid( qb1pg.getValid(  ) );
     } else {
       setValid( false );
     }
   }

  /**
   * Testbed.
   */
  public static void main( String args[] ) {
    JFrame jf = new JFrame("Test");
    jf.getContentPane().setLayout( new GridLayout( 1,2));
    QbinsPG qbpg = new QbinsPG( "Test Qbins", null, true );
    qbpg.initGUI( null );
    jf.getContentPane().add(qbpg.getGUIPanel());
    JButton  jb = new JButton("Result");
    jf.getContentPane().add(jb);
    jb.addActionListener( new PGActionListener( qbpg));
    jf.setSize( 500,100);
    jf.invalidate();
    jf.show();
  }
    

}//QbinsPG
