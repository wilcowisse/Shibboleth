package shibboleth.test.data;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import static org.mockito.Matchers.*;

import org.junit.Before;
import org.junit.Test;

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
		verify(source, never()).storeRepo(any(Repo.class));
	}

	@Test
	public void testStoreUser() {
		store.storeUser(user1);
		verify(cache).storeUser(user1);
		verify(source, never()).storeUser(any(User.class));
	}

	@Test
	public void testStoreContribution() {
		store.storeContribution(c1);
		verify(cache).storeContribution(c1);
		verify(source, never()).storeContribution(any(Contribution.class));
	}

	@Test
	public void testStoreContributions() {
		List<Contribution> cs = Arrays.asList(new Contribution[]{c1, c2});
		store.storeNewContributions(cs);
		verify(cache).storeNewContributions(cs);
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
		when(source.getUser("user1")).thenReturn(user1);
		store.getUser("user1");
		verify(cache).getUser("user1");
		verify(source).getUser("user1");
		verify(cache).storeUser(user1);
	}

	@Test
	public void testGetRepoFromCache() {
		when(cache.getRepo("repo1")).thenReturn(repo1);
		store.getRepo("repo1");
		verify(cache).getRepo("repo1");
		verifyZeroInteractions(source);
	}

	@Test
	public void testGetRepoFromSource() {
		when(cache.getRepo("repo")).thenReturn(null);
		when(source.getRepo("repo")).thenReturn(repo1);
		store.getRepo("repo");
		verify(cache).getRepo("repo");
		verify(source).getRepo("repo");
		verify(cache).storeRepo(repo1);
	}
	
	@Test
	public void testGetContributions() {
		List<Contribution> mockResult = Arrays.asList(new Contribution[]{c1, c2});
		when(cache.getContributions("repo", false)).thenReturn(mockResult);
		
		store.getContributions("repo", false);
		
		verify(cache).getContributions("repo", false);
		verifyZeroInteractions(source);
	}
	
	@Test
	public void testGetContributionsFromCache() {
		List<Contribution> mockResult = Arrays.asList(new Contribution[]{c1, c2});
		when(cache.getContributions("repo", true)).thenReturn(mockResult);
		
		store.getContributions("repo", true);
		
		verify(cache).getContributions("repo", true);
		verifyZeroInteractions(source);
	}
	
	@Test
	public void testGetContributionsFromSource() {
		List<Contribution> mockResult = Arrays.asList(new Contribution[]{c1, c2});
		when(cache.getContributions("repo", true)).thenReturn(null);
		when(source.getContributions("repo", true)).thenReturn(mockResult);
		
		store.getContributions("repo", true);
		
		verify(cache).getContributions("repo", true);
		verify(source).getContributions("repo", true);
		
		verify(cache).storeNewContributions(mockResult);
		verify(cache).storedAllContributionsForRepo("repo", true);
	}

	@Test
	public void testGetRepos() {
		List<Repo> mockResult = Arrays.asList(new Repo[]{repo1, repo2});
		when(cache.getRepos(eq("user1"), any(RepoFilter.class), eq(false)))
		.thenReturn(mockResult)
		.thenReturn(new ArrayList<Repo>());
		
		store.getRepos("user1", new TransparantFilter(), false);
		store.getRepos("user1", new TransparantFilter(), false);
		
		verify(cache, times(2)).getRepos(eq("user1"), any(RepoFilter.class), eq(false));
		verifyZeroInteractions(source);
	}
	
	@Test
	public void testGetReposFromCache() {
		List<Repo> mockResult = Arrays.asList(new Repo[]{repo1, repo2});
		when(cache.getRepos(eq("user1"), any(RepoFilter.class), eq(true)))
		.thenReturn(mockResult);
		
		store.getRepos("user1", new TransparantFilter(), true);
		
		verify(cache).getRepos(eq("user1"), any(RepoFilter.class), eq(true));
		verifyZeroInteractions(source);
	}
	
	@Test
	public void testGetReposFromSource() {
		Repo[] mockResultA = {repo1, repo2};
		List<Repo> mockResult = Arrays.asList(mockResultA);
		
		when(cache.getRepos(eq("user1"), any(RepoFilter.class), eq(true)))
		.thenReturn(null);
		when(source.getRepos(eq("user1"), any(RepoFilter.class), eq(true)))
		.thenReturn(mockResult);
		
		store.getRepos("user1", new TransparantFilter(), true);
		
		verify(cache).getRepos(eq("user1"), any(RepoFilter.class), eq(true));
		verify(source).getRepos(eq("user1"), any(RepoFilter.class), eq(true));
		
		verify(cache).storeRepo(repo1);
		verify(cache).storeRepo(repo2);
		
		verify(cache).storedAllContributionsByUser("user1", true);
	}

	@Test
	public void testGetAllContributions() {
		store.getAllContributions();
		verify(cache).getAllContributions();
		verifyZeroInteractions(source);
	}

}
