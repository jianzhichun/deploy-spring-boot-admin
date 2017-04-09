package spring.boot.admin.deploy.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

public interface DeployBootstrap extends CommandLineRunner {
	
	final Logger logger = LoggerFactory.getLogger(DeployBootstrap.class);
	
}
