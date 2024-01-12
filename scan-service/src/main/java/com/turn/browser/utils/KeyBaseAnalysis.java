package com.turn.browser.utils;

import com.turn.browser.bean.keybase.Basics;
import com.turn.browser.bean.keybase.KeyBaseUserInfo;
import com.turn.browser.bean.keybase.Them;

import java.util.List;

/**
 * @Description: keyBase parsing tool
 */
public class KeyBaseAnalysis {

    private KeyBaseAnalysis(){}

    public static String getKeyBaseUseName(KeyBaseUserInfo keyBaseUser){
        List<Them> thems = keyBaseUser.getThem();
        if (thems == null || thems.isEmpty()) return null;
        // Get the latest one
        Them them = thems.get(0);
        if(them == null || them.getBasics() == null ) return null;
        Basics basics = them.getBasics();
        return basics.getUsername();
    }


    public static String getKeyBaseIcon( KeyBaseUserInfo keyBaseUser){
        List<Them> thems = keyBaseUser.getThem();
        if (thems == null || thems.isEmpty()) return null;
        // Get the latest one
        Them them = thems.get(0);
        if(them == null || them.getPictures() == null || them.getPictures().getPrimary() == null) return null;
        //Get avatar
        return them.getPictures().getPrimary().getUrl();
    }
}