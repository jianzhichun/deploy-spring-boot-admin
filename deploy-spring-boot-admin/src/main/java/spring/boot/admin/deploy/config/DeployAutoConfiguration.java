package spring.boot.admin.deploy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.codecentric.boot.admin.config.AdminServerWebConfiguration;
import de.codecentric.boot.admin.config.RevereseZuulProxyConfiguration;
import spring.boot.admin.deploy.web.DeployController;

@Configuration
@EnableConfigurationProperties(DeployProperties.class)
@AutoConfigureBefore({ AdminServerWebConfiguration.class, RevereseZuulProxyConfiguration.class })
public class DeployAutoConfiguration {
	
	@Autowired DeployProperties properties;

	@Bean
	public DeployController deployController() {
		return new DeployController(properties.getActions());
	}

}
