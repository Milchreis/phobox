# What is phobox?
Phobox is a small tool to extend your local picture storage with a bunch of connectivity features. 
It starts a lightweight server process, which you allows to access your pictures over a graphical user interface
or a REST-API. Moreover you can organize your files and create collections of the best photographs.

**I developed phobox to solve three main problems in my picture workflow:**
 - organize new pictures from camera to the correct directories
 - get a simple and fast access to the pictures on my smartphone at home
 - search for pictures

![phobox](https://github.com/Milchreis/phobox/raw/master/screenshots/phobox.gif)

# Getting started
## Download (BETA-Version)
If you want to try phobox, you can download the jar file from github. Look [here](https://github.com/Milchreis/phobox/raw/master/bin/phobox-0.0.2.jar) for the last version. All versions/builds are saved in the [bin-directory](https://github.com/Milchreis/phobox/tree/master/bin).

## Graphical interface 
1) Start the program with double click on phobox-<version>.jar or `java -jar phobox-<version>.jar` in terminal
2) Set up the directory to your local pictures
3) Open a browser on your local machine or maybe a smartphone in the same wifi to the shown ip address
4) Enjoy your pictures 

## Commandline interface (maybe for raspberry pi or other headless systems)
1) Start the program with `java -jar phobox-<version>.jar [ARGS]`
```
usage: phobox
 -b,--backupDirectory <arg>   Sets the directory for backups
 -h,--help                    Prints this help
 -nw,--noWindow               Hides the application window for headless usage
 -p,--port <arg>              Sets the port for this application (default 8080)
 -s,--storage <arg>           Defines the main storage path
 -w,--watchDirectory <arg>    Scans this directory for new files
```

## Importing new pictures
Phobox reads the `phobox/import/` directory inside your specified picture directory. Each new readable picture will
be processed. Phobox moves the files to the configured directory (if EXIF data exists) and creates thumbnails for
a faster display on remote devices. Additionally you can define another watch directory to listen for new files (maybe a remote directory).  

For small number of files you can use a upload function in the phobox front-end or the drag-and-drop area of the phobox window.

# Features
 - Automatic picture organization in the file directory
 - Browse your pictures on different devices in the same wifi
 - Tagging your pictures
 - Searching for pictures by name or TAGs
 - Creating albums
 - Statistics
 - Upload pictures from remote devices in the same wifi
 - Password saved access
 - Languages: English and German

# Requirements
Phobox is build on Java and just requires a JRE 1.8 or higher. The jar file is a standalone application and runs with `java -jar phobox.jar`. 
The tool runs on the raspberry pi 3 with the Java ARM version great, too.

# Build
The project can build with maven and the goal `clean compile assembly:single` or simply with `mvn`.

# Contribute
Please try it out and leave issues if something goes wrong or you have a feature request. Pull requests are also welcome :-)
[Look here](https://phoboxhq.github.io/support/) for more support possibilities
