/*
 * File:  PrintableDocumentConstants.java
 *
 * Copyright (C) 2004 Dominic Kramer
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
 *           Dominic Kramer <kramerd@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA and by
 * the National Science Foundation under grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 */
 package devTools.Hawk.classDescriptor.tools.printing;

/**
 * @author Dominic Kramer
 */
public class PrintableDocumentConstants
{
	public static final String BOLD = "bold";
	public static final String ITALIC = "italic";
	public static final String UNDERLINE = "underline";
	public static final String PLAIN = "plain";
	
	public static final String SECTION_HEADER = "section_header";
	public static final String END_HEADER = "end_header";
	
	public static final String PACKAGE_LIST = "package_list";

	public static final String UML_LIST_ALPHABETICAL = "uml_list_alphabetical";
	public static final String UML_LIST_BY_PACKAGE = "uml_list_by_package";
	
	public static final String SHORTENED_SOURCE_LIST_ALPHABETICAL = "shortened_source_list_alphabetical";
	public static final String SHORTENED_SOURCE_LIST_BY_PACKAGE = "shortened_source_by_package";
	
	public static final String UML_SHORTENED_SOURCE_GROUPED_ALPHABETICAL = "uml_shortened_source_grouped_alphabetically";
	public static final String UML_SHORTENED_SOURCE_GROUPED_BY_PACKAGE = "uml_shortened_source_grouped_by_package";
	
	/** Private because this class has only static members and methods. */
	private PrintableDocumentConstants(){}
}
