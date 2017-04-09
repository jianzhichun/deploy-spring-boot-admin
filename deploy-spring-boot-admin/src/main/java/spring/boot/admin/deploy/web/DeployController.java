package spring.boot.admin.deploy.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.bind.annotation.ResponseBody;

import de.codecentric.boot.admin.web.AdminController;
import spring.boot.admin.deploy.config.DeployProperties.DeployStep;
import spring.boot.admin.deploy.web.DeployService.ReturnData;

/**
 * 
 * @author chun
 *
 */
@AdminController
@ResponseBody
@RequestMapping("/api/deploy")
public class DeployController {
	
	private final Map<String, List<DeployStep>> actions;
	private final DeployService deployService;

	public DeployController(Map<String, List<DeployStep>> actions, DeployService deployService) {
		this.actions = actions;
		this.deployService = deployService;
	}
	
	@RequestMapping(value = "/actions", method = GET)
	public Map<String, List<DeployStep>> getActions() {
		return actions;
	}

	@RequestMapping(value = "/doAction", method = POST)
	public ReturnData doAction(@RequestBody List<DeployStep> actions) {
		return deployService.doActionInvoke(actions);
	}

	@RequestMapping(value = "/doAction/{name}", method = GET)
	public ReturnData doAction(@PathVariable("name") String name) {
		return deployService.doActionInvoke(actions.get(name));
	}

	

}
