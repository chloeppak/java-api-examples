# Java Upgrade Playbook: Java 8 → Java 17 (Spring Boot 2.x → 3.1.4)

This playbook records how the sample projects in this repo were moved from Java 8 to Java 17, and gives a checklist for repeating or re-checking the upgrade on the other modules or on other projects.

> **Scope note:** On `main`, only **one** module has been upgraded so far. The fork's own upgrade of three more modules is on branch `upgrade/session-a` in [PR #2](https://github.com/chloeppak/java-api-examples/pull/2), which is still open and has **not** been merged into `main`. The other 11 modules are still on Java 8. See [Summary](#1-summary) and [Gaps](#gaps--unconfirmed-details).

---

## 1. Summary

| | Before | After |
|---|---|---|
| Java (compiler level) | `1.8` | `17` |
| Spring Boot parent (`spring-boot-starter-parent`) | `2.7.x` (2.7.3 / 2.7.4) | `3.1.4` |
| Spring Framework (resolved) | `5.3.x` | `6.0.12` (managed by Boot 3.1.4) |
| OpenAPI Generator Maven plugin | `6.2.0` | `6.6.0` |
| Annotations in the generated client | `javax.annotation.*` | `jakarta.annotation.*` |

### Upgrade commits

| Commit | Date | Author | Where | Modules |
|---|---|---|---|---|
| `7713e87` "Update pom.xml" | 2023-11-30 | rcmasey (upstream UPS) | `main` | `oauth_AuthCode` |
| `0d38056` "Upgrade addressValidation, dangerousGoods, landedCost to Java 17 / Spring Boot 3.1.4" | 2026-10-10 | chloepvarnas (with Devin) | branch `upgrade/session-a`, [PR #2](https://github.com/chloeppak/java-api-examples/pull/2) (open, not on `main`) | `addressValidation`, `dangerousGoods`, `landedCost` |

### Status of each module

Values below come from each module's `pom.xml` on `main`, plus `upgrade/session-a` where noted.

| Module | Java | Spring Boot | Status |
|---|---|---|---|
| `oauth_AuthCode` | 17 | 3.1.4 | Upgraded on `main` (`7713e87`) |
| `addressValidation` | 1.8 → 17 | 2.7.4 → 3.1.4 | Upgraded on `upgrade/session-a` (`0d38056`) |
| `dangerousGoods` | 1.8 → 17 | 2.7.4 → 3.1.4 | Upgraded on `upgrade/session-a` (`0d38056`) |
| `landedCost` | 1.8 → 17 | 2.7.4 → 3.1.4 | Upgraded on `upgrade/session-a` (`0d38056`) |
| `locator` | 1.8 | 2.6.12 | Not upgraded |
| `oauth_ClientCredentials` | 1.8 | 2.7.4 | Not upgraded |
| `paperlessDocuments` | 1.8 | 2.7.4 | Not upgraded |
| `pickup` | 1.8 | 2.7.4 | Not upgraded |
| `preNotification` | 1.8 | 2.6.12 | Not upgraded |
| `pubSub` | 1.8 | 2.5.3 | Not upgraded |
| `quantumView` | 1.8 | 2.6.12 | Not upgraded |
| `rating` | 1.8 | 2.7.4 | Not upgraded |
| `shipping` | 1.8 | 2.7.4 | Not upgraded |
| `timeInTransit` | 1.8 | 2.7.4 | Not upgraded |
| `track` | 1.8 | 2.7.4 | Not upgraded |

---

## 2. Prerequisites

- [ ] **JDK 17** (the new target; Spring Boot 3.x needs Java 17 or later).
- [ ] **Apache Maven 3.6.3 or later.** The verification below used Maven 3.9.9. The `oauth_AuthCode` pom pins `maven-core` 3.9.4 as a plugin dependency.
- [ ] Check what is installed:

  ```sh
  java -version   # expect: openjdk version "17.x"
  mvn -version    # expect: "Java version: 17.x" (Maven must be running on JDK 17)
  ```

- [ ] If `mvn -version` shows a different JDK from `java -version`, set `JAVA_HOME` to the JDK 17 install. Example: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`.

**Version managers / toolchains:** The repo has no `.sdkmanrc`, `.java-version`, `.tool-versions`, `toolchains.xml`, or Maven wrapper (`mvnw`). It also has no CI configuration. You must install the JDK and Maven yourself. If you use SDKMAN!, `sdk install java 17.x.x-tem && sdk use java 17.x.x-tem` works, but nothing in the repo enforces it.

**Repo layout:** There is no root/aggregator `pom.xml`. Every top-level folder is its own self-contained Spring Boot/Maven project, so you upgrade, build, and run each module separately.

---

## 3. What changed

### 3a. `oauth_AuthCode/pom.xml` (commit `7713e87`, on `main`)

This was a large rewrite of the pom (+281 / −58 lines). The commit message is only "Update pom.xml", so the reasons behind the changes are not recorded.

**Java / Boot**
- `spring-boot-starter-parent` `2.7.3` → `3.1.4`
- `<java.version>` `1.8` → `17`
- Added `<maven.compiler.source>17</maven.compiler.source>` and `<maven.compiler.target>17</maven.compiler.target>`
- Added `<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>`

**Dependency / property bumps**
- `snakeyaml.version` `1.33` → `2.0`
- `commons-io.version` `2.9.0` → `2.13.0`
- `plexus-utils.version` `3.0.24` → `4.0.0`
- `tomcat-version` `9.0.70`: commented out, together with the explicit `tomcat-embed-core` dependency that used it. Boot 3 manages Tomcat 10.
- `spring-web` and `spring-context` added **without** a `<version>` (the `${spring-web-version}` reference is commented out), so Boot 3.1.4 manages them. The `spring-web-version` property (`5.3.29`) is still declared but nothing uses it.
- New properties and dependencies added in the same commit: `swagger-annotations` 1.5.2, `jsr305` 3.0.2, `jackson-*` / `jackson-jaxrs-json-provider` `${jackson-databind.version}` (2.14.0), `jackson-databind-nullable` 0.2.3, `jackson-datatype-jsr310`, `jakarta.annotation-api` `${jakarta-annotation-version}` (**1.3.5**, `provided`), `junit` 4.13.2, `lombok`.
- `spring-boot-starter-web` and `spring-boot-starter-test` now exclude `tomcat-embed-websocket`, `tomcat-embed-core`, and `spring-webmvc`. The earlier `spring-boot-starter-webflux`, `reactor-test`, and `tomcat-embed-core` dependencies are commented out.

**Build plugins**
- Added `openapi-generator-maven-plugin` `6.6.0`, with guava `32.1.0-jre`, rhino, handlebars, commons-text, plexus-utils, and snakeyaml as plugin dependencies. It has **no `<executions>`**, so it generates no code in this module.
- `maven-compiler-plugin` pinned to `3.11.0`
- `maven-jar-plugin` `3.2.2` → `3.3.0`. Added `plexus-archiver` 4.8.0, `maven-core` 3.9.4, and `maven-compat` 3.9.4 as plugin dependencies.
- `maven-resources-plugin` `3.3.0`. The old `maven-resources-plugin` block is commented out and redeclared further down.
- `spring-boot-maven-plugin` gained `maven-core` 3.9.4, `jdom2` `${jdom2.version}` (2.0.6.1), `jdependency`, and `jackson-databind` as plugin dependencies.

**Source changes in the same period (2023-11-30):** These commits reworked `oauth_AuthCode/src/main/java/...` (they deleted the `controller/`, `service/`, and `src/test` folders, added `dto/AuthorizeClientResponse.java` and `dto/ErrorResponse.java`, and edited `AuthCodeDemo.java` and `AuthCodeApplication.java`). Nothing in history shows them as *needed* for Java 17. They look like a functional rework done alongside the upgrade.

### 3b. `addressValidation`, `dangerousGoods`, `landedCost` `pom.xml` (commit `0d38056`, branch `upgrade/session-a`, [PR #2](https://github.com/chloeppak/java-api-examples/pull/2))

The same 28-line change was made in all three files: `addressValidation/pom.xml`, `dangerousGoods/pom.xml`, and `landedCost/pom.xml`.

| Element | Before | After |
|---|---|---|
| `spring-boot-starter-parent` `<version>` | `2.7.4` | `3.1.4` |
| `<java.version>` | *(not set)* | `17` (added) |
| `<maven.compiler.source>` / `<maven.compiler.target>` | `1.8` | `17` |
| `<spring-web-version>` | `5.3.18` | **removed** |
| `<spring-framework.version>` | `5.3.18` | **removed** |
| `<jakarta-annotation-version>` | `1.3.5` | `2.1.1` |
| `<snakeyaml.version>` | `1.33` | `2.0` |
| `<commons-io.version>` | `2.9.0` | `2.13.0` |
| `<plexus-utils.version>` | `3.0.24` | `4.0.0` |
| `spring-web` / `spring-context` `<version>` | `${spring-web-version}` | **removed** (Boot-managed → 6.0.12) |
| `openapi-generator-maven-plugin` `<version>` | `6.2.0` | `6.6.0` |
| Each openapi-generator `<execution>` | n/a | added `<configOptions><useJakartaEe>true</useJakartaEe></configOptions>` |
| `maven-compiler-plugin` `<version>` | *(inherited)* | `3.11.0` |

**Source code changes:** None. The hand-written sources in these modules import no `javax.*` packages. The OpenAPI client is generated into `target/generated-sources` at build time and is not committed, so switching the generator to Jakarta EE was enough.

---

## 4. Step-by-step upgrade procedure

Do this for each module (`<module>` = a top-level folder such as `track`). Make it one commit per module or per small group of modules, so each can be rolled back on its own.

1. - [ ] **Record a baseline on the old version.**
   ```sh
   cd <module>
   mvn -B clean test > /tmp/<module>-before.log 2>&1   # tests hit live UPS CIE; failures without creds are expected
   mvn -B clean package -DskipTests
   ```
2. - [ ] **Switch to JDK 17** (see [Prerequisites](#2-prerequisites)) and confirm with `mvn -version`.
3. - [ ] **Bump the Spring Boot parent** in `<module>/pom.xml`:
   ```xml
   <parent>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-parent</artifactId>
     <version>3.1.4</version>
   </parent>
   ```
4. - [ ] **Set the Java level** in `<properties>`:
   ```xml
   <java.version>17</java.version>
   <maven.compiler.source>17</maven.compiler.source>
   <maven.compiler.target>17</maven.compiler.target>
   ```
   The Boot 3.1.4 parent also sets `maven.compiler.release` to `${java.version}`, so `<java.version>` is the setting that controls the bytecode level.
5. - [ ] **Remove the Spring 5 pins.** Delete `<spring-framework.version>` and `<spring-web-version>`, and remove the `<version>` from the `spring-web` and `spring-context` dependencies. **This step is critical:** `spring-framework.version` is the property the Boot parent uses to choose the Spring version, so if you leave it at 5.3.x, Spring 5 stays pinned under Boot 3 without any warning.
6. - [ ] **Bump the supporting versions:**
   - `jakarta-annotation-version` → `2.1.1`
   - `snakeyaml.version` → `2.0`
   - `commons-io.version` → `2.13.0`
   - `plexus-utils.version` → `4.0.0`
7. - [ ] **Update the OpenAPI generator.** Set `openapi-generator-maven-plugin` to `6.6.0`, and add this to **every** `<execution>`'s `<configuration>`:
   ```xml
   <configOptions>
     <useJakartaEe>true</useJakartaEe>
   </configOptions>
   ```
8. - [ ] **Pin the compiler plugin**: `maven-compiler-plugin` `<version>3.11.0</version>`.
9. - [ ] **Fix the code for javax → jakarta and removed APIs.** Search the hand-written sources:
   ```sh
   grep -rn 'javax\.' --include=*.java src
   ```
   Rename `javax.annotation`, `javax.validation`, `javax.servlet`, etc. to their `jakarta.*` equivalents. JDK modules such as `javax.net`, `javax.crypto`, and `javax.xml.parsers` are part of the JDK and stay as they are. None of the upgraded modules needed source edits.
10. - [ ] **Rebuild and verify** (next section). Then compare `mvn -B clean test` with the baseline: the same set of failures (or fewer) means no regression.
11. - [ ] Commit only `<module>/pom.xml` (plus any source fixes), and open a PR.

---

## 5. Build & verification

Run these per module, following `README.md` ("Build and run"):

```sh
cd <module>
mvn clean package                 # README command; runs tests that call live UPS CIE
mvn clean package -DskipTests     # use this without valid OAuth creds in application.properties
java -jar target/<artifactId>-<version>.jar   # e.g. target/addressValidation-1.0-SNAPSHOT.jar
```

Then check the console for the application's REST response, as `README.md` describes. To get a real response, set `api.oauth.partner.client.id` and `api.oauth.partner.secret` in `<module>/src/main/resources/application.properties` and fill in the `<>` placeholders in the request `.json` files.

**Checklist: confirm the build targets Java 17 / Boot 3**
- [ ] `BUILD SUCCESS`
- [ ] The jar manifest shows `Build-Jdk-Spec: 17` and `Spring-Boot-Version: 3.1.4`:
  ```sh
  unzip -p target/*.jar META-INF/MANIFEST.MF | grep -E 'Build-Jdk-Spec|Spring-Boot-Version'
  ```
- [ ] The class file major version is **61** (Java 17), not 52 (Java 8):
  ```sh
  javap -v -cp target/classes <main.Class.Name> | grep 'major version'
  ```
- [ ] Spring Framework resolves to 6.x, not 5.3.x:
  ```sh
  mvn -q dependency:tree -Dincludes=org.springframework:spring-web
  ```
- [ ] The generated client uses Jakarta, with no `javax.annotation` left:
  ```sh
  grep -rhoE '(javax|jakarta)\.annotation\.[A-Za-z]+' target/generated-sources | sort | uniq -c
  ```
- [ ] The startup log says `Running with Spring Boot v3.1.4, Spring v6.0.12` and `using Java 17.x`.

**Reference run** (`addressValidation` on `upgrade/session-a`, OpenJDK 17.0.19, Maven 3.9.9, `-DskipTests`):
- `BUILD SUCCESS`. Manifest: `Build-Jdk-Spec: 17`, `Spring-Boot-Version: 3.1.4`. Class major version `61`. `spring-web:6.0.12`.
- The generated sources use `jakarta.annotation.Generated`, `Nullable`, and `Nonnull`. Before the upgrade, `main` generated `javax.annotation.*`.
- `java -jar target/addressValidation-1.0-SNAPSHOT.jar` started on Spring Boot v3.1.4 / Spring v6.0.12. The OAuth call returned `401 ClientId is Invalid`, which is expected with the placeholder credentials. A successful REST response needs real CIE credentials.
- PR #2 also records `mvn clean test` on JDK 17 before and after the change. The results were identical: addressValidation 9 run / 5 errors, dangerousGoods 7 / 4, landedCost 9 / 2. All errors are live-CIE `401`s or the `dangerousGoods` JSON bug that predates the upgrade.
- For comparison, `addressValidation` on `main` (not upgraded) builds with JDK 17 but produces class major version `52`, with `Spring-Boot-Version: 2.7.4`.

---

## 6. Rollback

A JDK 17 install can still build the Java 8 modules (they compile to `1.8` bytecode), so a rollback only needs pom changes, not a JDK downgrade.

- **Upgrade not merged yet** (for example `upgrade/session-a` / [PR #2](https://github.com/chloeppak/java-api-examples/pull/2)): close the PR or don't merge the branch. `main` is unaffected.
- **Upgrade merged:** revert the upgrade commit and open a PR with the revert:
  ```sh
  git revert <upgrade-sha>        # e.g. 0d38056 (session-a) or 7713e87 (oauth_AuthCode)
  ```
- **Roll back one module only** (when a commit covers several modules):
  ```sh
  git checkout <upgrade-sha>~1 -- <module>/pom.xml
  git commit -m "Roll back <module> to Java 8 / Spring Boot 2.7.x"
  ```
- After rolling back, run `mvn clean package -DskipTests` again. Confirm the manifest shows `Spring-Boot-Version: 2.x` and the class major version is `52`.

> `7713e87` mixes the Java upgrade with a broader rework of the `oauth_AuthCode` pom. Reverting it fully would also undo the plugin and dependency changes listed in §3a.

---

## 7. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| Boot 3 builds, but `dependency:tree` still shows `spring-web:5.3.x`, or you get `NoSuchMethodError` / `ClassNotFoundException` for Spring classes at runtime | `<spring-framework.version>` or `<spring-web-version>` is still in the pom and pins Spring 5 | Delete both properties and remove `<version>` from the `spring-web`/`spring-context` dependencies (Step 5) |
| `package javax.annotation does not exist` (or `javax.annotation.Generated` / `Nullable` not found) in `target/generated-sources` | OpenAPI Generator 6.x emits `javax.*` by default | Add `<useJakartaEe>true</useJakartaEe>` under `<configOptions>` in **every** execution (Step 7) |
| `invalid target release: 17` / `release version 17 not supported` | Maven is running on an older JDK | Point `JAVA_HOME` to JDK 17. `mvn -version` must show Java 17 |
| `UnsupportedClassVersionError ... class file version 61.0` when running the jar | The jar is being run on a JRE older than 17 | Run it with `java` 17 or later |
| Compiler plugin errors or odd `--release` behavior | Old or inherited `maven-compiler-plugin` | Pin `maven-compiler-plugin` `3.11.0` (Step 8) |
| SnakeYAML / plugin classpath conflicts under Boot 3 | SnakeYAML 1.x and old plexus-utils/commons-io are incompatible with Boot 3.1 | Use `snakeyaml.version` 2.0, `plexus-utils.version` 4.0.0, and `commons-io.version` 2.13.0 (Step 6) |
| `javax.servlet` / `javax.validation` / `javax.persistence` not found in hand-written code | Jakarta EE 9+ renamed these namespaces | Rename the imports to `jakarta.*`. Add `jakarta.annotation-api` 2.1.1 if `@PostConstruct`/`@Generated` are used |
| Removed JDK APIs (`javax.xml.bind`/JAXB, `javax.activation`, Nashorn) missing on 17 | These were removed from the JDK in 11+ (Nashorn in 15) | Add them as explicit dependencies (`jakarta.xml.bind-api`, etc.), or replace the code that uses them. None of the upgraded modules use them |
| Tests fail with `401` / `ClientId is Invalid` | The `*AppTest`/`*Tests` classes call live UPS CIE and need real OAuth credentials | Expected without credentials. Use `-DskipTests`, or compare test results before and after (Step 1 vs Step 10) |
| `dangerousGoods` `allJsonFileExist` / `acceptanceauditprecheck` test fails | A bug that predates the upgrade: `PreCheckRequest.json` has an `AcceptanceAuditPreCheckRequest` root wrapper | Not caused by the upgrade. It fails on both Java 8 and Java 17 |
| `oauth_AuthCode` on `main` fails with `cannot find symbol: class Service` in `AuthCodeDemo.java` | Commit `8544454` added `@Service` without `import org.springframework.stereotype.Service;` | Not caused by the Java version. Add the import, or use `@Component`. This module currently does **not** build on `main` |
| Dependency downloads fail with HTTP 429 from Maven Central | Rate limiting on some networks/CI hosts | Configure a mirror in `~/.m2/settings.xml` (for example `https://maven-central.storage-download.googleapis.com/maven2/`) |

---

## Gaps / unconfirmed details

- **Upgrade incomplete on `main`.** Only `oauth_AuthCode` is on Java 17 on `main`. `addressValidation`, `dangerousGoods`, and `landedCost` are upgraded only on the unmerged branch `upgrade/session-a` ([PR #2](https://github.com/chloeppak/java-api-examples/pull/2)). The other 11 modules (see §1) are still on Java 8, and no upgrade branch for them exists in the remote.
- **No stated rationale for `7713e87`.** Its message is "Update pom.xml". The reasons for each pin (for example guava `32.1.0-jre`, `maven-core` 3.9.4, the Tomcat exclusions) are not documented.
- **`oauth_AuthCode` inconsistencies.** Its pom still declares `jakarta-annotation-version` `1.3.5` and the unused `spring-web-version` `5.3.29`. It also does not compile on `main` (see Troubleshooting). Whether it ran correctly on Java 17 after the upgrade cannot be confirmed.
- **No CI or toolchain enforcement.** No pipeline builds the modules on JDK 17, so all verification is manual (§5).
- **End-to-end REST response not verified.** The reference run used placeholder credentials. Getting a successful UPS CIE response needs real OAuth client credentials.
