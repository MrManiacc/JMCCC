package org.to2mbn.jmccc.mcdownloader;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.to2mbn.jmccc.mcdownloader.download.DownloaderService;
import org.to2mbn.jmccc.mcdownloader.download.HttpAsyncDownloader;
import org.to2mbn.jmccc.mcdownloader.download.JreHttpDownloader;
import org.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.MojangDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;

public class MinecraftDownloaderBuilder {

	public static MinecraftDownloaderBuilder create() {
		return new MinecraftDownloaderBuilder();
	}

	private int maxConnections = 50;
	private int maxConnectionsPerRouter = 10;
	private MinecraftDownloadProvider provider = new MojangDownloadProvider();
	private int poolMaxThreads = Runtime.getRuntime().availableProcessors();
	private long poolThreadLivingTime = 1000 * 10;
	private int defaultTries = 3;
	private int connectTimeout = 10000;
	private int soTimeout = 30000;
	private boolean disableApacheHttpAsyncClient = false;

	protected MinecraftDownloaderBuilder() {
	}

	public MinecraftDownloaderBuilder setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
		return this;
	}

	public MinecraftDownloaderBuilder setMaxConnectionsPerRouter(int maxConnectionsPerRouter) {
		this.maxConnectionsPerRouter = maxConnectionsPerRouter;
		return this;
	}

	public MinecraftDownloaderBuilder setProvider(MinecraftDownloadProvider provider) {
		this.provider = provider;
		return this;
	}

	public MinecraftDownloaderBuilder setPoolMaxThreads(int poolMaxThreads) {
		this.poolMaxThreads = poolMaxThreads;
		return this;
	}

	public MinecraftDownloaderBuilder setPoolThreadLivingTime(long poolThreadLivingTime) {
		this.poolThreadLivingTime = poolThreadLivingTime;
		return this;
	}

	public MinecraftDownloaderBuilder setDefaultTries(int defaultTries) {
		this.defaultTries = defaultTries;
		return this;
	}

	public MinecraftDownloaderBuilder setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	public MinecraftDownloaderBuilder setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
		return this;
	}

	public MinecraftDownloaderBuilder appendProvider(final MinecraftDownloadProvider appendprovider) {
		final MinecraftDownloadProvider prevprovider = provider;
		provider = new MinecraftDownloadProvider() {

			@Override
			public MultipleDownloadTask<RemoteVersionList> versionList() {
				MultipleDownloadTask<RemoteVersionList> t = appendprovider.versionList();
				if (t == null) {
					return prevprovider.versionList();
				}
				return t;
			}

			@Override
			public MultipleDownloadTask<Object> library(MinecraftDirectory mcdir, Library library) {
				MultipleDownloadTask<Object> t = appendprovider.library(mcdir, library);
				if (t == null) {
					return prevprovider.library(mcdir, library);
				}
				return t;
			}

			@Override
			public MultipleDownloadTask<Object> gameVersionJson(MinecraftDirectory mcdir, String version) {
				MultipleDownloadTask<Object> t = appendprovider.gameVersionJson(mcdir, version);
				if (t == null) {
					return prevprovider.gameVersionJson(mcdir, version);
				}
				return t;
			}

			@Override
			public MultipleDownloadTask<Object> gameJar(MinecraftDirectory mcdir, Version version) {
				MultipleDownloadTask<Object> t = appendprovider.gameJar(mcdir, version);
				if (t == null) {
					return prevprovider.gameJar(mcdir, version);
				}
				return t;
			}

			@Override
			public MultipleDownloadTask<Set<Asset>> assetsIndex(MinecraftDirectory mcdir, Version version) {
				MultipleDownloadTask<Set<Asset>> t = appendprovider.assetsIndex(mcdir, version);
				if (t == null) {
					return prevprovider.assetsIndex(mcdir, version);
				}
				return t;
			}

			@Override
			public MultipleDownloadTask<Object> asset(MinecraftDirectory mcdir, Asset asset) {
				MultipleDownloadTask<Object> t = appendprovider.asset(mcdir, asset);
				if (t == null) {
					return prevprovider.asset(mcdir, asset);
				}
				return t;
			}
		};
		return this;
	}

	public MinecraftDownloaderBuilder disableApacheHttpAsyncClient() {
		disableApacheHttpAsyncClient = true;
		return this;
	}

	public MinecraftDownloader build() {
		ExecutorService executor = new ThreadPoolExecutor(poolMaxThreads, poolMaxThreads, poolThreadLivingTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

		DownloaderService downloader;
		if (!disableApacheHttpAsyncClient && isApacheHttpAsyncClientAvailable()) {
			HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClientBuilder.create().setMaxConnTotal(maxConnections).setMaxConnPerRoute(maxConnectionsPerRouter).setDefaultIOReactorConfig(IOReactorConfig.custom().setConnectTimeout(connectTimeout).setSoTimeout(soTimeout).build());
			downloader = new HttpAsyncDownloader(httpClientBuilder, executor);
		} else {
			downloader = new JreHttpDownloader(maxConnections, connectTimeout, soTimeout, poolThreadLivingTime);
		}

		return new MinecraftDownloaderImpl(downloader, executor, provider, defaultTries);
	}

	private static boolean isApacheHttpAsyncClientAvailable() {
		try {
			Class.forName("org.apache.http.impl.nio.client.HttpAsyncClientBuilder");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}
}
