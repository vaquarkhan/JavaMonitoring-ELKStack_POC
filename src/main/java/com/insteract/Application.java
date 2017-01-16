package main.java.com.insteract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Application
{
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final Marker fatal = MarkerFactory.getMarker("FATAL");

    public static void main( String[] args ) {
        final ExecutorService executor = Executors.newCachedThreadPool();
        final Collection< Future< Void > > futures = new ArrayList< Future< Void > >();
        final Random random = new Random();

        for( int i = 0; i < 10; ++i ) {
            try {
                futures.add(
                    executor.submit(
                        new Callable< Void >() {
                            public Void call() throws Exception {
                                int randomNum = random.nextInt( 10000 );
                                logger.debug("Randome number: " + randomNum); // To demonstrate DEBUG in ELK Stack
                                int sleep = Math.abs( randomNum % 10000 );
                                logger.warn( "Sleeping for " + sleep + "ms" ); // To demonstrate WARN in ELK Stack
                                Thread.sleep( sleep );
                                return null;
                            }
                        }
                    )
                );
                if(i == 5)
                    throw new Exception("To demonstrate FATAL"); // To demonstrate FATAL in Sentry
                logger.trace("Submitted task no: " +i); // To demonstrate TRACE in Sentry
            } catch (Exception e) {
                logger.error(fatal, "Failed to submit task: ", e);
            }
        }
   
        for( final Future< Void > future: futures ) {
            try {
                Void result = future.get( 3, TimeUnit.SECONDS );
                logger.info( "Result " + result ); // To demonstrate INFO in ELK Stack
            } catch (InterruptedException | ExecutionException | TimeoutException ex ) {
                logger.error("There is a problem: " + ex.getMessage(), ex); // To demonstrate ERROR in Sentry
            }  
        }
    }
}

