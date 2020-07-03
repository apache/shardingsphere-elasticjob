package org.apache.shardingsphere.elasticjob.lite.console.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request object of uri '/event-trace/status'.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindJobStatusTraceEventsRequest extends BasePageRequest {

    private String jobName;

    private String source;

    private String executionType;

    private String state;

    @JsonProperty("startTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date start;

    @JsonProperty("endTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date end;

    /**
     * Create new FindJobStatusTraceEventsRequest with pageSize and pageNumber.
     * @param pageNumber page number.
     * @param pageSize page size.
     */
    public FindJobStatusTraceEventsRequest(final Integer pageSize, final Integer pageNumber) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }

    /**
     * Create new FindJobStatusTraceEventsRequest with properties.
     * @param pageNumber page number
     * @param pageSize page size
     * @param sortBy the field name sort by
     * @param orderType order type, asc or desc
     * @param startTime start time
     * @param endTime end time
     */
    public FindJobStatusTraceEventsRequest(final Integer pageSize, final Integer pageNumber, final String sortBy,
        final String orderType, final Date startTime, final Date endTime) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.sortBy = sortBy;
        this.orderType = orderType;
        this.start = startTime;
        this.end = endTime;
    }
}
