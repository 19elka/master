package task1.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import task1.grpc.WeatherServiceGrpc;

@Configuration
@Profile("service-a")
public class GrpcClientConfig {

    @Value("${grpc.client.weather-service-b.host:localhost}")
    private String grpcHost;

    @Value("${grpc.client.weather-service-b.port:9090}")
    private int grpcPort;

    @Bean
    public ManagedChannel weatherChannel() {
        return ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                .usePlaintext()
                .build();
    }

    @Bean
    public WeatherServiceGrpc.WeatherServiceBlockingStub weatherStub(ManagedChannel weatherChannel) {
        return WeatherServiceGrpc.newBlockingStub(weatherChannel);
    }
}
