package com.sssukho.testcontainer.mysql;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public interface MySQLContainerBaseTest {

    String MYSQL_VERSION = "9.1";
    String MYSQL_IMAGE_NAME = "mysql:" + MYSQL_VERSION;
    String MYSQL_DB_NAME = "realestate_test";
    String MYSQL_USERNAME = "test";
    String MYSQL_PASSWORD = "test";

    MySQLContainer<?> MYSQL_CONTAINER = createAndStartContainer();

    // 컨테이너 생성 및 시작을 위한 static 메서드
    static MySQLContainer<?> createAndStartContainer() {
        MySQLContainer<?> container = new MySQLContainer<>(
            DockerImageName.parse(MYSQL_IMAGE_NAME))
            .withDatabaseName(MYSQL_DB_NAME)
            .withUsername(MYSQL_USERNAME)
            .withPassword(MYSQL_PASSWORD);
        container.start();
        return container;
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = MYSQL_CONTAINER.getJdbcUrl();
        String datasourceUrlParams = "?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true";

        registry.add("spring.datasource.url", () -> jdbcUrl + datasourceUrlParams);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.datasource.username", () -> MYSQL_USERNAME);
        registry.add("spring.datasource.password", () -> MYSQL_PASSWORD);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

}
