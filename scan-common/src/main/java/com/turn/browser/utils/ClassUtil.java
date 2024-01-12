package com.turn.browser.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {
    private static Logger logger = LoggerFactory.getLogger(ClassUtil.class);
    public static Set<Class<?>> getClasses(String pack) {
        //The collection of the first class class
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        // Whether to loop and iterate
        boolean recursive = true;
        // Get the name of the package and replace it
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // Define a collection of enumerations and loop to process things in this directory
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // Loop iterates
            while (dirs.hasMoreElements()) {
                // Get the next element
                URL url = dirs.nextElement();
                // Get the name of the protocol
                String protocol = url.getProtocol();
                // If it is saved on the server in the form of a file
                if ("file".equals(protocol)) {
                    logger.info("file type scan");
                    // Get the physical path of the package
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // Scan the files under the entire package as files and add them to the collection
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // If it is a jar package file
                    //Define a JarFile
                    logger.info("jar type scanning");
                    JarFile jar;
                    try {
                        // Get jar
                        jar = ((JarURLConnection) url.openConnection())
                                .getJarFile();
                        // Get an enumeration class from this jar package
                        Enumeration<JarEntry> entries = jar.entries();
                        // Same loop iteration
                        while (entries.hasMoreElements()) {
                            // Get an entity in the jar, which can be a directory and some other files in the jar package, such as META-INF and other files
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // If it starts with /
                            if (name.charAt(0) == '/') {
                                // Get the following string
                                name = name.substring(1);
                            }
                            // If the first half is the same as the defined package name
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // If it ends with "/", it is a package
                                if (idx != -1) {
                                    // Get the package name and replace "/" with "."
                                    packageName = name.substring(0, idx)
                                            .replace('/', '.');
                                }
                                // If it can be iterated and it is a package
                                if ((idx != -1) || recursive) {
                                    // If it is a .class file and not a directory
                                    if (name.endsWith(".class")
                                            && !entry.isDirectory()) {
                                        // Remove the ".class" at the end to get the real class name
                                        String className = name.substring(
                                                packageName.length() + 1, name
                                                        .length() - 6);
                                        try {
                                            //Add to classes
                                            classes.add(Class
                                                    .forName(packageName + '.'
                                                            + className));
                                        } catch (ClassNotFoundException e) {
                                            // log
                                            logger.error("Add user-defined view class error:",e);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        logger.error("Error getting files from jar package when scanning user-defined views:",e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("ERROR:",e);
        }

        return classes;
    }

    public static void findAndAddClassesInPackageByFile(String packageName,
                                                        String packagePath, final boolean recursive, Set<Class<?>> classes) {
        // Get the directory of this package and create a File
        File dir = new File(packagePath);
        // If it does not exist or is not a directory, return directly
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("There are no files under the user-defined package name " + packageName + "");
            return;
        }
        // If it exists, get all files under the package, including directories
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // Custom filtering rules if it can be looped (including subdirectories) or files ending with .class (compiled java class files)
            public boolean accept(File file) {
                return (recursive && file.isDirectory())
                        || (file.getName().endsWith(".class"));
            }
        });
        // Loop through all files
        for (File file : dirfiles) {
            // If it is a directory, continue scanning
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "."
                                + file.getName(), file.getAbsolutePath(), recursive,
                        classes);
            } else {
                // If it is a java class file, remove the .class at the end and leave only the class name.
                String className = file.getName().substring(0,
                        file.getName().length() - 6);
                try {
                    //Add to the collection
                    // classes.add(Class.forName(packageName + '.' +
                    // className));
                    // After replying to the reminder from classmates, there are some disadvantages in using forName here. It will trigger the static method and the load of classLoader is not used cleanly.
                    classes.add(Thread.currentThread().getContextClassLoader()
                            .loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    // log.error("Error in adding user-defined view class. Such .class file cannot be found");
                    logger.error("Error in adding user-defined view class. Such .class file cannot be found:",e);
                }
            }
        }
    }
}
