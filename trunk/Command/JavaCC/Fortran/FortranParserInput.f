C File:  FortranParserInput.f
C 
C Copyright 2003 Chris M. Bouzek
C 
C This program is free software; you can redistribute it and/or
C modify it under the terms of the GNU General Public License
C as published by the Free Software Foundation; either version 2
C of the License, or (at your option) any later version.
C 
C This program is distributed in the hope that it will be useful,
C but WITHOUT ANY WARRANTY; without even the implied warranty of
C MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
C GNU General Public License for more details.
C 
C You should have received a copy of the GNU General Public License
C along with this library; if not, write to the Free Software
C Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
C 
C Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
C           Chris Bouzek <coldfusion78@yahoo.com>
C           Department of Mathematics, Statistics and Computer Science
C           University of Wisconsin-Stout
C           Menomonie, WI 54751, USA
C 
C This work was supported by the Intense Pulsed Neutron Source Division
C of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
C This work was supported by the National Science Foundation under
C grant number DMR-0218882.
C 
C For further information, see <http://www.pns.anl.gov/ISAW/>
C 
C Modified:
C $Log$
C Revision 1.2  2004/01/16 01:07:48  bouzekc
C Added one space after each comment line with nothing on it.  This is
C needed because the parser expects at least a C and a space.
C
C Revision 1.1  2004/01/16 01:02:29  bouzekc
C Added to CVS.
C 

C the math functions
  abs(5.5)
  abs(5)
  iabs(5)
  sqrt(2.0)
  exp(0.5)
  log(0.5)
  mod( try, tr2 )
  mod( 5, 5.5 )

C geometric math functions
  sin(0.75)
  cos(0.5)
  tan(1.0)
  asin(5.0)
  atan(5.0)
  acos(12.0)
  
C conversion functions
  int( myvar )
  int( 4.5 )
  float( 345 )
  float( var1 )
  fraction(4.5)
  fraction(myfloat)
  
C integer declarations
  integer i
  integer k1, k2, k3
  integer i(4), i(5)
  integer i3, ir5, i(10)
  integer i(10)
  
C float declarations
  real myreal
  real r1, r2,  t5
  real r(1)
  real var1(9), var2(10)
  real r1, r2, r(9)
  real r1 = 5.0, r2 = 10.0
  real r1 = 2.0
  double precision test
  double precision test1, test2
  double precision var1(9), var2(10)
  double precision r1, r2, r(9)
  double precision r1 = 2.0
  double precision r1 = 5.0, r2 = 10.0

  
C boolean declarations
  logical tryui
  logical try1, try2     ,tr4
  logical var1(9), var2(10)
  logical r1, r2, r(9)
  logical r1 = .true., r2 = .false., r4, r5(9)
  logical r1 = .true.
  
C character declarations
  character *5 mychar
  character *7 mychar1, mychar2,   mychar3
  character mychar *1, mychar *2,    mychar   *5

C some math expressions
  ( 5 * 7 + 8 / r )
  5 * 7 + t / 9
  5 * ( wer + ( 8 / 9 ) )
  ( 5 * ( 7 + ( ttt / 9 ) ) )
  ( 5 * ( 7 + ( 8 / 9 ) + 9 ) / zzz )
  5 * ( 7 + ( 8 / 9 ) + 9 ) / zzz
  var1 = ( 5 * 7 + 8 / r )
  var2 = 5 * 7 + t / 9
  var3 = 5 * ( wer + ( 8 / 9 ) )
  var4 = ( 5 * ( 7 + ( ttt / 9 ) ) )
  var5 = ( 5 * ( 7 + ( 8 / 9 ) + 9 ) / zzz )
  var6 = 5 * ( 7 + ( 8 / 9 ) + 9 ) / zzz
  
C some if statements

C single line if
C  if( x .lt. 0 ) y = 5 + 7
  
C multi line single if
  if( x .lt. 1 ) then
    x = 5 * 7 * 9
  endif
  
C multi line multi statement if...else if
  if (x .gt. 0) then
    if (x .ge. y) then
      "x is positive and x >= y"
    else 
      "x is positive but x < y"
    endif
  elseif (x .lt. 0) then
    "x is negative"
  else 
    "x is zero"
  endif

C do (aka for) loop
    integer i

    do 20 i = 10, 1, -2
      x = x +5
  20  continue
