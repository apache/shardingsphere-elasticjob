package com.dangdang.ddframe.job.lite.console.restful;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.dangdang.ddframe.job.lite.console.domain.EventTraceDataSourceFactory;
import com.dangdang.ddframe.job.lite.console.domain.EventTraceDataSourceConfiguration;
import com.dangdang.ddframe.job.lite.console.service.impl.EventTraceDataSourceServiceImpl;
import com.dangdang.ddframe.job.lite.console.util.SessionEventTraceDataSourceConfiguration;
import com.google.common.base.Optional;

@Path("/data-source")
public class EventTraceDataSourceRestfulApi {
    
    public static final String DATA_SOURCE_CONFIG_KEY = "data_source_config_key";
    
    private EventTraceDataSourceServiceImpl eventTraceDataSourceService = new EventTraceDataSourceServiceImpl();
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<EventTraceDataSourceConfiguration> load(final @Context HttpServletRequest request) {
        Optional<EventTraceDataSourceConfiguration> eventTraceDataSourceConfig = eventTraceDataSourceService.loadActivated();
        if (eventTraceDataSourceConfig.isPresent()) {
            setDataSourceNameToSession(eventTraceDataSourceConfig.get(), request.getSession());
        }
        return eventTraceDataSourceService.loadAll().getEventTraceDataSourceConfigurations().getEventTraceDataSourceConfiguration();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean add(final EventTraceDataSourceConfiguration config) {
        return eventTraceDataSourceService.add(config);
    }
    
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(final EventTraceDataSourceConfiguration config) {
        eventTraceDataSourceService.delete(config.getName());
    }
    
    @POST
    @Path("/connect")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public boolean connect(final EventTraceDataSourceConfiguration config, final @Context HttpServletRequest request) {
        boolean isConnected = setDataSourceNameToSession(eventTraceDataSourceService.findDataSourceConfiguration(config.getName(), eventTraceDataSourceService.loadAll()), request.getSession());
        if (isConnected) {
            eventTraceDataSourceService.load(config.getName());
        }
        return isConnected;
    }
    
    private boolean setDataSourceNameToSession(final EventTraceDataSourceConfiguration eventTraceDataSourceConfig, final HttpSession session) {
        session.setAttribute(DATA_SOURCE_CONFIG_KEY, eventTraceDataSourceConfig);
        try {
            EventTraceDataSourceFactory.createCoordinatorDataSource(eventTraceDataSourceConfig.getDriver(), eventTraceDataSourceConfig.getUrl(), 
                    eventTraceDataSourceConfig.getUsername(), Optional.fromNullable(eventTraceDataSourceConfig.getPassword()));
            SessionEventTraceDataSourceConfiguration.setDataSourceConfiguration((EventTraceDataSourceConfiguration) session.getAttribute(DATA_SOURCE_CONFIG_KEY));
        } catch (final Exception ex) {
            return false;
        }
        return true;
    }
}
