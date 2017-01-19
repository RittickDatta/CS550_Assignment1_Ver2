import java.io.File;
import java.util.ArrayList;

/**
 * Created by rittick on 1/18/17.
 */
public class HelperFunctions {

    public static ArrayList<String> getFileData(String path) {
        ArrayList<String> fileData = new ArrayList<String>();
        File file = new File(path);
        String[] list = file.list();
        for (int i = 0; i<list.length; i++)
            fileData.add("Path: "+path+"#Filename: "+list[i]+"#Size: "+ list[i].length());

        return fileData;
    }

    public static void main(String[] args) {
        ArrayList<String> fileData = getFileData("Node1/");
        for(String s: fileData){

                System.out.println(s);

        }
    }
}
