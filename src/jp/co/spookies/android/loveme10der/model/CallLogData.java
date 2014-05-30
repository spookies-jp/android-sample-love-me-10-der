package jp.co.spookies.android.loveme10der.model;

public class CallLogData {

    private String type;
    private String cachedName;
    private String number;
    private Integer duration;
    
    public CallLogData() {
        
    }
    
    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
    public void setCachedName(String cachedName) {
        this.cachedName = cachedName;
    }
    public String getCachedName() {
        return this.cachedName;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getNumber() {
        return this.number;
    }
    public void setDuration (Integer duration) {
        this.duration = duration;
    }
    public void addDuration (Integer duration) {
        this.duration += duration;
    }
    public Integer getDuration() {
        return this.duration;
    }
    
}
