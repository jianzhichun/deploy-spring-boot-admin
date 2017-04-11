package spring.boot.admin.deploy.web;


import java.util.List;

import rx.Observable;
import spring.boot.admin.deploy.config.DeployProperties.ActionResult;
import spring.boot.admin.deploy.config.DeployProperties.DefaultAction;

public interface DeployService {
	
	default Observable<List<ActionResult>> doActions(DefaultAction... actions) {
		return Observable
				.from(actions)
				.map(action -> action.call())
				.toList();
	}
	
}
