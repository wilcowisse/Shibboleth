package shibboleth.model;

import java.util.Objects;

public class Commit {
	public String sha;
	public Committer committer;
	public SimpleUser user;
	
	public String toString(){
		return String.format("%s [%s] [%s]", sha, committer, user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sha, committer, user);
	}

	@Override
	public boolean equals(Object obj) {				
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Commit other = (Commit) obj;
		if (committer == null) {
			if (other.committer != null)
				return false;
		} else if (!committer.equals(other.committer))
			return false;
		if (sha == null) {
			if (other.sha != null)
				return false;
		} else if (!sha.equals(other.sha))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
	
	
}
