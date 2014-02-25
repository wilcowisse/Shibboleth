package shibboleth.git;

import java.util.ArrayList;
import java.util.List;

import shibboleth.model.Commit;
import shibboleth.model.RecordLink;

public class CommitLinker implements Linker {
	
	private List<Commit> commits;
	
	public CommitLinker(List<Commit> commits) {
		this.commits=commits;
	}
	
	@Override
	public List<RecordLink> link() {
		List<RecordLink> links = new ArrayList<RecordLink>();
		for(Commit commit : commits){
			RecordLink link = new RecordLink(commit.committer, commit.user);
			if(!links.contains(link))
				links.add(link);
		}
		return links;
	}

}
