package spring.boot.admin.deploy.web;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.bind.annotation.ResponseBody;

import de.codecentric.boot.admin.web.AdminController;
import spring.boot.admin.deploy.config.DeployProperties;
import spring.boot.admin.deploy.config.DeployProperties.DeployAction;

/**
 * 
 * @author chun
 *
 */
@AdminController
@ResponseBody
@RequestMapping("/api/deploy")
public class DeployController {
	private final Map<String, DeployAction> actions;
	
	public DeployController(Map<String, DeployAction> actions) {
		this.actions = actions;
	}

	@RequestMapping(value = "/actions", method = GET)
	public Map<String, DeployAction> getActions() {
		return actions;
	}
	
	@RequestMapping(value = "/doAction", method = POST)
	public ReturnData doAction(@RequestBody DeployProperties.DeployAction action) {
		return doActionInvoke(action);
	}

	@RequestMapping(value = "/doAction/{actionName}", method = GET)
	public ReturnData doAction(@PathVariable("actionName") String actionName) {
		return doActionInvoke(actions.get(actionName));
	}
	
	private ReturnData doActionInvoke(DeployProperties.DeployAction action) {
		String info;
		try {
			info = action.doAction();
			return ReturnData.newReturnData(true, info);
		} catch (IOException e) {
			return ReturnData.newReturnData(false, "");
		}
	}
	
	public static class ReturnData{
		private boolean success;
		private String info;
		private ReturnData(){};
		public static ReturnData newReturnData(boolean success, String info){
			ReturnData returnData = new ReturnData();
			returnData.setSuccess(success);
			returnData.setInfo(info);
			return returnData;
		}
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public String getInfo() {
			return info;
		}
		public void setInfo(String info) {
			this.info = info;
		}
	}

}
