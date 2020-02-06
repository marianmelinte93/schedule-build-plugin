package org.jenkinsci.plugins.schedulebuild;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;

import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Descriptor.FormException;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

public class ScheduleBuildAction implements Action, StaplerProxy {

    private static final Logger logger = Logger.getLogger(ScheduleBuildAction.class.getName());

    private final Job<?, ?> target;
    private static final long SECURITY_MARGIN = 120 * 1000L;

    private long quietperiod;

    public ScheduleBuildAction(final Job<?, ?> target) {
        this.target = target;
    }
    
    public Job<?, ?> getOwner() {
        return target;
    }

    @Override
    public String getIconFileName() {
        return target.hasPermission(Item.BUILD) && this.target.isBuildable() ? "/plugin/schedule-build/images/schedule.png" : null;
    }

    @Override
    public String getDisplayName() {
        return target.hasPermission(Item.BUILD) && this.target.isBuildable() ? Messages.ScheduleBuildAction_DisplayName() : null;
    }

    @Override
    public String getUrlName() {
        return "schedule";
    }
    
    public boolean schedule(StaplerRequest req, JSONObject formData) throws FormException {
        return true;
    }

    @Override
    public Object getTarget() {
        target.checkPermission(Item.BUILD);
        return this;
    }

    public String getIconPath() {
        Jenkins instance = Jenkins.get();
        String rootUrl = instance.getRootUrl();

        if (rootUrl != null) {
            return rootUrl + "plugin/schedule-build/";
        }
        throw new IllegalStateException("couldn't load rootUrl");
    }

    public String getDefaultDate() {
        return dateFormat().format(getDefaultDateObject());
    }

    public Date getDefaultDateObject() {
        Date buildtime = new Date();
        int defaultDelay = new ScheduleBuildGlobalConfiguration().getDefaultDelay();
        
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(buildtime);
        gc.set(Calendar.SECOND, 0);
        gc.add(Calendar.MINUTE, defaultDelay);
		buildtime = gc.getTime();
        
        return buildtime;
    }

    public FormValidation doCheckDate(@QueryParameter String date) {
        Date ddate;
        Date now = new Date();
        DateFormat dateFormat = dateFormat();
        try {
            ddate = dateFormat.parse(date);
            now = dateFormat.parse(dateFormat.format(now));
        } catch (ParseException ex) {
            return FormValidation.error(Messages.ScheduleBuildAction_ParsingError());
        }

        if (now.getTime() > ddate.getTime() + ScheduleBuildAction.SECURITY_MARGIN) {
            return FormValidation.error(Messages.ScheduleBuildAction_DateInPastError());
        }

        return FormValidation.ok();
    }

    public long getQuietPeriodInSeconds() {
        return quietperiod / 1000;
    }

    public HttpResponse doNext(StaplerRequest req) throws ServletException {
        JSONObject param = req.getSubmittedForm();
        Date ddate = getDefaultDateObject();
        Date now = new Date();
        DateFormat dateFormat = dateFormat();
        try {
            now = dateFormat.parse(dateFormat.format(now));
        } catch (ParseException e) {
            logger.log(Level.WARNING, "Error while parsing date", e);
        }

        if (param.containsKey("date")) {
            try {
                ddate = dateFormat().parse(param.getString("date"));
            } catch (ParseException ex) {
                return HttpResponses.redirectTo("error");
            }
        }

        quietperiod = ddate.getTime() - now.getTime();
        if (quietperiod + ScheduleBuildAction.SECURITY_MARGIN < 0) { // 120 sec security margin
            return HttpResponses.redirectTo("error");
        }
        return HttpResponses.forwardToView(this, "redirect");
    }

    public boolean isJobParameterized() {
        ParametersDefinitionProperty paramDefinitions = target.getProperty(ParametersDefinitionProperty.class);
        return paramDefinitions != null && paramDefinitions.getParameterDefinitions() != null && paramDefinitions.getParameterDefinitions().isEmpty();
    }

    private DateFormat dateFormat() {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Stapler.getCurrentRequest().getLocale());
        TimeZone tz = new ScheduleBuildGlobalConfiguration().getUserTimeZoneObject();
        df.setTimeZone(tz);
        logger.log(new LogRecord(Level.FINEST, "{dateFormat} tz:[" + tz + "]"));
        return df;
    }
}
