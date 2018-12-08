package com.vitali.cloud.jlong.cloudfoundryclient.deployer;

import lombok.RequiredArgsConstructor;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.services.BindServiceInstanceRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ApplicationDeployer {

    private final CloudFoundryOperations cf;

    public Mono<Void> deployApplication(File jar, String applicationName, Map<String, String> envOg, Duration timeout, String... svcs) {
        return cf.applications()
                .push(pushApp(jar, applicationName))
                .then(bindServices(applicationName, svcs))
                .then(setEnvironmentVariables(applicationName, new HashMap<>(envOg)))
                .then(startApplication(applicationName, timeout));
    }

    private PushApplicationRequest pushApp(File jar, String applicationName) {
        return PushApplicationRequest.builder()
                .name(applicationName)
                .noStart(true)
                .randomRoute(true)
                .buildpack("https://github.com/cloudfoundry/java-buidpack.git")
                .application(jar.toPath())
                .instances(1)
                .build();
    }

    private Mono<Void> bindServices(String applicationName, String[] svcs) {
        return Flux
                .just(svcs)
                .flatMap(svc -> {
                    BindServiceInstanceRequest request = BindServiceInstanceRequest.builder()
                            .applicationName(applicationName)
                            .serviceInstanceName(svc)
                            .build();
                    return cf.services().bind(request);
                })
                .then();
    }

    private Mono<Void> startApplication(String applicationName, Duration timeout) {
        return cf.applications()
                .start(StartApplicationRequest.builder()
                        .name(applicationName)
                        .startupTimeout(timeout)
                        .build());
    }

    private Mono<Void> setEnvironmentVariables(String applicationName, Map<String, String> env) {
        return Flux
                .fromIterable(env.entrySet())
                .flatMap(kv -> cf.applications().setEnvironmentVariable(
                        SetEnvironmentVariableApplicationRequest.builder()
                                .name(applicationName)
                                .variableName(kv.getKey())
                                .variableValue(kv.getValue())
                                .build()))
                .then();
    }
}
