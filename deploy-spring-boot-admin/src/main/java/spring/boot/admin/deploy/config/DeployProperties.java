package spring.boot.admin.deploy.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.google.common.base.Joiner;

/**
 * 
 * @author chun
 *
 */
@ConfigurationProperties("spring.boot.admin.deploy")
public class DeployProperties {
	
	private static String charset = "utf-8";
	
	private Map<String, List<DeployStep>> actions;
	
	private String[] bootstrap;
	
	private String[] destroy;
	
	public interface Step{
		String doStep() throws IOException;
	}
	
	public static class DeployStep implements Step{
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
		public String doStep() throws IOException {
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
	            String out = outputStream.toString(charset);  
	            String error = errorStream.toString(charset);  
				return out + System.lineSeparator() + error;
			}
		}
		@Override
		public String toString() {
			return "action [ " + executable + " " + Joiner.on(" ").join(args) + " ]";
		}
		
	}
	
	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		DeployProperties.charset = charset;
	}

	public Map<String, List<DeployStep>> getActions() {
		return actions;
	}

	public void setActions(Map<String, List<DeployStep>> actions) {
		this.actions = actions;
	}

	public String[] getBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(String[] bootstrap) {
		this.bootstrap = bootstrap;
	}

	public String[] getDestroy() {
		return destroy;
	}

	public void setDestroy(String[] destroy) {
		this.destroy = destroy;
	}
	

}
