package sample;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class SiteImport {
    
    public static ArrayList<Object> getAnimeList(){
        ArrayList<String> animelist = new ArrayList<>();
        ArrayList<String> animelinks = new ArrayList<>();
        
        
        try {
			Document doc = Jsoup.connect("https://www.animefreak.tv/home/anime-list").userAgent("Mozilla/5.0").timeout(120 * 1000).get();
			
			Elements divs = doc.getElementsByClass("container-item");
			for(Element div : divs) {
				if(!div.getElementsByClass("ci-title").first().text().equals("Ongoing Animes")) {
					//System.out.println("~~~~~~~~~~~~~~~~"+div.getElementsByClass("ci-title").first().text());
					Elements links = div.getElementsByTag("a");
					for(Element link : links) {
						//System.out.println(link.text());
	                    //System.out.println(link.absUrl("href"));
	                    animelist.add(link.text());
	                    animelinks.add(link.absUrl("href"));
					}	
				}					
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        ArrayList<Object> fullanimelist = new ArrayList<>();
        fullanimelist.add(animelist);
        fullanimelist.add(animelinks);
        return fullanimelist;
    }
    
    public static ArrayList<String> getAnimeinfoNew(String animetitle,String animelink){
    	ArrayList<String> animeinfo = new ArrayList<>();
    	
    	try {
			Document doc = Jsoup.connect(animelink).userAgent("Mozilla/5.0").timeout(120*1000).get();
			
			String imgurl = doc.getElementsByClass("animeDetail-image").first().getElementsByTag("img").first().absUrl("src");
			//System.out.println(imgurl);
			
			String genre="    ",episodes="    ",type="    ",agerating="    ";
			Elements detaildivs = doc.getElementsByClass("animeDetail-item");
			for(Element detaildiv : detaildivs) {
				//System.out.println(detaildiv.text());
				String detailtext = detaildiv.text();
				
				if(detailtext.contains("Genres")) {
					genre = detailtext.split(" : ")[1].replace(" ", ", ");					
				}else if(detailtext.contains("Status")) {
					episodes = detailtext.split(" : ")[1];
				}else if(detailtext.contains("Type")) {
					type = detailtext.split(" : ")[1];
				}else if(detailtext.contains("Rating")) {
					agerating = detailtext.split(" : ")[1];
				}
				
			}
			String description = doc.getElementsByClass("anime-details").first().text();
			//System.out.println(description);
			
			animeinfo.add("0");
			animeinfo.add(imgurl);
			animeinfo.add(genre);
			animeinfo.add(episodes);
			animeinfo.add(type);
			animeinfo.add(agerating);
			animeinfo.add(description);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    	
    	
    	return animeinfo;
    }

}
