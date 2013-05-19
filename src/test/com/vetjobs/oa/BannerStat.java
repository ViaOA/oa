package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "bs",
    displayName = "Banner Stat"
)
@OATable(
    indexes = {
        @OAIndex(name = "BannerStatStat", columns = { @OAIndexColumn(name = "StatId") }), 
    }
)
public class BannerStat extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_DisplayCount = "DisplayCount";
    public static final String PROPERTY_HitCount = "HitCount";
     
     
    public static final String PROPERTY_Stat = "Stat";
    public static final String PROPERTY_Banner = "Banner";
     
    protected int id;
    protected int displayCount;
    protected int hitCount;
     
    // Links to other objects.
    protected transient Stat stat;
    protected transient Banner banner;
     
     
    public BannerStat() {
    }
     
    public BannerStat(int id) {
        this();
        setId(id);
    }
    @OAProperty(displayLength = 5)
    @OAId()
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        int old = id;
        this.id = newValue;
        firePropertyChange(PROPERTY_Id, old, this.id);
    }
    
     
    @OAProperty(displayName = "Display Count", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getDisplayCount() {
        return displayCount;
    }
    
    public void setDisplayCount(int newValue) {
        int old = displayCount;
        this.displayCount = newValue;
        firePropertyChange(PROPERTY_DisplayCount, old, this.displayCount);
    }
    
     
    @OAProperty(displayName = "Hit Count", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getHitCount() {
        return hitCount;
    }
    
    public void setHitCount(int newValue) {
        int old = hitCount;
        this.hitCount = newValue;
        firePropertyChange(PROPERTY_HitCount, old, this.hitCount);
    }
    
     
    @OAOne(reverseName = Stat.PROPERTY_BannerStats)
    @OAFkey(columns = {"StatId"})
    public Stat getStat() {
        if (stat == null) {
            stat = (Stat) getObject(PROPERTY_Stat);
        }
        return stat;
    }
    
    public void setStat(Stat newValue) {
        Stat old = this.stat;
        this.stat = newValue;
        firePropertyChange(PROPERTY_Stat, old, this.stat);
    }
    
     
    @OAOne(reverseName = Banner.PROPERTY_BannerStats)
    @OAFkey(columns = {"BannerId"})
    public Banner getBanner() {
        if (banner == null) {
            banner = (Banner) getObject(PROPERTY_Banner);
        }
        return banner;
    }
    
    public void setBanner(Banner newValue) {
        Banner old = this.banner;
        this.banner = newValue;
        firePropertyChange(PROPERTY_Banner, old, this.banner);
    }
    
     
}
 
