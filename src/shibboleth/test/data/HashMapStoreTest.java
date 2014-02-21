package shibboleth.test.data;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import shibboleth.data.DataStore;
import shibboleth.data.HashMapStore;
import shibboleth.data.TransparantFilter;
import shibboleth.model.Contribution;
import shibboleth.model.Repo;
import shibboleth.model.SimpleRepo;
import shibboleth.model.SimpleUser;
import shibboleth.model.User;

public class HashMapStoreTest {

	private DataStore store;
	private User kees, jan, piet, henk;
	private SimpleUser simpleU1, simpleU2;
	private Repo keesR1, keesR2, janR1, pietR1, henkR1;
	private SimpleRepo simpleR1, simpleR2;
	private Contribution keesR1_kees, keesR1_jan, keesR1_henk;
	private Contribution keesR2_kees;
	private Contribution janR1_jan;
	private Contribution pietR1_piet, pietR1_jan;
	private Contribution janR1_simpleU1;
	
	@Before
	public void setUp() throws Exception {
		store = new HashMapStore();
		kees = new User();
		kees.login="kees";
		jan = new User();
		jan.login="jan";
		piet=new User();
		piet.login="piet";
		henk=new User();
		henk.login="henk";
		
		simpleU1=new SimpleUser("simpleU1");
		simpleU2=new SimpleUser("simpleU2");
		
		keesR1 = new Repo();
		keesR1.full_name="kees/R1";
		keesR2 = new Repo();
		keesR2.full_name="kees/R2";
		
		janR1 = new Repo();
		janR1.full_name="jan/R1";
		
		pietR1 = new Repo();
		pietR1.full_name="piet/R1";
		
		henkR1 = new Repo();
		henkR1.full_name="henk/R1";
		
		simpleR1 = new SimpleRepo();
		simpleR1.full_name="simple/R1";
		simpleR2 = new SimpleRepo();
		simpleR2.full_name="simple/R2";
				
		keesR1_kees = new Contribution(kees, keesR1);
		keesR2_kees = new Contribution(kees, keesR2);
		keesR1_jan  = new Contribution(jan, keesR1);
		keesR1_henk = new Contribution(henk, keesR1);
		janR1_jan   = new Contribution(jan, janR1);
		pietR1_piet = new Contribution(piet, pietR1);
		pietR1_jan  = new Contribution(jan, pietR1);
		janR1_simpleU1 = new Contribution(simpleU1, janR1);
	}

	@Test
	public void testGetUser() {
		store.storeUser(kees);
		assertThat(store.getUser("kees"), is(equalTo(kees)));
	}
	
	@Test
	public void testGetRepo() {
		store.storeRepo(keesR1);
		assertThat(store.getRepo("kees/R1"), is(equalTo(keesR1)));
	}
	
	@Test
	public void testStoreContribution() {
		store.storeContribution(janR1_jan);
		
		List<Contribution> cs = Arrays.asList(store.getContributions("jan/R1", false));
		assertThat(cs.contains(janR1_jan), is(true));
		
		List<Repo> repos = Arrays.asList(store.getRepos("jan", new TransparantFilter(), false));
		
		assertThat(repos.contains(janR1), is(false));
		
		store.storeRepo(janR1);
		

		repos = Arrays.asList(store.getRepos("jan", new TransparantFilter(), false));
		assertThat(repos.contains(janR1), is(true));
	}
	
	@Test
	public void storeContributions() {
		store.storeContribution(janR1_jan);
		store.storeRepo(janR1);
		store.storeRepo(keesR1);
		
		Contribution[] cs = new Contribution[]{janR1_jan, keesR1_henk};
		store.storeNewContributions(cs);
		assertThat(store.getRepos("jan", new TransparantFilter(), false).length, is(equalTo(1)));
	}
	
	@Test
	public void testGetReposWithoutStoredAllContributions() {
		Contribution[] cs = {keesR2_kees, keesR1_kees};
		store.storeNewContributions(cs);
		store.storeRepo(keesR1);
		store.storeRepo(keesR2);
				
		List<Repo> repos = Arrays.asList(store.getRepos("kees", new TransparantFilter(), false));
		assertThat(repos.contains(keesR1), is(true));
		assertThat(repos.contains(keesR2), is(true));
		
		Repo[] repos2 = store.getRepos("kees", new TransparantFilter(), true);
		assertThat(repos2, is(nullValue()));
	}
	
	@Test
	public void testGetReposWithStoredAllContributions() {
		Contribution[] cs = {keesR2_kees, keesR1_kees};
		store.storeNewContributions(cs);
		store.storeRepo(keesR1);
		store.storeRepo(keesR2);
		store.storedAllContributionsByUser("kees", true);
		
		List<Repo> repos = Arrays.asList(store.getRepos("kees", new TransparantFilter(), false));
		assertThat(repos.contains(keesR1), is(true));
		assertThat(repos.contains(keesR2), is(true));
		
	}
	
	@Test
	public void testGetContributionsWithhoutStoredAllContributions() {
		Contribution[] cs = {keesR1_kees, keesR1_henk, keesR1_jan};
		store.storeNewContributions(cs);
		
		List<Contribution> contributions = Arrays.asList(store.getContributions("kees/R1", false));
		assertThat(contributions.contains(keesR1_kees), is(true));
		assertThat(contributions.contains(keesR1_henk), is(true));
		assertThat(contributions.contains(keesR1_jan), is(true));
		
		Contribution[] contributions2 = store.getContributions("kees/R1", true);
		assertThat(contributions2, is(nullValue()));
	}
	
	
	@Test
	public void testGetContributionsWithStoredAllContributions() {
		Contribution[] cs = {keesR1_kees, keesR1_henk, keesR1_jan};
		store.storeNewContributions(cs);
		store.storedAllContributionsForRepo("kees/R1", true);
		
		List<Contribution> contributions = Arrays.asList(store.getContributions("kees/R1", false));
		assertThat(contributions.contains(keesR1_kees), is(true));
		assertThat(contributions.contains(keesR1_henk), is(true));
		assertThat(contributions.contains(keesR1_jan), is(true));
		
		assertThat(store.getContributions("kees/R1", true), is(not(nullValue())));
		List<Contribution> contributions2 = Arrays.asList(store.getContributions("kees/R1", true));

		assertThat(contributions2.contains(keesR1_kees), is(true));
		assertThat(contributions2.contains(keesR1_henk), is(true));
		assertThat(contributions2.contains(keesR1_jan), is(true));
	}
	

	@Test
	public void testDeleteRepo() {
		store.storeRepo(keesR1);
		store.storeContribution(keesR1_jan);
		assertThat(store.getRepo("kees/R1"), is(not(nullValue())));
		assertThat(store.getAllContributions().length, is(not(equalTo(0))));
		store.deleteRepo(keesR1.full_name);
		assertThat(store.getRepo("kees/R1"), is(nullValue()));
		assertThat(store.getAllContributions().length, is(equalTo(0)));
	}
	

	@Test
	public void testDeleteUser() {
		store.storeUser(piet);
		assertThat(store.getUser("piet"), is(not(nullValue())));
		store.storeContribution(pietR1_piet);
		assertThat(store.getAllContributions().length, is(not(equalTo(0))));
		store.deleteUser(piet.login);
		assertThat(store.getUser("piet"), is(nullValue()));
		assertThat(store.getAllContributions().length, is(equalTo(0)));
		
	}

	@Test
	public void testDeleteContributionsByUser() {
		Contribution[] cs = {keesR1_jan, pietR1_jan, janR1_jan, janR1_simpleU1};
		store.storeNewContributions(cs);
		store.storeRepo(janR1);
		store.storeRepo(pietR1);
		store.storeRepo(keesR1);
		store.storeUser(jan);
		store.deleteContributionsByUser("jan");
		
		assertThat(store.getRepos("kees", new TransparantFilter(), false).length , is(equalTo(0)));
		assertThat(store.getRepo("kees/R1"), is(not(nullValue())));
		assertThat(store.getRepo("jan/R1"), is(not(nullValue())));
		assertThat(store.getRepo("piet/R1"), is(not(nullValue())));
		assertThat(store.getUser("jan"), is(not(nullValue())));
		
		assertThat(store.getRepos("simpleU1", new TransparantFilter() , false).length, is(equalTo(1)));
	}

	@Test
	public void testDeleteContributionsByRepo() {
		Contribution[] cs = {keesR1_kees, keesR1_henk, keesR1_jan, pietR1_jan};
		store.storeNewContributions(cs);
		store.storeRepo(keesR1);
		store.storeUser(kees);
		store.deleteContributionsByRepo("kees/R1");
		assertThat(store.getContributions("kees/R1", false).length , is(equalTo(0)));
		assertThat(store.getRepo("kees/R1"), is(not(nullValue())));
		assertThat(store.getUser("kees"), is(not(nullValue())));
		
		assertThat(store.getContributions("piet/R1", false).length, is(equalTo(1)));
		List<Contribution> contributionsPietR1 = Arrays.asList(store.getContributions("piet/R1", false));
		assertThat(contributionsPietR1.contains(pietR1_jan), is(true));
		
	}

	@Test
	public void testGetAllContributions() {
		Contribution[] cs = {keesR1_kees, keesR1_henk, keesR1_jan, pietR1_jan};
		store.storeNewContributions(cs);
		Contribution[] cs2 = {keesR1_kees, keesR1_henk, keesR1_jan};
		store.storeNewContributions(cs2);
		
		assertThat(store.getAllContributions().length , is(equalTo(4)));
	}

}
