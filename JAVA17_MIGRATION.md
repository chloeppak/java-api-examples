# Java 8 → Java 17 migration summary

All 15 modules target Java 17. `oauth_AuthCode` was already on Java 17 / Spring Boot 3.1.4 upstream. The other 14 modules move to Java 17 but stay on Spring Boot 2.x.

## Discovery

- Each top-level directory is a standalone Maven / Spring Boot module. There's no root pom and no `mvnw`.
- No Gradle, CI workflows, Dockerfiles, toolchains files or `.java-version`/`.sdkmanrc` files, so the only place the Java version is pinned is each module's `pom.xml`.
- A grep of `src/` found no JDK 17 incompatibilities: no `sun.misc`/`Unsafe`, Nashorn, boxing constructors, `Thread.stop`, `SecurityManager`, `javax.xml.bind`, or identifiers named `var`/`yield`/`record`.

## Changes (per module `pom.xml`)

| Setting | Before | After |
|---|---|---|
| `java.version` / compiler level | `maven.compiler.source/target` 1.8 | `<java.version>17</java.version>`, `<release>${java.version}</release>` |
| `maven-compiler-plugin` | inherited from the Boot parent | 3.13.0 |
| `maven-surefire-plugin` | 3.0.0-M7 | 3.2.5 |
| `jacoco-maven-plugin` (where present) | 0.8.7 (Java 17 support was only experimental) | 0.8.12 |
| `spring-boot-starter-parent` | 2.5.3 (pubSub), 2.6.12 (locator, preNotification, quantumView), 2.7.4 (others) | 2.7.18 |
| Spring Framework | pinned to 5.3.18 via `spring-framework.version` / `spring-web-version` | managed by Boot (5.3.31) |
| Lombok | Boot-managed; pubSub got 1.18.20, which predates JDK 17 | Boot-managed (1.18.30) |

Modules not upgraded: `oauth_AuthCode`, which was already on Java 17.

No source changes, extra dependencies, `--add-opens` or `--add-exports` were needed.

## Verification (JDK 17.0.19, Maven 3.9.9)

`mvn -B clean test` ran in every module, first on `main` and then with all four upgrade branches merged. The two runs gave identical results in every module: the same test counts and the same failing tests. Compiled classes are major version 61 (Java 17). There were no illegal-access, `InaccessibleObjectException` or `UnsupportedClassVersionError` errors.

| Module | Compiles | Tests run / errors (same before and after) |
|---|---|---|
| addressValidation | yes | 9 / 5 |
| dangerousGoods | yes | 7 / 4 |
| landedCost | yes | 9 / 2 |
| locator | yes | 6 / 2 |
| paperlessDocuments | yes | 8 / 4 |
| pickup | yes | 9 / 9 |
| preNotification | yes | 6 / 2 |
| pubSub | yes | 10 / 2 |
| quantumView | yes | 7 / 3 |
| shipping | yes | 10 / 4 |
| timeInTransit | yes | 11 / 2 |
| track | yes | 3 / 2 |
| rating | no (fails on `main` too) | – |
| oauth_ClientCredentials | no (fails on `main` too) | – |
| oauth_AuthCode | no (fails on `main` too) | – |

Test errors are expected without credentials. The `*AppTest` / `*Tests` classes call the live UPS CIE endpoints and need real OAuth credentials in `application.properties`, so without them they fail with 401. A packaged jar started with placeholder credentials boots on Java 17 and then gets a 401 from UPS.

## Problems that already exist on `main` (not caused by the upgrade, not fixed here)

- `rating`: `RateDemo` imports `RatedShipmentGuaranteedDelivery`, which `Rate.json` no longer generates.
- `oauth_ClientCredentials`: `ClientCredentialDemo` imports `TokenSuccessResponse`, but the spec generates `GenerateTokenSuccessResponse`.
- `oauth_AuthCode`: `AuthCodeDemo` uses `@Service` without importing it.
- `dangerousGoods`: `PreCheckRequest.json` has an `AcceptanceAuditPreCheckRequest` root wrapper, which breaks `allJsonFileExist` / `acceptanceauditprecheck`.
- `pickup`: the tests can't find the `api.oauth.partner.client.id` property.
- `preNotification`: the pom's main class is `com.ups.dap.PreNotificationApplication`, but the class is in `com.ups.api`, so the packaged jar doesn't start.
- `preNotification`, `quantumView`: `target/` is not gitignored.

## Upgrade PRs (merge in any order; they touch disjoint modules)

- #12: addressValidation, dangerousGoods, landedCost, locator
- #11: oauth_ClientCredentials, paperlessDocuments, pickup, preNotification
- #10: pubSub, quantumView, rating
- #9: shipping, timeInTransit, track
