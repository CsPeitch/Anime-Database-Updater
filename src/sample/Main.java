package sample;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main{

    public static void main(String[] args) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //get current date time with Date()
        Date date = new Date();
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Initiating update: "+dateFormat.format(date));
        /*
        if(!databaseControl.testConnection("CsPeitch","poisodagger9598")) {
            System.out.println("Cannot connect to amazon database");
            return;
        }*/
        //SiteImport.getNums();
        
        /*
        ArrayList<Object> siteimportlist = SiteImport.getAllAnimeData();
        ArrayList<String> templist =(ArrayList<String>) siteimportlist.get(0);
        ArrayList<String> templinks =(ArrayList<String>) siteimportlist.get(1);
        for(String title : templist){
            System.out.println(title);
        }*/
        databaseControl.updateAllDatabases();

    }
}
