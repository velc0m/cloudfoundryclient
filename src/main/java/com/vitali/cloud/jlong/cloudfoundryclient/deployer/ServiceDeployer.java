package com.vitali.cloud.jlong.cloudfoundryclient.deployer;

import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.services.DeleteServiceInstanceRequest;
import org.cloudfoundry.operations.services.ServiceInstanceSummary;
import org.cloudfoundry.operations.services.UnbindServiceInstanceRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@RequiredArgsConstructor
public class ServiceDeployer {

    private final Log log = LogFactory.getLog(getClass());
    private final CloudFoundryOperations cf;

    public Mono<Void> deployService(String applicationName, String svcInstanceName, String svcTypeName, String planName) {
        return cf.services()
                .listInstances()
                .cache()
                .filter(si1 -> si1.getName().equalsIgnoreCase(svcInstanceName))
                .transform(unbindAndDelete(applicationName, svcInstanceName))
                .thenEmpty(createService(svcInstanceName, svcTypeName, planName));
    }

    private Function<Flux<ServiceInstanceSummary>, Publisher<Void>> unbindAndDelete(String applicationName, String svcInstanceName) {
        return siFlux -> Flux.concat(
                unbind(applicationName, svcInstanceName, siFlux),
                delete(svcInstanceName, siFlux)
        );
    }

    private Flux<Void> unbind(String applicationName, String svcInstanceName, Flux<ServiceInstanceSummary> siFlux) {
        return siFlux.filter(si -> si.getApplications().contains(applicationName))
                .flatMap(si -> cf.services().unbind(UnbindServiceInstanceRequest.builder()
                        .applicationName(applicationName)
                        .serviceInstanceName(svcInstanceName)
                        .build()));
    }

    private Flux<Void> delete(String svcInstanceName, Flux<ServiceInstanceSummary> siFlux) {
        return siFlux.flatMap(si -> cf.services().deleteInstance(DeleteServiceInstanceRequest.builder()
                .name(svcInstanceName)
                .build()));
    }

    private Mono<Void> createService(String svcInstanceName, String svcTypeName, String planName) {
        return cf.services()
                .createInstance(CreateServiceInstanceRequest.builder()
                        .serviceName(svcTypeName)
                        .planName(planName)
                        .serviceInstanceName(svcInstanceName)
                        .build());
    }
}
