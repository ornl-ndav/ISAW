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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.*;
import java.beans.*;
import DataSetTools.components.ParametersGUI.*;
import java.util.*;
import DataSetTools.util.*;

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
     this.type = "Qbins";
   }

   public QbinsPG( String Prompt, Object val, boolean valid ) {
     super( Prompt, val, valid );
     setParam( new Qbins1PG( "Set Q bins or Qx,Qy min/max", null ) );
     this.type = "Qbins";
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

   public Object clone(){
      QbinsPG X = new QbinsPG( getName(), getValue());
      if( initialized)
          X.initGUI( new Vector());
      return X;

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
    
  /**
  *     This class is used to enter start, end, and number of Q values for a
  *     sublist.  The constant dQ or dQ/Q choice is also supported
  */
  private class Qbins1PG  extends ParameterGUI implements Concatenator{
     private JPanel  Container;
     private StringEntry start,end;
     private StringEntry steps;
     private JRadioButton dQ; 
     private JButton Add, Help;

     public Qbins1PG( String Prompt, Object val){ 
       super( Prompt, val );
       this.type = "Qbins1";
     }

     public Qbins1PG( String Prompt, Object val, boolean valid ) {
       super( Prompt, val, valid );
       this.type = "Qbins1";
     }

     public void initGUI( Vector V){
        this.entrywidget = new EntryWidget(  );
        this.entrywidget.setLayout(new GridLayout( 2,3));
        start = new StringEntry(".0035",7,new FloatFilter());
        end = new StringEntry("1.04",7,new FloatFilter());
        steps = new StringEntry("117", 5,new IntegerFilter());
        dQ = new JRadioButton( "dQ");
        JRadioButton dQQ = new JRadioButton("dQ/Q");
        dQQ.setSelected(true);
        ButtonGroup Group = new ButtonGroup();
        Group.add( dQ); Group.add( dQQ);
        //dQ.setSelected( true );
        JPanel jp = new JPanel( new GridLayout( 1,2));
        jp.add( dQ); jp.add( dQQ);

        this.entrywidget.add( new Comb("Start Q",start));
        this.entrywidget.add( new Comb("N Steps",steps));
        this.entrywidget.add( new Comb("End Q",end));
        this.entrywidget.add( new Comb("Constant",jp));
        this.entrywidget.validate();
        super.initGUI();
     }

    public void setValue( Object V){
       if( V instanceof Vector ) {
         this.value = V;
       } else {
         Vector temp = new Vector(  );
         if( V != null ) {
           temp.addElement( V );
         }
         this.value = temp;
       }
    }
   
    public Object getValue(){
       if( !this.initialized ) {
         return new Vector(  );
       }
       float s = (new Float(start.getText())).floatValue();
       float e = (new Float(end.getText())).floatValue();
       int n  = (new Integer(steps.getText())).intValue();
       Vector temp = new Vector(  );
       String R; 
       if( dQ.isSelected())
           R = "dQ";
       else
           R = "dQ/Q"; 

       if( n <=0){
          temp.add( new Float( s) );
          return temp;
       }
       if( R.equals("dQ/Q"))
       if( (s <=0) ||(e <=0))
         return new Vector(  );

       boolean mult = false;
       if( R.equals("dQ/Q"))
         mult = true;
       float stepSize;
       if( mult){
          stepSize = (float)Math.pow( e/s, 1.0/n);
       }
       else{
          stepSize = (e-s)/n;

       }
      for( int i=0; i <= n; i++){
        temp.add( new Float(s) );
        if( mult)
          s = s*stepSize;
        else
          s = s+stepSize;
      }
      return temp;
    }

    /**
     * Validates this Qbins1PG.  A Qbins1PG is considered valid if its
     * getValue(  ) returns a non-null, non-empty Vector.
     */
    public void validateSelf(  ) {
      Object val = getValue(  );

      if( val != null && val instanceof Vector ) {
        Vector elems = ( Vector )val;

        this.setValid( elems.size(  ) > 0 );
      } else {
        this.setValid( false );
      }
    }
  }//Qbins1

  //Utility to add a prompt to the left of text boxes, etc.
  private class Comb  extends JPanel{
    public Comb( String Prompt, JComponent Comp){
      super( new GridLayout( 1,2));
      add( new JLabel( Prompt,SwingConstants.CENTER));
      add( Comp);

    }
  }//Comb
}//QbinsPG
