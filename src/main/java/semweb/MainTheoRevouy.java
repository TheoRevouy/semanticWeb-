package semweb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;;

public class MainTheoRevouy {

	public static void main(String[] args) {
		
		String file = "./stops.txt";
		BufferedReader br = null;
		String line = "";
		String split = ",";
		String []stops = null;
		
		try {
			br = new BufferedReader(new FileReader(file));
			while((line=br.readLine()) != null) {
				stops = line.split(split);
				
				Model model = ModelFactory.createDefaultModel();
				Resource r1 = model.createResource("http://example.org/" + stops[0]);
				Resource r2 = model.createResource("http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing");
				Resource r3 = model.createResource(stops[1]);
				RDFNode r4 = model.createTypedLiteral(stops[3], XSDDatatype.XSDdecimal);
				RDFNode r5 = model.createTypedLiteral(stops[4], XSDDatatype.XSDdecimal);
				Property p1 = model.createProperty("a");
				Property p3 = model.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
				Property p4 = model.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#long");
				
				r1.addProperty(p1, r2);
				r1.addProperty(RDFS.label, stops[1],"fr");
				r1.addProperty(p3, r4);
				r1.addProperty(p4,r5);
				
				model.write(System.out, "Turtle");
				
				String datasetURL = "http://localhost:3030/test";
				String sparqlEndpoint = datasetURL + "/sparql";
				String sparqlUpdate = datasetURL + "/update";
				String graphStore = datasetURL + "/data";
				RDFConnection conneg = RDFConnectionFactory.connect(sparqlEndpoint,sparqlUpdate,graphStore);
				conneg.load(model); // add the content of model to the triplestore
				conneg.update("INSERT DATA { <test> a <TestClass> }"); // add the triple to the triplestore	
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
