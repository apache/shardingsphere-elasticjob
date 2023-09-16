package org.apache.shardingsphere.elasticjob.tracing.metrics.config;

public class MetricConfig {
    private Integer metricsPort = 9090;
    private String metricsPath = "/metrics";

    public Integer getMetricsPort() {
        return metricsPort;
    }

    public void setMetricsPort(Integer metricsPort) {
        this.metricsPort = metricsPort;
    }

    public String getMetricsPath() {
        return metricsPath;
    }

    public void setMetricsPath(String metricsPath) {
        this.metricsPath = metricsPath;
    }
}
