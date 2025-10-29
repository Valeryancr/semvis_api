package gt.skynet.semvis.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class RequestTimingFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestTimingFilter.class);

    private final DataSource dataSource;

    public RequestTimingFilter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                String ip = request.getRemoteAddr();

                Connection connection = null;
                try {
                    connection = DataSourceUtils.getConnection(dataSource);
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute("SET semvis_sk.app_username = '" + username.replace("'", "''") + "'");
                        stmt.execute("SET semvis_sk.app_ip = '" + ip + "'");
                    }
                } catch (Exception ex) {
                    LOG.warn("No se pudo setear app_username en la sesión PostgreSQL", ex);
                } finally {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                }
            }
        } catch (Exception ignored) {
        }

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            String fullUrl = request.getRequestURL().toString();
            if (request.getQueryString() != null) {
                fullUrl += "?" + request.getQueryString();
            }
            int status = response.getStatus();
            LOG.info("Url to \"{}\" in {} ms → {}", fullUrl, duration, status);
        }
    }
}