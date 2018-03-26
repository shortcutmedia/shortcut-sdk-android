package com.scm.reader.model;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * POJO representing a target.
 */
public class Target {
    public static final String KIND_AD = "Ad";
    public static final String KIND_PUBLICATION = "PeriodicalPage";
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ KIND_AD, KIND_PUBLICATION })
    public @interface Kind {}

    public static final int RESULT_PAGE_TARGET = 0;
    public static final int WEB_TARGET = 1;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ RESULT_PAGE_TARGET, WEB_TARGET })
    public @interface ResponseTarget {}


    public String name;
    public String metadata;
    public @Kind String kind;
    public String title;
    public String subtitle;
    public String uuid;
    public @ResponseTarget int responseTarget;
    public String responseContent;
    public String thumbnailUrl;




    public boolean isAd() {
        return KIND_AD.equals(kind);
    }


}
