# KeycloakAffixProtocolMapper

Prefix / Suffix Protocol Mapper for Keycloak.

## Dev
### Github Release
`gh release create v0.0.1 --title "0.0.1" --notes "initial release"`
### Deploy to k8s
`kubectl create configmap affix-mapper --namespace keycloak  --from-file=./lib/build/libs/affix-protocol-mapper.jar --dry-run=client -o yaml | kubectl apply -f -`

## Tests
add tests, e.g. with a [keycloak testcontainer](https://github.com/dasniko/testcontainers-keycloak) and its [kotest extension](https://github.com/kotest/kotest-extensions-testcontainers)
