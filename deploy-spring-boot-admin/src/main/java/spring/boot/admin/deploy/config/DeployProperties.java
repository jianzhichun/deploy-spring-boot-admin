package spring.boot.admin.deploy.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.google.common.base.Joiner;
import static com.google.common.collect.Lists.*;

import rx.Observable;

/**
 * 
 * @author chun
 *
 */
@ConfigurationProperties("spring.boot.admin.deploy")
public class DeployProperties {

	private static String charset = "utf-8";

	private Map<String, List<DefaultStep>> actions;
	
	private Map<String, DefaultAction> actionMap;

	private List<String> bootstrap = newArrayList();
	
	private List<DefaultAction> bootstrapList = newArrayList();

	private List<String> destroy = newArrayList();
	
	private List<DefaultAction> destroyList = newArrayList();
	
	@PostConstruct void initialize() throws InterruptedException, ExecutionException {
		actionMap = Observable
						.from(actions.entrySet())
						.map(entry -> DefaultAction.newDefaultAction(entry.getKey(), entry.getValue()))
						.toMap(action -> action.getName())
						.toBlocking()
						.toFuture()
						.get();
		bootstrapList = bootstrap.stream()
							.map(name -> actionMap.get(name))
							.collect(Collectors.toList());
		destroyList = destroy.stream()
							.map(name -> actionMap.get(name))
							.collect(Collectors.toList());
	}
	
	public interface Step {
		String call() throws IOException;
	}

	public interface Action {
		ActionResult call();
	}

	public static class DefaultAction implements Action {
		private String name = "undefined";
		private List<DefaultStep> steps;
		
		private DefaultAction(){};
		
		public static DefaultAction newDefaultAction(String name, List<DefaultStep> steps){
			DefaultAction defaultAction = new DefaultAction();
			defaultAction.setName(name);
			defaultAction.setSteps(steps);
			return defaultAction;
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<DefaultStep> getSteps() {
			return steps;
		}

		public void setSteps(List<DefaultStep> steps) {
			this.steps = steps;
		}

		@Override
		public ActionResult call(){
			try {
				String info = Observable
						.from(steps)
						.map(step -> {
							try {
								return step.call();
							} catch (IOException e) {
								return step.toString() + " ERROR: " + e.getMessage();
							}
						})
						.reduce((info1, info2) -> info1 + System.lineSeparator() + info2)
						.toBlocking()
						.toFuture()
						.get();
				return ActionResult
						.newActionResult(!StringUtils.containsIgnoreCase(info, "ERROR")
								, name + ": " + System.lineSeparator() + info);
			} catch (InterruptedException | ExecutionException e) {
				return ActionResult.newActionResult(false, name + " ERROR: " + e.getMessage());
			}
		}

	}

	public static class ActionResult {
		private boolean success;
		private String info;

		private ActionResult() {
		};

		public static ActionResult newActionResult(boolean success, String info) {
			ActionResult actionResult = new ActionResult();
			actionResult.setSuccess(success);
			actionResult.setInfo(info);
			return actionResult;
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
			return info;
		}
		
	}
	
	public static class DefaultStep implements Step {
		private String exec;
		private String[] args;

		public String getExec() {
			return exec;
		}

		public void setExec(String exec) {
			this.exec = exec;
		}

		public String[] getArgs() {
			return args;
		}

		public void setArgs(String[] args) {
			this.args = args;
		}

		@Override
		public String call() throws IOException {
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ByteArrayOutputStream errorStream = new ByteArrayOutputStream();) {
				CommandLine commandline = CommandLine.parse(exec);
				if (null != args) {
					commandline.addArguments(args);
				}
				DefaultExecutor exec = new DefaultExecutor();
				exec.setExitValues(null);
				PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
				exec.setStreamHandler(streamHandler);
				exec.execute(commandline);
				String out = outputStream.toString(charset);
				String error = errorStream.toString(charset);
				return out + System.lineSeparator() + error;
			}
		}

		@Override
		public String toString() {
			return "action [ " + exec + " " + Joiner.on(" ").join(args) + " ]";
		}

	}
	
	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		DeployProperties.charset = charset;
	}

	public Map<String, List<DefaultStep>> getActions() {
		return actions;
	}

	public void setActions(Map<String, List<DefaultStep>> actions) {
		this.actions = actions;
	}

	public List<String> getBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(List<String> bootstrap) {
		this.bootstrap = bootstrap;
	}

	public List<String> getDestroy() {
		return destroy;
	}

	public void setDestroy(List<String> destroy) {
		this.destroy = destroy;
	}

	public Map<String, DefaultAction> getActionMap() {
		return actionMap;
	}

	public List<DefaultAction> getBootstrapList() {
		return bootstrapList;
	}

	public List<DefaultAction> getDestroyList() {
		return destroyList;
	}
	
	
}
