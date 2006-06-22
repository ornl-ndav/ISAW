/*
 * File:  Qbins1PG.java
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
 * Revision 1.11  2005/06/10 15:58:30  rmikk
 * Fixed dQ, and dQ/Q choice mechanism
 *
 * Revision 1.10  2005/06/10 15:30:49  rmikk
 * Can now have natural initial values of start, end, numx
 *
 * Revision 1.9  2004/05/11 18:23:54  bouzekc
 * Added/updated javadocs and reformatted for consistency.
 *
 * Revision 1.8  2004/03/15 03:28:41  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.7  2004/03/12 21:13:49  bouzekc
 * Added clear() method.
 *
 * Revision 1.6  2004/03/12 21:12:11  bouzekc
 * Added javadocs.
 *
 * Revision 1.5  2004/01/21 18:01:39  bouzekc
 * Removed unused local variables.
 *
 * Revision 1.4  2003/12/16 00:06:00  bouzekc
 * Removed unused imports.
 *
 * Revision 1.3  2003/11/19 04:13:22  bouzekc
 * Is now a JavaBean.
 *
 * Revision 1.2  2003/11/09 22:25:13  rmikk
 * -Changed the class to be a public class
 * -This class is now valid if its value is a Vector with no elements in it
 *
 * Revision 1.1  2003/10/11 19:29:21  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.parameter;

import Command.JavaCC.ParameterGUIParser;
import DataSetTools.components.ParametersGUI.EntryWidget;
import DataSetTools.components.ParametersGUI.StringEntry;

import gov.anl.ipns.Util.StringFilter.FloatFilter;
import gov.anl.ipns.Util.StringFilter.IntegerFilter;

import java.awt.GridLayout;

import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;


/**
 * This class is used to enter start, end, and number of Q values for a
 * sublist.  The constant dQ or dQ/Q choice is also supported.  It was
 * extracted out of QbinsPG because it did not properly support clone() with
 * the reflection set up in ParameterGUI when it was an inner class.
 */
public class Qbins1PG extends ParameterGUI implements Concatenator {
  //~ Instance fields **********************************************************

  private StringEntry start;
  private StringEntry end;
  private StringEntry steps;
  private JRadioButton dQ;
  private float startv=Float.NaN, //Saves for the StringEntry values until
          endv = Float.NaN;       //GUI elements are initialized
  private  int   stepsv = -1;
  private  boolean dQ_v = true;
  //~ Constructors *************************************************************

  /**
   * Creates a new Qbins1PG object.
   *
   * @param Prompt The name of this PG.
   * @param val The initial value of this PG Either the initial Vector value
   *           or a String form for a list representing start, end, number
   *           of points, and whether to use constant dQ 
   */
  public Qbins1PG( String Prompt, Object val ) {
    super( Prompt, val );
    initTextBoxes(val);
    this.setType( "Qbins1" );
  }

  /**
   * Creates a new Qbins1PG object.
   *
   * @param Prompt The name of this PG.
   * @param val The initial start,end,steps and dQ choice of this PG.
   * @param valid Whether this PG should be considered initially valid.
   */
  public Qbins1PG( String Prompt, Object val, boolean valid ) {
    super( Prompt, null, valid );
    initTextBoxes( val);
    this.setType( "Qbins1" );
  }

  // Checks val1.  It takes care of the case if val1 is a string
  private boolean initTextBoxes(Object val1){
    if( val1 instanceof String){
       Vector val;
       try{
          val = ParameterGUIParser.parseText((String) val1);
       }catch( Throwable ss){
         return false;
       }
       
       try{
         startv= ((Number)((val).elementAt(0))).floatValue();

         endv= ((Number)((val).elementAt(1))).floatValue();
         stepsv= ((Number)((val).elementAt(2))).intValue();
         if( ((Vector)val).size()>3){
           String s4=val.elementAt(3).toString();
           dQ_v= (new Boolean(s4)).booleanValue();
         }
         setValue( startv,endv,stepsv,dQ_v);
         if( getInitialized()){
           start.setText(""+startv);
           end.setText(""+endv);
           steps.setText(""+stepsv);
           if(dQ_v)
             dQ.setSelected(true);
           else
             dQ.setSelected( false);
           
         }
         return true;
       }catch(Exception ss){
       }
    }
   return false; 
  }
  //~ Methods ******************************************************************

  /**
   * Sets the value of this QBinsPG.  If value is a string it is assumed
   * that it represents the start, end, size of Vector result and whether
   * to use const dQ, otherwise it is the bin boundaries.
   * The saved and returned value is always a Vector of bins
   * @param V The new value.
   */
  public void setValue( Object V ) {
    if(initTextBoxes(V))
       return;
    if( V instanceof Vector ) {
      super.setValue( V );
    } else {
      Vector temp = new Vector(  );

      if( V != null ) {
        temp.addElement( V );
      }

      super.setValue( temp );
    }
  }

  /**
   * @return A Vector representation of the Qbins.
   */
  public Object getValue(  ) {
    if( !this.getInitialized(  ) ) {
      return super.getValue();
    }
          
    float s     = ( new Float( start.getText(  ) ) ).floatValue(  );
    float e     = ( new Float( end.getText(  ) ) ).floatValue(  );
    int n       =( new Integer( steps.getText(  ) ) ).intValue(  );
    Vector temp = new Vector(  );
    String R;

    if( dQ.isSelected() ) {
      R = "dQ";
    } else {
      R = "dQ/Q";
    }

    if( n <= 0 ) {
      temp.add( new Float( s ) );

      return temp;
    }

    if( R.equals( "dQ/Q" ) ) {
      if( ( s <= 0 ) || ( e <= 0 ) ) {
        return new Vector(  );
      }
    }

    boolean mult = false;

    if( R.equals( "dQ/Q" ) ) {
      mult = true;
    }

    float stepSize;

    if( mult ) {
      stepSize = ( float )Math.pow( e / s, 1.0 / n );
    } else {
      stepSize = ( e - s ) / n;
    }

    for( int i = 0; i <= n; i++ ) {
      temp.add( new Float( s ) );

      if( mult ) {
        s = s * stepSize;
      } else {
        s = s + stepSize;
      }
    }
    super.setValue( temp);
    return temp;
  }


  // Calculates and sets the values corresponding to a given start, end...
  private void setValue( float start, float end, int nsteps, boolean dQ){

    

    boolean mult =!dQ;
    float e=end, s=start, n=nsteps;
   
    float stepSize;

    if( mult ) {
      stepSize = ( float )Math.pow( e / s, 1.0 / n );
    } else {
      stepSize = ( e - s ) / n;
    }
    Vector temp = new Vector();
    for( int i = 0; i <= n; i++ ) {
      temp.add( new Float( s ) );

      if( mult ) {
        s = s * stepSize;
      } else {
        s = s + stepSize;
      }
    }

    setValue(temp);

  }
  /**
   * Used to clear out the PG.  This resets the Vector to an empty one.
   */
  public void clear(  ) {
    setValue( new Vector(  ) );
  }

  /**
   * Initializes the GUI.
   *
   * @param V The new value.
   */
  public void initGUI( Vector V ) {
    this.setEntryWidget( new EntryWidget(  ) );
    this.getEntryWidget(  ).setLayout( new GridLayout( 2, 3 ) );
    String S=".0035";
    String E = "1.04";
    String N="117";
    String DQ ="dQ";
    if( !Float.isNaN(startv) &&!Float.isNaN(endv)){
        S = ""+startv;
        E = ""+endv;
    }
    if( stepsv>=0)
      N=""+stepsv;
    if(!dQ_v)
      DQ="dQ/Q";
      
    start   = new StringEntry( S, 7, new FloatFilter(  ) );
    end     = new StringEntry( E, 7, new FloatFilter(  ) );
    steps   = new StringEntry( N, 5, new IntegerFilter(  ) );
    dQ      = new JRadioButton("dQ");

    JRadioButton dQQ = new JRadioButton( "dQ/Q" );
    if(dQ_v)
       dQ.setSelected(true);
    else
       dQQ.setSelected( true );

    ButtonGroup Group = new ButtonGroup(  );

    Group.add( dQ );
    Group.add( dQQ );

    //dQ.setSelected( true );
    JPanel jp = new JPanel( new GridLayout( 1, 2 ) );

    jp.add( dQ );
    jp.add( dQQ );
    this.getEntryWidget(  ).add( new Comb( "Start Q", start ) );
    this.getEntryWidget(  ).add( new Comb( "N Steps", steps ) );
    this.getEntryWidget(  ).add( new Comb( "End Q", end ) );
    this.getEntryWidget(  ).add( new Comb( "Constant", jp ) );
    this.getEntryWidget(  ).validate(  );
    super.initGUI(  );
  }

  /**
   * Validates this Qbins1PG.  A Qbins1PG is considered valid if its getValue(
   * ) returns a non-null, non-empty Vector.
   */
  public void validateSelf(  ) {
    Object val = getValue(  );

    if( ( val != null ) && val instanceof Vector ) {
      Vector elems = ( Vector )val;

      this.setValid( elems.size(  ) >= 0 );
    } else {
      this.setValid( false );
    }
  }

  /**
   *  Copies the start, end, nbinBoundaries and wether to use dQ
   *  to the super cloned value.  These are not part of the regular
   *  value.
   */
  public Object clone(){
      Object O = super.clone();
      Qbins1PG pg = (Qbins1PG)O;
      pg.startv=startv;
      pg.endv=endv;
      pg.stepsv = stepsv;
      pg.dQ_v=dQ_v;
      return pg;
  }
  //~ Inner Classes ************************************************************

  /**
   * Utility class to add a prompt to the left of text boxes, etc.
   */
  private class Comb extends JPanel {
    //~ Constructors ***********************************************************

    /**
     * Creates a new Comb object.
     *
     * @param Prompt The prompt to use.
     * @param Comp The component to insert.
     */
    public Comb( String Prompt, JComponent Comp ) {
      super( new GridLayout( 1, 2 ) );
      add( new JLabel( Prompt, SwingConstants.CENTER ) );
      add( Comp );
    }
  }
}
