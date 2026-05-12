package task1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@ConfigurationPropertiesScan
@EnableConfigurationProperties
@SpringBootApplication
public class ServiceAApplication {
//    private final ApplicationContext applicationContext;
//
//    public ServiceAApplication(ApplicationContext applicationContext) {
//        this.applicationContext = applicationContext;
//        var a = applicationContext.getBean("weatherConsumerConfigs");
//        var b = applicationContext.getBean("weatherConsumerFactory");
//        var c = applicationContext.getBean("weatherKafkaListenerContainerFactory");
//        System.out.println("ok");
//    }

    public static void main(String[] args) {
        SpringApplication.run(ServiceAApplication.class, args);
    }
}