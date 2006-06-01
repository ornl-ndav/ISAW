/* 
 * File: QuickTableViewer.java
 *  
 * Copyright (C) 2006  Ruth Mikkelson
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
 * Contact :  Ruth  Mikkelson<mikkelsonr@uwstout.edu>>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 * $Log$
 * Revision 1.3  2006/06/01 20:41:49  rmikk
 * Eliminated Debu printing
 *
 * Revision 1.2  2006/06/01 18:47:40  rmikk
 * -Added a table model for the data.
 * -Coordinated the UI more:The close button works and the Viewer does not
 *    loose its focus when this Quick Pointed at Viewer changes
 *
 * Revision 1.1  2006/05/31 14:30:39  rmikk
 * Initial Checkin for a quick view of the pointed at block of a DataSet.
 * This is NOT a data set viewer. It is just a JFrame.
 *
 */
package DataSetTools.viewer;

import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.Util.Messaging.*;
import javax.swing.*;

import Command.ScriptUtil;
import DataSetTools.dataset.*;

import java.awt.Point;
import java.awt.event.*; 
import javax.swing.table.*;
import javax.swing.event.*;
/**
 * This class creates a JFrame containing a table of the pointed at data block
 * values 
 * @author Ruth Mikkelson
 *
 */
public class QuickTableViewer extends FinishJFrame implements WindowListener, 
                                                    IObserver,ComponentListener,
                                                    ActionListener{

	 JFrame parent;
	 boolean hugg;
	 DataSet DS;
	 JCheckBoxMenuItem ShowErrs,
	                   ShowIndx,
	                   Hug;
	 JTable table;
	 JScrollPane jscr;
	 int PointedAtXindex;
	 
	 
	 
	/*
	 * Constructor for the QuickTableViewer
	 * @param parent   The parent to attach to if attach is chosen
	 * @param DS       The DataSet that is to be viewed 
	 */
	public QuickTableViewer( JFrame parent, DataSet DS ) {
		
		super( "Quick Table View " );
		this.parent = parent;
		this.parent.addWindowListener( this );
		this.parent.addComponentListener( this);
		this.DS = DS;
		DS.addIObserver( this );
		
		JMenu jm = new JMenu("Options" );
		ShowErrs= new JCheckBoxMenuItem( "Show Errors" );
		ShowIndx = new JCheckBoxMenuItem( "Show Index" );
		Hug = new JCheckBoxMenuItem( "Attach to parent" );
		JMenuItem Exit = new JMenuItem( "Close" );
		
		jm.add( ShowErrs );
		jm.add( ShowIndx );
		jm.add( Hug );
		jm.add( Exit );
		
		JMenuBar jmenbar = new JMenuBar();
		jmenbar.add( jm );
		ShowErrs.addActionListener( this );
		ShowIndx.addActionListener( this );
		Hug.addActionListener( this );
		Exit.addActionListener( this );
		
		this.setJMenuBar( jmenbar );
		table = null;
		jscr = null;
		hugg = true;
		PointedAtXindex = -1;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(this);
		DrawTable();
        WindowShower.show( this );
        
	}

	
	
	
	
	int ncols;
	// Draws the table corresponding to the DataSet and options
	private void DrawTable() {
		
		int pointedAtIndex = DS.getPointedAtIndex();
		if ( pointedAtIndex == DataSet.INVALID_INDEX )
			pointedAtIndex = 0;
		if ( jscr != null )
			remove( jscr );
		jscr = null;

		ncols = 2;
		if ( ShowErrs.isSelected() )
			ncols++;
		if ( ShowIndx.isSelected() )
			ncols++;
		
		
		
		Data db = DS.getData_entry( pointedAtIndex );
		float x = DS.getPointedAtX();
		if ( Float.isNaN( x ) )
			PointedAtXindex = -1;
		else
			PointedAtXindex = db.getX_scale().getI_GLB( x );
		setTitle( DS.toString() + "::" + db.toString() );
		
		
		
		table = new JTable( new MTableModel( db,ShowErrs.isSelected(), ShowIndx.isSelected() ));
		table.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
		jscr = new JScrollPane( table );
		getContentPane( ).add( jscr );
		ShowTable();
		
		//WindowShower.show(this);
	}
	
	
	
	//Moves the JFrame to the correct spot with the correct size
	// and highlights the pointed at X
	private void ShowTable(){
		
		  if( Hug == null )
			  return;
		  
       
          
          //Center the Viewport
          if( PointedAtXindex >= 0 )if( jscr != null ){
        	table.changeSelection( PointedAtXindex,0, false, false);  
        	java.awt.Rectangle R = table.getCellRect( PointedAtXindex,0,false );        
        	R.y = Math.max( 0,R.y-getSize().height/2 );
        	JViewport vpt = jscr.getViewport();
        	vpt.setViewPosition( new Point( R.x, R.y ) );
        	jscr.setViewport( vpt );
        	
          }
        
          
          if( (Hug.isSelected() )&&( parent != null )&&hugg ){
         	
         	 java.awt.Rectangle D = parent.getBounds(); 
         	 setBounds( D.x + D.width, D.y, ncols*80 , D.height );
           }else{
          	
 		      setSize( ncols*80,(int)( getToolkit().getScreenSize().height*.8 ) );
           }
         
          //WindowShower.show( this );
          table.invalidate();
          table.repaint();
          jscr.invalidate();
          jscr.repaint();
          invalidate();
          repaint();
          validate();
          //pack();
          jscr.invalidate();
          jscr.repaint();
          table.invalidate();
          table.repaint();
          
		
		 
	}
	
	
	/**
	 * Attempts to remove all connections and destroy all resources for this JFrame
	 *
	 */
	private void destroy(){
		DS.deleteIObserver( this );
		if( parent != null){
		  parent.removeComponentListener( this );
		  parent.removeWindowListener( this );
		}

		removeWindowListener(this);
        parent = null;
		setVisible( false );
		dispose();
		try{
		   super.finalize();
		}catch( Throwable ss){
			ss.printStackTrace();
		}
		
		
	}
	
	/**
	 * Main test program
	 * @param args  Runfile, group index, pointed at x
	 */
	public static void main( String[] args ) {
		
		DataSet[] DSS=null;
		try{
			DSS = ScriptUtil.load( args[ 0 ] );
		}catch( Exception ss ){
			System.exit( 0 );
		}
	    DataSet DS = DSS[ DSS.length-1 ];
	    
	    FinishJFrame jf = new FinishJFrame( "parent" );
	    jf.setSize( 600,600 );
	    WindowShower.show( jf );
	    
	    
	    DS.setPointedAtIndex(( new Integer( args[ 1 ] ) ).intValue() );
	    DS.setPointedAtX( ( new Float( args[ 2 ] ) ).floatValue() );
	    new QuickTableViewer( jf , DS );
	}

	 
	
	//-------------------- window listener methods -------------------------------
	public void windowActivated(WindowEvent e) {
		
	}

	
	
	public void windowClosed(WindowEvent e) {
	

		if( e.getSource().equals( this)){
			
			destroy();
			parent = null;
			hugg = false;
		}else{
			if(parent != null)parent.removeWindowListener( this);
			parent = null;
			hugg = false;
		}
		
		parent = null;
		hugg = false;
		
	}

	
	
	public void windowClosing(WindowEvent e) {
		
		

		if( e.getSource().equals( this)){
		
			destroy();
			parent = null;
			hugg = false;
		}else{
			if(parent != null)parent.removeWindowListener( this);
			parent = null;
			hugg = false;	
		}
		
		
		
		
	}

	
	
	public void windowDeactivated(WindowEvent e) {
		
		
		
		
		
	}

	
	
	public void windowDeiconified(WindowEvent e) {

       
		
		hugg = true;
		if( Hug.isSelected())ShowTable();
	}

	
	
	public void windowIconified(WindowEvent e) {

        if( e.getSource().equals(this))
        	return;
		
		hugg = false;

	}


	
	public void windowOpened(WindowEvent e) {
        if( e.getSource().equals(this))
        	return;
		
		hugg = true;
		if( Hug.isSelected())ShowTable();
		
	}

	
	// ----------------------IObserver Method-----------------------

	public void update(java.lang.Object observed_obj, java.lang.Object reason) {

		
		if (reason.equals(IObserver.POINTED_AT_CHANGED))
			DrawTable();

	}

	
	
	//------------------ ActionListenr method-------------------------
	public void actionPerformed(ActionEvent evt) {

		
		if( evt.getActionCommand().equals( "Close")){
			removeAll();
			table=null;
			jscr=null;
			destroy();
			
			hugg = false;
			parent = null;
			return;
		}
		DrawTable();
	}

	
	
	//------------------ ComponentListener  methods----------------
	public void componentHidden(ComponentEvent e) {

		
		hugg = false;
	}

	public void componentMoved(ComponentEvent e) {

		
		if( Hug.isSelected())ShowTable();
	}

	public void componentResized(ComponentEvent e) {

		
		if( Hug.isSelected())ShowTable();
	}

	public void componentShown(ComponentEvent e) {

		
		hugg = true;
		if( Hug.isSelected())ShowTable();
		
	}
	
  /**
   * Table model for the data so not all data has to be stoews.
   * @author Ruth
   *
   */

   class MTableModel implements TableModel{
	  Data db;
	  boolean showErrs,showInd;
	  float[] yy, ee;
	  public MTableModel(Data db, boolean showErrs, boolean showInd){
		   this.db = db;
		   this.showErrs = showErrs;
		   this.showInd = showInd;
		   yy= db.getY_values();
		   ee=db.getErrors();
		   
	   }
	  
	  
	  //No changes are allowed
	  public void 	addTableModelListener(TableModelListener l){}
	  
	  
	  public Class 	getColumnClass(int columnIndex){
		  
		  return String.class;
		  
	  }
	  
	  
	  public int 	getColumnCount(){
		  
		  int c =2;
		  if( showErrs)c++;
		  if( showInd)c++;
		  return c;
		  
	  }
	  
	  
	  public String getColumnName(int columnIndex){
		  
		  if(columnIndex ==0)
			  return "x vals";
		  
		  if( columnIndex ==1)
			  return "intensity";
		  
		  if( columnIndex == 2)
			  if(showErrs)
				  return "Errors";
			  else 
				  return "Index";
		  
		  return "Index";
		  
				  
	  }
	  
	  
	  
	  public int 	getRowCount(){
		  
		  return db.getX_scale().getNum_x();
		  
	  }
	  
	  
	  public Object getValueAt(int rowIndex, int columnIndex){
		  
		  if( columnIndex ==0)
			  return db.getX_scale().getX( rowIndex);
		  
		  if(columnIndex == 1){
			  
			  if( rowIndex < yy.length)
				  return ""+yy[rowIndex];
			  else
				  return "";
		  }
		  
		  if( columnIndex ==2)
			  if( showErrs)
				 if( rowIndex < ee.length)
					 return ""+ee[rowIndex];
				 else
					 return "";
			  else
				  return ""+rowIndex;
		  
				 
		  if( columnIndex ==3)
			  return ""+rowIndex;
		  
		  return"";
		
	  }
	  
	  
	  public boolean 	isCellEditable(int rowIndex, int columnIndex){
		  
		   return false;
		   
	  }
	  
	  
	  
	  public void 	removeTableModelListener(TableModelListener l){}
	  
	  
	  public void 	setValueAt(Object aValue, int rowIndex, int columnIndex){}
	  
	  
   }
}
