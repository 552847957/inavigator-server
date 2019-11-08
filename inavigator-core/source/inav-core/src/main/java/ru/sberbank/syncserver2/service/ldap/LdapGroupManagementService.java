package ru.sberbank.syncserver2.service.ldap;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import ru.sberbank.syncserver2.service.core.AbstractService;

/**
 * Сервис для работы с группами LDAP
 * @author sbt-gordienko-mv
 *
 */
public class LdapGroupManagementService extends AbstractService {
	
	private static final String SECURITY_AUTHENTIFICATION = "simple";
	private static final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	
	private String provider;
	private String domain;
	private Properties settings;
	
	public LdapGroupManagementService() {
		super();
	}

	/**
	 * Получить содержимое настроек в пропертях
	 * @return
	 */
	public String getSettings() {
		return 
				settings.getProperty("username")  + "//" + 
				settings.getProperty("password")  + "//" +
				settings.getProperty("iNavigatorGroupLdapDN")  + "//" + 
				settings.getProperty("base_ctx");
	}

	/**
	 * Изменить группу настроек
	 * @param settings
	 */
	public void setSettings(String settings) {
		this.settings = new Properties();
		try {
			String settingsArray[] = settings.split("//");
			this.settings.put("username", settingsArray[0]);
			this.settings.put("password", settingsArray[1]);
			this.settings.put("iNavigatorGroupLdapDN", settingsArray[2]);
			this.settings.put("base_ctx", settingsArray[3]);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * Получить список аттрибутов пользователя по EMAIL
	 * @param ctx
	 * @param email
	 * @return
	 * @throws Exception
	 */
	private NamingEnumeration<?> getUserAttributesByEmail(DirContext ctx,String email) throws Exception {
	
		SearchControls searchCtls = new SearchControls();
        searchCtls.setReturningAttributes(new String[] {"memberOf","cn"});
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        
		NamingEnumeration<?> results = ctx.search(settings.getProperty("base_ctx"),MessageFormat.format( "mail={0}", email),searchCtls);
        return results;
		
	}
	
	/**
	 * Подключение к LDAP
	 * @return
	 * @throws Exception
	 */
	private DirContext connectToLdap() throws Exception {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY,INITIAL_CONTEXT_FACTORY);
		env.put(Context.SECURITY_AUTHENTICATION, SECURITY_AUTHENTIFICATION);
		env.put(Context.SECURITY_PRINCIPAL, domain + "\\" + settings.getProperty("username"));
		env.put(Context.SECURITY_CREDENTIALS, settings.getProperty("password"));

		Exception exception = null;
		
		String[] hosts = provider.split(";");
		for(String host:hosts) {
			try {
				env.put(Context.PROVIDER_URL, host);
				DirContext ctx = new InitialDirContext(env);
				return ctx;
			} catch (Exception ex) {
				exception = ex;
			}
		}
		if (exception != null)
			throw exception;
		
		return null;
	}
	
	/**
	 * Проверка входит ли пользователь в группу навигатора
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public boolean hasInavUserGroup(String email) throws Exception {
		boolean result =  false;
		DirContext ctx = connectToLdap();
		NamingEnumeration<?> userAttributes = getUserAttributesByEmail(ctx,email);
        while(userAttributes.hasMoreElements()) {
        	 SearchResult sr = (SearchResult)userAttributes.next();
        	NamingEnumeration<?> results1 = sr.getAttributes().get("memberOf").getAll(); 
        	while(results1.hasMore()) {
        		if (results1.next().equals(settings.getProperty("iNavigatorGroupLdapDN")))
        			result = true;
        	}
        }
        return result;
	}
	
	/**
	 * Добавление пользователя в группу навигатора
	 * @param email
	 * @throws Exception
	 */
	public void addInavGroupToUser(String email) throws Exception {
		DirContext ctx = connectToLdap();
		NamingEnumeration<?> userAttributes = getUserAttributesByEmail(ctx,email);
		String userDn = null;
        while(userAttributes.hasMoreElements()) {
        	SearchResult sr = (SearchResult)userAttributes.next();
        	userDn = sr.getNameInNamespace();
        }
        
        // далее происходит добавление пользователя в группу
        ModificationItem[] mods = new ModificationItem[1];
        Attribute mod =new BasicAttribute("member",userDn);
        mods[0] =new ModificationItem(DirContext.ADD_ATTRIBUTE, mod);
        ctx.modifyAttributes(settings.getProperty("iNavigatorGroupLdapDN"), mods);        
	}

	@Override
	protected void doStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void waitUntilStopped() {
		// TODO Auto-generated method stub
		
	}
}
