# SWIM Smart City

A real-time view of traffic conditions in the city of Palo Alto.

# Installation

* Install [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). Ensure that your JAVA_HOME environment variable is pointed to the Java 8 installation location. Ensure that your PATH includes $JAVA_HOME.

# Run

## Run the application
Execute the command `./gradlew run` from a shell pointed to the application's home directory. This will start the Swim server on port 9001.
   ```console
    user@machine:~$ ./gradlew run
   ```

## Run the UI
Open the following URL on your browser: 'http://localhost:9001'.

Click on an intersection's traffic light (the blue dots) or an intersection's lane to get real-time insights on each element of the intersection. 
    
