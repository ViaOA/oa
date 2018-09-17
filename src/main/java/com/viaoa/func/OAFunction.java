package com.viaoa.func;

import com.viaoa.hub.*;
import com.viaoa.jsp.OATemplate;
import com.viaoa.object.*;
import com.viaoa.util.*;


/**
 * OA functions that work from OAObject, Hub and use property paths.
 * @author vvia
 *
 */
public class OAFunction {

    public static int count(OAObject obj, String pp) {
        if (obj == null || OAString.isEmpty(pp)) return 0;
        OAInteger cnt = new OAInteger();
        OAFinder f = new OAFinder(obj, pp) {
            @Override
            protected boolean isUsed(OAObject obj) {
                cnt.add();
                return false;
            }
        };
        f.find();
        return cnt.get();
    }
    public static int count(Hub hub, String pp) {
        if (hub == null || OAString.isEmpty(pp)) return 0;
        OAInteger cnt = new OAInteger();
        OAFinder f = new OAFinder(hub, pp) {
            @Override
            protected boolean isUsed(OAObject obj) {
                cnt.add();
                return false;
            }
        };
        f.find();
        return cnt.get();
    }

    public static double sum(OAObject obj, String pp) {
        if (obj == null || OAString.isEmpty(pp)) return 0;
        String pp1, pp2;
        int x = pp.lastIndexOf('.');
        if (x < 0) {
            pp1 = null;
            pp2 = pp;
        }
        else {
            pp1 = pp.substring(0, x);
            pp2 = pp.substring(x+1);
        }
        return sum(obj, pp1, pp2);
    }
    public static double sum(Hub hub, String pp) {
        if (hub == null || OAString.isEmpty(pp)) return 0;
        String pp1, pp2;
        int x = pp.lastIndexOf('.');
        if (x < 0) {
            pp1 = null;
            pp2 = pp;
        }
        else {
            pp1 = pp.substring(0, x);
            pp2 = pp.substring(x+1);
        }
        return sum(hub, pp1, pp2);
    }
    public static double sum(OAObject obj, String ppToObject, String pp) {
        if (obj == null || OAString.isEmpty(pp)) return 0;
        OADouble sum = new OADouble();
         
        OAFinder f = new OAFinder(obj, ppToObject) {
            @Override
            protected boolean isUsed(OAObject obj) {
                Object val = obj.getProperty(pp);
                if (val != null) {
                    try {
                        double d = OAConv.toDouble(val);
                        sum.add(d);
                    }
                    catch (Exception e) {}
                }
                return false;
            }
        };
        f.find();
        return sum.get();
    }
    public static double sum(Hub hub, String ppToObject, String pp) {
        if (hub == null || OAString.isEmpty(pp)) return 0;
        OADouble sum = new OADouble();
         
        OAFinder f = new OAFinder(hub, ppToObject) {
            @Override
            protected boolean isUsed(OAObject obj) {
                Object val = obj.getProperty(pp);
                if (val != null) {
                    try {
                        double d = OAConv.toDouble(val);
                        sum.add(d);
                    }
                    catch (Exception e) {}
                }
                return false;
            }
        };
        f.find();
        return sum.get();
    }


    public static double max(OAObject obj, String pp) {
        if (obj == null || OAString.isEmpty(pp)) return 0;
        String pp1, pp2;
        int x = pp.lastIndexOf('.');
        if (x < 0) {
            pp1 = null;
            pp2 = pp;
        }
        else {
            pp1 = pp.substring(0, x);
            pp2 = pp.substring(x+1);
        }
        return max(obj, pp1, pp2);
    }
    public static double max(Hub hub, String pp) {
        if (hub == null || OAString.isEmpty(pp)) return 0;
        String pp1, pp2;
        int x = pp.lastIndexOf('.');
        if (x < 0) {
            pp1 = null;
            pp2 = pp;
        }
        else {
            pp1 = pp.substring(0, x);
            pp2 = pp.substring(x+1);
        }
        return max(hub, pp1, pp2);
    }
    public static double max(OAObject obj, String ppToObject, String pp) {
        if (obj == null || OAString.isEmpty(pp)) return 0;
        OADouble max = new OADouble();
         
        OAFinder f = new OAFinder(obj, ppToObject) {
            @Override
            protected boolean isUsed(OAObject obj) {
                Object val = obj.getProperty(pp);
                if (val != null) {
                    try {
                        double d = OAConv.toDouble(val);
                        if (d > max.get()) max.set(d);
                    }
                    catch (Exception e) {}
                }
                return false;
            }
        };
        f.find();
        return max.get();
    }
    public static double max(Hub hub, String ppToObject, String pp) {
        if (hub == null || OAString.isEmpty(pp)) return 0;
        OADouble max = new OADouble();
         
        OAFinder f = new OAFinder(hub, ppToObject) {
            @Override
            protected boolean isUsed(OAObject obj) {
                Object val = obj.getProperty(pp);
                if (val != null) {
                    try {
                        double d = OAConv.toDouble(val);
                        if (d > max.get()) max.set(d);
                    }
                    catch (Exception e) {}
                }
                return false;
            }
        };
        f.find();
        return max.get();
    }


    public static double min(OAObject obj, String pp) {
        if (obj == null || OAString.isEmpty(pp)) return 0;
        String pp1, pp2;
        int x = pp.lastIndexOf('.');
        if (x < 0) {
            pp1 = null;
            pp2 = pp;
        }
        else {
            pp1 = pp.substring(0, x);
            pp2 = pp.substring(x+1);
        }
        return min(obj, pp1, pp2);
    }
    public static double min(Hub hub, String pp) {
        if (hub == null || OAString.isEmpty(pp)) return 0;
        String pp1, pp2;
        int x = pp.lastIndexOf('.');
        if (x < 0) {
            pp1 = null;
            pp2 = pp;
        }
        else {
            pp1 = pp.substring(0, x);
            pp2 = pp.substring(x+1);
        }
        return min(hub, pp1, pp2);
    }
    public static double min(OAObject obj, String ppToObject, String pp) {
        if (obj == null || OAString.isEmpty(pp)) return 0;
        OADouble min = new OADouble();
         
        OAFinder f = new OAFinder(obj, ppToObject) {
            @Override
            protected boolean isUsed(OAObject obj) {
                Object val = obj.getProperty(pp);
                if (val != null) {
                    try {
                        double d = OAConv.toDouble(val);
                        if (d > min.get()) min.set(d);
                    }
                    catch (Exception e) {}
                }
                return false;
            }
        };
        f.find();
        return min.get();
    }
    public static double min(Hub hub, String ppToObject, String pp) {
        if (hub == null || OAString.isEmpty(pp)) return 0;
        OADouble min = new OADouble();
         
        OAFinder f = new OAFinder(hub, ppToObject) {
            @Override
            protected boolean isUsed(OAObject obj) {
                Object val = obj.getProperty(pp);
                if (val != null) {
                    try {
                        double d = OAConv.toDouble(val);
                        if (d > min.get()) min.set(d);
                    }
                    catch (Exception e) {}
                }
                return false;
            }
        };
        f.find();
        return min.get();
    }
    
    public static String template(OAObject obj, String template) {
        if (obj == null || OAString.isEmpty(template)) return null;
        OATemplate temp = new OATemplate();
        temp.setTemplate(template);
        String s = temp.process(obj);
        return s;
    }
    public static String template(Hub hub, String template) {
        if (hub == null || OAString.isEmpty(template)) return null;
        OATemplate temp = new OATemplate();
        temp.setTemplate(template);
        String s = temp.process(hub);
        return s;
    }
    

/** TODO:  function with math parser  qqqqqqqqqqqqqqqqqqqqqqqqqq     
    public static String func(OAObject obj, String equation) {
        return null;
    }
    public static double math(OAObject obj, String equation) {
        return 0.0d;
    }
*/    
    
}

