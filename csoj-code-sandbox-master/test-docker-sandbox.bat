@echo off
echo Testing JavaDockerCodeSandbox...
java -cp "target/classes;target/test-classes;target/dependency/*" TestJavaDockerCodeSandbox
echo Test completed
pause