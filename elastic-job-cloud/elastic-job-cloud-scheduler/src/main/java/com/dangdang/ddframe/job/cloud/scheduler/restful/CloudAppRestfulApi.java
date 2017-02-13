/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.scheduler.restful;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.dangdang.ddframe.job.cloud.scheduler.config.app.CloudAppConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.app.CloudAppConfigurationGsonFactory;
import com.dangdang.ddframe.job.cloud.scheduler.config.app.CloudAppConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.exception.AppConfigurationException;
import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.common.base.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.lang.StringBuilder;


/**
 * 云作业App的REST API.
 *
 * @author caohao
 */
@Path("/app")
public final class CloudAppRestfulApi {

    private static CoordinatorRegistryCenter regCenter;

    private final CloudAppConfigurationService configService;

    public CloudAppRestfulApi() {
        configService = new CloudAppConfigurationService(regCenter);
    }

    /**
     * 初始化.
     *
     * @param regCenter 注册中心
     */
    public static void init(final CoordinatorRegistryCenter regCenter) {
        CloudAppRestfulApi.regCenter = regCenter;
        GsonFactory.registerTypeAdapter(CloudAppConfiguration.class, new CloudAppConfigurationGsonFactory.CloudAppConfigurationGsonTypeAdapter());
    }

    /**
     * 注册云作业APP配置.
     *
     * @param appConfig 云作业APP配置
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void register(final CloudAppConfiguration appConfig) {
        Optional<CloudAppConfiguration> appConfigFromZk = configService.load(appConfig.getAppName());
        if (appConfigFromZk.isPresent()) {
            throw new AppConfigurationException("app '%s' already existed.", appConfig.getAppName());
        }
        configService.add(appConfig);
    }

    /**
     * 更新云作业App配置.
     *
     * @param appConfig 云作业App配置
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(final CloudAppConfiguration appConfig) {
        configService.update(appConfig);
    }

    /**
     * 查询云作业App配置.
     *
     * @param appName 云作业App配置名称
     * @return 云作业App配置
     */
    @GET
    @Path("/{appName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public CloudAppConfiguration detail(@PathParam("appName") final String appName) {
        Optional<CloudAppConfiguration> config = configService.load(appName);
        if (config.isPresent()) {
            return config.get();
        }
        throw new JobSystemException("Cannot find app '%s', please check the appName.", appName);
    }

    /**
     * 查找全部云作业App配置.
     *
     * @return 全部云作业App配置
     */
    @GET
    @Path("/list")
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<CloudAppConfiguration> findAllApps() {
        return configService.loadAll();
    }

    /**
     * 注销云作业App配置.
     *
     * @param appConfig 云作业App配置
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public void deregister(final String appConfig) {
        configService.remove(appConfig);
    }

    /**
     * 将作业程序包发布到应用仓库(Nexus)
     * @param groupId  程序发布包的所在group
     * @param artifactId  程序发布包的artifact
     * @param version  程序发布包的版本
     * @param packagefileExtension  程序发布包的文件后缀名
     * @param uploadedInputStream
     * @param fileDetail
     * @return
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @FormDataParam("groupId") String groupId,
            @FormDataParam("artifactId") String artifactId,
            @FormDataParam("version") String version,
            @FormDataParam("extension") String packagefileExtension,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        String uploadedFileLocation = "/tmp/" + fileDetail.getFileName();
        try{
            // save the file to tmp directory
            FileUtils.copyInputStreamToFile(uploadedInputStream,new File(uploadedFileLocation));
        }catch(IOException e){
            throw new AppConfigurationException("Cannot receive package file '%s', error : %s",
                    uploadedFileLocation, e.getMessage());
        }
        try{
            // upload to nexus server
            String server = BootstrapEnvironment.getInstance().getRestfulServerConfiguration().getNexus_server();
            String repo = BootstrapEnvironment.getInstance().getRestfulServerConfiguration().getNexus_repo();
            String username = BootstrapEnvironment.getInstance().getRestfulServerConfiguration().getNexus_username();
            String password = BootstrapEnvironment.getInstance().getRestfulServerConfiguration().getNexus_password();
            String url = sendToNexus(uploadedFileLocation,server,repo,groupId,artifactId,version,
                    packagefileExtension,username,password);
            return Response.status(200).entity(url).build();
        }catch(Exception e){
            throw new AppConfigurationException("Cannot send package '%s' to nexus , error : %s",
                    uploadedFileLocation, e.getMessage());
        }finally{
            FileUtils.deleteQuietly(new File(uploadedFileLocation));
        }
    }

    // send file to nexus server
    // for nexus 2.x
    private String sendToNexus(String uploadedFileLocation, String server, String repo, String groupId,
                             String artifactId, String version, String packagefileExtension, String username, String password)
        throws Exception {
        StringBuilder downloadUrl = new StringBuilder();
        downloadUrl.append(server).append("/content/repositories/")
                .append(repo).append("/").append(groupId.replaceAll("\\.", "/")).append("/")
                .append(artifactId).append("/").append(version).append("/")
                .append(artifactId).append("-").append(version).append(".").append(packagefileExtension);
        String target = server + "/service/local/artifact/maven/content";
        URL url = new URL(target);
        HttpHost targetHost = new HttpHost(url.getHost(),url.getPort(),url.getProtocol());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(username, password));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider).build();
        try {
            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);
            // Add AuthCache to the execution context
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);
            // execute
            HttpPost httppost = new HttpPost(target);
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("r", new StringBody(repo, ContentType.DEFAULT_TEXT))
                    .addPart("hasPom", new StringBody("false", ContentType.DEFAULT_TEXT))
                    .addPart("g", new StringBody(groupId, ContentType.DEFAULT_TEXT))
                    .addPart("a", new StringBody(artifactId, ContentType.DEFAULT_TEXT))
                    .addPart("v", new StringBody(version, ContentType.DEFAULT_TEXT))
                    .addPart("p", new StringBody(packagefileExtension, ContentType.DEFAULT_TEXT))
                    .addPart("file", new FileBody(new File(uploadedFileLocation)))
                    .build();
            httppost.setEntity(reqEntity);
            CloseableHttpResponse response = httpclient.execute(targetHost,httppost,localContext);
            try {
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED){
                    return downloadUrl.toString();
                }else{
                    throw new Exception("Create package failed at nexus with status code line : "+response.getStatusLine());
                }
            } finally {
                response.close();
            }
        }catch(Exception e){
            throw e;
        }finally {
            httpclient.close();
        }
    }

}
