package spring.boot.admin.deploy.web;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	@RequestMapping(value = "/actions", method = RequestMethod.GET)
	public Map<String, DeployAction> getActions() {
		return actions;
	}
	
	@RequestMapping(value = "/doAction", method = RequestMethod.GET)
	public ReturnData doAction(DeployProperties.Action action) {
		String info;
		try {
			info = action.doAction();
			return ReturnData.newReturnData(true, info);
		} catch (IOException e) {
			return ReturnData.newReturnData(false, "");
		}
	}
	
	@RequestMapping(value = "/doAction/{actionName}", method = RequestMethod.GET)
	public ReturnData doAction(@PathVariable("actionName") String actionName) {
		String info;
		try {
			info = actions.get(actionName).doAction();
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
