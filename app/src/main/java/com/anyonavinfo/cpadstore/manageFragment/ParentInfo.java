package com.anyonavinfo.cpadstore.manageFragment;

import com.anyonavinfo.cpadstore.R;

import java.security.PublicKey;
import java.util.ArrayList;

/**
 * Created by zza on 2016/6/7.
 * 存储管理列表的一级菜单信息
 */
public class ParentInfo {

    public String title;
    public String subtitles;
    public static final String ITEM1_PARENTINFO_TITLE="更新管理";
    public static final String ITEM1_PARENTINFO_SUBTITLES="抢先体验新版本";
    public static final String ITEM2_PARENTINFO_TITLE="已安装管理";
    public static final String ITEM2_PARENTINFO_SUBTITLES="快速卸载应用";
//    public int imageID;

    public static ArrayList<ParentInfo> getParentInfos(){
        ArrayList<ParentInfo> parentInfos=new ArrayList<>();
        ParentInfo parentInfo1=new ParentInfo();
        parentInfo1.title=ITEM1_PARENTINFO_TITLE;
        parentInfo1.subtitles=ITEM1_PARENTINFO_SUBTITLES;
//        parentInfo1.imageID= R.drawable.more;
        parentInfos.add(parentInfo1);

        ParentInfo parentInfo2=new ParentInfo();
        parentInfo2.title=ITEM2_PARENTINFO_TITLE;
        parentInfo2.subtitles=ITEM2_PARENTINFO_SUBTITLES;
//        parentInfo2.imageID= R.drawable.more;
        parentInfos.add(parentInfo2);

        return parentInfos;
    }
}
