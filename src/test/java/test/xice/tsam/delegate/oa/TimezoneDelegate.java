package test.xice.tsam.delegate.oa;

import java.util.TimeZone;

import com.viaoa.util.OADateTime;

public class TimezoneDelegate {

    private static final TimeZone tzLocal = TimeZone.getDefault();
    
    public static OADateTime convert(OADateTime dt, TimeZone tz) {
        if (dt == null || tz == null) return dt;
        
        int offset = tz.getRawOffset();
        
        OADateTime dtx = dt.addMilliSeconds(offset);
        return dtx;
    }
    
    
    
}
