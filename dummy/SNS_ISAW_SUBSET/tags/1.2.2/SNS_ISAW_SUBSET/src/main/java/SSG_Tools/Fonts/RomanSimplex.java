/*
 * File: RomanSimplex.java
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
 * This class stores the data for the font: RomanSimplex
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

public class RomanSimplex extends StrokeFont
{
 private static short my_char_start[] = {
 0, 1, 10, 16, 28, 55, 87, 122, 130, 141, 152, 161, 167, 175,
 178, 184, 187, 205, 210, 225, 241, 248, 266, 290, 296, 326, 350,
 362, 376, 380, 386, 390, 411, 467, 476, 500, 519, 535, 547, 556,
 579, 588, 591, 602, 611, 617, 629, 638, 660, 674, 699, 716, 737,
 743, 754, 760, 772, 778, 785, 794, 806, 809, 821, 829, 832, 840,
 858, 876, 891, 909, 927, 936, 959, 970, 979, 991, 1000, 1003, 1022,
 1033, 1051, 1069, 1087, 1096, 1114, 1123, 1134, 1140, 1152, 1158, 1168, 1177,
 1217, 1220, 1260 };

 private static short my_char_width[] = {
 16, 10, 16, 21, 20, 24, 26, 10, 14, 14, 16, 26, 10, 26, 10, 22, 20, 20, 20,
 20, 20, 20, 20, 20, 20, 20, 10, 10, 24, 26, 24, 18, 27, 18, 21, 21, 21,
 19, 18, 21, 22, 8, 16, 21, 17, 24, 22, 22, 21, 22, 21, 20, 16, 22, 18,
 24, 20, 18, 20, 14, 14, 14, 22, 18, 10, 19, 19, 18, 19, 18, 12, 19, 19,
 8, 10, 17, 8, 30, 19, 19, 19, 19, 13, 17, 12, 19, 16, 22, 17, 16, 17,
 14, 8, 14, 24 };

 private static short my_font_x[] = {
 0, 65, 65, 0, 65, 64, 65, 66, 65, 0, 64, 64, 0, 72, 72, 0, 71, 64, 0,
 77, 70, 0, 64, 78, 0, 63, 77, 0, 68, 68, 0, 72, 72, 0, 77, 75, 72,
 68, 65, 63, 63, 64, 65, 67, 73, 75, 76, 77, 77, 75, 72, 68, 65, 63, 0,
 81, 63, 0, 68, 70, 70, 69, 67, 65, 63, 63, 64, 66, 68, 70, 73, 76, 79,
 81, 0, 77, 75, 74, 74, 76, 78, 80, 81, 81, 79, 77, 0, 83, 83, 82, 81,
 80, 79, 77, 75, 73, 71, 67, 65, 64, 63, 63, 64, 65, 72, 73, 74, 74, 73,
 71, 69, 68, 68, 69, 71, 76, 78, 80, 82, 83, 83, 0, 65, 64, 65, 66, 66,
 65, 64, 0, 71, 69, 67, 65, 64, 64, 65, 67, 69, 71, 0, 63, 65, 67, 69,
 70, 70, 69, 67, 65, 63, 0, 68, 68, 0, 63, 73, 0, 73, 63, 0, 73, 73,
 0, 64, 82, 0, 65, 64, 65, 66, 66, 65, 64, 0, 64, 82, 0, 65, 64, 65,
 66, 65, 0, 80, 62, 0, 69, 66, 64, 63, 63, 64, 66, 69, 71, 74, 76, 77,
 77, 76, 74, 71, 69, 0, 66, 68, 71, 71, 0, 64, 64, 65, 66, 68, 72, 74,
 75, 76, 76, 75, 73, 63, 77, 0, 65, 76, 70, 73, 75, 76, 77, 77, 76, 74,
 71, 68, 65, 64, 63, 0, 73, 63, 78, 0, 73, 73, 0, 75, 65, 64, 65, 68,
 71, 74, 76, 77, 77, 76, 74, 71, 68, 65, 64, 63, 0, 76, 75, 72, 70, 67,
 65, 64, 64, 65, 67, 70, 71, 74, 76, 77, 77, 76, 74, 71, 70, 67, 65, 64,
 0, 77, 67, 0, 63, 77, 0, 68, 65, 64, 64, 65, 67, 71, 74, 76, 77, 77,
 76, 75, 72, 68, 65, 64, 63, 63, 64, 66, 69, 73, 75, 76, 76, 75, 72, 68,
 0, 76, 75, 73, 70, 69, 66, 64, 63, 63, 64, 66, 69, 70, 73, 75, 76, 76,
 75, 73, 70, 68, 65, 64, 0, 65, 64, 65, 66, 65, 0, 65, 64, 65, 66, 65,
 0, 65, 64, 65, 66, 65, 0, 65, 64, 65, 66, 66, 65, 64, 0, 80, 64, 80,
 0, 64, 82, 0, 64, 82, 0, 64, 80, 64, 0, 63, 63, 64, 65, 67, 71, 73,
 74, 75, 75, 74, 73, 69, 69, 0, 69, 68, 69, 70, 69, 0, 78, 77, 75, 72,
 70, 69, 68, 68, 69, 71, 74, 76, 77, 0, 72, 70, 69, 69, 70, 71, 0, 78,
 77, 77, 79, 81, 83, 84, 84, 83, 82, 80, 78, 75, 72, 69, 67, 65, 64, 63,
 63, 64, 65, 67, 69, 72, 75, 78, 80, 81, 0, 79, 78, 78, 79, 0, 69, 61,
 0, 69, 77, 0, 64, 74, 0, 64, 64, 0, 64, 73, 76, 77, 78, 78, 77, 76,
 73, 0, 64, 73, 76, 77, 78, 78, 77, 76, 73, 64, 0, 78, 77, 75, 73, 69,
 67, 65, 64, 63, 63, 64, 65, 67, 69, 73, 75, 77, 78, 0, 64, 64, 0, 64,
 71, 74, 76, 77, 78, 78, 77, 76, 74, 71, 64, 0, 64, 64, 0, 64, 77, 0,
 64, 72, 0, 64, 77, 0, 64, 64, 0, 64, 77, 0, 64, 72, 0, 78, 77, 75,
 73, 69, 67, 65, 64, 63, 63, 64, 65, 67, 69, 73, 75, 77, 78, 78, 0, 73,
 78, 0, 64, 64, 0, 78, 78, 0, 64, 78, 0, 64, 64, 0, 72, 72, 71, 70,
 68, 66, 64, 63, 62, 62, 0, 64, 64, 0, 78, 64, 0, 69, 78, 0, 64, 64,
 0, 64, 76, 0, 64, 64, 0, 64, 72, 0, 80, 72, 0, 80, 80, 0, 64, 64,
 0, 64, 78, 0, 78, 78, 0, 69, 67, 65, 64, 63, 63, 64, 65, 67, 69, 73,
 75, 77, 78, 79, 79, 78, 77, 75, 73, 69, 0, 64, 64, 0, 64, 73, 76, 77,
 78, 78, 77, 76, 73, 64, 0, 69, 67, 65, 64, 63, 63, 64, 65, 67, 69, 73,
 75, 77, 78, 79, 79, 78, 77, 75, 73, 69, 0, 72, 78, 0, 64, 64, 0, 64,
 73, 76, 77, 78, 78, 77, 76, 73, 64, 0, 71, 78, 0, 77, 75, 72, 68, 65,
 63, 63, 64, 65, 67, 73, 75, 76, 77, 77, 75, 72, 68, 65, 63, 0, 68, 68,
 0, 61, 75, 0, 64, 64, 65, 67, 70, 72, 75, 77, 78, 78, 0, 61, 69, 0,
 77, 69, 0, 62, 67, 0, 72, 67, 0, 72, 77, 0, 82, 77, 0, 63, 77, 0,
 77, 63, 0, 61, 69, 69, 0, 77, 69, 0, 77, 63, 0, 63, 77, 0, 63, 77,
 0, 64, 64, 0, 65, 65, 0, 64, 71, 0, 64, 71, 0, 60, 74, 0, 69, 69,
 0, 70, 70, 0, 63, 70, 0, 63, 70, 0, 63, 71, 79, 0, 63, 71, 79, 0,
 60, 78, 0, 65, 66, 65, 64, 64, 65, 66, 0, 75, 75, 0, 75, 73, 71, 68,
 66, 64, 63, 63, 64, 66, 68, 71, 73, 75, 0, 64, 64, 0, 64, 66, 68, 71,
 73, 75, 76, 76, 75, 73, 71, 68, 66, 64, 0, 75, 73, 71, 68, 66, 64, 63,
 63, 64, 66, 68, 71, 73, 75, 0, 75, 75, 0, 75, 73, 71, 68, 66, 64, 63,
 63, 64, 66, 68, 71, 73, 75, 0, 63, 75, 75, 74, 73, 71, 68, 66, 64, 63,
 63, 64, 66, 68, 71, 73, 75, 0, 70, 68, 66, 65, 65, 0, 62, 69, 0, 75,
 75, 74, 73, 71, 68, 66, 0, 75, 73, 71, 68, 66, 64, 63, 63, 64, 66, 68,
 71, 73, 75, 0, 64, 64, 0, 64, 67, 69, 72, 74, 75, 75, 0, 63, 64, 65,
 64, 63, 0, 64, 64, 0, 65, 66, 67, 66, 65, 0, 66, 66, 65, 63, 61, 0,
 64, 64, 0, 74, 64, 0, 68, 75, 0, 64, 64, 0, 64, 64, 0, 64, 67, 69,
 72, 74, 75, 75, 0, 75, 78, 80, 83, 85, 86, 86, 0, 64, 64, 0, 64, 67,
 69, 72, 74, 75, 75, 0, 68, 66, 64, 63, 63, 64, 66, 68, 71, 73, 75, 76,
 76, 75, 73, 71, 68, 0, 64, 64, 0, 64, 66, 68, 71, 73, 75, 76, 76, 75,
 73, 71, 68, 66, 64, 0, 75, 75, 0, 75, 73, 71, 68, 66, 64, 63, 63, 64,
 66, 68, 71, 73, 75, 0, 64, 64, 0, 64, 65, 67, 69, 72, 0, 74, 73, 70,
 67, 64, 63, 64, 66, 71, 73, 74, 74, 73, 70, 67, 64, 63, 0, 65, 65, 66,
 68, 70, 0, 62, 69, 0, 64, 64, 65, 67, 70, 72, 75, 0, 75, 75, 0, 62,
 68, 0, 74, 68, 0, 63, 67, 0, 71, 67, 0, 71, 75, 0, 79, 75, 0, 63,
 74, 0, 74, 63, 0, 62, 68, 0, 74, 68, 66, 64, 62, 61, 0, 74, 63, 0,
 63, 74, 0, 63, 74, 0, 69, 67, 66, 65, 65, 66, 67, 68, 68, 66, 0, 67,
 66, 66, 67, 68, 69, 69, 68, 64, 68, 69, 69, 68, 67, 66, 66, 67, 0, 66,
 68, 68, 67, 66, 65, 65, 66, 67, 69, 0, 64, 64, 0, 65, 67, 68, 69, 69,
 68, 67, 66, 66, 68, 0, 67, 68, 68, 67, 66, 65, 65, 66, 70, 66, 65, 65,
 66, 67, 68, 68, 67, 0, 68, 66, 66, 67, 68, 69, 69, 68, 67, 65, 0, 63,
 63, 64, 66, 68, 70, 74, 76, 78, 80, 81, 0, 63, 64, 66, 68, 70, 74, 76,
 78, 80, 81, 81, 0 };

 private static short my_font_y[] = {
 2, 72, 58, 1, 53, 52, 51, 52, 53, 2, 72, 65, 1, 72, 65, 2, 76, 44, 1,
 76, 44, 1, 63, 63, 1, 57, 57, 2, 76, 47, 1, 76, 47, 1, 69, 71, 72,
 72, 71, 69, 67, 65, 64, 63, 61, 60, 59, 57, 54, 52, 51, 51, 52, 54, 2,
 72, 51, 1, 72, 70, 68, 66, 65, 65, 67, 69, 71, 72, 72, 71, 70, 70, 71,
 72, 1, 58, 57, 55, 53, 51, 51, 52, 54, 56, 58, 58, 2, 63, 64, 65, 65,
 64, 62, 57, 54, 52, 51, 51, 52, 53, 55, 57, 59, 60, 64, 65, 67, 69, 71,
 72, 71, 69, 67, 64, 61, 54, 52, 51, 51, 52, 53, 2, 70, 71, 72, 71, 69,
 67, 66, 2, 76, 74, 71, 67, 62, 58, 53, 49, 46, 44, 2, 76, 74, 71, 67,
 62, 58, 53, 49, 46, 44, 2, 66, 54, 1, 63, 57, 1, 63, 57, 2, 69, 51,
 1, 60, 60, 2, 51, 52, 53, 52, 50, 48, 47, 2, 60, 60, 2, 53, 52, 51,
 52, 53, 2, 76, 44, 2, 72, 71, 68, 63, 60, 55, 52, 51, 51, 52, 55, 60,
 63, 68, 71, 72, 72, 2, 68, 69, 72, 51, 2, 67, 68, 70, 71, 72, 72, 71,
 70, 68, 66, 64, 61, 51, 51, 2, 72, 72, 64, 64, 63, 62, 59, 57, 54, 52,
 51, 51, 52, 53, 55, 2, 72, 58, 58, 1, 72, 51, 2, 72, 72, 63, 64, 65,
 65, 64, 62, 59, 57, 54, 52, 51, 51, 52, 53, 55, 2, 69, 71, 72, 72, 71,
 68, 63, 58, 54, 52, 51, 51, 52, 54, 57, 58, 61, 63, 64, 64, 63, 61, 58,
 2, 72, 51, 1, 72, 72, 2, 72, 71, 69, 67, 65, 64, 63, 62, 60, 58, 55,
 53, 52, 51, 51, 52, 53, 55, 58, 60, 62, 63, 64, 65, 67, 69, 71, 72, 72,
 2, 65, 62, 60, 59, 59, 60, 62, 65, 66, 69, 71, 72, 72, 71, 69, 65, 60,
 55, 52, 51, 51, 52, 54, 2, 65, 64, 63, 64, 65, 1, 53, 52, 51, 52, 53,
 2, 65, 64, 63, 64, 65, 1, 51, 52, 53, 52, 50, 48, 47, 2, 69, 60, 51,
 2, 63, 63, 1, 57, 57, 2, 69, 60, 51, 2, 67, 68, 70, 71, 72, 72, 71,
 70, 68, 66, 64, 63, 61, 58, 1, 53, 52, 51, 52, 53, 2, 64, 66, 67, 67,
 66, 65, 62, 59, 57, 56, 56, 57, 59, 1, 67, 65, 62, 59, 57, 56, 1, 67,
 59, 57, 56, 56, 58, 61, 63, 66, 68, 70, 71, 72, 72, 71, 70, 68, 66, 63,
 60, 57, 55, 53, 52, 51, 51, 52, 53, 54, 1, 67, 59, 57, 56, 2, 72, 51,
 1, 72, 51, 1, 58, 58, 2, 72, 51, 1, 72, 72, 71, 70, 68, 66, 64, 63,
 62, 1, 62, 62, 61, 60, 58, 55, 53, 52, 51, 51, 2, 67, 69, 71, 72, 72,
 71, 69, 67, 64, 59, 56, 54, 52, 51, 51, 52, 54, 56, 2, 72, 51, 1, 72,
 72, 71, 69, 67, 64, 59, 56, 54, 52, 51, 51, 2, 72, 51, 1, 72, 72, 1,
 62, 62, 1, 51, 51, 2, 72, 51, 1, 72, 72, 1, 62, 62, 2, 67, 69, 71,
 72, 72, 71, 69, 67, 64, 59, 56, 54, 52, 51, 51, 52, 54, 56, 59, 1, 59,
 59, 2, 72, 51, 1, 72, 51, 1, 62, 62, 2, 72, 51, 2, 72, 56, 53, 52,
 51, 51, 52, 53, 56, 58, 2, 72, 51, 1, 72, 58, 1, 63, 51, 2, 72, 51,
 1, 51, 51, 2, 72, 51, 1, 72, 51, 1, 72, 51, 1, 72, 51, 2, 72, 51,
 1, 72, 51, 1, 72, 51, 2, 72, 71, 69, 67, 64, 59, 56, 54, 52, 51, 51,
 52, 54, 56, 59, 64, 67, 69, 71, 72, 72, 2, 72, 51, 1, 72, 72, 71, 70,
 68, 65, 63, 62, 61, 61, 2, 72, 71, 69, 67, 64, 59, 56, 54, 52, 51, 51,
 52, 54, 56, 59, 64, 67, 69, 71, 72, 72, 1, 55, 49, 2, 72, 51, 1, 72,
 72, 71, 70, 68, 66, 64, 63, 62, 62, 1, 62, 51, 2, 69, 71, 72, 72, 71,
 69, 67, 65, 64, 63, 61, 60, 59, 57, 54, 52, 51, 51, 52, 54, 2, 72, 51,
 1, 72, 72, 2, 72, 57, 54, 52, 51, 51, 52, 54, 57, 72, 2, 72, 51, 1,
 72, 51, 2, 72, 51, 1, 72, 51, 1, 72, 51, 1, 72, 51, 2, 72, 51, 1,
 72, 51, 2, 72, 62, 51, 1, 72, 62, 2, 72, 51, 1, 72, 72, 1, 51, 51,
 2, 76, 44, 1, 76, 44, 1, 76, 76, 1, 44, 44, 2, 72, 48, 2, 76, 44,
 1, 76, 44, 1, 76, 76, 1, 44, 44, 2, 67, 72, 67, 1, 67, 71, 67, 2,
 44, 44, 2, 70, 71, 72, 71, 69, 67, 66, 2, 65, 51, 1, 62, 64, 65, 65,
 64, 62, 59, 57, 54, 52, 51, 51, 52, 54, 2, 72, 51, 1, 62, 64, 65, 65,
 64, 62, 59, 57, 54, 52, 51, 51, 52, 54, 2, 62, 64, 65, 65, 64, 62, 59,
 57, 54, 52, 51, 51, 52, 54, 2, 72, 51, 1, 62, 64, 65, 65, 64, 62, 59,
 57, 54, 52, 51, 51, 52, 54, 2, 59, 59, 61, 63, 64, 65, 65, 64, 62, 59,
 57, 54, 52, 51, 51, 52, 54, 2, 72, 72, 71, 68, 51, 1, 65, 65, 2, 65,
 49, 46, 45, 44, 44, 45, 1, 62, 64, 65, 65, 64, 62, 59, 57, 54, 52, 51,
 51, 52, 54, 2, 72, 51, 1, 61, 64, 65, 65, 64, 61, 51, 2, 72, 71, 72,
 73, 72, 1, 65, 51, 2, 72, 71, 72, 73, 72, 1, 65, 48, 45, 44, 44, 2,
 72, 51, 1, 65, 55, 1, 59, 51, 2, 72, 51, 2, 65, 51, 1, 61, 64, 65,
 65, 64, 61, 51, 1, 61, 64, 65, 65, 64, 61, 51, 2, 65, 51, 1, 61, 64,
 65, 65, 64, 61, 51, 2, 65, 64, 62, 59, 57, 54, 52, 51, 51, 52, 54, 57,
 59, 62, 64, 65, 65, 2, 65, 44, 1, 62, 64, 65, 65, 64, 62, 59, 57, 54,
 52, 51, 51, 52, 54, 2, 65, 44, 1, 62, 64, 65, 65, 64, 62, 59, 57, 54,
 52, 51, 51, 52, 54, 2, 65, 51, 1, 59, 62, 64, 65, 65, 2, 62, 64, 65,
 65, 64, 62, 60, 59, 58, 57, 55, 54, 52, 51, 51, 52, 54, 2, 72, 55, 52,
 51, 51, 1, 65, 65, 2, 65, 55, 52, 51, 51, 52, 55, 1, 65, 51, 2, 65,
 51, 1, 65, 51, 2, 65, 51, 1, 65, 51, 1, 65, 51, 1, 65, 51, 2, 65,
 51, 1, 65, 51, 2, 65, 51, 1, 65, 51, 47, 45, 44, 44, 2, 65, 51, 1,
 65, 65, 1, 51, 51, 2, 76, 75, 74, 72, 70, 68, 67, 65, 63, 61, 1, 75,
 73, 71, 69, 68, 66, 64, 62, 60, 58, 56, 54, 52, 51, 49, 47, 45, 1, 59,
 57, 55, 53, 52, 50, 48, 46, 45, 44, 2, 76, 44, 2, 76, 75, 74, 72, 70,
 68, 67, 65, 63, 61, 1, 75, 73, 71, 69, 68, 66, 64, 62, 60, 58, 56, 54,
 52, 51, 49, 47, 45, 1, 59, 57, 55, 53, 52, 50, 48, 46, 45, 44, 2, 57,
 59, 62, 63, 63, 62, 59, 58, 58, 59, 61, 1, 59, 61, 62, 62, 61, 58, 57,
 57, 58, 61, 63, 2 };

  /* --------------------- Constructor ------------------- */
  /**
   *  Construct a font object for the font RomanSimplex
   *  to be used with the StrokeText class.  To draw text in
   *  3D, pass an instance of this font along with the
   *  string to the constructor of the StrokeText class, and
   *  then add the StrokeText object to the ThreeD_GL_Panel
   *  where the string should be drawn.
   */
  public RomanSimplex()
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