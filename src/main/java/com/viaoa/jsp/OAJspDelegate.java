package com.viaoa.jsp;

import java.util.HashMap;

import com.viaoa.util.OAString;

public class OAJspDelegate {

    // list of known css files that oajsp components use.
    public static final String CSS_jquery_ui = "jquery-ui";
    public static final String CSS_jquery_ui_basetheme = "jquery-ui-basetheme";
    public static final String CSS_jquery_ui_theme = "jquery-ui-theme";
    public static final String CSS_jquery_timepicker = "jquery-timepicker";
    public static final String CSS_bootstrap_treeview = "bootstrap-treeview";
    public static final String CSS_bootstrap = "bootstrap";
    public static final String CSS_bootstrap_theme = "bootstrap-theme";
    public static final String CSS_bootstrap_datetimepicker = "bootstrap-datetimepicker";
    public static final String CSS_bootstrap_tagsinput = "bootstrap-tagsinput";
    public static final String CSS_bootstrap_typeahead = "bootstrap-typeahead";
    public static final String CSS_bootstrap_select = "bootstrap-select";
    public static final String CSS_bootstrap_ladda = "bootstrap-ladda";
    public static final String CSS_summernote = "summernote";
    public static final String CSS_oajsp = "oajsp";

    /**
     * name/file location for CSS resources.
     * Used by OAJSP components to map requirements
     */
    protected static final HashMap<String, String> hmCssFilePath = new HashMap<>();
    /**
     * name/file location for JS resources.
     * Used by OAJSP components to map requirements
     */
    protected static final HashMap<String, String> hmJsFilePath = new HashMap<>();
    
    
    static {
        registerRequiredCssName(CSS_jquery_ui, "vendor/jquery-ui-1.12.1/jquery-ui.css");
        registerRequiredCssName(CSS_jquery_ui_basetheme, "vendor/jquery-ui-1.12.1/jquery-ui.theme.css");
        registerRequiredCssName(CSS_jquery_ui_theme, "vendor/jquery-ui-themes-1.12.1/themes/base/theme.css");
        registerRequiredCssName(CSS_jquery_timepicker, "vendor/jquery-timepicker/timepicker.css");
        registerRequiredCssName(CSS_bootstrap_treeview, "vendor/bootstrap-treeview/bootstrap-treeview.css");
        registerRequiredCssName(CSS_bootstrap, "vendor/bootstrap-3.3.7/css/bootstrap.css");
        registerRequiredCssName(CSS_bootstrap_theme, "vendor/bootstrap-3.3.7/css/bootstrap-theme.css");
        registerRequiredCssName(CSS_bootstrap_datetimepicker, "vendor/bootstrap-datetimepicker/bootstrap-datetimepicker.css");
        registerRequiredCssName(CSS_bootstrap_tagsinput, "vendor/bootstrap-tagsinput/bootstrap-tagsinput.css");
        registerRequiredCssName(CSS_bootstrap_typeahead, "vendor/bootstrap-typeahead/bootstrap-typeahead.css");
        registerRequiredCssName(CSS_bootstrap_select, "vendor/bootstrap-select/css/bootstrap-select.css");
        registerRequiredCssName(CSS_bootstrap_ladda, "vendor/bootstrap-spinner/ladda-themeless.css");
        registerRequiredCssName(CSS_summernote, "vendor/summernote/summernote.css");
        registerRequiredCssName(CSS_oajsp, "vendor/viaoa/oajsp.css");
    }
    

    // list of known js files that oajsp components use.
    public static final String JS_jquery = "jquery";
    public static final String JS_jquery_ui = "jquery-ui";
    public static final String JS_jquery_maskedinput = "jquery-maskedinput";
    public static final String JS_jquery_timepicker = "jquery-timepicker";
    public static final String JS_bootstrap = "bootstrap";
    public static final String JS_bootstrap_treeview = "bootstrap-treeview";
    public static final String JS_moment = "moment";
    public static final String JS_bootstrap_datetimepicker = "bootstrap-datetimepicker";
    public static final String JS_bootstrap_tagsinput = "bootstrap-tagsinput";
    public static final String JS_bootstrap_typeahead = "bootstrap-typeahead";
    public static final String JS_bootstrap_select = "bootstrap-select";
    public static final String JS_bootstrap_ladda = "bootstrap-ladda";
    public static final String JS_bootstrap_spin = "bootstrap-spin";
    public static final String JS_jquery_slimscroll = "jquery-slimscroll";
    public static final String JS_summernote = "summernote";
    
    static {
        registerRequiredJsName(JS_jquery, "vendor/jquery-3.1.1/jquery.js");
        registerRequiredJsName(JS_jquery_ui, "vendor/jquery-ui-1.12.1/jquery-ui.js");
        registerRequiredJsName(JS_jquery_maskedinput, "vendor/jquery-maskedinput/maskedinput.js");
        registerRequiredJsName(JS_jquery_timepicker, "vendor/jquery-timepicker/timepicker.js");
        registerRequiredJsName(JS_bootstrap, "vendor/bootstrap-3.3.7/js/bootstrap.js");
        registerRequiredJsName(JS_bootstrap_treeview, "vendor/bootstrap-treeview/bootstrap-treeview.js");
        registerRequiredJsName(JS_moment, "vendor/moment/moment.min.js");
        registerRequiredJsName(JS_bootstrap_datetimepicker, "vendor/bootstrap-datetimepicker/bootstrap-datetimepicker.js");
        registerRequiredJsName(JS_bootstrap_tagsinput, "vendor/bootstrap-tagsinput/bootstrap-tagsinput.js");
        registerRequiredJsName(JS_bootstrap_typeahead, "vendor/bootstrap-typeahead/bootstrap-typeahead.js");
        registerRequiredJsName(JS_bootstrap_select, "vendor/bootstrap-select/js/bootstrap-select.js");
        registerRequiredJsName(JS_bootstrap_ladda, "vendor/bootstrap-spinner/ladda.js");
        registerRequiredJsName(JS_bootstrap_spin, "vendor/bootstrap-spinner/spin.js");
        registerRequiredJsName(JS_jquery_slimscroll, "vendor/jquery-slimscroll/jquery.slimscroll.js");
        registerRequiredJsName(JS_summernote, "vendor/summernote/summernote.js");
    }
    
    
    
    
    /**
     * Add a name / filepath of a CSS.
     * @param name lookup name, not case sensitive
     * @param filePath location of file, relative to webcontent
     */
    public static void registerRequiredCssName(String name, String filePath) {
        if (OAString.isEmpty(name)) return;
        hmCssFilePath.put(name.toUpperCase(), filePath);
    }
    public static String getCssFilePath(String name) {
        if (OAString.isEmpty(name)) return null;
        return hmCssFilePath.get(name.toUpperCase());
    }
    

    /**
     * Add a name / filepath of a JS.
     * @param name lookup name, not case sensitive
     * @param filePath location of file, relative to webcontent
     */
    public static void registerRequiredJsName(String name, String filePath) {
        if (OAString.isEmpty(name)) return;
        hmJsFilePath.put(name.toUpperCase(), filePath);
    }
    public static String getJsFilePath(String name) {
        if (OAString.isEmpty(name)) return null;
        return hmJsFilePath.get(name.toUpperCase());
    }

    public static final String LOCATION_Top = "top";
    public static final String LOCATION_Bottom = "bottom";
    public static final String LOCATION_Right = "right";
    public static final String LOCATION_Left = "left";
    
    
}
