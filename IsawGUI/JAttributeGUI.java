/*
 * @(#)JAttributeGUI.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */
package IsawGUI;
 
 import javax.swing.*;
 import DataSetTools.*;
 import DataSetTools.dataset.*;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */
 
abstract public class JAttributeGUI 
{
    Attribute attr;
    public JAttributeGUI(Attribute attr)
    { 
       this.attr = attr;
    }
    
    public Attribute getAttribute()
    {
        return attr;
    }
    
    abstract public JPanel getGUISegment();
}
