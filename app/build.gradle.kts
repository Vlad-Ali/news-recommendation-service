plugins {
    application
    pmd
    jacoco
    id("com.gradleup.shadow") version "8.3.2"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.12.1"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:25.0.0")

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")

    testImplementation("org.testcontainers:testcontainers:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.testcontainers:postgresql:1.20.4")


    implementation(libs.guava)

    implementation("com.typesafe:config:1.4.3")

    implementation("org.postgresql:postgresql:42.7.5")
    implementation("org.jdbi:jdbi3-core:3.47.0")
    implementation("org.flywaydb:flyway-core:11.2.0")
    implementation("org.flywaydb:flyway-database-postgresql:11.2.0")

    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("com.sparkjava:spark-template-freemarker:2.7.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")

    implementation("org.jsoup:jsoup:1.18.1")
    implementation("com.rometools:rome:2.1.0")

    implementation("com.microsoft.onnxruntime:onnxruntime:1.20.0")
    implementation("ai.djl.huggingface:tokenizers:0.31.0")

    implementation("org.springframework.boot:spring-boot-starter:3.4.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.4.3")

    implementation("org.springframework.boot:spring-boot-starter-web:3.4.3")

    implementation("org.springframework.boot:spring-boot-starter-security:3.4.3")
    implementation("org.springframework.security:spring-security-test:6.4.3")
    implementation("io.jsonwebtoken:jjwt:0.12.3")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.2")
    implementation("org.hibernate:hibernate-core:6.6.9.Final")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.0")

    implementation("org.telegram:telegrambots-spring-boot-starter:6.7.0")
    implementation("org.telegram:telegrambots:6.5.0")

    implementation("org.springframework.boot:spring-boot-starter-mail")

    testImplementation("org.eclipse.jetty:jetty-server:9.4.44.v20210927")
    testImplementation("org.eclipse.jetty:jetty-servlet:9.4.44.v20210927")
    testImplementation("org.eclipse.jetty:jetty-util:9.4.44.v20210927")
    testImplementation("org.eclipse.jetty:jetty-util-ajax:9.4.44.v20210927")
    testImplementation("org.eclipse.jetty:jetty-io:9.4.44.v20210927")
    testImplementation("org.eclipse.jetty:jetty-security:9.4.44.v20210927")
    testImplementation("org.eclipse.jetty:jetty-webapp:9.4.44.v20210927")
    testImplementation("org.eclipse.jetty:jetty-client:9.4.44.v20210927")
    testImplementation("org.eclipse.jetty:jetty-alpn-client:9.4.44.v20210927")
    testImplementation("org.eclipse.jetty:jetty-xml:9.4.44.v20210927")
    testImplementation("org.eclipse.jetty:jetty-http:9.4.44.v20210927")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = "org.hsse.news.Application"
}

pmd {
    ruleSets = emptyList()
    ruleSetFiles = files("${projectDir}/src/main/resources/pmd/custom-ruleset.xml")
}


tasks.build {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "BUNDLE"

            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = BigDecimal.valueOf(0.01)
            }
        }
    }
}

version = "0.1"
