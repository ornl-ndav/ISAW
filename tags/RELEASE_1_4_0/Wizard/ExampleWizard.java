/**
 *  This is a very crude initial test of building a Wizard.
 */
package Wizard;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import DataSetTools.wizard.*;
import DataSetTools.util.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;

public class ExampleWizard 
{

  public static void main( String args[] )
  {                                                     // build the wizard
    Wizard w = new Wizard( "Example Wizard" ); 
    Wizard.status_display.append("Example Wizard Main\n");

                                                        // define the master
                                                        // list of parameters
    w.setParameter( "Name", new StringPG( "My Name","Dennis", true ));
    w.setParameter( "Height", 
                   new FloatPG( "My Height", new Float(6.42), false ));
    w.setParameter( "Age", 
                   new FloatPG( "My Age", new Float( 51 ), false ));
    w.setParameter( "YourAge", 
          new FloatPG( "Your Age", new Float( 51 ), true ));

                                          // Create forms using the parameters
                                          // Since the base Form class execute()
                                          // method doesn't do anything these
                                          // forms also don't do anything
    String info_parms[] = { "Name" };
    String edit_parms[] = { "Height" };
    String out_parms[]  = {};
    Form form0 = new Form( "Form 0 title", 
                            info_parms, 
                            edit_parms, 
                            out_parms, 
                            w );
    form0.setHelpMessage("Help Message for Form 0");
    w.add( form0 );

    String info_parms_1[] = {};
    String edit_parms_1[] = { "Age" };
    String out_parms_1[]  = { "YourAge" };
    Form form1 = new Form( "Form 1 title", 
                            info_parms_1, 
                            edit_parms_1, 
                            out_parms_1, 
                            w );
    form1.setHelpMessage("Help Message for Form 1");
    w.add( form1 );

    w.show(0);
  }
}
