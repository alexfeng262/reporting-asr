/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asr_utils;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author alexf
 */
public class Directories {
    private static void recursiveDelete(File file) {
       //to end the recursive loop
       if (!file.exists())
           return;

       //if directory, go inside and call recursively
       if (file.isDirectory()) {
           for (File f : file.listFiles()) {
               //call recursively
               recursiveDelete(f);
           }
       }
       //call delete to delete files and empty directory
       file.delete();
       System.out.println("Deleted file/folder: "+file.getAbsolutePath());
    }
    
    public static String[] getAllSpeakers(){
        Resource_manager rm = new Resource_manager();
        
        File dir = new File(rm.getWav_dir_path());
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return new File(f, name).isDirectory();
            }
        };
      
        return dir.list(filter);
    }
    
    public static int createSpeakerDir(String name){
        Resource_manager rm = new Resource_manager();
        
        File dir = new File(rm.getWav_dir_path()+"\\"+name);
    
        if(!dir.isDirectory()){
            boolean created = dir.mkdir();
            if(created)
                return 0; // directory created succesfully
            else
                return 1; // couldn't create directory
        }
        else{
            return 2;  // directory already exists
        }
    }
    
    public static void deleteSpeakerDir(String name){
        Resource_manager rm = new Resource_manager();
        
        File dir = new File(rm.getWav_dir_path()+"\\"+name);
        recursiveDelete(dir);
    }
    
    public static boolean isEmptyDir(String name){
        Resource_manager rm = new Resource_manager();
        
        File dir = new File(rm.getWav_dir_path()+"\\"+name);
        
        String [] files = dir.list();
        
        if(files == null)
            return true;
        
        return false;
    }
    
    public static String[] getAllLm(){
        Resource_manager rm = new Resource_manager();
        
        File dir = new File(rm.getLm_dir_path());
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".lm");
            }
        };
        return dir.list(filter);
    }
    
}
