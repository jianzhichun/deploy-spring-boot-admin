package spring.boot.admin.deploy.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 
 * @author chun
 *
 */
@ConfigurationProperties("spring.boot.admin.deploy")
public class DeployProperties {
	
	private Map<String, DeployAction> actions;
	
	public interface Action{
		String doAction() throws IOException;
	}
	
	public static class DeployAction implements Action{
		private String executable;
		private String[] args;
		public String getExecutable() {
			return executable;
		}
		public void setExecutable(String executable) {
			this.executable = executable;
		}
		public String[] getArgs() {
			return args;
		}
		public void setArgs(String[] args) {
			this.args = args;
		}
		@Override
		public String doAction() throws IOException {
			try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream();  
		            ByteArrayOutputStream errorStream = new ByteArrayOutputStream(); ){
				CommandLine commandline = new CommandLine(executable);
				if(null != args){
					commandline.addArguments(args);
				}
				DefaultExecutor exec = new DefaultExecutor(); 
				exec.setExitValues(null);
	            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream,errorStream);  
	            exec.setStreamHandler(streamHandler);  
	            exec.execute(commandline);  
	            String out = outputStream.toString();  
	            String error = errorStream.toString();  
				return out + System.lineSeparator() + error;
			}
		}
	}

	public Map<String, DeployAction> getActions() {
		return actions;
	}

	public void setActions(Map<String, DeployAction> actions) {
		this.actions = actions;
	}
	
}
