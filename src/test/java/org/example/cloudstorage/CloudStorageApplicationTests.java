package org.example.cloudstorage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
// TODO временно (потом будут тест контеинеры и test.properties изменятся)
@ActiveProfiles("test")
class CloudStorageApplicationTests {

    @Test
    void contextLoads() {
    }

}
