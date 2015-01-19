// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import com.tmgsc.hifivetest.model.oa.*;
 
public class PageInfoPP {
    private static LocationPageInfoPPx locationPageInfos;
    private static PagePPx page;
    private static PageGroupPageInfoPPx pageGroupPageInfos;
    private static PageThemePageInfoPPx pageThemePageInfos;
    private static ProgramPageInfoPPx programPageInfos;
     

    public static LocationPageInfoPPx locationPageInfos() {
        if (locationPageInfos == null) locationPageInfos = new LocationPageInfoPPx(PageInfo.P_LocationPageInfos);
        return locationPageInfos;
    }

    public static PagePPx page() {
        if (page == null) page = new PagePPx(PageInfo.P_Page);
        return page;
    }

    public static PageGroupPageInfoPPx pageGroupPageInfos() {
        if (pageGroupPageInfos == null) pageGroupPageInfos = new PageGroupPageInfoPPx(PageInfo.P_PageGroupPageInfos);
        return pageGroupPageInfos;
    }

    public static PageThemePageInfoPPx pageThemePageInfos() {
        if (pageThemePageInfos == null) pageThemePageInfos = new PageThemePageInfoPPx(PageInfo.P_PageThemePageInfos);
        return pageThemePageInfos;
    }

    public static ProgramPageInfoPPx programPageInfos() {
        if (programPageInfos == null) programPageInfos = new ProgramPageInfoPPx(PageInfo.P_ProgramPageInfos);
        return programPageInfos;
    }

    public static String id() {
        String s = PageInfo.P_Id;
        return s;
    }

    public static String code() {
        String s = PageInfo.P_Code;
        return s;
    }

    public static String description() {
        String s = PageInfo.P_Description;
        return s;
    }

    public static String seq() {
        String s = PageInfo.P_Seq;
        return s;
    }
}
 
