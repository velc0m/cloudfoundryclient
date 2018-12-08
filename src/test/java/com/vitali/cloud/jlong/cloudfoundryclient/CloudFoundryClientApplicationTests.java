package com.vitali.cloud.jlong.cloudfoundryclient;

import com.vitali.cloud.jlong.cloudfoundryclient.deployer.ApplicationDeployer;
import com.vitali.cloud.jlong.cloudfoundryclient.deployer.ServiceDeployer;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Config.class)
public class CloudFoundryClientApplicationTests {

    @Autowired
    private ApplicationDeployer applicationDeployer;

    @Autowired
    private ServiceDeployer serviceDeployer;

    @Test
    public void deploy() {
        File projectFolder = new File(new File("."), "../customerapplication");
        File jar = new File(projectFolder, "target/customerapplication-0.0.1-SNAPSHOT.jar");

        String applicationName = "bootcamps-customers";
        String mysqlSvc = "mysql";
        Map<String, String> env = new HashMap<>();
        env.put("SPRING_PROFILES_ACTIVE", "cloud");

        Duration timeout = Duration.ofMinutes(5);

        serviceDeployer.deployService(applicationName, mysqlSvc, "ClearDB MySQL Database", "Spark DB")
                .then(applicationDeployer.deployApplication(jar, applicationName, env, timeout, mysqlSvc))
                .block();
    }
}

@SpringBootApplication
class Config {

    @Bean
    public ApplicationDeployer applicationDeployer(CloudFoundryOperations cf) {
        return new ApplicationDeployer(cf);
    }

    @Bean
    public ServiceDeployer serviceDeployer(CloudFoundryOperations cf) {
        return new ServiceDeployer(cf);
    }
}
