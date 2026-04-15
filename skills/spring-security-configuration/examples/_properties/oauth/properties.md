# OAuth2 Client Properties

## application.properties

```properties
spring.security.oauth2.client.registration.{prefix}.client-id={clientId}
spring.security.oauth2.client.registration.{prefix}.provider={prefix}
spring.security.oauth2.client.registration.{prefix}.client-secret={clientSecret}
spring.security.oauth2.client.registration.{prefix}.client-name={name}
spring.security.oauth2.client.registration.{prefix}.client-authentication-method={clientAuthMethod}
spring.security.oauth2.client.registration.{prefix}.authorization-grant-type={grantType}
spring.security.oauth2.client.registration.{prefix}.redirect-uri={redirectUri}
spring.security.oauth2.client.registration.{prefix}.scope={scope}
spring.security.oauth2.client.provider.{prefix}.authorization-uri={authorizationUri}
spring.security.oauth2.client.provider.{prefix}.token-uri={tokenUri}
spring.security.oauth2.client.provider.{prefix}.user-info-uri={userInfoUri}
spring.security.oauth2.client.provider.{prefix}.user-info-authentication-method={userInfoAuthMethod}
spring.security.oauth2.client.provider.{prefix}.user-name-attribute={userNameAttribute}
spring.security.oauth2.client.provider.{prefix}.jwk-set-uri={jwkSetUri}
spring.security.oauth2.client.provider.{prefix}.issuer-uri={issuerUri}
```

Note: `client-id`, `provider`, `client-secret`, `client-name` are always written. All other fields are only written if non-empty.

For predefined providers (GOOGLE, GITHUB, FACEBOOK, OKTA), the `provider.*` section is NOT needed -- Spring auto-discovers those.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{prefix}` | derived from provider name (lowercase, non-alphanum replaced with `_`) | — |
| `{clientId}` | user input | — |
| `{clientSecret}` | user input | — |
| `{name}` | user input | provider name |
| `{clientAuthMethod}` | user input | skip if empty |
| `{grantType}` | user input | skip if empty |
| `{redirectUri}` | user input | skip if empty |
| `{scope}` | user input | skip if empty |
| `{authorizationUri}` | user input | skip if empty |
| `{tokenUri}` | user input | skip if empty |
| `{userInfoUri}` | user input | skip if empty |
| `{userInfoAuthMethod}` | user input | skip if empty |
| `{userNameAttribute}` | user input | skip if empty |
| `{jwkSetUri}` | user input | skip if empty |
| `{issuerUri}` | user input | skip if empty |
