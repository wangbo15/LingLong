package edu.pku.sei.conditon.dedu.writer;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.pku.sei.conditon.dedu.feature.CondVector;
import edu.pku.sei.conditon.dedu.feature.Predicate;
import edu.pku.sei.conditon.util.FileUtil;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.csv.CSVChecker;

public class AllPredWriter extends Writer {

	private List<CondVector> plainCondVec;
	
	public AllPredWriter(String outFilePrefix, List<CondVector> plainCondVec) {
		super(outFilePrefix);
		this.plainCondVec = plainCondVec;
	}
	
	public final static String getAllPredHeader() {
		List<String> header = new ArrayList<>();
		header.add("id");
		header.add("pred");
		header.add("posnum");
		header.add("tps");
		header.add("vars");
		header.add("oricond");
		String str = StringUtil.join(header, del);
		return str;
	}

	@Override
	public void write() {
		writeAllPred();
	}
	
	private void writeAllPred(){
		String allPredPath = outFilePrefix + ".allpred.csv";
		FileOutputStream fos = null;
		BufferedOutputStream bs = null;
		//allpred
		try {
			fos = new FileOutputStream(allPredPath, false);
			bs = new BufferedOutputStream(fos);

			String header = getAllPredHeader() + "\n";
			bs.write(header.getBytes());
			
			for(CondVector vec: plainCondVec) {
				
				if(!CONFIG.isPredAll() && omit(vec)) {
					continue;
				}
				
				/*for all expr*/
				Predicate pred = vec.getPredicate();
				
				if(!CONFIG.isPredAll() && pred.getSlopNum() == 0) {
					continue;
				}
				
				String exprForCsv = predForCSV(pred);
				List<String> allPredList = new ArrayList<>();
				allPredList.add("" + vec.getId());
				allPredList.add(exprForCsv);
				allPredList.add(""+ pred.getSlopNum());
				allPredList.add(pred.getSlopTypes().toString());
				allPredList.add(pred.getOriSlopVars().toString());
				allPredList.add(pred.getOriLiteral());
				
				String allPredLine = StringUtil.join(allPredList, del) + "\n";
				bs.write(allPredLine.getBytes());
				bs.flush();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.closeInputStream(bs, fos);
		}
		CSVChecker.checkAllCSV(allPredPath);
	}

}
