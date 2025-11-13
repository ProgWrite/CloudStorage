package utils;

import io.minio.messages.Item;
import org.example.cloudstorage.exception.InvalidPathException;

import java.nio.file.Path;
import java.nio.file.Paths;


public class PathUtils {

    public static String buildParentPath(String path) {
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

    public static String buildRootPath(Long id) {
        if (id == null) {
            throw new InvalidPathException("Id cannot be null");
        }

        return "user-" + id + "-files/";
    }

    public static String extractResourceName(String objectName, boolean isTrailingSlash) {
        if (objectName == null) {
            throw new InvalidPathException("Object name cannot be null");
        }

        Path testFilePath = Paths.get(objectName);
        String folderName = testFilePath.getFileName().toString();
        return isTrailingSlash ? folderName + "/" : folderName;
    }

    public static String deleteRootPath(String path, Long id) {
        if (path == null) {
            throw new InvalidPathException("Path cannot be null");
        }
        if (id == null) {
            throw new InvalidPathException("Id cannot be null");
        }
        return path.replace("user-" + id + "-files/", "");
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

    public static String buildRelativeResourcePath(Item item, String currentPath, Long id) {
        if (currentPath == null) {
            throw new InvalidPathException("Path cannot be null");
        }
        if (id == null) {
            throw new InvalidPathException("Id cannot be null");
        }
        if (item == null) {
            throw new InvalidPathException("Item cannot be null");
        }

        String fullPath = deleteRootPath(item.objectName(), id);
        Path currentDirectory = Paths.get(currentPath);

        if (fullPath.endsWith("/")) {
            return getRelativePath(fullPath, currentDirectory) + "/";
        } else {
            return getRelativePath(fullPath, currentDirectory);
        }
    }

    public static String getRelativePath(String pathWithoutRoot, Path parentDirectory) {
        if (pathWithoutRoot == null) {
            throw new InvalidPathException("Path cannot be null");
        }
        if (parentDirectory == null) {
            throw new InvalidPathException("Path cannot be null");
        }

        Path currentDirectory = Paths.get(pathWithoutRoot);
        String currentPath = parentDirectory.relativize(currentDirectory).toString().replace("\\", "/");
        return currentPath;
    }

    private static boolean hasMultipleSlashes(String path) {
        if (path.equals("")) {
            return false;
        }

        return path.contains("//");
    }

}
