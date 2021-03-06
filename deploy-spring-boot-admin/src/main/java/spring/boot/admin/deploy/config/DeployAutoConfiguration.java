package spring.boot.admin.deploy.config;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import de.codecentric.boot.admin.config.AdminServerWebConfiguration;
import de.codecentric.boot.admin.config.RevereseZuulProxyConfiguration;
import rx.Observable;
import spring.boot.admin.deploy.config.DeployProperties.ActionResult;
import spring.boot.admin.deploy.config.DeployProperties.Action;
import spring.boot.admin.deploy.pipe.Pipeline;
import spring.boot.admin.deploy.pipe.Pipeline.Defaults.ActionsPipe;

import static spring.boot.admin.deploy.pipe.Pipeline.Defaults.*;
import spring.boot.admin.deploy.runner.DeployBootstrap;
import spring.boot.admin.deploy.web.DeployController;
import spring.boot.admin.deploy.web.DeployService;

@Configuration
@EnableConfigurationProperties(DeployProperties.class)
@AutoConfigureBefore({ AdminServerWebConfiguration.class, RevereseZuulProxyConfiguration.class })
@ConditionalOnProperty(value = "spring.boot.admin.deploy.enabled", matchIfMissing = true)
public class DeployAutoConfiguration {

	@Configuration
	@ConditionalOnBean(MailSender.class)
	@AutoConfigureAfter({ MailSenderAutoConfiguration.class, DeployAutoConfiguration.class })
	@EnableConfigurationProperties(MailNotifierConfiguration.DeployMailProperties.class)
	public static class MailNotifierConfiguration {

		@ConfigurationProperties("spring.boot.admin.deploy.mail")
		public static class DeployMailProperties {
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
		@Bean BootstrapPipe emailNotifier(DeployMailProperties properties, MailSender mailSender) {
			return new BootstrapPipe() {

				@Override
				public List<ActionResult> call(List<ActionResult> actionResults) {
					SimpleMailMessage mailMessage = properties.getSimpleMailMessage();
					if (mailMessage == null)
						return actionResults;
					mailMessage.setText(mailMessage.getText() + System.lineSeparator()
							+ Joiner.on(System.lineSeparator()).join(actionResults));
					mailSender.send(mailMessage);
					return actionResults;
				}
			};

		}
	}

	@ConditionalOnMissingBean(DeployService.class)
	@Bean DeployService deployService() {
		return new DeployService() {};
	}

	@ConditionalOnMissingBean(DeployBootstrap.class)
	@Bean DeployBootstrap deployBootstrapRunner(DeployService deployService
			, DeployProperties properties, List<BootstrapPipe> pipes) {
		return new DeployBootstrap() {

			@Override
			public void run(String... args) throws Exception {
				if(CollectionUtils.isEmpty(properties.getBootstrapList()))
					return;
				Observable<List<ActionResult>> observable = deployService
						.doActions(properties.getBootstrapList().toArray(new Action[]{}));
				Pipeline.pipelize(pipes, observable).subscribe();
			}

		};
	}

	@Bean LogPipe logPipe() {
		return new LogPipe() {};
	}
	
	@ConditionalOnMissingBean(DeployController.class)
	@Bean DeployController deployController(DeployService deployService, DeployProperties properties,
			List<ActionsPipe> pipes) {
		return new DeployController() {

			@RequestMapping(value = "/actions", method = GET)
			public Collection<Action> getActions() {
				return properties.getActionMap().values();
			}

			@RequestMapping(value = "/doAction", method = POST)
			public List<ActionResult> doActions(@RequestBody Action... action)
					throws InterruptedException, ExecutionException {
				Observable<List<ActionResult>> observable = deployService.doActions(action);
				return Pipeline.pipelize(pipes, observable).toBlocking().toFuture().get();
			}

			@RequestMapping(value = "/doAction/{names}", method = GET)
			public List<ActionResult> doActions(@PathVariable("names") String names)
					throws InterruptedException, ExecutionException {
				Action[] actions = Splitter.on("and").splitToList(names)
												.stream()
												.map(name -> properties.getActionMap().get(name))
												.collect(Collectors.toList())
												.toArray(new Action[]{});
				Observable<List<ActionResult>> observable = deployService
						.doActions(actions);
				return Pipeline.pipelize(pipes, observable).toBlocking().toFuture().get();
			}
		};
	}

}
