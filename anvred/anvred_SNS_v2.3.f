C**************************   ANVRED  ******************************
C
C ARGONNE NATIONAL LABORATORY VARIABLE WAVELENGTH DATA REDUCTION PROGRAM
C
C		Major contributions from:
C			P. C. W. Leung
C			A. J. Schultz
C			R. G. Teller
C			L. R. Falvello
C
C     The data output by this program are corrected  for  variations  in
C  spectral distribution, variations in detector efficiency  across  the
C  face  of  the  detector,  and the pertinent geometric factors such as
C  (SIN(THETA))**2 and LAMBDA**4.
C
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!	Linux version:	A. Schultz   January, 2003                    !
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

!	Version 4:		A. Schultz		June, 2003
!		1. Process data from two detectors
!		2. Does not use an x-file.
!		3. Gets MONCNT from integrate file. If zero, sets CMONX = 1.0.
!		4. Corrected ALPHAP for calculation of SPECT1.

!	Version 5:		A. Schultz		July, 2003
!		This version outputs a expnam.hkl file which can be input
!		into SHELX with HKL 2.
!	Version 5a:
!		Cleaned-up and removed a lot of unused code.
!		Added a test for dmin.
!
!	Version 6:		L. Falvello		January, 2004
!		Polyhedral absorption correction with two detectors.
!
!	Version 7:		A. Schultz		2007
!		Use spectrum obtained from each SCD detector
!
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!	ANVRED_SNS:		A. Schultz		2008               !
!		Process SNS data.                                          !
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!	ANVRED_SNS_v2:		A. Schultz		June, 2008
!		New spherical absorption correction. Removed all
!		of the old correction code.
!	ANVRED_SNS-v2.1: read detector parameters from integrate file.
!	ANVRED_SNS-v2.2: get filename for spectrum file.
!       ANVRED_SNS-v2.3: everything included in one file.  8/17/2008
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	Program ANVRED_SNS

	INCLUDE 'datacom_SNS.inc'

	INTEGER CURHST
	integer today(3), now(3)
	CHARACTER*60 ANS,NAM2,NAM1,BNAM,DEFNAM, SpecNam
	CHARACTER ALINE*78

	DIMENSION PJ(11,2)	!Spectrum coefficients
	dimension xtof(100), spect1(100)

       DATA IS/0/
       DATA IQ/1/
       DATA DN/1/
       DATA PRGNAM/'ANVRED_SNS'/

C--------------------------------------------------------------------

	OPEN(UNIT=16,FILE='anvred.log')

	WRITE (*, *) ' '
	WRITE (*, *) '*** ANVRED_SNS ***'
	WRITE (*, *) ' '
	WRITE (16, *) '*** ANVRED_SNS ***'
	WRITE (16, *) ' '

C
C  REQUEST THE DATA NEEDED TO DEFINE THE FILE NEEDED
C
	WRITE (*, 11)
11	format(' Experiment name (EXPNAM)? ',$)
	READ (*, 100) EXPNAM
100	FORMAT (A) 
	LCS = LNBLNK(EXPNAM)

	WRITE (16,11)
	WRITE (16,100) EXPNAM

C
C  CHECK ON THE EXISTANCE OF THE REFL FILE
C
	  defnam=expnam(1:LCS)//'.integrate'
	  lcs2 = lcs + 10

	WRITE (*,552) DEFNAM(1:LCS2)
552	FORMAT(/,
	1' Input the name of the input reflection file <',A,
	2'>: ',$)
	CALL READANS (NCHRS, NAM1)	
	IF (nchrs.eq.0) nam1=defnam
	  OPEN(UNIT=9,TYPE='OLD',NAME=NAM1,
	1	err=5004)
	
!  Initial read of integrate file to get instrument and detectors calibration.
	call readrefl_SNS_header (9, ieof)
	write (*, *) ' '
		

	WRITE (16,552) DEFNAM(1:LCS2)
	WRITE (16,'(A)') NAM1

	defnam=expnam(1:LCS)//'.hkl'
	LCS2 = LCS+4
	WRITE (*, 60) defnam(1:LCS2)
60	FORMAT(/,
	1' Input the name of the output reflection file <',A,
	2'>: ',$)
	CALL READANS (NCHRS, BNAM)
	if (nchrs.eq.0) bnam=defnam

	WRITE (16,60) defnam(1:LCS2)
	WRITE (16,'(A)') BNAM

	OPEN(UNIT=4,TYPE='OLD',NAME=BNAM,ERR=610)
	WRITE (*, *) ' ***Output will be appended to existing file.***'
605	READ (4,100,END=400) ALINE
	GO TO 605

	GO TO 400

610	WRITE (*,*) ' ***New output reflection file is being created.***'
	OPEN(UNIT=4,TYPE='NEW',NAME=BNAM,ERR=5004)

400	CONTINUE

C
C  ECHO MUCH OF THE ABOVE DATA ON THE LINE-PRINTER
C

     	call idate(today)   ! today(1)=day, (2)=month, (3)=year
      	call itime(now)     ! now(1)=hour, (2)=minute, (3)=second
     	write ( 16, 9000 )  today(2), today(1), today(3), now
9000	format (//,' ***   ANVRED   ', i2.2, '/', i2.2, '/', i4.4,' ',
	1	i2.2, ':', i2.2, ':', i2.2,'   ***'//)
	
      WRITE (*, 9613)
9613	FORMAT(//,
     1' Do you wish to do an absorption correction ',
     1'(Y,<N>)? ',$)
	CALL READANS (NCHRS, ANS)
	if (nchrs.eq.0) ans='N'
	IABS = 0
      
	IF (ANS.EQ.'Y' .OR. ANS.EQ.'y') THEN
      WRITE (16, 198)
  198 FORMAT(//,' ***** ABSORPTION CORRECTION APPLIED ******')
      IABS = 1
	END IF

98	CONTINUE

C
C   SETUP FOR ABSORPTION CORRECTION
C
	IF (ANS.EQ.'Y' .OR. ANS.EQ.'y') CALL ABSOR1_SNS (smu, amu, radius)
	transmin = 1.0
	transmax = 0.0

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	OPEN(UNIT=21,TYPE='OLD',FILE=SpecNam)
	WRITE (*, *) ' '
	WRITE (*, 6290) SpecNam
6290	FORMAT(
	1' Incident spectrum coefficients in ',A)
	WRITE (16,6290)
	WRITE (*, *) ' '

	read (21,100) ALINE
	read (21,*) (PJ(J,1),J=1,11)
	write (*,100) ALINE
	write (*,*) (PJ(J,1),J=1,11)

	read (21,100) ALINE
	read (21,*) (PJ(J,2),J=1,11)
	write (*,100) ALINE
	write (*,*) (PJ(J,2),J=1,11)


!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

!  Permit only data above a I/sig(I) threshold to be saved.
	WRITE (*, 5360)
5360	FORMAT(//,' Input minimum I/sig(I) (<0>): ',$)
	CALL READANS (NCHRS, ANS)
	IF (NCHRS.EQ.0) THEN
		ISIG = 0
	ELSE
		READ (ANS, *) ISIG
	END IF

	WRITE (16, 5360)
	WRITE (16, *) ISIG

!  Reject border peaks.
	WRITE (*, 6360)
6360	FORMAT(//,' Reject peaks within N channels of the border.'/,
	1' Input a value for N (<10>): ',$)
	CALL READANS (NCHRS, ANS)
	IF (NCHRS.EQ.0) THEN
		NBCH = 10
	ELSE
		READ (ANS, *) NBCH
	END IF
	WRITE (16, 6360)
	WRITE (16, *) NBCH

!  Reject peaks for which CENTROID failed (REFLAG .NE. 10)
	WRITE (*, 7360)
7360	FORMAT(//,
	1' Reject peaks for which the centroid calculation failed',
	2' (<y> or n): ',$)
	CALL READANS (NCHRS, ANS)
	IF (NCHRS.EQ.0) THEN
		JREF = 1
	ELSE
		IF (ANS.EQ.'Y' .OR. ANS.EQ.'y') THEN
			JREF = 1
		ELSE
			JREF = 0
		END IF
	END IF

	WRITE (16, 7360)
	WRITE (16, '(A)') ANS

!  Reject peaks below a threshold peak count	!ajs 5/6/03
	WRITE (*, 8360)
8360	FORMAT(//,
	1' Input mininum peak count (<0>): ',$)
	CALL READANS (NCHRS, ANS)
	IF (NCHRS.EQ.0) THEN
		IPKMIN = 0
	ELSE
		READ (ANS, *) IPKMIN
	END IF

	WRITE (16, 8360)
	WRITE (16, '(A)') ANS

!  Reject peaks below a minimum d-spacing		!ajs 7/2/03
	WRITE (*, 8370)
8370	FORMAT(//,
	1' Input mininum d-spacing (<0.0>): ',$)
	CALL READANS (NCHRS, ANS)
	IF (NCHRS.EQ.0) THEN
		DMIN = 0.0
	ELSE
		READ (ANS, *) DMIN
	END IF

	WRITE (16, 8370)
	WRITE (16, '(A)') ANS


!  Multiply by a scale factor.
	WRITE (*, 5370)
5370	FORMAT(//,
	1' If integrated counts are very large, one may wish to multiply'/,
	2' by a factor, such as 0.1 or less.'/,
	3' Scaling factor applied to FSQ and sig(FSQ) (<1.0>): ',$)
	CALL READANS (NCHRS, ANS)
	IF (NCHRS.EQ.0) THEN
		SCALEFACTOR = 1.0
	ELSE
		READ (ANS, *) SCALEFACTOR
	END IF

	WRITE (16, 5370)
	WRITE (16, *) SCALEFACTOR 
	
	write (*, *) ' '


C-----------------------------------------------------------------------
C  Calculate spectral correction at 1.0 Angstrom to normalize
C  spectral correction factors later on.   This code was copied
C  from SEPD_SPEC.

		do id = 1, nod
	
	xtof(id) = (L1 + dist(id)) / hom
	
c++	TOF = WLMIN * XTOF
c++  Normalize to a wavelength of 1.0 Angstroms
	TOF = 1.0 * XTOF(id)
	T = TOF/1000.

C	SPECT1 = A1(JS)*EXP(-A2(JS)/T**2)/T**5 + A3(JS)*EXP(-A4(JS)*T*T)
C     &		+ A5(JS)*EXP(-A6(JS)*T**3) + A7(JS)*EXP(-A8(JS)*T**4) 
! TYPE 2 function in GSAS.
	SPECT1(id) = PJ(1,id) + PJ(2,id)*EXP(-PJ(3,id)/T**2)/T**5
	1	+ PJ(4,id)*EXP(-PJ(5,id)*T**2)
	2	+ PJ(6,id)*EXP(-PJ(7,id)*T**3)
	3	+ PJ(8,id)*EXP(-PJ(9,id)*T**4)
	4	+ PJ(10,id)*EXP(-PJ(11,id)*T**5)
	
		end do
		

C-----------------------------------------------------------------------
 

C
C  SET THE CURRENT HISTOGRAM NUMBER TO 0 AND INITIALIZE THE MONITOR COUN
C
	CURHST = 0
	IDET = 0		!++
	HSTNUM = 0		!++
	CMON = 1000000.
	NCNTR = 0		!Number of processed reflections
C
C   SET UP LOOP TO PROCESS THE REFLECTION DATA
C
	write (*, *) ' '

4000	CONTINUE

	CALL READREFL_SNS (9, IEOF)
	
	IF (IEOF .EQ. 1) GO TO 114

	IF(NRUN.EQ.CURHST .AND. DN.EQ.IDET) GO TO 1100
	
	CURHST = NRUN
	
	IDET = DN	!IDET and DN is the arbitrary detector number.
			!ID is a sequential number in the order they are listed.
	do id=1,nod
		if (detnum(id).eq.dn) go to 1090
	end do
	STOP 'Error -- no match for detector ID'
	
	
1090	continue
	
	HSTNUM = HSTNUM + 1
	WRITE (*, *) '   NRUN=',NRUN,'   DETNUM=',DN,'   HSTNUM=',HSTNUM

	MNSUM = MONCNT	!++

	IF (MNSUM.EQ.0) THEN	!++ Check for zero.
		CMONX = 1.0
	ELSE
		CMONX = CMON / MNSUM
		IF (CMONX .EQ. 0) CMONX=1.
	END IF

      WRITE (16, 1110) NRUN,CHI,PHI,OMEGA,DETA,MNSUM,CMONX
 1110 FORMAT(/,' HISTOGRAM NUMBER',I5/3X,' ANGLES ARE CHI =',F7.2,1X,
     * '  PHI =',F7.2,'  OMEGA =',F7.2,'  DETA =',F7.2/,
     * '    TOTAL MONITOR COUNTS ELAPSD',I10,'   CMONX =',F8.4,
     * '    * DATA SCALED TO 1 MILLION MONITOR COUNTS *'/,
     * '    CORREC = SCALEFACTOR * CMONX * SINSQT / ( SPECT ',
     * ' * (DET EFF) * WL4 * ABTRANS )')

      write(16,117) 
  117 FORMAT(/,'    H   K   L       FSQ     SIG     WL      INTI',      
     2'     SIG   SPECT  DET_EFF SINSQT',
     3'  ABTRANS   TBAR'/)


1100	CONTINUE


		IF (ISIG .GT. 0 .AND. INTI .LT. ISIG*SIGI) GO TO 4000
		IF (INTI .EQ. 0.0) GO TO 4000
		IF (IPKOBS .LT. IPKMIN) GO TO 4000	!ajs 5/6/03
		x = col
		y = row
		IF (x.LT.NBCH .OR. x.GT.(ncols(id)-NBCH)) GO TO 4000	!ajs 4/14/03
		IF (Y.LT.NBCH .OR. Y.GT.(nrows(id)-NBCH)) GO TO 4000
		IF (JREF.EQ.1 .AND. REFLAG.NE.10) GO TO 4000
		IF(REFLAG.GT.299) GO TO 4000

!  The d-spacing is read from the SNS refelction file.

		IF (DSP.LT.DMIN) GO TO 4000



	NCNTR=NCNTR+1		!Number of processed reflections.

C+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
C   Spectral correction based on SCD TiZr data.

C	SPECT = A1(JS)*EXP(-A2(JS)/T**2)/T**5 + A3(JS)*EXP(-A4(JS)*T*T)
C     &		+ A5(JS)*EXP(-A6(JS)*T**3) + A7(JS)*EXP(-A8(JS)*T**4)

	TOF = WL * XTOF(id)
	T = TOF/1000.

! TYPE 2 function in GSAS.

	SPECT = PJ(1,id) + PJ(2,id)*EXP(-PJ(3,id)/T**2)/T**5
	1	+ PJ(4,id)*EXP(-PJ(5,id)*T**2)
	2	+ PJ(6,id)*EXP(-PJ(7,id)*T**3)
	3	+ PJ(8,id)*EXP(-PJ(9,id)*T**4)
	4	+ PJ(10,id)*EXP(-PJ(11,id)*T**5)
 
	SPECT = SPECT/SPECT1(id)
C+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++



	SINSQT = (WL/(2.0*dsp))**2

!	CALL EFFCALC (XCM, YCM, DETD, WL, EFF, IEFF)	!ajs 3/10/03
	eff = 1.0

	WL4=WL**4	

	CORREC = SCALEFACTOR *( SINSQT*CMONX ) / ( WL4*SPECT*EFF ) !ajs Added SCALEFACTOR 3/14/03

C
C   ABSORPTION CORRECTION
C
      trans = 1.0	!transmission

C LRF 7/96
	IF (IABS.NE.0) THEN
      
		call absor_sphere(smu, amu, radius, trans, tbar)
		
		if (trans.lt.transmin) transmin = trans
		if (trans.gt.transmax) transmax = trans
	
	ENDIF
	
	CORREC = CORREC/trans

!ajs 3/14/03
	FSQ = INTI * CORREC
	SIGFSQ = SIGI * CORREC

      write(16,111)H,K,L,FSQ,SIGFSQ,WL,INTI,SIGI,SPECT,EFF,
     2 SINSQT,trans,tbar			! TBAR=Coppens's Tbar.
  111 FORMAT (1X,3I4,F10.2,F8.2,F7.3,F10.2,F8.2,4F8.4,F8.4)
  
	IQ = HSTNUM

!++  Write output file that is compatible with SHELX   June, 2003
      WRITE(4,1160)H,K,L,FSQ,SIGFSQ,IQ,WL,TBAR,CURHST,SEQNUM,
     2    EFF,trans
 1160 FORMAT(3I4,2F8.2,I4,F8.4,F7.4,2I7,2F7.4)


       GO TO 4000

C
C  END OF THE REFLECTION LOOP
C
  114 WRITE (16, 1113)
 1113 FORMAT ('*',79X/' END       END OF THE DATA SET',51X)
      IS = 1
      CLOSE(UNIT=45)
	WRITE(4,*)'  0   0   0    0.00    0.00   0  0.0000'
      CLOSE(UNIT=4)
C LRF 7/96
      if (iabs.ne.0) write(*,7001) transmax, transmin
 7001 format(/,1x,'Absorption correction applied -- max., min.',
     1  ' transmission: ',2f8.4,/)
	if (iabs.ne.0) WRITE (16, 7001) transmax, transmin
C
      WRITE (*, 20) NCNTR
	WRITE (16, 20) NCNTR
   20 FORMAT(1X,I6,' REFLECTIONS PROCESSED',/)
      STOP
 5003 WRITE (*, 5103)
 5103 FORMAT(' THE REFLECTION FILE DOES NOT EXIST')
      STOP
 5004 WRITE (*, 5104) NAM2
 5104 FORMAT(1X,18A1,' DOES NOT EXIST')
      STOP
 5005 WRITE (*,*) ' ERROR OPENING UNIT=4'
      STOP
      END

C=======================================================================C
      SUBROUTINE ABSOR1_SNS (smu, amu, radius)
      
	dimension znums(2)
	character c133a*133

      write(*,1001) ' '
 1001 format(1x,a,6f10.5)
      write(*,1001) 'Input linear absorption coefficients (mu)'
     1     //' corresponding to:'
      write(*,1001)
     1  '  (a) Total scattering (wavelength independent); and'
      write(*,1001)
     1  '  (b) True absorption at <Lambda> = 1.8 Angstroms.'
      write(*,1001)
     1  '  (*) Use dimensions of reciprocal centimeters. (*)'
      write(*,1001) ' '

  101 write(*,1002) 
     1   '(Free Format) (a)Mu(scattering); (b)Mu(absorption): '
 1002 format(a,$)
      c133a=' '
	CALL READ133 (N, C133A)
!      read(*,1003) n,c133a(:n)
      if (n.eq.0) go to 101
      call frein3(n,i,znums,c133a)
      if (i.lt.2) go to 101

      smu=znums(1)
      amu=znums(2)
      write(*,1001) ' '
      write(*,1001) 'Mu(scattering), Mu(absorption),'
     1 //' reciprocal centimeters: ',smu,amu

  102  write(*,1002) '(Answer with <Y>, N, y, or n) OK? '
!       read(*,1003,end=102) n,c133a(:n)
	CALL READ133 (N, C133A)
c       if (n.eq.0) go to 101
       if (n.eq.0) c133a(:1)='Y'
       if (c133a(:1).eq.'N' .or. c133a(:1).eq.'n') go to 101

CCC	Write to anvred.log file
      write(16,1001) ' '
      write(16,1001) 'Input linear absorption coefficients (mu)'
     1     //' corresponding to:'
      write(16,1001)
     1  '  (a) Total scattering (wavelength independent); and'
      write(16,1001)
     1  '  (b) True absorption at <Lambda> = 1.8 Angstroms.'
      write(16,1001)
     1  '  (*) Use dimensions of reciprocal centimeters. (*)'
      write(16,1001) ' '

	write(16,1002) 
     1   '(Free Format) (a)Mu(scattering); (b)Mu(absorption): '
	WRITE (16, *) SMU,AMU
CCC

C --------------------------------------------------
C Spherical absorption correction.
  104 write(*,1001) 'Spherical Absorption Correction --'
  151 write(*,1002) 'Enter radius of sphere, in cm.: '

	write(16,1001) 'Spherical Absorption Correction --'
	write(16,1002) 'Enter radius of sphere, in cm.: '

      c133a=' '
!      read(*,1003) n,c133a(:n)
	CALL READ133 (N, C133A)
      if (n.eq.0) go to 151
      call frein3(n,i,znums,c133a)
      if (i.eq.0) go to 151
      radius=znums(1)     ! This is saved by the 'save' instruction at top.

	WRITE (16, 1601) RADIUS
1601	FORMAT(F10.3)

      RETURN
      end
      
C+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
C
      SUBROUTINE FREIN3(NI,N,NOS,CI)
C**** Subroutine Frein3 -- free format input.
C**** Input:  CI -- Character*133, contains the input
C****               line (text or numbers).
C**** Input:  NI -- integer, contains the number of characters
C****               that are meaningful.  This can be the max.
C****               length of CI if necessary.
C**** Output:  NOS -- Real(20) array -- the interpreted numerical
C****                 input, stored as Reals.
C**** Output:  N -- integer -- the number of numbers found in CI.
C**** This subroutine ignores textual input.
      character*1 ct(13),c
      data ct/'0','1','2','3','4','5','6','7','8','9',
     1       '.','+','-'/
      dimension nc(10)
      data nc/0,1,2,3,4,5,6,7,8,9/
      real nos(20)
      character*133 ci
      integer st,d
C
      ni=ni+1
      st=1
      t=0.0
      n=0
      d=0
      s=1.0
C
      DO 106 I=1,NI
      c=ci(i:i)
      DO 111 J=1,13
       if (c.ne.ct(j)) go to 111
       k=j
       go to (1,1,1,1,1,1,1,1,1,1,2,3,4), j
  111 CONTINUE
      go to (106,116,116), st
  116 assign 106 to irt
      st=1
      go to 9
    1 t=10.*t+nc(k)
      go to (11,106,12), st
   11 st=2
      n=n+1
      go to 106
   12 d=d+1
      go to 106
C
    2 go to (21,22,23), st
   21 n=n+1
   22 st=3
      go to 106
   23 assign 21 to irt
      go to 9
C
    3 go to (11,32,32), st
   32 assign 11 to irt
      go to 9
C
    4 go to (41,42,42), st
   41 s=-1.0
      go to 11
   42 assign 41 to irt
    9 if (d.eq.0) go to 92
      t=t*10.**-d
   92 nos(n)=s*t
      t=0.0
      d=0
      s=1.0
      go to irt, (11,21,41,106)
  106 CONTINUE
C
      return
      end
C

C-------------------------------------------

	SUBROUTINE READ133 (N, C133A)

	CHARACTER*133 C133A

	DO I=1,133
		C133A(I:I) = ' '
	END DO

	READ (*,'(A)') C133A
	N = LNBLNK(C133A)

	RETURN
	END
	
C------------------------------------------------------------

	Subroutine absor_sphere(smu, amu, radius, trans, tbar)
	
!	Subroutine to calculate a spherical absorption correction
!	and tbar. Based on values in:
!
!	C. W. Dwiggins, Jr., Acta Cryst. A31, 395 (1975).
!
!	In this paper, A is the transmission and A* = 1/A is
!	the absorption correction.

!	Input are the smu (scattering) and amu (absorption at 1.8 Ang.)
!	linear absorption coefficients, and the radius R of the sample.
!	The theta angle and wavelength are obained from the common.
!	The absorption (absn) and tbar are returned.

!	A. J. Schultz, June, 2008
	
	include 'datacom_sns.inc'
	
	real mu, muR	!mu is the linear absorption coefficient,
			!R is the radius of the spherical sample.
	
!	For each of the 19 theta values in Dwiggins (theta = 0.0 to 90.0
!	in steps of 5.0 deg.), the ASTAR values vs.muR were fit to a third
!	order polynomial in Excel. These values are given below in the
!	data statement.
	dimension pc(4,19)	!polynomial coefficients
	
	data pc/
	1	0.9369,  2.1217, -0.1304,  1.1717,
	2	0.9490,	 2.0149,  0.0423,  1.0872,
	3	0.9778,  1.7559,  0.4664,  0.8715,
	4	1.0083,  1.4739,  0.9427,  0.6068,
	5	1.0295,  1.2669,  1.3112,  0.3643,
	6	1.0389,  1.1606,  1.5201,  0.1757,
	7	1.0392,  1.1382,  1.5844,  0.0446,
	8	1.0338,  1.1724,  1.5411, -0.0375,
	9	1.0261,  1.2328,  1.4370, -0.0853,
	1	1.0180,  1.3032,  1.2998, -0.1088,
	2	1.0107,  1.3706,  1.1543, -0.1176,
	3	1.0046,  1.4300,  1.0131, -0.1177,
	4	0.9997,  1.4804,  0.8820, -0.1123,
	5	0.9957,  1.5213,  0.7670, -0.1051,
	6	0.9929,  1.5524,  0.6712, -0.0978,
	7	0.9909,  1.5755,  0.5951, -0.0914,
	8	0.9896,  1.5913,  0.5398, -0.0868,
	9	0.9888,  1.6005,  0.5063, -0.0840,
	1	0.9886,  1.6033,  0.4955, -0.0833/
	
	
	mu = smu + (amu/1.8)*wl
	
	muR = mu*radius
	
	pi = acos(-1.0)
	
	theta = (twoth*180.0/pi)/2.0

!	Using the polymial coefficients, calulate ASTAR (= 1/transmission) at
!	theta values below and above the actual theta value.

	i = int(theta/5.0) + 1
	astar1 = pc(1,i) + pc(2,i)*mur + pc(3,i)*mur**2 + pc(4,i)*mur**3
	
	i = i+1
	astar2 = pc(1,i) + pc(2,i)*mur + pc(3,i)*mur**2 + pc(4,i)*mur**3
	
!	Do a linear interpolation between theta values.

	frac = mod(theta,5.0)/5.0
		
	astar = astar1*(1-frac) + astar2*(frac)		!Astar is the correction
	
	trans = 1.0/astar	!trans is the transmission
				!trans = exp(-mu*tbar)
	
!	Calculate TBAR as defined by Coppens.

	tbar = -log(trans)/mu
	
	RETURN
	END
	
C------------------------------------------------------------

	SUBROUTINE READREFL_SNS_HEADER (IUNIT,IEOF)

!!!	This subroutine will read the first 6 lines of a peaks or
!!!	integrate file with the SNS format.
!!!	A. J. Schultz	June, 2008


	INCLUDE 'datacom_SNS.inc'

!	Read a reflection from a reflection file.
!	The first variable of each record is ISENT.

!	Linux version, January 2002, A.Schultz

!	ISENT = 0 --> variable name list for describing the histogram
!	      = 1 --> variable values of above list
!	      = 2 --> variable name list for a reflection
!	      = 3 --> variable values of above list
!	      = 4 --> variable name list for parameters for detectors
!	      = 5 --> variable values of parameters for detectors
!	      = 6 --> variable name list: L1    T0_SHIFT
!	      = 7 --> variable values: L1    T0_SHIFT

!	IEOF = 0 --> not end-of-file
!	     = 1 --> end-of-file

	CHARACTER*160 TEXT
	
	IEOF=0
	
	write (*, 50)
50	format(//,'Reflection file header:')

!  Read the first line
	READ (IUNIT, 100, END=999) TEXT
100	FORMAT(A)
	write (*, 100) TEXT

!  Read the second line, ISENT = 6
	READ (IUNIT,100,END=999) TEXT
	write (*, 100) TEXT
	
!  Read the third line, ISENT = 7
	READ (IUNIT,100,END=999) TEXT

	READ (TEXT,150) ISENT
150	FORMAT(I1)

! ISENT=7
	IF (ISENT.EQ.7) THEN
		READ (text, *) ISENT, L1, TZERO
	END IF

	write (*, 100) TEXT

	
!  Read the fourth line, ISENT = 4
	READ (IUNIT,100,END=999) TEXT

!  Read lines with ISENT = 5
	nod = 0				!number of detectors
300	read (iunit, 100) text      	!ISENT=5

	READ (TEXT,150) ISENT
          IF (ISENT.NE.5) THEN	!End of reading detector parameters
                  BACKSPACE (IUNIT)
                  write (*, *) ' '
                  write (*, *) 'There are ',nod,' detectors.'
                  write (*, *)
                  RETURN
          END IF

	write (*, 100) TEXT
          
	  nod = nod + 1
	  i = nod
	  READ (TEXT, *) ISENT, detnum(i), NROWS(i), NCOLS(i),
	1	WIDTH(i), HEIGHT(i), DEPTH(i), DIST(i),
	2	CenterX(i), CenterY(i), CenterZ(i),
	3	BaseX(i), BaseY(i), BaseZ(i),
	4	UpX(i), UpY(i), UpZ(i) 
         GO TO 300
	

	WRITE (*,*) '*** Error in SUBROUTINE READREFL ***'
	RETURN

999	IEOF=1	!END OF FILE
	RETURN	
	END


C------------------------------------------------------------
C------------------------------------------------------------
C------------------------------------------------------------

	SUBROUTINE READREFL_SNS (IUNIT,IEOF)

!!!	Read a peaks or integrate file with the SNS format.
!!!	A. J. Schultz	April, 2008


	INCLUDE 'datacom_SNS.inc'

!	Read a reflection from a reflection file.
!	The first variable of each record is ISENT.

!	Linux version, January 2002, A.Schultz

!	ISENT = 0 --> variable name list for describing the histogram
!	      = 1 --> variable values of above list
!	      = 2 --> variable name list for a reflection
!	      = 3 --> variable values of above list
!	      = 4 --> variable name list for parameters for detectors
!	      = 5 --> variable values of parameters for detectors
!	      = 6 --> variable name list: L1    T0_SHIFT
!	      = 7 --> variable values: L1    T0_SHIFT

!	IEOF = 0 --> not end-of-file
!	     = 1 --> end-of-file

	CHARACTER*160 TEXT	

	READ (IUNIT,100,END=999) TEXT
100	FORMAT(A)

	IEOF=0

	READ (TEXT,150) ISENT
150	FORMAT(I1)

! ISENT=3 ---> reflection
	IF (ISENT.EQ.3) THEN				!Reflection record
		READ (text, *) ISENT,SEQNUM,H,K,L,col,row,chan,L2, !ISENT=3
	1		twoth,az,wl,dsp,ipkobs,inti,sigi,reflag
		RETURN
	END IF

! ISENT=0 ---> new histogram
	IF (ISENT.EQ.0) THEN	!First record of a new histogram
		READ (IUNIT,*) ISENT,NRUN,DN,		!ISENT=1
	1		CHI,PHI,OMEGA,MONCNT
		READ (IUNIT,100) TEXT				!ISENT=2
		READ (IUNIT, *) ISENT,SEQNUM,H,K,L,col,row,chan,L2, !ISENT=3
	1		twoth,az,wl,dsp,ipkobs,inti,sigi,reflag
		RETURN
	END IF


	WRITE (*,*) '*** Error in SUBROUTINE READREFL ***'
	RETURN

999	IEOF=1	!END OF FILE
	RETURN	
	END


C------------------------------------------------------------
C------------------------------------------------------------
C------------------------------------------------------------

        SUBROUTINE READANS (NCHRS,ANS)

!     Read terminal input (answer) from user.
!	Return number of characters and text.
!	A.Schultz	January, 2002

        CHARACTER*60 ANS

	DO I=1,60
		ANS(I:I) = ' '
	END DO

        READ (*,'(A)') ANS
        NCHRS = LNBLNK(ANS)

        RETURN
        END




