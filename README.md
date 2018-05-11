# SWIM Smart City

A real-time view of traffic conditions in the city of Palo Alto.

# Installation

* Install [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). Ensure that your JAVA_HOME environment variable is pointed to the Java 8 installation location. Ensure that your PATH includes $JAVA_HOME.

* Install [Gradle](https://gradle.org/install/). Ensure that your PATH includes the Gradle `bin` directory.

* Install a web server that can load static html pages. So please install a web server of your choice. eg: [Apache Web Server](https://httpd.apache.org/), 
  [Node's http-server](https://www.npmjs.com/package/http-server), [Nginx](https://docs.nginx.com/nginx/admin-guide/installing-nginx/installing-nginx-open-source/) etc.
  
# Run

## Run the application
Execute the command `gradle run` from a shell pointed to the application's home directory. This will start the Swim server on port 9001.
   ```console
    user@machine:~$ gradle run
   ```

## Run the UI
Go to the ui directory. Run the web server (which you installed earlier) in the UI directory. Load the index.html pages in 
your browser using the web-server. For example if your web server is running on port 8080 then type the following URL on your browser: 'http://localhost:8080/index.html'.

Click on an intersection's traffic light (the blue dots) or an intersection's lane to get real-time insights on each element of the intersection. 
    
