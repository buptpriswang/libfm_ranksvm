package ict.edu.learning.test;

import ict.edu.learning.logisticRankSVM.LogisticRankSVM;
import ict.edu.learning.measure.Measurement;
import ict.edu.learning.utilities.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ciir.umass.edu.features.Normalizer;
import ciir.umass.edu.features.SumNormalizor;
import ciir.umass.edu.features.ZScoreNormalizor;
import ciir.umass.edu.learning.DataPoint;
import ciir.umass.edu.learning.Matrix;
import ciir.umass.edu.learning.PartialPair;
import ciir.umass.edu.learning.PartialPairList;
import ciir.umass.edu.learning.RankList;
import ciir.umass.edu.learning.Vector;

public class Test_w {

	/**
	 * @param args
	 * @throws IOException 
	 */
	static String filename = null;
	static String validationFile =null;
	static String testFile = null;
	static String trainFile = null;
	public static boolean normalize = false;
	public static Normalizer nml = new SumNormalizor();
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub		
		for(int i=0;i<args.length;i++)
		{
			if(args[i].compareTo("-filename")==0)
				filename = args[++i];
			else if(args[i].compareTo("-train")==0)
				trainFile = args[++i];
			else if(args[i].compareTo("-validate")==0)
				validationFile = args[++i];
			else if(args[i].compareTo("-test")==0)
				testFile = args[++i];	
		}		
		LogisticRankSVM lrs = new LogisticRankSVM();
		List<RankList> rll_train = lrs.readInput(trainFile);//read input
		Matrix.RowsOfVMatrix = RowSize_V(rll_train);
		Vector.setVectorSize(DataPoint.featureCount+1);
//		List<Matrix> ml = FileUtils.readFromFileGetMatrixList(filename);		
//		Vector w = getW(rll_train ,ml.get(ml.size()-1));
		Vector w = FileUtils.readFromFileGetVector(filename);
		List<RankList> rll_validation = null;
              if(validationFile.compareTo("")!=0)
			rll_validation = lrs.readInput(validationFile);
		List<RankList> rll_test = null;
		if(testFile.compareTo("")!=0)
			rll_test = lrs.readInput(testFile);		
				
		String fold_n = (String) trainFile.subSequence(trainFile.indexOf("Fold"),
				trainFile.indexOf("Fold")+5);
		StringBuffer sb = new StringBuffer();
		/*Matrix V =ml.get(ml.size()-1);//选择最新训练出来的matrix
		Vector w = getW(rll_train, V);*/
		String dir = "output_data/factorizedLR/prediction/" + fold_n;
		makeDir(dir);
		List<ArrayList<Double>> dll_train1 = getScoreByFun(rll_train,w);
		FileUtils.write2File(dir + "/prediction_train.txt", dll_train1, "");
		List<ArrayList<Double>> dll_vali1 = getScoreByFun(rll_validation,w);
		FileUtils.write2File(dir + "/prediction_validation.txt", dll_vali1, "");
		List<ArrayList<Double>> dll_test1 = getScoreByFun(rll_test,w);
		FileUtils.write2File(dir + "/prediction_test.txt", dll_test1, "");
//		for (int j = 0; j < ml.size(); j++) {
//			sb.append("--------------for matrix "+j+"--------------").append(System.getProperty("line.separator"));
//			Vector w_ite = getW(rll_train, w);
		/*	List<ArrayList<Double>> dll_train = getScoreByFun(rll_train,w);
			List<ArrayList<Double>> dll_vali = getScoreByFun(rll_validation,w);
			List<ArrayList<Double>> dll_test = getScoreByFun(rll_test,w);
			double map1 = Measurement.MAP(dll_train, rll_train);
			double map2 = Measurement.MAP(dll_vali, rll_validation);
			double map3 = Measurement.MAP(dll_test, rll_test);
			
			sb.append("MAP").append(System.getProperty("line.separator"));
			sb.append("\t train"+"\t validation" +"\t test").append(System.getProperty("line.separator"));
			sb.append("\t" + map1 + "\t" + map2 +"\t" + map3).append(System.getProperty("line.separator"));
			sb.append("NDCG").append(System.getProperty("line.separator"));
			sb.append("\t train"+"\t validation" +"\t test").append(System.getProperty("line.separator"));
			System.out.println("map for train:vili:test:" + map1 + ":" + map2 +":" + map3);
			for (int i = 1; i <= 10; i++) {
				double ndcg_1 = Measurement.NDCG(dll_train, rll_train,i);
				double ndcg_2 = Measurement.NDCG(dll_vali, rll_validation,i);
				double ndcg_3 = Measurement.NDCG(dll_test, rll_test,i);
				sb.append(i+"\t"+ndcg_1+"\t"+ndcg_2+"\t"+ndcg_3).append(System.getProperty("line.separator"));			
			}	*/		
//		}
		/*FileUtils.write2File("output_data/afterLearningMatrixV/evaluation.txt", sb, "");*/
		System.out.println("test.main() is over");
	}
	private static int RowSize_V(List<RankList> rll) {
		int total = 0;		
		for (int i = 0; i < rll.size(); i++) {			
			total += rll.get(i).size();
		}
		return total;
	}	
	public static String makeDir(String tail) {  
	    String[] sub = tail.split("/");  
	    File dir = new File(".");  
	    for (int i = 0; i < sub.length; i++) {  
	        if (!dir.exists()) {  
	            dir.mkdir();  
	        }  
	        File dir2 = new File(dir + File.separator + sub[i]);  
	        if (!dir2.exists()) {  
	            dir2.mkdir();  
	        }  
	        dir = dir2;  
	    }  
	    return dir.toString();  
	}
	public static List<ArrayList<Double>> getScoreByFun(List<RankList> rll,Vector w){
		List<ArrayList<Double>> dll = new ArrayList<ArrayList<Double>>();
//		List<PartialPairList> ppll = getPartialPairForAllQueries(rll);		
	//	Vector w = getW(rll, matrixV);
		for (int i = 0; i < rll.size(); i++) {
			ArrayList<Double> dl = new ArrayList<Double>();
			for (int j = 0; j < rll.get(i).size(); j++) {
				Vector x_ij = new Vector(rll.get(i).get(j).getFeatureVector());
				double scoreByFun = Vector.dotProduct(w, x_ij);
				dl.add(scoreByFun);
			}
			dll.add(dl);
		}
		return dll;		
	}
	public static Vector getW(List<RankList> rll, Matrix matrixV){
		HashMap<String, Integer> hp_V = getRowIDofVMatrix(rll);
		List<PartialPairList> ppll = getPartialPairForAllQueries(rll);		
		Vector w = new Vector(Matrix.getColsOfVMatrix());
		for (int i = 0; i < ppll.size(); i++) {
			for (int j = 0; j < ppll.get(i).size(); j++) {
				PartialPair pp = ppll.get(i).get(j);
				String qid = pp.getQueryID();
				String largeDocID = qid + "-" + pp.getLargeDocID();
				String smallDocID = qid + "-" + pp.getSmallDocID();
				int v_iq = hp_V.get(largeDocID);
				int v_jq = hp_V.get(smallDocID);
				double factor = matrixV.getInnerProduct(v_iq, v_jq);
				double [] temp = Matrix.multiplyRowVector(factor, pp.getPartialFVals());
				w = Vector.addition(w, new Vector(temp));
			}
		}
		return w;
	}
	public static List<PartialPairList> getPartialPairForAllQueries(List<RankList> rll)
	{
		List<PartialPairList> ppll =new ArrayList<PartialPairList>();
		//int num=0;
		for (int i = 0; i < rll.size(); i++) {
			PartialPairList tem = getPartialPairForOneQuery(rll.get(i));
			ppll.add(tem);
			//num++;
		}
		//System.out.println(num);
		return ppll;
	}
	public static PartialPairList getPartialPairForOneQuery(RankList rl)//rl holds all documents for one query 
	{
		PartialPairList ppl = new PartialPairList();
		for (int i = 0; i < rl.size(); i++) {
			for (int j = i+1; j < rl.size(); j++) {
				if(rl.get(i).getLabel()!=(rl.get(j).getLabel())){
					ppl.add(new PartialPair(rl.get(i),rl.get(j)));
				}
			}
		}
		return ppl;
		
	}
public static HashMap<String, Integer>  getRowIDofVMatrix(List<RankList> rll){
		
		HashMap<String, Integer> hp = new HashMap<String, Integer>();
		int index = 0;
		for (int i = 0; i < rll.size(); i++) {
			for (int j = 0; j < rll.get(i).size(); j++) {				
				String key=rll.get(i).get(j).getID() + "-" + rll.get(i).get(j).getDocID();
				
				hp.put(key, index);
				index++;
			}
		}
		return hp;
		
	}

}
