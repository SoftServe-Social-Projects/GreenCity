  # Helm Chart for Greencity

  ## Prerequisites

  - Kubernetes cluster 1.20+
  - Helm v3.2.0+
  - External Secrets Operator [https://external-secrets.io]
  - Reloader [https://github.com/stakater/Reloader]

  ## Installation

  ### In project folder
  ```bash
  helm install my-release greencity-chart
  ```

  ## Uninstallation

  To uninstall/delete the `my-release` deployment:
  ```bash
  helm uninstall my-release
  ```

  ### Configure the chart

  The following items can be set via `--set` flag during installation or configured by editing the `values.yaml` directly.
  ### If you want to use local secret you can skip step External Secret
  ### External Secret 
  To connect Secret Store to azure key vault(Or others providers see https://external-secrets.io/v0.5.7/) you need Managed Identity authentication. Also you can use others methods (see https://external-secrets.io/v0.5.7/provider-azure-key-vault/). 

  ### Managed Identity authentication Secret
  Here you can see example of secret to Secret store 
  ```yml
  apiVersion: v1
  kind: Secret
  metadata:
    name: azure-secret-sp
  type: Opaque
  data:
    ClientID: bXktc2VydmljZS1wcmluY2lwbGUtY2xpZW50LWlkCg==  #service-principal-ID
    ClientSecret: bXktc2VydmljZS1wcmluY2lwbGUtY2xpZW50LXNlY3JldAo= #service-principal-secret
  ```
  ### Secret Store 
  ```yml
  apiVersion: external-secrets.io/v1beta1
  kind: SecretStore
  metadata:
    name: example-secret-store
  spec:
    provider:
      # provider type: azure keyvault
      azurekv:
        # azure tenant ID, see: https://docs.microsoft.com/en-us/azure/active-directory/fundamentals/active-directory-how-to-find-tenant
        tenantId: "d3bc2180-xxxx-xxxx-xxxx-154105743342"
        # URL of your vault instance, see: https://docs.microsoft.com/en-us/azure/key-vault/general/about-keys-secrets-certificates
        vaultUrl: "https://my-keyvault-name.vault.azure.net"
        authSecretRef:
          # points to the secret that contains
          # the azure service principal credentials
          clientId:
            name: azure-secret-sp
            key: ClientID
          clientSecret:
            name: azure-secret-sp
            key: ClientSecret
  ```

  ### List of environment variables you need

| Name | Value |
| ------ | ------ |
| API_KEY | "your_API_KEY" |
| API_SECRET | "your_API_SECRET" |
| APPLICATIONINSIGHTS_CONNECTION_STRING | "InstrumentationKey=KEY;IngestionEndpoint=Endpoint" |
| AZURE_CONNECTION_STRING | "DefaultEndpointsProtocol=https;AccountName=your_account_name;AccountKey=your_account_key;EndpointSuffix=core.windows.net" |
| AZURE_CONTAINER_NAME | "your_AZURE_CONTAINER_NAME" |
| BUCKET_NAME |  |
| CACHE_SPEC |  |
| CLIENT_ADDRESS |  |
| DATABASE_PASSWORD |  |
| DATABASE_USER |  |
| DIALECT | "org.hibernate.dialect.PostgreSQLDialect" |
| DRIVER | "org.postgresql.Driver" |
| ECO_NEWS_ADDRESS |  |
| FACEBOOK_APP_ID |  |
| FACEBOOK_APP_SECRET |  |
| GREENCITYUSER_SERVER_ADDRESS |  |
| HIBERNATE_CONFIG |  |
| JAWSDB_URL |  |
| JAWSDB_URL |  |
| LIQUIBASE_ENABLE |  |
| LIQUIBASE_LOG |  |
| LOG_EXCEPTION_HANDLER |  |
| LOG_FILE |  |
| LOG_LEVEL_ROOT |  |
| LOG_PATH |  |
| LOG_PATTERN |  |
| MAIL_HOST |  |
| MAIL_PASSWORD |  |
| MAIL_PORT |  |
| MAIL_USER |  |
| POOL_SIZE |  |
| SHOW_SQL |  |
| SMTP_AUTH |  |
| SMTP_ENABLE |  |
| SPRING_PROFILES_ACTIVE |  |
| TOKEN_ACCESS_TIME |  |
| TOKEN_KEY |  |
| TOKEN_REFRESH_TIME |  |
| VERIFY_EMAIL |  |
| GOOGLE_API_KEY |  |
