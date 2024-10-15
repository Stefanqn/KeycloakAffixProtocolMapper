# KeycloakAffixProtocolMapper

Prefix / Suffix Protocol Mapper for Keycloak.

## Dev
### Github Release
`gh release create v0.0.1 --title "0.0.1" --notes "initial release"`
### Deploy to k8s
`kubectl create configmap affix-mapper --namespace keycloak  --from-file=./lib/build/libs/affix-protocol-mapper.jar --dry-run=client -o yaml | kubectl apply -f -`

## Tests
add tests, e.g. with a [keycloak testcontainer](https://github.com/dasniko/testcontainers-keycloak) and its [kotest extension](https://github.com/kotest/kotest-extensions-testcontainers)


## use

In Keycloak kustomize
```yaml

configMapGenerator:
  - name: affix-mapper
    files:
      - https://github.com/Stefanqn/KeycloakAffixProtocolMapper/releases/download/v0.0.1/affix-protocol-mapper-0.0.1.jar # unknown renovate detection
```
in keycloaks instance
```yaml
  unsupported:
    podTemplate:
      spec:
        containers:
          - name: keycloak
            volumeMounts:
              - name: affix-mapper
                mountPath: /opt/keycloak/providers/
                subPath: affix-protocol-mapper.jar
        volumes:
          - name: affix-mapper
            configMap:
              name: affix-mapper
```

```terraform
resource "keycloak_generic_protocol_mapper" "affix_protocol_mapper" {
  realm_id        = keycloak_realm.main.id
  client_scope_id = keycloak_openid_client_scope.nextcloud_client_scope.id
  name            = "Affix(Prefix/ Suffix) Protocol Mapper"
  protocol        = "openid-connect"
  protocol_mapper = "oidc-affix-mapper"
  config = {
    "claim.name"                = "nextcloud_groups"
    "multivalued"               = "true"
    "lowerBound"                = "nextcloud_"
    "upperBound"                = "_group"
    "id.token.claim"            = "true"
    "access.token.claim"        = "true"
    "introspection.token.claim" = "true"
    "lightweight.claim"         = "true"
    "userinfo.token.claim"      = "false"
  }
}
```
