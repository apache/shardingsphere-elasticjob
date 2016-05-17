package com.dangdang.ddframe.job.internal.network;

import com.dangdang.ddframe.job.internal.env.LocalHostService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 *  本机IP地址黑白名单过滤器
 * @author Leon Guo
 */
@Slf4j
public class LocalIPFilter {

    private final LocalHostService localHostService = new LocalHostService();

    private List<IPRange> allowList;

    private List<IPRange> denyList;

    /**
     *  当allow或deny为empty，表示没有白或黑名单
     * @param allow
     * @param deny
     */
    public LocalIPFilter(String allow, String deny) {
        allowList = initIPFilterList(allow);
        denyList = initIPFilterList(deny);
    }

    /**
     *  本机地址是否在白名单
     * @return
     */
    public boolean isAllowed() {
        if (allowList == null || allowList.isEmpty()) {
            //true as default
            return true;
        }
        IPAddress ipAddress = new IPAddress(localHostService.getIp());
        for (IPRange range : allowList) {
            if (range.isIPAddressInRange(ipAddress)) {
                return true;
            }
        }
        // doesn't in the allow list
        return false;
    }

    /**
     *  本机地址是否在黑名单
     * @return
     */
    public boolean isDenied() {
        if (denyList == null || denyList.isEmpty()) {
            //false as default
            return false;
        }
        IPAddress ipAddress = new IPAddress(localHostService.getIp());
        for (IPRange range : denyList) {
            if (range.isIPAddressInRange(ipAddress)) {
                return true;
            }
        }
        // doesn't in the deny list
        return false;
    }

    private List<IPRange> initIPFilterList(String param) {
        List<IPRange> ipRanges = new ArrayList<>();
        try {
            if (param != null && param.trim().length() != 0) {
                param = param.trim();
                String[] items = param.split(",");

                for (String item : items) {
                    if (item == null || item.length() == 0) {
                        continue;
                    }

                    IPRange ipRange = new IPRange(item);
                    ipRanges.add(ipRange);
                }
            }
        } catch (Exception e) {
            log.error("initParameter config error, param : " + param, e);
        }
        return ipRanges;
    }

}
