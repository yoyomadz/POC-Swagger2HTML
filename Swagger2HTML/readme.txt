How to mvn build project with dependency and run application.
1. Go to source/application directory.
2. Execute command "mvn clean dependency:copy-dependencies package", wait a few minute to success.
3. Go to directory "<app-directory>/target", create folders "INBOUND", "OUTBOUND" and "TEMPLATE".
4. Copy files
	4.1 JSON swagger file from "<app-directory>/INBOUND" to "target/INBOUND".
	4.2 "application.yml" and "logback.xml" from "<app-directory>/src/main/resources" to "target"
5. Go to directory "<app-directory>/target" and execute command
	5.1 "java -jar Swagger2HTML.jar". for execute without file name.
	5.2 "java -jar Swagger2HTML.jar Swagger_iSprint-verifyPasswordEx(approvalotp)_v1.4" for execute with file name.
	* You can input both full name and a part of file name.
6. Check the result 
	6.1 HTML file in directory "<app-directory>/target/OUTBOUND".
	6.2 Log file in directory "<app-directory>/target/LOG".
7. Happy without error.