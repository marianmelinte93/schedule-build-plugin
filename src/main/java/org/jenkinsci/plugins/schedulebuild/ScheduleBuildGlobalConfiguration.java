package org.jenkinsci.plugins.schedulebuild;

import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.TimeZoneProperty;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

@Extension
@Symbol("scheduleBuild")
public class ScheduleBuildGlobalConfiguration extends GlobalConfiguration {
	
    protected final transient Logger logger = Logger.getLogger(getClass().getName());
	
    private String timeZone;
    private String userTimeZone;
    private int defaultDelay;

    @DataBoundConstructor
    public ScheduleBuildGlobalConfiguration() {
        this.defaultDelay = 15;
		this.timeZone = TimeZone.getDefault().getID();
        load();
        logger.log(new LogRecord(Level.FINEST, "{constructor} this.defaultDelay:[" + this.defaultDelay + "]"));
        logger.log(new LogRecord(Level.FINEST, "{constructor} this.timeZone:[" + this.timeZone + "]"));
    }
    
    public int getDefaultDelay() {
        return this.defaultDelay;
    }

    @DataBoundSetter
    public void setDefaultDelay(int defaultDelay) {
        this.defaultDelay = defaultDelay;
    }
    
    @DataBoundSetter
    public void setTimeZone(String timeZone) {
        logger.log(new LogRecord(Level.FINEST, "{setTimeZone} timeZone:[" + timeZone + "]"));
        this.timeZone = timeZone;
    }
    
    public String getTimeZone() {
        logger.log(new LogRecord(Level.FINEST, "{getTimeZone} this.timeZone:[" + this.timeZone + "]"));
        return this.timeZone;
    }
    
    public TimeZone getTimeZoneObject() {
        TimeZone tz = TimeZone.getTimeZone(getTimeZone());
        logger.log(new LogRecord(Level.FINEST, "{getTimeZoneObject} tz:[" + tz + "]"));
        return tz;
    }
    
    public String getUserTimeZone() {
        if (this.userTimeZone == null) {
            String tzp = TimeZoneProperty.forCurrentUser();
            logger.log(new LogRecord(Level.FINEST, "{getUserTimeZone} tzp:[" + tzp + "]"));
            if (tzp != null) {
                this.userTimeZone = tzp;
            } else {
                logger.log(new LogRecord(Level.FINE, "User timezone not set. Fallback to default."));
                this.userTimeZone = this.timeZone;
            }
        }
        logger.log(new LogRecord(Level.FINEST, "{getUserTimeZone} this.userTimeZone:[" + this.userTimeZone + "]"));
        return this.userTimeZone;
    }
	
    public TimeZone getUserTimeZoneObject() {
        TimeZone tz = TimeZone.getTimeZone(getUserTimeZone());
        logger.log(new LogRecord(Level.FINEST, "{getUserTimeZoneObject} tz:[" + tz + "]"));
        return tz;
    }
	
    public FormValidation doCheckTimeZone(@QueryParameter String value) {
        TimeZone zone = TimeZone.getTimeZone(value);
        logger.log(new LogRecord(Level.FINEST, "{doCheckTimeZone} zone:[" + zone + "]"));
        if (StringUtils.equals(zone.getID(), value)) {
            return FormValidation.ok();
        } else {
            return FormValidation.error(Messages.ScheduleBuildGlobalConfiguration_TimeZoneError());
        }
    }
    
    public FormValidation doCheckDefaultDelay(@QueryParameter String value) {
        logger.log(new LogRecord(Level.FINEST, "{doCheckDelay} entry"));
        try {
            logger.log(new LogRecord(Level.FINEST, "{doCheckDelay} value:[" + value + "]"));
            Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            logger.log(new LogRecord(Level.FINEST, "{doCheckDelay} ex:[" + ex + "]"));
            return FormValidation.error(Messages.ScheduleBuildGlobalConfiguration_ParsingError());
        }
        return FormValidation.ok();
    }
    
    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        // reset before data-binding
        this.timeZone = null;
        if (json.containsKey("timeZone") && json.containsKey("defaultDelay")) {
            try {
    			this.timeZone = json.getString("timeZone");
    			this.defaultDelay = Integer.parseInt(json.getString("defaultDelay"));
    			logger.log(new LogRecord(Level.FINEST, "{configure} timeZone:[" + timeZone + "]"));
    			logger.log(new LogRecord(Level.FINEST, "{configure} defaultDelay:[" + defaultDelay + "]"));
    			save();
    			return true;
            } catch (NumberFormatException ex) {
            }
        }
        return false;
    }
}
