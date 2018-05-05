package com.example.xukefeng.musicplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.xukefeng.musicplayer.MusicService.MusicPlayerService;
import com.example.xukefeng.musicplayer.PackageClass.MusicInfo;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
     自定义音乐播放器
     1 从存储卡内部读取所有音乐列表，并显示相关音乐信息
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    //用于存储从系统数据库查询的出结果
    private Cursor mCursor ;
    //用于将mCursor的数据导入到List对象中，再作为Adapater参数传入
    private List<Map<String , String>> List_map  ;
    //指定布局文件的ListView
    private ListView MusicListView ;
    //申明ContentResolver对象，用于访问系统数据库
    private ContentResolver contentResolver ;
    //用于装载MusicInfo对象
    private List<MusicInfo> musicInfos ;
    //指定SimpleAdapter对象
    private SimpleAdapter simpleAdapter ;
    //权限申请码requestCode
    private final static int STORGE_REQUEST = 1 ;
    //是否处于播放状态
    private boolean isPlyer = false ;
    //用于启动服务的Intent
    private Intent intent ;

    @Override
    protected void onResume() {
        super.onResume();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MusicListView = (ListView) findViewById(R.id.MusicListView) ;
        //首先检查自身是否已经拥有相关权限，拥有则不再重复申请
        int check = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ;
            //没有相关权限
            if (check != PackageManager.PERMISSION_GRANTED)
            {
                //申请权限
                ActivityCompat.requestPermissions(this , new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE} ,STORGE_REQUEST);
            }else {
            //已有权限的情况下可以直接初始化程序
            init();
        }

        //启动服务
        intent = new Intent() ;
        intent.setClass(MainActivity.this , MusicPlayerService.class) ;
        /*

         */
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent) ;
//        } else {
//            startService(intent) ;
//        }
        startService(intent) ;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //程序退出时，终止服务
        stopService(intent) ;
    }

    /*
        界面列表初始化
         */
    private void init()
    {
        //获取系统的ContentResolver
        contentResolver = getContentResolver() ;

        //从数据库中获取指定列的信息
        mCursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI ,
                new String[] {MediaStore.Audio.Media._ID ,
                        MediaStore.Audio.Media.TITLE , MediaStore.Audio.Media.ALBUM ,
                        MediaStore.Audio.Media.ARTIST , MediaStore.Audio.Media.DURATION ,
                        MediaStore.Audio.Media.DISPLAY_NAME , MediaStore.Audio.Media.SIZE ,
                        MediaStore.Audio.Media.DATA , MediaStore.Audio.Media.ALBUM_ID } , null ,null ,null) ;

        List_map = new ArrayList<Map<String, String>>() ;
        musicInfos = new ArrayList<>() ;
        for (int i = 0 ; i < mCursor.getCount() ; i++)
        {
            Map<String , String> map = new HashMap<>() ;
            MusicInfo musicInfo = new MusicInfo() ;

            //列表移动
            mCursor.moveToNext() ;

            //将数据装载到List<MusicInfo>中
            musicInfo.set_id(mCursor.getInt(0));
            musicInfo.setTitle(mCursor.getString(1));
            musicInfo.setAlbum(mCursor.getString(2));
            musicInfo.setArtist(mCursor.getString(3));
            musicInfo.setDuration(mCursor.getInt(4));
            musicInfo.setMusicName(mCursor.getString(5));
            musicInfo.setSize(mCursor.getInt(6));
            musicInfo.setData(mCursor.getString(7));
              //将数据装载到List<Map<String ,String>>中
            //获取本地音乐专辑图片
            String MusicImage = getAlbumArt(mCursor.getInt(8)) ;
            //判断本地专辑的图片是否为空
            if (MusicImage == null)
            {
                //为空，用默认图片
                map.put("image" , String.valueOf(R.mipmap.timg)) ;
                musicInfo.setAlbum_id(String.valueOf(R.mipmap.timg));
            }else
            {
                //不为空，设定专辑图片为音乐显示的图片
                map.put("image" , MusicImage) ;
                musicInfo.setAlbum_id(MusicImage);
            }
           // musicInfo.setAlbum_id(mCursor.getInt(8));
            musicInfos.add(musicInfo) ;


            map.put("name" , mCursor.getString(5)) ;
            //将获取的音乐大小由Byte转换成mb 并且用float个数的数据表示
            Float size = (float) (mCursor.getInt(6) * 1.0 / 1024 / 1024)  ;
            //对size这个Float对象进行保留两位小数处理
            BigDecimal b  =   new  BigDecimal(size);
            Float   f1   =  b.setScale(2,  BigDecimal.ROUND_HALF_UP).floatValue();
            map.put("size" , f1.toString() + "mb") ;
            List_map.add(map) ;


        }
        //SimpleAdapter实例化
        simpleAdapter = new SimpleAdapter(this , List_map ,R.layout.music_adapte_view ,
                new String[] {"image" , "name" , "size"} ,new int[]{R.id.MusicImage ,
                R.id.MusicName , R.id.MusicSize}) ;
        //为ListView对象指定adapter
        MusicListView.setAdapter(simpleAdapter);
        //绑定item点击事件
        MusicListView.setOnItemClickListener(this);
    }

    /*
        获取本地音乐专辑的图片
     */
    private String getAlbumArt(int album_id)
    {
        String UriAlbum = "content://media/external/audio/albums" ;
        String projecttion[] =  new String[] {"album_art"} ;
        Cursor cursor = contentResolver.query(Uri.parse(UriAlbum + File.separator +Integer.toString(album_id)) ,
                projecttion , null , null , null);
        String album = null ;
        if (cursor.getCount() > 0 && cursor.getColumnCount() > 0)
        {
            cursor.moveToNext() ;
            album = cursor.getString(0) ;
        }
        //关闭资源数据
        cursor.close();
        return album ;
    }
    /*
    申请权限处理结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         switch (requestCode)
         {
             case STORGE_REQUEST :
                 if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                 {
                     //完成程序的初始化
                     init();
                     System.out.println("程序申请权限成功，完成初始化") ;
                 }
                 else {
                     System.out.println("程序没有获得相关权限，请处理");
                 }
                 break ;
         }

    }

    /*
       item点击实现
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String MusicData = musicInfos.get(position).getData();
        System.out.println("THE MUSIC DATA IS " + MusicData);

        //将点击位置传递给播放界面，在播放界面获取相应的音乐信息再播放。
        Bundle bundle = new Bundle() ;
        bundle.putInt("position" , position);
        bundle.putSerializable("musicinfo" , (Serializable) getMusicInfos());
        Intent intent = new Intent() ;
        //绑定需要传递的参数
        intent.putExtras(bundle) ;
        intent.setClass(this ,MusicPlay.class ) ;
        startActivity(intent);
    }
    //播放Activity调用方法来获取MusicMediainfo数据
    public List<MusicInfo> getMusicInfos()
    {
        return musicInfos ;
    }
}
