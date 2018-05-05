package com.example.xukefeng.musicplayer.LrcDealClass;

import com.example.xukefeng.musicplayer.PackageClass.LrcContent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by initializing on 2018/4/26.
 * 处理歌词文件
 */

public class LrcProcess {

    //所有需要处理的歌词对象
    private List<LrcContent> lrcList;
    //一个歌词对象
    private LrcContent mLrcContent;

    /*
    构造函数完成对象的初始化
     */
    public LrcProcess() {
        lrcList = new ArrayList<>();
    }
     /*
    *从内存中读取歌词文件，并转换为String对象输出
    */
     public String readLrc(String path)
     {
         //用StringBuild来存储歌词内容
         StringBuilder sb = new StringBuilder() ;
         //获取歌词文件  因为传入的文件为MusicInfo类中的Data内容，其文件为mp3，需要更换为lrc文件
         File f = new File(path.replace(".mp3" , ".lrc")) ;
         try{
             //通过文件流对象来获取文件内容并且导入歌词内容对象集合中(lrcList)
             FileInputStream inputStream = new FileInputStream(f) ;
             InputStreamReader streamReader = new InputStreamReader(inputStream , "utf-8") ;
             BufferedReader bufferedReader = new BufferedReader(streamReader) ;
             String tempStr = "" ;
             while((tempStr = bufferedReader.readLine()) != null)
             {
                 //实现字符替换
                 tempStr = tempStr.replace("[" , "") ;
                 tempStr = tempStr.replace("]" , "@") ;

                 //根据@分号对文件分离
                 String[] splitData = tempStr.split("@") ;

               //  System.out.println("THE TEMP STR IS " + splitData[0]) ;

                 if (splitData.length > 1)
                 {
                     //新建歌词内容对象
                     mLrcContent = new LrcContent();
                     //设置歌词文本内容
                     mLrcContent.setLrcStr(splitData[1]);
                     //设置歌词时间
                     int lrcTime = timeToStr(splitData[0]) ;
                     mLrcContent.setLrcTime(lrcTime);
                     //添加到列表
                     lrcList.add(mLrcContent);
                   //  System.out.println("录入歌词成功") ;
                 }else {
                    // System.out.println("录入歌词失败") ;

                 }
             }
         }catch (FileNotFoundException e)
         {
             sb.append("木有歌词文件，赶紧去下载！...");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
             sb.append("木有读取到歌词哦！");
         } catch (IOException e) {
             e.printStackTrace();
             sb.append("木有读取到歌词哦！");
         }
         return sb.toString() ;

     }
     /*
     * 对歌词文件lrc中的时间内容进行转码
     * [00:02.32]陈奕迅   时间分别代表分，秒，毫秒
     * [00:03.43]好久不见
      */
     public int timeToStr(String timeStr)
     {
         timeStr = timeStr.replace(":" , ".") ;
         timeStr = timeStr.replace("." , "@") ;
         String []splitTime = timeStr.split("@") ;

         //分离出分， 秒， 毫秒
         int minute = Integer.parseInt(splitTime[0]) ;
         int second = Integer.parseInt(splitTime[1]) ;
         int millisSecond = Integer.parseInt(splitTime[2]) ;
         int time = (minute * 60 + second) * 1000 + millisSecond ;
         return time ;
     }
     /*
     提供一个外界方法歌词对象集合的方法
      */
     public  List<LrcContent> getLrcList()
     {
         return lrcList ;
     }


}
