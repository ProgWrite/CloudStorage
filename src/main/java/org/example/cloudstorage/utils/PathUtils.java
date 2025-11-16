package org.example.cloudstorage.utils;

import org.example.cloudstorage.exception.InvalidPathException;

import java.nio.file.Path;
import java.nio.file.Paths;


public class PathUtils {

    public static String extractParentPath(String path) {
        if (path == null) {
            throw new InvalidPathException("Path cannot be null");
        }

        String truePath;
        Path pathObject = Paths.get(path);
        Path parent = pathObject.getParent();
        if (parent == null) {
            truePath = "";
        } else {
            truePath = parent.toString().replace("\\", "/") + "/";
        }
        return truePath;
    }

    public static String extractResourceName(String objectName, boolean isTrailingSlash) {
        if (objectName == null) {
            throw new InvalidPathException("Object name cannot be null");
        }

        Path testFilePath = Paths.get(objectName);
        String folderName = testFilePath.getFileName().toString();
        return isTrailingSlash ? folderName + "/" : folderName;
    }

    public static String convertToRelativePath(String pathWithoutRoot, Path parentDirectory) {
        if (pathWithoutRoot == null) {
            throw new InvalidPathException("Path cannot be null");
        }
        if (parentDirectory == null) {
            throw new InvalidPathException("Path cannot be null");
        }

        Path currentDirectory = Paths.get(pathWithoutRoot);
        return parentDirectory.relativize(currentDirectory).toString().replace("\\", "/");
    }

}
