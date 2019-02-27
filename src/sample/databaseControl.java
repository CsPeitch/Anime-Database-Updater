package sample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
/**
 * Created by admin on 6/15/2016.
 */
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class databaseControl {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://127.0.0.1/anime";
    static final String online_url = "jdbc:mysql://127.0.0.1/";
    public static String username = "root",password="246538";

    static boolean doChangeVersion = false;

    public static boolean testConnection(String usr,String pass){
        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(online_url, usr, pass);
            con.close();
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void updateAllDatabases(){
        updateAllanimeDatabase("online");
        updateAnimeEpisodes();
        //updateAnimeUltimaDatabase("online",false);
    }

    public static void updateAllanimeDatabase(String updatetype){
        boolean newupdates=false;
        Connection con = null;
        PreparedStatement state = null;
        //site import
        ArrayList<String> tempanimelist;
        ArrayList<String> tempanimelinks;
        ArrayList<String> animelist = new ArrayList<>();
        ArrayList<String> animelinks = new ArrayList<>();
        
        //read database password from file
        File configfile = new File("config.ini");
        
        if(configfile.exists()) {
        	try {
    			BufferedReader br = new BufferedReader(new FileReader(configfile));
    			username = br.readLine();
    			password = br.readLine();
    			br.close();
    			System.out.println(username);
    			System.out.println(password);
    		} catch (FileNotFoundException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }else {
        	try {
				configfile.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(configfile));
				bw.write("root");
				bw.newLine();
				bw.write("246538");
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	System.exit(1);
        }
        
        //System.exit(0);

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //get current date time with Date()
        java.util.Date date = new java.util.Date();
        System.out.println("Starting allanime update: "+dateFormat.format(date));

        ArrayList<Object> siteimportlist = SiteImport.getAnimeList();
        
        /*
        animelist = (ArrayList<String>)siteimportlist.get(0);
        animelinks = (ArrayList<String>)siteimportlist.get(1);
        for(int i=0; i<1; i++) {
        	SiteImport.getAnimeinfoNew(animelist.get(i), animelinks.get(i));
        }
        System.exit(0);*/
        if(updatetype.equals("jsoup")) {
            tempanimelist = (ArrayList<String>) siteimportlist.get(0);
            tempanimelinks = (ArrayList<String>) siteimportlist.get(1);
            System.out.println(tempanimelist.size());
            for(int i=0; i<tempanimelist.size(); i++){
                if(!animelist.contains(tempanimelist.get(i))){
                    animelist.add(tempanimelist.get(i));
                    animelinks.add(tempanimelinks.get(i));
                }
            }
            System.out.println(animelist.size());
            for(String title : animelist){
                System.out.println(title);
            }
            newupdates = true;
        }else{
            animelist=new ArrayList<>();
            animelinks=new ArrayList<>();
            ArrayList<String> templist =(ArrayList<String>) siteimportlist.get(0);
            ArrayList<String> templinks =(ArrayList<String>) siteimportlist.get(1);
            ArrayList<String> dblist=new ArrayList<>();
            int cc =0;
            try{
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection(DB_URL, username, password);

                Statement stt = con.createStatement();
                String command = "select title from allanime";
                ResultSet rs = stt.executeQuery(command);
                while(rs.next()){
                    dblist.add(rs.getString("title"));
                }
                rs.close();


                command = "insert into tempallanime (title) VALUES(?)";
                for (int i = 0; i < templist.size(); i++) {
                    state = con.prepareStatement(command);
                    state.setString(1, templist.get(i));
                    state.executeUpdate();
                }
                templist = new ArrayList<>();
                stt = con.createStatement();
                command = "select title from tempallanime";
                rs = stt.executeQuery(command);
                while(rs.next()){
                    templist.add(rs.getString("title"));
                }
                rs.close();
                stt = con.createStatement();
                command= "truncate table tempallanime";
                stt.execute(command);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    state.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            Collections.sort(dblist);
            for(int i=0; i<templist.size(); i++){
                boolean foundflag = true;
                cc = 0;
                while (foundflag && cc < dblist.size()) {
                    if (templist.get(i).equals(dblist.get(cc))) {
                        foundflag = false;
                    }
                    cc++;
                }
                if (foundflag) {
                    if (!animelist.contains(templist.get(i))) {
                        System.out.println(templist.get(i));
                        animelist.add(templist.get(i));
                        animelinks.add(templinks.get(i));
                    }
                }

            }
            if(animelist.size()!=0){
                newupdates=true;
            }
        }
        if(newupdates) {
            doChangeVersion = true;
            int version = 0;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection(DB_URL, username, password);
                Statement stt;
                String command;

                if(updatetype.equals("jsoup")) {
                    stt = con.createStatement();
                    command = "truncate table allanime";
                    stt.execute(command);
                }


                stt = con.createStatement();
                command = "select version from animedbversion";
                ResultSet rs = stt.executeQuery(command);
                while (rs.next()) {
                    version = rs.getInt("version");
                }
                rs.close();
                version++;
                /*
                stt = con.createStatement();
                command = "update animedbversion set version=" + version;
                stt.execute(command);*/


                command = "insert into allanime (title,links,version) VALUES(?,?,?)";
                for (int i = 0; i < animelist.size(); i++) {
                    state = con.prepareStatement(command);
                    state.setString(1, animelist.get(i));
                    state.setString(2, animelinks.get(i));
                    state.setInt(3, version);
                    state.executeUpdate();
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    state.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            updateAnimeinfoDatabase(animelist, animelinks, updatetype, version);         
        }

    }

    public static void updateAnimeinfoDatabase(ArrayList<String> animelist, ArrayList<String> animelinks, String updatetype, int version){
        Connection con = null;
        PreparedStatement state = null;

        ArrayList<String> title = new ArrayList<>();
        ArrayList<String> imgurl = new ArrayList<>();
        ArrayList<String> genre = new ArrayList<>();
        ArrayList<String> episodes = new ArrayList<>();
        ArrayList<String> animetype = new ArrayList<>();
        ArrayList<String> agerating = new ArrayList<>();
        ArrayList<String> description = new ArrayList<>();

        ArrayList<String> result;
        ArrayList<String> errorlist = new ArrayList<>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //get current date time with Date()
        java.util.Date date = new java.util.Date();
        System.out.println("Starting animeinfo update: "+dateFormat.format(date));

        //Site import
        for(int i=0; i<animelist.size(); i++){
            String animetitle = animelist.get(i);
            result = SiteImport.getAnimeinfoNew(animetitle,animelinks.get(i));
            if(result.get(0).equals("0")) {
                title.add(animetitle);
                imgurl.add(result.get(1));
                genre.add(result.get(2));
                episodes.add(result.get(3));
                animetype.add(result.get(4));
                agerating.add(result.get(5));
                description.add(result.get(6));
            }else {
                errorlist.add(result.get(1));
            }
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, username, password);
            Statement stt;
            String command;

            if(updatetype.equals("jsoup")) {
                stt = con.createStatement();
                command = "truncate table animeinfo";
                stt.execute(command);
            }

            command = "insert into animeinfo (title,imgurl,genre,episodes,animetype,agerating,description,version) VALUES(?,?,?,?,?,?,?,?)";
            for(int i=0; i<imgurl.size(); i++){
                state = con.prepareStatement(command);
                state.setString(1,title.get(i));
                state.setString(2,imgurl.get(i));
                state.setString(3,genre.get(i));
                state.setString(4,episodes.get(i));
                state.setString(5,animetype.get(i));
                state.setString(6,agerating.get(i));
                state.setString(7,description.get(i));
                state.setInt(8,version);
                state.executeUpdate();
            }

            stt = con.createStatement();
            command = "truncate table animeerror";
            stt.execute(command);
            command ="insert into animeerror(title)values(?)";
            for(String anime : errorlist){
                state = con.prepareStatement(command);
                state.setString(1,anime);
                state.executeUpdate();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                state.close();
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateAnimeEpisodes(){
        Connection con = null;
        PreparedStatement state = null;

        ArrayList<String> title = new ArrayList<>();
        ArrayList<String> imgurl = new ArrayList<>();
        ArrayList<String> genre = new ArrayList<>();
        ArrayList<String> episodes = new ArrayList<>();
        ArrayList<String> animetype = new ArrayList<>();
        ArrayList<String> agerating = new ArrayList<>();
        ArrayList<String> description = new ArrayList<>();

        ArrayList<String> result;
        ArrayList<String> errorlist = new ArrayList<>();
        ArrayList<String> animelist = new ArrayList<>();
        ArrayList<String> animelinks = new ArrayList<>();
        ArrayList<String> animeepisodes = new ArrayList<>();
        ArrayList<String> animegenres = new ArrayList<>();
        final ArrayList<String> allGenres = new ArrayList<>();
        allGenres.add("Action");
        allGenres.add("Comedy");
        allGenres.add("Fantasy");
        allGenres.add("Kids");
        allGenres.add("Military");
        allGenres.add("Police");
        allGenres.add("Seinen");
        allGenres.add("Space");
        allGenres.add("Adventure");
        allGenres.add("Demons");
        allGenres.add("Game");
        allGenres.add("Romance");
        allGenres.add("Movie");
        allGenres.add("Psychological");
        allGenres.add("Shoujo");
        allGenres.add("Sports");
        allGenres.add("Animation");
        allGenres.add("Drama");
        allGenres.add("Harem");
        allGenres.add("Magic");
        allGenres.add("Music");
        allGenres.add("Samurai");
        allGenres.add("Shounen");
        allGenres.add("Super Power");
        allGenres.add("Bishounen");
        allGenres.add("Ecchi");
        allGenres.add("Historical");
        allGenres.add("Martial Arts");
        allGenres.add("Mystery");
        allGenres.add("School");
        allGenres.add("Shounen Ai");
        allGenres.add("Supernatural");
        allGenres.add("Cartoon");
        allGenres.add("English");
        allGenres.add("Horror");
        allGenres.add("Mecha");
        allGenres.add("Parody");
        allGenres.add("Sci-Fi");
        allGenres.add("Slice of Life");
        allGenres.add("Vampire");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //get current date time with Date()
        java.util.Date date = new java.util.Date();
        System.out.println("Starting episodes and genres update: "+dateFormat.format(date));
        try{
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, username, password);

            Statement stt = con.createStatement();
            String command = "select Info.title,Allan.links,Info.episodes,Info.genre from animeinfo Info inner join allanime Allan on Info.title=Allan.title";
            ResultSet rs = stt.executeQuery(command);
            while(rs.next()){
                animelist.add(rs.getString("title"));
                animelinks.add(rs.getString("links"));
                animeepisodes.add(rs.getString("episodes"));
                animegenres.add(rs.getString("genre"));
            }
            rs.close();

            System.out.println("eps:"+animeepisodes.size()+"anm:"+animelist.size());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //Site import
        for(int i=0; i<animelist.size(); i++){
            String animetitle = animelist.get(i);
            result = SiteImport.getAnimeinfoNew(animetitle,animelinks.get(i));
            if(result.get(0).equals("0")) {
                title.add(animetitle);
                imgurl.add(result.get(1));
                genre.add(result.get(2));
                episodes.add(result.get(3));
                animetype.add(result.get(4));
                agerating.add(result.get(5));
                description.add(result.get(6));
            }else {
                title.add(animetitle);
                episodes.add("    ");
                genre.add("    ");
                errorlist.add(result.get(1));
            }
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, username, password);
            Statement stt;
            String command;

            System.out.println("Site import episodes list size: "+episodes.size() + "     Database episodes list size: "+ animeepisodes.size());
            if(episodes.size()==animeepisodes.size())
                System.out.println("koble");
            else{
                System.out.println("skatoules");
                System.exit(1);
            }
            boolean foundevenone = false;

            int version = 0;
            stt = con.createStatement();
            command = "select version from animedbversion";
            ResultSet rs = stt.executeQuery(command);
            while (rs.next()) {
                version = rs.getInt("version");
            }
            rs.close();
            version++;

            for(int i=0; i<episodes.size(); i++){
                if(animeepisodes.get(i).contains("Ongoing")&&!animeepisodes.get(i).equals(episodes.get(i))&&!episodes.get(i).equals("    ")){
                    command = "update animeinfo set episodes=?,version=? where title=?";
                    state = con.prepareStatement(command);
                    state.setString(1,episodes.get(i));
                    state.setInt(2, version);
                    state.setString(3,title.get(i));
                    state.executeUpdate();
                    foundevenone=true;
                    System.out.println("Updated episodes for: "+animelist.get(i));
                }
                if(!animegenres.get(i).equals(genre.get(i))&&!genre.get(i).equals("    ")) {
                    boolean isGenreLegit = true;
                    String[] genresplit = genre.get(i).split(", ");
                    for(int j=0; i<genresplit.length; i++) {
                    	if (!allGenres.contains(genresplit[j]))
                            isGenreLegit = false;
                    }
                   
                    if(isGenreLegit){
                        command = "update animeinfo set genre=?,version=? where title=?";
                        state = con.prepareStatement(command);
                        state.setString(1,genre.get(i));
                        state.setInt(2, version);
                        state.setString(3,title.get(i));
                        state.executeUpdate();
                        foundevenone=true;
                        System.out.println("Updated genre for: "+animelist.get(i));
                    }
                }

            }
            date = new java.util.Date();
            if(foundevenone || doChangeVersion){
                if(!foundevenone)
                    System.out.println("All episodes and genres are up to date");
                System.out.println("Update finished changing version to: "+version+ " Date: "+dateFormat.format(date));
                stt = con.createStatement();
                command = "update animedbversion set version=" + version;
                stt.execute(command);
            }else{
                System.out.println("Update finished everything is up to date / Date: "+dateFormat.format(date));
            }
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println();
            stt = con.createStatement();
            command = "truncate table animeerror";
            stt.execute(command);
            command ="insert into animeerror(title)values(?)";
            state = con.prepareStatement(command);
            for(String anime : errorlist){
                state = con.prepareStatement(command);
                state.setString(1,anime);
                state.executeUpdate();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                state.close();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
