/*
 * File: RomanDuplex.java
 *
 * Copyright (C) 2004, Dennis Mikkelson
 *
 * DO NOT EDIT.  THIS FILE WAS AUTOMATICALLY GENERATED BY
 * THE FileStrokeFont class.
 *
 * This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software
 * Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330
 * Boston, MA  02111-1307, USA.
 *
 * Contact: 
 * Dennis Mikkelson <mikkelsond@uwstout.edu>
 * Department of Mathematics, Statistics and Computer Science
 * University of Wisconsin-Stout
 * Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation
 * under grant number DMR-0218882.
 *
 * For further information see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2007/08/14 00:03:29  dennis
 * Major update to JSR231 based version from UW-Stout repository.
 *
 * Revision 1.1  2005/10/14 04:02:21  dennis
 * Copied into local CVS repository from CVS repository at IPNS.
 *
 * Revision 1.1  2005/07/19 13:02:15  dennis
 * Stroke font data files.
 *
 * Revision 1.2  2004/06/18 19:46:24  dennis
 * Removed import of GL_ThreeD package, no longer needed since
 * StrokeFont moved to Fonts package.
 *
 * Revision 1.1  2004/06/02 14:11:27  dennis
 * Hershey Font file for use with OpenGL, generated from plain text form
 * of font data, using the FileStrokeFont class.
 *
 */

package SSG_Tools.Fonts;

/**
 * This class stores the data for the font: RomanDuplex
 * that was derived from the public domain Hershey font data,
 * distributed on the USENET network.  The Hershey Fonts were
 * originally digitized by Dr. Allen V. Hershey, a
 * mathematical physicist, while working at the U.S. Naval
 * Weapons Laboratory in Dahlgren, Virginia during the 1960's
 * The USENET distribution was originally created by:
 *
 * James Hurt
 * Cognition, Inc.
 * 900 Technology Park Drive
 * Billerica, MA 01821
 *
 * The data in this class was produced from the *.txf file
 * form of the data produced by Dennis Mikkelson from the 
 * data distributed on USENET.
 */

public class RomanDuplex extends StrokeFont
{
 private static short my_char_start[] = {
 0, 1, 25, 47, 59, 110, 142, 191, 202, 222, 242, 281, 297, 321,
 329, 345, 353, 395, 407, 441, 489, 507, 560, 622, 634, 702, 764,
 796, 836, 840, 856, 860, 918, 974, 994, 1038, 1076, 1108, 1135, 1156,
 1200, 1222, 1230, 1250, 1272, 1286, 1312, 1332, 1372, 1399, 1447, 1481, 1524,
 1539, 1563, 1577, 1603, 1619, 1636, 1656, 1668, 1671, 1683, 1691, 1694, 1702,
 1738, 1774, 1806, 1842, 1878, 1902, 1950, 1975, 1999, 2023, 2045, 2053, 2095,
 2120, 2156, 2192, 2228, 2249, 2299, 2315, 2340, 2354, 2380, 2396, 2413, 2433,
 2473, 2476, 2516 };

 private static short my_char_width[] = {
 16, 11, 18, 21, 19, 24, 25, 9, 14, 14, 16, 25, 11, 25, 11, 23, 20, 20, 20,
 20, 20, 20, 20, 20, 20, 20, 11, 11, 24, 25, 24, 19, 27, 20, 20, 21, 21,
 19, 18, 21, 22, 9, 17, 21, 17, 24, 22, 22, 20, 22, 20, 20, 17, 22, 20,
 26, 20, 19, 20, 14, 14, 14, 22, 18, 10, 20, 20, 18, 20, 18, 14, 20, 20,
 9, 9, 19, 9, 31, 20, 19, 20, 20, 14, 17, 11, 20, 16, 24, 18, 16, 18,
 14, 8, 14, 24 };

 private static short my_font_x[] = {
 0, 65, 65, 66, 0, 65, 66, 66, 0, 65, 64, 64, 65, 66, 67, 67, 66, 65, 0,
 65, 65, 66, 66, 65, 0, 65, 64, 64, 0, 65, 64, 0, 65, 66, 64, 0, 74,
 73, 73, 0, 74, 73, 0, 74, 75, 73, 0, 71, 64, 0, 77, 70, 0, 64, 78,
 0, 63, 77, 0, 69, 69, 70, 0, 69, 70, 70, 0, 74, 76, 74, 71, 68, 65,
 63, 63, 64, 65, 73, 74, 75, 75, 74, 71, 68, 66, 65, 0, 74, 73, 71, 68,
 65, 64, 64, 65, 73, 75, 76, 76, 75, 74, 71, 68, 65, 63, 65, 0, 75, 72,
 0, 81, 63, 0, 68, 70, 70, 69, 67, 65, 63, 63, 64, 66, 68, 70, 73, 76,
 79, 81, 0, 77, 75, 74, 74, 76, 78, 80, 81, 81, 79, 77, 0, 81, 80, 81,
 82, 82, 81, 80, 79, 78, 76, 74, 72, 70, 67, 64, 63, 63, 64, 70, 72, 73,
 73, 72, 70, 68, 67, 67, 68, 70, 75, 77, 80, 81, 82, 82, 0, 67, 65, 64,
 64, 65, 67, 0, 67, 68, 76, 78, 80, 0, 65, 64, 64, 0, 65, 64, 0, 65,
 66, 64, 0, 71, 69, 67, 65, 64, 64, 65, 67, 69, 71, 0, 69, 67, 66, 65,
 65, 66, 67, 69, 0, 63, 65, 67, 69, 70, 70, 69, 67, 65, 63, 0, 65, 67,
 68, 69, 69, 68, 67, 65, 0, 68, 67, 69, 68, 0, 68, 68, 0, 68, 69, 67,
 68, 0, 63, 64, 72, 73, 0, 63, 73, 0, 63, 63, 73, 73, 0, 73, 72, 64,
 63, 0, 73, 63, 0, 73, 73, 63, 63, 0, 72, 72, 73, 0, 72, 73, 73, 0,
 64, 81, 81, 0, 64, 64, 81, 0, 67, 66, 65, 64, 64, 65, 66, 67, 67, 66,
 64, 0, 65, 65, 66, 66, 65, 0, 66, 67, 0, 67, 66, 0, 64, 81, 81, 0,
 64, 64, 81, 0, 65, 64, 64, 65, 66, 67, 67, 66, 65, 0, 65, 65, 66, 66,
 65, 0, 80, 62, 63, 0, 80, 81, 63, 0, 69, 66, 64, 63, 63, 64, 66, 69,
 71, 74, 76, 77, 77, 76, 74, 71, 69, 0, 67, 65, 64, 64, 65, 67, 0, 66,
 69, 71, 74, 0, 73, 75, 76, 76, 75, 73, 0, 74, 71, 69, 66, 0, 66, 68,
 71, 71, 0, 66, 66, 68, 70, 70, 71, 0, 64, 64, 65, 66, 68, 72, 74, 75,
 76, 76, 75, 73, 64, 0, 64, 65, 65, 66, 68, 72, 74, 75, 75, 74, 72, 63,
 0, 64, 77, 77, 0, 63, 77, 0, 65, 76, 69, 0, 65, 65, 75, 0, 75, 68,
 0, 69, 71, 74, 76, 77, 77, 76, 74, 71, 68, 65, 64, 63, 64, 0, 68, 71,
 74, 76, 0, 72, 75, 76, 76, 75, 72, 0, 76, 74, 71, 68, 65, 64, 0, 67,
 64, 0, 73, 73, 74, 0, 74, 74, 0, 74, 63, 78, 0, 73, 64, 0, 64, 78,
 78, 0, 65, 64, 0, 66, 65, 0, 65, 75, 75, 0, 66, 75, 0, 65, 68, 71,
 74, 76, 77, 77, 76, 74, 71, 68, 65, 64, 63, 64, 0, 64, 65, 67, 71, 74,
 76, 0, 72, 75, 76, 76, 75, 72, 0, 76, 74, 71, 68, 65, 64, 0, 67, 64,
 0, 74, 75, 76, 75, 72, 70, 67, 65, 64, 64, 65, 67, 70, 71, 74, 76, 77,
 77, 76, 74, 71, 70, 67, 65, 0, 75, 72, 70, 67, 0, 68, 66, 65, 65, 66,
 69, 0, 65, 67, 70, 71, 74, 76, 0, 72, 75, 76, 76, 75, 72, 0, 76, 74,
 71, 70, 67, 65, 0, 69, 66, 65, 0, 63, 77, 67, 0, 63, 63, 76, 0, 76,
 66, 67, 0, 68, 65, 64, 64, 65, 66, 68, 72, 74, 75, 76, 76, 75, 72, 68,
 65, 64, 64, 65, 66, 68, 72, 74, 75, 76, 76, 75, 72, 68, 0, 66, 65, 65,
 66, 68, 72, 74, 76, 77, 77, 76, 75, 72, 68, 65, 64, 63, 63, 64, 66, 68,
 72, 74, 75, 75, 74, 0, 75, 72, 68, 65, 0, 64, 67, 0, 73, 76, 0, 75,
 73, 70, 69, 66, 64, 63, 63, 64, 66, 69, 70, 73, 75, 76, 76, 75, 73, 70,
 68, 65, 64, 65, 66, 0, 75, 74, 71, 0, 75, 73, 70, 69, 66, 64, 0, 68,
 65, 64, 64, 65, 68, 0, 64, 66, 69, 70, 73, 75, 0, 71, 74, 75, 75, 74,
 72, 0, 73, 70, 68, 65, 0, 65, 64, 64, 65, 66, 67, 67, 66, 65, 0, 65,
 65, 66, 66, 65, 0, 65, 64, 64, 65, 66, 67, 67, 66, 65, 0, 65, 65, 66,
 66, 65, 0, 65, 64, 64, 65, 66, 67, 67, 66, 65, 0, 65, 65, 66, 66, 65,
 0, 67, 66, 65, 64, 64, 65, 66, 67, 67, 66, 64, 0, 65, 65, 66, 66, 65,
 0, 66, 67, 0, 67, 66, 0, 80, 64, 80, 0, 64, 81, 81, 0, 64, 64, 81,
 0, 64, 81, 81, 0, 64, 64, 81, 0, 64, 80, 64, 0, 63, 63, 64, 65, 68,
 71, 74, 75, 76, 76, 75, 74, 72, 69, 0, 63, 64, 64, 65, 68, 71, 74, 75,
 75, 74, 72, 69, 0, 64, 67, 0, 72, 75, 0, 75, 71, 0, 69, 69, 70, 70,
 0, 69, 68, 68, 69, 70, 71, 71, 70, 69, 0, 69, 69, 70, 70, 69, 0, 78,
 77, 75, 72, 70, 69, 68, 68, 69, 71, 74, 76, 77, 0, 72, 70, 69, 69, 70,
 71, 0, 78, 77, 77, 79, 81, 83, 84, 84, 83, 82, 80, 78, 75, 72, 69, 67,
 65, 64, 63, 63, 64, 65, 67, 69, 72, 75, 78, 80, 81, 0, 79, 78, 78, 79,
 0, 70, 62, 0, 70, 63, 62, 0, 70, 77, 78, 0, 70, 78, 0, 65, 75, 0,
 64, 76, 0, 64, 64, 0, 65, 65, 0, 64, 72, 75, 76, 77, 77, 76, 75, 72,
 0, 65, 72, 75, 76, 76, 75, 72, 0, 65, 72, 75, 76, 77, 77, 76, 75, 72,
 64, 0, 65, 72, 75, 76, 76, 75, 72, 65, 0, 78, 77, 75, 73, 69, 67, 65,
 64, 63, 63, 64, 65, 67, 69, 73, 75, 77, 78, 0, 78, 77, 76, 75, 73, 69,
 67, 65, 64, 64, 65, 67, 69, 73, 75, 76, 77, 78, 0, 64, 64, 0, 65, 65,
 0, 64, 71, 74, 76, 77, 78, 78, 77, 76, 74, 71, 64, 0, 65, 71, 74, 75,
 76, 77, 77, 76, 75, 74, 71, 65, 0, 64, 64, 0, 65, 65, 0, 64, 76, 0,
 65, 76, 76, 0, 65, 71, 71, 0, 65, 71, 0, 65, 76, 76, 0, 64, 76, 0,
 64, 64, 0, 65, 65, 64, 0, 64, 76, 0, 65, 76, 76, 0, 65, 71, 71, 0,
 65, 71, 0, 78, 77, 75, 73, 69, 67, 65, 64, 63, 63, 64, 65, 67, 69, 73,
 75, 77, 78, 78, 73, 0, 78, 77, 76, 75, 73, 69, 67, 66, 65, 64, 64, 65,
 66, 67, 69, 73, 75, 76, 77, 77, 73, 73, 0, 64, 64, 0, 64, 65, 65, 64,
 0, 78, 77, 77, 78, 0, 78, 78, 0, 65, 77, 0, 65, 77, 0, 64, 64, 65,
 0, 64, 65, 65, 0, 72, 72, 71, 69, 67, 65, 64, 63, 0, 72, 73, 73, 72,
 71, 69, 67, 65, 64, 63, 0, 64, 64, 65, 0, 64, 65, 65, 0, 78, 77, 65,
 0, 78, 65, 0, 68, 77, 78, 0, 69, 78, 0, 64, 64, 0, 64, 65, 65, 0,
 65, 76, 76, 0, 64, 76, 0, 64, 64, 0, 65, 65, 64, 0, 65, 72, 0, 64,
 72, 0, 80, 72, 0, 79, 72, 0, 79, 79, 80, 0, 80, 80, 0, 64, 64, 0,
 65, 65, 64, 0, 65, 78, 0, 64, 77, 0, 77, 77, 0, 77, 78, 78, 0, 69,
 67, 65, 64, 63, 63, 64, 65, 67, 69, 73, 75, 77, 78, 79, 79, 78, 77, 75,
 73, 69, 0, 70, 67, 65, 64, 64, 65, 67, 70, 72, 75, 77, 78, 78, 77, 75,
 72, 70, 0, 64, 64, 0, 65, 65, 64, 0, 64, 73, 75, 76, 77, 77, 76, 75,
 73, 65, 0, 65, 73, 75, 76, 76, 75, 73, 65, 0, 69, 67, 65, 64, 63, 63,
 64, 65, 67, 69, 73, 75, 77, 78, 79, 79, 78, 77, 75, 73, 69, 0, 70, 67,
 65, 64, 64, 65, 67, 70, 72, 75, 77, 78, 78, 77, 75, 72, 70, 0, 72, 77,
 78, 0, 72, 73, 78, 0, 64, 64, 0, 65, 65, 64, 0, 64, 72, 75, 76, 77,
 77, 76, 75, 72, 65, 0, 65, 72, 75, 76, 76, 75, 72, 65, 0, 70, 76, 77,
 0, 71, 77, 0, 77, 75, 72, 68, 65, 63, 63, 64, 65, 67, 72, 74, 75, 76,
 76, 75, 72, 68, 66, 65, 63, 0, 77, 75, 74, 72, 68, 65, 64, 64, 65, 67,
 72, 74, 76, 77, 77, 75, 72, 68, 65, 63, 0, 68, 68, 0, 69, 69, 68, 0,
 62, 75, 75, 0, 62, 62, 75, 0, 64, 64, 65, 67, 70, 72, 75, 77, 78, 78,
 0, 64, 65, 65, 66, 67, 70, 72, 75, 76, 77, 77, 78, 0, 62, 70, 0, 62,
 63, 70, 0, 78, 77, 70, 0, 78, 70, 0, 62, 68, 0, 62, 63, 68, 0, 73,
 68, 0, 73, 68, 0, 73, 78, 0, 73, 78, 0, 84, 83, 78, 0, 84, 78, 0,
 63, 76, 77, 0, 63, 64, 77, 0, 77, 76, 63, 0, 77, 64, 63, 0, 62, 69,
 69, 70, 0, 62, 63, 70, 0, 77, 76, 69, 0, 77, 70, 70, 0, 76, 63, 0,
 77, 64, 0, 63, 77, 0, 63, 63, 76, 0, 64, 77, 77, 0, 63, 77, 0, 64,
 64, 0, 65, 65, 0, 64, 71, 0, 64, 71, 0, 60, 74, 0, 69, 69, 0, 70,
 70, 0, 63, 70, 0, 63, 70, 0, 63, 71, 79, 0, 63, 71, 79, 0, 60, 78,
 0, 65, 66, 65, 64, 64, 65, 66, 0, 75, 75, 76, 0, 75, 76, 76, 0, 75,
 73, 71, 68, 66, 64, 63, 63, 64, 66, 68, 71, 73, 75, 0, 75, 71, 68, 66,
 65, 64, 64, 65, 66, 68, 71, 75, 0, 64, 64, 65, 0, 64, 65, 65, 0, 65,
 67, 69, 72, 74, 76, 77, 77, 76, 74, 72, 69, 67, 65, 0, 65, 69, 72, 74,
 75, 76, 76, 75, 74, 72, 69, 65, 0, 75, 73, 71, 68, 66, 64, 63, 63, 64,
 66, 68, 71, 73, 75, 0, 75, 74, 73, 71, 68, 66, 65, 64, 64, 65, 66, 68,
 71, 73, 74, 75, 0, 75, 75, 76, 0, 75, 76, 76, 0, 75, 73, 71, 68, 66,
 64, 63, 63, 64, 66, 68, 71, 73, 75, 0, 75, 71, 68, 66, 65, 64, 64, 65,
 66, 68, 71, 75, 0, 64, 75, 75, 74, 73, 71, 68, 66, 64, 63, 63, 64, 66,
 68, 71, 73, 75, 0, 64, 74, 74, 73, 71, 68, 66, 65, 64, 64, 65, 66, 68,
 71, 73, 74, 75, 0, 71, 69, 67, 66, 66, 67, 0, 71, 71, 69, 67, 0, 68,
 67, 67, 0, 63, 70, 70, 0, 63, 63, 70, 0, 76, 75, 75, 74, 73, 71, 69,
 67, 66, 64, 0, 76, 76, 75, 73, 71, 68, 66, 64, 0, 75, 73, 71, 68, 66,
 64, 63, 63, 64, 66, 68, 71, 73, 75, 0, 75, 71, 68, 66, 65, 64, 64, 65,
 66, 68, 71, 75, 0, 64, 64, 65, 0, 64, 65, 65, 0, 65, 68, 70, 73, 75,
 76, 76, 0, 65, 68, 70, 72, 74, 75, 75, 76, 0, 64, 63, 63, 64, 65, 66,
 66, 65, 64, 0, 64, 64, 65, 65, 64, 0, 64, 64, 65, 0, 64, 65, 65, 0,
 64, 63, 63, 64, 65, 66, 66, 65, 64, 0, 64, 64, 65, 65, 64, 0, 64, 64,
 65, 0, 64, 65, 65, 0, 64, 64, 65, 0, 64, 65, 65, 0, 76, 75, 65, 0,
 76, 65, 0, 68, 74, 76, 0, 69, 76, 0, 64, 64, 65, 0, 64, 65, 65, 0,
 64, 64, 65, 0, 64, 65, 65, 0, 65, 68, 70, 73, 75, 76, 76, 0, 65, 68,
 70, 72, 74, 75, 75, 76, 0, 76, 79, 81, 84, 86, 87, 87, 0, 76, 79, 81,
 83, 85, 86, 86, 87, 0, 64, 64, 65, 0, 64, 65, 65, 0, 65, 68, 70, 73,
 75, 76, 76, 0, 65, 68, 70, 72, 74, 75, 75, 76, 0, 68, 66, 64, 63, 63,
 64, 66, 68, 71, 73, 75, 76, 76, 75, 73, 71, 68, 0, 68, 66, 65, 64, 64,
 65, 66, 68, 71, 73, 74, 75, 75, 74, 73, 71, 68, 0, 64, 64, 65, 0, 64,
 65, 65, 0, 65, 67, 69, 72, 74, 76, 77, 77, 76, 74, 72, 69, 67, 65, 0,
 65, 69, 72, 74, 75, 76, 76, 75, 74, 72, 69, 65, 0, 75, 75, 76, 0, 75,
 76, 76, 0, 75, 73, 71, 68, 66, 64, 63, 63, 64, 66, 68, 71, 73, 75, 0,
 75, 71, 68, 66, 65, 64, 64, 65, 66, 68, 71, 75, 0, 64, 64, 65, 0, 64,
 65, 65, 0, 65, 66, 68, 70, 73, 0, 65, 66, 68, 70, 73, 73, 0, 74, 73,
 70, 67, 64, 63, 64, 66, 71, 73, 0, 72, 73, 73, 72, 0, 73, 70, 67, 64,
 0, 65, 64, 63, 0, 74, 73, 72, 0, 73, 70, 67, 64, 0, 65, 64, 65, 0,
 64, 66, 71, 73, 74, 74, 73, 70, 67, 64, 63, 0, 65, 65, 66, 0, 65, 66,
 66, 0, 62, 69, 69, 0, 62, 62, 69, 0, 64, 64, 65, 67, 70, 72, 75, 0,
 64, 65, 65, 66, 68, 70, 72, 75, 0, 75, 75, 76, 0, 75, 76, 76, 0, 62,
 68, 0, 62, 63, 68, 0, 74, 73, 68, 0, 74, 68, 0, 63, 68, 0, 63, 64,
 68, 0, 72, 68, 0, 72, 68, 0, 72, 76, 0, 72, 76, 0, 81, 80, 76, 0,
 81, 76, 0, 63, 74, 75, 0, 63, 64, 75, 0, 75, 74, 63, 0, 75, 64, 63,
 0, 62, 68, 0, 62, 63, 68, 0, 74, 73, 68, 64, 0, 74, 68, 65, 64, 0,
 73, 63, 0, 75, 65, 0, 63, 75, 0, 63, 63, 73, 0, 65, 75, 75, 0, 63,
 75, 0, 69, 67, 66, 65, 65, 66, 67, 68, 68, 66, 0, 67, 66, 66, 67, 68,
 69, 69, 68, 64, 68, 69, 69, 68, 67, 66, 66, 67, 0, 66, 68, 68, 67, 66,
 65, 65, 66, 67, 69, 0, 64, 64, 0, 65, 67, 68, 69, 69, 68, 67, 66, 66,
 68, 0, 67, 68, 68, 67, 66, 65, 65, 66, 70, 66, 65, 65, 66, 67, 68, 68,
 67, 0, 68, 66, 66, 67, 68, 69, 69, 68, 67, 65, 0, 63, 63, 64, 66, 68,
 70, 74, 76, 78, 80, 81, 0, 63, 64, 66, 68, 70, 74, 76, 78, 80, 81, 81,
 0 };

 private static short my_font_y[] = {
 2, 72, 58, 58, 1, 72, 72, 58, 1, 54, 53, 52, 51, 51, 52, 53, 54, 54, 1,
 53, 52, 52, 53, 53, 2, 72, 71, 65, 1, 71, 65, 1, 72, 71, 65, 1, 72,
 71, 65, 1, 71, 65, 1, 72, 71, 65, 2, 76, 44, 1, 76, 44, 1, 63, 63,
 1, 57, 57, 2, 76, 47, 47, 1, 76, 76, 47, 1, 69, 69, 71, 72, 72, 71,
 69, 67, 65, 64, 60, 59, 57, 55, 53, 52, 52, 53, 54, 1, 69, 70, 71, 71,
 70, 69, 67, 65, 61, 59, 57, 55, 53, 52, 51, 51, 52, 54, 54, 1, 54, 52,
 2, 72, 51, 1, 72, 70, 68, 66, 65, 65, 67, 69, 71, 72, 72, 71, 70, 70,
 71, 72, 1, 58, 57, 55, 53, 51, 51, 52, 54, 56, 58, 58, 2, 64, 63, 62,
 63, 64, 65, 65, 64, 62, 57, 54, 52, 51, 51, 52, 54, 57, 59, 63, 65, 67,
 69, 71, 72, 71, 69, 67, 64, 61, 54, 52, 51, 51, 52, 53, 1, 51, 52, 54,
 57, 59, 61, 1, 67, 65, 54, 52, 51, 2, 72, 71, 65, 1, 71, 65, 1, 72,
 71, 65, 2, 76, 74, 71, 67, 62, 58, 53, 49, 46, 44, 1, 74, 70, 67, 62,
 58, 53, 50, 46, 2, 76, 74, 71, 67, 62, 58, 53, 49, 46, 44, 1, 74, 70,
 67, 62, 58, 53, 50, 46, 2, 72, 71, 61, 60, 1, 72, 60, 1, 72, 71, 61,
 60, 1, 69, 69, 63, 63, 1, 69, 63, 1, 69, 68, 64, 63, 1, 69, 69, 63,
 63, 1, 69, 63, 1, 69, 68, 64, 63, 2, 69, 52, 52, 1, 69, 69, 52, 1,
 61, 61, 60, 1, 61, 60, 60, 2, 52, 51, 51, 52, 53, 54, 54, 53, 50, 48,
 47, 1, 53, 52, 52, 53, 53, 1, 51, 50, 1, 52, 48, 2, 61, 61, 60, 1,
 61, 60, 60, 2, 54, 53, 52, 51, 51, 52, 53, 54, 54, 1, 53, 52, 52, 53,
 53, 2, 76, 44, 44, 1, 76, 76, 44, 2, 72, 71, 68, 63, 60, 55, 52, 51,
 51, 52, 55, 60, 63, 68, 71, 72, 72, 1, 71, 68, 63, 60, 55, 52, 1, 53,
 52, 52, 53, 1, 52, 55, 60, 63, 68, 71, 1, 70, 71, 71, 70, 2, 68, 69,
 72, 51, 1, 68, 67, 68, 70, 51, 51, 2, 67, 68, 70, 71, 72, 72, 71, 70,
 68, 66, 64, 61, 51, 1, 67, 67, 68, 70, 71, 71, 70, 68, 66, 64, 61, 51,
 1, 52, 52, 51, 1, 51, 51, 2, 72, 72, 63, 1, 72, 71, 71, 1, 72, 63,
 1, 64, 64, 63, 61, 58, 57, 54, 52, 51, 51, 52, 53, 55, 55, 1, 63, 63,
 62, 59, 1, 63, 61, 58, 57, 54, 52, 1, 56, 53, 52, 52, 53, 55, 1, 52,
 54, 2, 69, 51, 51, 1, 72, 51, 1, 72, 56, 56, 1, 69, 56, 1, 57, 57,
 56, 2, 72, 63, 1, 71, 64, 1, 72, 72, 71, 1, 71, 71, 1, 64, 65, 65,
 64, 62, 59, 57, 54, 52, 51, 51, 52, 53, 55, 55, 1, 63, 63, 64, 64, 63,
 60, 1, 64, 62, 59, 57, 54, 52, 1, 56, 53, 52, 52, 53, 55, 1, 52, 54,
 2, 71, 69, 69, 71, 72, 72, 71, 68, 63, 58, 54, 52, 51, 51, 52, 54, 57,
 58, 61, 63, 64, 64, 63, 61, 1, 70, 71, 71, 70, 1, 71, 68, 63, 58, 54,
 52, 1, 56, 53, 52, 52, 53, 56, 1, 52, 54, 57, 58, 61, 63, 1, 59, 62,
 63, 63, 62, 59, 1, 63, 61, 58, 2, 72, 72, 51, 1, 72, 71, 71, 1, 72,
 51, 51, 2, 72, 71, 69, 67, 65, 64, 63, 62, 61, 60, 58, 55, 53, 52, 52,
 53, 55, 58, 60, 61, 62, 63, 64, 65, 67, 69, 71, 72, 72, 1, 71, 69, 67,
 65, 64, 63, 62, 60, 58, 55, 53, 52, 51, 51, 52, 53, 55, 58, 60, 62, 63,
 64, 65, 67, 69, 71, 1, 70, 71, 71, 70, 1, 54, 52, 1, 52, 54, 2, 62,
 60, 59, 59, 60, 62, 65, 66, 69, 71, 72, 72, 71, 69, 65, 60, 55, 52, 51,
 51, 52, 54, 54, 52, 1, 65, 62, 60, 1, 64, 61, 60, 60, 61, 64, 1, 60,
 62, 65, 66, 69, 71, 1, 67, 70, 71, 71, 70, 67, 1, 71, 69, 65, 60, 55,
 52, 1, 53, 52, 52, 53, 2, 65, 64, 63, 62, 62, 63, 64, 65, 65, 1, 64,
 63, 63, 64, 64, 1, 54, 53, 52, 51, 51, 52, 53, 54, 54, 1, 53, 52, 52,
 53, 53, 2, 65, 64, 63, 62, 62, 63, 64, 65, 65, 1, 64, 63, 63, 64, 64,
 1, 52, 51, 51, 52, 53, 54, 54, 53, 50, 48, 47, 1, 53, 52, 52, 53, 53,
 1, 51, 50, 1, 52, 48, 2, 69, 60, 51, 2, 65, 65, 64, 1, 65, 64, 64,
 1, 57, 57, 56, 1, 57, 56, 56, 2, 69, 60, 51, 2, 67, 68, 70, 71, 72,
 72, 71, 70, 68, 66, 64, 63, 62, 61, 1, 67, 67, 68, 70, 71, 71, 70, 68,
 66, 64, 63, 62, 1, 69, 71, 1, 71, 69, 1, 65, 62, 1, 62, 58, 58, 62,
 1, 54, 53, 52, 51, 51, 52, 53, 54, 54, 1, 53, 52, 52, 53, 53, 2, 64,
 66, 67, 67, 66, 65, 62, 59, 57, 56, 56, 57, 59, 1, 67, 65, 62, 59, 57,
 56, 1, 67, 59, 57, 56, 56, 58, 61, 63, 66, 68, 70, 71, 72, 72, 71, 70,
 68, 66, 63, 60, 57, 55, 53, 52, 51, 51, 52, 53, 54, 1, 67, 59, 57, 56,
 2, 72, 51, 1, 69, 51, 51, 1, 69, 51, 51, 1, 72, 51, 1, 57, 57, 1,
 56, 56, 2, 72, 51, 1, 71, 52, 1, 72, 72, 71, 70, 68, 65, 63, 62, 61,
 1, 71, 71, 70, 68, 65, 63, 62, 1, 62, 62, 61, 60, 58, 55, 53, 52, 51,
 51, 1, 61, 61, 60, 58, 55, 53, 52, 52, 2, 67, 69, 71, 72, 72, 71, 69,
 67, 64, 59, 56, 54, 52, 51, 51, 52, 54, 56, 1, 67, 67, 69, 70, 71, 71,
 70, 67, 64, 59, 56, 53, 52, 52, 53, 54, 56, 56, 2, 72, 51, 1, 71, 52,
 1, 72, 72, 71, 69, 67, 64, 59, 56, 54, 52, 51, 51, 1, 71, 71, 70, 69,
 67, 64, 59, 56, 54, 53, 52, 52, 2, 72, 51, 1, 71, 52, 1, 72, 72, 1,
 71, 71, 72, 1, 62, 62, 61, 1, 61, 61, 1, 52, 52, 51, 1, 51, 51, 2,
 72, 51, 1, 71, 51, 51, 1, 72, 72, 1, 71, 71, 72, 1, 62, 62, 61, 1,
 61, 61, 2, 67, 69, 71, 72, 72, 71, 69, 67, 64, 59, 56, 54, 52, 51, 51,
 52, 54, 56, 60, 60, 1, 67, 67, 69, 70, 71, 71, 70, 69, 67, 64, 59, 56,
 54, 53, 52, 52, 53, 54, 56, 59, 59, 60, 2, 72, 51, 1, 72, 72, 51, 51,
 1, 72, 72, 51, 51, 1, 72, 51, 1, 62, 62, 1, 61, 61, 2, 72, 51, 51,
 1, 72, 72, 51, 2, 72, 56, 53, 52, 52, 53, 56, 56, 1, 72, 72, 56, 53,
 52, 51, 51, 52, 53, 56, 2, 72, 51, 51, 1, 72, 72, 51, 1, 72, 72, 60,
 1, 72, 59, 1, 63, 51, 51, 1, 63, 51, 2, 72, 51, 1, 72, 72, 52, 1,
 52, 52, 51, 1, 51, 51, 2, 72, 51, 1, 67, 51, 51, 1, 67, 51, 1, 72,
 54, 1, 72, 54, 1, 67, 51, 1, 67, 51, 51, 1, 72, 51, 2, 72, 51, 1,
 69, 51, 51, 1, 69, 51, 1, 72, 54, 1, 72, 54, 1, 72, 72, 51, 2, 72,
 71, 69, 67, 64, 59, 56, 54, 52, 51, 51, 52, 54, 56, 59, 64, 67, 69, 71,
 72, 72, 1, 71, 70, 67, 64, 59, 56, 53, 52, 52, 53, 56, 59, 64, 67, 70,
 71, 71, 2, 72, 51, 1, 71, 51, 51, 1, 72, 72, 71, 70, 68, 65, 63, 62,
 61, 61, 1, 71, 71, 70, 68, 65, 63, 62, 62, 2, 72, 71, 69, 67, 64, 59,
 56, 54, 52, 51, 51, 52, 54, 56, 59, 64, 67, 69, 71, 72, 72, 1, 71, 70,
 67, 64, 59, 56, 53, 52, 52, 53, 56, 59, 64, 67, 70, 71, 71, 1, 54, 49,
 49, 1, 54, 54, 49, 2, 72, 51, 1, 71, 51, 51, 1, 72, 72, 71, 70, 68,
 65, 63, 62, 61, 61, 1, 71, 71, 70, 68, 65, 63, 62, 62, 1, 61, 51, 51,
 1, 61, 51, 2, 69, 71, 72, 72, 71, 69, 67, 65, 64, 63, 61, 60, 59, 57,
 54, 53, 52, 52, 53, 54, 54, 1, 69, 69, 70, 71, 71, 70, 69, 67, 65, 64,
 62, 61, 59, 57, 54, 52, 51, 51, 52, 54, 2, 71, 51, 1, 71, 51, 51, 1,
 72, 72, 71, 1, 72, 71, 71, 2, 72, 57, 54, 52, 51, 51, 52, 54, 57, 72,
 1, 72, 72, 57, 54, 53, 52, 52, 53, 54, 57, 72, 72, 2, 72, 51, 1, 72,
 72, 54, 1, 72, 72, 54, 1, 72, 51, 2, 72, 51, 1, 72, 72, 54, 1, 72,
 54, 1, 69, 51, 1, 69, 51, 1, 72, 54, 1, 72, 72, 54, 1, 72, 51, 2,
 72, 51, 51, 1, 72, 72, 51, 1, 72, 72, 51, 1, 72, 51, 51, 2, 72, 62,
 51, 51, 1, 72, 72, 62, 1, 72, 72, 62, 1, 72, 62, 51, 2, 72, 51, 1,
 72, 51, 1, 72, 72, 1, 72, 71, 71, 1, 52, 52, 51, 1, 51, 51, 2, 76,
 44, 1, 76, 44, 1, 76, 76, 1, 44, 44, 2, 72, 48, 2, 76, 44, 1, 76,
 44, 1, 76, 76, 1, 44, 44, 2, 67, 72, 67, 1, 67, 71, 67, 2, 44, 44,
 2, 70, 71, 72, 71, 69, 67, 66, 2, 65, 51, 51, 1, 65, 65, 51, 1, 62,
 64, 65, 65, 64, 62, 59, 57, 54, 52, 51, 51, 52, 54, 1, 62, 64, 64, 63,
 62, 59, 57, 54, 53, 52, 52, 54, 2, 72, 51, 51, 1, 72, 72, 51, 1, 62,
 64, 65, 65, 64, 62, 59, 57, 54, 52, 51, 51, 52, 54, 1, 62, 64, 64, 63,
 62, 59, 57, 54, 53, 52, 52, 54, 2, 62, 64, 65, 65, 64, 62, 59, 57, 54,
 52, 51, 51, 52, 54, 1, 62, 61, 63, 64, 64, 63, 62, 59, 57, 54, 53, 52,
 52, 53, 55, 54, 2, 72, 51, 51, 1, 72, 72, 51, 1, 62, 64, 65, 65, 64,
 62, 59, 57, 54, 52, 51, 51, 52, 54, 1, 62, 64, 64, 63, 62, 59, 57, 54,
 53, 52, 52, 54, 2, 58, 58, 61, 63, 64, 65, 65, 64, 62, 59, 57, 54, 52,
 51, 51, 52, 54, 1, 59, 59, 61, 63, 64, 64, 63, 62, 59, 57, 54, 53, 52,
 52, 53, 55, 54, 2, 72, 72, 71, 68, 51, 51, 1, 72, 71, 71, 70, 1, 71,
 68, 51, 1, 65, 65, 64, 1, 65, 64, 64, 2, 65, 65, 50, 47, 46, 45, 45,
 46, 47, 47, 1, 65, 50, 47, 45, 44, 44, 45, 47, 1, 62, 64, 65, 65, 64,
 62, 59, 57, 54, 52, 51, 51, 52, 54, 1, 62, 64, 64, 63, 62, 59, 57, 54,
 53, 52, 52, 54, 2, 72, 51, 51, 1, 72, 72, 51, 1, 61, 64, 65, 65, 64,
 61, 51, 1, 61, 63, 64, 64, 63, 61, 51, 51, 2, 72, 71, 70, 69, 69, 70,
 71, 72, 72, 1, 71, 70, 70, 71, 71, 1, 65, 51, 51, 1, 65, 65, 51, 2,
 72, 71, 70, 69, 69, 70, 71, 72, 72, 1, 71, 70, 70, 71, 71, 1, 65, 44,
 44, 1, 65, 65, 44, 2, 72, 51, 51, 1, 72, 72, 51, 1, 65, 65, 55, 1,
 65, 54, 1, 58, 51, 51, 1, 59, 51, 2, 72, 51, 51, 1, 72, 72, 51, 2,
 65, 51, 51, 1, 65, 65, 51, 1, 61, 64, 65, 65, 64, 61, 51, 1, 61, 63,
 64, 64, 63, 61, 51, 51, 1, 61, 64, 65, 65, 64, 61, 51, 1, 61, 63, 64,
 64, 63, 61, 51, 51, 2, 65, 51, 51, 1, 65, 65, 51, 1, 61, 64, 65, 65,
 64, 61, 51, 1, 61, 63, 64, 64, 63, 61, 51, 51, 2, 65, 64, 62, 59, 57,
 54, 52, 51, 51, 52, 54, 57, 59, 62, 64, 65, 65, 1, 64, 63, 62, 59, 57,
 54, 53, 52, 52, 53, 54, 57, 59, 62, 63, 64, 64, 2, 65, 44, 44, 1, 65,
 65, 44, 1, 62, 64, 65, 65, 64, 62, 59, 57, 54, 52, 51, 51, 52, 54, 1,
 62, 64, 64, 63, 62, 59, 57, 54, 53, 52, 52, 54, 2, 65, 44, 44, 1, 65,
 65, 44, 1, 62, 64, 65, 65, 64, 62, 59, 57, 54, 52, 51, 51, 52, 54, 1,
 62, 64, 64, 63, 62, 59, 57, 54, 53, 52, 52, 54, 2, 65, 51, 51, 1, 65,
 65, 51, 1, 59, 62, 64, 65, 65, 1, 59, 61, 63, 64, 64, 65, 2, 62, 64,
 65, 65, 64, 62, 60, 59, 57, 56, 1, 57, 55, 54, 52, 1, 53, 52, 52, 53,
 1, 52, 54, 54, 1, 62, 62, 64, 1, 63, 64, 64, 63, 1, 64, 62, 60, 1,
 61, 60, 58, 57, 55, 54, 52, 51, 51, 52, 54, 2, 72, 51, 51, 1, 72, 72,
 51, 1, 65, 65, 64, 1, 65, 64, 64, 2, 65, 55, 52, 51, 51, 52, 55, 1,
 65, 65, 55, 53, 52, 52, 53, 55, 1, 65, 51, 51, 1, 65, 65, 51, 2, 65,
 51, 1, 65, 65, 53, 1, 65, 65, 53, 1, 65, 51, 2, 65, 51, 1, 65, 65,
 54, 1, 65, 54, 1, 62, 51, 1, 62, 51, 1, 65, 54, 1, 65, 65, 54, 1,
 65, 51, 2, 65, 51, 51, 1, 65, 65, 51, 1, 65, 65, 51, 1, 65, 51, 51,
 2, 65, 51, 1, 65, 65, 53, 1, 65, 65, 53, 44, 1, 65, 51, 44, 44, 2,
 64, 51, 1, 65, 52, 1, 65, 65, 1, 65, 64, 64, 1, 52, 52, 51, 1, 51,
 51, 2, 76, 75, 74, 72, 70, 68, 67, 65, 63, 61, 1, 75, 73, 71, 69, 68,
 66, 64, 62, 60, 58, 56, 54, 52, 51, 49, 47, 45, 1, 59, 57, 55, 53, 52,
 50, 48, 46, 45, 44, 2, 76, 44, 2, 76, 75, 74, 72, 70, 68, 67, 65, 63,
 61, 1, 75, 73, 71, 69, 68, 66, 64, 62, 60, 58, 56, 54, 52, 51, 49, 47,
 45, 1, 59, 57, 55, 53, 52, 50, 48, 46, 45, 44, 2, 57, 59, 62, 63, 63,
 62, 59, 58, 58, 59, 61, 1, 59, 61, 62, 62, 61, 58, 57, 57, 58, 61, 63,
 2 };

  /* --------------------- Constructor ------------------- */
  /**
   *  Construct a font object for the font RomanDuplex
   *  to be used with the StrokeText class.  To draw text in
   *  3D, pass an instance of this font along with the
   *  string to the constructor of the StrokeText class, and
   *  then add the StrokeText object to the ThreeD_GL_Panel
   *  where the string should be drawn.
   */
  public RomanDuplex()
  {
    num_chars       = 95;
    first_char_code = 32;
    left_edge       = 60;
    top    = 77;
    cap    = 72;
    half   = 60;
    base   = 51;
    bottom = 43;
    char_start = my_char_start;
    char_width = my_char_width;
    font_x     = my_font_x;
    font_y     = my_font_y;
  }
}
