package utils;

import io.minio.messages.Item;

import java.nio.file.Path;
import java.nio.file.Paths;



public class PathUtils {

    public static String buildParentPath(String path){
        String truePath;
        Path pathObject =  Paths.get(path);
        Path parent =  pathObject.getParent();
        if(parent == null){
            truePath = "";
        }else{
            truePath = parent.toString().replace("\\","/")+ "/";
        }
        return truePath;
    }

    public static String buildRootPath(Long id){
        return "user-" + id + "-files/";
    }

    public static String extractResourceName(String objectName, boolean isTrailingSlash){
        Path testFilePath = Paths.get(objectName);
        String folderName = testFilePath.getFileName().toString();
        return isTrailingSlash ? folderName + "/" : folderName;
    }

    public static String deleteRootPath(String path, Long id){
        return path.replace("user-"+id+"-files/","");
    }

    //TODO 3 похожих метода! Надо что-то придумать
    public static boolean isPathValid(String path){
        if(path.startsWith("/")) {
            return false;
        }

        if(hasMultipleSlashes(path)){
            return false;
        }

        if(path.equals("") || path.endsWith("/")){
            return true;
        }

        return false;
    }

    public static boolean isPathValidToDeleteOrDownload(String path){
        if(path.equals("") || path.startsWith("/")){
            return false;
        }
        if(hasMultipleSlashes(path)){
            return false;
        }
        return true;
    }

    public static boolean isPathValidToMove(String path){
        if(path.startsWith("/")) {
            return false;
        }
        if(hasMultipleSlashes(path)){
            return false;
        }

        return true;
    }

    private static boolean hasMultipleSlashes(String path){
        if(path.equals("")){
            return false;
        }

       return path.contains("//");
    }


    public static String getRelativePath(String pathWithoutRoot, Path parentDirectory){
        Path currentDirectory = Paths.get(pathWithoutRoot);
        String currentPath = parentDirectory.relativize(currentDirectory).toString().replace("\\", "/");
        return currentPath;
    }

    public static String buildRelativeResourcePath(Item item, String currentPath, Long id){
        String fullPath = deleteRootPath(item.objectName(), id);
        Path currentDirectory = Paths.get(currentPath);

        if (fullPath.endsWith("/")) {
            return getRelativePath(fullPath, currentDirectory) + "/";
        } else {
           return getRelativePath(fullPath, currentDirectory);
        }
    }








}
