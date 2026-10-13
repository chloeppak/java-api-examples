# UPS API Samples
This respository contains example java projects for our most commonly used APIs. 

The projects are self-contained and will run within a main method to transact with the UPS Customer Integration Environment (CIE).

Additionally the projects offer the following features:
- Threadsafe OAuth Access Token Management (Client Credential)
- POJO generation from UPS Open API Specifications
- Modifiable example requests
## Getting Started
### Prerequisites
- You will need to have Maven and the Java Development Kit installed.

### Download
- You can either download a local copy or clone the repository:

```sh
git clone https://github.com/UPS-API/java-api-examples.git
```

### Insert Your Information
- Update your OAuth Client information in <project home>/src/main/resources/application.properties file. These values can be found in the UPS Developer Portal under Apps and your specific application's information.

|Property Name |
| :------: |
|```api.oauth.partner.client.id```|
|```api.oauth.partner.secret```|

- Additionally, modify tags with "<>" in the provided .json files in the resources directory (<project home>/src/main/resources/) with your information. These fields will be listed in the individual API folders.

### Build and run

- Requires JDK 17+ and Maven 3.6.3+. The projects use Spring Boot 3.x.
- The repository root `pom.xml` is a parent/aggregator for all API projects. It centralizes the Java version, Spring Boot version, OpenAPI generator version, and shared dependency/plugin versions.

- Build every project from the repository root:
```sh
mvn clean package
```

- Or build a single project from the root:
```sh
mvn clean package -pl <project home> -am
```

- Or build it from its own directory (the root `pom.xml` must still be present one level up):
```sh
cd <project home>
mvn clean package
```

- Each build writes an executable jar named `<artifactId>-<version>.jar` to `<project home>/target`. Run it from the project directory:
```sh
cd <project home>
java -jar target/<artifactId>-<version>.jar
```
  For example: `cd addressValidation && java -jar target/addressValidation-1.0-SNAPSHOT.jar`
- Check the console for the application's REST response.

## License

Copyright 2022 UPS.

Code licensed under the MIT License: <http://opensource.org/licenses/MIT>

