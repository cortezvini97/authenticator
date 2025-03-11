package com.vcinsidedigital.authenticator.util;

import android.util.Log;
import android.view.View;

import com.vcinsidedigital.authenticator.R;

public class ResourceUtil
{
    public static int getIcon(String issuer){

        if(issuer == null){
            Log.i("ResourceUtil", "Issuer is null");
            return 0;
        }

        if(issuer.equals("Facebook") || issuer.equals("facebook")){
           return R.drawable.facebook;
        }else if(issuer.equals("Instagram") || issuer.equals("instagram")) {
            return R.drawable.instagram;
        }else if(issuer.equals("LinkdIn") || issuer.equals("linkdIn")) {
            return R.drawable.linkdin;
        }else if(issuer.equals("Github") || issuer.equals("github")) {
           return R.drawable.github;
        } else if (issuer.equals("TikTok") || issuer.equals("tiktok")) {
            return R.drawable.tiktok;
        } else {
            return 0;
        }
    }
}
