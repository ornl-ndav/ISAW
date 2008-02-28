/*
 * License Applicability. Except to the extent portions of this file are
 * made subject to an alternative license as permitted in the SGI Free
 * Software License B, Version 1.1 (the "License"), the contents of this
 * file are subject only to the provisions of the License. You may not use
 * this file except in compliance with the License. You may obtain a copy
 * of the License at Silicon Graphics, Inc., attn: Legal Services, 1600
 * Amphitheatre Parkway, Mountain View, CA 94043-1351, or at:
 * 
 * http://oss.sgi.com/projects/FreeB
 * 
 * Note that, as provided in the License, the Software is distributed on an
 * "AS IS" basis, with ALL EXPRESS AND IMPLIED WARRANTIES AND CONDITIONS
 * DISCLAIMED, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED WARRANTIES AND
 * CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A
 * PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
 * 
 * NOTE:  The Original Code (as defined below) has been licensed to Sun
 * Microsystems, Inc. ("Sun") under the SGI Free Software License B
 * (Version 1.1), shown above ("SGI License").   Pursuant to Section
 * 3.2(3) of the SGI License, Sun is distributing the Covered Code to
 * you under an alternative license ("Alternative License").  This
 * Alternative License includes all of the provisions of the SGI License
 * except that Section 2.2 and 11 are omitted.  Any differences between
 * the Alternative License and the SGI License are offered solely by Sun
 * and not by SGI.
 *
 * Original Code. The Original Code is: OpenGL Sample Implementation,
 * Version 1.2.1, released January 26, 2000, developed by Silicon Graphics,
 * Inc. The Original Code is Copyright (c) 1991-2000 Silicon Graphics, Inc.
 * Copyright in any portions created by third parties is as indicated
 * elsewhere herein. All Rights Reserved.
 * 
 * Additional Notice Provisions: The application programming interfaces
 * established by SGI in conjunction with the Original Code are The
 * OpenGL(R) Graphics System: A Specification (Version 1.2.1), released
 * April 1, 1999; The OpenGL(R) Graphics System Utility Library (Version
 * 1.3), released November 4, 1998; and OpenGL(R) Graphics with the X
 * Window System(R) (Version 1.3), released October 19, 1998. This software
 * was created using the OpenGL(R) version 1.2.1 Sample Implementation
 * published by SGI, but has not been independently verified as being
 * compliant with the OpenGL(R) version 1.2.1 Specification.
 */

package com.sun.opengl.impl.glue;

/**
 *
 * @author  Administrator
 */
public class Glue {
  private static String[] __gluNurbsErrors = {
    " ",
    "spline order un-supported",
    "too few knots",
    "valid knot range is empty",
    "decreasing knot sequence knot",
    "knot multiplicity greater than order of spline",
    "gluEndCurve() must follow gluBeginCurve()",
    "gluBeginCurve() must precede gluEndCurve()",
    "missing or extra geometric data",
    "can't draw piecewise linear trimming curves",
    "missing or extra domain data",
    "missing or extra domain data",
    "gluEndTrim() must precede gluEndSurface()",
    "gluBeginSurface() must precede gluEndSurface()",
    "curve of improper type passed as trim curve",
    "gluBeginSurface() must precede gluBeginTrim()",
    "gluEndTrim() must follow gluBeginTrim()",
    "gluBeginTrim() must follow gluEndTrim()",
    "invalid or missing trim curve",
    "gluBeginTrim() must precede gluPwlCurve()",
    "piecewise linear trimming curve referenced twice",
    "piecewise linear trimming curve and nurbs curve mixed",
    "improper usage of trim data type",
    "nurbs curve referenced twice",
    "nurbs curve and piecewise linear trimming curve mixed",
    "nurbs surface referenced twice",
    "invalid property",
    "gluEndSurface() must follow gluBeginSurface()",
    "intersecting or misoriented trim curve",
    "intersecting trim curves",
    "UNUSED",
    "inconnected trim curves",
    "unknown knot error",
    "negative vertex count encountered",
    "negative byte-stride encountered",
    "unknown type descriptor",
    "null control point reference",
    "duplicate point on piecewise linear trimming curve"
  } ;
  
  /** Creates a new instance of Glue */
  public Glue() {
  }
  
  public static String __gluNURBSErrorString( int errno ) {
    return( __gluNurbsErrors[ errno ] );
  }
  
  private static String[] __gluTessErrors = {
    " ",
    "gluTessBeginPolygon() must precede a gluTessEndPolygon",
    "gluTessBeginContour() must precede a gluTessEndContour()",
    "gluTessEndPolygon() must follow a gluTessBeginPolygon()",
    "gluTessEndContour() must follow a gluTessBeginContour()",
    "a coordinate is too large",
    "need combine callback"
  };
  
  public static String __gluTessErrorString( int errno ) {
    return( __gluTessErrors[ errno ] );
  }
}
