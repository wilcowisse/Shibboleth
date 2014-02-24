package shibboleth.data.github;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shibboleth.data.DataSource;
import shibboleth.data.RepoFilter;
import shibboleth.data.TransparantFilter;
import shibboleth.data.sql.CommitInfoStore;
import shibboleth.model.Contribution;
import shibboleth.model.ContributionInfo;
import shibboleth.model.Repo;
import shibboleth.model.SimpleRepo;
import shibboleth.model.SimpleUser;
import shibboleth.model.User;

/**
 * This class represents a data source on top of the Github REST API.
 * See <a href="developer.github.com/v3/‎">developer.github.com/v3/‎</a>
 * 
 * @author Wilco Wisse
 *
 */
public class GithubDataSource implements DataSource{
	
	private static HttpTransport HTTP_TRANSPORT;
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	private RateLimitValue rateLimit;
	private HttpRequestFactory requestFactory;
	private String accessToken = null;
	
	private Pattern linkHeaderPattern;
	/**
	 * Construct a DataSource on top of the Github API without set an access token.
	 */
	public GithubDataSource(){
		HTTP_TRANSPORT = new NetHttpTransport();
		this.rateLimit=new RateLimitValue();
		init();
	}
	
	/**
	 * Construct a DataSource on top of the Github API without set an access token.
	 * Store rate limit values of the API in <tt>rateLimit</tt>.
	 * @param rateLimit RateLimitValue object to store rate limit values.
	 */
	public GithubDataSource(RateLimitValue rateLimit){
		HTTP_TRANSPORT = new NetHttpTransport();
		this.rateLimit=rateLimit;
		init();
	}
	
	/**
	 * Construct a GithubDatasource with proxy settings
	 * @param rateLimit RateLimitValue object to store rate limit values.
	 * @param proxy Proxy to be used.
	 */
	public GithubDataSource(RateLimitValue rateLimit, Proxy proxy){
		HTTP_TRANSPORT = new NetHttpTransport.Builder()
			.setProxy(proxy)
			.build();
		this.rateLimit=rateLimit;
		init();
	}
	
	
	private void init(){
		requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
			@Override
			public void initialize(HttpRequest request) {
				request.setParser(new JsonObjectParser(JSON_FACTORY));
			}
		});
		
		linkHeaderPattern = Pattern.compile(".*<((?!>).*)>;\\s*rel=\"next\".*");
	}
	
	/**
	 * A Generic URL which points to the Github REST service.
	 * @author Wilco Wisse
	 *
	 */
	public static class GithubUrl extends GenericUrl{
		
		@Key
	    public String access_token = null;
		
		@Key
		public int per_page=100;
		
		
		public GithubUrl(String url){
			super(url);
		}
		
		/**
		 * Access Github API with given access token.
		 * @param token The token.
		 * @return <tt>this</tt>.
		 */
		public GithubUrl withAccessToken(String token){
			this.access_token = token;
			return this;
		}
	}
	
	/**
	 * Data class to be used for the json parser of the Google http client.
	 * @author Wilco Wisse
	 *
	 */
	public static class RESTContribution {
		@Key
		public String login;
		
		@Key("contributions")
		public int count;
	}
	
	
	public <T> List<T> doPaginatedRequest(GithubUrl url, Class<T> dataClass) throws IOException, HttpResponseException {
		List<T> result = new ArrayList<T>();
		boolean nextpage=true;
		HttpRequest request = requestFactory.buildGetRequest(url);

		while(nextpage){
			nextpage=false;
			request.getHeaders().set("Link",String.format("Link", "<%s>; rel=\"next\"", url.build()));
			HttpResponse response = request.execute();
		 	int limit=Integer.parseInt(response.getHeaders().getFirstHeaderStringValue("X-RateLimit-Limit"));
		 	int remaining = Integer.parseInt(response.getHeaders().getFirstHeaderStringValue("X-RateLimit-Remaining"));
		 	int reset = Integer.parseInt(response.getHeaders().getFirstHeaderStringValue("X-RateLimit-Reset"));
		 	
		 	rateLimit.set(RateLimitValue.LIMIT, limit)
		 		.set(RateLimitValue.REMAINING, remaining)
		 		.set(RateLimitValue.RESET, reset);
		 	
		 	T page = response.parseAs(dataClass);
		 	result.add(page);
		 	
		 	if(response.getHeaders().containsKey("Link")){
		 		String linkHeaderVal = response.getHeaders().getFirstHeaderStringValue("Link");
		 		Matcher m = linkHeaderPattern.matcher(linkHeaderVal);
				if (m.find()) {
					request = requestFactory.buildGetRequest(new GithubUrl(m.group(1)));
				    nextpage=true;
				}

		 	}
			
		}

		return result;
	}
	
	
	/**
	 * Do a http request to the Github REST API.
	 * @param url URL description of the API location.
	 * @param dataClass Data class type to be returned.
	 * @return An instantiated object of the provided <tt>dataClass</tt>.
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> T doRequest(GithubUrl url, Class<T> dataClass) throws IOException, HttpResponseException {
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse response = request.execute();
		
	 	int limit=Integer.parseInt(response.getHeaders().getFirstHeaderStringValue("X-RateLimit-Limit"));
	 	int remaining = Integer.parseInt(response.getHeaders().getFirstHeaderStringValue("X-RateLimit-Remaining"));
	 	int reset = Integer.parseInt(response.getHeaders().getFirstHeaderStringValue("X-RateLimit-Reset"));
	 	
	 	rateLimit.set(RateLimitValue.LIMIT,limit)
	 		.set(RateLimitValue.REMAINING,remaining)
	 		.set(RateLimitValue.RESET, reset);
		
		if(!response.isSuccessStatusCode()){
			throw new IOException("Github returned status code "+response.getStatusCode()
					+ " " + response.getStatusMessage());
		}
		
		T result = response.parseAs(dataClass);
		response.disconnect();
		return result;
	}
	
	@Override
	public Repo getRepo(String fullRepoName){
		GithubUrl url = new GithubUrl("https://api.github.com/repos/"+fullRepoName)
			.withAccessToken(accessToken);
		Repo repo = null;
		try{
			repo = doRequest(url, Repo.class);
		}catch(HttpResponseException e){
			System.out.println("HTTP response exception!");
		}catch (IOException e) {
			e.printStackTrace();
		}
		return repo;
	}
	
	@Override
	public User getUser(String userName) {
		GithubUrl url = new GithubUrl("https://api.github.com/users/"+userName)
			.withAccessToken(accessToken);
		User user = null;
		try{
			user = doRequest(url, User.class);
		}catch(HttpResponseException e){
			System.out.println(e.getMessage());
		}catch (IOException e) {
			e.printStackTrace();
		}
		return user;
	}
	
	@Override
	public List<Repo> getRepos(String user, RepoFilter filter, boolean ensureAll) {
		if(ensureAll){
			GithubUrl url = new GithubUrl("https://api.github.com/users/"+user+"/repos")
				.withAccessToken(accessToken);
			Repo[] repos = null;
			try {
				repos = doRequest(url, Repo[].class);
			}catch(HttpResponseException e){
				System.out.println(e.getMessage());
			}catch (IOException e) {
				e.printStackTrace();
			}
			
			if(repos == null){
				return new ArrayList<Repo>();	
			}
			
			List<Repo> repoList = new ArrayList<Repo>();
			for(Repo r : repos){
				if(filter.accepts(r))
					repoList.add(r);
			}
			return repoList;
		}
		else{
			return new ArrayList<Repo>();	
		}
	}
	
	
	//@Override
	public Repo[] getRepos2(String user, RepoFilter filter, boolean ensureAll) {
		if(ensureAll){
			GithubUrl url = new GithubUrl("https://api.github.com/users/"+user+"/repos")
				.withAccessToken(accessToken);
			List<Repo[]> repos = null;
			try {
				repos = doPaginatedRequest(url, Repo[].class);
			}catch(HttpResponseException e){
				System.out.println(e.getMessage());
			}catch (IOException e) {
				e.printStackTrace();
			}
			
			if(repos == null){
				return new Repo[]{};	
			}
			
			List<Repo> repoList = new ArrayList<Repo>();
//			for(Repo r : repos){
//				if(filter.accepts(r))
//					repoList.add(r);
//			}
			return repoList.toArray(new Repo[]{});
		}
		else{
			return new Repo[]{};
		}
	}
	
	@Override
	public List<Contribution> getContributions(String fullRepoName, boolean ensureAll){
		GithubUrl url = new GithubUrl("https://api.github.com/repos/"+fullRepoName+"/contributors")
			.withAccessToken(accessToken);
		
		RESTContribution[] rContributions = null;
		try {
			rContributions = doRequest(url, RESTContribution[].class);
			
			
			
		}catch(HttpResponseException e){
			System.out.println(e.getMessage());
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		if(rContributions==null){
			return new ArrayList<Contribution>();
		}
		
		if(rContributions.length == 100){
			System.err.println("WARNING: full page returned in getContributions for repo "+fullRepoName);
		}
		
		int totalContributionCount = 0;
		for(RESTContribution r : rContributions){
			totalContributionCount += r.count;
		}
		
		List<Contribution> contributions= new ArrayList<Contribution>(rContributions.length);
		for(RESTContribution rc : rContributions){
			SimpleUser u = new SimpleUser(rc.login);
			SimpleRepo r = new SimpleRepo(fullRepoName);
			Contribution c = new Contribution(u, r);
			
			ContributionInfo info = new ContributionInfo(rc.count, (int)((float)rc.count/(float)totalContributionCount * 100f));
			c.setContributionInfo(info);
			contributions.add(c);
		}
		
		return contributions;
	}
	
	@Override
	public List<Contribution> getAllContributions() {
		return new ArrayList<Contribution>();
	}
	
	
	public Repo[] getForks(String fullRepoName){
		GithubUrl url = new GithubUrl("https://api.github.com/repos/"+fullRepoName+"/forks")
			.withAccessToken(accessToken);
		Repo[] forks = null;
		try {
			forks = doRequest(url, Repo[].class);
		}catch(HttpResponseException e){
			System.out.println(e.getMessage());
		}catch (IOException e) {
			e.printStackTrace();
		}
		return forks;
	}
	
	/**
	 * Access Github API with a request token.
	 * See: <a href="https://github.com/blog/1509-personal-api-tokens">
	 * https://github.com/blog/1509-personal-api-tokens</a>
	 * @param token Your personal token.
	 */
	public void setAccessToken(String token){
		this.accessToken = token;
	}

	// manual testing
	public static void main(String[] args){
		
		RateLimitValue v = new RateLimitValue();
		//Repo[] res = new GithubDataSource(v).getRepos("creationix", new TransparantFilter(), true);
		//List<Repo[]> res = 
		
		//System.out.println(Arrays.toString(res));	
		
//		// <url>; rel="next"
//		String pattern = ".*<((?!>).*)>;\\s*rel=\"next\".*";
//		
//		String link = "<url>; rel=\"next\"";
//		link = "<https://api.github.com/user/89353/repos?per_page=3&page=2>; rel=\"next\", <https://api.github.com/user/89353/repos?per_page=3&page=82>; rel=\"last\"";
//		link = "<https://api.github.com/user/89353/repos?per_page=3&page=82>; rel=\"last\", <https://api.github.com/user/89353/repos?per_page=3&page=3>; rel=\"next\", <https://api.github.com/user/89353/repos?per_page=3&page=1>; rel=\"first\", <https://api.github.com/user/89353/repos?per_page=3&page=1>; rel=\"prev\"";
//		link = "<https://api.github.com/user/89353/repos?per_page=3&page=82>; rel=\"last\", <https://api.github.com/user/89353/repos?per_page=3&page=1>; rel=\"first\", <https://api.github.com/user/89353/repos?per_page=3&page=1>; rel=\"prev\"";
//
//		Pattern linkHeaderPattern = Pattern.compile(".*<((?!>).*)>;\\s*rel=\"next\".*");
//		Matcher m = linkHeaderPattern.matcher(link);
//		if (m.find()) {
//		    System.out.println(m.group(1));
//		}
//		else{
//			System.out.println("not found");
//		}
		
		//RateLimitValue v = new RateLimitValue();
		
//		Repo[] res = new GithubREST(v).getRepos("livingston");
//		System.out.println(Arrays.toString(res));	
		
		//User res = new GithubDataSource(v).getUser("livingston");
		//System.out.println(res);
		
//		Repo res =  new GithubREST(v).getRepo("livingston/autoSize");
//		System.out.println(res);
		
//		Repo[] res = new GithubREST(v).getForks("livingston/autoSize");
//		System.out.println(Arrays.toString(res));
		
		//System.out.println(v);
	}

	
	
}

