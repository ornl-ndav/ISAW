/*
 * File:  CommandPane.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Revision 1.5  2002/11/27 23:12:10  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/08/19 17:07:00  pfpeterson
 * Reformated file to make it easier to read.
 *
 * Revision 1.3  2002/01/09 19:29:43  rmikk
 * Added constructors so that the toolbar can be placed on
 *    any of the four corners.
 *
*/
package Command;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * This a utility that quickly lets you place one or two JButtons at
 * the top left of a JComponent
 */
public class JPanelwithToolBar  extends JPanel{
  /**
   * A constructor that places one JButton at the top right of a
   * JComponent
   */  
    public JPanelwithToolBar( String ButtonText, ActionListener ButtonHandler,
                              JComponent  nonToolBarComponent,               
                              String BorderPosition ){
        super( new BorderLayout());
        
	JPanel JToolPanel = new JPanel( new BorderLayout() );
        JButton Button= new JButton( ButtonText);
        Button.addActionListener( ButtonHandler );
        boolean horizontal=true;
        if( BorderPosition .equals("BorderLayout.EAST"))
            horizontal = false;
        else if( BorderPosition .equals("BorderLayout.WEST"))
            horizontal = false;
        if( horizontal)
            JToolPanel.add( Button,BorderLayout.EAST  );
        else
            JToolPanel.add(Button, BorderLayout.NORTH);
        add( JToolPanel, BorderPosition );
        add( nonToolBarComponent, BorderLayout.CENTER );
    }

    public JPanelwithToolBar( String ButtonText, ActionListener ButtonHandler,
                              JComponent  nonToolBarComponent){
        this(ButtonText,ButtonHandler,nonToolBarComponent,
             BorderLayout.NORTH);
    }

    
    /**
     * The constructor that places two JButtons at the top right of a
     * JComponent
     */
    public JPanelwithToolBar( String ButtonText, String Button1Text,
                              ActionListener ButtonHandler,
                              ActionListener Button1Handler,
                              JComponent  nonToolBarComponent,
                              String BorderPosition ){
        super( new BorderLayout());
        boolean horizontal = true;
        if( BorderPosition .equals(BorderLayout.EAST))
            horizontal = false;
        else if( BorderPosition .equals(BorderLayout.WEST))
            horizontal = false;
        Box JToolPanel;
               
        if( horizontal)
            JToolPanel = new Box( BoxLayout.X_AXIS );
        else
            JToolPanel = new Box( BoxLayout.Y_AXIS);
        Component glue;
        if( horizontal) 
            glue = Box.createHorizontalGlue();
        else 
            glue = Box.createVerticalGlue();

        if( horizontal){
            JToolPanel.add(glue);
            JToolPanel.add( glue);
        }
        JButton Button= new JButton( ButtonText);
        Button.addActionListener( ButtonHandler );
        JButton Button1= new JButton( Button1Text);
        Button1.addActionListener( Button1Handler );
        JToolPanel.add( Button);
        JToolPanel.add( Button1);
        if( !horizontal){
            JToolPanel.add(glue);
            JToolPanel.add( glue);
        }
        add( JToolPanel,BorderPosition );
        add( nonToolBarComponent, BorderLayout.CENTER );
        
    }

    public JPanelwithToolBar( String ButtonText, String Button1Text,
                              ActionListener ButtonHandler,
                              ActionListener Button1Handler,
                              JComponent  nonToolBarComponent){
        this( ButtonText, Button1Text, ButtonHandler, Button1Handler,
              nonToolBarComponent, BorderLayout.NORTH);
    }
    
  /**
   * Test program and example of the use of this class <P> Comp.add(
   * new JPanelwithToolBar( "Exit", exitlistener, TheComponent);
   */
    public static void main ( String args[] ){
        JFrame JF = new JFrame( "Hi There");
        
        JTextArea JA = new JTextArea(5,8);
        JA.setText( "123\n456\n789\n101112\n");
        JF.getContentPane().add( new JPanelwithToolBar( "EXIT","Save",
                                                        new MyHandler() 
                                                        ,new MyHandler1() 
                                                        , JA ));
        JF.setSize( 400,400);
        JF.show();
    }
}

class MyHandler implements ActionListener{
    public void actionPerformed(ActionEvent e){
        System.out.println("in action performed 0");
    }
}

class MyHandler1 implements ActionListener{
    public void actionPerformed(ActionEvent e){
        System.out.println("in action performed 1");
    }
}
