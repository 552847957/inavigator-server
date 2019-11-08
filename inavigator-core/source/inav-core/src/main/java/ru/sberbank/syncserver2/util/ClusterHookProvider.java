package ru.sberbank.syncserver2.util;

import javax.naming.InitialContext;

public class ClusterHookProvider {
	//не используем аннотацию, чтобы не было ошибок при инициализации, если в web.xml не указан <env-entry>
	//@Resource(name="clusterHookSuffix",mappedName="clusterHookSuffix",type=String.class)
	private static String clusterHookSuffix;
	
	private static volatile boolean init = false;
	
	private static final String LOOKUP = "java:comp/env/clusterHookSuffix";
	
	private static volatile InnerSuffixProvider provider = new InnerSuffixProvider() {		
		@Override
		public String getSuffixForHook() {
			if (!init)
				init();
			return clusterHookSuffix;
		}
	};
	
	
	private ClusterHookProvider() {
	}
	
	private static void init() {
		String local = null;
		try{
			// возможно будет NullPointerException, но это не поломает логику
			local = new InitialContext().lookup(LOOKUP).toString().trim();
			if ("".equals(local))
				local = null;			
		} catch (Exception e) { }
		clusterHookSuffix = local;
		init = true;
		// меняем имплиментацию, чтобы в будущем избежать дополнительной проверки на init
		provider = new InnerSuffixProvider() {			
			@Override
			public String getSuffixForHook() {
				return clusterHookSuffix;
			}
		};
	}
	
	public static String getSuffixForHook() {
		return provider.getSuffixForHook();
	}
	
	public static boolean isClusterHooked() {
		return getSuffixForHook() != null ;
	}
	
	private static interface InnerSuffixProvider {
		String getSuffixForHook();
	}

}
