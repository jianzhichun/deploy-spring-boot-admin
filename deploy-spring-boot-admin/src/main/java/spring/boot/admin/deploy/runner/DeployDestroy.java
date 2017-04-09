package spring.boot.admin.deploy.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

public interface DeployDestroy extends DisposableBean {
	
	final Logger logger = LoggerFactory.getLogger(DeployDestroy.class);
	
}
