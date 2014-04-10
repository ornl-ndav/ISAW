C-------------------------------------------------------------------------------
	PROGRAM ANVRED_TO_GSAS
C-------------------------------------------------------------------------------

C	Convert ANVRED SCD reflection file to GSAS reflection files.
C	A. J. Schultz	November, 1994

C			PC version:  February, 1998

C			A. J. Schultz
C			IPNS, Bldg. 360
C			Argonne National Laboratory
C			Argonne, IL 60439-3814, USA
C			Email: ajschultz@anl.gov
C
C       Add option to convert more than one ANVRED SCD file.  
C	Add option to process ANVRED SCD file with discontinuous run numbers.           
C       		X. Wang		August, 2001
C

!	Version 2:  June, 2003
!	Sets histogram number equal to X4.  With two detectors, there are now
!	two histograms for each run file.

!	Version 5:	July, 2003
!	This version is compatible with output from ANVRED5.

!	Version 6:  May, 2005
!	Sets P = 0.0 instead of 0.04

!	Last modification:  February 5, 2007
!		Input format error that did not effect output.

	DIMENSION HKL(3),SCR1(3),SCR2(3),REL(6),REC(6),realVal(3)
	REAL LAM
	CHARACTER EXPNAM*20,FNANV*40,NUM*2,FNAME*40,FNEXP*40
	CHARACTER HTYP(99)*4,key*12,ans*20,aline*80,blank*20
	CHARACTER YN*3, AYN*1	

	DATA HTYP/99*'SNT '/
	data blank/'                    '/

	NVERSION = 6	!VERSION NUMBER

	NFILE = 0
	IHST = 0
	IHST1 = 0		
	NDIFF = 0		
	MUL = 0
	ICODE = 11111
	INDCNT = 0
	FOTSQ = 0.0
	FCSQ = 0.0
	FCTSQ = 0.0
	PHAS = 0.0
	TRANS = 0.0
	EXTCOR = 1.0
	WTFO = 0.0
	TOF = 0.0
	XDET = 0.0
	YDET = 0.0
	PEAK = 0.0
	PKFRAC = 1.0
	SIGMAI = 0.0
	DO I=1,3
	  SCR1(I) = 0.0
	  SCR2(I) = 0.0
	END DO

	OPEN(UNIT=10,FILE='ANV2GSAS.LST',status='OLD',err=20)
	go to 40
20	OPEN(UNIT=10,FILE='ANV2GSAS.LST',status='NEW')

40	write (*, 50) NVERSION
50	FORMAT(/,' Program ANV2GSAS VERSION ', I2,':'//,
	1 '   Convert ANVRED SCD reflection file to GSAS reflection files.'/,
	2 '   1.  First use PC-GSAS to create an EXP file.'/,
	3 '   2.  Use EXPEDT to input single crystal phase information.'/,
	4 '   3.  Exit EXPEDT.'/,
	5 '   4.  Run ANV2GSAS from a DOS command window.'/,
	6 '   5.  In GSAS, run EXPEDT and enter ''Y X'' to rewrite EXP file.'//,
	2 ' See file ANV2GSAS.LST for log of input parameters.'//)
	WRITE (10, 50) NVERSION

90	write (*, 100)
100	FORMAT('EXPNAM ==> ',$)
	read (*, 105) EXPNAM
105	FORMAT(A)
	nchrs = lnblnk(expnam)		!Get number of characters
	write (10,106) EXPNAM(1:NCHRS)
106	FORMAT(/,' EXPNAM ==> ',A)

	FNEXP = EXPNAM(1:NCHRS)//'.EXP'
	OPEN(UNIT=25,FILE=FNEXP,status='OLD',
	1	ERR=1060)
	GO TO 190
1060	write (*,*) ' EXPNAM.EXP FILE DOES NOT EXIST'
	GO TO 90

190	write (*, 200)
200	FORMAT('ANVRED file name (name.hkl) ==> ',$)
	read (*,110) FNANV
110	FORMAT(A)
	write (10,111) fnanv
111	format(/' ANVRED file name ==> ',A)	

	OPEN(UNIT=21,FILE=FNANV,status='OLD',ERR=250)
	read (21,105) aline
	write (*,240) aline
240	format(/'The first line of the ANVRED file is:'/,A)
	rewind(21)
	GO TO 260
250	write (*, *) 'ERROR OPENING ANVRED FILE'
	GO TO 190

260	WRITE (*, 265)
265	FORMAT(/' Type RETURN or ENTER to use default values in <...>'/)
	write (*, 270)
270	FORMAT(/,
	1' Integrated intensities obtained with'/,
	2'	(1) PEAKINT'/,
	3'   or (2) INTSCD or INTEGRATE?'/,
	4' Input 1 or <2> ==> ',$)
	ans=blank
	read (*,105) ans
	if (lnblnk(ans) .eq. 0) then
		iint = 2
	else
		read(unit=ans,fmt=*) iint
	end if
	write (10,270)
	write (10,*) iint

! Get run number of the first historgram
	IF (IINT.EQ.1) THEN
	  READ (21, 589) NRUN1
589	  FORMAT(59X,I5)
	ELSE
	  READ(21, 590) NRUN1 
590	  FORMAT(48X,I7)
	END IF	
	REWIND(21)

	write (*, 600) NRUN1
600	FORMAT(/,'Run number of first histogram ==> <', I5,'>',$)
	ans=blank
	READ (*,105) ans
	IF (lnblnk(ans) .NE. 0) THEN
	READ(unit=ans,fmt=*) NRUN1
	WRITE (10,600)
	WRITE (10,*) NRUN1
	ELSE
	ENDIF
	NFILE = NFILE + 1	! current number of ANVRED SCD files read
	ihst1=ihst1+1		!new histogram number 
	write (*, 601) ihst1
601	FORMAT(/,' GSAS histogram number of first histogram.'/,
	1	'Enter a 1 if these are the first or only data.'/,
	2	'Enter n+1 if there are already n histograms.'/,
	6	'Input new histogram number <', I2, '> ==> ',$)
	write (10,601) ihst1
	ans=blank
	read (*,105) ans
	if (lnblnk(ans) .NE. 0) then
	read(unit=ans,fmt=*) ihst1
	else
	end if
	write (10,*) ihst1

	write (*, 602)
602	FORMAT(/,' SIG(Fo**2) = sqrt( SIGFOSQ**2 + ( P * FOSQ)**2 + K )'/,
	1	'Input P <0.0> ==> ',$)
	ans=blank
	read (*,105) ans
	if (lnblnk(ans) .eq. 0) then
		xp = 0.0
	else
		read(unit=ans,fmt=*) xp
	end if
	write (10,602)
	write (10,*) xp
	write (*, 603)
603   FORMAT('Input K <0> ==> ',$)
	ans=blank
	read (*,105) ans
	if (lnblnk(ans) .eq. 0) then
		xk = 0.0
	else
		read(unit=ans,fmt=*) xk
	end if
	write (10,603)
	write (10,*) xk

! Obtain coefficients for d-spacing calculation.
	CALL RECIP(A1,A2,A3,A4,A5,A6)


C---------  See if histogram records already exist in the EXP file.

	IX = 0	! IX = 0 means that histogram records do NOT already exist in 
		! the EXP file.

	KEY=' EXPR  NHST '
	iFormat = 1
	call readExp (key, iFormat, nhist, realVal, iErr)

	if (iErr .eq. 1) go to 900 

	IF (NHST.EQ.1) HTYP(1) = 'SXC ' !Assume an existing x-ray data set.

C  The EXP file already contains histogram records.
C  The EXP_FILE.INC file can be included in the EXP file
C  at some later stage.
	IX = 1	! IX = 1 means that histogram records already exist in the EXP 
		! file and we will not try to write directly to the EXP file.
	CLOSE(UNIT=25)				!Close the EXP file.
	OPEN(UNIT=25,FILE='EXP-FILE.INC',status='OLD',err=750)
	go to 790
750	OPEN(UNIT=25,FILE='EXP-FILE.INC',status='NEW')

790	write (*, 800)
800	FORMAT(/,
	1' The EXP file will not be automatically updated with new'/,
	2' histogram information. If you wish to update, open the EXP file'/,
	3' with a text editor (Notepad) and include EXP-FILE.INC at the'/,
	4' end of the EXP file. Then enter EXPEDT and type ''Y X''. This'/,
        5' will replace the records at the beginning of the file with'/,
	6' those at the end with the same keys, and will restore the file'/,
	7' to keyed access.'/)

	write (10,800)

C---------

C 800	FORMAT(/,
C	1' The EXP file will not be automatically updated with new'/,
C	2' histogram information.  If you wish to update, enter GSAS'/,
C	3' use EDEXP and include EXP-FILE.INC at the end of the'/,
C	4' EXP file.  Then enter EXPEDT and type ''Y X''.  This will'/,
C	5' replace the records at the beginning of the file with those'/,
C	6' at the end with the same keys, and will restore the file to'/,
C	7' keyed access.'/)
C	write (10,800)
C
C---------


900	IHSTOLD = 0
	
!!!!!!!!!  Begin reading and writing reflections.  !!!!!!!!

1000	IF (IINT .EQ. 1) THEN

C   Data from PEAKINT
	READ (21,1002,END=3000) ISENT,HKL(1),HKL(2),HKL(3),YO1,SIGYO1,
	1	FOSQ,SIGFOSQ,TBAR,LAM,NRUN,ISEQ,IREFLAG
1002	FORMAT(I1,3F4.0,2(F9.2,F7.2),2F7.4,2I5,I4)

		ELSE

C   Data from INTSCD or INTEGRATE
	READ (21,1003,END=3000) HKL(1),HKL(2),HKL(3),
	1	FOSQ,SIGFOSQ,X4,LAM,TBAR,NRUN,ISEQ,
	2       transmission, idn, twoth, dsp, col, row
1003	FORMAT(3F4.0,2F8.2,F4.0,2F8.4,2I7,
	1       F7.4, I4, F9.5, F9.4, 2F7.2)

	xdet = col
	ydet = row
	scr1(1) = nrun
	scr1(2) = iseq
	scr1(3) = idn
	
        END IF

	IF (HKL(1).EQ.0.0 .AND. HKL(2).EQ.0.0
	1	.AND. HKL(3).EQ.0.0) GO TO 3000	!END OF FILE

	SIGFOSQ = SQRT(SIGFOSQ**2 + (XP*FOSQ)**2 + XK)	!!!***

C       ---------------------------------------------------------------

!++	IHST = NRUN - NRUN1 + IHST1	!Convert RUN number to histogram number
	IHST = X4	!++
!!! Open new reflection file if new histogram.

	IF (IHST .NE. IHSTOLD) THEN

	 	IF (NFILE .GT. 1) GO TO 1200	!for more than one ANVERED file

		NDIFF = IHST - IHSTOLD

		IF (NDIFF .GT. 1) THEN
		WRITE(10, 1100) NRUN - NDIFF, NRUN
1100		FORMAT(/'There is a discontinuity of SCD run numbers'/,
		1' between ', I5, ' and ', I5/)  
		IHST1 = IHSTOLD + 1
		NRUN1 = NRUN
		IHST = IHST1
		ELSE
		END IF
C 	-----------------------------------------------------------------

1200		write (*, *) 'IHST = ',IHST
		CLOSE(UNIT=22)

		WRITE (NUM,1700) IHST
1700	  	FORMAT(I2)

		IF (IHST.LT.10) THEN
		  FNAME = EXPNAM(1:NCHRS)//'.S0'//NUM(2:2)
		ELSE
		  FNAME = EXPNAM(1:NCHRS)//'.S'//NUM
		END IF

		OPEN(UNIT=22,FILE=FNAME,
	1	FORM='UNFORMATTED',	!This is a binary file
	1	RECL=144,		!Records currently are RECLEN longwords long,
	1	status='new',		!	RECL = 36*4 = 144
	1	IOSTAT=IFLAG,		!Error flag on open request
	1	ACCESS='DIRECT')
 
C		IF (IHST .GT. 1) CALL WRITEXP (IHSTOLD,FNANV,NREF,DMIN)
		IF (IHSTOLD .GE. 1) CALL WRITEXP (IHSTOLD,FNANV,NREF,DMIN)

		IHSTOLD = IHST
		NREF = 0
		DMIN = 1000.

	END IF

!!! Calculate d-spacing.
	DSP = HKL(1)*HKL(1)*A1 + HKL(2)*HKL(2)*A2 + HKL(3)*HKL(3)*A3 +
	1	2.0*HKL(1)*HKL(2)*A4 + 2.0*HKL(1)*HKL(3)*A5 +
	2	2.0*HKL(2)*HKL(3)*A6
	DSP = SQRT(DSP)
	DSP = 1.0/DSP
!!!

	NREF = NREF + 1
	
        
	WRITE (22,REC=NREF) (HKL(I),I=1,3),MUL,ICODE,INCDNT,
	1	DSP,LAM,FOSQ,SIGFOSQ,FOTSQ,FCSQ,FCTSQ,PHAS,TRANS,
	2	EXTCOR,WTFO,TOF,XDET,YDET,PEAK,TBAR,PKFRAC,
	3	SIGMAI,(SCR1(I),I=1,3),(SCR2(I),I=1,3)

	IF (DSP .LT. DMIN) DMIN = DSP

	GO TO 1000

3000	CALL WRITEXP (IHST,FNANV,NREF,DMIN) !Write last histogram.

C	---------------------------------------------
C	Option to convert additional anvred SCD file 
C

	WRITE (*, 3010)
3010	FORMAT('Convert Another ANVRED SCD Reflection File ==> (y or <n>) ',$)
	READ (*, 3020) YN
3020	FORMAT(A)
	AYN=YN(1:1)
	IF (AYN.EQ.'Y') Then
	IHST1 =IHST		! current number of histograms
	GOTO 190       
	ELSEIF (AYN.EQ.'y') Then
	IHST1 =IHST		! current number of histograms
	GOTO 190   
	ENDIF                
C	------------------------------------------------
C  Write some additional records.

3101	WRITE (25,3200) IHST
3200	FORMAT(' EXPR  NHST ',I5)

	I = (IHST/12) + 1

	DO J=1,I

		IF (J .LT. I) N = 12
		IF (J .EQ. I) N = IHST - 12*(I-1)
		WRITE (25,3100) J,(HTYP(JJ),JJ=1,N)
3100		FORMAT(' EXPR  HTYP',I1,2X,12(A4,1X))

	END DO

	write (*,*) 'End of program.  Type RETURN to exit.'
	read (*,105) ans

	stop
	end

C-------------------------------------------------------------------------------
	SUBROUTINE RECIP (A1,A2,A3,A4,A5,A6)
C-------------------------------------------------------------------------------

C	CALCULATE RECIPROCAL CELL PARAMETERS FROM THE REAL PARAMETERS.
C	THEN CALCULATE COEFFICIENTS FOR D-SPACING CALCULATION.

	DIMENSION REC(6),realVal(3)
	character key*12

	RAD = 57.29577951

	KEY='CRS1  ABC   '
500	FORMAT(12X,3F10.6)
	iFormat = 2
	call readExp (key,iFormat,intVal,realVal,iErr)
	a = realVal(1)
	b = realVal(2)
	c = realVal(3)

	key = 'CRS1  ANGLES'
	iFormat = 3
	call readExp (key, iFormat, intVal, realVal,iErr)
	alpha = realVal(1)
	beta = realVal(2)
	gamma = realVal(3)

	write (*, 700)A,B,C,ALPHA,BETA,GAMMA
700	FORMAT(/,' Lattice constants:'/,5x,3f10.6,3f10.4/)

	CA = COS(ALPHA/RAD)
	SA = SIN(ALPHA/RAD)
	CB = COS(BETA/RAD)
	SB = SIN(BETA/RAD)
	CG = COS(GAMMA/RAD)
	SG = SIN(GAMMA/RAD)

	V = A*B*C*SQRT(1.0 - CA**2 - CB**2 - CG**2
	1	+ 2*CA*CB*CG)

	REC(1) = B*C*SA/V
	REC(2) = A*C*SB/V
	REC(3) = A*B*SG/V

	REC(4) = (CB*CG - CA)/(SB*SG)
	REC(4) = ACOS(REC(4))

	REC(5) = (CA*CG - CB)/(SA*SG)
	REC(5) = ACOS(REC(5))

	REC(6) = (CA*CB - CG)/(SA*SB)
	REC(6) = ACOS(REC(6))

	VS = 1.0/V


	A1 = REC(1)*REC(1)		!a* squared
	A2 = REC(2)*REC(2)		!b* squared
	A3 = REC(3)*REC(3)		!c* squared
	A4 = REC(1)*REC(2)*COS(REC(6))	!a*b*cos(gamma)
	A5 = REC(1)*REC(3)*COS(REC(5))	!a*c*cos(beta)
	A6 = REC(2)*REC(3)*COS(REC(4))	!b*c*cos(alpha)

	RETURN
	END

C-------------------------------------------------------------------------------
	SUBROUTINE WRITEXP (IHST,FNANV,NREF,DMIN)
C-------------------------------------------------------------------------------

C	This subroutine writes histogram records to be included in the EXP file.

	CHARACTER FNANV*12
	WRITE (25,100) IHST,FNANV
100	FORMAT('HST',I3,'  HFIL  ',A)
	WRITE (25,110) IHST
110	FORMAT('HST',I3,'  HNAM  Histogram generated from ANVRED data')

	WRITE (25,120) IHST
120	FORMAT('HST',I3,'  INST    1')

	WRITE (25,130) IHST
130	FORMAT('HST',I3,' IRAD     0')

	WRITE (25,140) IHST,NREF,DMIN
140	FORMAT('HST',I3,' NREF ',I5,F10.5)

	WRITE (25,150) IHST
150	FORMAT('HST',I3,'HSCALE     1.0000        Y    0')

	RETURN
	END

!--------------------------------------------------------------------
	subroutine readExp (key, iFormat, intVal, realVal, iErr)
!-------------------------------------------------------------------------------

	dimension realVal(3)
	character key*12,key1*12,line*80

	iErr=0
	rewind 25

10	read (25,50,end=1000) line
50	format(a)

	read (unit=line,fmt=50) key1
	if (key1 .ne. key) go to 10

!  The KEYs match.
	go to (1,2,3) iformat

1	read (unit=line,FMT=100) iVal
100	format(12x,I5)
	return

2	read (unit=line, fmt=200) (realVal(i),i=1,3)
200	format(12x, 3F10.6)
	return

3	read (unit=line, fmt=300) (realVal(i),i=1,3)
300	format(12x, 3F10.4)
	return

1000	iErr = 1	! Could not find key.
	return

	end

