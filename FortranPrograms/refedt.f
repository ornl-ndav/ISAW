C-------------------------------------------------------------------------------
	PROGRAM REFEDT
C-------------------------------------------------------------------------------

C	Re-write GSAS reflection files.  Set ICODE to reject reflections based
C	on various tests.

C       Last modification data: April 4, 2014

C	A.J. Schultz (Email: ajschultz@anl.gov)	May, 1997

C	Modified for PC, February 1998.		AJS

C	Output rejected reflections to refedt.lst file
C				October, 2003		AJS

	DIMENSION HKL(3),SCR1(3),SCR2(3),REL(6),REC(6)
	REAL LAM
	CHARACTER EXPNAM*20,NUM*2,FNAME*30,FNEXP*30
	CHARACTER key*12,KEY1*12,ans*20,blank*20

	data blank/'                    '/


	OPEN(UNIT=10,FILE='refedt.lst',status='OLD',err=20)
	go to 90
20	open(unit=10,file='refedt.lst',status='new')

	write (*,10)
10	FORMAT(/,'*****  PROGRAM REFEDT  *****'//,
	1' Program REFEDT allows one to reject reflections based on'/,
	2' criteria not available within GSAS.  For each reflection,'/,
	3' ICODE is re-set back to 11211 if it is 11215.  Then, various'/,
	4' tests are performed to see if ICODE should be set to 11215'/,
	5' which will mean GSAS will not use it in the least squares,'/,
	6' although Fcalc is still calculated and can be examined with'/,
	7' REFLIST.'/)
	WRITE (10,10)


90      write (*, 100)
100	FORMAT('EXPNAM ==> ',$)
	read (*,105) EXPNAM
105	FORMAT(A)
	write (10,106) EXPNAM
106	FORMAT(/,' EXPNAM ==> ',A)
	nchrs = lnblnk(expnam)		!Get number of characters

	FNEXP = EXPNAM(1:nchrs)//'.EXP'
	write (*,105) fnexp
	open(unit=25,file=fnexp,status='old',err=160)

	GO TO 190
160	write (*,105) ' EXPNAM.EXP FILE DOES NOT EXIST'
	GO TO 90

190	key =' EXPR  NHST '
	iFormat = 1		!Indicates the format for the READ
	call readExp (key,iFormat,nHist)
	write (10,605) nhist
605	format(/' Number of histograms ==> ',I4/)
	write (*, 605) nhist

	write (*,700)
700	format('Type RETURN or ENTER to use default values in <...>.')

	write (*,1030	)
1030	FORMAT(/,'Input minimum wavelength <0.0>: ',$)
	write (10,1030)
	ans=blank
	read (*,105) ans
	if (lnblnk(ans).EQ.0) then
		WLMIN=0.0
	else
		read(unit=ans,fmt=*) wlmin
	end if
	write (10,*) wlmin

	write (*,1040)
1040	FORMAT(/,'Input maximum wavelength <99.0>: ',$)
	write (10,1040)
	ans=blank
	read (*,105) ans
	if (lnblnk(ans).EQ.0) then
		WLmax=99.0
	else
		read(unit=ans,fmt=*) wlmax
	end if
	write (10,*) wlmax

	write (*,1050)
1050	FORMAT(/,'Input minimum extintion correction <0.0>: ',$)
	write (10,1050)
	ans=blank
	read (*,105) ans
	if (lnblnk(ans).EQ.0) then
		EXTMIN=0.0
	else
		read(unit=ans,fmt=*) extmin
	end if
	write (10,*) extmin

	write (*,1080)
1080	FORMAT(/,'Input maximum FOSQ/FCSQ ratio <0 for no test>: ',$)
	write (10,1080)
	ans=blank
	read (*,105) ans
	if (lnblnk(ans).EQ.0) then
		xfcsq=0
	else
		read(unit=ans,fmt=*) xfcsq
	end if
	write (10,*) xfcsq

	write (*,1090)
1090	FORMAT(/,'Input maximum FCSQ/FOSQ ratio <0 for no test>: ',$)
	write (10,1090)
	ans=blank
	read (*,105) ans
	if (lnblnk(ans).EQ.0) then
		xfosq=0.0
	else
		read(unit=ans,fmt=*) xfosq
	end if
	write (10,*) xfosq

	write (*,1095)
1095	FORMAT(/,
	1'Input maximum ABS(FOSQ-FCSQ)/SIGFOSQ <0 for no test>: ',$)
	write (10,1095)
	ans=blank
	read (*,105) ans
	if (lnblnk(ans).EQ.0) then
		nWDF=0
	else
		read(unit=ans,fmt=*) nWDF
	end if
	write (10,*) nWDF

	write (*,2095)
2095	FORMAT(/,
	1'Input maximum FOSQ/SIGFOSQ <0 for no test>: ',$)
	write (10,2095)
	ans=blank
	read (*,105) ans
	if (lnblnk(ans).EQ.0) then
		nSIG=0
	else
		read(unit=ans,fmt=*) nSIG
	end if
	write (10,*) nSIG


	WRITE (10,1100)
1100	FORMAT(//,' IHST   H    K    L       DSP       LAM      FOSQ',
	1	'   SIGFOSQ      FCSQ     FCTSQ    EXTCOR      TBAR'/)

!!!!!!!!!  Begin reading and writing reflections.  !!!!!!!!

	ITOTAL=0	! Total number of reflections.
	IREJ=0		! Number of rejected reflections.
	write (*,*) ' '

	DO IHST=1,NHIST

		write (*,1600) IHST
1600		FORMAT('Processing histogram ',I3)
		write (10,1600) ihst

		WRITE (unit=NUM,fmt=1700) IHST
1700	  	FORMAT(I2)

		IF (IHST.LT.10) THEN
		  FNAME = EXPNAM(1:NCHRS)//'.S0'//NUM(2:2)
		ELSE
		  FNAME = EXPNAM(1:NCHRS)//'.S'//NUM
		END IF

		OPEN(UNIT=12,FILE=FNAME,
	1	FORM='UNFORMATTED',		!This is a binary file
	1	RECL=36*4,			!Records currently are RECLEN longwords long
	1	status='old',
	1	IOSTAT=IFLAG,			!Error flag on open request
	1	ACCESS='DIRECT')

		if (iflag.ne.0) go to 1750
		GO TO 1800
1750		write (*,*) ' IHST =',IHST,'  Error opening reflection file.'
		GO TO 4000

1800		KEY1 = 'HST '//NUM//' NREF '
		call readExp (key1, iFormat, nref)

		IREF = 0

2000		IREF = IREF + 1

		IF (IREF .GT. NREF) GO TO 3000

!  Inserted the err=2990 parameter since for some reason NREF from the
!  exp file too large by two.
!  Don't know why?						AJS 2/22/05
		READ (12,rec=iref,err=2990)
	1	(HKL(I),I=1,3),MUL,ICODE,INCDNT,
	2	DSP,LAM,FOSQ,SIGFOSQ,FOTSQ,FCSQ,FCTSQ,PHAS,TRANS,
	3	EXTCOR,WTFO,TOF,XDET,YDET,PEAK,TBAR,PKFRAC,
	4	SIGMAI,(SCR1(I),I=1,3),(SCR2(I),I=1,3)

        if (ICODE .eq. 11212) go to 2200  ! space group extinct
	    ITOTAL = ITOTAL + 1
		IF (ICODE.EQ.11215) ICODE=11211		!Reset ICODE

		IF (LAM .LT. WLMIN) ICODE=11215
		IF (LAM .GT. WLMAX) ICODE=11215
		IF (EXTCOR .LT. EXTMIN) ICODE=11215
		IF (xFCSQ.GT.0 .AND. FOSQ.GT.(xFCSQ*FCSQ)) ICODE=11215
		IF (xFOSQ.GT.0 .AND. FCSQ.GT.(xFOSQ*FOSQ)) ICODE=11215
		  WDF = ABS(FOSQ-FCSQ)/SIGFOSQ
		IF (NWDF.GT.0 .AND. WDF.GT.NWDF) ICODE=11215
		IF (NSIG.GT.0 .AND. FOSQ.LT.(NSIG*SIGFOSQ)) ICODE=11215

		IF (ICODE.EQ.11215) THEN
			IREJ = IREJ + 1
			WRITE (10,2100) IHST,(HKL(I),I=1,3),DSP,LAM,
	1			FOSQ,SIGFOSQ,FCSQ,FCTSQ,EXTCOR,TBAR
2100			FORMAT(I5,3F5.0,8F10.3)
		END IF

2200		WRITE (12,REC=IREF) (HKL(I),I=1,3),MUL,ICODE,INCDNT,
	1	DSP,LAM,FOSQ,SIGFOSQ,FOTSQ,FCSQ,FCTSQ,PHAS,TRANS,
	2	EXTCOR,WTFO,TOF,XDET,YDET,PEAK,TBAR,PKFRAC,
	3	SIGMAI,(SCR1(I),I=1,3),(SCR2(I),I=1,3)

		GO TO 2000

2990		write (*,*) 'NREF too large, IHST =',ihst
3000		CLOSE(UNIT=12)

4000	END DO

	WRITE (10,5000) IREJ, ITOTAL
5000	FORMAT(//,'*** ',I6,
	1' reflections were rejected out of a total of ',
	2 I6,'. ***'/)
	write (*,5000) IREJ,ITOTAL
	write (*,6000)
6000	FORMAT(/,' ************************************************'/,
	1	 ' See file REFEDT.LST for log of input parameters.'/,
	2	 ' ************************************************'/)

	write (*,*) 'Type RETURN to exit.'
	read (*,105) ans

 	stop
	end

!--------------------------------------------------------------------

	subroutine readExp (key,iFormat,iVal)

	character key*12,key1*12,line*80

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

2	return

3	return

1000	write (*,1001) key
1001	format(' Could not find key = ',A,' in the EXP file.')
	stop

	end

