rm *.class
javac Server.java
javac Client.java
java Server 4 & (sleep 2 && java Client 4)