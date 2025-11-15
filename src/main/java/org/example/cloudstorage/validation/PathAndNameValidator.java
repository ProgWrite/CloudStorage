package org.example.cloudstorage.validation;

import org.example.cloudstorage.exception.InvalidPathException;

public class PathAndNameValidator {
    private final static int MIN_FOLDER_NAME_LENGTH = 3;
    private final static int MAX_FOLDER_NAME_LENGTH = 20;
    private static final int UPLOADED_RESOURCE_NAME_MAX_LENGTH = 150;


    public static void validateResourceName(String folderName) {
        if(folderName == null){
            throw new InvalidPathException("Folder name cannot be null");
        }

        if (folderName.length() < MIN_FOLDER_NAME_LENGTH || folderName.length() > MAX_FOLDER_NAME_LENGTH) {
            throw new InvalidPathException(String.format("Resource name must be between %d and %d characters",
                    MIN_FOLDER_NAME_LENGTH, MAX_FOLDER_NAME_LENGTH));
        }

    }

    public static void validateResourceNameForUpload(String folderName) {
        if(folderName == null){
            throw new InvalidPathException("Folder name cannot be null");
        }

        if (folderName.length() > UPLOADED_RESOURCE_NAME_MAX_LENGTH) {
            throw new InvalidPathException(String.format("Uploaded resource name must be less then %d characters",
                    UPLOADED_RESOURCE_NAME_MAX_LENGTH));
        }

    }


    public static boolean isPathValid(String path) {
        if (path == null) {
            throw new InvalidPathException("Path cannot be null");
        }

        if (path.startsWith("/")) {
            return false;
        }

        if (hasMultipleSlashes(path)) {
            return false;
        }

        if (path.equals("") || path.endsWith("/")) {
            return true;
        }

        return false;
    }

    public static boolean isPathValidToDeleteOrDownload(String path) {
        if (path == null) {
            throw new InvalidPathException("Path cannot be null");
        }

        if (path.equals("") || path.startsWith("/")) {
            return false;
        }
        if (hasMultipleSlashes(path)) {
            return false;
        }
        return true;
    }

    public static boolean isPathValidToMove(String path) {
        if (path == null) {
            throw new InvalidPathException("Path cannot be null");
        }

        if (path.startsWith("/")) {
            return false;
        }
        if (hasMultipleSlashes(path)) {
            return false;
        }

        return true;
    }

    private static boolean hasMultipleSlashes(String path) {
        if (path.equals("")) {
            return false;
        }

        return path.contains("//");
    }

}
