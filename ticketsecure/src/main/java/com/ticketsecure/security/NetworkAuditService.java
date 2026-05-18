package com.ticketsecure.security;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class NetworkAuditService {
    // Extrai o IP real do cliente, atravessando Proxies e Load Balancers
    public String extractClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        if (ipAddress != null && ipAddress.length() > 15 && ipAddress.contains(",") ) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }

        return ipAddress;
    }

    // Extrai o modelo do navegador e sistema operacional
    public String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return (userAgent != null) ? userAgent : "Unknown";
    }
}
