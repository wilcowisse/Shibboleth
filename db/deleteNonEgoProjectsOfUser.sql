DELETE 
FROM Contributions 
WHERE user_name != 'sindresorhus' 
OR NOT EXISTS (
SELECT cs.repo_name, COUNT( cs.repo_name ) 
FROM Contributions AS cs 
WHERE cs.repo_name = Contributions.repo_name 
GROUP BY cs.repo_name 
HAVING count( cs.user_name ) = 1
);

-- jihaa! DeMorgan.
