Shibboleth
==========

Shibboleth provides functionality for the collection of training data for the source code authorship identification process. 
It provides means for collection and exploration of suitable Github code fragments 
and link them to the appropriate developers. The application has been developed in a modular way, 
which enables to extend the application easily. 

The name 'Shibboleth' originates from the military conflict described in the Book of Judges in the Bible where the 
pronunciation of this word was used to distinguish Ephraimites from Gileadites:

''Gilead then cut Ephraim off from the fords of the Jordan, and whenever Ephraimite fugitives said, 'Let me cross,' the men of Gilead would ask, 'Are you an Ephraimite?' If he said, 'No,' they then said, 'Very well, say "Shibboleth". If anyone said, "Sibboleth", because he could not pronounce it, then they would seize him and kill him by the fords of the Jordan. Forty-two thousand Ephraimites fell on this occasion.''
	--Judges 12:5-6, NJB


The application uses the 
<a href="http://developer.github.com/v3/">Github API</a> for repository collection. This API makes it possible
to query Github projects, which can subsequently be displayed in developer-repository graphs. After appropriate
repositories have been explored, they can be cloned and analyzed on authorship information.
 
Shibboleth includes a command line interface (CLI) and a graphical user interface (GUI). 
The GUI enables the visualization developer graphs.

The core functionality of the application comprises:

* Explore repo's and related contributors from the Github API (repository networks);
* Retrieve and display meta information about users and repositories.
* Clone Github repositories;
* Link Github users to committers in a git repositories;
* Visualize repository networks;
* Runtime caching functionality;
* Local storage of networks;
* CRUD functionality on stored networks;
* Execute a list of core actions;
* Show and respect Github API rate limits;
* Github API authentication using a personal OAuth2 Token;
* Export repository networks to a <a href="https://gephi.org/">Gephi</a> file.



