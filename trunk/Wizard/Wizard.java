/*
 * File:  Wizard.java
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2002/02/27 17:27:52  dennis
 * Wizard class for controlling a sequence of "Forms" that
 * determine a calculation
 *
 *
 */

package Wizard;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;

/**
 *  The Wizard class provides the top level control for a sequence of 
 *  operations to be carried out with user interaction.  The quantities
 *  the the user interacts with are stored in a master list of all quantities 
 *  used in the Wizard.  The quantites are stored as WizardParameter objects.
 *  The operations that are to be carried out are described by Form objects.
 *  The Wizard also controls a sequence of forms and allows the user to
 *  step back and forth between forms.  Each form defines an execute() method
 *  to carry out the action determined by the form.  The Wizard also manages
 *  a simple help system.  Help messages should be set for each form that is
 *  used in a particular Wizard application, as well as for the Wizard itself.
 *
 *  @see Form
 *  @see WizardParameter
 */

public class Wizard implements Serializable
{
  private static final int FRAME_WIDTH   = 600;
  private static final int FRAME_HEIGHT  = 400;
  private static final int BUTTON_HEIGHT =  30;
  private static final int LARGE         = 2000;

  private static final String EXIT_COMMAND  = "Exit";
  private static final String BACK_COMMAND  = "<<-- Back";
  private static final String NEXT_COMMAND  = "Next-->>";
  private static final String HELP_ABOUT_COMMAND   = "Help about...";
  private static final String WIZARD_HELP_COMMAND  = "Help on Wizard...";
  private static final String FORM_HELP_COMMAND    = "Help on Current Form... ";
  private static final String SAVE_FORM_COMMAND    = 
                                                  "Save Current Form State...";
  private static final String LOAD_FORM_COMMAND    = 
                                                  "Load Current Form State...";
  private static final String SAVE_WIZARD_COMMAND  = "Save Wizard State...";
  private static final String LOAD_WIZARD_COMMAND  = "Load Wizard State...";
  private String help_message  = "Help not available for Wizard";
  private String about_message = "Default Help About Message";

  private   JFrame    frame;
  private   String    title;
  private   Hashtable master_list;
  private   Vector    forms;
  private   int       form_num;
  private   JPanel    form_panel;
  public    Frame     help_frame = null;
  public static TextArea status_display = new TextArea();

  public Wizard( String title )
  {
    status_display = new TextArea();
    this.title  = title;
    master_list = new Hashtable();
    forms       = new Vector();
    form_num    = -1;
    form_panel  = new JPanel();
  
    Box      work_area    = new Box( BoxLayout.Y_AXIS );
    JPanel   button_panel = new JPanel();
    JMenuBar menu_bar     = new JMenuBar();
    JMenu    file_menu    = new JMenu("File");
    JMenu    help_menu    = new JMenu("Help");

    frame = new JFrame( title );
    frame.setJMenuBar( menu_bar );
    frame.setBounds( 0,0, FRAME_WIDTH, FRAME_HEIGHT );
    frame.addWindowListener( new CloseWizardWindow() );
    frame.getContentPane().setLayout( new GridLayout(1,1) );
    frame.getContentPane().add( work_area );

    JMenuItem help_about  = new JMenuItem( HELP_ABOUT_COMMAND );
    JMenuItem wizard_help = new JMenuItem( WIZARD_HELP_COMMAND );
    JMenuItem form_help   = new JMenuItem( FORM_HELP_COMMAND );
    help_menu.add( help_about );
    help_menu.add( wizard_help );
    help_menu.add( form_help );
    
    JMenuItem save_form   = new JMenuItem( SAVE_FORM_COMMAND ); 
    JMenuItem load_form   = new JMenuItem( LOAD_FORM_COMMAND ); 
    JMenuItem save_wizard = new JMenuItem( SAVE_WIZARD_COMMAND ); 
    JMenuItem load_wizard = new JMenuItem( LOAD_WIZARD_COMMAND ); 
    JMenuItem exit_item   = new JMenuItem( EXIT_COMMAND ); 
    file_menu.add( save_form );
    file_menu.add( load_form );
    file_menu.add( save_wizard );
    file_menu.add( load_wizard );
    file_menu.add( exit_item );
    menu_bar.add(file_menu);
    menu_bar.add(help_menu);

    form_panel.setLayout( new GridLayout(1,1) );
    form_panel.setPreferredSize( new Dimension( FRAME_WIDTH, LARGE ));

    work_area.add( form_panel );
    work_area.add( button_panel );
    work_area.add( status_display );

    button_panel.setLayout( new GridLayout(1,2) ); 
    button_panel.setMaximumSize( new Dimension( FRAME_WIDTH, BUTTON_HEIGHT ));
    JButton back_button = new JButton( BACK_COMMAND );
    JButton next_button = new JButton( NEXT_COMMAND );
    button_panel.add( back_button );
    button_panel.add( next_button );

    CommandHandler command_handler = new CommandHandler();
    save_form.addActionListener( command_handler );
    load_form.addActionListener( command_handler );
    save_wizard.addActionListener( command_handler );
    load_wizard.addActionListener( command_handler );
    back_button.addActionListener( command_handler );
    next_button.addActionListener( command_handler );
    help_about.addActionListener( command_handler );
    wizard_help.addActionListener( command_handler );
    form_help.addActionListener( command_handler );
    exit_item.addActionListener( command_handler );

    frame.show();
  }


  /**
   *  This method adds a new parameter to the master list of parameters
   *  maintained by the Wizard.
   *
   *  @param  name   A descriptive name for this parameter.  
   *  @param  param  The parameter to be added to the master list
   */
  public void setParameter( String name, WizardParameter param )
  {
    master_list.put( name, param );
  }


  /**
   *  This method gets a parameter from the master list of parameters
   *  maintained by the Wizard.
   *
   *  @param  name   the name of the paramter to be retrieved from the 
   *                 master list.
   *  @return param  The parameter from the master list that is identified 
   *                 by the given name.  If there is no such parameter, this
   *                 returns null. 
   */ 
  public WizardParameter getParameter( String name )
  {
    Object obj = master_list.get( name );
    if ( obj == null || !(obj instanceof WizardParameter ))
    { 
     status_display.append("name not found in Wizard.getParameter()"+name+"\n");
     return null;
    }
    else
      return (WizardParameter)obj; 
  }

  /**
   *  Add another form to the list of forms maintained by this wizard.
   * 
   *  @param f  The form to be added to the list.
   */
  public void add( Form f )
  {
    forms.addElement( f );
  }


  /**
   *  Get the form that is currently displayed by the wizard.
   * 
   *  @return  The currently displayed form.
   */ 
  public Form getCurrentForm()
  {
    if ( form_num >= 0 && form_num < forms.size() )
      return (Form)forms.elementAt(form_num);
    else
      return null;
  }


  /**
   *  Show the form at the specified position in the list of forms.  If
   *  the index is invalid, an error message will be displayed in the 
   *  status pane.
   *
   *  @param  index  The index of the form to show.
   */
  public void show( int index )
  {
    if ( index < 0 || index > forms.size()-1 )  // invalid index
    {
      status_display.append("Error: invalid form number in Wizard.show()"
                             + index + "\n");
      return;
    }

    Form f = getCurrentForm();                  // get rid of any current form
    if ( f != null )
      f.hide();
    
    form_panel.removeAll();

    f = (Form)forms.elementAt(index);           // show the specified form
    form_panel.add( f.getPanel() );
    f.show();
    form_panel.validate();
    form_num = index;
  }


  /**
   *  Save the state of the wizard then exit the wizard application.
   */
  public void close()
  {
    status_display.append("close(): Not Fully Implemented\n");
    save();
    frame.dispose();
    System.exit(0);     // this should only be done if this is running as
                        // a stand alone application
  }

  
  /**
   *  Save the state of the wizard to a file
   */
  public void save()
  {
    status_display.append("Wizard State save() Not Implemented\n");
  }

  /**
   *  Load the state of the wizard from a file
   */ 
  public boolean load()
  {
    status_display.append("Wizard State load() Not Implemented\n");
    return false;
  }

  /**
   *  Set the help message that will be displayed when the user requests
   *  help with this wizard.
   *
   *  @param help_message  String giving the help message to use for this
   *                       wizard.
   */
  public void setHelpMessage( String help_message )
  {
     this.help_message = help_message;
  }

  /**
   *  Get the help message for this wizard.
   *
   *  @return the String giving the help message for this wizard.
   */
  public String getHelpMessage()
  {
     return help_message;
  }

  /**
   *  Set the message that will be displayed when the user chooses the
   *  help about option.
   *
   *  @param about_message  String giving the message to use for the 
   *                        "Help About" option.
   */
  public void setAboutMessage( String about_message )
  {
     this.about_message = about_message;
  }

  /**
   *  Get the help about message for this wizard.
   *
   *  @return the String giving the help about message for this wizard.
   */
  public String getAboutMessage()
  {
     return about_message;
  }

  /**
   *  Show the specified String in the help frame.
   *
   *  @param str   The message to display in the help frame.
   */
  private void ShowHelpMessage( String str )
  {
    if ( help_frame == null )
    {
      help_frame  = new Frame( "Help..." );
      help_frame.setSize(400,200);
      help_frame.addWindowListener( new HideHelpWindow() );
    }
    help_frame.removeAll();
    TextArea text  = new TextArea(str);
    help_frame.add(text);
    help_frame.show();
  }

  /* ----------------- Internal Event Handler Classes --------------------- */
  /**
   *  This class closes down the application when the user closes the frame.
   */
  private class CloseWizardWindow extends WindowAdapter
  {
    public void windowClosing( WindowEvent event )
    {
      close();
    }   
  }

  /**
   *  This class diposes of the help frame when the user closes it.
   */
  private class HideHelpWindow extends WindowAdapter
  {
    public void windowClosing( WindowEvent event )
    {
      help_frame.dispose();
      help_frame = null;
    }  
  }

  /**
   *  This class handles all of the commands from buttons and menu items.
   */
  private class CommandHandler implements ActionListener
  {
    public void actionPerformed( ActionEvent event )
    {
      String command = event.getActionCommand();

      if ( command.equals( NEXT_COMMAND ) )
      {
        if ( form_num+1 < forms.size() )
        {
          form_num++;
          show(form_num);
        }  
        else
          status_display.append( "NO MORE FORMS, CAN'T ADVANCE\n" );
      }

      else if ( command.equals( BACK_COMMAND ) )
      {
        if ( form_num-1 >= 0 )
        { 
          form_num--;
          show(form_num);
        }
        else
          status_display.append( "FORM 0 SHOWN, CAN'T STEP BACK\n" );
      }

      else if ( command.equals( HELP_ABOUT_COMMAND ) )
        ShowHelpMessage( about_message );
  
      else if ( command.equals( WIZARD_HELP_COMMAND ) )
        ShowHelpMessage( help_message );

      else if ( command.equals( FORM_HELP_COMMAND ) )
      {
        Form f = getCurrentForm();
        if ( f != null )
          ShowHelpMessage( f.getHelpMessage() );
      }

      else if ( command.equals( SAVE_WIZARD_COMMAND ) )
        save();

      else if ( command.equals( LOAD_WIZARD_COMMAND ) )
        load();

      else if ( command.equals( SAVE_FORM_COMMAND ) )
      { 
        Form f = getCurrentForm();
        if ( f != null )
          f.save();
      }

      else if ( command.equals( LOAD_FORM_COMMAND ) )
      {
        Form f = getCurrentForm();
        if ( f != null )
          f.load();
      }

      else if ( command.equals( EXIT_COMMAND ) )
        close();
    } 
  }

  /**
   *  Main program for preliminary testing only.
   */
  public static void main( String args[] )
  {
    System.out.println("Wizard Main");

    Wizard w = new Wizard( "Wizard Test" ); 
    
    w.setParameter( "Height", 
         new WizardParameter( "Height_Name", new Float(5.0), true ));
    w.setParameter( "Name", new WizardParameter( "Name_Name","Dennis", false ));
    
    Parameter p = w.getParameter( "Height" );
    System.out.println("P1=" + p.getName() + ", " + p.getValue() );
 
    p = w.getParameter( "Name" );
    System.out.println("P2=" + p.getName() + ", " + p.getValue() );

    w.show(0);
  }
}
