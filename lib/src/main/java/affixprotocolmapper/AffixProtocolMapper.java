package affixprotocolmapper;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.keycloak.utils.RoleResolveUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AffixProtocolMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

//    private static final Logger logger = LoggerFactory.getLogger(AffixProtocolMapper.class);

    public static final String PROVIDER_ID = "oidc-affix-mapper";
    public static final String DISPLAY_TYPE_TXT = "Affix(Prefix/ Suffix) Protocol Mapper";
    public static final String HELP_TXT = "Filters and removes prefix and suffixes from XYZ and adds result to a token claim.";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static final String PREFIX = "prefix";
    static final String SUFFIX = "suffix";


    static {
        configProperties.add(new ProviderConfigProperty(PREFIX, "Prefix", "prefix, e.g. 'pre_'", ProviderConfigProperty.STRING_TYPE, ""));
        configProperties.add(new ProviderConfigProperty(SUFFIX, "Suffix", "suffix, e.g. '_suf'", ProviderConfigProperty.STRING_TYPE, ""));
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
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, AffixProtocolMapper.class);
    }

    private static Stream<String> applyAffix(String prefix, String suffix, String role) {
        var mappedRole = role;
        if ((prefix == null || prefix.isEmpty()) && (suffix == null || suffix.isEmpty())) {
            return Stream.of(mappedRole);
        }
        if (prefix != null && !prefix.isEmpty()) {
            if (mappedRole.startsWith(prefix)) {
                mappedRole = mappedRole.substring(prefix.length());
            } else {
                return Stream.empty();
            }
        }
        if (suffix != null && !suffix.isEmpty()) {
            if (mappedRole.endsWith(suffix)) {
                mappedRole = mappedRole.substring(0, mappedRole.length() - suffix.length());
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

        Set<String> allResolvedClientRoles = getAllResolvedClientRoles(keycloakSession, clientSessionCtx);
        Set<String> resolvedRealmRoles = getResolvedRealmRoles(keycloakSession, clientSessionCtx);
        Set<String> resolvedClientRoles = getResolvedClientRoles(keycloakSession, clientSessionCtx, clientId);

        Set<String> roles = Stream.of(allResolvedClientRoles, resolvedRealmRoles, resolvedClientRoles)
                .flatMap(Set::stream)
                .flatMap(role -> applyAffix(prefix, suffix, role))
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

    private static Set<String> getAllResolvedClientRoles(KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        return RoleResolveUtil.getAllResolvedClientRoles(keycloakSession, clientSessionCtx)
                .values().stream()
                .flatMap(access -> access.getRoles().stream())
                .collect(Collectors.toSet());
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
