package shibboleth.test.data;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;


import org.junit.Test;

import shibboleth.data.TransparantFilter;
import shibboleth.model.Repo;

public class TestTransparantFilter {

	@Test
	public void testAcceptsJavaScriptRepo() {
		Repo r = new Repo();
		r.language = "JavaScript";
		
		TransparantFilter filter=new TransparantFilter();
		assertThat(filter.accepts(r), is(true));
	}
	
	@Test
	public void testAcceptsNonJavaScriptRepo() {
		Repo r = new Repo();
		r.language = "Dutch";
		
		TransparantFilter filter=new TransparantFilter();
		assertThat(filter.accepts(r), is(true));
	}

}
