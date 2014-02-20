package shibboleth.test.data;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import shibboleth.data.CachedStore;
import shibboleth.data.CrawlSource;
import shibboleth.data.DataSource;
import shibboleth.data.RepoFilter;
import shibboleth.data.TransparantFilter;
import shibboleth.data.github.GithubDataSource;
import shibboleth.data.sql.SqlDataStore;
import shibboleth.model.Contribution;
import shibboleth.model.Repo;
import shibboleth.model.User;

public class CrawlSourceTest {
	
	@Mock
	SqlDataStore mysql;
	
	@Mock
	GithubDataSource github;
	
	private CrawlSource crawlSource;
	private Repo repo1, repo2;
	private User user1, user2;
	private Contribution c1, c2;
	
	@Before
	public void setUp() throws Exception {
		mysql = mock(SqlDataStore.class);
		github= mock(GithubDataSource.class);
		crawlSource = new CrawlSource(mysql, github);
		
		repo1 = new Repo();
		repo1.full_name="repo";
		
		repo2 = new Repo();
		repo2.full_name="klaas/repo";
		
		user1 = new User();
		user1.login="user1";
		user2 = new User();
		user2.login="user2";
		
		c1 = new Contribution(user1, repo1);
		c2 = new Contribution(user2, repo1);
	}
	
	@Test
	public void testGetUserFromSource() {
		when(github.getUser("user1")).thenReturn(user1);
		crawlSource.getUser("user1");
		
		verify(mysql, never()).getUser("user1");
		verify(github).getUser("user1");
		verify(mysql).storeUser(user1);
	}

	@Test
	public void testGetRepoFromSource() {
		when(github.getRepo("repo")).thenReturn(repo1);
		crawlSource.getRepo("repo");
		verify(mysql, never()).getRepo("repo");
		verify(github).getRepo("repo");
		verify(mysql).storeRepo(repo1);
	}
	
	
	@Test
	public void testGetContributionsEnsureAll() {
		Contribution[] mockResult = {c1, c2};
		when(github.getContributions("repo", true)).thenReturn(mockResult);
		
		crawlSource.getContributions("repo", true);
		
		verify(mysql, times(1)).storeContributionWithoutInfo(c1);
		verify(mysql, times(1)).storeContributionWithoutInfo(c2);
		verify(mysql).storedAllContributionsForRepo("repo", true);
		verify(github).getContributions("repo", true);
		verify(mysql, never()).getContributions(eq("repo"), anyBoolean());
		
		crawlSource.getContributions("repo", true);
		
		verify(mysql).getContributions("repo", true);
		verifyNoMoreInteractions(github);
		verify(mysql, times(1)).storeContributionWithoutInfo(c1);
		verify(mysql, times(1)).storeContributionWithoutInfo(c2);
		
		crawlSource.getContributions("repo", false);
		verify(mysql).getContributions("repo", false);
		verifyNoMoreInteractions(github);
		verify(mysql, times(1)).storeContributionWithoutInfo(c1);
		verify(mysql, times(1)).storeContributionWithoutInfo(c2);
	}
	
	@Test
	public void testGetContributions() {
		Contribution[] mockResult = {c1, c2};
		when(mysql.getContributions("repo", false)).thenReturn(mockResult);
		
		crawlSource.getContributions("repo", false);
		
		verify(mysql).getContributions("repo", false);
		verifyZeroInteractions(github);
		
		verify(mysql, never()).storeNewContributions(mockResult);
		verify(mysql, never()).storedAllContributionsForRepo("repo", true);
	}

	@Test
	public void testGetRepos() {
		Repo[] mockResult = {repo1, repo2};
		when(mysql.getRepos(eq("user1"), any(RepoFilter.class), eq(false)))
		.thenReturn(mockResult)
		.thenReturn(new Repo[]{});
		
		crawlSource.getRepos("user1", new TransparantFilter(), false);
		crawlSource.getRepos("user1", new TransparantFilter(), false);
		
		verify(mysql, times(2)).getRepos(eq("user1"), any(RepoFilter.class), eq(false));
		verifyZeroInteractions(github);
	}

	
	@Test
	public void testGetReposEnsureAll() {
		Repo[] mockResult = {repo1, repo2};
		
		when(github.getRepos(eq("user1"), any(RepoFilter.class), eq(true)))
		.thenReturn(mockResult);
		
		crawlSource.getRepos("user1", new TransparantFilter(), true);
		
		verify(github).getRepos(eq("user1"), any(RepoFilter.class), eq(true));
		verify(mysql, never()).getRepos(eq("user1"), any(RepoFilter.class), eq(true));
		verify(mysql).storedAllContributionsByUser("user1", true);
		verify(mysql).storeRepo(repo1);
		verify(mysql).storeRepo(repo2);
		verify(mysql, times(2)).storeContributionWithoutInfo(any(Contribution.class));
		
		crawlSource.getRepos("user1", new TransparantFilter(), true);
		verifyNoMoreInteractions(github);
		verify(mysql).getRepos(eq("user1"), any(RepoFilter.class), eq(true));
		
		crawlSource.getContributions("repo", false);
		verify(mysql).getContributions("repo", false);
		verifyNoMoreInteractions(github);
	}

	@Test
	public void testGetAllContributions() {
		crawlSource.getAllContributions();
		verify(mysql).getAllContributions();
		verifyZeroInteractions(github);
	}

}
