package lsds.ft.context

import com.williamhill.scoreboards.replayer.properties.TargetEnvironment
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource('classpath:env/${env}.properties')
@ComponentScan("com.williamhill.lsds.ft.*")
public class Config {

    @Value('${env}')
    String environment

    @Bean
    TargetEnvironment targetEnvironment() {
        def environment = TargetEnvironment.get(environment)
      //  assert environment == TargetEnvironment.LOCAL
        environment
    }

}