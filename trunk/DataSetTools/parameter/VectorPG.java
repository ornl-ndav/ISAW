
/*
 * File:  VectorPG.java 
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
 * Revision 1.2  2003/05/21 20:10:30  pfpeterson
 * Turned MyActionList into a private class so there is not conflicts
 * when compiling the whole package.
 *
 * Revision 1.1  2003/05/21 17:33:39  rmikk
 * -Initial Checkin.  Base for other intermediate length array entries
 *
 */

package DataSetTools.parameter;

import javax.swing.*;
import java.awt.event.*;
import java.beans.*;
import java.awt.*;
import java.util.*;

/**
*   This parameterGUI is the parent class of other parameterGUI's whose values are
*   Vectors with common Object data types for the elements.  This GUI is best for
*   medium sized list.  They are stored in a list box where the values can be editted
*   deleted, and/or rearanged
*/
public class VectorPG extends ParameterGUI implements PropertyChangeListener
  {
    String typeName;
    ParameterGUI param;
    PropertyChangeSupport pcs;
    MJPanel GUI;
    JButton butt;
    JPanel buttonHolder;
     
/*   public VectorPG()
     { this( ObjectPG);
       typeName = "Array";
      }
 */
   /**
   *  Constructor 
   *  @param   param   a ParameterGUI that determines the data type of the elements of the resultant
   *                   vector.
   *  @param  Prompt   the prompt string that appears on the  GUI( a button) and the resultant
   *                   JFrame when the button is pressed
   *
   *  The ParameterGUI is just a button in a JPanel.  When the button is pressed a more complicated
   *  JFrame is created with the list box and editting buttons
   */
   public VectorPG( ParameterGUI param, String Prompt) 
      {super();
       typeName = param.getType()+"Array";
       this.param = param;
       setName( Prompt);
       pcs = new PropertyChangeSupport( this);

       GUI = new MJPanel();
       GUI.addPropertyChangeListener( new MyPropertyChangeListener() );
      
       butt = new JButton( param.getName());
       buttonHolder = new JPanel( new GridLayout( 1,1) );
       buttonHolder.add( butt );
       butt.addActionListener(  new ButtonListener());


       }

   /**
   *    Adds property change listeners for new values of the Vector
   */
   public void addPropertyChangeListener(PropertyChangeListener listener)
     {
       
       GUI.addPropertyChangeListener( listener );
       GUI.addPropertyChangeListener( this );
      }


   /**
   *    Removes the property change listener 
   */
   public void removePropertyChangeListener(PropertyChangeListener listener)
     {
       GUI.removePropertyChangeListener( listener);
       GUI.removePropertyChangeListener( this );
      }


   // Receives notification of a new Vector value from the JFrame that pops up after
   //  the button is pressed
   class MyPropertyChangeListener implements PropertyChangeListener
     {
      public void propertyChange(PropertyChangeEvent evt)
        { 
          setValue( GUI.getValues());
          setValid( true );
        }
     }

   /**
   *    Gets the value of the Vector
   */
   public Object getValue()
     {
      return value;


     }


   public void setEnabled( boolean enable)
     {
       enabled = enable;
     }

   public void setValue( Object valuee)
     {
       
       value = valuee;
      }
  
   boolean isShowing = false;
   // Called when the original button is pressed. It creates the JFrame that stores the
   //  list box and editting buttons, etc.
   class ButtonListener implements ActionListener
    {

      
      public void actionPerformed( ActionEvent evt )
        { if( isShowing) 
               return;
          JFrame jf = new JFrame( param.getName() );
          jf.setSize( 500, 300);
          
          jf.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE);
          jf.addWindowListener( new MyWindowListener() );
          
          jf.getContentPane().setLayout( new GridLayout( 1,1));

          jf.getContentPane().add( GUI );
          jf.invalidate();
          jf.show();

         }
      

     }

   // Used to ensure there is only one copy of a window up
   class MyWindowListener extends WindowAdapter
     {
       public void windowClosed(WindowEvent e)
         {
           isShowing = false;
          }

      }



   public Object clone()
    {
      VectorPG v= new VectorPG( param, getName());
      v.setValue( v.getValue());
      return (Object)v;
    }
  
   public String getType()
     {
       return typeName;
     }

   /**
   *  Returns a JPanel that holds a button.  When the button is pressed, a new JFrame with more
   *  options appears
   */  
   public JPanel getGUIPanel()
     {
       return buttonHolder;
      }


   public void init( Vector V)
     {
       setValue( V);
       //super.init();

     }

   //This has the listbox and editting buttons for fixing an array
   class MJPanel  extends JPanel implements ActionListener
    {JList jlist;
     DefaultListModel jlistModel;
     JButton Delete,Add,Up,Down, Edit, OK, Show;
     PropertyChangeSupport pcs;
     Vector oldVector;
      public MJPanel()
        {
          super( new BorderLayout() );
         
          oldVector = getValues();
          jlistModel = new DefaultListModel();
          jlist = new JList( jlistModel );
          if( oldVector != null)
             for( int i=0; i< oldVector.size() ; i++)
                jlistModel.addElement( oldVector.elementAt( i ));
          add( jlist, BorderLayout.CENTER);
          JPanel jp = new JPanel( new GridLayout( 7,1));
          Up = new JButton( "Up");
          Down = new JButton( "Down");
          Delete = new JButton( "Delete");
          Add = new JButton("Add");
          Edit = new JButton( "Edit");
          OK = new JButton("Ok");
          Show = new JButton("Show");
          jp.add( Up);
          jp.add( Down );
          jp.add( Show );
          jp.add( Add) ;
          jp.add( Delete );
          jp.add( Edit );
          jp.add( OK );
          add( jp, BorderLayout.EAST);

          Up.addActionListener( this);
          Down.addActionListener( this);
          Show.addActionListener( this);
          Add.addActionListener( this);
          Delete.addActionListener( this);
          Edit.addActionListener( this);
          OK.addActionListener( this);
          invalidate();
          pcs = new PropertyChangeSupport( this );
         
 
         }
   
      private void move( int i)
        {
           int j = jlist.getSelectedIndex();
           if( j <=0)
             if( i == -1)
                return;

           if( j < 0) return;
           if( j >=  jlist.getModel().getSize())
             return;
           if( i >0)
             if( j == jlist.getModel().getSize() -1)
                return;
           Object V = jlistModel.elementAt( j);
           jlistModel.removeElementAt( j );
           jlistModel.insertElementAt(V, j+i);
           jlist.setSelectedIndex( j+i);
         }

      private void newVal( int pos)
        {
         if( isShowing)
           return; 
         position = pos;
        
         if( (pos >=0) && (pos < jlistModel.getSize()))
           {
             
             param.setValue( jlistModel.elementAt(pos));
    
           }
         param.init();
         JFrame jjf = new JFrame( param.getName());
         jjf.getContentPane().setLayout( new BorderLayout() );
         jjf.getContentPane().add( param.getGUIPanel(), BorderLayout.CENTER);
         JButton OOk = new JButton( "OK");
         jjf.getContentPane().add(OOk, BorderLayout.SOUTH);
         jjf.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE);
          jjf.addWindowListener( new MWindowListener());

         OOk.addActionListener( new OOkActionListener());
         
         jjf.invalidate();
         jjf.setSize( 100,100);
         jjf.show();
         
         isShowing = true;  
         }
      boolean isShowing = false;
      class MWindowListener extends WindowAdapter
        {
          public void windowClosed(WindowEvent e)
            {
              isShowing = false;
             }



         }
     
      int position = -1;
      class OOkActionListener implements ActionListener
        {

          public void actionPerformed( ActionEvent evt )
            {
              Object O = param.getValue();
              if( position < 0)
                 jlistModel.addElement( O);
              else
                 jlistModel.setElementAt( O, position);   
           }
         }
      public void actionPerformed( ActionEvent evt)
        {
          JButton butt = (JButton)(evt.getSource());
          if( butt == Up)
            {
              move(-1);
             }
          else if ( butt == Down)           
             {
              move( +1);
             }
          else if ( butt == Edit)
             {
               newVal( jlist.getSelectedIndex());
              
             }
          else if ( butt == Add)
             {
               newVal( -1);    
             }
          else if ( butt == Delete)
             {
              int j = jlist.getSelectedIndex();
              jlistModel.removeElementAt( j );
              if( j >=0)
                if( j < jlistModel.getSize() )
                 jlist.setSelectedIndex( j );
             }
          else if ( butt == Show)
             { 
               (new JOptionPane()).showMessageDialog(null, 
                 (new NexIO.NxNodeUtils()).Showw( jlist.getSelectedValue()));
             }
          else if ( butt == OK)
            
             { 
              Vector newVector = getValues();
              pcs.firePropertyChange("DataChanged", oldVector, newVector);
              oldVector = newVector;
             }


        }

      public void addPropertyChangeListener(PropertyChangeListener listener)
        {
       
         pcs.addPropertyChangeListener( listener );
     
         }

      public void removePropertyChangeListener(PropertyChangeListener listener)
        {
         pcs.removePropertyChangeListener( listener);
      
        }



      public Vector getValues()
        {
          if( jlist == null)
            return new Vector();
          ListModel lmodel = jlist.getModel();
          Vector V = new Vector( lmodel.getSize());
          for( int i = 0; i < lmodel.getSize() ; i++)
            V.addElement( lmodel.getElementAt( i ) );
          return V;

        }

     }//MJPanel

    /**
    *   Test program for this module.  There are no arguments
    */
    public static void main( String args[] )
      {
         JFrame jf = new JFrame("Test");
         jf.getContentPane().setLayout( new GridLayout( 1,2));
         FloatPG ipg = new FloatPG("Enter Float",5.0f);
         if( ipg == null)
            DataSetTools.util.SharedData.addmsg("Null parameter????");
         VectorPG vpg = new VectorPG( ipg, "Integer List");
         jf.getContentPane().add(vpg.getGUIPanel());
         JButton  jb = new JButton("Result");
         jf.getContentPane().add(jb);
         jb.addActionListener( new MyActionList( vpg));
         jf.setSize( 500,100);
         jf.invalidate();
         jf.show();




      }      

static class MyActionList implements ActionListener
  {
   VectorPG vpf;
   public MyActionList( VectorPG vpg)
     {

       vpf = vpg;
     }

    public void actionPerformed( ActionEvent evt )
      { 
        (new JOptionPane()).showMessageDialog(null,"Result="+
       (new NexIO.NxNodeUtils()).Showw(vpf.getValue()));

      }

   }
  }
