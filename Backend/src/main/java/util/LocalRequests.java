package util;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.InetAddress;

/** Helper for development-only pages that must be reachable from this machine only. */
public final class LocalRequests {
    private LocalRequests() {
    }

    /** True when the request's remote address is a loopback address (127.0.0.1 or ::1). */
    public static boolean isLocal(HttpServletRequest request) {
        try {
            return InetAddress.getByName(request.getRemoteAddr()).isLoopbackAddress();
        } catch (IOException e) {
            return false;
        }
    }
}
