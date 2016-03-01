
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class JWIWrapper 
{
	static final String WN_DICT_FOLDER = "C:\\Program Files (x86)\\WordNet\\2.1\\dict";
	public IDictionary dictionary;
	
	
	public JWIWrapper() throws IOException 
	{
		File wordnetDir = new File(WN_DICT_FOLDER);
		
		 // construct the dictionary object and open it
		dictionary = new Dictionary (wordnetDir);
		dictionary.open();
	}
	
	
	public void getHypernyms(String term)
	{
		 // get the synset
		 IIndexWord idxWord = dictionary.getIndexWord (term, POS.NOUN);
		 IWordID wordID = idxWord . getWordIDs ().get(0) ; // 1st meaning
		 IWord word = dictionary.getWord ( wordID );
		 ISynset synset = word . getSynset ();
		
		 // get the hypernyms
		 List < ISynsetID > hypernyms =
		 synset . getRelatedSynsets (Pointer.HYPERNYM );
		
		 // print out each h y p e r n y m s id and synonyms
		 List <IWord > words ;
		 for( ISynsetID sid : hypernyms )
		 {
			 words = dictionary . getSynset (sid). getWords ();
			 System .out . print (sid + " {");
			 for( Iterator <IWord > i = words . iterator (); i. hasNext () ;)
			 {
				 System .out . print (i. next (). getLemma ());
				 if(i. hasNext ())
					 System .out . print (", ");
			 }
			 System .out . println ("}");
		 }
	 }
	
	public void exploreTerm(String term)
	{
		IIndexWord idxWord = dictionary.getIndexWord(term, POS.NOUN);
		List<IWordID> wordsID = idxWord.getWordIDs();
		
		System.out.println("Lemma: " + idxWord.getLemma());
		System.out.println("Number of word IDs: " + wordsID.size());
		
		int i = 0;
		for( IWordID wid : wordsID )
		{
			IWord word = dictionary.getWord(wid);
			ISynset synset = word.getSynset();
						
			//System.out.print(i +": " + word.getLemma() + " ("); 
			
			List<IWord> words = synset.getWords();
			System .out . print (synset.getID() + " {");
			for( Iterator <IWord > w = words . iterator (); w. hasNext () ;){
				System .out . print (w. next (). getLemma ());
				if(w. hasNext ())
					System .out . print (", ");
			
			}	
			System .out . println ("}");
			
			//System.out.println(synset.getGloss());
			exploreSynset(synset);
			
			
			i++;
			//System.out.println("WordNumber: " + wid.getWordNumber());
			//System.out.println("Lemma: " + wid.getLemma());
			//System.out.println(wid.getSynsetID().);
		}
		
	}
	
	public void exploreSynset(ISynset synset)
	{
		
		// get the hypernyms
		List < ISynsetID > hypernyms =
		synset . getRelatedSynsets (Pointer.HYPONYM);
		
		// print out each h y p e r n y m s id and synonyms
		List <IWord > words ;
		//System.out.println();
		for( ISynsetID sid : hypernyms ){
			words = dictionary . getSynset (sid). getWords ();
			System .out . print ("\t" + sid + " {");
			for( Iterator <IWord > i = words . iterator (); i. hasNext () ;){
				System .out . print (i. next (). getLemma ());
				if(i. hasNext ())
					System .out . print (", ");
			}
			System .out . println ("}");
		}
	}
}


