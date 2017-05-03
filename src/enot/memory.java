package enot;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.Index;

public class memory {

	private static final File DB_PATH = new File("enotmemory");
	private static GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector("0");
	private static RelationshipType REL = RelationshipType.withName("REL");
	static GraphDatabaseService graphDb = new GraphDatabaseFactory()
		    .newEmbeddedDatabaseBuilder(DB_PATH)
		    .setConfig( bolt.type, "BOLT" )
		    .setConfig(bolt.enabled, "true")
		    .setConfig(bolt.address, "0.0.0.0:7687")
		    .newGraphDatabase();
	static Index<Node> indexService;// = graphDb.index().forNodes( "nodes" );
	private static ArrayList<String> incomeMessage = new ArrayList<String>();
	private static Vector<Integer> incomeIndex = new Vector<Integer>();

	private static class countsRepeat {
		private int position;
		private int count;
	}

	static int finalOut = 0;

	public static int IncomigMessage(String message) {
		long startTime = System.currentTimeMillis();
		int dislocation = 0;
		// graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH) ;
		// indexService = graphDb.index().forNodes( "nodes" );
		//registerShutdownHook(graphDb);
		try (Transaction tx = graphDb.beginTx()){
			indexService = graphDb.index().forNodes("nodes");
			incomeIndex = turnToIndex(message);
			System.out.println("Индексы получены");
			System.out.println("Всего индексов: "+indexCount());
			Vector<Integer> incomeIndexDouble = new Vector<Integer>(incomeIndex);
			while (incomeIndex.size() > 1) {
				for (int i = 0; i < incomeIndex.size() - 1; i++) {
					Node nodeOne = getOrCreateNodeD("index", incomeIndex.get(i));
					if (nodeOne.hasRelationship()) {
						System.out.println("связи обнаружены");
						for (Relationship relationshipUp : nodeOne.getRelationships(Direction.OUTGOING)) {
							int relIndex = (int) relationshipUp.getProperty("RelationsNumber");
							for (Node nodeUp : relationshipUp.getNodes()) {
								for (Relationship relationshipDown : nodeUp.getRelationships(Direction.INCOMING)) {
									if ((int) relationshipDown.getProperty("RelationsNumber") == relIndex + 1) {
										for (Node nodeNext : relationshipDown.getNodes()) {
											if (nodeNext.getProperty("index")==incomeIndex.get(i + 1)) {
												incomeIndexDouble.set(i, (int) nodeUp.getProperty("index"));
												incomeIndexDouble.set(i + 1, (int) nodeUp.getProperty("index"));
											}
										}
									}
								}
							}
						}
					}
				}
				System.out.println("Проверка на пары прошла");
				System.out.print("индексы: ");
				for (int i=0;i<incomeIndexDouble.size();i++){
					System.out.print(incomeIndexDouble.get(i)+", ");
				}
				System.out.println("Всего индексов: "+indexCount());
				int cut = 0;
				while (cut == 0) {
					countsRepeat repeats = new countsRepeat();
					repeats.position = -1;
					repeats.count = 0;
					for (int i = 0; i < incomeIndexDouble.size() - 1; i++) {
						if (incomeIndexDouble.get(i) == incomeIndexDouble.get(i + 1) && repeats.position == -1) {
							repeats.position = i;
						}
						if (incomeIndexDouble.get(i) != incomeIndexDouble.get(i + 1) && repeats.position != -1) {
							repeats.count = i - repeats.position+1;
							break;
						}
					}
					if (repeats.position != -1 && repeats.count==0) {
						repeats.count =incomeIndexDouble.size()-repeats.position;
					}
					System.out.println("повторы с позиции: " + repeats.position);
					System.out.println("длинна повтора: " + repeats.count);
					System.out.println("Смещение индексов: "+(repeats.position + dislocation));
					if (repeats.count > 0) {
						Node nodeUp = getOrCreateNodeD("index",
								incomeIndexDouble.get(repeats.position));
						if (nodeUp.getDegree(Direction.INCOMING) > repeats.count) {
							int number = -1;
							int tempIndex = indexPlus();
							Node nodeMiddle = getOrCreateNodeD("index", tempIndex);
							for (int i = repeats.position; i < repeats.position + repeats.count; i++) {
								Node nodeDown = getOrCreateNodeD("index",
										incomeIndex.get(i + dislocation));
								for (Relationship relationshipDown : nodeUp.getRelationships(Direction.INCOMING)) {
									 int oneEnd=(int)relationshipDown.getOtherNode(nodeUp).getProperty("index");
									 System.out.println(oneEnd);
									 int otherEnd=(int)nodeDown.getProperty("index");
									 System.out.println(otherEnd);
									if (oneEnd==otherEnd) {
										int numberTemp = (int) relationshipDown.getProperty("RelationsNumber");
										if (number==-1 || numberTemp< number){
											number = numberTemp;
										}
										System.out.println("удаляем связь");
										relationshipDown.delete();
										Relationship relationshipNew = nodeDown.createRelationshipTo(nodeMiddle, REL);
										relationshipNew.setProperty("RelationsNumber", i - repeats.position);
									}
								}
							}
							Relationship relationshipNew = nodeMiddle.createRelationshipTo(nodeUp, REL);
							relationshipNew.setProperty("RelationsNumber", number);
							for (Relationship relationshipDown : nodeUp.getRelationships(Direction.INCOMING)) {
								if ((int) relationshipDown.getProperty("RelationsNumber") > number) {
									int numberUpgrade = (int) relationshipDown.getProperty("RelationsNumber");
									relationshipDown.setProperty("RelationsNumber", numberUpgrade - repeats.count+1);
								}
							}
							incomeIndexDouble.set(repeats.position, tempIndex);
							for (int i = repeats.position + repeats.count-1; i > repeats.position ; i--) {
								incomeIndexDouble.remove(i);
							}
						} else {
							for (int i = repeats.position + repeats.count-1; i > repeats.position ; i--) {
								incomeIndexDouble.remove(i);
							}
						}
						System.out.println("размер фразы: "+incomeIndexDouble.size());
					} else {

						cut = 1;
					}
					System.out.println("Найденые пары сокращены");
					System.out.println("Всего индексов: "+indexCount());
					System.out.print("индексы: ");
					for (int i=0;i<incomeIndexDouble.size();i++){
						System.out.print(incomeIndexDouble.get(i)+", ");
					}
					if (repeats.count>0){
					dislocation= dislocation+repeats.count-1;
					}
				}
				if (incomeIndex.equals(incomeIndexDouble)){
					Node nodeNewTop = getOrCreateNodeD("index", indexPlus());
					for (int i = 0; i < incomeIndex.size(); i++) {
						Node nodeDown = getOrCreateNodeD("index",incomeIndex.get(i));
								Relationship relationshipNew = nodeDown.createRelationshipTo(nodeNewTop, REL);
								relationshipNew.setProperty("RelationsNumber", i);
								//String tempChar= String.valueOf(nodeNewTop.getProperty("nameChar"))+String.valueOf(nodeDown.getProperty("nameChar"));
								//nodeNewTop.setProperty("nameChar", tempChar);
						}
					incomeIndex.clear();
					incomeIndex.add(indexCount());
					} else {
						incomeIndex = new Vector<Integer>(incomeIndexDouble);
						dislocation=0;
					}
				System.out.println("Длинна Индекса: "+incomeIndex.size());
				System.out.println("Длинна Индекса Дубля: "+incomeIndexDouble.size());
				}
			finalOut = indexCount();
			System.out.println("Закончили обработку");
			System.out.println("Всего индексов: "+indexCount());
		tx.success();
		tx.close();
		} 
		//	graphDb.shutdown();
		System.out.println(finalOut);
		long estimatedTime = System.currentTimeMillis()-startTime;
		System.out.println("Время на добавление: "+estimatedTime+ " миллис");
		return finalOut;
	}

	private static Node getOrCreateNode(String nameKey, String valueSign) {
		Node node = indexService.get(nameKey, valueSign).getSingle();
		if (node == null) {
			node = graphDb.createNode();
			node.setProperty(nameKey, valueSign);
			//node.setProperty("nameChar", "");
			indexService.add(node, nameKey, valueSign);
		}
		return node;
	}
	private static Node getOrCreateNodeD(String nameKey, Integer valueSign) {
		Node node = indexService.get(nameKey, valueSign).getSingle();
		if (node == null) {
			node = graphDb.createNode();
			node.setProperty(nameKey, valueSign);
			//node.setProperty("nameChar", "");
			indexService.add(node, nameKey, valueSign);
		}
		return node;
	}

	private static Vector<Integer> turnToIndex(String incomeMessage) {
		String[] parts = incomeMessage.split("");
		Vector<Integer> outIndex = new Vector<Integer>();
		for (int i = 0; i < parts.length; i++) {
			int charIndex = -1;
			System.out.println(parts[i]);
			//System.out.println(indexService.get("nameChar", parts[i]).size());
			 int tm = indexService.get("nameChar", parts[i]).size();
			if (tm==0 | tm<2 ){
			Node nodeI = indexService.get("nameChar", parts[i]).getSingle();
			if (nodeI == null) {
				System.out.println("Нода не найдена");
				nodeI = graphDb.createNode();
				nodeI.setProperty("nameChar", parts[i]);
				int indexPlus = indexPlus();
				nodeI.setProperty("index", indexPlus);
				indexService.add(nodeI, "nameChar", parts[i]);
			}
			 charIndex = (int) (nodeI.getProperty("index"));
			}else{
			for (Node nodeT : indexService.get("nameChar", parts[i])) {
				 charIndex = (int) (nodeT.getProperty("index"));
				if (!outIndex.contains(charIndex)) {
					break;
				}
			}
			}
			if (outIndex.contains(charIndex)) {
				Node nodeA = graphDb.createNode();
				nodeA.setProperty("nameChar", parts[i]);
				int indexPlus = indexPlus();
				nodeA.setProperty("index", indexPlus);
				indexService.add(nodeA, "nameChar", parts[i]);
				charIndex = (int) (nodeA.getProperty("index"));
			}
			outIndex.add(charIndex);
		}
		for (int i = 0; i < parts.length; i++) {
			String first= parts[i];
			for (int y = i+1; y < parts.length; y++) {
				if (first.equals(parts[y])){
					Node nodeFirst=getOrCreateNodeD("index", outIndex.get(i));
					Node nodeSecond=getOrCreateNodeD("index", outIndex.get(i+1));
					//System.out.println("нода номер "+ nodeFirst.getProperty("index"));
					if (nodeFirst.hasRelationship()&& nodeSecond.hasRelationship()) {
					for (Relationship relationshipFirst : nodeFirst.getRelationships(Direction.OUTGOING)) {
						int trr= (int) relationshipFirst.getOtherNode(nodeFirst).getProperty("index");
						for (Relationship relationshipSecond : nodeSecond.getRelationships(Direction.OUTGOING)) {
						int trr2 = (int) relationshipSecond.getOtherNode(nodeSecond).getProperty("index");
							if (trr == trr2) {
							int temp=outIndex.get(i);
							int temp2=outIndex.get(y);
							outIndex.set(i, temp2);
							outIndex.set(y, temp);
						}
						}
					}
					}
				}
			}
			
		}
		return outIndex;
	}

	private static Integer indexCount() {
		Node indexM = getOrCreateNode("indexMax", "");
		if (!indexM.hasProperty("maxIndex")) {
			indexM.setProperty("maxIndex", 0);
		}
		return (int) (indexM.getProperty("maxIndex"));
	}

	private static Integer indexPlus() {
		Node indexM = getOrCreateNode("indexMax", "");
		if (!indexM.hasProperty("maxIndex")) {
			indexM.setProperty("maxIndex", 0);
		}
		System.out.println(indexM.getProperty("maxIndex"));
		int maxPlus = (int) (indexM.getProperty("maxIndex")) + 1;
		indexM.setProperty("maxIndex", maxPlus);
		return (int) (indexM.getProperty("maxIndex"));
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}
}
