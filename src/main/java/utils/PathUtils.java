package utils;

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

    public static boolean isPathValid(String path){
        if(path.equals("/")){
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

    public static boolean isPathValidToDelete(String path){
        if(path.equals("/") || path.equals("")){
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

        char[] chars = path.toCharArray();
        int preLastIndex =  chars.length-2;

        if(chars[preLastIndex] == '/'){
            return true;
        }
        return false;
    }


    public static String getRelativePath(String pathWithoutRoot, Path parentDirectory){
        Path currentDirectory = Paths.get(pathWithoutRoot);
        String currentPath = parentDirectory.relativize(currentDirectory).toString().replace("\\", "/");
        return currentPath;
    }




}
