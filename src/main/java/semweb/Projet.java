package semweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import fr.mines_stetienne.ci.sparql_generate.SPARQLExt;
import fr.mines_stetienne.ci.sparql_generate.engine.PlanFactory;
import fr.mines_stetienne.ci.sparql_generate.engine.RootPlan;
import fr.mines_stetienne.ci.sparql_generate.query.SPARQLExtQuery;
import fr.mines_stetienne.ci.sparql_generate.stream.LocationMapperAccept;
import fr.mines_stetienne.ci.sparql_generate.stream.LocatorFileAccept;
import fr.mines_stetienne.ci.sparql_generate.stream.LookUpRequest;
import fr.mines_stetienne.ci.sparql_generate.stream.SPARQLExtStreamManager;
import fr.mines_stetienne.ci.sparql_generate.utils.ContextUtils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;



public class Projet {
	
	private static final Logger LOG = LoggerFactory.getLogger(Projet.class);

	public static void main(String[] args) {
		
		String file = "./src/main/resources/irve-sem-20200420.csv";
		BufferedReader br = null;
		String line = "";
		String split = ";";
		String []charger = null;
		
		try {
			br = new BufferedReader(new FileReader(file));
			while((line=br.readLine()) != null) {
				charger = line.split(split);
				
				Model model = ModelFactory.createDefaultModel();
				
				Literal planner = model.createTypedLiteral(charger[0]);
				Literal operator = model.createTypedLiteral(charger[1]);
				Literal brand = model.createTypedLiteral(charger[2]);
				Resource idStation = model.createResource("http://example.org/" + charger[3]);
				Literal nameStation = model.createTypedLiteral(charger[4]);
				Literal adressStation = model.createTypedLiteral(charger[5]);
				Literal insee = model.createTypedLiteral(charger[6]);
				//Literal longitude = model.createTypedLiteral(charger[7]);
				//Literal latitude = model.createTypedLiteral(charger[8]);
				RDFNode longitude = model.createTypedLiteral(charger[7], XSDDatatype.XSDdecimal);
				RDFNode latitude = model.createTypedLiteral(charger[8], XSDDatatype.XSDdecimal);
				Literal nbCharger = model.createTypedLiteral(charger[9]);
				Literal idCharger = model.createTypedLiteral(charger[10]);
				Literal maxPoxer = model.createTypedLiteral(charger[11]);
				Literal typeCharger = model.createTypedLiteral(charger[12]);
				Literal rechargeAccess = model.createTypedLiteral(charger[13]);
				Literal accessibility = model.createTypedLiteral(charger[14]);
				Literal observations = model.createTypedLiteral(charger[15]);
				Literal electringStationCharging = model.createTypedLiteral("https://www.wikidata.org/wiki/Q2140665");
				Literal updateDate = model.createTypedLiteral(charger[16]);
				
				Property a = model.createProperty("a");
				Property pLatitude = model.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
				Property pLongitude = model.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#long");
				
				//playground sparql-generate
				
				idStation.addProperty(a, electringStationCharging);
				idStation.addProperty(RDFS.label, planner);
				idStation.addProperty(RDFS.label, operator);
				idStation.addProperty(RDFS.label, brand);
				idStation.addProperty(RDFS.label, nameStation);
				idStation.addProperty(RDFS.label, adressStation);
				idStation.addProperty(RDFS.label, insee);
				idStation.addProperty(pLongitude, longitude);
				idStation.addProperty(pLatitude, latitude);
				idStation.addProperty(RDFS.label, nbCharger);
				idStation.addProperty(RDFS.label, idCharger);
				idStation.addProperty(RDFS.label, maxPoxer);
				idStation.addProperty(RDFS.label, typeCharger);
				idStation.addProperty(RDFS.label, rechargeAccess);
				idStation.addProperty(RDFS.label, accessibility);
				idStation.addProperty(RDFS.label, observations);
				idStation.addProperty(RDFS.label, updateDate);		
				
				model.write(System.out, "Turtle");
			
				String datasetURL = "http://localhost:3030/charger";
				String sparqlEndpoint = datasetURL + "/sparql";
				String sparqlUpdate = datasetURL + "/update";
				String graphStore = datasetURL + "/data";
				RDFConnection conneg = RDFConnectionFactory.connect(sparqlEndpoint,sparqlUpdate,graphStore);
				conneg.load(model); // add the content of model to the triplestore
				conneg.update("INSERT DATA { <test> a <TestClass> }"); // add the triple to the triplestore
				
				/*String queryString = IOUtils.toString(new FileInputStream("./resources/query.rqg"), StandardCharsets.UTF_8);
		        SPARQLExtQuery query = (SPARQLExtQuery) QueryFactory.create(queryString, SPARQLExt.SYNTAX);
		        LOG.info("query is\n" + query.toString());

		        // create the plan
		        RootPlan plan = PlanFactory.create(query);

		        // add the input graph to the builder
		        RDFDataMgr.read(model, new FileInputStream("./resources/irve-sem-20200420.ttl"), Lang.TURTLE);

		        // output the default graph
		        StringWriter sb = new StringWriter();
		        RDFDataMgr.write(sb, model, Lang.TURTLE);
		        LOG.info("default graph is\n" + sb.toString());

		        // set up the stream manager
		        LocatorFileAccept locator = new LocatorFileAccept(new File("resources").toURI().getPath());
		        LocationMapperAccept mapper = new LocationMapperAccept();
		        mapper.addAltEntry("https://example.com/irve-sem-20200420", "irve-sem-20200420.csv");
		        SPARQLExtStreamManager sm = SPARQLExtStreamManager.makeStreamManager(locator, mapper);

		        // output a message 
		        TypedInputStream tin = sm.open(new LookUpRequest("https://example.com/irve-sem-20200420"));
		        LOG.info("message is\n" + IOUtils.toString(tin, StandardCharsets.UTF_8));
		        
		        // create the context
		        Context context = ContextUtils.build()
		        		.setInputModel(model)
		        		.setStreamManager(sm)
		    			.build();
		        
		        long start = System.currentTimeMillis();
		        // execute the plan
		        Model output = plan.execGenerate(context);
		        long end = System.currentTimeMillis();

		        // output model
		        StringWriter sboutput = new StringWriter();
		        RDFDataMgr.write(sboutput, output, Lang.TURTLE);
		        LOG.info("query is\n" + sboutput.toString());
		        LOG.info("transformation time (ms): " + (end - start));*/

			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(br != null) {
				try {
					br.close();
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
