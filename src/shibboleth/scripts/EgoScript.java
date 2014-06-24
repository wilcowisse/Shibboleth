package shibboleth.scripts;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import shibboleth.Main;
import shibboleth.actions.AnalyzeAction;
import shibboleth.actions.CloneAction;
import shibboleth.actions.ExportAction;
import shibboleth.actions.GephiAction;
import shibboleth.actions.GetAction;
import shibboleth.actions.TokenAction;
import shibboleth.data.JavaScriptFilter;
import shibboleth.gui.ActionListener;
import shibboleth.gui.LogActionListener;
import shibboleth.model.Contribution;
import shibboleth.model.GephiGraph;
import shibboleth.model.Repo;
import shibboleth.model.User;
import shibboleth.util.GithubUtil;

public class EgoScript extends Main {

	private String token;
	
	private GephiGraph graph;
	
	private GetAction get;
	private ExportAction export;
	private AnalyzeAction analyze;
	private TokenAction tokenAction;
	private CloneAction clone;
	private GephiAction gephiExport;
	private ActionListener log;
	
	public EgoScript(String token){
		useProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.holmes.nl", 8080)));
		//useProxy(Proxy.NO_PROXY);
		
		graph = new GephiGraph();
		initApp(createSqliteConnection("db/db.sqlite"), graph);
		log = new LogActionListener();
		
		get = new GetAction(mysqlOnGithub, graph);
		get.addActionListener(log);
		
		export = new ExportAction(sqlOperations);
		export.addActionListener(log);
		
		analyze = new AnalyzeAction(mysqlOnGithub, github, infoStore, sqlOperations);
		analyze.addActionListener(log);
		
		tokenAction = new TokenAction(github);
		tokenAction.addActionListener(log);
		
		clone = new CloneAction(mysqlOnGithub);
		clone.addActionListener(log);
		
		gephiExport = new GephiAction(graph);
		gephiExport.addActionListener(log);

		this.token = token;
		
		execute();
	
	}

	public static void main(String[] args) {
		new EgoScript(args[0]);
	}
	
	public void execute() {
		tokenAction.execute(token);
		
		final String USER_POINTER = "briancavalier";
		final String REPO_POINTER = "briancavalier/aop-s2gx-2013";
		boolean hasSeenRepoPointer=false;
		boolean hasSeenUserPointer=false;
		
		for(String userName : users){
			
			if(USER_POINTER != null && !hasSeenUserPointer && !userName.equals(USER_POINTER)){
				log.messagePushed("Skipped "+userName);
				continue;
			}
			else{
				hasSeenUserPointer=true;
			}
			
			
			log.messagePushed("\n\n## USER: " + userName + "\n");
			User user = get.requestUser(userName);
			if(user != null && user.type.equals("User")){
				
				List<Repo> ownRepos = get.requestContributions(GithubUtil.createUser(userName), new JavaScriptFilter(), true);
				
				for(Repo repo : ownRepos){
					if(REPO_POINTER != null && !hasSeenRepoPointer && !repo.full_name.equals(REPO_POINTER)){
						log.messagePushed("Skipped "+repo);
						continue;
					}
					else{
						hasSeenRepoPointer=true;
					}
					
					log.messagePushed(" # REPO: " + repo.full_name);
					List<Contribution> contributionsToRepo = get.requestContributions(repo, true);
					if(contributionsToRepo.size()==1 && contributionsToRepo.get(0).getUser().login.equals(userName)){// only one contributor, which is owner
						if(clone.execute(repo, 4000)){
							log.messagePushed("   Analyzing "+ repo.full_name);
							analyze.execute("jaro", repo, 0.9, AnalyzeAction.PROMPT_NEVER);
							
							log.messagePushed("   Exporting "+ repo.full_name);
							List<Integer> filesOfRepo = sqlOperations.getFileIdsOfRepo(repo.full_name);
							export.execute(filesOfRepo, false);
							
							log.messagePushed("   Rate remaining: "+Integer.toString(rate.getRemaining()));
						}
						else{
							log.messagePushed("   Repo "+repo.full_name+" ignored, because cloning failed.");
						}
					}
					
				}
				
				graph.layout(GephiGraph.YIFAN_HU, 20000);
				gephiExport.execute("assets/graphs/"+userName);
				graph.removeAll();
				
				try {
					Files.copy(new File("db","db.sqlite").toPath(), new File("assets/dbs","db."+userName+".sqlite").toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					System.err.println("Failed to copy db/db.sqlite to assets/dbs/db."+userName+".sqlite");
					e.printStackTrace();
				}
			}
			else{// this is not a user but an organization..
				if(user == null)
					System.out.println(" User not found!");
				else
					System.out.println(" Ignored "+user+" because this is an "+user.type);
			}
			
			if(rate.getRemaining()>-1 && rate.getRemaining()<200){
				Main.suspend(rate);
			}
			
		}
		
		log.messagePushed("\n\nFINISHED!");
	}
	
	private String[] users = new String[]{
			"substack",
			"dominictarr",
			"mozilla",
			"mikolalysenko",
			"component",
			"Raynos",
			"bl-test-account",
			"DamonOehlman",
			"maxogden",
			"jaredhanson",
			"juliangruber",
			"visionmedia",
			"ForbesLindesay",
			"creationix",
			"thlorenz",
			"tmcw",
			"isaacs",
			"aufula",
			"dineshkummarc",
			"chrisdickinson",
			"wp-plugins",
			"codeforamerica",
			"Gozala",
			"hughsk",
			"nisaacson",
			"tower",
			"azer",
			"Runnable",
			"jesusabdullah",
			"mapbox",
			"jgallen23",
			"crcn",
			"TooTallNate",
			"yields",
			"segmentio",
			"enricomarino",
			"mmalecki",
			"ajlopez",
			"qamine",
			"timoxley",
			"cgfmedia",
			"gabrieluk",
			"MatthewMueller",
			"jeromeetienne",
			"cvdlab-cg",
			"rvagg",
			"mikeal",
			"shama",
			"shtylman",
			"jkroso",
			"coolaj86",
			"sindresorhus",
			"deoxxa",
			"okunishinishi",
			"fengmk2",
			"AndreasMadsen",
			"cowboy",
			"twilson63",
			"addyosmani",
			"stagas",
			"SixArm",
			"tmpvar",
			"toolness",
			"pgte",
			"lloyd",
			"Esri",
			"dscape",
			"carlos8f",
			"piroor",
			"bahamas10",
			"andris9",
			"bebraw",
			"antimatter15",
			"felixge",
			"nathan7",
			//"loiane",
			"bhurlow",
			"mafintosh",
			"twolfson",
			"HenrikJoreteg",
			"sethvincent",
			"btford",
			"dotnetcurry",
			"indutny",
			"collective",
			"webjars",
			"CamShaft",
			"assemble",
			"hij1nx",
			"punkave",
			"okfn",
			"superjoe30",
			"calvinmetcalf",
			"jifoo20",
			"lepture",
			"medikoo",
			"3rd-Eden",
			"jonathanong",
			"epeli",
			"c9",
			"jprichardson",
			"mapmeld",
			"codrops",
			"Colingo",
			"gruntjs",
			"KuduApps",
			"apache",
			"jquery",
			"bramstein",
			"kony",
			"kaelzhang",
			"webinos",
			"qualiancy",
			"binocarlos",
			"rwldrn",
			"rjrodger",
			"remy",
			"keijiro",
			"Vizzuality",
			"travis4all",
			"Integralist",
			"JacksonTian",
			"busterjs",
			"borismus",
			"chjj",
			"shane-tomlinson",
			"soldair",
			"dpweb",
			"arunoda",
			"bmcmahen",
			"discore",
			"phuu",
			"mathiasbynens",
			"tbranyen",
			"logicalparadox",
			"jaz303",
			"wikimedia",
			"coderiver",
			"jden",
			"rpflorence",
			"mitchellsimoens",
			"makesites",
			"LearnBoost",
			"aheckmann",
			"webpack",
			"thinkphp",
			"pazguille",
			"Benvie",
			"desandro",
			"scottgonzalez",
			"secretrobotron",
			"possibilities",
			"davglass",
			"montagejs",
			"popomore",
			"chbrown",
			"IonicaBizau",
			"jzaefferer",
			"bem",
			"mixu",
			"jsoverson",
			"Dashboard-X",
			"yeoman",
			"ednapiranha",
			"grimen",
			"lightsofapollo",
			"airportyh",
			"fent",
			"devcurry",
			"eugeneware",
			"No9",
			"filamentgroup",
			"k88hudson",
			"anthonyshort",
			"jrburke",
			"WindowsAzure-TrainingKit",
			"sunlightlabs",
			"hoodiehq",
			"serby",
			"Bryukh-Checkio-Tasks",
			"topcoat",
			"WebReflection",
			"pksunkara",
			"NHQ",
			"nearinfinity",
			"eldargab",
			"v1factory",
			"zaach",
			"goatslacker",
			"Marak",
			"fnobi",
			"vesln",
			"sidorares",
			"JohnMcLear",
			"kreativkombinat",
			"maccman",
			"pvorb",
			"guille",
			"arian",
			"brighthas",
			"stephenmathieson",
			"sandro-pasquali",
			"ryanramage",
			"caolan",
			"edwardhotchkiss",
			"dthompson",
			"atogle",
			"ncb000gt",
			"poying",
			"afc163",
			"daleharvey",
			"louisremi",
			"exfm",
			"markdalgleish",
			"jpillora",
			"kig",
			"yckart",
			"simonfan",
			"chilts",
			"sourcemint",
			"domenic",
			"paulirish",
			"bredele",
			"cykod",
			"brycebaril",
			"gxcsoccer",
			"kanso",
			"jlongster",
			"ntotten",
			"werckerbot",
			"jfromaniello",
			"ianstormtaylor",
			"aralejs",
			"bogavante",
			"tellnes",
			"neekey",
			"jwerle",
			"nko",
			"Rockncoder",
			"01org",
			"chilijung",
			"indexzero",
			"jbuck",
			"briancavalier",
			"nprapps",
			"clux",
			"wolfeidau",
			"bradygaster",
			"erikvold",
			"rajaraodv",
			"walmartlabs",
			"wookiehangover",
			"EventedMind",
			"feedhenry",
			"codeactual",
			"PascalPrecht",
			"potch",
			"flatiron",
			"bmeck",
			"tcr",
			"cpsubrian",
			"daxxog",
			"mklabs",
			"jed",
			"yahoo",
			"toutpt",
			"openplans",
			"financeCoding",
			"Joncom",
			"ecto",
			"x-tag",
			"jcoglan",
			"Skookum",
			"revolunet",
			"Gagle",
			"codepo8",
			"mixteam",
			"hubgit",
			"SlexAxton",
			"brianc",
			"padolsey",
			"tmeasday",
			"reebalazs",
			"fouber",
			"prashtx",
			"hotoo",
			"alanshaw",
			"killdream",
			"karma-runner",
			"vlandham",
			"NV",
			"doug-martin",
			"awssum",
			"aaronksaunders",
			"guybedford",
			"mattneary",
			"paulmillr",
			"thisandagain",
			"robertkowalski"
	};

}