package com.dangdang.ddframe.job.lite.console.restful;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.dangdang.ddframe.job.lite.console.domain.EventTraceDataSourceFactory;
import com.dangdang.ddframe.job.lite.console.domain.DataSourceConfiguration;
import com.dangdang.ddframe.job.lite.console.service.impl.DataSourceServiceImpl;
import com.dangdang.ddframe.job.lite.console.util.SessionDataSourceConfiguration;
import com.google.common.base.Optional;

@Path("/data_source")
public class DataSourceRestfulApi {
    
    public static final String DATA_SOURCE_CONFIG_KEY = "data_source_config_key";
    
    private DataSourceServiceImpl dataSourceService = new DataSourceServiceImpl();
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<DataSourceConfiguration> load(final @Context HttpServletRequest request) {
        Optional<DataSourceConfiguration> dataSourceConfig = dataSourceService.loadActivated();
        if (dataSourceConfig.isPresent()) {
            setDataSourceNameToSession(dataSourceConfig.get(), request.getSession());
        }
        return dataSourceService.loadAll().getDataSourceConfiguration();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public boolean add(final DataSourceConfiguration config) {
        return dataSourceService.add(config);
    }
    
    @POST
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(final DataSourceConfiguration config) {
        dataSourceService.delete(config.getName());
    }
    
    @POST
    @Path("/connect")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Boolean connect(final DataSourceConfiguration config, final @Context HttpServletRequest request) {
        boolean isConnected = setDataSourceNameToSession(dataSourceService.findDataSourceConfiguration(config.getName(), dataSourceService.loadAll()), request.getSession());
        if (isConnected) {
            dataSourceService.load(config.getName());
        }
        return isConnected;
    }
    
    private boolean setDataSourceNameToSession(final DataSourceConfiguration dataSourceConfig, final HttpSession session) {
        session.setAttribute(DATA_SOURCE_CONFIG_KEY, dataSourceConfig);
        try {
            EventTraceDataSourceFactory.createCoordinatorDataSource(dataSourceConfig.getDriver(), dataSourceConfig.getUrl(), dataSourceConfig.getUsername(), Optional.fromNullable(dataSourceConfig.getPassword()));
            SessionDataSourceConfiguration.setDataSourceConfiguration((DataSourceConfiguration) session.getAttribute(DATA_SOURCE_CONFIG_KEY));
        } catch (final Exception ex) {
            return false;
        }
        return true;
    }
}
