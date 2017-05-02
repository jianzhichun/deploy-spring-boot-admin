package spring.boot.admin.deploy.config;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
import rx.functions.Func1;

/**
 * 
 * @author chun
 *
 */
@ConfigurationProperties("spring.boot.admin.deploy")
public class DeployProperties {

	private static String charset = "utf-8";

	private Map<String, List<Step>> actions;
	
	private Map<String, Action> actionMap;

	private List<String> bootstrap = newArrayList();
	
	private List<Action> bootstrapList = newArrayList();

	@PostConstruct void initialize() throws InterruptedException, ExecutionException {
		actionMap = Observable
						.from(actions.entrySet())
						.map(entry -> Action.newDefaultAction(entry.getKey(), entry.getValue()))
						.toMap(action -> action.getName())
						.toBlocking()
						.toFuture()
						.get();
		bootstrapList = bootstrap.stream()
							.map(name -> actionMap.get(name))
							.collect(Collectors.toList());
	}
	

	public static class Action {
		private String name = "undefined";
		private List<Step> steps;
		private static Func1<Action, ActionResult> runner = new Func1<Action, ActionResult>(){

			@Override
			public ActionResult call(Action t) {
				try {
					String info = Observable
							.from(t.getSteps())
							.map(step -> step.call())
							.onErrorResumeNext(err -> Observable.just(err.getMessage()))
							.reduce((info1, info2) -> info1 + System.lineSeparator() + info2)
							.toBlocking()
							.toFuture()
							.get();
					return ActionResult
							.newActionResult(!StringUtils.containsIgnoreCase(info, "ERROR")
									, t.getName() + ": " + System.lineSeparator() + info);
				} catch (InterruptedException | ExecutionException e) {
					return ActionResult.newActionResult(false, t.getName() + " ERROR: " + e.getMessage());
				}
			}
			
		};
		
		private Action(){};
		
		public static Action newDefaultAction(String name, List<Step> steps){
			Action defaultAction = new Action();
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

		public List<Step> getSteps() {
			return steps;
		}

		public void setSteps(List<Step> steps) {
			this.steps = steps;
		}
		
		
		public Func1<Action, ActionResult> getRunner() {
			return runner;
		}

		public void setRunner(Func1<Action, ActionResult> runner) {
			Action.runner = runner;
		}

		public ActionResult call(){
			return runner.call(this);
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
	
	public static class Step {
		private String exec;
		private String[] args;
		private String workingDirectory = ".";

		private static Func1<Step, String> runner = new Func1<Step, String>(){

			@Override
			public String call(Step t) {
				try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						ByteArrayOutputStream errorStream = new ByteArrayOutputStream();) {
					CommandLine commandline = CommandLine.parse(t.getExec());
					if (null != t.getArgs()) {
						commandline.addArguments(t.getArgs());
					}
					DefaultExecutor exec = new DefaultExecutor();
					exec.setExitValues(null);
					PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
					exec.setWorkingDirectory(new File(t.getWorkingDirectory()));
					exec.setStreamHandler(streamHandler);
					exec.execute(commandline);
					String out = outputStream.toString(charset);
					String error = errorStream.toString(charset);
					return out + System.lineSeparator() + error;
				} catch (IOException e) {
					return toString() + " ERROR: " + e.getMessage();
				}
			}

			
		};
		
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
		

		public String getWorkingDirectory() {
			return workingDirectory;
		}

		public void setWorkingDirectory(String workingDirectory) {
			this.workingDirectory = workingDirectory;
		}

		public String call() {
			return runner.call(this);
		}
		
		
		public static Func1<Step, String> getRunner() {
			return runner;
		}

		public static void setRunner(Func1<Step, String> runner) {
			Step.runner = runner;
		}

		@Override
		public String toString() {
			return "step [ " + exec + (null == args ? "" : " " + Joiner.on(" ").join(args)) + " ]";
		}

	}
	
	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		DeployProperties.charset = charset;
	}

	public Map<String, List<Step>> getActions() {
		return actions;
	}

	public void setActions(Map<String, List<Step>> actions) {
		this.actions = actions;
	}

	public List<String> getBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(List<String> bootstrap) {
		this.bootstrap = bootstrap;
	}

	public Map<String, Action> getActionMap() {
		return actionMap;
	}

	public List<Action> getBootstrapList() {
		return bootstrapList;
	}
	
	
}
