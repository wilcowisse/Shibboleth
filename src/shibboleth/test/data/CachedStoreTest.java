package shibboleth.test.data;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import shibboleth.data.CachedStore;
import shibboleth.data.DataStore;
import shibboleth.data.RepoFilter;
import shibboleth.data.TransparantFilter;
import shibboleth.model.Contribution;
import shibboleth.model.Repo;
import shibboleth.model.User;

public class CachedStoreTest {
	
	@Mock
	DataStore cache;
	
	@Mock
	DataStore source;
	
	private DataStore store;
	private Repo repo1, repo2;
	private User user1, user2;
	private Contribution c1, c2;
	
	@Before
	public void setUp() throws Exception {
		cache = mock(DataStore.class);
		source= mock(DataStore.class);
		store=new CachedStore(cache, source);
		
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
	public void testStoreRepo() {
		store.storeRepo(repo1);
		verify(cache).storeRepo(repo1);
		verify(source, never()).storeRepo(Matchers.any(Repo.class));
	}

	@Test
	public void testStoreUser() {
		store.storeUser(user1);
		verify(cache).storeUser(user1);
		verify(source, never()).storeUser(Matchers.any(User.class));
	}

	@Test
	public void testStoreContribution() {
		store.storeContribution(c1);
		verify(cache).storeContribution(c1);
		verify(source, never()).storeContribution(Matchers.any(Contribution.class));
	}

	@Test
	public void testStoreContributions() {
		Contribution[] cs = {c1,c2};
		store.storeContributions(cs);
		verify(cache).storeContributions(cs);
		verifyZeroInteractions(source);
	}

	@Test
	public void testDeleteRepo() {
		store.deleteRepo("repo");
		verify(cache).deleteRepo("repo");
		//verify(source, never()).deleteRepo(anyString());
		verifyZeroInteractions(source);
	}

	@Test
	public void testDeleteUser() {
		store.deleteUser("user");
		verify(cache).deleteUser("user");
		verifyZeroInteractions(source);
	}

	@Test
	public void testDeleteContributionsByUser() {
		store.deleteContributionsByUser("user");
		verify(cache).deleteContributionsByUser("user");
		verifyZeroInteractions(source);
	}

	@Test
	public void testDeleteContributionsByRepo() {
		store.deleteContributionsByRepo("repo");
		verify(cache).deleteContributionsByRepo("repo");
		verifyZeroInteractions(source);
	}

	@Test
	public void testStoredAllContributionsForRepo() {
		store.storedAllContributionsForRepo("repo", true);
		verify(cache).storedAllContributionsForRepo("repo", true);
		verifyZeroInteractions(source);
	}

	@Test
	public void testStoredAllContributionsByUser() {
		store.storedAllContributionsByUser("user", true);
		verify(cache).storedAllContributionsByUser("user", true);
		verifyZeroInteractions(source);
	}

	@Test
	public void testGetUserFromCache() {
		when(cache.getUser("user1")).thenReturn(user1);
		store.getUser("user1");
		verify(cache).getUser("user1");
		verifyZeroInteractions(source);
	}
	
	@Test
	public void testGetUserFromSource() {
		when(cache.getUser("user1")).thenReturn(null);
		store.getUser("user1");
		verify(cache).getUser("user1");
		verify(source).getUser("user1");
	}

	@Test
	public void testGetRepoFromCache() {
		when(cache.getRepo("repo1")).thenReturn(repo1);
		store.getRepo("repo1");
		verify(cache).getRepo("repo1");
		verifyZeroInteractions(source);
	}

	@Test
	public void testGetContributions() {
		Contribution[] mockResult = {c1, c2};
		when(cache.getContributions("repo", false)).thenReturn(mockResult);
		
		store.getContributions("repo", false);
		
		verify(cache).getContributions("repo", false);
		verifyZeroInteractions(source);
	}
	
	@Test
	public void testGetContributionsFromCache() {
		Contribution[] mockResult = {c1, c2};
		when(cache.getContributions("repo", true)).thenReturn(mockResult);
		
		store.getContributions("repo", true);
		
		verify(cache).getContributions("repo", true);
		verifyZeroInteractions(source);
	}
	
	@Test
	public void testGetContributionsFromSource() {
		Contribution[] mockResult = {c1, c2};
		when(cache.getContributions("repo", true)).thenReturn(null);
		when(source.getContributions("repo", true)).thenReturn(mockResult);
		
		store.getContributions("repo", true);
		
		verify(cache).getContributions("repo", true);
		verify(source).getContributions("repo", true);
		
		verify(cache).storeContributions(mockResult);
		verify(cache).storedAllContributionsForRepo("repo", true);
	}

	@Test
	public void testGetRepos() {
		Repo[] mockResult = {repo1, repo2};
		when(cache.getRepos(eq("user1"), Matchers.any(RepoFilter.class), eq(false)))
		.thenReturn(mockResult)
		.thenReturn(new Repo[]{});
		
		store.getRepos("user1", new TransparantFilter(), false);
		store.getRepos("user1", new TransparantFilter(), false);
		
		verify(cache, times(2)).getRepos(eq("user1"), Matchers.any(RepoFilter.class), eq(false));
		verifyZeroInteractions(source);
	}
	
	@Test
	public void testGetReposFromCache() {
		Repo[] mockResult = {repo1, repo2};
		when(cache.getRepos(eq("user1"), Matchers.any(RepoFilter.class), eq(true)))
		.thenReturn(mockResult);
		
		store.getRepos("user1", new TransparantFilter(), true);
		
		verify(cache).getRepos(eq("user1"), Matchers.any(RepoFilter.class), eq(true));
		verifyZeroInteractions(source);
	}
	
	@Test
	public void testGetReposFromSource() {
		Repo[] mockResult = {repo1, repo2};
		when(cache.getRepos(eq("user1"), Matchers.any(RepoFilter.class), eq(true)))
		.thenReturn(null);
		when(source.getRepos(eq("user1"), Matchers.any(RepoFilter.class), eq(true)))
		.thenReturn(mockResult);
		
		store.getRepos("user1", new TransparantFilter(), true);
		
		verify(cache).getRepos(eq("user1"), Matchers.any(RepoFilter.class), eq(true));
		verify(source).getRepos(eq("user1"), Matchers.any(RepoFilter.class), eq(true));
		
		verify(cache).storeRepo(repo1);
		verify(cache).storeRepo(repo2);
		
		verify(cache).storedAllContributionsByUser("user1", true);
	}

	@Test
	public void testContainsUser() {
		store.containsUser("user");
		verify(cache).containsUser("user");
		verifyZeroInteractions(source);
	}

	@Test
	public void testContainsRepo() {
		store.containsRepo("repo");
		verify(cache).containsRepo("repo");
		verifyZeroInteractions(source);
	}

	@Test
	public void testContainsContribution() {
		store.containsContribution("repo", "user");
		verify(cache).containsContribution("repo", "user");
		verifyZeroInteractions(source);
	}

	@Test
	public void testContainsContributionInfo() {
		store.containsContributionInfo("repo", "user");
		verify(cache).containsContributionInfo("repo", "user");
		verifyZeroInteractions(source);
	}

	@Test
	public void testGetAllContributions() {
		store.getAllContributions();
		verify(cache).getAllContributions();
		verifyZeroInteractions(source);
	}

}
