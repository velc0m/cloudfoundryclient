package com.vitali.cloud.jlong.cloudfoundryclient;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CloudFoundryClientApplication {

    private static final String DOUBLE_SLASH = "://";

    public static void main(String[] args) {
        SpringApplication.run(CloudFoundryClientApplication.class, args);
    }

    @Bean
    public ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext,
                                                        TokenProvider tokenProvider) {
        return ReactorCloudFoundryClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();
    }

    @Bean
    public ReactorDopplerClient dopplerClient(ConnectionContext connectionContext,
                                              TokenProvider tokenProvider) {
        return ReactorDopplerClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();
    }

    @Bean
    public ReactorUaaClient uaaClient(ConnectionContext connectionContext,
                                      TokenProvider tokenProvider) {
        return ReactorUaaClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();
    }

    @Bean
    public DefaultCloudFoundryOperations cloudFoundryOperations(CloudFoundryClient cloudFoundryClient,
                                                                ReactorDopplerClient dopplerClient,
                                                                ReactorUaaClient uaaClient,
                                                                @Value("${cf.api}") String organization,
                                                                @Value("${cf.space}") String space) {
        return DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(cloudFoundryClient)
                .dopplerClient(dopplerClient)
                .uaaClient(uaaClient)
                .organization(organization)
                .space(space)
                .build();
    }

    @Bean
    public DefaultConnectionContext connectionContext(@Value("${cf.api}") String apiHost) {

        if (apiHost.contains(DOUBLE_SLASH)) {
            apiHost = apiHost.split("://")[1];
        }
        return DefaultConnectionContext.builder()
                .apiHost(apiHost)
                .build();
    }

    @Bean
    public PasswordGrantTokenProvider tokenProvider(@Value("${cf.user}") String username,
                                                    @Value("$cf.password") String password) {
        return PasswordGrantTokenProvider.builder()
                .username(username)
                .password(password)
                .build();
    }
}
