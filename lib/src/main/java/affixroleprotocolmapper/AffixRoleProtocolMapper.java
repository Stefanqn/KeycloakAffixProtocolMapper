package affixroleprotocolmapper;

import org.keycloak.models.*;
import org.keycloak.protocol.ProtocolMapperConfigException;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.keycloak.utils.RoleResolveUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AffixRoleProtocolMapper extends AbstractOIDCProtocolMapper
  implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

//    private static final Logger logger = LoggerFactory.getLogger(AffixProtocolMapper.class);

  public static final String PROVIDER_ID = "oidc-affix-role-protocol-mapper";
  public static final String DISPLAY_TYPE_TXT = "Affix(Prefix/ Suffix) Protocol Mapper";
  public static final String HELP_TXT = "Filters and removes prefix and suffixes from XYZ and adds result to a token claim.";

  private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  static final String PREFIX = "prefix";
  static final String SUFFIX = "suffix";
  static final String INCLUDE_CLIENT_ROLES = "include-client-roles";
  static final String INCLUDE_REALM_ROLES = "include-realm-roles";

  static final String STRIP_PREFIX = "strip-prefix";
  static final String STRIP_SUFFIX = "strip-suffix";


  static {
    configProperties.add(new ProviderConfigProperty(PREFIX, "Prefix", "prefix, e.g. 'pre_'", ProviderConfigProperty.STRING_TYPE, ""));
    configProperties.add(new ProviderConfigProperty(SUFFIX, "Suffix", "suffix, e.g. '_suf'", ProviderConfigProperty.STRING_TYPE, ""));

    configProperties.add(new ProviderConfigProperty(INCLUDE_CLIENT_ROLES, "include client roles", "if client roles should be included", ProviderConfigProperty.BOOLEAN_TYPE, false));
    configProperties.add(new ProviderConfigProperty(INCLUDE_REALM_ROLES, "include realm roles", "if realm roles should be included", ProviderConfigProperty.BOOLEAN_TYPE, true));

    configProperties.add(new ProviderConfigProperty(STRIP_PREFIX, "strip prefix", "strip prefix if matched", ProviderConfigProperty.BOOLEAN_TYPE, true));
    configProperties.add(new ProviderConfigProperty(STRIP_SUFFIX, "strip suffix", "strip suffix if matched", ProviderConfigProperty.BOOLEAN_TYPE, true));


    configProperties.add(new ProviderConfigProperty(
      ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID,
      ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID_LABEL,
      ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID_HELP_TEXT,
      ProviderConfigProperty.CLIENT_LIST_TYPE, null));
//        configProperties.add(new ProviderConfigProperty(
//                ProtocolMapperUtils.MULTIVALUED,
//                ProtocolMapperUtils.MULTIVALUED_LABEL,
//                ProtocolMapperUtils.MULTIVALUED_HELP_TEXT,
//                ProviderConfigProperty.BOOLEAN_TYPE,
//                Boolean.TRUE.toString()));

    OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
    OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, AffixRoleProtocolMapper.class);
  }

  private static Stream<String> applyAffix(String prefix, String suffix, String role, boolean stripPrefix, boolean stripSuffix) {
    var mappedRole = role;
    if ((prefix == null || prefix.isEmpty()) && (suffix == null || suffix.isEmpty())) {
      return Stream.of(mappedRole);
    }
    if (prefix != null && !prefix.isEmpty()) {
      if (mappedRole.startsWith(prefix)) {
        mappedRole = stripPrefix ? mappedRole.substring(prefix.length()) : mappedRole;
      } else {
        return Stream.empty();
      }
    }
    if (suffix != null && !suffix.isEmpty()) {
      if (mappedRole.endsWith(suffix)) {
        mappedRole = stripSuffix ? mappedRole.substring(0, mappedRole.length() - suffix.length()) : mappedRole;
      } else {
        return Stream.empty();
      }
    }
    return Stream.of(mappedRole);
  }

  @Override
  protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
    mappingModel.getConfig().put(ProtocolMapperUtils.MULTIVALUED, Boolean.TRUE.toString());

    String prefix = mappingModel.getConfig().get(PREFIX);
    String suffix = mappingModel.getConfig().get(SUFFIX);

    String clientId = mappingModel.getConfig().get(ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID);

    Set<String> resolvedRealmRoles = Boolean.parseBoolean(mappingModel.getConfig().get(INCLUDE_REALM_ROLES)) ? getResolvedRealmRoles(keycloakSession, clientSessionCtx) : Set.of();
    Set<String> resolvedClientRoles = Boolean.parseBoolean(mappingModel.getConfig().get(INCLUDE_CLIENT_ROLES)) ? getResolvedClientRoles(keycloakSession, clientSessionCtx, clientId) : Set.of();

    boolean stripPrefix = Boolean.parseBoolean(mappingModel.getConfig().get(STRIP_PREFIX));
    boolean stripSuffix = Boolean.parseBoolean(mappingModel.getConfig().get(STRIP_SUFFIX));

    Set<String> roles = Stream.of(resolvedRealmRoles, resolvedClientRoles)
      .flatMap(Set::stream)
      .flatMap(role -> applyAffix(prefix, suffix, role, stripPrefix, stripSuffix))
      .collect(Collectors.toSet());
//        logger.warn("allResolvedClientRoles " + allResolvedClientRoles);
//        logger.warn("resolvedRealmRoles " + resolvedRealmRoles);
//        logger.warn("resolvedClientRoles " + resolvedClientRoles);
//        logger.warn("setting roles " + roles);
//        logger.warn("mappingModel " + OIDCAttributeMapperHelper.isMultivalued(mappingModel));
    OIDCAttributeMapperHelper.mapClaim(token, mappingModel, roles);
  }

  private static Set<String> getResolvedClientRoles(KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx, String clientId) {
    AccessToken.Access resolvedClientRolesAccess = RoleResolveUtil.getResolvedClientRoles(keycloakSession, clientSessionCtx, clientId, false);
    Set<String> resolvedClientRoles;
    if (resolvedClientRolesAccess != null) {
      resolvedClientRoles = resolvedClientRolesAccess.getRoles();
    } else {
      resolvedClientRoles = Set.of();
    }
    return resolvedClientRoles;
  }

  private static Set<String> getResolvedRealmRoles(KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
    AccessToken.Access resolvedRealmRolesAccess = RoleResolveUtil.getResolvedRealmRoles(keycloakSession, clientSessionCtx, false);
    Set<String> resolvedRealmRoles;
    if (resolvedRealmRolesAccess != null) {
      resolvedRealmRoles = resolvedRealmRolesAccess.getRoles();
    } else {
      resolvedRealmRoles = Set.of();
    }
    return resolvedRealmRoles;
  }

  @Override
  public void validateConfig(KeycloakSession session, RealmModel realm, ProtocolMapperContainerModel client, ProtocolMapperModel mapperModel) throws ProtocolMapperConfigException {

    if(Boolean.parseBoolean(mapperModel.getConfig().get(INCLUDE_CLIENT_ROLES)) && null == mapperModel.getConfig().get(ProtocolMapperUtils.USER_MODEL_CLIENT_ROLE_MAPPING_CLIENT_ID)) {
      throw new ProtocolMapperConfigException("Client ID is required to include client roles","clientIdRequired");
    }
    String prefix = mapperModel.getConfig().get(PREFIX);
    String suffix = mapperModel.getConfig().get(SUFFIX);
    if( (prefix == null || prefix.isEmpty()) && ( suffix == null || suffix.isEmpty()) ){
      throw new ProtocolMapperConfigException("either prefix or suffix must not be empty","preOrSuffixRequired");
    }
    super.validateConfig(session, realm, client, mapperModel);
  }

  // defaults >>>
  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public String getDisplayCategory() {
    return TOKEN_MAPPER_CATEGORY;
  }

  @Override
  public String getDisplayType() {
    return DISPLAY_TYPE_TXT;
  }

  @Override
  public String getHelpText() {
    return HELP_TXT;
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return configProperties;
  }
}
