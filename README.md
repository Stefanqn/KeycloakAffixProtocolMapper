# KeycloakAffixProtocolMapper

Prefix / Suffix Protocol Mapper for Keycloak.


## Dev
`kubectl create configmap affix-mapper --namespace keycloak  --from-file=./lib/build/libs/affix-protocol-mapper.jar --dry-run=client -o yaml | kubectl apply -f -`

## Tests
add tests, e.g. with a [keycloak testcontainer](https://github.com/dasniko/testcontainers-keycloak)
