package spring.boot.admin.deploy.pipe;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.functions.Func1;
import spring.boot.admin.deploy.config.DeployProperties.ActionResult;

public interface Pipeline extends Func1<List<ActionResult>, List<ActionResult>>{
	
	@Override List<ActionResult> call(List<ActionResult> actionResults);
	
	static <T> void pipelize(Iterable<? extends Func1<? super T, T>> pipes, Observable<T> observable){
		Iterator<? extends Func1<? super T, T>> iter = pipes.iterator();
		while(iter.hasNext()){
			observable = observable.map(iter.next());
		}
	}
	
	public interface Defaults{
		
		public interface BootstrapPipe extends Pipeline{};
		
		public interface DestroyPipe extends Pipeline{};
		
		public interface ActionsPipe extends Pipeline{};
		
		public interface LogPipe extends BootstrapPipe, DestroyPipe, ActionsPipe{
			
			final Logger logger = LoggerFactory.getLogger("LogPipe");
			
			@Override
			default List<ActionResult> call(List<ActionResult> actionResults) {
				actionResults.forEach(rs -> logger.info(rs.toString()));
				return actionResults;
			}
		};
		
	}
	
}
