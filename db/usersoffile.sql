SELECT RecordLinks.user, Chunks.'start', Chunks.'end', Chunks.'time', Committers.email, Committers.name, Files.repo, Files.file_path, Files.head 
FROM RecordLinks 
JOIN Committers ON RecordLinks.committer=Committers.id 
JOIN Chunks ON Committers.id=Chunks.committer_id 
JOIN Files ON Chunks.file_id=Files.id 
WHERE Files.id=6;
