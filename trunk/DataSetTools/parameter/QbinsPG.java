
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
public class QbinsPG  extends ParameterGUI{

    JButton But;
    ArrayEntryJPanel list;
    Qbins1PG  Qbins1;
    public QbinsPG( String Prompt, Object Value){
       super();
       setName( Prompt);
       setValue( Value);
       But = null;
       list = null;
       Qbins1 = null;
    }


    public void initGUI( Vector V){
      But = new JButton( "Set Q bins");
      entrywidget = But;
      But.addActionListener( new ButtonListener(this));      
      super.initGUI();
    }

   /**
   *     returns Qbins, a Type that can be used for parameters in
   *    iss scripts
   */
   public String getType(){
     return "Qbins";
   }



   public void setEnabled( boolean Enabled){
      But.setEnabled( Enabled);

   }



   public Object clone(){
      QbinsPG X = new QbinsPG( getName(), getValue());
      if( initialized)
          X.initGUI( new Vector());
      return X;

   }

  JFrame jf = null;
   boolean isShowing = false;

   /**
   *    Displays the Entry JFrame with the list of values
   */
   private  void show(){
     if( jf == null){
       jf = new JFrame( getName());
       jf.getContentPane().add(new JScrollPane(list));
       jf.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE);
       jf.addWindowListener( new WindowListener(this));
       jf.setSize( 700,400);
     }
     if( !isShowing){
        jf.show();
        isShowing = true;
     }
    }


   public void setValue( Object V){
      if( !(V instanceof Vector))
        return;
      if( value == null)
           value = V;
      else
           ((Vector)value).addAll((Vector)V);
      if( list != null)
        for( int i=0; i < ((Vector)V).size(); i++)
           list.setValue( ((Vector)V).elementAt( i ));

   }


   public Object getValue(){;
     if( value !=null)
       return value;
     return new Vector();
   }
   class WindowListener extends WindowAdapter{
      QbinsPG QQ;
      public WindowListener( QbinsPG QQ){
        this.QQ = QQ;
      }

       public void windowClosing(WindowEvent e){
          QQ.isShowing = false;
       }
   }
   class ButtonListener implements ActionListener{
      QbinsPG QQ;
      public ButtonListener( QbinsPG QQ){
        this.QQ = QQ;
      }
      public void actionPerformed( ActionEvent evt){
        if( QQ.list != null)
           QQ.show();
        QQ.Qbins1 = new Qbins1PG();
        QQ.list = new ArrayEntryJPanel( QQ.Qbins1);
        QQ.list.setValue( QQ.value);
        QQ.list.addPropertyChangeListener( new GetNewValuesListener(QQ));
        QQ.Qbins1.setList( QQ.list);
        show();
      }

   }

   }//QbinsPG
  class GetNewValuesListener implements PropertyChangeListener{
     QbinsPG QQ;
     public GetNewValuesListener( QbinsPG QQ){
       this.QQ = QQ;
     }
     public void propertyChange(PropertyChangeEvent evt){
        QQ.value =( evt.getNewValue());

     }
  }

  /**
  *     This class is used to enter start, end, and number of Q values for a
  *     sublist.  The constant dQ or dQ/Q choice is also supported
  */
  class Qbins1PG  extends ParameterGUI{
     JPanel  Container;
     StringEntry start,end;
     StringEntry steps;
     JRadioButton dQ; 
     JButton Add, Help;
     ArrayEntryJPanel list;
     AddListener_C AddListener;
     public Qbins1PG(){
        super();
        Container = null;
     }


     public void initGUI( Vector V){
        Container = new JPanel();
        Container.setLayout(new GridLayout( 2,3));
        start = new StringEntry(".0035",7,new FloatFilter());
        end = new StringEntry("4.0",7,new FloatFilter());
        steps = new StringEntry("-1", 5,new IntegerFilter());
        dQ = new JRadioButton( "dQ");
        JRadioButton dQQ = new JRadioButton("dQ/Q");
        ButtonGroup Group = new ButtonGroup();
        Group.add( dQ); Group.add( dQQ);
        dQ.setSelected( true );
        JPanel jp = new JPanel( new GridLayout( 1,2));
        jp.add( dQ); jp.add( dQQ);
        Add = new JButton( "Add");
        Help = new JButton("Help");
        AddListener = new AddListener_C(this, start,end,steps,dQ);
        Add.addActionListener( AddListener);
        Help.addActionListener( new HelpListener());

        Container.add( new Comb("Start Q",start));
        Container.add( new Comb("N Steps",steps));
        Container.add( Add);
        Container.add( new Comb("End Q",end));
        Container.add( new Comb("Constant",jp));
        Container.add( Help);
        Container.validate();
        entrywidget = Container;
        super.initGUI();
     }


    public void setValue( Object V){
     
       value = V;
    }
   
    public Object getValue(){
     
          return value;
     
    }
    public void setEnabled( boolean enabled){


    }

    //not needed.  Got at list through keyListener
    public void setList( ArrayEntryJPanel list){
      this.list = list;
      AddListener.setList( list);
    }

  }//Qbins1


  // Handles the Add button in Qbins1PG.  It adds the new list to the
  //    list box
  class AddListener_C implements ActionListener{
     StringEntry start,end;
     StringEntry  step;
      JRadioButton dQ;
     ArrayEntryJPanel list;
     Qbins1PG QQ;

     public AddListener_C( Qbins1PG QQ,StringEntry start, StringEntry end, StringEntry step, 
          JRadioButton dQ){
        this.QQ = QQ;
        this.start = start;
        this.end = end;
        this.step = step;
        this.dQ = dQ;
     }

     public void actionPerformed( ActionEvent evt){
       if( list == null)
          return;
       float s = (new Float(start.getText())).floatValue();
       float e = (new Float(end.getText())).floatValue();
       int n  = (new Integer(step.getText())).intValue();
       String R; 
       if( dQ.isSelected())
           R = "dQ";
       else
           R = "dQ/Q"; 
       if( (s <=0) ||(e <=0))
         return;
       if( n <=0){
          QQ.value = ( new Float( s));
          list.keyPressed( new KeyEvent(new JLabel(), KeyEvent.KEY_PRESSED ,(long)0,0,
                                 KeyEvent.VK_ENTER,'\n'));
          return;
       }

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
        QQ.value = new Float(s);
        list.keyPressed( new KeyEvent(new JLabel(), KeyEvent.KEY_PRESSED ,(long)0,0,
                                 KeyEvent.VK_ENTER,'\n'));
        if( mult)
          s = s*stepSize;
        else
          s = s+stepSize;
      }
      
     }
     public void setList( ArrayEntryJPanel list){
       this.list = list;
     }
  }//ActionListener_C

  // Creates the Help box
  class HelpListener implements ActionListener{
      JFrame jjf = null;
      public void actionPerformed( ActionEvent evt){
        if( jjf != null)
          jjf.show();

        jjf = new JFrame("Help");
        String ttext ="  Enter startQ, end Q, Nsteps, and dQ or dQ/Q spacings\n";
        ttext += "\n  Then press Add to get in the lower box\n";
        ttext  +="      This can be repeated to concatenate lists\n";
          
        ttext +="\n Press DONE in lower box to record the list showing\n\n";
        ttext += "   The other buttons in the bottom can be used for editting";
    
        JEditorPane jep = new JEditorPane("text/plain",ttext);
        jjf.getContentPane().add( jep);
        jjf.setSize( 400,200);
        jjf.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        jjf.show();
      }
  }//HelpListener

  //Utility to add a prompt to the left of text boxes, etc.
  class Comb  extends JPanel{
    public Comb( String Prompt, JComponent Comp){
      super( new GridLayout( 1,2));
      add( new JLabel( Prompt,SwingConstants.CENTER));
      add( Comp);

  }
}//QbinsPG
