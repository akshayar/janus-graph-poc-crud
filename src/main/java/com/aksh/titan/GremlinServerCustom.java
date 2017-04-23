package com.aksh.titan;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.tinkerpop.gremlin.server.Channelizer;
import org.apache.tinkerpop.gremlin.server.Settings;
import org.apache.tinkerpop.gremlin.server.util.LifeCycleHook;
import org.apache.tinkerpop.gremlin.server.util.MetricManager;
import org.apache.tinkerpop.gremlin.server.util.ServerGremlinExecutor;
import org.apache.tinkerpop.gremlin.server.util.ThreadFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

public class GremlinServerCustom {
	
	 static {
	        // hook slf4j up to netty internal logging
	        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
	    }

	    private static final String SERVER_THREAD_PREFIX = "gremlin-server-";

	    private static final Logger logger = LoggerFactory.getLogger(GremlinServerCustom.class);
	    private final Settings settings;
	    private Channel ch;

	    private CompletableFuture<Void> serverStopped = null;
	    private CompletableFuture<ServerGremlinExecutor<EventLoopGroup>> serverStarted = null;

	    private final EventLoopGroup bossGroup;
	    private final EventLoopGroup workerGroup;
	    private final ExecutorService gremlinExecutorService;
	    private final ServerGremlinExecutor<EventLoopGroup> serverGremlinExecutor;

	    /**
	     * Construct a Gremlin Server instance from {@link Settings}.
	     */
	    public GremlinServerCustom(final Settings settings) {
	        this.settings = settings;

	        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.stop().join(), SERVER_THREAD_PREFIX + "shutdown"));

	        final ThreadFactory threadFactoryBoss = ThreadFactoryUtil.create("boss-%d");
	        bossGroup = new NioEventLoopGroup(settings.threadPoolBoss, threadFactoryBoss);

	        final ThreadFactory threadFactoryWorker = ThreadFactoryUtil.create("worker-%d");
	        workerGroup = new NioEventLoopGroup(settings.threadPoolWorker, threadFactoryWorker);

	        serverGremlinExecutor = new ServerGremlinExecutor<>(settings, null, workerGroup, EventLoopGroup.class);
	        gremlinExecutorService = serverGremlinExecutor.getGremlinExecutorService();
	    }

	    /**
	     * Construct a Gremlin Server instance from the {@link ServerGremlinExecutor} which internally carries some
	     * pre-constructed objects used by the server as well as the {@link Settings} object itself.  This constructor
	     * is useful when Gremlin Server is being used in an embedded style and there is a need to share thread pools
	     * with the hosting application.
	     */
	    public GremlinServerCustom(final ServerGremlinExecutor<EventLoopGroup> serverGremlinExecutor) {
	        this.serverGremlinExecutor = serverGremlinExecutor;
	        this.settings = serverGremlinExecutor.getSettings();

	        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.stop().join(), SERVER_THREAD_PREFIX + "shutdown"));

	        final ThreadFactory threadFactoryBoss = ThreadFactoryUtil.create("boss-%d");
	        bossGroup = new NioEventLoopGroup(settings.threadPoolBoss, threadFactoryBoss);
	        workerGroup = serverGremlinExecutor.getScheduledExecutorService();
	        gremlinExecutorService = serverGremlinExecutor.getGremlinExecutorService();
	    }

	    /**
	     * Start Gremlin Server with {@link Settings} provided to the constructor.
	     */
	    public synchronized CompletableFuture<ServerGremlinExecutor<EventLoopGroup>> start() throws Exception {
	        if(serverStarted != null) {
	            // server already started - don't get it rolling again
	            return serverStarted;
	        }

	        serverStarted = new CompletableFuture<>();
	        final CompletableFuture<ServerGremlinExecutor<EventLoopGroup>> serverReadyFuture = serverStarted;
	        try {
	            final ServerBootstrap b = new ServerBootstrap();

	            // when high value is reached then the channel becomes non-writable and stays like that until the
	            // low value is so that there is time to recover
	            b.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, settings.writeBufferLowWaterMark);
	            b.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, settings.writeBufferHighWaterMark);
	            b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

	            // fire off any lifecycle scripts that were provided by the user. hooks get initialized during
	            // ServerGremlinExecutor initialization
	            serverGremlinExecutor.getHooks().forEach(hook -> {
	                logger.info("Executing start up {}", LifeCycleHook.class.getSimpleName());
	                try {
	                    hook.onStartUp(new LifeCycleHook.Context(logger));
	                } catch (UnsupportedOperationException uoe) {
	                    // if the user doesn't implement onStartUp the scriptengine will throw
	                    // this exception.  it can safely be ignored.
	                }
	            });

	            final Channelizer channelizer = createChannelizer(settings);
	            channelizer.init(serverGremlinExecutor);
	            b.group(bossGroup, workerGroup)
	                    .channel(NioServerSocketChannel.class)
	                    .childHandler(channelizer);

	            // bind to host/port and wait for channel to be ready
	            b.bind(settings.host, settings.port).addListener(new ChannelFutureListener() {
	                @Override
	                public void operationComplete(final ChannelFuture channelFuture) throws Exception {
	                    if (channelFuture.isSuccess()) {
	                        ch = channelFuture.channel();

	                        logger.info("Gremlin Server configured with worker thread pool of {}, gremlin pool of {} and boss thread pool of {}.",
	                                settings.threadPoolWorker, settings.gremlinPool, settings.threadPoolBoss);
	                        logger.info("Channel started at port {}.", settings.port);

	                        serverReadyFuture.complete(serverGremlinExecutor);
	                    } else {
	                        serverReadyFuture.completeExceptionally(new IOException(
	                                String.format("Could not bind to %s and %s - perhaps something else is bound to that address.", settings.host, settings.port)));
	                    }
	                }
	            });
	        } catch (Exception ex) {
	            logger.error("Gremlin Server Error", ex);
	            serverReadyFuture.completeExceptionally(ex);
	        }

	        return serverStarted;
	    }

	    private static Channelizer createChannelizer(final Settings settings) throws Exception {
	        try {
	            final Class clazz = Class.forName(settings.channelizer);
	            final Object o = clazz.newInstance();
	            return (Channelizer) o;
	        } catch (ClassNotFoundException fnfe) {
	            logger.error("Could not find {} implementation defined by the 'channelizer' setting as: {}",
	                    Channelizer.class.getName(), settings.channelizer);
	            throw new RuntimeException(fnfe);
	        } catch (Exception ex) {
	            logger.error("Class defined by the 'channelizer' setting as: {} could not be properly instantiated as a {}",
	                    settings.channelizer, Channelizer.class.getName());
	            throw new RuntimeException(ex);
	        }
	    }

	    /**
	     * Stop Gremlin Server and free the port binding. Note that multiple calls to this method will return the
	     * same instance of the {@link java.util.concurrent.CompletableFuture}.
	     */
	    public synchronized CompletableFuture<Void> stop() {
	        if (serverStopped != null) {
	            // shutdown has started so don't fire it off again
	            return serverStopped;
	        }

	        serverStopped = new CompletableFuture<>();
	        final CountDownLatch servicesLeftToShutdown = new CountDownLatch(3);

	        // it's possible that a channel might not be initialized in the first place if bind() fails because
	        // of port conflict.  in that case, there's no need to wait for the channel to close.
	        if (null == ch)
	            servicesLeftToShutdown.countDown();
	        else
	            ch.close().addListener(f -> servicesLeftToShutdown.countDown());

	        logger.info("Shutting down thread pools.");

	        try {
	            gremlinExecutorService.shutdown();
	        } finally {
	            logger.debug("Shutdown Gremlin thread pool.");
	        }

	        try {
	            workerGroup.shutdownGracefully().addListener((GenericFutureListener) f -> servicesLeftToShutdown.countDown());
	        } finally {
	            logger.debug("Shutdown Worker thread pool.");
	        }
	        try {
	            bossGroup.shutdownGracefully().addListener((GenericFutureListener) f -> servicesLeftToShutdown.countDown());
	        } finally {
	            logger.debug("Shutdown Boss thread pool.");
	        }

	        // channel is shutdown as are the thread pools - time to kill graphs as nothing else should be acting on them
	        new Thread(() -> {
	            serverGremlinExecutor.getHooks().forEach(hook -> {
	                logger.info("Executing shutdown {}", LifeCycleHook.class.getSimpleName());
	                try {
	                    hook.onShutDown(new LifeCycleHook.Context(logger));
	                } catch (UnsupportedOperationException | UndeclaredThrowableException uoe) {
	                    // if the user doesn't implement onShutDown the scriptengine will throw
	                    // this exception.  it can safely be ignored.
	                }
	            });

	            try {
	                gremlinExecutorService.awaitTermination(30000, TimeUnit.MILLISECONDS);
	            } catch (InterruptedException ie) {
	                logger.warn("Timeout waiting for Gremlin thread pool to shutdown - continuing with shutdown process.");
	            }

	            try {
	                servicesLeftToShutdown.await(30000, TimeUnit.MILLISECONDS);
	            } catch (InterruptedException ie) {
	                logger.warn("Timeout waiting for boss/worker thread pools to shutdown - continuing with shutdown process.");
	            }

	            serverGremlinExecutor.getGraphManager().getGraphs().forEach((k, v) -> {
	                logger.debug("Closing Graph instance [{}]", k);
	                try {
	                    v.close();
	                } catch (Exception ex) {
	                    logger.warn(String.format("Exception while closing Graph instance [%s]", k), ex);
	                } finally {
	                    logger.info("Closed Graph instance [{}]", k);
	                }
	            });

	            logger.info("Gremlin Server - shutdown complete");
	            serverStopped.complete(null);
	        }, SERVER_THREAD_PREFIX + "stop").start();

	        return serverStopped;
	    }

	    public static GremlinServerCustom start(final String[] args) throws Exception {
	        // add to vm options: -Dlog4j.configuration=file:conf/log4j.properties
	        printHeader();
	        final String file;
	        if (args.length > 0)
	            file = args[0];
	        else
	            file = "conf/gremlin-server.yaml";

	        final Settings settings;
	        try {
	            settings = Settings.read(file);
	        } catch (Exception ex) {
	            logger.error("Configuration file at {} could not be found or parsed properly. [{}]", file, ex.getMessage());
	            return null;
	        }

	        logger.info("Configuring Gremlin Server from {}", file);
	        settings.optionalMetrics().ifPresent(GremlinServerCustom::configureMetrics);
	        final GremlinServerCustom server = new GremlinServerCustom(settings);
	        server.start().exceptionally(t -> {
	            logger.error("Gremlin Server was unable to start and will now begin shutdown: {}", t.getMessage());
	            server.stop().join();
	            return null;
	        }).join();
	        return server;
	    }

	    public static String getHeader() {
	        final StringBuilder builder = new StringBuilder();
	        builder.append("\r\n");
	        builder.append("         \\,,,/\r\n");
	        builder.append("         (o o)\r\n");
	        builder.append("-----oOOo-(3)-oOOo-----\r\n");
	        return builder.toString();
	    }

	    private static void configureMetrics(final Settings.ServerMetrics settings) {
	        final MetricManager metrics = MetricManager.INSTANCE;
	        settings.optionalConsoleReporter().ifPresent(config -> {
	            if (config.enabled) metrics.addConsoleReporter(config.interval);
	        });

	        settings.optionalCsvReporter().ifPresent(config -> {
	            if (config.enabled) metrics.addCsvReporter(config.interval, config.fileName);
	        });

	        settings.optionalJmxReporter().ifPresent(config -> {
	            if (config.enabled) metrics.addJmxReporter(config.domain, config.agentId);
	        });

	        settings.optionalSlf4jReporter().ifPresent(config -> {
	            if (config.enabled) metrics.addSlf4jReporter(config.interval, config.loggerName);
	        });

	        settings.optionalGangliaReporter().ifPresent(config -> {
	            if (config.enabled) {
	                try {
	                    metrics.addGangliaReporter(config.host, config.port,
	                            config.optionalAddressingMode(), config.ttl, config.protocol31, config.hostUUID, config.spoof, config.interval);
	                } catch (IOException ioe) {
	                    logger.warn("Error configuring the Ganglia Reporter.", ioe);
	                }
	            }
	        });

	        settings.optionalGraphiteReporter().ifPresent(config -> {
	            if (config.enabled) metrics.addGraphiteReporter(config.host, config.port, config.prefix, config.interval);
	        });
	    }

	    private static void printHeader() {
	        logger.info(getHeader());
	    }
	    
	    public ServerGremlinExecutor<EventLoopGroup> getServerGremlinExecutor() {
			return serverGremlinExecutor;
		}

}
