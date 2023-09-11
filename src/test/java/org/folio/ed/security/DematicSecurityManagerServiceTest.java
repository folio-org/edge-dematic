package org.folio.ed.security;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.folio.ed.TestBase;
import org.folio.ed.service.DematicSecurityManagerService;
import org.folio.edge.api.utils.exception.AuthorizationException;
import org.folio.edgecommonspring.domain.entity.ConnectionSystemParameters;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DematicSecurityManagerServiceTest extends TestBase {

  public static final String OKAPI_TOKEN = "AAA-BBB-CCC-DDD";
  public static final String USER_PASSWORD = "password";

  @Autowired
  DematicSecurityManagerService sms;

  @Test
  void testGetTenants() {
    log.info("=== Test: Get tenants ===");
    var tenants = sms.getStagingDirectorTenantsUserMap();
    assertThat(tenants, hasSize(1));
  }

  @Test
  void testGetConnectionSystemParametersByTenant() {
    log.info("=== Test: Get connection system parameters by tenantId ===");

    var connectionSystemParameters = sms.getStagingDirectorConnectionParameters(TEST_TENANT);
    verifyConnectionSystemParameters(connectionSystemParameters);
  }

  @Test
  void testInvalidTenant() {
    log.info("=== Test: Get connection system parameters by invalid tenant ===");
    AuthorizationException exception = assertThrows(AuthorizationException.class,
        () -> sms.getStagingDirectorConnectionParameters("invalid-tenant"));
    assertThat(exception.getMessage(), is("Cannot get system connection properties for user with name: null, for tenant: invalid-tenant"));
  }


  private void verifyConnectionSystemParameters(ConnectionSystemParameters connectionSystemParameters) {
    assertThat(connectionSystemParameters.getTenantId(), is(TEST_TENANT));
    assertThat(connectionSystemParameters.getOkapiToken().accessToken(), is(OKAPI_TOKEN));
  }
}
