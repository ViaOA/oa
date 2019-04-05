package test.xice.tsac.delegate.oa;

import java.util.TimeZone;

import com.viaoa.util.*;

import test.xice.tsac.model.oa.*;

public class ScheduleDelegate {

    /**
     * get number of hours to add to the value of a Schedule's, using Site timezone.
     * @return number of hours to add to get silo time.
     */
    public static int getTimezoneOffset(Schedule schedule) {
        if (schedule == null) return 0;

        Silo silo = null;
        Application app = schedule.getApplication();
        if (app != null) {
            Server server = app.getServer();
            if (server != null) silo = server.getSilo();
        }
        if (silo == null) {
            ApplicationGroup appGrp = schedule.getApplicationGroup();
            if (appGrp != null) {
                silo = appGrp.getSilo();
            }
        }
        if (silo == null) return 0;
        
        Environment env = silo.getEnvironment();
        if (env == null) return 0;
        Site site = env.getSite();
        if (site == null) return 0;
        Timezone timezone = site.getTimezone();
        if (timezone == null) return 0;
        
        OADateTime dt = new OADateTime();
        TimeZone tz = dt.getTimeZone();
        int x = tz.getOffset(dt.getTime());  // ms
        x /= (1000 * 60 * 60); // hours
        x = (timezone.getUTCOffset() - x);
        
        return x;
    }
    
    public static boolean isActive(Schedule sch, OADateTime dt) {
        if (sch == null || dt == null) return false;
        
        OADate d1 = sch.getActiveBeginDate();
        if (d1 == null) return true;
        OADate d2 = sch.getActiveEndDate();
        if (d2 == null) return true;

        OADate d = new OADate();
        d1.setYear(d.getYear());
        
        d2.setYear(d.getYear());
        
        if (d.before(d1)) return false;
        if (d.after(d2)) return false;
        
        
        return true;
    }
}
