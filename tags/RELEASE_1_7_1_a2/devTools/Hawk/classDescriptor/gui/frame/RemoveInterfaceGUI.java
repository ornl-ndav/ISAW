/*
 * Created on Mar 9, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package devTools.Hawk.classDescriptor.gui.frame;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import devTools.Hawk.classDescriptor.modeledObjects.Interface;
import devTools.Hawk.classDescriptor.modeledObjects.InterfaceDefn;
import devTools.Hawk.classDescriptor.modeledObjects.Project;
import devTools.Hawk.classDescriptor.tools.InterfaceUtilities;

/**
 * 
 * @author Dominic Kramer
 */
public class RemoveInterfaceGUI extends JFrame implements ActionListener, TreeSelectionListener
{
	protected JTree tree;
	protected DefaultTreeModel model;
	protected Project project;
	
	public RemoveInterfaceGUI(Project pro, boolean packageShortJava, boolean packageShortOther, boolean classShortJava, boolean classShortOther)
	{
		project = pro;
		
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(pro.getProjectName()+"'s Packages");
		
		model = new DefaultTreeModel(rootNode);
		tree = new JTree(model);
		tree.setCellRenderer(new PackageRenderer());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
//		tree.putClientProperty("JTree.lineStyle","None");
		ToolTipManager.sharedInstance().registerComponent(tree);
		fillTree(rootNode,packageShortJava,packageShortOther,classShortJava,classShortOther);
		
		setTitle("Remove Classes or Interfaces from Project "+project.getProjectName());
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(1,1));
		
		JScrollPane scrollPane = new JScrollPane(tree);
		mainPanel.add(scrollPane);
		getContentPane().add(mainPanel);
		pack();
		
		setVisible(true);
		JOptionPane opPane = new JOptionPane();
		JOptionPane.showMessageDialog(opPane,"This window will allow you to remove classes and interfaces from a project.\n" +
			"Currently it is under construction.  However, it gives you a view of how the\nfuture tree design will look."
			,"Note"
			,JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void fillTree(DefaultMutableTreeNode root, boolean packageShortJava, boolean packageShortOther, boolean classShortJava, boolean classShortOther)
	{
		Vector packageVec = InterfaceUtilities.getVectorOfVectorOfInterfaces(project.getInterfaceVec(), packageShortJava, packageShortOther, classShortJava, classShortOther);
		Vector vec = new Vector();
		DefaultMutableTreeNode packageNode = new DefaultMutableTreeNode(new PackageNameTreeNode());
		InterfaceTreeNode intfNode = new InterfaceTreeNode();
		for (int i=0; i<packageVec.size(); i++)
		{
			vec = (Vector)packageVec.elementAt(i);
			packageNode = new DefaultMutableTreeNode(new PackageNameTreeNode(((Interface)vec.elementAt(0)).getPgmDefn().getPackage_Name(packageShortJava,packageShortOther),packageShortJava,packageShortOther));
			root.add(packageNode);
			for (int j=0; j<vec.size(); j++)
				packageNode.add(new DefaultMutableTreeNode(new InterfaceTreeNode( (Interface)vec.elementAt(j),classShortJava,classShortOther )));
		}
		
		tree.expandPath(new TreePath(root));
		model.reload();
	}
	
	public ImageIcon getImageIcon(String location)
	{
		URL imageURL = ClassLoader.getSystemClassLoader().getResource(location);
		ImageIcon icon = null;
		if (imageURL != null)
			icon = new ImageIcon(imageURL);
		
		return icon;
	}
	
	public void actionPerformed(ActionEvent e)
	{
	}
	
	public void valueChanged(TreeSelectionEvent event)
	{
	}
	
	public class PackageRenderer extends DefaultTreeCellRenderer
	{
		public Component getTreeCellRendererComponent(JTree tree, Object ob, boolean isSelected, boolean isExpanded, boolean isLeaf, int rowNum, boolean isFocused)
		{
			super.getTreeCellRendererComponent(tree,ob,isSelected,isExpanded,isLeaf,rowNum,isFocused);
			
			if (isLeaf)
			{
				if ( (ob instanceof DefaultMutableTreeNode) && (((DefaultMutableTreeNode)ob).getUserObject() instanceof InterfaceTreeNode))
				{
					ImageIcon icon = null;
					InterfaceDefn intfDefn = ((InterfaceTreeNode)((DefaultMutableTreeNode)ob).getUserObject()).getInterface().getPgmDefn();
					if (intfDefn.isClass())
					{
						if (intfDefn.isAbstract())
						{
							icon = getImageIcon("devTools/Hawk/classDescriptor/pixmaps/abstract_class_icon.png");
							setFont(new Font("Plain",Font.PLAIN,12));
							setToolTipText("Abstract Class "+intfDefn.getInterface_name());
						}
						else
						{
							setToolTipText("Class "+intfDefn.getInterface_name());
							icon = getImageIcon("devTools/Hawk/classDescriptor/pixmaps/class_icon.png");
							setFont(new Font("Plain",Font.PLAIN,12));
						}
					}
					else
					{
						icon = getImageIcon("devTools/Hawk/classDescriptor/pixmaps/interface_icon.png");
						setFont(new Font("Italic",Font.ITALIC,12));
						setToolTipText("Interface "+intfDefn.getInterface_name());
					}
					
					if (icon != null)
						setIcon(icon);
				}
			}
			else if (rowNum == 0) //then it is the root node
			{
				setFont(new Font("Plain",Font.BOLD,13));
				setToolTipText("Project "+project.getProjectName());
				ImageIcon icon = getImageIcon("devTools/Hawk/classDescriptor/pixmaps/project_icon.png");
				if (icon != null)
					setIcon(icon);
			}
			else
			{
				setFont(new Font("Plain",Font.BOLD,12));
				if ( (ob instanceof DefaultMutableTreeNode) && (((DefaultMutableTreeNode)ob).getUserObject() instanceof PackageNameTreeNode))
					setToolTipText("Package "+((PackageNameTreeNode)((DefaultMutableTreeNode)ob).getUserObject()).getPackageName() );
				ImageIcon icon = getImageIcon("devTools/Hawk/classDescriptor/pixmaps/package_icon.png");
				if (icon != null)
					setIcon(icon);
			}
			
			return this;
		}
	}
	
	public class InterfaceTreeNode
	{
		protected boolean shortenJava;
		protected boolean shortenOther;
		protected Interface selectedInterface;
		
		public InterfaceTreeNode()
		{
			selectedInterface = new Interface();
			shortenJava = true;
			shortenOther = true;
		}
		
		public InterfaceTreeNode(Interface intf, boolean shortJava, boolean shortOther)
		{
			selectedInterface = intf;
			shortenJava = shortJava;
			shortenOther = shortOther;
		}
		
		public Interface getInterface()
		{
			return selectedInterface;
		}
		
		public String toString()
		{
			return selectedInterface.getPgmDefn().getInterface_name(shortenJava,shortenOther);
		}
	}
	
	public class PackageNameTreeNode
	{
		protected boolean shortenJava;
		protected boolean shortenOther;
		protected String name;
		
		public PackageNameTreeNode()
		{
			name = "";
			shortenJava = false;
			shortenOther = false;
		}
		
		public PackageNameTreeNode(String nm, boolean shortJava, boolean shortOther)
		{
			name = nm;
			shortenJava = shortJava;
			shortenOther = shortOther;
		}
		
		public String getPackageName()
		{
			return name;
		}
		
		public String toString()
		{
			return InterfaceUtilities.getAbbreviatedName(name,shortenJava,shortenOther);
		}
	}
}
