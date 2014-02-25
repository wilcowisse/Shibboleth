package shibboleth.data.github;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import shibboleth.data.DataSource;
import shibboleth.data.RepoFilter;
import shibboleth.model.Commit;
import shibboleth.model.Committer;
import shibboleth.model.Contribution;
import shibboleth.model.ContributionInfo;
import shibboleth.model.Repo;
import shibboleth.model.SimpleRepo;
import shibboleth.model.SimpleUser;
import shibboleth.model.UnknownUser;
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
	
	public static class RESTCommitInfo{
		
		public static class RESTCommit{
			@Key
			Committer author;
		}
		
		@Key
		public String sha;
		
		@Key
		public RESTCommit commit;
		
		@Key("committer")
		public SimpleUser user;
		
	}
	
	/**
	 * Do a number of http request to the Github REST API. This method takes different pages into account.
	 * @see <a href="http://developer.github.com/v3/#pagination">Github API pagination</a>
	 * @param url URL description of the API location.
	 * @param dataClass Data class type to be returned.
	 * @return A list of <tt>T</tt>s.
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public <T> List<T> doPaginatedRequest(GithubUrl url, Class<T[]> dataClass) throws IOException, HttpResponseException {
		List<T> result = new ArrayList<T>();
		boolean nextpage=true;
		HttpRequest request = requestFactory.buildGetRequest(url);

		while(nextpage){
			nextpage=false;
			
			HttpHeaders headers = new HttpHeaders()
				.set("link", String.format("Link", "<%s>; rel=\"next\"", url.build()));
			request.setHeaders(headers);
			
			HttpResponse response = request.execute();
		 	int limit=Integer.parseInt(response.getHeaders().getFirstHeaderStringValue("X-RateLimit-Limit"));
		 	int remaining = Integer.parseInt(response.getHeaders().getFirstHeaderStringValue("X-RateLimit-Remaining"));
		 	int reset = Integer.parseInt(response.getHeaders().getFirstHeaderStringValue("X-RateLimit-Reset"));
		 	
		 	rateLimit.set(RateLimitValue.LIMIT, limit)
		 		.set(RateLimitValue.REMAINING, remaining)
		 		.set(RateLimitValue.RESET, reset);
		 	
		 	T[] page = response.parseAs(dataClass);
		 	
		 	for(T t : page){
				result.add(t);
			}

		 	if(response.getHeaders().containsKey("link")){
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
	 * @return An instantiated <tt>T</tt>.
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
	public Repo getRepo(String fullRepoName) {
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
			List<Repo> repoList = null;
			try {
				repoList = doPaginatedRequest(url, Repo[].class);
			}catch(HttpResponseException e){
				System.out.println(e.getMessage());
			}catch (IOException e) {
				e.printStackTrace();
			}
		
			return repoList;
		}
		else{
			return  new ArrayList<Repo>();
		}
	}
	
	@Override
	public List<Contribution> getContributions(String fullRepoName, boolean ensureAll){
		GithubUrl url = new GithubUrl("https://api.github.com/repos/"+fullRepoName+"/contributors")
			.withAccessToken(accessToken);
		
		List<RESTContribution> rContributionList= null;
		try {
			rContributionList = doPaginatedRequest(url, RESTContribution[].class);
		}catch(HttpResponseException e){
			System.out.println(e.getMessage());
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		int totalContributionCount = 0;
		for(RESTContribution r : rContributionList){
			totalContributionCount += r.count;
		}
		
		List<Contribution> contributions= new ArrayList<Contribution>();
		for(RESTContribution r : rContributionList){
			SimpleUser user = new SimpleUser(r.login);
			SimpleRepo repo = new SimpleRepo(fullRepoName);
			Contribution c = new Contribution(user, repo);
			
			ContributionInfo info = new ContributionInfo(r.count, (int)((float)r.count/(float)totalContributionCount * 100f));
			c.setContributionInfo(info);
			contributions.add(c);
		}
		
		return contributions;
	}
	
	@Override
	public List<Contribution> getAllContributions() {
		return new ArrayList<Contribution>();
	}
	
	/**
	 * Get forks of given repo
	 * @param fullRepoName The name of the repo.
	 * @return The repo forks
	 */
	public List<Repo> getForks(String fullRepoName){
		GithubUrl url = new GithubUrl("https://api.github.com/repos/"+fullRepoName+"/forks")
			.withAccessToken(accessToken);
		List<Repo> forks = null;
		try {
			forks = doPaginatedRequest(url, Repo[].class);
		}catch(HttpResponseException e){
			System.out.println(e.getMessage());
		}catch (IOException e) {
			e.printStackTrace();
		}
		return forks;
	}
	
	/**
	 * Get commits of a given repo
	 * @param fullRepoName The name of the repo.
	 * @return All commits of the repo.
	 */
	public List<Commit> getCommits(String fullRepoName){
		GithubUrl url = new GithubUrl("https://api.github.com/repos/"+fullRepoName+"/commits")
			.withAccessToken(accessToken);
		List<RESTCommitInfo> commitInfoList = null;
		try {
			commitInfoList = doPaginatedRequest(url, RESTCommitInfo[].class);
		} catch (HttpResponseException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Commit> result = new ArrayList<Commit>(commitInfoList.size());
		for(RESTCommitInfo ci : commitInfoList){
			Commit commit = new Commit();
			commit.sha = ci.sha;
			commit.committer=ci.commit.author;
			if(ci.user.login == null){
				commit.user = UnknownUser.getInstance();
			}
			else {
				commit.user = ci.user;
			}
			commit.committer.repo=fullRepoName;
			result.add(commit);
		}
		
		return result;
		
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
	public static void main(String[] args) {
		RateLimitValue v = new RateLimitValue();
		
		List<Commit> commits = new GithubDataSource(v).getCommits("wilcowisse/Shibboleth");
		System.out.println(commits);
		
		//List<Repo> res = new GithubDataSource(v).getRepos("creationix", new TransparantFilter(), true);
		//System.out.println(res);

	}
	
	
}

