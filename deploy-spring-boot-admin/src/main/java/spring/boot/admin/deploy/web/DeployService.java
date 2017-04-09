package spring.boot.admin.deploy.web;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import rx.Observable;
import spring.boot.admin.deploy.config.DeployProperties;
import spring.boot.admin.deploy.config.DeployProperties.DeployStep;

public interface DeployService {
	
	default ReturnData doActionInvoke(List<DeployStep> steps) {
		String info;
		try {
			info = Observable
					.from(steps)
					.map(step -> {
						try {
							return step.doStep();
						} catch (IOException e) {
							return step.toString() + " error: " + e.getMessage();
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
	
	default void doActions(DeployProperties properties, String... names) {
		if(null == names)
			return;
		Observable
			.from(names)
			.map(actionName -> 
				actionName + ":" + System.lineSeparator() + doActionInvoke(properties.getActions().get(actionName)).getInfo())
			.reduce((info1, info2) -> info1 + System.lineSeparator() + info2)
			.subscribe(content -> subscribe(content));

	}
	
	default void subscribe(String content){}

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

		@Override
		public String toString() {
			return "ReturnData [success=" + success + ", info=" + info + "]";
		}
		
		
	}
	
}
