package org.folio.ed.service;

import static java.util.stream.Collectors.toMap;
import static org.folio.ed.util.Constants.COMMA;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.folio.ed.error.AuthorizationException;
import org.folio.ed.security.SecureStoreFactory;
import org.folio.ed.security.SecureTenantsProducer;
import org.folio.edge.core.security.SecureStore;
import org.folio.edgecommonspring.security.SecurityManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DematicSecurityManagerService {

  public static final String SYSTEM_USER_PARAMETERS_CACHE = "systemUserParameters";

  @Value("${secure_store}")
  private String secureStoreType;

  @Value("${secure_store_props}")
  private String secureStorePropsFile;

  @Value("${staging_director_tenants}")
  private String stagingDirectorTenants;

  @Value("${staging_director_client}")
  private String stagingDirectorClient;
  @Autowired
  private SecurityManagerService sms;
  private static final Pattern isURL = Pattern.compile("(?i)^http[s]?://.*");
  private Map<String, String> stagingDirectorTenantsUserMap = new HashMap<>();

  @PostConstruct
  public void init() {

    Properties secureStoreProps = getProperties(secureStorePropsFile);

    SecureStore secureStore = SecureStoreFactory.getSecureStore(secureStoreType, secureStoreProps);

    Optional<String> tenants = SecureTenantsProducer.getTenants(secureStoreProps, secureStore, stagingDirectorTenants);
    tenants.ifPresent(tenantsStr -> stagingDirectorTenantsUserMap = Arrays.stream(COMMA.split(tenantsStr))
      .collect(toMap(Function.identity(), tenant -> stagingDirectorClient)));
  }

  @Cacheable(value = SYSTEM_USER_PARAMETERS_CACHE, key = "#tenantId")
  public org.folio.edgecommonspring.domain.entity.ConnectionSystemParameters getStagingDirectorConnectionParameters(String tenantId) {
    return sms.getParamsDependingOnCachePresent(stagingDirectorClient, tenantId, stagingDirectorTenantsUserMap.get(tenantId));
  }



  public Set<String> getStagingDirectorTenantsUserMap() {
    return stagingDirectorTenantsUserMap.keySet();
  }

  public Map<String, String> getStagingDirectorTenantsUsers() {
    return stagingDirectorTenantsUserMap;
  }


  private static Properties getProperties(String secureStorePropFile) {
    Properties secureStoreProps = new Properties();

    log.info("Attempt to load properties from: " + secureStorePropFile);

    if (secureStorePropFile != null) {
      URL url = null;
      try {
        if (isURL.matcher(secureStorePropFile).matches()) {
          url = new URL(secureStorePropFile);
        }

        try (
          InputStream in = url == null ? new FileInputStream(secureStorePropFile) : url.openStream()) {
          secureStoreProps.load(in);
          log.info("Successfully loaded properties from: " + secureStorePropFile);
        }
      } catch (Exception e) {
        throw new AuthorizationException("Failed to load secure store properties");
      }
    } else {
      log.warn("No secure store properties file specified. Using defaults");
    }
    return secureStoreProps;
  }
}
