package com.vitali.cloud.jlong.cloudfoundryclient;

import lombok.RequiredArgsConstructor;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationListingCommandLineRunner implements CommandLineRunner {

    private final CloudFoundryOperations cf;

    @Override
    public void run(String... args) throws Exception {
        cf.applications()
                .list()
                .subscribe(System.out::println);
    }
}
