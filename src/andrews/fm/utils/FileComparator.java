package andrews.fm.utils;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File> {  
    public final int compare(File pFirst, File pSecond) {  

        if(((File) pFirst).isDirectory()&&((File) pSecond).isFile()){
        	return -1;
        }
        else if(((File) pFirst).isFile()&&((File) pSecond).isDirectory()){
        	return 1;
        }
        String name1 = ((File) pFirst).getName();
        String name2 = ((File) pSecond).getName();
        if(name1.compareToIgnoreCase(name2)<0){
        	return -1;
        }
        else
        	return 1;
    }  
}  