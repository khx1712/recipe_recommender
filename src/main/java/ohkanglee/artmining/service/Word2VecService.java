package ohkanglee.artmining.service;

import ohkanglee.artmining.dto.recipeDto.*;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.plot.BarnesHutTsne;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.ui.api.UIServer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.shade.guava.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.util.*;

@Service
public class Word2VecService {

    private static final Logger log = LoggerFactory.getLogger(Word2VecService.class);
    private static Word2Vec word2Vec;
    private static ArrayList<UrlINDArray> urlINDArrays;

    @Autowired
    public Word2VecService() throws IOException {
        try {
            word2Vec = WordVectorSerializer.loadFullModel("pathToSaveModel.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        urlINDArrays = new ArrayList<>();
        File file = new File("recipeList.txt");
        BufferedReader inFile = new BufferedReader(new FileReader(file));
        String sLine = null;

        while( (sLine = inFile.readLine()) != null ){
            int spiltIdx = sLine.indexOf(" ");
            String title = sLine.substring(0, spiltIdx);
            String ingredients = sLine.substring(spiltIdx+1);
            Collection<String> recipeCollection = Splitter.on(' ').splitToList(ingredients);
            INDArray vectorSum = word2Vec.getWordVectorsMean(recipeCollection);
            urlINDArrays.add(new UrlINDArray(title, vectorSum));
        }
        inFile.close();
    };


    public void Word2vecTrain() throws IOException {
        log.info("Load data....");
        ClassPathResource resource = new ClassPathResource("raw_sentences.txt");
        SentenceIterator iter = new LineSentenceIterator(resource.getFile());
        iter.setPreProcessor(new SentencePreProcessor() {
            @Override
            public String preProcess(String sentence) {
                return sentence.toLowerCase();
            }
        });

        TokenizerFactory tokenizer = new DefaultTokenizerFactory();
        tokenizer.setTokenPreProcessor(new CommonPreprocessor());

        int batchSize = 5000;
        int iterations = 200;
        int layerSize = 150;

        log.info("Build model....");
        Word2Vec vec = new Word2Vec.Builder()
                .batchSize(batchSize) //# words per minibatch.
                .minWordFrequency(1) //
                .useAdaGrad(false) //
                .layerSize(layerSize) // word feature vector size
                .iterations(iterations) // # iterations to train
                .learningRate(0.025) //
                .minLearningRate(1e-3) // learning rate decays wrt # words. floor learning
                .negativeSample(10) // sample size 10 words
                .iterate(iter) //
                .tokenizerFactory(tokenizer)
                .build();
        vec.fit();
        WordVectorSerializer.writeWord2VecModel(vec, "pathToWriteto.txt");

        log.info("Save vectors....");
        WordVectorSerializer.writeFullModel(vec, "pathToSaveModel.txt");
    }

    public void Word2vecTest() throws FileNotFoundException {
        log.info("Closest Words:");
        Collection<String> lst = word2Vec.wordsNearest("김치", 10);
        System.out.println(lst);
        UIServer server = UIServer.getInstance();
        System.out.println("Started on port " + server.getPort());

        double cosSim = word2Vec.similarity("김치", "양파");
        System.out.println(cosSim);
        //output: 0.7704452276229858

        Collection<String> lst3 = word2Vec.wordsNearest("양파", 10);
        System.out.println(lst3);
        //output: [director, company, program, former, university, family, group, such, general]

//        log.info("Plot TSNE....");
//        BarnesHutTsne tsne = new BarnesHutTsne.Builder()
//                .setMaxIter(1000)
//                .stopLyingIteration(250)
//                .learningRate(500)
//                .useAdaGrad(false)
//                .theta(0.5)
//                .setMomentum(0.5)
//                .normalize(true)
//                .build();
//        word2Vec.lookupTable().plotVocab(tsne);
    }

    public List<String> Word2vecRecommend(ingredientList list, Integer uriSize) {
        List<vectorTitle> vecAndTitles = new ArrayList<>();
        List<String> res = new ArrayList<>();

        try{
            //파일 객체 생성
            File file = new File("recipeList.txt");
            BufferedReader inFile = new BufferedReader(new FileReader(file));
            String sLine = null;
            List<String> reqIngList = list.getList();

            int cnt = 0;
            while( (sLine = inFile.readLine()) != null ){
                if(cnt % 1000 == 0)
                    System.out.println("Cnt: " + cnt);
                cnt++;
                Double sum = 0.0;
                List<String> recipeList = new ArrayList<String>(Arrays.asList(sLine.split(" ")));
                String title = recipeList.remove(0);
                for(String ingredient : reqIngList){
                    sum += getMaxSimilarity(recipeList, ingredient);
                }
                vecAndTitles.add(new vectorTitle(sum, title));
            }

            vecAndTitles.sort(new Comparator<vectorTitle>() {
                @Override
                public int compare(vectorTitle v1, vectorTitle v2) {
                    return v2.vector.compareTo(v1.vector);
                }
            });

            for(int i = 0; i < uriSize; i++){
                res.add(vecAndTitles.get(i).title +" : "+vecAndTitles.get(i).vector);
            }
            inFile.close();

            return res;
        }catch (FileNotFoundException e) {
            // TODO: handle exception
        }catch(IOException e){
            System.out.println(e);
        }
        return null;
    }

    public List<String> Word2vecRecommend2(String list, Integer uriSize) {
        List<vectorTitle> vecAndTitles = new ArrayList<>();
        List<String> res = new ArrayList<>();

        Collection<String> regIngCollection = Splitter.on(' ').splitToList(list);
        INDArray regIngVec = word2Vec.getWordVectorsMean(regIngCollection);
        for(UrlINDArray urlINDArray :urlINDArrays){
            Double recipeSimilarity = null;
            try{
                recipeSimilarity = Transforms.cosineSim(urlINDArray.vectorSum, regIngVec);
            }catch(Exception e) {
                e.printStackTrace();
            }
            if(recipeSimilarity != null)
                vecAndTitles.add(new vectorTitle(recipeSimilarity, urlINDArray.title));
        }

        vecAndTitles.sort(new Comparator<vectorTitle>() {
            @Override
            public int compare(vectorTitle v1, vectorTitle v2) {
                return v2.vector.compareTo(v1.vector);
            }
        });

        for(int i = 0; i < uriSize; i++){
            res.add(vecAndTitles.get(i).title);
        }
        return res;
    }

    private Double getMaxSimilarity(List<String> ingredientList, String ingredient){
        Double Max = 0.0;
        for(String ingre : ingredientList){
            Double similarity = word2Vec.similarity(ingre, ingredient);
            if(similarity > Max) Max = similarity;
        }
        return Max;
    }

    public static class vectorTitle {
        public final Double vector;
        public final String title;

        public vectorTitle(Double vector, String title) {
            this.vector = vector;
            this.title = title;
        }
    }

    public static class UrlINDArray {
        public final String title;
        public final INDArray vectorSum;

        public UrlINDArray(String title, INDArray vectorSum) {
            this.title = title;
            this.vectorSum = vectorSum;
        }
    }
}