package utils;

import org.example.cloudstorage.exception.InvalidPathException;

public class ValidationUtils {
    public final static int MIN_FOLDER_NAME_LENGTH = 3;
    public final static int MAX_FOLDER_NAME_LENGTH = 20;
    private static final int UPLOADED_RESOURCE_NAME_MAX_LENGTH = 50;


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
}
