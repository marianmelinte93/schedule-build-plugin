package org.jenkinsci.plugins.schedulebuild;

import hudson.Extension;
import hudson.model.TimeZoneProperty;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

@Extension
@Symbol("scheduleBuild")
public class ScheduleBuildGlobalConfiguration extends GlobalConfiguration {
	
    protected final Logger logger = Logger.getLogger(getClass().getName());
	
    //private Date defaultScheduleTime;
    private String timeZone;

    @DataBoundConstructor
    public ScheduleBuildGlobalConfiguration() {
        load();
        //this.defaultScheduleTime = new Date(0, 0, 0, 22, 0);
	
        String tzp = TimeZoneProperty.forCurrentUser();
        logger.log(new LogRecord(Level.SEVERE, "{constructor} tzp:[" + tzp + "]"));
        if (tzp != null) {
            this.timeZone = tzp;
        } else {
            this.timeZone = TimeZone.getDefault().getID();
        }
        logger.log(new LogRecord(Level.SEVERE, "{constructor} this.timeZone:[" + this.timeZone + "]"));
    }

    /*public String getDefaultScheduleTime() {
        return getTimeFormat().format(this.defaultScheduleTime);
    }*/

    /*@DataBoundSetter
    public void setDefaultScheduleTime(String defaultScheduleTime) throws ParseException {
        this.defaultScheduleTime = getTimeFormat().parse(defaultScheduleTime);
    }*/

    public String getTimeZone() {
        logger.log(new LogRecord(Level.SEVERE, "{getTimeZone} this.timeZone:[" + this.timeZone + "]"));
        return this.timeZone;
    }

    @DataBoundSetter
    public void setTimeZone(String timeZone) {
        logger.log(new LogRecord(Level.SEVERE, "{setTimeZone} timeZone:[" + timeZone + "]"));
        this.timeZone = timeZone;
    }

    public TimeZone getTimeZoneObject() {
        String timeZone = getTimeZone();
        logger.log(new LogRecord(Level.SEVERE, "{getTimeZoneObject} timeZone:[" + timeZone + "]"));
		TimeZone tz = TimeZone.getTimeZone(timeZone);
        logger.log(new LogRecord(Level.SEVERE, "{getTimeZoneObject} tz:[" + tz + "]"));
        return tz;
    }

    private DateFormat getTimeFormat() {
        logger.log(new LogRecord(Level.SEVERE, "{getTimeFormat} Stapler.getCurrentRequest().getLocale():[" + Stapler.getCurrentRequest().getLocale() + "]"));
        return DateFormat.getTimeInstance(DateFormat.MEDIUM, Stapler.getCurrentRequest().getLocale());
    }

    /*public Date getDefaultScheduleTimeObject() {
        return new Date(this.defaultScheduleTime.getTime());
    }*/

    /*public FormValidation doCheckDefaultScheduleTime(@QueryParameter String value) {
        try {
            logger.log(new LogRecord(Level.SEVERE, "{doCheckDefaultScheduleTime} value:[" + value + "]"));
            getTimeFormat().parse(value);
        } catch (ParseException ex) {
            return FormValidation.error(Messages.ScheduleBuildGlobalConfiguration_ParsingError());
        }
        return FormValidation.ok();
    }*/

    public FormValidation doCheckTimeZone(@QueryParameter String value) {
        TimeZone zone = TimeZone.getTimeZone(value);
        logger.log(new LogRecord(Level.SEVERE, "{doCheckTimeZone} zone:[" + zone + "]"));
        if(StringUtils.equals(zone.getID(), value)) {
            return FormValidation.ok();
        }else {
            return FormValidation.error(Messages.ScheduleBuildGlobalConfiguration_TimeZoneError());
        }
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        // reset before data-binding
        //this.defaultScheduleTime = null;
        this.timeZone = null;
        if (/*json.containsKey("defaultScheduleTime") &&*/ json.containsKey("timeZone")) {
            //try {
                //this.defaultScheduleTime = getTimeFormat().parse(json.getString("defaultScheduleTime"));
                this.timeZone = json.getString("timeZone");
                logger.log(new LogRecord(Level.SEVERE, "{configure} timeZone:[" + timeZone + "]"));
                save();
                return true;
            /*} catch (ParseException ex) {
            }*/
        }
        return false;
    }
}
