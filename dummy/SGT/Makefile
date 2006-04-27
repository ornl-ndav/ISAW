
deploy:
	mvn deploy:deploy-file -DgroupId=ISAW -DartifactId=SGT \
		-Dversion=1.0-SNAPSHOT -Dpackaging=jar -Dfile=sgt_v2.jar \
		-DrepositoryId=sns-internal \
		-Durl=scpexe://snsdev.ornl.gov/var/www/maven/internal

local_deploy:
	mvn install:install-file -DgroupId=ISAW -DartifactId=SGT \
		-Dversion=1.0-SNAPSHOT -Dpackaging=jar -Dfile=sgt_v2.jar

