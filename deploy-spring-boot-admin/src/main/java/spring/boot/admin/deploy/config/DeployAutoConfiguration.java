package spring.boot.admin.deploy.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;


import de.codecentric.boot.admin.config.AdminServerWebConfiguration;
import de.codecentric.boot.admin.config.RevereseZuulProxyConfiguration;
import spring.boot.admin.deploy.notify.DeployNotifier;
import spring.boot.admin.deploy.runner.DeployBootstrap;
import spring.boot.admin.deploy.runner.DeployDestroy;
import spring.boot.admin.deploy.web.DeployController;
import spring.boot.admin.deploy.web.DeployService;

@Configuration
@EnableConfigurationProperties(DeployProperties.class)
@AutoConfigureBefore({ AdminServerWebConfiguration.class, RevereseZuulProxyConfiguration.class })
@ConditionalOnProperty(value = "spring.boot.admin.deploy.enabled", matchIfMissing = true)
public class DeployAutoConfiguration {

	@Autowired DeployProperties properties;
	
	@Configuration
	@ConditionalOnBean(MailSender.class)
	@AutoConfigureAfter({ MailSenderAutoConfiguration.class, DeployAutoConfiguration.class })
	@EnableConfigurationProperties(MailNotifierConfiguration.DeployMailProperties.class)
	public static class MailNotifierConfiguration {
		
		@Autowired private MailSender mailSender;
		@Autowired DeployMailProperties properties;
		
		@ConfigurationProperties("spring.boot.admin.deploy.mail")
		public static class DeployMailProperties{
			private SimpleMailMessage simpleMailMessage;
			public SimpleMailMessage getSimpleMailMessage() {
				return simpleMailMessage;
			}
			public void setSimpleMailMessage(SimpleMailMessage simpleMailMessage) {
				this.simpleMailMessage = simpleMailMessage;
			}
		}
		
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "spring.boot.admin.deploy.mail.enabled", matchIfMissing = true)
		@Bean DeployNotifier emailNotifier(){
			return new DeployNotifier() {
				
				@Override
				public void sendNotification(String content) {
					SimpleMailMessage mailMessage = properties.getSimpleMailMessage();
					if(mailMessage == null)
						return;
					mailMessage.setText(mailMessage.getText() + System.lineSeparator() + content);
					mailSender.send(mailMessage);
				}
			};
			
		}
	}
	
	@ConditionalOnMissingBean(DeployService.class)
	@Bean DeployService deployService(List<DeployNotifier> deployNotifiers) {
		return new DeployService(){
			final Logger logger = LoggerFactory.getLogger(DeployService.class);
			@Override
			public void subscribe(String content){
				
				deployNotifiers.forEach(notifier->{
					try {
						notifier.sendNotification(content);
					} catch (Exception e) {
						logger.error(e.toString());
					}
				});
			}

		};
	}
	
	@Bean DeployNotifier loggerNotifier(){
		return new DeployNotifier() {
			final Logger logger = LoggerFactory.getLogger(DeployNotifier.class);
			@Override
			public void sendNotification(String content) {
				logger.info(content);
			}
		};
		
	}
	
	@ConditionalOnMissingBean(DeployBootstrap.class)
	@Bean DeployBootstrap deployBootstrapRunner(DeployService deployService) {
		return new DeployBootstrap(){
			
			@Override
			public void run(String... args) throws Exception {
				deployService.doActions(properties, properties.getBootstrap());
			}

		};
	}
	
	@ConditionalOnMissingBean(DeployDestroy.class)
	@Bean DeployDestroy deployDestroyRunner(DeployService deployService) {
		return new DeployDestroy(){
			

			@Override
			public void destroy() throws Exception {
				deployService.doActions(properties, properties.getDestroy());
				
			}

		};
	}

	@Bean DeployController deployController(DeployService deployService) {
		return new DeployController(properties.getActions(), deployService);
	}

}
