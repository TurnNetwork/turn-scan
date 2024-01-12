package com.turn.browser.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.turn.browser.verify.Errors;
import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSON;

public class RequestUtil {

    private static final String CONTENT_TYPE_URLENCODED = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_TEXT = "text/plain";

    private static final String UTF8 = "UTF-8";
    private static final String GET = "get";

    private static final String UNKOWN = "unknown";
    private static final String LOCAL_IP = "127.0.0.1";
    private static final int IP_LEN = 15;

    public static String getText(HttpServletRequest request) throws Exception {
        return IOUtils.toString(request.getInputStream(), UTF8);
    }

    /**
     * Get json from request. If the submission method is application/x-www-form-urlencoded, it will be assembled into json format.
     *
     * @param request request object
     * @return return json
     * @throws IOException
     */
    public static String getJson(HttpServletRequest request) throws Exception {
        String requestJson = null;
        String contectType = request.getContentType();
        if (contectType == null || "".equals(contectType.trim())) {
            throw Errors.NO_CONTECT_TYPE_SUPPORT.getException(contectType);
        }

        contectType = contectType.toLowerCase();

        if (contectType.contains(CONTENT_TYPE_JSON) || contectType.contains(CONTENT_TYPE_TEXT)) {
            requestJson = getText(request);
        } else if (contectType.contains(CONTENT_TYPE_URLENCODED)) {
            Map<String, Object> params = convertRequestParamsToMap(request);
            requestJson = JSON.toJSONString(params);
        } else {
            throw Errors.NO_CONTECT_TYPE_SUPPORT.getException(contectType);
        }
        return requestJson;
    }

    /**
     * Convert the parameters in request to map
     *
     * @param request request object
     * @return Return parameter key-value pair
     */
    public static Map<String, Object> convertRequestParamsToMap(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        if(paramMap == null || paramMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> retMap = new HashMap<String, Object>(paramMap.size());

        Set<Entry<String, String[]>> entrySet = paramMap.entrySet();

        for (Entry<String, String[]> entry : entrySet) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            if (values.length == 1) {
                retMap.put(name, values[0]);
            } else if (values.length > 1) {
                retMap.put(name, values);
            } else {
                retMap.put(name, "");
            }
        }

        return retMap;
    }

    /**
     * Get the real IP of the client
     *
     * @param request request object
     * @return return ip
     */
    public static String getClientIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || UNKOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || UNKOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || UNKOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (LOCAL_IP.equals(ipAddress)) {
                // Get the IP configured on this machine based on the network card
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    // ignore
                }
            }

        }

        // For the case of multiple proxies, the first IP is the real IP of the client, and multiple IPs are divided according to ','
        if (ipAddress != null && ipAddress.length() > IP_LEN) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;

    }

    /**
     * Whether it is a get request
     * @param request request object
     * @return true, yes
     */
    public static boolean isGetRequest(HttpServletRequest request) {
        return GET.equalsIgnoreCase(request.getMethod());
    }

}

