/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nasaprogram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 *
 * @author
 */
public class NasaData{
    
    HashMap<String, Integer> ipmap=new HashMap(); //store ip and its acess time
    HashMap<String, Integer> remap=new HashMap(); //store resources and is total bandwith
    int tolerence=20;
    HashMap<String, Integer> hmap=new HashMap(); //store the access times in 20s
    ArrayList<String> hips=new ArrayList(); //failed access ip in time order
    ArrayList<Calendar> htimes=new ArrayList(); //failed acess time
    
    //set to store result
    ArrayList<String> topip=new ArrayList();
    ArrayList<Integer> topact=new ArrayList();
    ArrayList<String> topre=new ArrayList();
    ArrayList<Integer> topbd=new ArrayList();
    ArrayList<Calendar> toptm=new ArrayList();
    ArrayList<Integer> toptmact=new ArrayList();
    ArrayList<String> hacker=new ArrayList();
    ArrayList<Calendar> htime=new ArrayList();
    
//    static String path=System.getProperty("user.dir");
    
    

    
    public void Read(String path){
        
        for(int i=0; i<10;i++){
            Calendar temp=new GregorianCalendar();
            temp.set(0, 0, 0, 0, 0, 0);
            toptm.add(temp);
            toptmact.add(0);
        }
        
        Calendar time=new GregorianCalendar();
        time.set(0, 0, 0, 0, 0, 0);
        int act=0; 
        
        
        
        BufferedReader crunchifyBuffer = null;
        try {
            String crunchifyLine;
            //crunchifyBuffer = new BufferedReader(new FileReader("C:\\Users\\ \\Downloads\\log.txt"));
            crunchifyBuffer = new BufferedReader(new FileReader(path+"\\log.txt"));
            while ((crunchifyLine = crunchifyBuffer.readLine()) != null) {
//                System.out.println("Raw CSV data: " + crunchifyLine);
                String[] temp= crunchifyLine.split("\"|- -");
                if(temp.length>=5){
                    temp[3]=temp[temp.length-1]+" 0";
                }
                String ip=(temp[0].trim());
                String re=(temp[2].trim());
                
                String[] temp1=temp[3].trim().split(" ");
                
                int sd=0;
                try{
                    sd=Integer.valueOf(temp1[0]);
                }catch (Exception e) {
                  //  System.out.println(crunchifyLine + temp1[0]);
                }
                
                int bd=0;
                try{
                    bd=Integer.valueOf(temp1[1]);
                }catch (Exception e) {
                  //  System.out.println(crunchifyLine + temp1[1]);
                }
                
                
                String[] temp2=temp[1].trim().substring(1, 20).split("/|:");
                int d=(Integer.valueOf(temp2[0]));
                int m=((findMonth(temp2[1])));
                int y=(Integer.valueOf(temp2[2]));
                int h=(Integer.valueOf(temp2[3]));
                int mi=(Integer.valueOf(temp2[4]));
                int s=(Integer.valueOf(temp2[5]));
                Calendar t=new GregorianCalendar();
                t.set(y, m, d, h, mi, s);
 
                
                //for top ip
                if (ipmap.containsKey(ip)) {
                    ipmap.put(ip, (int) ipmap.get(ip) + 1);
                } else {
                    ipmap.put(ip, 1);
                }
                //for top re
                if (remap.containsKey(re)) {
                    remap.put(re, (Integer) remap.get(re) + bd);
                } else {
                    remap.put(re, bd);
                }
                //for top hour
                //is a new time period
                if(t.get(Calendar.HOUR_OF_DAY)!=time.get(Calendar.HOUR_OF_DAY)){
                    //check weather new period is a top period
                    for(int j=0; j<10;j++){
                        if(act>toptmact.get(j)){
                            toptmact.add(j,act);
                            toptmact.remove(10);
                            toptm.add(j,time);
                            toptm.remove(10);
                            break;
                        }
                    }
                    act=1;
                    time=new GregorianCalendar();
                    time.set(t.get(Calendar.YEAR),t.get(Calendar.MONTH),t.get(Calendar.DATE),t.get(Calendar.HOUR_OF_DAY),0,0);
                }else{
                    //still the same hour peoriod
                    act++;
                }
                
                //for find hacker
                if(sd!=200){//deal with failed access
                //remove access older than 20 s
                    for(int j=0;j<htimes.size();j++){
                        if((htimes.get(0).getTimeInMillis()-t.getTimeInMillis())>(tolerence*1000)){
                            String oldip=hips.get(0);
                            int count=hmap.get(oldip);
                            hips.remove(0);
                            htimes.remove(0);
                            if(count>1){
                                hmap.put(oldip, count-1);
                            }else{
                                hmap.remove(oldip);
                            }
                        }else{
                            break;
                        }
                    }
                   //add new failed to list
                    hips.add(ip);
                    htimes.add(t);
                    if(hmap.containsKey(ip)){
                        int count=hmap.get(ip);
                        count=count+1;
                        hmap.put(ip, count);
                        if(count>=3){
                            hacker.add(ip);
                            htime.add(t);
                        }
                    }else{
                        hmap.put(ip, 1);
                    }
                }else{
                    //reset when secess login
                    if(hmap.containsKey(ip)){
                    hmap.remove(ip);}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (crunchifyBuffer != null) crunchifyBuffer.close();
            } catch (IOException crunchifyException) {
                crunchifyException.printStackTrace();
            }
        }
        this.findTopIp();
        this.findTopRe();
    }
    
    public void Save(String path){
        
        try {
            PrintWriter pw = new PrintWriter(new File(path+"\\"+"hosts.txt"));
            for(int i=0;i<topip.size();i++){
                pw.append(i+" "+topip.get(i)+" "+topact.get(i)+System.lineSeparator());
            }
            pw.close();
            pw = new PrintWriter(new File(path+"\\"+"resources.txt"));
            for(int i=0;i<topre.size();i++){
                pw.append(i+" "+topre.get(i)+" "+topbd.get(i)+System.lineSeparator());
            }
            pw.close();
            pw = new PrintWriter(new File(path+"\\"+"hours.txt"));
            for(int i=0;i<toptm.size();i++){
                pw.append(i+" "+toptm.get(i).getTime()+" "+toptmact.get(i)+System.lineSeparator());
            }
            pw.close();
            pw = new PrintWriter(new File(path+"\\"+"blocked.txt"));
            for(int i=0;i<hacker.size();i++){
                pw.append(hacker.get(i)+"  "+htime.get(i).getTime()+System.lineSeparator());
            }
            pw.close();
            System.out.printf("Output 4 files to:"+path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
         
    }
    
    public int findMonth(String st){
        int m;
        switch(st){
            case "Jan":
                m=1;
                break;
            case "Feb":
                m=2;
                break;
            case "Mar":
                m=3;
                break;
            case "Apr":
                m=4;
                break;
            case "May":
                m=5;
                break;
            case "Jun":
                m=6;
                break;
            case "Jul":
                m=7;
                break;
            case "Aug":
                m=8;
                break;
            case "Sep":
                m=9;
                break;
            case "Oct":
                m=10;
                break;
            case "Nov":
                m=11;
                break;
            case "Dec":
                m=12;
                break;
            default:
                m=13;
        }
        return m;
    }
    public void findTopRe(){
        
        topre.clear();
        topbd.clear();

        //initial
        for(int i=0; i<10;i++){
            topre.add("NA");
            topbd.add(0);
        }
        //find top 10
        for(String re:remap.keySet()){
            Integer v=remap.get(re);
            for(int i=0; i<10;i++){
                if(v>topbd.get(i)){
                    topbd.add(i, v);
                    topbd.remove(10);
                    topre.add(i,re);
                    topre.remove(10);
                    break;
                }
            }
        }
    }
    public void findTopIp(){
        
        topip.clear();
        topact.clear();

        //initial
        for(int i=0; i<10;i++){
            topip.add("default");
            topact.add(0);
        }
        //find top 10
        for(String ip:ipmap.keySet()){
            Integer v=ipmap.get(ip);
            for(int i=0; i<10;i++){
                if(v>topact.get(i)){
                    topact.add(i, v);
                    topact.remove(10);
                    topip.add(i,ip);
                    topip.remove(10);
                    break;
                }
            }
        }
    }
    
    public static void main(String args[]) {
       NasaData ndata=new NasaData();
       System.out.print("Input File:"+args[0]+"\\log.txt"+"\n");
       ndata.Read(args[0]);
       ndata.Save(args[1]);
     }

}