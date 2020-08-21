# Restful Service

## Usage

### Create a RestfulController
```java
@ContextPath("/job")
public class JobController implements RestfulController {
    
    @Mapping(method = Http.POST, path = "/{group}/{jobName}")
    public JobPojo createJob(@Param(name = "group", source = ParamSource.PATH) final String group,
                             @Param(name = "jobName", source = ParamSource.PATH) final String jobName,
                             @Param(name = "cron", source = ParamSource.QUERY) final String cron,
                             @RequestBody String description) {
        JobPojo jobPojo = new JobPojo();
        jobPojo.setName(jobName);
        jobPojo.setCron(cron);
        jobPojo.setGroup(group);
        jobPojo.setDescription(description);
        return jobPojo;
    }

    @Mapping(method = Http.GET, pattern = "/code/204")
    @Returning(code = 204)
    public Object return204() {
        return null;
    }
}
```

### (Optional) Create ExceptionHandler
```java
public class CustomIllegalStateExceptionHandler implements ExceptionHandler<IllegalStateException> {
    @Override
    public ExceptionHandleResult handleException(final IllegalStateException ex) {
        return ExceptionHandleResult.builder()
                .statusCode(403)
                .contentType(Http.DEFAULT_CONTENT_TYPE)
                .result(ResultDto.builder().code(1).data(ex.getLocalizedMessage()).build())
                .build();
    }
}
```

### Configure Restful Service and Start Up
```java
NettyRestfulServiceConfiguration configuration = new NettyRestfulServiceConfiguration(8080);
configuration.addControllerInstance(new JobController());
configuration.addExceptionHandler(IllegalStateException.class, new CustomIllegalStateExceptionHandler());
RestfulService restfulService = new NettyRestfulService(configuration);
restfulService.startup();
```
