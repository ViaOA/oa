// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import com.tmgsc.hifivetest.model.oa.*;
 
public class CardVendorPP {
    private static CardPPx cards;
     

    public static CardPPx cards() {
        if (cards == null) cards = new CardPPx(CardVendor.P_Cards);
        return cards;
    }

    public static String id() {
        String s = CardVendor.P_Id;
        return s;
    }

    public static String created() {
        String s = CardVendor.P_Created;
        return s;
    }

    public static String name() {
        String s = CardVendor.P_Name;
        return s;
    }

    public static String digitalCard() {
        String s = CardVendor.P_DigitalCard;
        return s;
    }

    public static String traditionalCard() {
        String s = CardVendor.P_TraditionalCard;
        return s;
    }

    public static String celebrateCard() {
        String s = CardVendor.P_CelebrateCard;
        return s;
    }
}
 
