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

    //TODO возможно потом пригодится этот метод когда я буду делать тоже самое для файлов! Может придется его переделать
    public static String extractFolderName(String objectName, boolean isTrailingSlash){
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

}
