package org.folio.ed.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.catalina.connector.RequestFacade;
import org.apache.commons.lang3.StringUtils;
import org.folio.ed.domain.entity.RequestWithHeaders;
import org.folio.ed.error.AuthorizationException;
import org.folio.edge.api.utils.util.ApiKeyHelper;
import org.folio.edge.api.utils.util.ApiKeyHelper.ApiKeySource;
import org.folio.edgecommonspring.security.SecurityManagerService;
import org.folio.spring.integration.XOkapiHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class EdgeSecurityFilter implements Filter {

  public static final String HEALTH_ENDPOINT = "/admin/health";
  public static final String INFO_ENDPOINT = "/admin/info";

  private final SecurityManagerService sms;
  private final ApiKeyHelper apiKeyHelper;
  private final List<ApiKeySource> apiKeySources;

  public EdgeSecurityFilter(SecurityManagerService sms, ApiKeyHelper apiKeyHelper,
                            @Value("${api_key_sources}") String apiKeySources) {
    this.sms = sms;
    this.apiKeyHelper = apiKeyHelper;
    this.apiKeySources = Arrays.stream(apiKeySources.split(","))
      .map(String::trim)
      .map(ApiKeySource::valueOf)
      .toList();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    String path = ((RequestFacade) request).getServletPath();
    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    RequestWithHeaders wrapper = new RequestWithHeaders(httpRequest);

    if (isAuthorizationNeeded(path)) {
      var edgeApiKey = apiKeyHelper.getEdgeApiKey(request, apiKeySources);

      if (StringUtils.isEmpty(edgeApiKey)) {
        throw new AuthorizationException("Edge API key not found in the request");
      }

      var systemUserParameters = sms.getParamsWithToken(edgeApiKey);

      wrapper.putHeader(XOkapiHeaders.TOKEN, systemUserParameters.getOkapiToken().accessToken());
      wrapper.putHeader(XOkapiHeaders.TENANT, systemUserParameters.getTenantId());

    }
    filterChain.doFilter(wrapper, response);
  }

  private boolean isAuthorizationNeeded(String path) {
    return !(path.contains(HEALTH_ENDPOINT) || path.equals(INFO_ENDPOINT));
  }
}
