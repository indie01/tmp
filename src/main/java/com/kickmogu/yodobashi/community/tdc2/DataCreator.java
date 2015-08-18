package com.kickmogu.yodobashi.community.tdc2;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kickmogu.hadoop.mapreduce.job.util.ThreadsafeOptionBuilder;
import com.kickmogu.lib.core.id.IDGenerator;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseDirectOperations;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.lib.solr.SolrContainer;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.meta.SolrSchemaMeta;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.domain.BaseWithTimestampDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageUploadResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.tdc2.TestDataCreators.DataCreateContext;

@SuppressWarnings("rawtypes")
public abstract class DataCreator<T, I extends TestId.Target> {
	
	private static Log LOG = LogFactory.getLog(DataCreator.class);
	
	private static final Date CURRENT_DATE = new Date();
	
	private static Random random;
	
	static  ApplicationContext APPLICATION_CONTEXT;

	
	public synchronized static ApplicationContext getApplicationContext() {
		if (APPLICATION_CONTEXT != null ) return APPLICATION_CONTEXT;
		System.setProperty("hbase.skip.locking", "true");
		APPLICATION_CONTEXT = new ClassPathXmlApplicationContext("serviceContext.xml");
		return APPLICATION_CONTEXT;
	}
	
	static final List<String> SKU_LIST = Lists.newArrayList(
		"100000001001377998",
		"100000009000738581",
		"100000001001391025",
		"100000001000827415",
		"100000001000624829",
		"100000001001342958",
		"100000001001251318",
		"100000001001182574",
		"100000001001260141",
		"100000001001312388"
	);

	@Autowired @Qualifier("MySite")
	HBaseContainer hBaseContainer;
	
	@Autowired
	HBaseDirectOperations hbaseDirectOperations;
	
	@Autowired @Qualifier("MySite")
	SolrContainer solrContainer;
	
	@Autowired @Qualifier("default")
	SolrOperations solrOperations;
	
	HBaseTableMeta hBaseTableMeta;
	
	SolrSchemaMeta solrSchemaMeta;
	
	@Autowired
	ResourceConfig resourceConfig;
	

	@Autowired @Qualifier("default")
	 IDGenerator<String> idGenerator;
	
//	private byte[] communityUserProfileImage;
//	
//	private byte[] communityUserProfileThumbnailImage;
//	
//	private byte[] productImage;
//	
//	private byte[] productThumbnailImage;
	
	double countPerCommunityUser;
	
	int communityUserNum;

	byte[] getCommunityUserProfileImage() {
		return new String("dummyImage").getBytes();
//		if (communityUserProfileImage != null) return communityUserProfileImage;
//		try {
//			communityUserProfileImage = FileUtils.readFileToByteArray(new File("\\\\192.168.101.101\\public\\data\\testData\\communityUserProfile.png"));
//			return communityUserProfileImage;
//		} catch (Throwable e) {
//			throw new CommonSystemException(e);
//		}
	}
	
	byte[] getCommunityUserProfileThumbnailImage() {
		return new String("dummyImage").getBytes();
//		if (communityUserProfileThumbnailImage != null) return communityUserProfileThumbnailImage;
//		try {
//			communityUserProfileThumbnailImage = FileUtils.readFileToByteArray(new File("\\\\192.168.101.101\\public\\data\\testData\\communityUserProfileThumbnail.png"));
//			return communityUserProfileThumbnailImage;
//		} catch (Throwable e) {
//			throw new CommonSystemException(e);
//		}
	}
	
	byte[] getProductImage() {
		return new String("dummyImage").getBytes();
//		if (productImage != null) return productImage;
//		try {
//			productImage = FileUtils.readFileToByteArray(new File("\\\\192.168.101.101\\public\\data\\testData\\product.png"));
//			return productImage;
//		} catch (Throwable e) {
//			throw new CommonSystemException(e);
//		}
	}
	
	byte[] getProductThumbnailImage() {
		return new String("dummyImage").getBytes();
//		if (productThumbnailImage != null) return productThumbnailImage;
//		try {
//			productThumbnailImage = FileUtils.readFileToByteArray(new File("\\\\192.168.101.101\\public\\data\\testData\\productThumbnail.png"));
//			return productThumbnailImage;
//		} catch (Throwable e) {
//			throw new CommonSystemException(e);
//		}
	}
	
	
	public abstract void fillItems(T object, DataCreateContext dataCreateContext);
	
	Class<T> type;
	Class<TestId.Target> idType;
	protected int hbaseBulkSize = 1000;
	protected int solrBulkSize = 100;
	protected ApplicationContext applicationContext;
	
	@SuppressWarnings("unchecked")
	public DataCreator() {
		this.applicationContext = getApplicationContext();
		this.applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(this,
				AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		this.type = ((Class<T>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
		this.idType = (Class<TestId.Target>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
		this.hBaseTableMeta = hBaseContainer.getMeta().getTableMeta(type);
		this.solrSchemaMeta = this.solrContainer.getMeta().getSchemaMeta(type);
		Asserts.notNull(this.idType);
		Asserts.notNull(this.hBaseTableMeta);
	}
	
	protected void beforeCreate(DataCreateContext dataCreateContext){
		
	}
	
	// SKUを外部ファイルから読み込む
	IdTable skuIdTable = new IdTable(TestId.DIRECTORY + File.separator +  "sku_list.csv");
	String getSku(int counter) {
		return skuIdTable.getRecord(counter)[0];
	}
	
	String getOtherSku(int counter) {
		return skuIdTable.getOther(counter)[0];
	}

	
	public static void main(String[] args) throws Throwable {
		try {
			mainInternal(args);
			System.exit(0);
		} catch (Throwable th) {
			th.printStackTrace();
			System.exit(1);
		}
	}

	
	@SuppressWarnings({"unchecked" })
	private static void mainInternal(String[] args) throws Throwable {
		Options options = new Options();
		options.addOption(
			new ThreadsafeOptionBuilder().withArgName("table")
		    .hasArg(true)
		    .isRequired(true)
		   
		    .withLongOpt("table")
		    .create("table")
	    );
		options.addOption(
				new ThreadsafeOptionBuilder().withArgName("userCount")
			    .hasArgs()
			    .isRequired(true)
			    .withDescription("userCount")
			    .create("userCount")
		);
		options.addOption(
			new ThreadsafeOptionBuilder().withArgName("idType")
		    .hasArgs()
		    .isRequired(false)
		    .withDescription("idType")
		    .withLongOpt("idType")
		    .create("idType")
	    );
		options.addOption(
				new ThreadsafeOptionBuilder().withArgName("hbaseBulkSize")
			    .hasArgs()
			    .isRequired(false)
			    .withDescription("hbaseBulkSize")
			    .withLongOpt("hbaseBulkSize")
			    .create("hbaseBulkSize")
		    );
		
		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = null;
        try {
        	commandLine = parser.parse(options, args);
        } catch (ParseException e) {
        	argError(options);
        }
        
        

        
        Map<HBaseTableMeta,List<DataCreator>> creatorMap = Maps.newTreeMap(new Comparator(){

			@Override
			public int compare(Object o1, Object o2) {
				int order1 = ((HBaseTableMeta)o1).getSizeGroup().getCode();
				int order2 = ((HBaseTableMeta)o2).getSizeGroup().getCode();
				int order = order1 - order2;
				if (order != 0) return order;
				return ((HBaseTableMeta)o1).getTableName().compareTo( ((HBaseTableMeta)o2).getTableName());
			}
        	
        });

    	HBaseContainer hBaseContainer = getApplicationContext().getBeansOfType(HBaseContainer.class).values().iterator().next();

    	int userCount = Integer.valueOf(commandLine.getOptionValue("userCount"));
    	Asserts.isTrue(userCount > 0);
    	
        for (String tableName:commandLine.getOptionValue("table").split(",")) {
        	System.out.println(tableName);
        	HBaseTableMeta tableMeta = hBaseContainer.getMeta().getTableMetaBySimpleClassName(tableName.trim());
        	Asserts.notNull(tableMeta, tableName);
        	Asserts.isFalse(creatorMap.containsKey(tableMeta));
    		List<DataCreator> creators = Lists.newArrayList();
        	for (Class dataCreatorClass:getDataCreatorClasses(tableMeta.getType())) {
        		DataCreator dataCreator = (DataCreator)dataCreatorClass.newInstance();
        		dataCreator.communityUserNum = userCount;
        		if (commandLine.hasOption("hbaseBulkSize")) {
        			dataCreator.hbaseBulkSize = Integer.valueOf(commandLine.getOptionValue("hbaseBulkSize"));
        		}
        		creators.add(dataCreator);
        	}
        	Asserts.isFalse(creators.isEmpty());
        	creatorMap.put(tableMeta, creators);
        }
        
        Asserts.isTrue(creatorMap.size()>0);
       
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 2,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        
        for (List<DataCreator> dataCreators:creatorMap.values()) {
        	for (DataCreator dataCreator:dataCreators) {
        		dataCreator.createHBaseAndSolr(threadPoolExecutor);
        	}
        }
        waitForEndingTasks(threadPoolExecutor);

	}
	
	private static void waitForEndingTasks(ThreadPoolExecutor threadPoolExecutor) {

		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e1) {
		}
		while (true) {
			if (threadPoolExecutor.getActiveCount() == 0) break;
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
			}
		}
		threadPoolExecutor.shutdown();
	}

	
	public void createHBaseAndSolr(ThreadPoolExecutor threadPoolExecutor) {
		CreateCount createCount = this.getClass().getAnnotation(CreateCount.class);
		this.countPerCommunityUser = createCount.perCommunityUser();
//
//		Asserts.isTrue(this.countPerCommunityUser > 0);

		final int num = (int)((this.countPerCommunityUser != CreateCount.UNDEFINED) ? communityUserNum*countPerCommunityUser : createCount.fixed());


		final DataCreator me = this;
		
		final HBaseProducerConsumer<List<T>> hbaseProducerConsumer = new HBaseProducerConsumer<List<T>>(this, createDataCreateContext(num, communityUserNum), num, 1, 2, hbaseBulkSize);
		threadPoolExecutor.submit(new Runnable() {
			@Override
			public void run() {
				System.out.println("startHBase");
				LOG.info("startHBaseDataCreate:" + me.getClass().getSimpleName()+" count=" + num);
				hbaseProducerConsumer.start();
				hbaseProducerConsumer.waitToFinish();
				LOG.info("endHBaseDataCreate:" + me.getClass().getSimpleName()+" count=" + num);
			}
		});

		if (solrSchemaMeta == null) return;
		
		final SolrProducerConsumer<List<T>> solrProducerConsumer = new SolrProducerConsumer<List<T>>(this, createDataCreateContext(num, communityUserNum), num, 1, 2, solrBulkSize);
		
		threadPoolExecutor.submit(new Runnable() {
			@Override
			public void run() {
				System.out.println("startSolr");
				LOG.info("startSolrDataCreate:" + this.getClass().getSimpleName()+" count=" + num);
				solrProducerConsumer.start();
				solrProducerConsumer.waitToFinish();
				solrOperations.optimize(solrSchemaMeta.getType());
				LOG.info("endSolrDataCreate:" + this.getClass().getSimpleName()+" count=" + num);
			}
		});

	}
	
	private DataCreateContext createDataCreateContext(int num, int communityUserNum) {
		DataCreateContext dataCreateContext = new DataCreateContext();
		dataCreateContext.idType = this.idType;
		dataCreateContext.num = num;
		dataCreateContext.communityUserNum = communityUserNum;
		dataCreateContext.readIdList(idType, num);
		beforeCreate(dataCreateContext);
		dataCreateContext.readIdList(TestId.CommunityUserId.class, (int)(num/countPerCommunityUser));
		return dataCreateContext;
	}
	
	
	
	static abstract class CreateDataProducerConsumer<T> extends ProducerConsumer<List<T>> {
		
		DataCreateContext dataCreateContext;
		DataCreator dataCreator;
		int num;
		int currentCount;
		int bulkSize;
		
		CreateDataProducerConsumer(DataCreator dataCreator, DataCreateContext dataCreateContext, int num, int producerNum, int consumerNum, int bulkSize) {
			super(producerNum, consumerNum);
			this.num = num;
			this.dataCreator = dataCreator;
			this.dataCreateContext = dataCreateContext;
			this.bulkSize = bulkSize;
		}

		
		@SuppressWarnings("unchecked")
		@Override
		public List<T> produce(int threadNo, int count) {
			List<T> list = Lists.newArrayList();

			for (int i = 1 ; i <= bulkSize ; i++) {
				
				if (currentCount>=num) break;
				dataCreateContext.currentTime = CURRENT_DATE;
				dataCreateContext.currentCount = currentCount;
				
				dataCreateContext.currentCountAsString = new DecimalFormat("00000000").format(currentCount);
				T object = ((T)dataCreator.hBaseTableMeta.createObject());
				
				if (object instanceof BaseWithTimestampDO) {
					BaseWithTimestampDO timestampDO = (BaseWithTimestampDO)object;
					timestampDO.setRegisterDateTime(dataCreateContext.currentTime);
					timestampDO.setModifyDateTime(dataCreateContext.currentTime);
				}
				dataCreator.fillItems(object, dataCreateContext);
				list.add(object);
				currentCount++;
			}
			return list.isEmpty() ? null : list;
		}
	}
	

	
	static class HBaseProducerConsumer<T> extends CreateDataProducerConsumer<T> {

		AtomicInteger total = new AtomicInteger(0);
		
		HBaseProducerConsumer(DataCreator dataCreator,
				DataCreateContext dataCreateContext, int num, int producerNum,
				int consumerNum, int bulkSize) {
			super(dataCreator, dataCreateContext, num, producerNum, consumerNum, bulkSize);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void consume(List<T> list, int threadNo) {
			dataCreator.hbaseDirectOperations.batchObjects(dataCreator.type, list);
			LOG.info("hbaseConsumed "+dataCreator.getClass().getSimpleName()+ " " + total.addAndGet(list.size()) + "/" + num);
		}
		
	}
	
	static class SolrProducerConsumer<T> extends CreateDataProducerConsumer<T> {

		AtomicInteger total = new AtomicInteger(0);

		SolrProducerConsumer(DataCreator dataCreator,
				DataCreateContext dataCreateContext, int num, int producerNum,
				int consumerNum,int bulkSize) {
			super(dataCreator, dataCreateContext, num, producerNum, consumerNum, bulkSize);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void consume(List<T> list, int threadNo) {
			dataCreator.solrOperations.save(dataCreator.type, list);
			LOG.info("solrConsumed "+dataCreator.getClass().getSimpleName()+ " " + total.addAndGet(list.size()) + "/" + num);
		}
		
	}
	
	private static List<Class> getDataCreatorClasses(Class type) {
		List<Class> creatorClasses = Lists.newArrayList();
		for (Class creatorClass:TestDataCreators.class.getClasses()) {
			Class doClass = (Class)((ParameterizedType)creatorClass.getGenericSuperclass()).getActualTypeArguments()[0];
			if (type.equals(doClass)) {
				creatorClasses.add(creatorClass);
			}
		}
		return creatorClasses;
	}
	
	private static void argError(Options options) {
        HelpFormatter help = new HelpFormatter();
        help.setWidth(120);
        help.printHelp(DataCreator.class.getSimpleName(), options, true);
        System.exit(1);
	}
	
	protected String getImageUrl(String imageId, String mimeType, PostContentType postContentType, boolean isThum) {
		
		String dir = resourceConfig.imageUrl + "/test/";
		if (!isThum) {
			if (postContentType.equals(PostContentType.PROFILE)) return dir+"communityUserProfile.png";
			else return dir+"product.png";
		} else {
			if (postContentType.equals(PostContentType.PROFILE)) return dir+"communityUserProfileThumbnail.png";
			else return dir+"productThumbnail.png";
		}
		
		
//本来の処理		
//		String ext = mimeType.substring(mimeType.lastIndexOf("/") + 1);
//		String remoteTargetDirectory = (resourceConfig.imageUploadPath
//				+ "/" + postContentType.getCode() + randomPath()).replace("//", "/");
//		String remoteFileName = imageId + "." + ext;
//		return resourceConfig.imageUrl + remoteTargetDirectory + "/" + remoteFileName;
	}


	static {
		try {
			random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		} catch (Exception e) {
			random = new Random();
		}
	}
	
	@SuppressWarnings({ "unchecked", "hiding" })
	public <T> T createObjectWithId(Class<T> type, String id) {
		HBaseTableMeta tableMeta =hBaseContainer.getMeta().getTableMeta(type);
		T obj = (T)tableMeta.createObject();
		tableMeta.getKeyMeta().setValueAsObject(obj, id);
		return obj;
	}
	
	protected String randomPath() {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			buffer.append("/");
			buffer.append(getRandomChar());
			buffer.append(getRandomChar());
		}
		return buffer.toString();
	}
	

	/**
	 * ランダムなアルファベットを返します。
	 * @return ランダムなアルファベット
	 */
	private char getRandomChar() {
		int i = random.nextInt(36);
		if (i < 10) {
			return (char) (i + 48);
		} else {
			return (char) (i + 55);
		}
	}
	
	protected void fillImageHeaderCommonItems(ImageHeaderDO object, DataCreateContext dataCreateContext) {
		object.setStatus(ContentsStatus.SUBMITTED);
		object.setListViewFlag(true);
		object.setWidth(210);
		object.setHeigth(210);
		object.setThumbnail(false);
		object.setPostDate(dataCreateContext.currentTime);
		object.setImageUploadResult(ImageUploadResult.SUCCESS);
	}
	
	protected void fillThumbnailImageHeaderCommonItems(ImageHeaderDO object, DataCreateContext dataCreateContext) {
		object.setStatus(ContentsStatus.SUBMITTED);
		object.setListViewFlag(false);
		object.setWidth(50);
		object.setHeigth(50);
		object.setThumbnail(true);
		object.setPostDate(dataCreateContext.currentTime);
		object.setImageUploadResult(ImageUploadResult.SUCCESS);
	}

}
