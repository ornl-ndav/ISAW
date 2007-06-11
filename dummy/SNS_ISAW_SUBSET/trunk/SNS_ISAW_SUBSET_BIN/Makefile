
deploy:
	mvn deploy:deploy-file -DgroupId=ISAW \
		-DartifactId=sns-isaw-subset-bin \
		-Dversion=1.0 -Dpackaging=jar \
		-Dfile=sns-isaw-subset-bin.jar \
		-DrepositoryId=sns-internal \
		-Durl=scpexe://snsdev.ornl.gov/var/www/maven/internal

local_deploy:
	mvn install:install-file -DgroupId=ISAW \
		-DartifactId=sns-isaw-subset-bin \
		-Dversion=1.0 -Dpackaging=jar \
		-Dfile=sns-isaw-subset-bin.jar

