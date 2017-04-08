package spring.boot.admin.deploy.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.bind.annotation.ResponseBody;

import de.codecentric.boot.admin.web.AdminController;
import rx.Observable;
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
	private final Map<String, List<DeployAction>> actions;

	public DeployController(Map<String, List<DeployAction>> actions) {
		this.actions = actions;
	}

	@RequestMapping(value = "/actions", method = GET)
	public Map<String, List<DeployAction>> getActions() {
		return actions;
	}

	@RequestMapping(value = "/doAction", method = POST)
	public ReturnData doAction(@RequestBody List<DeployAction> actions) {
		return doActionInvoke(actions);
	}

	@RequestMapping(value = "/doAction/{name}", method = GET)
	public ReturnData doAction(@PathVariable("name") String name) {
		return doActionInvoke(actions.get(name));
	}

	private ReturnData doActionInvoke(List<DeployAction> actions) {
		String info;
		try {
			info = Observable
					.from(actions)
					.map(action -> {
						try {
							return action.doAction();
						} catch (IOException e) {
							return action.toString() + " error: " + e.getMessage();
						}
					})
					.reduce((info1, info2) -> info1 + System.lineSeparator() + info2)
					.toBlocking()
					.toFuture()
					.get();

			return ReturnData.newReturnData(true, info);

		} catch (InterruptedException | ExecutionException e) {
			return ReturnData.newReturnData(false, e.getMessage());
		}

	}

	public static class ReturnData {
		private boolean success;
		private String info;

		private ReturnData() {
		};

		public static ReturnData newReturnData(boolean success, String info) {
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
