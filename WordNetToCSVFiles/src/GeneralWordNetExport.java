

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class GeneralWordNetExport 
{
	static JWIWrapper jwiwrapper;	
	
	static String objFileName = "export/indexes/WordNetWIDIndex.obj";
	
	static TreeMap<String, Integer> sidMap = new TreeMap<String, Integer>();
	static TreeMap<String, Integer> widMap = new TreeMap<String, Integer>();
	
	static int synsetLevel = 0;
	
	static final int MAX_LEVEL = 18;
	static final int MAX_FREQUENCY = 28;
	static final int MAX_POLYSEMY = 33;
	
	public static void main(String[] args) 
	{
		try 
		{
			jwiwrapper = new JWIWrapper();
			
			//Exporting synset nodes
			FileWriter fileWriterConcept = new FileWriter("WordNet_Synset_V2.csv");
			POS[] pos_values = POS.values();
			fileWriterConcept.write ("id,SID,type,POS,level,dimension,words,label\n");
			int id = 0;
				
			for (POS pos:pos_values)
			{
				Iterator<ISynset> synsetIterator = jwiwrapper.dictionary.getSynsetIterator(pos);
				
					while(synsetIterator.hasNext()) 
					{
						ISynset synset = synsetIterator.next();
						
						String line = getSynsetLine(synset, id);
						//System.out.println(line);
						fileWriterConcept.write(line + "\n");
						sidMap.put(synset.getID().toString(), id);
						id++;
					}
			}
			
			fileWriterConcept.flush();
			fileWriterConcept.close();
						
			
			// Exporting semantic relations
			
			FileWriter fileWriterProp = new FileWriter("WordNet_Prop_V1.csv");
			fileWriterProp.write ("Prop,Src,Dest\n");
			
			for (POS pos:pos_values)
			{
			
				Iterator<ISynset> synsetIterator = jwiwrapper.dictionary.getSynsetIterator(pos);
				
				while(synsetIterator.hasNext()) 
				{
					ISynset synset = synsetIterator.next();
					
					Map<IPointer, List<ISynsetID>> map = synset.getRelatedMap();
					
					for (Map.Entry<IPointer, List<ISynsetID>> entry : map.entrySet()) 
					{
						IPointer ipointer = entry.getKey();
						List<ISynsetID> synList = entry.getValue();
						
						Iterator<ISynsetID> iterator = synList.iterator();
						while (iterator.hasNext()) {
							ISynsetID synsetID = iterator.next();
							//System.out.println(ipointer.getName() + "," + sidMap.get(synset.getID().toString()).intValue() + "," + sidMap.get(synsetID.toString()).intValue());
							fileWriterProp.write (ipointer.getName() + "," + sidMap.get(synset.getID().toString()).intValue() + "," + sidMap.get(synsetID.toString()).intValue() +"\n");
						}
					}  
					
				}
			}
			
			fileWriterProp.flush();
			fileWriterProp.close();
						
						
						
			// Exporting wordsenses nodes
			FileWriter fileWriterWordSenses = new FileWriter("WordNet_WordSenses_V2.csv");
			fileWriterWordSenses.write ("id,Sid,WID,type,POS,polysemy,frequency,dimension,label\n");
			
			//POS[] pos_values = POS.values();
			id = 0;
			
			for (POS pos:pos_values)
			{
				Iterator<IIndexWord> wordIterator = jwiwrapper.dictionary.getIndexWordIterator(pos);
				
				while(wordIterator.hasNext()) 
				{
					IIndexWord indexWord = wordIterator.next();
					List<IWordID> wordIDs = indexWord.getWordIDs();
					int sense_count = wordIDs.size();
					
					int level = 0;
					int frequency;
					
					for (IWordID wid : wordIDs)
					{
						IWord word = jwiwrapper.dictionary.getWord(wid);
						
						String str = "" +  id + ",";
						str += sidMap.get(word.getSynset().getID().toString()).intValue() + ",";
						str += word.getID().toString() + ",";
						str += "wordSense,";
						str += word.getPOS().name() + ",";
						str += sense_count + ",";
						frequency = getFrequencyRank(word)+1;
						str += frequency + ",";
						getLevel(word.getSynset(), 0);
						str += (int)((double)(2*((MAX_LEVEL - synsetLevel)*10)/3)*((double)1/sense_count)*((double)1/frequency)) + ",";
						str += word.getLemma();
						
						//System.out.println("Level: " + getLevel(word.getSynset(), 0));
						fileWriterWordSenses.write(str + "\n");
						
						widMap.put(word.getID().toString(), id);
						id++;
						
						synsetLevel = 0;
					}
				}
			}
			
			fileWriterWordSenses.flush();
			fileWriterWordSenses.close();
			
			// Exporting sem_syn relations
			
			FileWriter fileWriterSemSyn = new FileWriter("WordNet_SemSyn_V1.csv");
			fileWriterSemSyn.write ("Src,Dest\n");
			
			for (POS pos:pos_values)
			{
				Iterator<IIndexWord> wordIterator = jwiwrapper.dictionary.getIndexWordIterator(pos);
				
				while(wordIterator.hasNext()) 
				{
					IIndexWord indexWord = wordIterator.next();
					List<IWordID> wordIDs = indexWord.getWordIDs();
					
					for (IWordID wid : wordIDs)
					{
						IWord word = jwiwrapper.dictionary.getWord(wid);
						int src, dest;
						src = widMap.get(word.getID().toString()).intValue();
						dest = sidMap.get(word.getSynset().getID().toString());
						fileWriterSemSyn.write("" + src + "," + dest + "\n");
						
					}
				}
			}
			
			fileWriterSemSyn.flush();
			fileWriterSemSyn.close();
			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	static String getSynsetLine(ISynset synset, int id)
	{
		String str = "" +  id + ",";
		str += synset.getID().toString() + ",";
		str += "synset,";
		str += synset.getPOS().name() + ",";
		getLevel(synset, 0);
		str += synsetLevel + ",";
		str += (MAX_LEVEL - synsetLevel)*10 + ",";
		synsetLevel = 0;
		
		List<IWord> words = synset.getWords();
		str += "{";
		for( Iterator <IWord > w = words . iterator (); w. hasNext () ;){
			str += w. next ().getLemma ();
			if(w. hasNext ())
				str += "; ";
		}
		str += "}";
		str += ",";
		
		str += "\"" + synset.getGloss().replaceAll("\"", "\"\"") + "\"";
		
		return str;
	}
	
	static int getFrequencyRank(IWord word)
	{
		ISynset synset = word.getSynset();
		int rank = 0, i = 0;
		
		List<IWord> wordList = synset.getWords();
		for (IWord w:wordList)
		{
			if (w.equals(word))
				rank = i;
			i++;
		}
		return rank;
	}
	
	public static void getLevel(ISynset synset, int level)
	{
		List<ISynsetID> hypernims = synset.getRelatedSynsets(Pointer.HYPERNYM);
			
		if (level > synsetLevel) 
			synsetLevel = level;
		
		for(ISynsetID synID:hypernims)
		{
			ISynset syn = jwiwrapper.dictionary.getSynset(synID);
			getLevel(syn, ++level);
			level--;
		}
	}
}
